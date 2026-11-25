package com.scenelens.app.domain

import android.net.Uri
import com.scenelens.app.domain.model.SceneAnalysis

fun interface AnalyzeSceneUseCase {
  suspend operator fun invoke(uri: Uri): SceneAnalysis
}
