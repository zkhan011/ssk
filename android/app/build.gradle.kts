plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android { namespace = "com.ssk.kiosk"; compileSdk = 35
 defaultConfig { applicationId = "com.ssk.kiosk"; minSdk = 31; targetSdk = 35; versionCode = 1; versionName = "1.0.0"
  buildConfigField("String", "KIOSK_URL", "\"${System.getenv("ANDROID_KIOSK_URL") ?: "https://kiosk.example.invalid"}\"")
 }
 buildTypes { debug { applicationIdSuffix = ".debug"; buildConfigField("boolean", "ALLOW_CLEARTEXT", "true") }; release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"); buildConfigField("boolean", "ALLOW_CLEARTEXT", "false") } }
 buildFeatures { buildConfig = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
}
