# SceneLens UX and accessibility recommendations

## Recommended experience

Use a **photo-first, result-first** flow for the MVP. Keep the first screen simple enough to be understood through TalkBack:

1. `Take a photo` or `Choose from gallery`
2. A short `Looking at your image` state
3. One concise result
4. Optional detail, tappable items, bounding boxes, and recognized text
5. `Read aloud`, `Stop`, and `Choose another photo`

Later, the home becomes a mode selector: **Describe**, **Read**, **Find**, and **Food**. Modes should be explicit because the expected result and safety language differ.

## Research-backed UI decisions

### Accessibility is a product workflow, not a layer

- Use meaningful Compose semantics, roles, heading navigation, state descriptions, and custom action labels. Icons that only decorate text should have no content description.
- Make every interactive target at least `48dp`; use `56dp` to `80dp` for capture, stop, and primary actions.
- Do not expose state by color alone. A selected object should have text, a distinct outline, and a state announcement.
- Use font sizes in `sp`, support 200% font scale, and keep controls out of overlaid image gestures.
- The reading order should be: mode/title, image, concise result, speech controls, item choices, detail, then next action.
- Mark short state changes as polite live regions. Do not continuously announce camera analysis frames.

### Camera and gallery

- Keep Camera and Gallery visible together on the first screen. Denying the camera must not block the rest of the app.
- Request camera permission only after the user activates capture and has seen the reason.
- Use the system photo picker so selecting one photo does not grant access to the whole library.
- A live preview needs an explicit capture action; tap-to-focus is helpful but must not be the only focus method.
- Give “camera in use,” no permission, capture failure, blur, and low-light states clear recovery actions.
- CameraX should release the camera when the app stops and use a latest-only strategy for any live analyzer.

### Concise first, detail second

Generated descriptions are not equivalent to authored alternative text and can miss user intent. The UI therefore should:

- Lead with one sentence.
- Preserve visibly uncertain language.
- Let the user open detail rather than reading every detector result by default.
- Separate direct observations, OCR, and predictions.
- Offer `retake`, `move closer`, or `more light` when confidence is low.
- Never show a bare confidence number as if model scores from different systems were interchangeable.

Recommended wording:

| Confidence | Visible and spoken wording |
| --- | --- |
| High | “The image likely shows…” |
| Medium | “The image may show…” |
| Low | “I’m not sure. It might be… Try a sharper image or more light.” |

### Tappable detections

- Use the image box and a chip/list representation together. Users who cannot target a small box can select the corresponding text item.
- The selected item expands into one accessible description, source, and confidence.
- Bounding boxes use `3dp` strokes, a thicker selected state, and a high-contrast shape; they are not the only target.
- OCR blocks and visual detections should be identified by type so text is not mistaken for an object.

### Speech and TalkBack coexistence

- Reserve app TTS for deliberate long-result playback.
- Use accessibility announcements for short UI state changes.
- A new `Read aloud` request interrupts the previous result.
- `Stop` remains visible and reachable without speech or gesture discovery.
- Add rate, pitch, voice sample, and reset controls in a later settings screen. Prefer step buttons to a slider for this accessibility use case.

### Privacy in the interface

- Show `On-device · offline · no upload` before the first capture.
- Do not request microphone, location, contacts, notifications, or broad storage access in the MVP.
- Explain the exact difference between bundled, downloadable, and cloud models.
- If a future model pack is downloaded, show package purpose, size, status, cancel, and delete.
- Any cloud mode must be a separate, per-image confirmation with “This image leaves your device,” never a global default.

### Specialist food mode

- Ask users to choose `Food` explicitly.
- Prefer printed ingredients when text is available.
- Phrase visual ingredients as “possible” or “may be visible.”
- Never state that an image proves an allergen is absent or that a dish is safe.
- Give prominent next actions: scan the package text, ask staff, or use another image.

## Screen-level recommendation

### Start screen

- Privacy badge
- One-sentence purpose statement
- Large camera action
- Large system-gallery action
- Three capability rows: objects, text, confidence
- Short privacy explanation

The camera button can be more visually prominent, but gallery must remain the same control size.

### Result screen

- Image with non-color-only outlines
- One-sentence result
- `Read aloud` and `Stop`
- Selected-item card
- Horizontally scrollable/tappable item choices
- Detailed breakdown
- OCR card with copy
- `Choose another photo`
- Safety note

### Camera screen

- Live preview occupies the screen.
- Top-left close action with at least `48dp`.
- Bottom `80dp` capture control.
- Simple alignment and lighting hint.
- No capture-zone masking that hides important content.
- Future live OCR or finder results should appear below or over the preview without rapid focus changes.

## Acceptance journeys

1. **TalkBack capture:** open app, hear privacy statement, open camera rationale, grant permission, capture, hear result, stop speech, start another image.
2. **Gallery without camera permission:** decline camera, select a system-picker image, complete analysis.
3. **Low confidence:** select a low-confidence object, hear uncertainty and the recovery suggestion.
4. **OCR:** extract multi-line text, copy it, and hear it without upload.
5. **Failure:** select an unreadable file or encounter a model/camera error, hear the reason, and retry.
6. **Scale:** complete journeys at 200% font scale and maximum display size.

## Authoritative references

- [Accessibility principles](https://developer.android.com/guide/topics/ui/accessibility/principles)
- [Semantics in Compose](https://developer.android.com/develop/ui/compose/accessibility/semantics)
- [Compose accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults)
- [Test your app's accessibility](https://developer.android.com/guide/topics/ui/accessibility/testing)
- [Android privacy and runtime permission guidance](https://developer.android.com/privacy)
- [Request runtime permissions in context](https://developer.android.com/training/permissions/requesting)
- [System photo picker](https://developer.android.com/training/data-storage/shared/photo-picker)
- [CameraX image capture](https://developer.android.com/media/camera/camerax/take-photo)
- [CameraX image analysis](https://developer.android.com/media/camera/camerax/analyze)
- [ML Kit overview: on-device and offline](https://developers.google.com/ml-kit/)
- [ML Kit image labeling](https://developers.google.com/ml-kit/vision/image-labeling/android)
- [ML Kit text recognition v2](https://developers.google.com/ml-kit/vision/text-recognition/v2/android)
- [ML Kit object detection](https://developers.google.com/ml-kit/vision/object-detection/android)
- [Android touch target guidance](https://support.google.com/accessibility/android/answer/7101858)
- [W3C work on AI and accessibility](https://w3c.github.io/ai-accessibility/)
- [Google Play Data safety](https://support.google.com/googleplay/android-developer/answer/10787469)

## Product references

Google Lookout and Microsoft Seeing AI informed the mode-based direction: short text, document, describe/explore, product, and object-finding flows are useful categories. SceneLens borrows interaction principles only; it does not copy branding, assets, or proprietary behavior.

- [Google Lookout help](https://support.google.com/accessibility/android/answer/9031274)
- [Seeing AI overview](https://www.guidedogs.org.uk/getting-support/information-and-advice/how-can-technology-help-me/apps/seeing-ai/)
