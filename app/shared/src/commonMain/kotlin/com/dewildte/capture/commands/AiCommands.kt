package com.dewildte.capture.commands

data class SendAiMessage(val message: String) : Command
data object StopAiGeneration : Command
