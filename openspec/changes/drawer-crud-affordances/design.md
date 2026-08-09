## Context

Material 3 Expressive guidelines recommend an "Expanding Navigation Rail" over the traditional `ModalNavigationDrawer` for apps with significant side-anchored content. This provides better visibility of the workspace and allows for smoother transitions between navigation and file management.

## Goals / Non-Goals

**Goals:**
- Implement a smooth, animated expansion from a 80dp rail to a 360dp workspace view.
- Maintain consistency with existing `Scaffold` and `NavigationBar` logic.
- Ensure the layout is responsive to window insets.

**Non-Goals:**
- Creating a separate library for the Rail (keep it within the Capture app).
- Implementing a right-side rail (leading edge only).

## Decisions

### 1. Root Layout: Row-Scaffold Pattern
- **Decision**: The root of the app will be a `Row` containing the Rail and a `Box` wrapping the `Scaffold`.
- **Rationale**: This is the standard pattern for side-navigation in Compose that pushes content.

### 2. Animation: `animateDpAsState` for Rail Width
- **Decision**: Use `animateDpAsState` to control the width of the `FileDrawerContent` container.
- **Rationale**: This provides a more fluid and precise control over the layout shift compared to `AnimatedVisibility`, which can sometimes cause sudden content jumps in parent rows.

### 3. Navigation Unification
- **Decision**: The Bottom Navigation Bar will remain for mobile but will be synced with the Rail's state. On larger screens (future proofing), we can hide the bottom bar and use the rail exclusively.

## Risks / Trade-offs

- **[Risk] Screen Real Estate on Small Phones** → **Mitigation**: On very narrow devices, the expanded rail might still feel like it's taking up a lot of space. We will ensure the animation is fast and the "Close" triggers (tapping the icon again) are obvious.
- **[Risk] Layout Complexity** → **Mitigation**: Centralizing the width logic in `App.kt` and passing it down to the `FileDrawerContent`.
