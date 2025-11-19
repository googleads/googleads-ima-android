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
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies {
  implementation(libs.appcompat)
  implementation(libs.material)
}
