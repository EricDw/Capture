## Why

The current Modal Navigation Drawer is being phased out in favor of more adaptive and expressive components in Material 3. The "Expanding Navigation Rail" provides a more modern, flexible way to manage workspace content and primary navigation, especially on larger screens, while remaining functional on mobile.

## What Changes

- **Replace `ModalNavigationDrawer`**: Transition the top-level UI from a modal overlay to a side-anchored `NavigationRail` that can expand and collapse.
- **Push-to-Layout Transition**: Instead of overlaying content, the expanded rail will now push the main screen content to the right (or stay fixed depending on screen size).
- **Consolidated Navigation**: Integrate primary navigation icons (Editor, AI, Settings) into the Rail's collapsed state.
- **Enhanced `FileDrawerContent`**: Adapt the workspace view to work seamlessly within the Expanding Rail framework.
- **Adaptive Bottom Bar**: Maintain the bottom bar for mobile-first navigation while syncing its state with the rail.

## Capabilities

### New Capabilities
- `workspace/expanding-rail-navigation`: Implements an adaptive side-rail that transitions between icon-only and full-workspace views.

### Modified Capabilities
- `workspace/file-management`: Update to reflect that management now occurs within the Expanding Rail.

## Impact
- `App.kt`: Structural change from `ModalNavigationDrawer` to `Row` + `Animated Rail`.
- `FileDrawerContent.kt`: UI refinements for the rail container.
- `AppContext`: State mapping from `isDrawerOpen` to `isRailExpanded`.
