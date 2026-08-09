package com.dewildte.capture.ai

import com.google.ai.edge.litertlm.Tool
import com.google.ai.edge.litertlm.ToolSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SystemTools : ToolSet {

    @Tool(description = "Returns the current date and time. Use this when the user mentions relative dates like 'today', 'yesterday', or 'next week'.")
    fun getCurrentDateTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy HH:mm:ss")
        return "The current date and time is: ${current.format(formatter)}"
    }
}
