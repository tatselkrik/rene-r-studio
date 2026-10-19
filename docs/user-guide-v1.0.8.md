# Rene'R Studio — user guide v1.0.8

[Back to README](../README.md) · [Application tour](../README.md#application-tour) · [Release history](../CHANGELOG.md)

Rene'R Studio prepares ID sheets and full-page landscape photos on Android, then shares the finished JPEG or prints to the established Epson L15150. Both workflows use 5×7-inch paper.

## Install and connect

Obtain the signed `ReneR-Studio-v1.0.8.apk` and its matching checksum from the maintainer. This build is kept locally rather than uploaded as a GitHub Release. Install it over the existing release to retain settings. The signing identity is unchanged; if Android reports a signature conflict, preserve needed photos and follow the [release guide](../GIT_AND_RELEASE_GUIDE.md).

Connect the phone and printer to the same local network, normally the same Wi-Fi. Internet is not required for local printing. Open the app and wait for **Printer ready**. If automatic discovery cannot pair, open **Printer Setup** and connect by address. Capture and editing remain available while the printer is offline.

## Print IDs

1. Choose **ID Photo** on the opening screen.
2. Choose **Single** for the guided square camera, or **Group** to photograph several people using the full camera frame. **Open Photo** also accepts either kind of existing image.
3. For Single capture, leave space above the hair inside the square. Camera originals save automatically before editing. Group capture and imported photos open **Choose person**.
4. Tap a detected face or a numbered **Person** button, then drag and resize the square. The person-button row scrolls sideways. Face suggestions are optional; manual positioning works without them.
5. Inspect the square and passport previews. Inner lines show where the narrower passport crop trims the sides; it retains the square crop's height. Tap **Use crop**.
6. On **Automatic Check**, inspect both previews. Set **Background**, **Auto** and the sliders as needed.
7. Tap **Layout**, choose one of eight combinations, then **Preview**.
8. Select **Matte** or **Glossy** immediately above Print. Tap **Print** or **Share**. **Back** returns to layouts.

**Reframe** returns to the same group/imported source so you can choose another person. Each new crop starts with neutral adjustments. **Choose another photo** replaces the source. Retake and reframing do not delete the saved camera original.

Checks cover exactly one face, both eyes, obvious closed eyes, head angle, blur, resolution and framing. Face/eye containment must pass for the crops used by the selected layout. Multiple faces within the final crop still fail the one-person check. Selected source crops below 600 pixels per side show an advisory warning; their pixels are not enlarged to bypass quality checks. These checks do not certify a passport authority's requirements or detect every hair boundary: inspect the previews yourself.

## Print a landscape photo

1. Choose **Landscape Photo**.
2. Tap **Take Photo** or **Open Photo**. A camera shot saves its untouched original automatically, then opens the crop.
3. Drag the image, pinch or use the zoom slider to fill the **7:5** area. Tap **Preview** to open adjustments.
4. Set **Auto** or use the sliders, then tap **Preview** again for the finished **7×5 Print Preview**. **Reframe** returns to the crop.
5. Choose **Matte** or **Glossy**, then **Print** or **Share**. **Back** returns to adjustments.

The crop always fills the whole page, including at minimum zoom. It trims part of the source when its shape differs from 7:5. White edges already inside a supplied image remain part of that image until you zoom and reposition to crop them away. Zoom cannot recover detail from a small file; the low-resolution warning remains advisory.

Landscape keeps the original scene and has no Background toggle or ID face checks. Its output contains no ID cells, corner cutting guides or added white margins. The exported JPEG is 4200×3000 pixels with 600 dpi metadata. Direct printing additionally requests zero margins and landscape orientation.

**Physical landscape edge coverage and paper compatibility are not yet verified for v1.0.8.** Use an L15150 sample print to check all four edges and orientation. The printer must support borderless printing for the requested paper and source; unsupported settings produce an error instead of silently printing a bordered page.

## Adjust the photo

| Control | Effect |
| --- | --- |
| Background — IDs only | Replaces the detected background with white and gently lifts subject shadows. Turn it off to retain the original background. |
| Auto | Applies conservative brightness/contrast correction; manual sliders are disabled while on. |
| Brightness | Lightens or darkens the image. |
| Contrast | Changes separation between light and dark tones. |
| Vibrance | Adjusts muted colors more gently, with reduced effect on skin-like warm hues. |
| Saturation | Changes overall color intensity. |
| Temperature | Cooler to the left, warmer to the right. |

Brightness and Contrast come first. Scroll for the remaining controls on smaller screens or larger text settings. There is no Exposure slider or Reset button. **Change Auto to clear manual edits; turn it off for neutral sliders.** New captures/crops start neutral with Auto off.

Edits are calculated from the working image rather than accumulated over previous edits. Previews, sharing and printing use the same adjusted photo. Wait for adjustment processing to finish before continuing. Direct printing then adds the established printer-specific color correction; shared JPEGs remain free of that correction. Replacement backgrounds remain white.

## Select paper

Choose **Matte / Glossy** immediately before Print in either final preview, or under **Printer Setup → Paper type**. The choice saves immediately, applies across both modes and survives reopening and reconnection. Matte is the initial choice. Select the type matching the paper actually loaded; the app does not detect the stock.

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
| Landscape print rejected | Check support for 5×7 borderless, paper type and Cassette 1. Unsupported requests are not silently converted to bordered printing. |
| Printer reports substituted settings | Inspect the physical page and edges; do not assume the requested output was honored. |
| Hair/face clipped in an ID | Reframe or retake, preserving headroom and checking both square and passport previews. |
| Low resolution | Prefer a larger original or a wider crop. A warning does not block a manually selected crop or landscape image solely for pixel count. |
| Imported format cannot open | Try JPEG or PNG; phone decoders determine other supported formats. |
| Controls below the screen | Scroll the content, especially with larger system text. Final action rows respect the screen insets. |

The ID scale correction was physically established for the existing L15150/RC Woven matte workflow. Other paper sizes, printer models and authority-specific ID rules require separate verification.
