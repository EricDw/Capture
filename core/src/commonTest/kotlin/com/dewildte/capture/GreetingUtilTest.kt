package com.dewildte.capture

import kotlin.test.Test
import kotlin.test.assertEquals

class GreetingUtilTest {

    @Test
    fun sayHello_returns_correct_greeting() {
        val result = sayHello("World")
        assertEquals("Hello, World!", result)
    }
}
