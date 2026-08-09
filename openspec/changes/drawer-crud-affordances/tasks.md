## 1. Structural Migration (App.kt)

- [ ] 1.1 Remove `ModalNavigationDrawer` wrapping logic from `App.kt`
- [ ] 1.2 Implement root `Row` layout to house the Rail and the main `Scaffold`
- [ ] 1.3 Implement `animateDpAsState` for the rail width based on `appContext.isDrawerOpen`
- [ ] 1.4 Add `NavigationRail` (collapsed state) to the side of the expanded workspace view

## 2. Component Adaptation (FileDrawerContent)

- [ ] 2.1 Update `FileDrawerContent` to handle variable widths gracefully
- [ ] 2.2 Add a "Collapse" toggle icon to the `FileDrawerHeader` when expanded
- [ ] 2.3 Ensure `systemBarsPadding` is applied correctly at the root of the Rail

## 3. Navigation Integration

- [ ] 3.1 Synchronize Rail navigation icons (Editor, AI) with the current `AppState`
- [ ] 3.2 Ensure the Bottom Navigation Bar correctly toggles the Rail expansion
- [ ] 3.3 Verify that clicking a file in the expanded rail still switches to the Editor tab

## 4. Verification

- [ ] 4.1 Verify smooth animation of rail expansion
- [ ] 4.2 Verify layout correctly pushes content without clipping
- [ ] 4.3 Verify window insets are respected in both states
