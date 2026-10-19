# Release history

Version milestones are listed below; intermediate development and validation builds are consolidated under their version. Dates use Philippine local time. Versions 1.0.0–1.0.2 are historical Git tags; GitHub APK releases begin at 1.0.3. Earlier notes describe the behavior at the time, including the Rene'R ID Print name and the former Save action.

## 1.0.8 - 2026-10-19

### Privacy and reliability

- Give every shared ID sheet and landscape image a unique file, so a recipient's earlier URI never points to a later export.
- Report printer-discovery deadlines as ordinary connection failures while preserving caller cancellation.
- Correct IPv6 printer address labels, already bracketed hosts and escaped IPPS resource paths.
- Block Back during Single/Group/Landscape capture and recover from synchronous camera submission failures; remove incomplete failed captures.
- Explicitly exclude private app data from cloud backup and device transfer, with the legacy no-backup setting retained.

### Installer and documentation

- Refuse existing versioned installers before building or opening signing state. Sign and verify a staged candidate before moving it into place without overwrite.
- Validate APK versionCode as well as versionName, package and non-debuggable status. Add isolated installer-safety checks to CI.
- Update the README to show only the current version, download, guide and release notes. Use stable screenshot paths and the canonical Rene'R Studio repository address; historical releases and documentation remain preserved.
- Preserve the application ID, automatic signing identity, dependency baseline, eight layouts and L15150 scale/color calibration. Published build 12.

### Verification scope

- 83 unit tests, release lint/assembly, signed installer identity and update checks, and isolated installer safeguards. Android quality CI must pass on the exact release commit before publication.
- API 36 emulator checks cover capture with immediate Back, album originals, imported portrait checks, ID/landscape previews and sharing. Two different ID layouts kept distinct export files and preserved the first file's digest. No share recipient or print job was selected.
- No new physical phone/printer or OEM migration test was performed. Landscape borderless edges, orientation and Matte/Glossy handling still need a physical L15150 sample print. Glossy-specific recalibration remains deferred.

## 1.0.7 - 2026-10-13

### Rene'R Studio and landscape photos

- Renamed the displayed application to Rene'R Studio and added ID Photo / Landscape Photo choices at startup. The application ID and automatic signing key are retained for updates.
- Added landscape camera capture and Open Photo, with automatic saving of untouched camera originals to the existing phone album.
- Added a movable 7:5 crop with drag, pinch and slider zoom. Minimum zoom fills the page by trimming the aspect difference; low resolution remains advisory.
- Added landscape adjustments and full-page 4200×3000 JPEG output with 600 dpi metadata. The original background is retained; Auto and all five sliders apply to preview, Share and Print.
- Added landscape direct-print requests for fill scaling, landscape orientation and zero margins, with strict handling of unsupported settings.

### Paper choice and layout

- Added a remembered Matte / Glossy choice in Printer Setup and immediately before Print in both final previews.
- Uses the printer's reported glossy media type; missing support requires refreshing capabilities or reporting the unsupported choice rather than silently using matte.
- Reserved space for the crop resolution warning so the preview, zoom and actions stay stationary.
- Fixed landscape camera viewport overflow with clipping limited to that mode.
- Preserved ID geometry, face/framing checks, eight layouts, adjustment processing and established L15150 size/color correction. ID preview gains the shared paper choice.

### Documentation and verification

- Reworked the README with a current application tour, release-log table and developer overview; added the versioned user guide and this release history.
- Published ten actual emulator screenshots using demonstration images, with source credits and privacy notes.
- Local checks: 76 unit tests, release assembly, lint with 0 errors and 19 warnings, retained signatures and installer update verification. Screenshot checks covered 32 states at 411 dp/default text and 360 dp/1.2 text scale.
- **Physical landscape printing is not yet verified:** borderless edges, orientation and paper/source compatibility need an L15150 sample print. Glossy color recalibration is deferred; current correction remains in use.

## 1.0.6 - 2026-10-02

### Photo input and person selection

- Added Open Photo for single-person and group images, face suggestions and adjustable square person crops.
- Added Single / Group camera modes. Camera originals automatically save to Pictures / ReneR ID Print before editing; Group capture opens person selection directly.
- Retained the group source for reframing and choosing another person. Crops handle EXIF orientation and decode from source pixels.
- Made low-resolution manually selected crops advisory rather than blocked solely by pixel count; face, framing and sharpness checks remain.
- Simplified camera controls and standardized crop action buttons.
- Removed final-sheet Save; final actions are Share, Back and Print. Imported originals remain unchanged and are not duplicated in the album.
- Preserved the signing identity, camera-framing correction, adjustments and printing calibration. Published build 9.

## 1.0.5 - 2026-09-23

### Camera framing

- Mapped the camera guide into the upright saved JPEG using CameraX's shared preview/capture region.
- Handled differing JPEG rotation conventions and rejected incompatible proportions instead of silently tightening the crop.
- Confirmed working after the A14 5G / S24 FE comparison and physical-test approval.
- Preserved v1.0.4 controls, layouts, printer settings and signing identity. Published build 6.

## 1.0.4 - 2026-09-22

### Photo adjustments

- Added Brightness, Contrast, Vibrance, Saturation and Temperature with conservative Auto correction; Exposure was intentionally excluded.
- Compacted Automatic Check around the two crop previews, Background / Auto row, and frequently used Brightness / Contrast sliders.
- Removed Reset. Changing Auto clears manual edits; switching Auto off returns the sliders to neutral.
- Applied edits consistently to preview, JPEG output and printing, while retaining separate printer calibration.
- Removed the superseded early planning document. Physical phone and adjusted-print checks passed before publication.

## 1.0.3 - 2026-09-14

### Connection, checks and signing

- Added automatic saved-printer checks on opening/foreground return and recovery when Wi-Fi becomes available.
- Required encrypted IPPS and a saved printer-certificate match on subsequent connections.
- Added face/eye crop containment and layout-aware blocking when the required crop does not pass.
- Introduced the explicitly authorized automatic signing identity, with a Windows-protected local credential and no normal password prompt.
- Preserved the original key and earlier releases. Physical testing passed before publication.

## 1.0.2 - 2026-08-06

- Added eight maximum-use ID layouts and the Choose Combination screen.
- Preserved fixed photo dimensions and established printing corrections.
- Historical local Git milestone; no GitHub APK release was created for this version.

## 1.0.1 - 2026-08-06

- Added white-background processing, gentle subject-shadow lifting and warmer direct-print color correction.
- Fixed transparent backgrounds flattening to black during export.
- Historical local Git milestone; no GitHub APK release was created for this version.

## 1.0.0 - 2026-08-05

- Introduced guided capture, face checks, fixed ID-photo crops and 5×7 preview.
- Added JPEG Save/Share and direct local Wi-Fi printing.
- Historical local Git milestone; no GitHub APK release was created for this version.
