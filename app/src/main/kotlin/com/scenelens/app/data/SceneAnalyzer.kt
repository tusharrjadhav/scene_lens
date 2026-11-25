package com.scenelens.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt

interface SceneAnalyzer {
  suspend fun analyze(uri: Uri): SceneAnalysis
}

class OnDeviceSceneAnalyzer(private val context: Context) : SceneAnalyzer {
  override suspend fun analyze(uri: Uri): SceneAnalysis {
    val bitmap = decodeBitmap(uri)
    val image = InputImage.fromBitmap(bitmap, 0)
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
        val objectsDeferred = async { objectDetector.process(image).await() }
        val labelsDeferred = async { imageLabeler.process(image).await() }
        val textDeferred = async { textRecognizer.process(image).await() }
        Triple(objectsDeferred.await(), labelsDeferred.await(), textDeferred.await())
      }

      val width = bitmap.width.toFloat()
      val height = bitmap.height.toFloat()
      val objectItems = objects.mapIndexedNotNull { index, detectedObject ->
        val label = detectedObject.labels.maxByOrNull { it.confidence } ?: return@mapIndexedNotNull null
        if (label.confidence < 0.20f) return@mapIndexedNotNull null
        DetectedItem(
          id = "object-$index-${detectedObject.trackingId ?: label.index}",
          label = label.text,
          confidence = label.confidence,
          confidenceBand = confidenceBand(label.confidence),
          source = DetectionSource.OBJECT,
          bounds = detectedObject.boundingBox.toNormalizedRect(width, height),
        )
      }
      val labelItems = labels
        .filter { label -> objectItems.none { it.label.equals(label.text, ignoreCase = true) } }
        .take(8)
        .mapIndexed { index, label ->
          DetectedItem(
            id = "label-$index-${label.index}",
            label = label.text,
            confidence = label.confidence,
            confidenceBand = confidenceBand(label.confidence),
            source = DetectionSource.IMAGE_LABEL,
            bounds = NormalizedRect(0.18f, 0.18f + index * 0.035f, 0.82f, 0.82f),
          )
        }
      val textItems = text.textBlocks.mapIndexed { index, block ->
        DetectedItem(
          id = "text-$index",
          label = block.text.lineSequence().firstOrNull().orEmpty(),
          confidence = null,
          confidenceBand = null,
          source = DetectionSource.TEXT,
          bounds = (block.boundingBox ?: Rect()).toNormalizedRect(width, height),
        )
      }.filter { it.label.isNotBlank() }
      val visualItems = (objectItems + labelItems)
        .sortedByDescending { it.confidence }
        .distinctBy { it.label.lowercase() }
      val summary = buildSummary(visualItems, text.text.takeIf { it.isNotBlank() })
      val details = buildDetails(visualItems, text.text)
      return SceneAnalysis(
        image = bitmap,
        summary = summary,
        details = details,
        recognizedText = text.text.takeIf { it.isNotBlank() },
        items = visualItems + textItems,
      )
    } finally {
      objectDetector.close()
      imageLabeler.close()
      textRecognizer.close()
    }
  }

  private suspend fun decodeBitmap(uri: Uri): Bitmap = withContext(Dispatchers.IO) {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    openInput(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
    require(bounds.outWidth > 0 && bounds.outHeight > 0) {
      "The selected file is not a readable image."
    }
    val targetLongSide = 1600
    var sampleSize = 1
    while (max(bounds.outWidth, bounds.outHeight) / sampleSize > targetLongSide) {
      sampleSize *= 2
    }
    val decoded = openInput(uri).use {
      BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
    } ?: error("The image could not be decoded.")
    val orientation = openInput(uri).use { ExifInterface(it).getAttributeInt(
      ExifInterface.TAG_ORIENTATION,
      ExifInterface.ORIENTATION_NORMAL,
    ) }
    decoded.applyOrientation(orientation)
  }

  private fun openInput(uri: Uri): InputStream =
    context.contentResolver.openInputStream(uri)
      ?: error("SceneLens could not open the selected image.")

  private fun Bitmap.applyOrientation(orientation: Int): Bitmap {
    val matrix = Matrix().apply {
      when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
          postRotate(90f)
          postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
          postRotate(270f)
          postScale(-1f, 1f)
        }
        else -> return this@applyOrientation
      }
    }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true).also {
      if (it != this) recycle()
    }
  }
}

private fun Rect.toNormalizedRect(imageWidth: Float, imageHeight: Float) = NormalizedRect(
  left = (left / imageWidth).coerceIn(0f, 1f),
  top = (top / imageHeight).coerceIn(0f, 1f),
  right = (right / imageWidth).coerceIn(0f, 1f),
  bottom = (bottom / imageHeight).coerceIn(0f, 1f),
)

private fun confidenceBand(confidence: Float) = when {
  confidence >= 0.80f -> ConfidenceBand.HIGH
  confidence >= 0.55f -> ConfidenceBand.MEDIUM
  else -> ConfidenceBand.LOW
}

private fun buildSummary(items: List<DetectedItem>, text: String?): String {
  val labels = items.take(3).map { it.label.lowercase() }
  val prefix = when (items.mapNotNull { it.confidence }.maxOrNull() ?: 0f) {
    in 0.80f..1f -> "The image likely shows"
    else -> "The image may show"
  }
  val visualPart = when (labels.size) {
    0 -> "I could not identify a clear visual subject"
    1 -> "$prefix ${labels.single()}"
    2 -> "$prefix ${labels[0]} and ${labels[1]}"
    else -> "$prefix ${labels.dropLast(1).joinToString(", ")}, and ${labels.last()}"
  }
  return buildString {
    append(visualPart)
    append('.')
    if (!text.isNullOrBlank()) append(" Readable text is also present.")
  }
}

private fun buildDetails(items: List<DetectedItem>, text: String): List<String> = buildList {
  add(
    when {
      items.isEmpty() -> "No objects passed the confidence threshold. Try more light or a closer image."
      items.count { it.confidenceBand == ConfidenceBand.LOW } == items.size ->
        "These identifications are uncertain. Try another angle, stronger light, or a sharper image."
      else -> "Items with lower confidence are marked so you can verify them against the photo."
    },
  )
  if (items.isNotEmpty()) {
    add(
      items.take(8).joinToString(", ") { item ->
        item.confidence?.let { "${item.label} (${(it * 100).roundToInt()}%)" } ?: item.label
      },
    )
  }
  if (text.isNotBlank()) add("Text extraction found ${text.lineSequence().count()} line(s).")
}
