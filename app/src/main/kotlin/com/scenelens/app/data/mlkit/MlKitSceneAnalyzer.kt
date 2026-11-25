package com.scenelens.app.data.mlkit

import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.scenelens.app.data.image.BitmapImageDecoder
import com.scenelens.app.domain.AnalyzeSceneUseCase
import com.scenelens.app.domain.model.SceneAnalysis
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class MlKitSceneAnalyzer(
  private val imageDecoder: BitmapImageDecoder,
) : AnalyzeSceneUseCase {
  override suspend fun invoke(uri: Uri): SceneAnalysis {
    val bitmap = imageDecoder.decode(uri)
    val inputImage = InputImage.fromBitmap(bitmap, 0)
    val objectDetector = ObjectDetection.getClient(
      ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
        .enableMultipleObjects()
        .enableClassification()
        .build(),
    )
    val imageLabeler = ImageLabeling.getClient(
      ImageLabelerOptions.Builder()
        .setConfidenceThreshold(0.25f)
        .build(),
    )
    val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    try {
      val (objects, labels, text) = coroutineScope {
        val objects = async { objectDetector.process(inputImage).await() }
        val labels = async { imageLabeler.process(inputImage).await() }
        val text = async { textRecognizer.process(inputImage).await() }
        Triple(objects.await(), labels.await(), text.await())
      }
      val mapper = MlKitResultMapper(bitmap.width.toFloat(), bitmap.height.toFloat())
      val objectItems = mapper.mapObjects(objects)
      val labelItems = mapper.mapImageLabels(labels, objectItems)
      return SceneAnalysisComposer().compose(
        image = bitmap,
        visualItems = objectItems + labelItems,
        textItems = mapper.mapTextBlocks(text),
        recognizedText = text.text.takeIf { it.isNotBlank() },
      )
    } finally {
      objectDetector.close()
      imageLabeler.close()
      textRecognizer.close()
    }
  }
}
