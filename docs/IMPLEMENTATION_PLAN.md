# SceneLens phased implementation plan

## Product decision

Build SceneLens as an **accessibility-first, local-by-default image assistant**, then add live analysis and specialist modes only after the still-image flow is reliable. A new user should understand the image without understanding model internals: concise result, optional detail, explicit uncertainty, and a persistent way to hear or stop speech.

## Architecture

```text
Compose UI
  -> SceneLensViewModel / UI state
    -> Scene analysis interface
      -> image decoder and orientation normalizer
      -> bundled ML Kit object detector
      -> bundled ML Kit image labeler
      -> bundled ML Kit OCR
      -> future LiteRT / GenAI adapters
```

### Invariants

- One active image analysis at a time; starting another analysis cancels the previous job.
- No feature may silently add network access. Any future model pack or cloud path requires explicit user consent and a visible processing-location label.
- Images are held in memory unless the user later opts into saving them.
- Domain results retain model confidence and source. Raw percentages are explanatory details, not the primary spoken result.
- Accessibility is part of each phase's exit criteria, not a final polish pass.
- Model outputs do not establish medical, allergy, navigation, identity, or legal facts.

## Phase 0 — Definition and measurement

**Goal:** establish the evaluation set and guardrails before expanding model scope.

- Recruit blind, low-vision, motor-accessibility, and sighted testers.
- Create a test image set spanning people, indoor and outdoor scenes, blur, glare, low light, dense text, packaging, documents, and food.
- Define task completion, false-positive, latency, crash-free, TalkBack, TTS-collision, and battery metrics.
- Create a product privacy matrix: input, model, processor, storage duration, network use, and user control for each feature.
- Establish the language used for high, medium, and low confidence and safety-critical limitations.

**Exit gate**

- Baseline result format and confidence language approved with test users.
- Evaluation corpus and privacy matrix committed.
- No feature enters implementation without a measurable acceptance criterion.

## Phase 1 — Still-image MVP

**Status:** initial implementation in this repository.

- Compose home experience with equally prominent camera and gallery entry points.
- Camera permission requested in context after a plain-language rationale.
- System photo picker; no broad media-library permission.
- CameraX still capture in app-private temporary storage.
- On-device object detection, image labeling, and OCR.
- Concise summary plus detailed breakdown.
- Tappable items and corresponding bounding boxes.
- Text-to-speech replay and always-visible stop control.
- Clear likely / possible / uncertain labels.
- No account, analytics, local history, face recognition, or upload.

**Remaining in Phase 1**

- Manual TalkBack pass and Accessibility Scanner run on at least three real devices.
- Verify 200% font scale and the largest display-size setting without clipping.
- Add image fixtures and a model-output snapshot/evaluation job.
- Verify merged manifest has no `INTERNET` permission.
- Measure end-to-end P50/P95 latency and peak memory on representative low-, mid-, and high-end devices.
- Replace the generic camera preview integration with the Compose-first CameraX viewfinder if its production API meets the supported device matrix.

**Exit gate**

- Camera and gallery flow complete offline.
- TalkBack-only user can capture/select, inspect the first result, hear it, stop it, and start again.
- Every interactive target is at least `48dp`; primary capture and stop actions are at least `56dp`.
- OCR and visual detection work in airplane mode.
- Camera captures are deleted after processing and no image leaves the process.

## Phase 2 — Accessible descriptions and OCR modes

**Goal:** make the result useful for real visual-assistance tasks rather than a list of labels.

- Add `Describe`, `Read`, and `Find` modes.
- Add a small on-device vision-language model adapter for a concise natural-language caption and a user-requested detailed version.
- Keep ML Kit object labels and OCR as deterministic fallbacks when the caption model is unavailable.
- Add tapped-item follow-up questions using grounded evidence only: the selected bounding box, nearby text, and detector labels.
- Split OCR into:
  - **Short text:** live, deduplicated speech.
  - **Document:** capture with alignment guidance and stable reading order.
- Add translation behind the same processing-location disclosure. Download language packs only after the user confirms size and transport.
- Add TTS rate and pitch controls, voice selection, a sample, and reset to system defaults.
- Announce only result/state changes; never stream every detector frame through a live region.
- Add copy, share-text, and opt-in local save actions. Sharing exports text, not the image, by default.

**Technical work**

- Introduce `VisionCaptioner`, `TextExtractor`, `ObjectFinder`, and `Translator` ports with model-specific adapters.
- Use per-model normalized output and calibration metadata instead of treating confidence scores as universally comparable.
- Run camera or model work off the main thread and cancel on lifecycle stop.
- Check GenAI/device feature availability at runtime; never make the core flow depend solely on a beta model.

**Exit gate**

- Brief caption is available without opening detail.
- Live OCR speaks a stable line once rather than repeating every frame.
- The app explains model unavailability and offers a working fallback.
- Translation packs are visibly downloadable/deletable; no model downloads silently.

## Phase 3 — Live object finder

**Goal:** help users locate specific things from the camera feed.

- Add a user-selected target list from categories a validated detector can actually support.
- Use CameraX `ImageAnalysis` with `STRATEGY_KEEP_ONLY_LATEST`.
- Use a custom LiteRT detector when the stock five broad ML Kit object classes are insufficient.
- Provide spoken left/right/up/down or centred guidance, bounded to state changes.
- Add a pitch/earcon and optional haptic pulse for proximity or centring.
- Show a high-contrast box for low-vision users, with text and shape cues rather than color alone.
- Provide a one-action `Stop finding` control and avoid analysis while the app is backgrounded.

**Exit gate**

- Finding is faster than scanning manually for the validated target set.
- False direction announcements meet the agreed rate.
- Speech is throttled and does not collide with TalkBack.
- Preview remains responsive under slow inference.

## Phase 4 — Food and ingredient recognition

**Goal:** from a food image, identify the likely dish and possible visible ingredients without overstating what a camera can know.

This is a separate specialist phase requested for the roadmap, not a claim made by the current generic classifier.

### Product shape

- Add a `Food` mode with a still-image-first flow.
- Return:
  1. A likely dish/category or “not enough information”.
  2. Candidate visible ingredients with confidence tiers.
  3. Visible food components the model cannot identify.
  4. A plain warning that hidden ingredients, recipes, cross-contamination, portion size, and allergens cannot be established from an image.
- Never present an ingredient or allergen as certain when it is inferred from appearance alone.
- For restaurant menus or packaged food, prefer OCR of the printed ingredient list over visual ingredient guessing and show the source as “printed text”.
- Add optional user correction so evaluation data can be labeled, but never upload corrections automatically.
- Do not add person/family recognition or use faces to infer identity, diet, or health.

### Technical work

- Assemble a licensed, culturally diverse food dataset with multi-ingredient labels and difficult lookalike pairs.
- Train or select an on-device, quantized classifier/segmenter and compare it with ML Kit's generic food labels.
- Separate **dish recognition**, **visible ingredient detection**, and **printed allergen extraction** into independently calibrated results.
- Evaluate across cuisine, lighting, occlusion, mixed dishes, packaging, and ambiguous meals.
- Add a model-pack download manager only if the selected model cannot be bundled; show size, status, cancel, and delete.
- Store no food image or result by default.

### Safety wording

- “I may see tomato and leafy greens” rather than “This contains tomato.”
- “Check the package or ask staff” for allergy-sensitive decisions.
- “Ingredient predictions are not a reliable allergy check.”
- Avoid nutritional, calorie, medication, or treatment conclusions from food appearance.

**Exit gate**

- Per-class precision/recall and calibration published for the supported food set.
- A dedicated allergy-safety test fails closed: uncertain or missing evidence is never expressed as absence.
- Low-confidence food results offer OCR, another angle, more light, or human verification.
- No user can confuse “ingredient not detected” with “ingredient absent”.

## Phase 5 — Local organization and scene timeline

- Opt-in encrypted local history.
- Searchable local descriptions such as “red umbrella on a beach.”
- Per-result delete and clear-all control.
- Scene timeline for an explicit live-session recording state, never an unnoticed background capture.
- Local thumbnail lifecycle and storage budget.
- Voice conversation with a visible listening state; request microphone permission only when that mode is opened.

**Exit gate**

- Saving is explicit, searchable, removable, and encrypted.
- No timeline frame exists outside an active, visible session.

## Phase 6 — Optional specialty packs and cloud enhancement

- Downloadable offline packs for travel, nature, fashion, food, or workplace labels.
- Optional cloud caption mode only for a specific complex scene, with a confirmation showing that the image will be sent.
- Keep an offline-local setting as the default and provide a network kill-switch.
- Revisit Play Data Safety and privacy disclosures for every added service.
- Never add cloud analytics, crash reporting, face recognition, or person context by default.

## Release gates

### Functional

- No P0/P1 crash, data-loss, capture, or wrong-navigation defect.
- Preview, capture, analysis, retry, denial, and lifecycle paths pass on the support-device matrix.

### Accessibility

- Manual TalkBack and Switch Access journeys pass.
- Meaningful icons and images have descriptions; decorative icons do not.
- State is exposed through role/state description, not visual styling alone.
- Large font and display scale do not hide the primary action.

### Privacy

- Merged manifest and traffic inspection match the disclosed feature set.
- Captured images are not written or retained beyond the documented lifetime.
- Any future download or cloud call has explicit, cancellable UI.

### AI quality

- Accuracy is reported by task and confidence band.
- Low-confidence states suggest a next action.
- The model card documents supported inputs, known failures, class scope, and last evaluation.

## Implementation sequence

1. Finish Phase 1 device validation and model fixtures.
2. Add settings and model/service abstraction hardening.
3. Prototype an on-device caption model and keep the generic ML fallback.
4. Add OCR live/document modes and translation.
5. Prototype live object finding with a limited validated target set.
6. Run a food-model spike and safety evaluation before committing to Phase 4 UI.
7. Add local history only after privacy and deletion UX are tested.
