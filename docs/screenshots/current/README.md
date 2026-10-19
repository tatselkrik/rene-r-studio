# Current screenshot gallery

These PNGs illustrate the unchanged interface of v1.0.8. They are byte-identical copies of the previously reviewed v1.0.7 gallery, given stable filenames rather than relabeled as newly captured images. Additional v1.0.8 release checks cover the signed installer and runtime smoke path; the code-review emulator checks covered capture, imports, previews and sharing. The README presents these real screens in workflow order.

| File | Screen |
| --- | --- |
| home.png | Mode selection and Printer Setup entry |
| id-camera.png | Single/Group ID camera and Open Photo |
| id-person-selection.png | Group face suggestions and adjustable crop |
| id-adjustments.png | Square/passport review and compact adjustments |
| id-layout.png | Eight ID combinations |
| id-preview.png | ID sheet and remembered paper choice |
| landscape-camera.png | Wide camera viewport |
| landscape-crop.png | Filled 7:5 crop, zoom and advisory warning |
| landscape-adjustments.png | Original scene, Auto and five sliders |
| landscape-preview.png | Full-page output and Glossy selection |

## Demonstration sources

The ID selection/review/output examples use NASA's STS-135 crew image, **STS135-S-002**, solely as sample input to demonstrate group selection and cropping. Credit: NASA. [Mission and crew information](https://www.nasa.gov/mission/sts-135/) · [Original image](https://images-assets.nasa.gov/image/sts135-s-002/sts135-s-002~orig.jpg) · [NASA image/media guidance](https://www.nasa.gov/nasa-brand-center/images-and-media/). NASA and the depicted individuals do not endorse this application. These are not customer portraits or documents for official ID use.

Camera and landscape examples use the Android emulator's virtual room. No private customer images, account information, printer addresses or signing material are included. Printer Setup captures containing local configuration are excluded from this public gallery.

## Verification scope

The full local QA bank covered 32 screen states at 411 dp/default text and 360 dp/1.2 text scale. Contact sheets were visually inspected and visible action bounds were checked for offscreen/overlapping controls. Warning on/off crop positions were also compared. Some content and the person-selector row scroll intentionally.

Printer-ready labels illustrate UI state and are not proof that a real printer job succeeded. Physical landscape borderless edges, orientation and paper compatibility remain pending. The older gallery is kept in [historical-ui.md](../../historical-ui.md).
