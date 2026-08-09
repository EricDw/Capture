# Delta Spec: File Navigation Drawer UI Component

## ADDED Requirements

### Requirement: Vertical Accordion Expansion
The file navigation drawer MUST expand nested folder contents vertically directly beneath the folder item rather than applying deep horizontal indentation, preserving drawer text visibility.

### Requirement: Material 3 Navigation Drawer Tokens
The component MUST use `MaterialTheme.colorScheme` tokens (`surface`, `surfaceVariant`, `primaryContainer`, `onPrimaryContainer`, `outline`) and Material 3 typography (`labelMedium`, `bodySmall`, `titleSmall`).

### Requirement: File Extension Icons
Files MUST display distinct contextual icons or badge indicators based on extension (`.kt`, `.md`, `.txt`, `.json`, `.png` / images, default file).

### Requirement: Standalone Preview
The component MUST include a `@Preview` composable containing realistic sample workspace file hierarchies so it can be previewed without app runtime wiring.
