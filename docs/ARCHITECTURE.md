# SceneLens Android architecture

SceneLens uses Gradle modules so dependency direction is enforced by the build system.

## Module graph

```text
:app
├── :feature:scenelens
│   ├── :feature:camera
│   │   └── :core:ui
│   ├── :feature:home
│   │   └── :core:ui
│   ├── :feature:results
│   │   ├── :domain
│   │   └── :core:ui
│   ├── :core:ui
│   └── :domain
├── :data:analysis
│   └── :domain
├── :core:ui
└── :domain
```

The graph has one composition root (`:app`) and no feature-to-feature dependency cycle.

## Module responsibilities

### `:app`

- Android `Application` and launcher activity
- Process-level dependency container
- Manifest, launcher resources, backup policy, and packaging

The app shell does not contain camera, ML, or result-screen implementation. It wires `AnalyzeSceneUseCase` into the coordinator feature.

### `:domain`

- Scene analysis models
- Detection confidence and source types
- `AnalyzeSceneUseCase`

This module has no dependency on ML Kit, CameraX, Compose, or application code. It currently uses Android's `Uri` and `Bitmap` platform types, which keeps the MVP practical while preserving a small, replaceable domain API.

### `:data:analysis`

- Image decoding, EXIF orientation, and downsampling
- ML Kit object detection, image labeling, and OCR
- ML Kit-to-domain mapping
- Scene summary and detailed-result composition

Only the analysis implementation knows ML Kit. Features receive domain models through the use-case interface.

### `:core:ui`

- Material theme and shared typography/colors
- Shared touch-target/action-size tokens
- Resource-backed confidence labels and copy
- TTS and clipboard adapters
- Reusable camera-permission rationale UI

### `:feature:camera`

- CameraX lifecycle and preview UI
- In-memory capture state
- Temporary photo-file lifecycle and capture errors
- Camera manifest feature and permission declarations

### `:feature:home`

- Start experience
- Camera/gallery actions
- Capability and privacy explanations

### `:feature:results`

- Analyzed image and bounding-box overlay
- Summary and TTS controls
- Tappable detections and confidence cards
- OCR copy UI

### `:feature:scenelens`

- Product flow coordination
- `SceneLensViewModel` and UI state
- Camera/gallery result launchers
- Home/progress/result screen switching

The coordinator depends on domain interfaces and sibling UI features, not on `:data:analysis` or `SceneLensApplication`.

## Dependency rules

- `:domain` depends on no project module.
- `:data:*` and `:feature:*` may depend on `:domain`; `:domain` never depends back.
- Features do not depend on `:app`.
- Features do not instantiate ML Kit clients.
- Only `:app` knows the concrete data implementation and composes dependencies.
- Shared system/UI adapters belong in `:core:ui`; they must not depend on a feature.

## State and data flow

```text
camera or photo-picker event
    -> :feature:scenelens
    -> SceneLensViewModel
    -> AnalyzeSceneUseCase
    -> :data:analysis
    -> immutable SceneLensUiState
    -> camera/home/results composables
```

New analysis cancels the previous job. UI composables receive state and callbacks rather than coordinating image decoding or model clients.

## Testing ownership

- `:feature:scenelens` owns ViewModel and coordinator Compose tests.
- Add pure ML mapping/composition tests to `:data:analysis`.
- Add camera lifecycle tests to `:feature:camera` only when they are device-oriented.
- Move tests with the implementation they verify instead of keeping a centralized app test directory.

## Extending the structure

Add a new vertical feature as `:feature:<name>`. It can depend on `:domain` and `:core:ui`; it should not depend on unrelated features. Add `:feature:food` for dish/ingredient recognition when its model and safety evaluation are ready, backed by either an additional `:data:food` adapter or a domain implementation registered in `:app`.

Future pure business logic can be moved into Kotlin/JVM modules after Android `Uri`/`Bitmap` dependencies are replaced by platform-neutral input and image abstractions.
