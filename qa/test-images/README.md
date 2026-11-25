# SceneLens QA image set

These JPEGs are QA fixtures for manual on-device testing. They are not Android
resources and are not packaged in the APK.

Copy one image at a time to a test device or emulator with:

```bash
adb push qa/test-images /sdcard/Pictures/SceneLensQA
```

Then select it through **Choose from gallery**. Do not expect an exact model
label or confidence score: ML Kit can vary across versions and image conditions.
Validate the behavior stated below.

## Test matrix

| File | Main coverage | Expected behavior |
| --- | --- | --- |
| `everyday-laptop-cc0.jpg` | Multiple objects, relationships, tappable chips, bounding boxes, concise and detailed results | Detects a laptop/table and may detect cup, keyboard, phone, or wood. Summary remains concise, detail lists evidence, and object chips/highlights can be selected. OCR may find “MacBook”. |
| `packaging-label-cc0.jpg` | Dense Latin OCR, readable text, copy action, small text | Recognizes substantial packaging text such as `Cooking Guidelines`, `Nutrition`, or ingredient/allergy wording. Result includes a recognized-text card and Copy can be activated. Check that OCR text is called extracted text, not a medical or allergy guarantee. |
| `street-sign-cc0.jpg` | Clean Latin OCR from a low-resolution image | Recognizes `HARLEY`, `STREET`, `W1`, and/or `CITY OF WESTMINSTER`. Check graceful behavior when the image only contains a few labels. |
| `food-table-public-domain.jpg` | Food labels, many objects, visual density, future food-mode baseline | Labels several visible foods and summarizes the mixed scene without claiming a complete ingredient list. This is useful for the future `Food` phase but must not imply allergy safety. |
| `uncertain-low-light-blur.jpg` | Blur, low light, lower confidence, recovery guidance | Fewer/weaker detections are acceptable. The result must use uncertainty wording and suggest better light, a sharper image, or another angle rather than presenting weak results as facts. |
| `ocr-exif-rotated.jpg` | EXIF orientation normalization, OCR after rotation | Appears upright in the result and readable text blocks remain correctly located. The fixture stores an EXIF orientation tag; raw encoded pixels are rotated. |

## Capability checklist

### Gallery and privacy

- Open **Choose from gallery** with no camera permission granted.
- Select every fixture through the system photo picker.
- Confirm the app does not request broad photo-library access.
- Put the device in airplane mode and confirm ML analysis still runs.
- Inspect the app's merged manifest and verify `INTERNET` is absent.

### Camera-only checks

The fixtures are for gallery/input and model tests. Cover the camera-specific
path separately:

1. Deny camera access and return to the app.
2. Tap **Take a photo**; the in-context rationale must explain camera use.
3. Deny again and confirm gallery remains usable.
4. Grant camera access, capture a printed version of the street-sign or label
   image, and confirm capture returns to analysis.
5. After analysis, check that `scenelens-*.jpg` no longer exists in the
   app-private cache.

### Accessibility and speech

- Complete gallery selection, item selection, Read aloud, Stop, and Choose
  another photo using TalkBack.
- Confirm controls have at least a `48dp` touch target and primary actions are
  at least `56dp`.
- Check that progress is announced once as a polite state, not continuously.
- Check reading order: result image, summary, speech controls, detected items,
  details, OCR, then next action.
- Start TTS, then press Stop immediately and verify speech ends.
- Test at 200% font scale and largest display size.

### Result checks

- Select at least two item chips and verify the corresponding state is exposed
  and a bounding box becomes selected when the item has bounds.
- Verify image-label chips without a meaningful box do not create a false box.
- Verify OCR blocks are identified as extracted text and do not receive invented
  confidence percentages.
- Verify low-confidence output contains a recovery action.

## Sources and licensing

The original four files are from Wikimedia Commons. They were visually inspected
before inclusion and are stored at a practical test resolution. Derived fixtures
preserve the source work's license/status.

1. [`everyday-laptop-cc0.jpg`](everyday-laptop-cc0.jpg)  
   Source: [A laptop on the wooden table (2, Pixabay).jpg](https://commons.wikimedia.org/wiki/File:A_laptop_on_the_wooden_table_(2,_Pixabay).jpg)  
   Status: **CC0 1.0 Universal / public-domain dedication**. Original uploader:
   Pixabay contributor represented on the Commons file page.

2. [`packaging-label-cc0.jpg`](packaging-label-cc0.jpg)  
   Source: [Food label from the Co-operative Food Sausages.jpg](https://commons.wikimedia.org/wiki/File:Food_label_from_the_Co-operative_Food_Sausages.jpg)  
   Status: **CC0 1.0 Universal / public-domain dedication**. Author:
   Malcomburetta (Commons file page).

3. [`street-sign-cc0.jpg`](street-sign-cc0.jpg)  
   Source: [HarleyStreetSign.JPG](https://commons.wikimedia.org/wiki/File:HarleyStreetSign.JPG)  
   Status: **CC0 1.0 Universal / public-domain dedication**.

4. [`food-table-public-domain.jpg`](food-table-public-domain.jpg)  
   Source: [Fruit, Vegetables and Grain NCI Visuals Online.jpg](https://commons.wikimedia.org/wiki/File:Fruit,_Vegetables_and_Grain_NCI_Visuals_Online.jpg)  
   Status: **Public domain**, U.S. National Cancer Institute / National
   Institutes of Health; unknown author. Reuse restrictions listed as none,
   with credit requested to the source.

5. `uncertain-low-light-blur.jpg` is a darkened and blurred derivative of image
   1, made only to test degraded input.

6. `ocr-exif-rotated.jpg` is an EXIF-orientation derivative of image 2, made only
   to test orientation normalization.
