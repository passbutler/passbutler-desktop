# Changelog

## [1.0.0] - UNRELEASED

### Added
- Password unmasking icon for password fields
- Language hint text when importing CSV from KeePassX 2 / KeePassXC to be sure deleted entries are recognized as such
- Pressing escape key on overview clears filter now
- Recent vault files menu
- Support of other operating systems than Linux (different configuration directory locations)
- Confirmation dialogs for destructive actions (close vault and item deletion)
- Button to copy recycle bin name string to clipboard in the import KeePassX 2 section

## Changed
- Allow to copy item details of read-only items (do not disable them anymore) - the save and delete button is hidden instead of disabled to avoid user confusion
- The filter view is focused automatically on overview
- Automatically focus first element if the filter is active

### Fixed
- The case of items is ignored for sorting on item and item authorization screens
- Wrong locale of application menu (system default instead of configured locale)
- Warping of some icons

## [1.0.0-Preview1] - 2021-02-02

### Added
- Initial release
