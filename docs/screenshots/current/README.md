# Current screenshot gallery

Single import framing, ASA options/side-by-side controls and Landscape Background screenshots were captured from the v1.0.10 local test build on October 27, 2026 in an isolated API 36 emulator. The Landscape print preview was refreshed from the signed v1.0.12 local candidate to show Borderless / Bordered and white margins. The remaining screens retain the previously approved gallery because their interfaces are unchanged. These are actual app screens using reviewed demonstration images; physical phone/printer acceptance remains unverified; source publication was authorized on October 28.

| File | Screen |
| --- | --- |
| home.png | Mode selection and Printer Setup entry |
| id-camera.png | Single/ASA camera entry and Open Photo (new capture) |
| id-framing.png | Single imported-photo framing without person buttons or face boxes |
| id-adjustments.png | Square/passport review and compact adjustments |
| id-layout.png | Eight ID combinations |
| id-preview.png | ID sheet and remembered paper choice |
| landscape-camera.png | Wide camera viewport |
| landscape-crop.png | Filled 7:5 crop, zoom and advisory warning |
| landscape-adjustments.png | Optional white Background, Auto and five sliders |
| landscape-preview.png | White-margin preview, Borderless / Bordered and Glossy selection |
| asa-options.png | Together or Separately, with one fixed four-copy layout |
| asa-separate.png | Side-by-side source photos, direct Open 1/Open 2 and left/right controls |
| asa-adjustments.png | Both people against white, Background/Auto and five sliders |
| asa-preview.png | Four upright horizontal photos on the landscape-oriented sheet preview |

## Demonstration sources

The ID selection/review/output examples use NASA's STS-135 crew image, **STS135-S-002**, solely as sample input to demonstrate group selection and cropping. Credit: NASA. [Mission and crew information](https://www.nasa.gov/mission/sts-135/) · [Original image](https://images-assets.nasa.gov/image/sts135-s-002/sts135-s-002~orig.jpg) · [NASA image/media guidance](https://www.nasa.gov/nasa-brand-center/images-and-media/). NASA and the depicted individuals do not endorse this application. These are not customer portraits or documents for official ID use.

ASA uses upper-body crops of two crew members from the same public NASA demonstration image, either together or supplied separately. No customer portraits are used. Background removal uses the established on-device segmenter, which [supports single or multiple people](https://developers.google.com/ml-kit/vision/selfie-segmentation).

Camera and retained landscape crop examples use the Android emulator's virtual room. The optional Landscape Background and new bordered preview use the same public NASA fixture. No private customer images, account information, printer addresses or signing material are included. Printer Setup captures containing local configuration are excluded from this public gallery.

## Verification scope

Baseline Single/Landscape QA covered 32 screen states at 411 dp/default text and 360 dp/1.2 text scale. ASA checks cover both capture paths, optional framing, complementary left/right controls, white/original backgrounds, Auto reset, four-copy exports and retained prior shared files. ASA layouts were checked at 411 dp/default text and 360 dp/1.2 text scale. The signed v1.0.12 Landscape previews were checked at both sizes in Borderless and Bordered modes, including saved border selection after a process restart and independent Matte/Glossy selection. Scrollable adjustment content can extend below the viewport; final actions remain visible.

Printer-ready labels illustrate UI state and are not proof that a real printer job succeeded. Physical Landscape border widths, borderless edges, orientation and paper compatibility remain pending. The older gallery is kept in [historical-ui.md](../../historical-ui.md).
