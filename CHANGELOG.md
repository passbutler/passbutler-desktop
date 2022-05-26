# Changelog

## [1.1.0] - UNRELEASED

### Added
- Icon button on item detail screen to quickly copy username, password or URL to clipboard without leaving the screen
- Copy icon in the password generation dialog to get random passwords without applying them
- Copy icon for the UUID of an item on the item detail screen

### Changed
- Show a default confirm dialog for closing a vault because it's not a dangerous action
- Disable item details of read-only items again (because copy button is available)

### Known issues
- If a screen has a default and cancel button (reacting to "ENTER" and "ESC") and a dialog is shown, the button behavior of the screen buttons not working anymore after dismissing the dialog
- The "logged out" about screen is not yet scrollable

## [1.0.0] - 2022-01-21

### Added
- Privacy policy link on about screen

### Known issues
- If a screen has a default and cancel button (reacting to "ENTER" and "ESC") and a dialog is shown, the button behavior of the screen buttons not working anymore after dismissing the dialog
- The "logged out" about screen is not yet scrollable

## [1.0.0-Preview2] - 2021-10-10

### Added
- Password unmasking icon for password fields
- Language hint text when importing CSV from KeePassX 2 / KeePassXC to be sure deleted entries are recognized as such
- Pressing escape key on overview clears filter now
- Recent vault files menu
- Support of other operating systems than Linux (different configuration directory locations)
- Confirmation dialogs for destructive actions (close vault and item deletion)
- Button to copy recycle bin name string to clipboard in the import KeePassX 2 section
- Confirmation dialog for remove Premium Key action
- Confirmation dialog for discard item and item authorization changes action
- Password generator dialog on item screen
- Shortcut for saving on item detail and item authorizations screen
- Imprint to about screen and buttons to show screen from multiple places

### Changed
- Allow copying item details of read-only items (do not disable them anymore) – the save and delete button is hidden instead of disabled to avoid user confusion
- The filter view is focused automatically on overview
- Automatically focus first element if the filter is active
- Disabled horizontal scrolling of notes text area
- Show dedicated error for entering wrong invitation code when try to register user
- Icon accent coloring to archive more beautiful list style
- Introduction flow to be more understandable / user-friendly

### Fixed
- The case of items is ignored for sorting on item and item authorization screens
- Wrong locale of application menu (system default instead of configured locale)
- Warping of some icons
- Non-working open URL action on Linux if the URL scheme was missing because `xdg-open` interpreted the URL as a local file

### Known issues
- If a screen has a default and cancel button (reacting to "ENTER" and "ESC") and a dialog is shown, the button behavior of the screen buttons not working anymore after dismissing the dialog

## [1.0.0-Preview1] - 2021-02-02

### Added
- Initial release
