package com.scenelens.app.data.mlkit

import android.graphics.Rect
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.text.Text
import com.scenelens.app.domain.model.ConfidenceBand
import com.scenelens.app.domain.model.DetectedItem
import com.scenelens.app.domain.model.DetectionSource
import com.scenelens.app.domain.model.NormalizedRect

internal class MlKitResultMapper(
  private val imageWidth: Float,
  private val imageHeight: Float,
) {
  fun mapObjects(objects: List<DetectedObject>): List<DetectedItem> =
    objects.mapIndexedNotNull { index, detectedObject ->
      val label = detectedObject.labels.maxByOrNull { it.confidence } ?: return@mapIndexedNotNull null
      if (label.confidence < MIN_OBJECT_CONFIDENCE) return@mapIndexedNotNull null
      DetectedItem(
        id = "object-$index-${detectedObject.trackingId ?: label.index}",
        label = label.text,
        confidence = label.confidence,
        confidenceBand = confidenceBand(label.confidence),
        source = DetectionSource.OBJECT,
        bounds = detectedObject.boundingBox.normalized(),
      )
    }

  fun mapImageLabels(
    labels: List<com.google.mlkit.vision.label.ImageLabel>,
    existingItems: List<DetectedItem>,
  ): List<DetectedItem> = labels
    .filter { label -> existingItems.none { it.label.equals(label.text, ignoreCase = true) } }
    .take(MAX_IMAGE_LABELS)
    .mapIndexed { index, label ->
      DetectedItem(
        id = "label-$index-${label.index}",
        label = label.text,
        confidence = label.confidence,
        confidenceBand = confidenceBand(label.confidence),
        source = DetectionSource.IMAGE_LABEL,
        bounds = NormalizedRect(
          left = 0.18f,
          top = 0.18f + index * 0.035f,
          right = 0.82f,
          bottom = 0.82f,
        ),
      )
    }

  fun mapTextBlocks(text: Text): List<DetectedItem> =
    text.textBlocks.mapIndexed { index, block ->
      DetectedItem(
        id = "text-$index",
        label = block.text.lineSequence().firstOrNull().orEmpty(),
        confidence = null,
        confidenceBand = null,
        source = DetectionSource.TEXT,
        bounds = (block.boundingBox ?: Rect()).normalized(),
      )
    }.filter { it.label.isNotBlank() }

  private fun Rect.normalized() = NormalizedRect(
    left = (left / imageWidth).coerceIn(0f, 1f),
    top = (top / imageHeight).coerceIn(0f, 1f),
    right = (right / imageWidth).coerceIn(0f, 1f),
    bottom = (bottom / imageHeight).coerceIn(0f, 1f),
  )

  private companion object {
    const val MIN_OBJECT_CONFIDENCE = 0.20f
    const val MAX_IMAGE_LABELS = 8

    fun confidenceBand(confidence: Float) = when {
      confidence >= 0.80f -> ConfidenceBand.HIGH
      confidence >= 0.55f -> ConfidenceBand.MEDIUM
      else -> ConfidenceBand.LOW
    }
  }
}
