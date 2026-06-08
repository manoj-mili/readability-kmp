plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.androidLint)
    `maven-publish`
}

group = providers.gradleProperty("GROUP").orElse("com.mili.readability").get()
version = providers.gradleProperty("VERSION_NAME").orElse("0.1.0-alpha01").get()

val generatedReadabilityDir =
    layout.buildDirectory.dir("generated/readability/src/commonMain/kotlin")

val embedReadabilityJs by tasks.registering {
    group = "reader"
    description = "Embeds Readability.js into commonMain as a Kotlin string"

    val inputJs = file("src/commonMain/resources/readability/Readability.js")
    val outputKt =
        generatedReadabilityDir.map {
            it.file("com/mili/readability/core/ReadabilityBundled.kt")
        }

    inputs.file(inputJs)
    outputs.file(outputKt)

    doLast {
        require(inputJs.exists()) {
            "Readability.js not found at ${inputJs.absolutePath}"
        }

        val js = inputJs.readText()
        // Chunk size must be < 65535 bytes (UTF-8) for Java string constants.
        val chunkSize = 15000
        val chunks = js.chunked(chunkSize)
        val outputFile = outputKt.get().asFile

        outputFile.parentFile.mkdirs()

        val stringBuilderContent = StringBuilder()
        stringBuilderContent.append("package com.mili.readability.core\n\n")
        stringBuilderContent.append("/**\n")
        stringBuilderContent.append(" * AUTO-GENERATED FILE - DO NOT EDIT\n")
        stringBuilderContent.append(" */\n")
        stringBuilderContent.append("internal object ReadabilityBundled {\n")
        stringBuilderContent.append("    val READABILITY_JS: String = buildString {\n")

        for (chunk in chunks) {
            val escapedChunk = chunk.replace("$", "\${'$'}")
            stringBuilderContent.append("        append(\"\"\"$escapedChunk\"\"\")\n")
        }

        stringBuilderContent.append("    }\n")
        stringBuilderContent.append("}\n")

        outputFile.writeText(stringBuilderContent.toString())
    }
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "com.mili.readability.core"
        compileSdk {
            version = release(36) { minorApiLevel = 1 }
        }
        minSdk = 28

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "ReadabilityCore"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            kotlin.srcDir(generatedReadabilityDir)

            dependencies {
                implementation(libs.kotlin.stdlib)
                // Add KMP dependencies here
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.testExt.junit)
            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }
    }

}

tasks
    .matching { task -> task.name.startsWith("compile") }
    .configureEach {
        dependsOn(embedReadabilityJs)
    }

tasks
    .matching { task -> task.name.endsWith("sourcesJar", ignoreCase = true) }
    .configureEach {
        dependsOn(embedReadabilityJs)
    }

publishing {
    repositories {
        maven {
            name = "localBuild"
            url = layout.buildDirectory.dir("repo").get().asFile.toURI()
        }
    }

    publications.withType<MavenPublication>().configureEach {
        pom {
            name = "Readability Core"
            description = "Kotlin Multiplatform reader-mode SDK core built around Mozilla Readability."
            url = "https://github.com/manoj-mili/readability-kmp"
            licenses {
                license {
                    name = "Apache License, Version 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0"
                }
            }
            developers {
                developer {
                    id = "mili"
                    name = "Mili"
                }
            }
            scm {
                url = "https://github.com/manoj-mili/readability-kmp"
                connection = "scm:git:https://github.com/manoj-mili/readability-kmp.git"
                developerConnection = "scm:git:ssh://git@github.com/manoj-mili/readability-kmp.git"
            }
        }
    }
}
