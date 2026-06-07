package com.mili.readabilitykmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform