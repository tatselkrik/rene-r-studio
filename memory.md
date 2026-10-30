# Project memory

## 2026-09-09

- Current source baseline at first review was version 1.0.2, commit 866a9b8. The app uses CameraX, on-device ML Kit, eight fixed 5x7 sheet layouts, and direct Epson L15150 printing. Preview/Save/Share are neutral; direct printing applies established size and color corrections.
- Kirk chose the same-Wi-Fi workflow after discussing internet printing. Automatic printer connection should preserve that simple local workflow.
- Keep finalized APK preparation separate from Git/release publication. Kirk tests on the phone first and explicitly authorizes publication afterward. Preserve the existing signing identity for updates.
- Automatic printer checks must also observe Wi-Fi availability while the app is visible. Emulator testing showed that a foreground-only check can finish before Wi-Fi recovers; observing network changes resolved that timing gap.
- Kirk explicitly authorized a new signing identity for v1.0.3 and automatic future signing without password entry. Keep the original key/releases intact. The automatic key uses a generated credential protected by Windows DPAPI and restricted local permissions; missing state must fail rather than silently rotate the key. The first installation requires uninstalling the old signing identity.

## 2026-09-14

- Kirk confirmed the v1.0.3 phone test was all good, including his prior framing concern, and explicitly authorized Git updates and APK publication. This is the new approved baseline; keep the automatic signing identity for future updates.

## 2026-09-21

- Kirk requested v1.0.4 with Brightness, Contrast, Vibrance, Saturation and Temperature controls, Auto and Reset below White Background + Lighting. Exposure is explicitly excluded. Prepare the APK locally and await phone-test approval before any Git commit or publication.
- Adjustments are non-destructive and shared by review, sheet preview and export/print. Auto starts off and uses conservative brightness/contrast only; switching it off restores manual settings. Reset restores neutral controls, and a new capture starts neutral. Keep alpha intact so replacement backgrounds stay white, and retain the separate printer calibration.
- Removed the obsolete early project-context document, which contained conflicting crop requirements. README and current code describe behavior; AGENTS.md and the release guide retain project/signing constraints. Earlier versions remain in Git history.

- Later on 2026-09-21, Kirk's phone test confirmed the first v1.0.4 candidate worked but its review screen required too much scrolling. Supersedes the earlier Reset/Auto interaction: remove Reset, clear manual adjustments whenever Auto changes, and return to neutral when Auto is off. Use compact unlabeled crop previews, Background left/Auto right on one row, followed immediately by Brightness and Contrast; remove explanatory text and numeric/end labels. Other sliders can require scrolling.

## 2026-09-22

- Kirk confirmed all physical checks passed for the compact v1.0.4 candidate, including printing with the adjusted settings, and authorized Git/release publication. This supersedes the pending phone-approval status above. Preserve this exact tested APK and the existing automatic signing identity.

## 2026-09-23

- Kirk confirmed v1.0.5 works and authorized publication after comparing A14 5G and S24 FE framing. The candidate replaces the raw-buffer/file transform and smaller-side selection with normalized guide mapping into the upright JPEG, relying on CameraX's existing shared viewport. Keep proportion-mismatch rejection and orientation/resolution tests. Phone results support the correction; the exact device-internal cause was not independently measured.

## 2026-09-26

- Kirk requested phone-file import and choosing an individual from a group photo, and explicitly chose an adjustable square crop. Retain the source group for selecting another person; preserve manual headroom and the narrower passport preview. Face detection offers starting suggestions, while final single-person and quality checks apply to the selected crop. Decode that region from the original source rather than enlarging a downsampled overview. Keep camera framing and printer calibration unchanged.

## 2026-09-28

- Kirk requested Single/Group camera modes and automatic phone-album saving of every camera original before continuing into editing. Single preserves the existing camera-square flow; Group saves the full frame and opens person selection without a second file-picking step. Gallery originals survive Retake and reframing. Kirk confirmed removing final-sheet Save and keeping Share, Back and Print. This supersedes the earlier candidate's final Save action; imported files need no duplicate gallery copy.

## 2026-09-29

- Kirk requested full-size Single/Group camera buttons without the helper paragraphs, Open Photo naming, and a standard Choose another photo button. For manually selected crops, low source resolution is an advisory warning rather than a printing block; retain face, framing and sharpness checks, and do not enlarge source pixels to claim better resolution.

## 2026-10-02

- Kirk authorized Git publication of v1.0.6 build 9 and requested a complete README update. Preserve the exact prepared APK. His publication approval supersedes the pending authorization state; do not infer detailed physical test results beyond what he reported.

## 2026-10-13

- Kirk requested a remembered Matte/Glossy choice in Printer Setup, applicable to both print modes. Keep current color correction for now; glossy-specific calibration is deferred.
- Add an opening choice of ID Photo or Landscape Photo. Landscape uses an existing customer image with a movable, zoomable 7:5 crop filling a borderless 5x7 sheet. Keep the existing ID layouts/checks separate. Crop to fill rather than letterbox, and request actual zero printer margins. Final proof of paper handling and borderless edges is a physical print test; prepare an APK before new publication approval.
- Kirk clarified that background replacement belongs only to IDs. Landscape must hide Background and retain the original scene, while keeping Auto and the five existing adjustment sliders for preview, sharing and printing.
- Kirk added landscape camera capture with automatic original saving, requested stable crop controls when the resolution warning appears, and asked to put the remembered Matte/Glossy choice immediately before Print in both workflows. ID photo processing remains the v1.0.6 baseline; the final preview gains the shared paper choice.
- Kirk renamed the displayed app to Rene'R Studio. Preserve the existing application identifier and signing key so it updates the installed app; retain previous named releases. He explicitly requested screenshot-based layout checks before handing over the APK.
- Kirk subsequently explicitly authorized GitHub publication of v1.0.7 build 11 and requested documentation modeled on HPC: a current collapsible screenshot tour, versioned user guide and complete release log. This supersedes the earlier pending publication state; no new physical landscape-print success was reported. Use reviewed demonstration images rather than customer photos in the public gallery.

## 2026-10-19

- Each Share export must keep its own immutable file and URI. Reusing a shared ID-sheet path can expose a later customer's image through an earlier recipient's still-valid grant; both workflows now allocate unique share files.
- Signing must preserve existing versioned installers. Refuse an existing destination before building or unlocking the key, check both APK version name and version code, verify the staged candidate, and move it to the final filename without overwrite. The isolated installer-safety script checks these guards without signing or accessing saved keys.
- Kirk requested a new version and Git updates after the review. Keep the README current-only: remove superseded version numbers, old download/guide links and its historical release table. This supersedes the October 13 README history-table preference; preserve history in CHANGELOG.md, versioned guides, tags and release assets.
- The canonical public repository is tatselkrik/rene-r-studio. GitHub redirects the former rene-r-id-print address to the same repository; use the canonical name in current links and the Git remote.
- For v1.0.8, Kirk explicitly chose to keep the APK local and said the Git update was enough. Keep the signed APK/checksum local, publish source/docs only, and omit unpublished tag/download links or a latest-release badge from the current README. Git updates alone do not authorize tagging or APK publication.

## 2026-10-27

- Kirk replaced Group capture with ASA for two people. Together uses one upper-chest photo; Separately takes two photos and lets the operator assign left/right. ASA has no person identification/selection or single-person ID checks. Single's imported-person selection and eight layouts remain unchanged.
- ASA uses horizontal photos, 3 inches wide by 2 high, with only four copies on 5x7 paper. Automatic fitting is the default; optional Adjust framing moves/zooms the whole Together photo or each Separate photo independently. Retain Single-style white Background, Auto, Brightness, Contrast, Vibrance, Saturation and Temperature controls.
- Prepare the ASA update and signed APK locally for Kirk's phone test. No Git commit or GitHub update is authorized until he reports back and approves it; preserve the signing identity and prior installers.
- Later phone-test corrections supersede the earlier Single import person-selection decision: Single must have no person identifier or numbered person picker for either camera or imported photos. Retain framing and normal Single photo-quality checks.
- ASA Separately must present the side-by-side slots and direct Open 1/Open 2 controls. File imports stay in that panel; only a first camera capture may advance automatically to the second camera after its original is saved.
- Kirk now requests optional white Background for Landscape as well, superseding the earlier original-scene-only restriction. Default it off; preserve its scene unless selected, while applying the chosen source consistently to adjustments, preview, Share and Print. Test all three corrections before the next phone APK handoff.
- Final ASA capture correction supersedes the earlier camera-sequencing exception: every successful camera shot, including photo 1, must return to the side-by-side editor after its original is saved. Never automatically open camera two. Kirk confirmed both direct-slot imports and imports from either camera screen work; preserve all four import paths.
- Kirk added a remembered Borderless / Bordered option for Landscape. Keep Borderless as the initial choice. Bordered uses normal 3 mm margins and fits the full landscape crop inside them; Share remains the adjusted photo without printer margins. This supersedes the earlier borderless-only Landscape restriction. Preserve ASA capture/import return-to-pair behavior and the local phone-test-before-Git boundary.

## 2026-10-28

- Kirk explicitly authorized source and release-log updates for v1.0.9 through v1.0.12, pushed one version at a time. This supersedes the October 27 Git hold; it does not establish physical-test success or authorize APK uploads, GitHub Releases or tags. Keep the README focused on the current version and remove code-review material from it; retain historical changes in the release log and versioned guides.

## 2026-10-30

- Security exceptions to the old toolchain baseline use Gradle 9.8.1 with integrity checks, compatible AGP 9.4.1 and patched Kotlin 2.4.21, Guava 33.7.2 variants and targeted build-library constraints. Preserve direct CameraX/ML Kit/Compose BOM pins, application identity, keys, layouts and color/size calibration.
- Bound IPP metadata before recursive decoding. ASA/Landscape can use region-decoded bitmaps directly; Single retains its file crop. Avoid reintroducing temporary PNG round trips without a demonstrated need.
- The local security candidate is v1.0.13/build17. Earlier Git authorization covered v1.0.9–v1.0.12 only; new publication approval is required. Keep review material outside the current-only README.
- Later on October 30, Kirk explicitly authorized the v1.0.13 source/documentation commit and push, excluding the security report. This supersedes the preceding Git hold. Keep that report, the signed APK and checksum local; no new tag or GitHub Release is authorized, and approval does not establish physical-test success.
