# Rene'R Studio

[![Android quality checks](https://github.com/tatselkrik/rene-r-studio/actions/workflows/android-quality.yml/badge.svg?branch=main)](https://github.com/tatselkrik/rene-r-studio/actions/workflows/android-quality.yml)
[![Latest release](https://img.shields.io/github/v/release/tatselkrik/rene-r-studio)](https://github.com/tatselkrik/rene-r-studio/releases/latest)

An Android photo workspace for Rene'R's in-house printing service. Capture or open a photo, prepare an ID sheet or a full-page landscape image, adjust its appearance, and print directly to an Epson EcoTank L15150 over local Wi-Fi.

**Current version:** 1.0.8 (build 12) · **Platform:** Android 6.0+ · **Distribution:** Signed APK

[Download APK](https://github.com/tatselkrik/rene-r-studio/releases/download/v1.0.8/ReneR-Studio-v1.0.8.apk) · [User guide](docs/user-guide-v1.0.8.md) · [Release notes](#release-notes) · [Application tour](#application-tour) · [Developer setup](#developer-setup)

## What's new in 1.0.8

- **Safer sharing:** every ID sheet and landscape export receives its own file. Preparing another image leaves previously shared images unchanged.
- **More reliable capture:** Back is blocked while Single, Group or Landscape capture is in progress; camera submission failures show an error and allow retrying.
- **Clearer printer recovery:** a discovery deadline reports a timeout while actual cancellation remains respected. IPv6 addresses and resource paths are handled correctly.
- **Explicit backup exclusions:** private app data is excluded from Android cloud backup and device transfer. Camera originals in the phone album follow the phone's gallery settings.
- **Protected installers:** release signing preserves existing versioned APKs, validates the exact version name and build code, and verifies a staged candidate before making it the final installer.

The eight ID layouts, camera framing, photo adjustments, paper choice and established L15150 size/color corrections remain unchanged. See the [release notes](CHANGELOG.md#108---2026-10-19).

## Two photo workflows

| | ID Photo | Landscape Photo |
| --- | --- | --- |
| Input | Single/Group camera capture or Open Photo | Camera capture or Open Photo |
| Crop | Adjustable person selection for groups/imports; square and narrower passport previews | Movable 7:5 crop with drag, pinch and zoom |
| Finishing | Optional white background, Auto and five sliders | Original background, Auto and five sliders |
| Checks | Face, eyes, framing, head angle, sharpness and resolution | Advisory resolution warning; no ID face checks |
| Output | Portrait 5×7 sheet with ID photos and corner cutting guides | Landscape 7×5 image filling the page, with no added margins or cutting guides |
| Paper | Remembered Matte / Glossy | Same remembered Matte / Glossy choice |

## Core capabilities

- **Capture or import:** use the camera or Android's photo/file picker, including single-person and group images.
- **Choose a person:** face suggestions provide starting points; manual crop positioning preserves headroom and allows another person to be selected from the same source.
- **Keep the original:** new camera photos automatically save to the phone album before editing. Imported originals stay unchanged.
- **Adjust consistently:** non-destructive edits appear in crop previews, the finished JPEG and printing.
- **Use eight ID layouts:** combine 2×2-inch, 35×45 mm passport-size and 1×1-inch photos on one sheet.
- **Reconnect automatically:** the app checks its saved printer on opening, returning to the foreground and Wi-Fi changes.
- **Share or print:** Android sharing exports the finished JPEG; encrypted IPPS sends it directly to the local printer.

## Installation and updates

1. Download **[ReneR-Studio-v1.0.8.apk](https://github.com/tatselkrik/rene-r-studio/releases/download/v1.0.8/ReneR-Studio-v1.0.8.apk)** from the [v1.0.8 release](https://github.com/tatselkrik/rene-r-studio/releases/tag/v1.0.8).
2. Open it on the phone, allow installation from that source if Android asks, and install the update.
3. Connect the phone and L15150 to the same local network, normally the same Wi-Fi. Open the app and check for **Printer ready**.

Install over the existing release to retain private settings. The application identifier and signing identity are unchanged. If Android reports a signature conflict, preserve needed photos and consult the [release guide](GIT_AND_RELEASE_GUIDE.md) before uninstalling.

The app is distributed through GitHub Releases, not Google Play. Updates are installed manually. Each release includes an APK checksum. Android Studio is needed only for development.

## From photo to print

**ID:** choose ID Photo → Single or Group / Open Photo → frame or select a person → Automatic Check and adjustments → Layout → Preview → choose paper → Print or Share.

**Landscape:** choose Landscape Photo → Take Photo or Open Photo → drag and zoom the crop → Preview → adjustments → Preview → choose paper → Print or Share.

Camera originals save under **Pictures / ReneR ID Print**. The existing album name is retained across the app rename. There is no final Save button: **Share** exports the finished image, while automatic saving preserves the untouched camera original.

The phone and printer need local network access to each other; internet is not required. If automatic discovery needs help, open **Printer Setup** and connect by address. See the [user guide](docs/user-guide-v1.0.8.md) for framing, paper selection and troubleshooting.

## Output and printer behavior

The default ID layout contains **4 × 2×2, 2 × passport and 4 × 1×1** photos. All eight presets use fixed millimetre coordinates:

| 2×2-inch | Passport 35×45 mm | 1×1-inch |
| ---: | ---: | ---: |
| 6 | 0 | 0 |
| 4 | 2 | 4 |
| 4 | 0 | 8 |
| 2 | 6 | 0 |
| 2 | 4 | 6 |
| 2 | 2 | 8 |
| 0 | 8 | 4 |
| 0 | 6 | 8 |

ID JPEGs are **3000×4200** pixels; landscape JPEGs are **4200×3000** pixels. Both carry **600 dpi** metadata for 5×7-inch paper. A large exported pixel count does not recover detail missing from a low-resolution source.

Direct printing targets the established L15150/Cassette 1 workflow at high quality. ID printing retains the measured RC Woven matte size correction (about 106.0% horizontal and 105.9% vertical). The established warmer direct-print color treatment remains in both modes; shared JPEGs do not receive printer calibration.

Landscape requests fill scaling, landscape orientation and zero margins, using a borderless 5×7 media keyword when advertised. Unsupported requested settings produce an error rather than a silent bordered fallback. **Actual edge coverage, orientation and Matte/Glossy paper compatibility still require a physical L15150 test for the new landscape workflow.** Glossy-specific color recalibration was intentionally deferred.

## Release notes

| Version | Date | Summary |
| --- | --- | --- |
| [1.0.8](https://github.com/tatselkrik/rene-r-studio/releases/tag/v1.0.8) | 2026-10-19 | Safer shared files, capture recovery, printer timeout/IPv6 fixes, backup exclusions and protected release signing |

See the [detailed notes](CHANGELOG.md#108---2026-10-19) and [GitHub release](https://github.com/tatselkrik/rene-r-studio/releases/tag/v1.0.8) for the installer, checksum and verification limits.

## Application tour

The gallery shows the current interface using reviewed demonstration images. The interface is unchanged by this maintenance release. Expand a screen to view it. Printer-ready labels illustrate the interface and do not establish a physical print result. [Screenshot sources and scope](docs/screenshots/current/README.md).

<details>
<summary><strong>1. Home — ID or Landscape</strong></summary>

<img src="docs/screenshots/current/home.png" alt="Rene'R Studio home with ID Photo, Landscape Photo and Printer Setup" width="400">

</details>

<details>
<summary><strong>2. ID camera — Single, Group or Open Photo</strong></summary>

<img src="docs/screenshots/current/id-camera.png" alt="ID camera with square framing guide and Single/Group controls" width="400">

</details>

<details>
<summary><strong>3. Group photo — choose and frame a person</strong></summary>

Tap a face or a Person button, then position and resize the square. The numbered person row scrolls horizontally.

<img src="docs/screenshots/current/id-person-selection.png" alt="Group photo with face suggestions, movable square and crop controls" width="400">

</details>

<details>
<summary><strong>4. ID review — Background, Auto and adjustments</strong></summary>

<img src="docs/screenshots/current/id-adjustments.png" alt="ID square and passport previews with Background, Auto and photo sliders" width="400">

</details>

<details>
<summary><strong>5. Layout — eight ID combinations</strong></summary>

<img src="docs/screenshots/current/id-layout.png" alt="Eight ID sheet combinations with 2 by 2, passport and 1 by 1 counts" width="400">

</details>

<details>
<summary><strong>6. ID preview — paper choice before Print</strong></summary>

<img src="docs/screenshots/current/id-preview.png" alt="Portrait ID sheet with cutting guides, Share, Matte/Glossy, Back and Print" width="400">

</details>

<details>
<summary><strong>7. Landscape camera — capture a wide image</strong></summary>

<img src="docs/screenshots/current/landscape-camera.png" alt="Landscape camera viewport with Take Photo and Open Photo" width="400">

</details>

<details>
<summary><strong>8. Landscape crop — drag and zoom to fill</strong></summary>

<img src="docs/screenshots/current/landscape-crop.png" alt="Landscape crop with zoom slider and advisory low-resolution warning" width="400">

</details>

<details>
<summary><strong>9. Landscape adjustments — preserve the original scene</strong></summary>

<img src="docs/screenshots/current/landscape-adjustments.png" alt="Landscape image with Auto and five sliders and no Background toggle" width="400">

</details>

<details>
<summary><strong>10. Landscape preview — full-page image and Glossy selection</strong></summary>

<img src="docs/screenshots/current/landscape-preview.png" alt="Landscape 7 by 5 preview with Glossy selected immediately before Print" width="400">

</details>

## Privacy and scope

Face detection, segmentation, checks and adjustments run on the phone. The app has no photo-cloud upload workflow. Camera originals stay in the phone album until the operator deletes them; imported originals stay unchanged. Working images and temporary sheets use private app cache. Share sends the finished image through the Android app the operator selects; Print sends it to the local printer.

Printing uses encrypted IPPS with a saved printer-certificate pin. Initial pairing trusts the certificate presented on the local network; subsequent connections must match it. A changed certificate requires an explicit reconnect in Printer Setup. There is no silent unencrypted fallback.

The public repository includes source, documentation and reviewed demonstration screenshots. Private signing keys, credentials, local device configuration, customer photos, build output and task-checkpoint files are excluded. Signed installers are distributed as release assets.

The project targets one established 5×7-inch L15150 workflow. Other paper sizes/printers, remote internet printing, automated photo retention and authority-specific passport certification are outside the current implementation. Automated ID checks assist the operator; they do not certify passport compliance or detect every hair boundary.

## Documentation

| Document | Purpose |
| --- | --- |
| [User guide — v1.0.8](docs/user-guide-v1.0.8.md) | Installation, ID and landscape workflows, adjustments, paper and troubleshooting |
| [Release history](CHANGELOG.md) | Version milestones, retained behavior and verification limits |
| [Screenshot notes](docs/screenshots/current/README.md) | Current gallery inventory, demo sources and privacy boundary |
| [Git and Release Guide](GIT_AND_RELEASE_GUIDE.md) | Branches, automatic signing and verified publication |

## Developer setup

| Component | Project baseline |
| --- | --- |
| Language / UI | Kotlin / Jetpack Compose, BOM 2026.06.00 |
| Build | Gradle 9.1.0 / Android Gradle Plugin 9.0.1 |
| Android | Compile SDK 36.1, target 36, minimum 23 |
| Java | JDK 17 or a compatible Android Studio embedded JDK |
| Camera | CameraX 1.6.1 |
| On-device vision | ML Kit face detection 16.1.7 / selfie segmentation 16.0.0-beta6 (retained baseline) |
| Printer protocol | JIPP 0.7.18 over encrypted IPPS |
| Application ID | `com.idphoto.printing` (preserved for upgrades) |

Open this root project in Android Studio, install SDK Platform 36.1, compatible Build-Tools and Platform-Tools, and allow the first sync to download dependencies. Run the `app` configuration on a phone or emulator. Development builds use a different signing identity from the release installer.

```text
app/src/main/java/com/idphoto/printing/
├── analysis/   Face and sharpness checks
├── core/       Crop geometry, physical units and ID layouts
├── image/      Loading, original saving and adjustments
├── print/      JPEG rendering, paper profiles and IPPS
└── ui/         Capture, crop, review, preview and setup screens
app/src/test/   Unit tests
docs/           User guides and screenshot galleries
tools/          Signed-installer helper and safety regression checks
```

Verification on Windows, with the JDK configured:

```powershell
.\gradlew.bat testDebugUnitTest lintRelease assembleRelease
powershell -ExecutionPolicy Bypass -File .\tools\test-installer-safety.ps1
```

GitHub's Android quality workflow runs unit tests, debug lint and a debug build. Release verification includes 83 unit tests, release lint/assembly, installer signature and package/version checks, and emulator capture/import/preview/sharing checks. Installer-safety regression checks also run in CI. Physical phone and printer validation remains separate; landscape edge coverage, orientation and paper compatibility still need an L15150 sample print.

The configured signing helper uses the permanent automatic key and a Windows-protected local credential without prompting for a password. Missing signing state fails rather than replacing the key. Private signing material must never enter Git or release assets. See the [release guide](GIT_AND_RELEASE_GUIDE.md) before preparing an installer.
