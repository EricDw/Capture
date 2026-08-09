package com.dewildte.capture.commands

import com.dewildte.capture.data.ModelInfo

data object LoadAvailableModels : Command
data class SwitchModel(val model: ModelInfo) : Command
data class DeleteModel(val model: ModelInfo) : Command

data object SaveSettings : Command

data object SignInWithGoogle : Command
data object SignOutWithGoogle : Command
data class UpdateGoogleClientId(val clientId: String) : Command
