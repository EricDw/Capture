## Purpose

Defines the behavior of the Expanding Navigation Rail, which serves as the primary side-anchored container for workspace management and global application navigation.

## ADDED Requirements

### Requirement: Adaptive side-anchored container
The system SHALL replace the `ModalNavigationDrawer` with a side-anchored container that resides on the leading edge of the screen.

#### Scenario: Rail occupies permanent layout space
- **WHEN** the rail is visible
- **THEN** it SHALL push the main application content to the side rather than overlaying it.

### Requirement: Expanding and Collapsing states
The rail SHALL support two distinct visual states: Collapsed and Expanded.

#### Scenario: Collapsed state
- **WHEN** the rail is collapsed
- **THEN** it SHALL show only icons for primary destinations (Editor, AI, Workspace).

#### Scenario: Expanded state
- **WHEN** the rail is expanded
- **THEN** it SHALL show the full workspace file tree and associated management actions.

### Requirement: State synchronization
The rail expansion SHALL be toggled by the primary "Menu" action (Workspace) in the navigation bar.

#### Scenario: Toggling from bottom bar
- **WHEN** the user taps the Workspace icon in the bottom navigation bar
- **THEN** the rail SHALL toggle between its collapsed and expanded states.
