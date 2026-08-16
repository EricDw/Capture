package com.dewildte.capture.utils

import com.dewildte.capture.data.TextFile
import kotlin.test.Test
import kotlin.test.assertEquals

class SnippetParserTest {

    @Test
    fun parse_splits_by_delimiter() {
        val content = "Snippet 1\n\n\nSnippet 2\n\n\nSnippet 3"
        val result = SnippetParser.parse(content)
        
        assertEquals(3, result.size)
        assertEquals("Snippet 1", result[0])
        assertEquals("Snippet 2", result[1])
        assertEquals("Snippet 3", result[2])
    }

    @Test
    fun parse_trims_whitespace() {
        val content = "  Snippet 1  \n\n\n  Snippet 2  "
        val result = SnippetParser.parse(content)
        
        assertEquals(2, result.size)
        assertEquals("Snippet 1", result[0])
        assertEquals("Snippet 2", result[1])
    }

    @Test
    fun parse_filters_empty_snippets() {
        val content = "Snippet 1\n\n\n\n\n\nSnippet 2"
        val result = SnippetParser.parse(content)
        
        assertEquals(2, result.size)
        assertEquals("Snippet 1", result[0])
        assertEquals("Snippet 2", result[1])
    }
    
    @Test
    fun parse_textFile_works() {
        val file = TextFile(contents = "S1\n\n\nS2")
        val result = SnippetParser.parse(file)
        
        assertEquals(2, result.size)
        assertEquals("S1", result[0])
        assertEquals("S2", result[1])
    }
}
