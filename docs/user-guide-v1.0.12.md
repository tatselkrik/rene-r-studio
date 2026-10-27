# Rene'R Studio — user guide v1.0.12

[Back to README](../README.md) · [Application tour](../README.md#application-tour) · [Release history](../CHANGELOG.md)

Rene'R Studio prepares Single ID sheets, ASA two-person sheets and full-page landscape photos on Android, then shares the finished JPEG or prints to the established Epson L15150. All workflows use 5×7-inch paper.

## Install and connect

Obtain the signed `ReneR-Studio-v1.0.12.apk` and its matching checksum from the maintainer. This build is kept locally rather than uploaded as a GitHub Release. Install it over the existing release to retain settings. The signing identity is unchanged; if Android reports a signature conflict, preserve needed photos and follow the [release guide](../GIT_AND_RELEASE_GUIDE.md).

Connect the phone and printer to the same local network, normally the same Wi-Fi. Internet is not required for local printing. Open the app and wait for **Printer ready**. If automatic discovery cannot pair, open **Printer Setup** and connect by address. Capture and editing remain available while the printer is offline.

## Print IDs

1. Choose **ID Photo** on the opening screen.
2. Keep **Single** for the guided square camera, or choose **Open Photo** for an existing image. Single has no person identifier or numbered person picker.
3. Leave space above the hair inside the camera square. Camera originals save automatically before editing.
4. Imported photos open **Adjust framing**. Tap or drag the square and use **Crop size** to fit the one-person photo; no face-selection boxes are shown.
5. Inspect the square and passport previews. Inner lines show where the narrower passport crop trims the sides while retaining the square's height. Tap **Use crop**.
6. On **Automatic Check**, inspect both previews. Set **Background**, **Auto** and the sliders as needed.
7. Tap **Layout**, choose one of eight combinations, then **Preview**.
8. Select **Matte** or **Glossy** immediately above Print. Tap **Print** or **Share**. **Back** returns to layouts.

**Reframe** returns to the same imported source so you can adjust its framing. Each new crop starts with neutral adjustments. **Choose another photo** replaces the source. Retake and reframing do not delete the saved camera original.

Checks cover exactly one face, both eyes, obvious closed eyes, head angle, blur, resolution and framing. Face/eye containment must pass for the crops used by the selected layout. Multiple faces within the final crop still fail the one-person check. Selected source crops below 600 pixels per side show an advisory warning; their pixels are not enlarged to bypass quality checks. These checks do not certify a passport authority's requirements or detect every hair boundary: inspect the previews yourself.

## Print ASA photos

1. Choose **ID Photo → ASA**. The only layout is four horizontal **3-inch-wide × 2-inch-high** photos on **5×7-inch** paper.
2. Choose **Together** for one photo of both people sitting side by side. Stand farther back and frame just above the chest. The full camera frame is used; there is no person identifier or face-selection step.
3. Alternatively choose **Separately** to open the two-slot view immediately. Choose **Open 1** and **Open 2** to import files directly, or **Take 1 / Take 2** for camera shots. Every imported or captured photo returns to the side-by-side view. Camera originals save first. After photo 1, choose Take 2 or Open 2 when ready; photo 2 is never opened automatically. Each source fills one half of the combined horizontal image.
4. In Separate mode, choose **Left / Right** for either photo. The other photo moves to the opposite side. Retaking one source preserves the other.
5. Automatic fitting is already applied. Optionally open **Adjust framing**: drag/pinch or use Zoom. For separate sources, select **First photo / Second photo** and adjust each independently. **Auto fit** resets only that source. Tap **Done**.
6. Tap **Preview** for finishing. **Background** starts on and replaces the background with white while keeping both people. **Auto** and Brightness, Contrast, Vibrance, Saturation and Temperature work as in Single. Inspect hair, shoulders and both faces; turn Background off to retain the original scene.
7. Tap **Preview** again. Choose **Matte / Glossy**, then **Share** or **Print**. There is no layout selector in ASA. Back returns to finishing, then to framing/placement.

ASA does not enforce Single's one-face/ID checks. Low source resolution is advisory. The neutral shared sheet is 3000×4200 pixels with 600-dpi metadata. Each horizontal photo is rotated on the portrait print sheet, so four fit while keeping the established printer orientation, calibration and cutting guides. The on-screen paper is rotated back so all four photos appear upright.

The signed APK remains local. Source and release-log publication was authorized on October 28, 2026; this does not establish a successful physical phone/printer test. Check both people, background edges, left/right placement and the physical 3×2-inch cut size on the L15150. No public installer upload is included.

## Print a landscape photo

1. Choose **Landscape Photo**.
2. Tap **Take Photo** or **Open Photo**. A camera shot saves its untouched original automatically, then opens the crop.
3. Drag the image, pinch or use the zoom slider to fill the **7:5** area. Tap **Preview** to open adjustments.
4. Keep **Background** off for the original scene, or enable it for white replacement. Set **Auto** or use the sliders, then tap **Preview** again for the finished **7×5 Print Preview**. **Reframe** returns to the crop.
5. Choose **Borderless** to fill the paper or **Bordered** for white margins. The preview changes with your choice, which is remembered when you reopen the app.
6. Choose **Matte** or **Glossy**, then **Print**. **Share** exports only the adjusted photo, without printer margins. **Back** returns to adjustments.

The crop always fills the whole page, including at minimum zoom. It trims part of the source when its shape differs from 7:5. White edges already inside a supplied image remain part of that image until you zoom and reposition to crop them away. Zoom cannot recover detail from a small file; the low-resolution warning remains advisory.

Landscape keeps the original scene by default and has an optional white Background toggle. It has no Single ID checks. White replacement and all adjustments carry through the preview, Share and Print; changing the crop resets Background off. Its photo contains no ID cells or corner cutting guides. The shared JPEG is 4200×3000 pixels with 600 dpi metadata and no added printer margins. Direct printing always uses landscape orientation. Borderless, the initial choice, requests fill scaling and zero margins. Bordered requests regular 5×7 media and 3 mm margins, fitting the whole crop inside them; the image can have wider margins on two edges to preserve its proportions. Both print modes support the shared Matte / Glossy choice. The preview illustrates the requested margins; printer behavior needs a sample print.

**Physical landscape border widths, edge coverage and paper compatibility are not yet verified for v1.0.12.** Use an L15150 sample print in each mode to check all four edges and orientation. The printer must support the requested paper, source, margins and scaling; unsupported settings produce an error instead of silently changing modes.

## Adjust the photo

| Control | Effect |
| --- | --- |
| Background — all workflows | Replaces the detected background with white and gently lifts subject shadows. Turn it off to retain the original background. |
| Auto | Applies conservative brightness/contrast correction; manual sliders are disabled while on. |
| Brightness | Lightens or darkens the image. |
| Contrast | Changes separation between light and dark tones. |
| Vibrance | Adjusts muted colors more gently, with reduced effect on skin-like warm hues. |
| Saturation | Changes overall color intensity. |
| Temperature | Cooler to the left, warmer to the right. |

Brightness and Contrast come first. Scroll for the remaining controls on smaller screens or larger text settings. There is no Exposure slider or Reset button. **Change Auto to clear manual edits; turn it off for neutral sliders.** New captures/crops start neutral with Auto off. Background starts on in Single/ASA and off in Landscape.

Edits are calculated from the working image rather than accumulated over previous edits. Previews, sharing and printing use the same adjusted photo. Wait for adjustment processing to finish before continuing. Direct printing then adds the established printer-specific color correction; shared JPEGs remain free of that correction. Replacement backgrounds remain white.

## Select paper

Choose **Matte / Glossy** immediately before Print in any final preview, or under **Printer Setup → Paper type**. The choice saves immediately, applies across all workflows and survives reopening and reconnection. Matte is the initial choice. Select the type matching the paper actually loaded; the app does not detect the stock.

Glossy uses the printer's advertised glossy media type. If the saved profile lacks it, reconnect in Printer Setup to refresh capabilities. If the printer still does not report support, the app reports the unsupported choice rather than silently using matte. The paper choice changes the media request; it does not introduce a new glossy color calibration. Current color correction is retained.

## Originals and finished images

- Camera originals save under **Pictures / ReneR ID Print**. The album name remains unchanged after the app rename.
- A failed album save keeps the captured working file and offers **Retry saving**. Android 9 and earlier require storage permission for the album.
- Importing does not duplicate or modify the selected original. Open Photo uses Android's picker without broad storage permission; files above 100 MB are rejected. JPEG and PNG are recommended if another format cannot be decoded.
- Temporary working images and sheets use private app cache. Replacing an imported source or returning to Camera removes the current source working copy.
- Each Share creates a separate finished-image file, leaving earlier shared images unchanged.
- There is no final Save button. **Share** sends the finished sheet/image through Android's sharing panel; automatic camera saving preserves the untouched original, not the edited output.
- Saved originals remain until you delete them. The app has no cloud-photo upload workflow or automatic retention policy.

## Troubleshooting

| Situation | What to do |
| --- | --- |
| Printer unavailable | Check power, same local network and network isolation. Reopen/retry; use Printer Setup if discovery needs help. |
| Certificate changed | Explicitly reconnect in Printer Setup after checking the printer address. Automatic connections do not replace the saved certificate. |
| Glossy unavailable | Reconnect to refresh reported capabilities; confirm that the printer supports the requested media. |
| Landscape print rejected | Check regular 5×7 / 3 mm margins / fit scaling for Bordered, or zero margins / fill for Borderless, plus the chosen paper type and Cassette 1. Reconnect to refresh capabilities. Unsupported requests are not silently changed. |
| Printer reports substituted settings | Inspect the physical page and edges; do not assume the requested output was honored. |
| Hair/face clipped in an ID | Reframe or retake, preserving headroom and checking both square and passport previews. |
| Low resolution | Prefer a larger original or a wider crop. A warning does not block a manually selected crop or landscape image solely for pixel count. |
| Imported format cannot open | Try JPEG or PNG; phone decoders determine other supported formats. |
| Controls below the screen | Scroll the content, especially with larger system text. Final action rows respect the screen insets. |

The ID scale correction was physically established for the existing L15150/RC Woven matte workflow. Other paper sizes, printer models and authority-specific ID rules require separate verification.
