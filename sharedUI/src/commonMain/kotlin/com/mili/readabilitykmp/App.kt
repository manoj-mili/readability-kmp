package com.mili.readabilitykmp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private sealed interface SampleScreen {
    data object Home : SampleScreen
    data object TestLinks : SampleScreen
    data class Reader(val url: String) : SampleScreen
}

private data class TestLink(
    val title: String,
    val url: String,
)

private val testLinks = listOf(
    TestLink("Wikipedia: WebView", "https://en.wikipedia.org/wiki/WebView"),
    TestLink("Mozilla Blog", "https://blog.mozilla.org/en/"),
    TestLink("The Verge", "https://www.theverge.com/"),
    TestLink("Ars Technica", "https://arstechnica.com/"),
    TestLink("Android Developers", "https://developer.android.com/"),
)

@Composable
@Preview
fun App() {
    MaterialTheme {
        var screen by remember { mutableStateOf<SampleScreen>(SampleScreen.Home) }

        when (val current = screen) {
            SampleScreen.Home -> HomeScreen(
                onOpenTestLinks = { screen = SampleScreen.TestLinks },
            )
            SampleScreen.TestLinks -> TestLinksScreen(
                onBack = { screen = SampleScreen.Home },
                onOpenUrl = { screen = SampleScreen.Reader(it) },
            )
            is SampleScreen.Reader -> ReaderScreen(
                url = current.url,
                onBack = { screen = SampleScreen.TestLinks },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    onOpenTestLinks: () -> Unit,
) {
    val sampleState = remember { ReadabilityUiSample().state() }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = sampleState.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Shared Compose sample running on ${sampleState.platformName}",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onOpenTestLinks) {
            Text("Test Button")
        }
        ValidationCard(sampleState)
        ArticlePreviewCard(sampleState)
    }
}

@Composable
private fun TestLinksScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
) {
    var urlInput by rememberSaveable { mutableStateOf("") }
    val normalizedUrl = urlInput.trim()
    val isUrlValid = normalizedUrl.isHttpsUrl()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Test Links",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Button(onClick = onBack) {
                Text("Back")
            }
        }
        Text(
            text = "Open a preset article page, or enter any HTTPS URL to load in the platform WebView.",
            style = MaterialTheme.typography.bodyLarge,
        )
        testLinks.forEach { link ->
            LinkCard(
                link = link,
                onClick = { onOpenUrl(link.url) },
            )
        }
        HorizontalDivider()
        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("HTTPS URL") },
            placeholder = { Text("https://example.com/article") },
            singleLine = true,
            isError = normalizedUrl.isNotEmpty() && !isUrlValid,
            supportingText = {
                if (normalizedUrl.isNotEmpty() && !isUrlValid) {
                    Text("Only https:// URLs are supported in this sample.")
                }
            },
        )
        Button(
            onClick = { onOpenUrl(normalizedUrl) },
            enabled = isUrlValid,
        ) {
            Text("Open URL")
        }
    }
}

@Composable
private fun LinkCard(
    link: TestLink,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = link.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = link.url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ReaderScreen(
    url: String,
    onBack: () -> Unit,
) {
    var readabilityMode by rememberSaveable(url) { mutableStateOf(false) }
    var status by rememberSaveable(url) { mutableStateOf("Loading page") }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .safeContentPadding()
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(onClick = onBack) {
                    Text("Back")
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Readability")
                    Switch(
                        checked = readabilityMode,
                        onCheckedChange = {
                            readabilityMode = it
                            status = if (it) "Applying readability mode" else "Loading normal page"
                        },
                    )
                }
            }
            Text(
                text = url,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        PlatformReaderWebView(
            url = url,
            readabilityMode = readabilityMode,
            modifier = Modifier.fillMaxSize(),
            onStatusChanged = { status = it },
        )
    }
}

@Composable
private fun ValidationCard(sampleState: ReadabilityUiSampleState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "SDK validation: ${sampleState.statusLabel}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text("Mozilla Readability script length: ${sampleState.scriptLength} chars")
            HorizontalDivider()
            Text(
                text = "Reader mode states covered",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            sampleState.validatedStates.forEach { state ->
                Text("- $state")
            }
        }
    }
}

@Composable
private fun ArticlePreviewCard(sampleState: ReadabilityUiSampleState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = sampleState.sampleArticleTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = sampleState.sampleArticleExcerpt,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

private fun String.isHttpsUrl(): Boolean {
    return startsWith("https://") && length > "https://".length
}
