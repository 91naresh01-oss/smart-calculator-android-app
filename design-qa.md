# Design QA — Modern White Box UI

## Evidence

- Source visual truth: `C:\Users\91nar\AppData\Local\Temp\codex-clipboard-ea7f8a7a-50a5-42c5-841c-3ceae4f65816.png`
- Implementation full screenshot: `qa-artifacts/modern-marks-light.png`
- Focused side-by-side comparison: `qa-artifacts/design-comparison-marks.png`
- Additional app-wide evidence: `qa-artifacts/modern-cal-light.png`, `qa-artifacts/modern-more-light.png`, `qa-artifacts/modern-settings-light.png`, `qa-artifacts/modern-dark-system-bars-fixed.png`
- Emulator: `emulator-5554`, Android 15, portrait
- Viewport: 1080 × 2400 px; Compose content uses the emulator's native density
- Source pixels: 426 × 187 px
- Implementation pixels: 1080 × 2400 px
- Focused implementation crop: 1022 × 482 px, normalized to 426 × 201 px for comparison
- Combined comparison pixels: 882 × 240 px
- State: Light theme, 4 VALUE → Marks; Dark theme checked separately

## Full-view comparison evidence

The source is a focused Marks-field crop rather than a complete screen. The complete implementation view was therefore reviewed for hierarchy, clipping, consistency and scroll behavior, while visual fidelity was judged on the normalized Marks-card crop. The full implementation preserves the existing 4 VALUE navigation and calculation flow and applies the same white surface, dull border and restrained black shadow to the surrounding mode, result and action boxes.

## Focused comparison evidence

`qa-artifacts/design-comparison-marks.png` places the source and implementation together. Both show the same two-column, two-row structure, centered uppercase labels, separate numeric/unit boxes, muted grey outlines and a softly elevated white outer card. The implementation intentionally uses pure white field backgrounds, as requested, while the reference fields are slightly off-white.

## Required fidelity surfaces

- Fonts and typography: The existing app font family and Android script fallbacks are retained. Weight, centered alignment, uppercase labels and numeric hierarchy match the reference without clipping.
- Spacing and layout rhythm: Two equal columns, consistent row gaps, rounded corners and compact unit boxes match the target structure. The implementation allows slightly more breathing room for Android touch targets.
- Colors and visual tokens: Light surfaces are pure white; borders use a dull grey semantic outline; shadows use low-opacity black. Dark mode maps the same tokens to dark surfaces and readable outlines.
- Image quality and asset fidelity: The source contains no raster illustration or logo asset. App tool emoji were replaced with Material vector icons; no placeholder or text-glyph icons remain in the redesigned tool grid.
- Copy and content: `TOTAL MARKS`, `FULL %`, `OBTAINED`, `PERCENT`, `Marks`, `100`, and `%` match the source state.

## Comparison history

### Iteration 1

- [P2] Dark system-bar icons were black on the dark status bar.
  - Fix: Synchronized status/navigation icon appearance with the app theme through `WindowCompat`.
  - Post-fix evidence: `qa-artifacts/modern-dark-system-bars-fixed.png` shows white status and navigation icons on dark surfaces.

### Iteration 2

- No actionable P0, P1 or P2 visual differences remain in the reference-matched Marks card or the checked CAL, MORE, Settings and Dark Mode screens.

## Primary interactions tested

- Main navigation: CAL → 4 VALUE → MORE
- 4 VALUE mode: Price → Marks
- MORE: Settings entry
- Settings: Light → Dark theme switch
- App relaunch with persisted theme

## Findings

- No remaining P0/P1/P2 findings.
- P3: The implementation shadow is intentionally a little more visible than the reference to satisfy the requested black-shadow treatment.

## Implementation checklist

- [x] Pure white Light-mode box surfaces
- [x] Dull grey borders
- [x] Restrained black shadows
- [x] Material icons across More and Settings
- [x] Dark-mode semantic surfaces and system-bar contrast
- [x] Unit tests, Android lint, debug build and release build

final result: passed
