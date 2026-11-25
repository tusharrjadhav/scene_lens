# SceneLens

SceneLens is a privacy-first Android visual assistant built with Kotlin and Jetpack Compose. The app analyzes a camera or gallery image entirely on-device, identifies visual labels and objects, extracts readable text, and exposes the results through accessible text, tappable detections, and system text-to-speech.

The project deliberately has no `INTERNET` permission. The current MVP bundles the ML Kit image-labeling and Latin OCR models; no account, upload, analytics, or cloud inference is required.

## Current MVP

- Camera capture with contextual camera permission rationale
- Android system photo picker without broad storage permission
- On-device image labeling and object detection
- On-device OCR for readable Latin-script text
- Concise scene summary and detailed, confidence-aware results
- Tappable detected-item chips and bounding-box highlights
- Text-to-speech with an immediate stop action
- `48dp`+ controls, heading semantics, live-region progress, and descriptive labels
- Camera captures deleted from app-private cache after analysis
- Device backups and image-data transfer disabled

The current summary is assembled from visual-label and object results. A vision-language description model is planned for a later phase.

## Product documents

- [Phased implementation plan](docs/IMPLEMENTATION_PLAN.md)
- [UX and accessibility recommendations](docs/UX_RECOMMENDATIONS.md)
- [Android architecture and package boundaries](docs/ARCHITECTURE.md)
- [Reusable QA images and manual test matrix](qa/test-images/README.md)

## Build

Requirements:

- JDK 17
- Android SDK 37
- Android device or emulator running API 26+

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The project uses real Gradle module boundaries for `domain`, `data:analysis`,
`core:ui`, `feature:camera`, `feature:home`, `feature:results`, and
`feature:scenelens`. The `app` module is the composition and launcher shell.
Module dependencies enforce that ML Kit data code and concrete infrastructure
cannot leak into feature presentation code.

## Privacy posture

SceneLens does not declare network access in `AndroidManifest.xml`. Selected images remain in memory for analysis. Camera captures use app-private cache files and are deleted after processing. There is no local history in the MVP.

Future capabilities that need model downloads, saved history, voice permissions, face/person context, or optional cloud processing must be added as explicit, separately gated phases. They must not weaken the no-upload default.

## Safety

Results are probabilistic visual interpretations. SceneLens is not an authoritative source for medical, food-allergy, traffic, accessibility-navigation, or emergency decisions. The UI labels uncertainty and directs users to verify important information independently.

## License

Apache-2.0. See [LICENSE](LICENSE).
