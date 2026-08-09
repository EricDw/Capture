## Purpose

Provides a suite of capabilities for users to manage their workspace content, including creating new files and folders, renaming existing items, and safely deleting content from the local file system.

## ADDED Requirements

### Requirement: Root-level creation actions
The workspace drawer SHALL provide persistent entry points at the top level for creating new files and folders within the root of the active workspace.

#### Scenario: Create new file at root
- **WHEN** the user triggers the "New File" action in the drawer header
- **THEN** the system SHALL prompt for a filename and create a new empty file at the workspace root upon confirmation

#### Scenario: Create new folder at root
- **WHEN** the user triggers the "New Folder" action in the drawer header
- **THEN** the system SHALL prompt for a folder name and create a new directory at the workspace root upon confirmation

### Requirement: Contextual node management
The system SHALL allow users to perform management actions (Rename, Delete) on any file or folder displayed in the drawer via a long-press interaction.

#### Scenario: Rename a file
- **WHEN** the user long-presses a file and selects "Rename"
- **THEN** the system SHALL prompt for a new name and update the file name on disk while maintaining the same content

#### Scenario: Delete a file
- **WHEN** the user long-presses a file and selects "Delete"
- **THEN** the system SHALL request confirmation and permanently remove the file from the storage upon approval

### Requirement: Contextual folder-specific creation
Folders SHALL provide additional contextual actions for creating new items directly within their own subdirectory.

#### Scenario: Create new file inside a folder
- **WHEN** the user long-presses a folder and selects "New File here"
- **THEN** the system SHALL prompt for a name and create the file as a direct child of that folder

#### Scenario: Create new subfolder
- **WHEN** the user long-presses a folder and selects "New Subfolder here"
- **THEN** the system SHALL prompt for a name and create the subfolder within that folder
