# Language Picker Design QA

## Evidence

- Source visual truth: `C:\Users\91nar\AppData\Local\Temp\codex-clipboard-4610fbb7-dbde-4347-b233-f1070fcaa7f1.png`
- Implementation, grouped view with flags: `D:\W\other project\smart calculator android app\app\build\qa\language-flags-settled.png`
- Implementation, Indian-language scroll state with flags: `D:\W\other project\smart calculator android app\app\build\qa\language-flags-bottom.png`
- Combined comparison: `D:\W\other project\smart calculator android app\app\build\qa\language-flags-comparison.png`
- Viewport: Android emulator, 1080 x 2400 px, 420 dpi, dark theme, Gujarati app locale.
- Comparison normalization: the 170 x 910 px source was scaled to the implementation height and placed beside the 1080 x 2400 px emulator capture. This comparison evaluates the requested redesign, not pixel fidelity to the narrow source menu.

## Visual review

- Hierarchy: Phone Language remains the primary full-width choice. International Languages appears first, followed by Indian Languages.
- Layout: both sections use the requested two-column cards with consistent spacing, borders, radii and minimum touch size.
- Typography: native language names and language codes remain readable across Latin, Indian, Arabic and CJK scripts.
- Color: semantic theme colors provide clear contrast in dark mode; the selected Gujarati card has a visible accent border and check badge.
- Overflow: the modal scrolls cleanly. The bottom state includes Punjabi, Tamil, Telugu, Kannada and Malayalam without clipping.
- Assets: real local country-flag PNGs render correctly without a network dependency. The Phone Language row uses the Material globe icon. The flag-icons MIT license is packaged in `res/raw`.

## Interaction review

- Dialog opens from Settings.
- Gujarati selection persists and is visually identified.
- UI hierarchy confirms all eight international and all nine Indian languages are reachable.
- Scroll to the final Indian-language row succeeds.
- Representative country flags render for all international languages; India flags render for all Indian languages.
- Debug compilation and unit tests pass after grouping.

## Findings and comparison history

- Pass 1: no P0, P1 or P2 visual issue found in the combined comparison.
- Pass 1 focused scroll check: no clipping, overlap or inaccessible language card found.
- No post-comparison visual fix was required.

## Final result

passed
