package com.dewildte.capture.events

class FailedToLoadSelectedFile(
    val cause: Throwable? = null,
): Event