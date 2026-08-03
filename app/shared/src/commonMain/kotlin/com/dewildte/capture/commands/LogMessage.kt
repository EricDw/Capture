package com.dewildte.capture.commands

import com.dewildte.capture.data.LogData

data class LogMessage(
    val logData: LogData
): Command
