package com.dewildte.capture

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class TextProcessorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_PROCESS_TEXT) {
            val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
            val isReadOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)

            // Perform your action here (e.g., translation, capitalization)
            val processedText = processMyText(selectedText)

            if (!isReadOnly) {
                // Return the modified text back to the text field
                val resultIntent = Intent().apply {
                    putExtra(Intent.EXTRA_PROCESS_TEXT, processedText)
                }
                setResult(RESULT_OK, resultIntent)
            }
            finish()
        }
    }

    private fun processMyText(text: String): String {
        return text.uppercase() // Example action
    }
}
