plugins { alias(libs.plugins.android.application) }

android {
  namespace = "com.example.snippets"
  compileSdk { version = release(36) }

  defaultConfig {
    applicationId = "com.example.snippets"
    minSdk = 23
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    // Required by IMA SDK v3.37.0+
    isCoreLibraryDesugaringEnabled = true

    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {
  coreLibraryDesugaring(libs.desugar)
  implementation(libs.appcompat)
  implementation(libs.material)
  implementation(libs.interactivemedia)
}
