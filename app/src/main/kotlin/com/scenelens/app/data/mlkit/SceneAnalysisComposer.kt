package com.scenelens.app.data.mlkit

import android.graphics.Bitmap
import com.scenelens.app.domain.model.ConfidenceBand
import com.scenelens.app.domain.model.DetectedItem
import com.scenelens.app.domain.model.SceneAnalysis
import kotlin.math.roundToInt

internal class SceneAnalysisComposer {
  fun compose(
    image: Bitmap,
    visualItems: List<DetectedItem>,
    textItems: List<DetectedItem>,
    recognizedText: String?,
  ): SceneAnalysis {
    val sortedVisualItems = visualItems
      .sortedByDescending { it.confidence }
      .distinctBy { it.label.lowercase() }
    val text = recognizedText.orEmpty()
    return SceneAnalysis(
      image = image,
      summary = buildSummary(sortedVisualItems, text),
      details = buildDetails(sortedVisualItems, text),
      recognizedText = recognizedText,
      items = sortedVisualItems + textItems,
    )
  }

  private fun buildSummary(items: List<DetectedItem>, text: String): String {
    val labels = items.take(3).map { it.label.lowercase() }
    val prefix = if ((items.mapNotNull { it.confidence }.maxOrNull() ?: 0f) >= 0.80f) {
      "The image likely shows"
    } else {
      "The image may show"
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
      if (text.isNotBlank()) append(" Readable text is also present.")
    }
  }

  private fun buildDetails(items: List<DetectedItem>, text: String): List<String> = buildList {
    add(
      when {
        items.isEmpty() -> "No objects passed the confidence threshold. Try more light or a closer image."
        items.all { it.confidenceBand == ConfidenceBand.LOW } ->
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
}
