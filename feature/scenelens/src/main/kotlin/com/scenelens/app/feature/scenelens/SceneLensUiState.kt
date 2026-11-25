package com.scenelens.app.feature.scenelens

import com.scenelens.app.domain.model.SceneAnalysis

data class SceneLensUiState(
  val isAnalyzing: Boolean = false,
  val isCameraOpen: Boolean = false,
  val analysis: SceneAnalysis? = null,
  val selectedItemId: String? = null,
  val errorMessage: String? = null,
)
