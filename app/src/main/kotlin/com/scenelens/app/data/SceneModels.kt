package com.scenelens.app.data

import android.graphics.Bitmap

data class NormalizedRect(
  val left: Float,
  val top: Float,
  val right: Float,
  val bottom: Float,
)

enum class ConfidenceBand(val label: String) {
  HIGH("Likely"),
  MEDIUM("Possibly"),
  LOW("Uncertain"),
}

enum class DetectionSource {
  OBJECT,
  IMAGE_LABEL,
  TEXT,
}

data class DetectedItem(
  val id: String,
  val label: String,
  val confidence: Float?,
  val confidenceBand: ConfidenceBand?,
  val source: DetectionSource,
  val bounds: NormalizedRect,
)

data class SceneAnalysis(
  val image: Bitmap,
  val summary: String,
  val details: List<String>,
  val recognizedText: String?,
  val items: List<DetectedItem>,
)

data class SceneLensUiState(
  val isAnalyzing: Boolean = false,
  val isCameraOpen: Boolean = false,
  val analysis: SceneAnalysis? = null,
  val selectedItemId: String? = null,
  val errorMessage: String? = null,
)
