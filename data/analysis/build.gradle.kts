plugins {
  alias(libs.plugins.android.library)
}

android {
  namespace = "com.scenelens.app.data.analysis"
  compileSdk = 37
  defaultConfig {
    minSdk = 26
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

kotlin {
  jvmToolchain(17)
}

dependencies {
  implementation(project(":domain"))
  implementation(libs.mlkit.objectdetector)
  implementation(libs.mlkit.image.labeling)
  implementation(libs.mlkit.text.recognition)
  implementation(libs.kotlinx.coroutines.play.services)
  implementation(libs.androidx.exifinterface)
}
