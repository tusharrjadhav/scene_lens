# SceneLens Android architecture

SceneLens uses a single Gradle application module with package-level feature modularity. This keeps the current MVP simple to build while giving each future capability a stable boundary that can become a Gradle module when independently built features justify the added build cost.

## Package boundaries

```text
com.scenelens.app
├── core
│   ├── clipboard    # system clipboard adapter
│   ├── speech       # TTS lifecycle and playback adapter
│   └── ui           # reusable UI tokens, labels, and permission UI
├── data
│   ├── image        # bitmap decoding, orientation, and downsampling
│   └── mlkit        # ML Kit processing, mapping, and result composition
├── di               # application dependency container
├── domain
│   ├── model        # scene-analysis models
│   └── AnalyzeSceneUseCase
└── feature
    ├── camera       # CameraX preview and capture lifecycle
    ├── home         # start and input-selection UI
    ├── results      # image overlays, summary, item, and OCR UI
    └── scenelens    # feature coordinator, screen state, and ViewModel
```

## Dependency rules

- `domain` has no dependency on `data`, `feature`, ML Kit, CameraX, or Compose. It currently uses Android's `Uri` and `Bitmap` platform types because the MVP remains a single Android app; extraction into a pure Kotlin module would first replace these with platform abstractions.
- `data` implements domain use cases and may depend on Android and third-party ML/image libraries.
- `feature` depends on `domain` and `core`; it does not instantiate ML Kit clients or decode image bytes.
- `core` contains reusable, product-agnostic helpers. It must not depend on feature packages.
- `di` is the only production composition point that wires concrete data implementations to domain interfaces.
- `MainActivity` owns the UI shell and does not construct model or data services.

## State and data flow

```text
Camera/gallery event
    -> SceneLensApp
    -> SceneLensViewModel
    -> AnalyzeSceneUseCase
    -> BitmapImageDecoder + ML Kit adapters
    -> immutable SceneLensUiState
    -> focused feature composables
```

`SceneLensViewModel` is the state owner. Composables receive immutable state and callbacks and do not coordinate image processing. New analysis cancels the previous job, and one-shot errors are cleared after the UI displays them.

## Composition strategy

`SceneLensApplication` owns a small manual `AppContainer`. The container keeps construction and lifecycle explicit without adding a dependency-injection framework before the app needs one. `SceneLensViewModel.Factory` reads the use case from that container, making the ViewModel independently constructible in local tests.

## UI conventions

- Feature screens are split from reusable cards, rows, image overlays, and permission UI.
- Shared touch-target and action-height values live in `SceneLensUiTokens`.
- User-visible confidence labels are resource-backed and resolved in the UI layer rather than embedded in domain types.
- Composables expose state plus callbacks; implementation details such as CameraX controllers and TTS engines remain in small lifecycle owners.
- Large-flow files should not mix camera, home, and result responsibilities.

## Testing

- Unit tests construct ViewModels with `AnalyzeSceneUseCase` fakes and do not require Android application wiring.
- Compose tests target feature screens through public/internal behavior and meaningful text/semantics.
- Data adapters should gain pure mapping tests and image-decoder tests as their inputs diversify.
- Camera integration remains an instrumented/device concern.

## When to extract Gradle modules

Keep package modularity until at least one of these becomes true:

- another feature needs to consume the domain model independently;
- a feature needs an isolated build, ownership boundary, or different dependency graph;
- ML/data dependencies materially slow unrelated UI compilation;
- app, benchmark, or alternate-form-factor targets need shared code.

At that point, extract in this order: `:core:domain`, `:core:ui`, `:data:analysis`, then individual `:feature:*` modules. Preserve the dependency direction above rather than moving code mechanically.
