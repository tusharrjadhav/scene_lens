package com.scenelens.app.di

import android.content.Context
import com.scenelens.app.data.image.BitmapImageDecoder
import com.scenelens.app.data.mlkit.MlKitSceneAnalyzer
import com.scenelens.app.domain.AnalyzeSceneUseCase

interface AppContainer {
  val analyzeSceneUseCase: AnalyzeSceneUseCase
}

class DefaultAppContainer(context: Context) : AppContainer {
  private val imageDecoder = BitmapImageDecoder(context.applicationContext)

  override val analyzeSceneUseCase: AnalyzeSceneUseCase = MlKitSceneAnalyzer(imageDecoder)
}
