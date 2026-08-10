package com.dewildte.capture

import com.dewildte.capture.events.ToggleDrawerClicked
import com.dewildte.capture.navigation.AppRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppContextImplTest {

    @Test
    fun initial_state_is_correct() {
        val appContext = AppContextImpl()
        assertFalse(appContext.isDrawerOpen)
        assertEquals(AppRoute.Editor, appContext.navBackStack.last())
    }

    @Test
    fun toggling_drawer_updates_isDrawerOpen() {
        val appContext = AppContextImpl()
        appContext.tell(ToggleDrawerClicked)
        assertTrue(appContext.isDrawerOpen)
        appContext.tell(ToggleDrawerClicked)
        assertFalse(appContext.isDrawerOpen)
    }

    @Test
    fun navBackStack_handles_multiple_routes() {
        val appContext = AppContextImpl()
        val mutableBackStack = appContext.navBackStack as MutableList<AppRoute>
        mutableBackStack.add(AppRoute.Settings)
        assertEquals(2, appContext.navBackStack.size)
        assertEquals(AppRoute.Settings, appContext.navBackStack.last())
    }
}
