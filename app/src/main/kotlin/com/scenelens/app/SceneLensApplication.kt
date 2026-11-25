package com.scenelens.app

import android.app.Application
import com.scenelens.app.di.AppContainer
import com.scenelens.app.di.DefaultAppContainer

class SceneLensApplication : Application() {
  lateinit var container: AppContainer
    private set

  override fun onCreate() {
    super.onCreate()
    container = DefaultAppContainer(this)
  }
}
