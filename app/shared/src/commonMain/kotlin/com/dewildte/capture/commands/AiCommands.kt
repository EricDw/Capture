package com.dewildte.capture.commands

import com.dewildte.capture.data.Conversation

data class SendAiMessage(val message: String, val conversation: Conversation) : Command
data object StopAiGeneration : Command
data object ClearActivePermissionRequest : Command
