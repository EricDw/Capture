package com.dewildte.capture.utils

import com.dewildte.capture.commands.LogMessage
import com.dewildte.capture.data.LogData
import com.dewildte.capture.data.LogLevel

fun Actor.tellVerboseLog(tag: String, message: String, error: Throwable? = null) {
    val logData = LogData(
        level = LogLevel.VERBOSE,
        tag = tag,
        message = message,
        error = error,
    )
    tell(LogMessage(logData))
}

fun Actor.tellDebugLog(tag: String, message: String, error: Throwable? = null) {
    val logData = LogData(
        level = LogLevel.DEBUG,
        tag = tag,
        message = message,
        error = error,
    )
    tell(LogMessage(logData))
}

fun Actor.tellInfoLog(tag: String, message: String, error: Throwable? = null) {
    val logData = LogData(
        level = LogLevel.INFO,
        tag = tag,
        message = message,
        error = error,
    )
    tell(LogMessage(logData))
}

fun Actor.tellWarningLog(tag: String, message: String, error: Throwable? = null) {
    val logData = LogData(
        level = LogLevel.WARN,
        tag = tag,
        message = message,
        error = error,
    )
    tell(LogMessage(logData))
}

fun Actor.tellErrorLog(tag: String, message: String, error: Throwable? = null) {
    val logData = LogData(
        level = LogLevel.ERROR,
        tag = tag,
        message = message,
        error = error,
    )
    tell(LogMessage(logData))
}

fun Actor.tellWtfLog(tag: String, message: String, error: Throwable? = null) {
    val logData = LogData(
        level = LogLevel.WTF,
        tag = tag,
        message = message,
        error = error,
    )
    tell(LogMessage(logData))
}