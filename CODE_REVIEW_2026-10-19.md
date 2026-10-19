# Code review — 19 October 2026

Baseline: `910ed8e`, Rene'R Studio 1.0.7 (11). One Gradle module, `:app`, containing 40 production Kotlin files. Review performed sequentially in the order below. Fixes are authorized locally; this review does not authorize publication or replacement of release APKs.

## Module and review list

| Order | Area | Kotlin files | Review result |
| --- | --- | ---: | --- |
| 1 | `core` — crop geometry, units, eight sheet layouts and print scale | 8 | Reviewed; no actionable defect in current callers. |
| 2 | `analysis` — face, eye, pose and sharpness checks | 2 | Reviewed; no actionable defect. |
| 3 | `image` — import, EXIF transforms, gallery saving, background and adjustments | 6 | Reviewed; no actionable defect in current callers. |
| 4 | `print` — export, requests, TLS pairing, discovery and connection state | 10 | Findings R1–R3. |
| 5 | `ui` — screens, capture, crop controls, sharing and printing | 13 | Findings R1 and R4. |
| 6 | App integration and navigation (`MainActivity.kt`) | 1 | Reviewed with screen callbacks and worker cancellation; R4 applies at the capture boundary. |
| 7 | Build, manifest/resources, Gradle wrapper, CI and signing helper | — | Findings R5–R7. |

## Findings and repair list

Locations refer to the reviewed baseline; final locations can move after repairs.

1. **R1 — P1: Sharing successive ID sheets reuses an already granted URI.** `ui/SheetPreviewScreen.kt:82–87` writes every shared sheet to `print/id-photo-5x7-share.jpg`. A receiving activity can retain permission to that URI while another customer's sheet overwrites its contents. Allocate a distinct file for each share, including landscape shares, and retain previous share contents. Status: fixed.
2. **R2 — P2: Discovery timeout is mistaken for caller cancellation.** `print/EpsonPrinterDiscovery.kt:32` lets its own timeout throw `TimeoutCancellationException`. `PrinterConnection.refresh` then labels the check paused, while setup/print actions rethrow cancellation and finish without a useful timeout message. Translate only the discovery operation's own deadline into an ordinary I/O failure and preserve real caller cancellation. The actual Wi-Fi observer uses `collectLatest`; this review does not establish that the observer permanently stops after a child timeout. Status: fixed.
3. **R3 — P2: IPv6 printer addresses can connect but fail to print.** `print/DirectPrintRequest.kt:34–35` brackets an already bracketed URI host a second time; `DirectPrinterProfile.addressLabel` also produces ambiguous unbracketed IPv6 plus port strings. Build the IPPS URI from its components and consistently bracket address labels. Status: fixed.
4. **R4 — P2: System Back can interrupt Single/Group capture before the original is saved.** `ui/CaptureScreen.kt:121` blocks Back only for landscape, while `MainActivity.kt:277` leaves ID capture immediately. Also, a synchronous CameraX submission failure leaves `isCapturing` true without an error handler. Block Back during either capture mode and recover from submission failure; remove partial failed captures. Status: fixed.
5. **R5 — P1: Signing can overwrite an existing versioned installer before verification succeeds.** `tools/create-signed-installer.ps1:147–168` signs directly to the final versioned APK, including an existing tested/released file. Refuse existing destinations before building and publish a locally staged candidate only after all signature checks, without overwrite. Status: fixed.
6. **R6 — P2: Signing does not validate Android versionCode.** `tools/create-signed-installer.ps1:131–145` checks package, versionName and debuggability, but a prepared APK from an earlier build with the same versionName can pass despite a newer source versionCode. Validate the exact build code from APK metadata. Status: fixed.
7. **R7 — P2: Android 12+ data-transfer exclusions are unspecified.** `app/src/main/AndroidManifest.xml:23–24` sets `allowBackup=false` but supplies no extraction rules. Android documents that some manufacturers can still permit device transfer when this flag is false. Explicitly exclude app data from cloud and device transfer while retaining the no-backup policy. Status: fixed.

## Verification and final state

All seven findings were fixed and checked on `codex/code-review-fixes`. No actionable finding remains open in this review. At review completion, the application ID, dependencies, version name/code, eight layouts, print scaling, color calibration and signing identity were preserved. This report records that review stage; the subsequent v1.0.8 version/publication work is documented in CHANGELOG.md and the release guide.

- **Before repair:** all 76 existing unit tests passed. Three added IPv6/path/label regression tests then failed against the original code, reproducing R3. Initial lint completed with zero errors and 19 warnings.
- **Final Android checks:** `testDebugUnitTest lintDebug assembleDebug --offline --no-daemon` passed. All **83 unit tests** passed with zero failures/errors. The seven new tests cover IPv6 labels/URIs, URI path escaping, local discovery timeout versus caller cancellation, connection recovery, and retention of successive share files.
- **Final lint:** zero errors and **18 advisory warnings**. The extraction-policy warning is resolved, including the older-Android `fullBackupContent=false` setting. Remaining warnings concern the preserved target SDK/portrait orientation, launcher icon styling and KTX style suggestions; these are outside the behavioral repairs.
- **Installer safeguards:** `tools/test-installer-safety.ps1` passed all **10 scenarios** under both Windows PowerShell 5.1 and PowerShell 7. The tests load the real guard functions without executing signing, and verify destination preservation, a final-move collision, intact candidate movement, exact metadata, and rejection of stale/wrong/debuggable APK metadata. Signing keys and existing release APKs were not accessed by these tests.
- **Android 16 / API 36 emulator:** installed the final debug APK in a fresh temporary AVD. Single and Group capture followed immediately by Back reached their expected next screen; both originals appeared under `Pictures/ReneR ID Print`. Imported the repository's public demo screenshot, selected a portrait, completed photo checks and reached both the default and six-square sheet previews.
- **Sharing regression on device:** opened the chooser for two different ID layouts. They produced different image digests at distinct filenames; the first image's SHA-256 stayed unchanged after the second export. Landscape import, crop, adjustment preview, final preview and Share also completed and produced its own unique file. No share recipient was selected and no print job was submitted.
- **Visual/runtime checks:** inspected ID and landscape preview screenshots; the emulator crash buffer remained empty. `git diff --check` passed. Generated emulator data, screenshots, logs and checkpoints are excluded through `work/`.

Limits: no physical phone/camera/printer test, no OEM migration test, and no real release signing or release build were performed. The synchronous CameraX submission-exception branch was reviewed and compiled but was not separately fault-injected. Debug output is `app/build/outputs/apk/debug/app-debug.apk`; it is not a new signed release or replacement for the existing v1.0.7 installer.

## Supporting API references

- [Android FileProvider permissions](https://developer.android.com/reference/androidx/core/content/FileProvider): URI permissions can last while the receiving activity stack remains active.
- [Kotlin withTimeoutOrNull](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/with-timeout-or-null.html): distinguish an operation's deadline from cancellation of its caller.
- [Android backup and transfer configuration](https://developer.android.com/identity/data/autobackup): extraction rules can explicitly exclude data from device transfers as well as cloud backups.
