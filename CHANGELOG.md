# Release history

Version milestones are listed below; intermediate development and validation builds are consolidated under their version. Dates use Philippine local time. Versions 1.0.0–1.0.2 are historical Git tags; GitHub APK releases begin at 1.0.3. Earlier notes describe the behavior at the time, including the Rene'R ID Print name and the former Save action.

Entries dated October 28 record source and documentation publication of local builds prepared on October 27. Signed APKs and checksums remain local. Physical-test limitations are retained below.

## 1.0.13 - 2026-10-30

- Patch Guava and affected build libraries. Use security-patched Gradle 9.8.1 and Kotlin/Compose compiler 2.4.21 with compatible Android Gradle Plugin 9.4.1. Pin the official Gradle distribution checksum, verify the refreshed wrapper JAR and freeze CI actions to their current immutable commits.
- Bound printer responses to 1 MiB, 10,000 wire tokens and 32 collection levels before JIPP decoding. Reject malformed framing and check elapsed time between reads while retaining socket timeouts, TLS pinning and cleartext rejection.
- Decode ASA/Landscape crops directly into preview bitmaps, removing temporary PNG compression, writes and second decoding. Preserve Single's file crop, EXIF transforms, resolution limits, backgrounds, layouts and calibration.
- Make background-mask rows cancellable and recycle partial output on failure/cancellation.
- Build 17 source and documentation are published on GitHub; the signed APK and checksum remain local. Checks passed: 104 unit tests, three API 36 image tests, debug/release assembly, release lint (zero errors/27 advisory warnings), 10 installer-safety scenarios and emulator smoke checks. OSV rescan found no matches across 165 runtime and 114 build coordinates; this does not certify shaded/native SDKs or firmware.
- ASA imports and first capture still return to side-by-side and originals are saved. Single reaches its final sheet; Landscape background, both borders and Share work. No recipient or print job selected.
- This update publishes source and release logs only, without a new tag, GitHub Release or installer upload. The exact signed upgrade retained camera permission and paper/border preferences, and passed Landscape preview plus the first ASA capture return. Installed and local APK hashes match. Physical printer acceptance remains pending; preserve earlier installers.

## 1.0.12 - 2026-10-28

- Add a remembered Borderless / Bordered choice on Landscape's final print preview. Borderless remains the default and keeps its zero-margin fill request. Bordered keeps landscape orientation, requests regular 5×7 paper and 3 mm margins, and fits the full crop inside the printable area. Both retain Matte / Glossy selection.
- Preview the bordered paper with white margins. Share continues exporting the adjusted full-crop JPEG without printer margins, avoiding double margins when printed elsewhere.
- Preserve the ASA return-to-pair fix, all import routes, Single behavior, optional Landscape white Background, original-photo saving, identifiers, signing identity, dependencies and printer calibration. Build 16 was prepared locally on October 27; source and documentation publication was authorized on October 28. The signed APK and checksum remain local.
- Verification passed: 95 unit tests, debug/release assembly and release lint (zero errors/18 retained warnings). The exact signed v1.0.11-to-v1.0.12 upgrade retained camera permission and paper selection. Both border modes, Matte/Glossy independence, border persistence after process restart and the sharing panel passed on API 36. Normal and 360-dp/1.2-text previews had visible controls and no crash. Source comparison confirms ASA, Single and JPEG renderers are unchanged from the previous candidate. No print job or share recipient was selected. Physical border widths and paper compatibility require an L15150 sample print.

## 1.0.11 - 2026-10-28

- Remove ASA Separate's automatic transition from the first capture to camera two. Every captured/imported photo returns to the side-by-side editor; camera originals are still saved before editing.
- Preserve the working direct-slot and camera-screen import paths for both photos, along with framing, positions, Single behavior and optional Landscape white background.
- Build 15 was prepared locally on October 27 without overwriting earlier installers. Source and release-log publication was authorized on October 28; physical phone/printer acceptance remains unverified. The APK stays local.
- Verification before handoff: 90 unit tests, debug/release assembly and release lint passed (zero errors/18 retained warnings). The signed v1.0.10-to-v1.0.11 upgrade retained camera permission. On API 36, photo 1 returned to the pair view with slot 2 empty; photo 2 and a photo-1 retake returned with both slots filled. The retake left photo 2 pixel-identical, all three originals were saved, all four direct/camera-screen import paths passed, and the final sheet preview opened without a crash. Only ASA navigation and version metadata changed in application source from the previous candidate.

## 1.0.10 - 2026-10-28

- Correct ASA Separate file imports reopening the second camera. Separately now starts in the side-by-side editor with direct Open 1/Open 2 controls; only actual first camera captures continue automatically to camera two after album saving.
- Remove the person picker, numbered Person buttons, face suggestion boxes and imported-person detection from Single. Retain manual framing, passport guides, normal Single photo-quality checks and background/adjustment settings.
- Add optional white Background to Landscape, off by default. Use the selected background consistently for Auto/manual adjustments, preview, Share and Print.
- Build 14 was prepared locally on October 27 for phone testing. Preserve the earlier test APK, identifiers, signing identity, dependency baseline, eight Single layouts and printer calibration. Source and release-log publication was authorized on October 28; the signed APK and checksum stay local.
- Verification passed before handoff: 90 unit tests, debug/release assembly, release lint (zero errors/18 retained warnings), and normal/360-dp larger-text UI checks. Emulator checks covered first/second ASA file imports, cancellation, mixed inputs, camera-only sequencing, Single framing through its print preview, and Landscape original/white preview and 4200×3000/600-dpi exports. Background off restored the original pixels and prior shared output remained unchanged. The signed build upgraded from v1.0.9 with camera permission retained and passed all three revised flows; the crash buffer was empty. Physical phone/printer acceptance remains pending.

## 1.0.9 - 2026-10-28

- Replace Group capture with ASA. Together uses a full-frame upper-chest photo of two people; Separately captures two originals and lets either source be placed left or right.
- Automatically fit a horizontal 3×2-inch photo. Optional framing adjusts the shared image or each separate source independently, without a person identifier or single-face ID checks.
- Use one fixed layout: four horizontal photos on 5×7 paper. Rotate the photos on the existing portrait print path and show an upright landscape-oriented preview. Retain the L15150 size/color correction, corner cutting guides and remembered paper choice.
- Include Single-style white Background, Auto and the five adjustment sliders. Preview, Share and Print use the same finished photo; shared exports remain unique and neutral.
- Preserve Single's eight layouts, Single Open Photo person selection, Landscape, album-original saving, application identifier, dependency baseline and permanent signing identity. Build 13 was prepared locally on October 27; source and release-log publication was authorized on October 28.
- Local checks passed: 90 unit tests, release assembly and lint with zero errors/18 retained advisory warnings. API 36 emulator checks covered both captures, independent framing, position swaps, white/original backgrounds, Auto reset, compact layouts and two immutable four-copy exports at 3000×4200/600 dpi. The signed APK upgraded from build 12 without uninstalling or losing camera permission, then passed an ASA Separate/white-background/preview smoke check.
- Physical phone/printer acceptance remains pending. No print job or share recipient was selected during local checks. The signed APK and checksum remain local. The authorized source update does not create a GitHub Release, version tag or installer upload.

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
- Update the README to show only the current version, installation instructions, guide and release notes. Use stable screenshot paths and the canonical Rene'R Studio repository address; historical releases and documentation remain preserved.
- Preserve the application ID, automatic signing identity, dependency baseline, eight layouts and L15150 scale/color calibration. Build 12 is prepared and signed locally; Git source/documentation are updated. Kirk explicitly chose to keep the APK local, with no new version tag or GitHub Release.

### Verification scope

- 83 unit tests, release lint/assembly, signed installer identity and update checks, and isolated installer safeguards. Android quality CI passed on the application-source commit. Documentation updates are checked separately; the signed APK remains local.
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
