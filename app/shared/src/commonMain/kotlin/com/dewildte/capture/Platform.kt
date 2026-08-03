package com.dewildte.capture

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform