plugins {
  alias(libs.plugins.android.library)
}

android {
  namespace = "com.scenelens.app.domain"
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
