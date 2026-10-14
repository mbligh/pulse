plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.mbligh.pulse"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.mbligh.pulse"
        minSdk = 29
        targetSdk = 36
        versionCode = System.getenv("PULSE_VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = "0.1.0"
    }
    signingConfigs {
        create("playUpload") {
            val keyPath = System.getenv("PULSE_UPLOAD_KEYSTORE")
            if (!keyPath.isNullOrBlank()) {
                storeFile = file(keyPath)
                storePassword = System.getenv("PULSE_UPLOAD_PASSWORD")
                keyAlias = System.getenv("PULSE_UPLOAD_ALIAS") ?: "pulse-upload"
                keyPassword = System.getenv("PULSE_UPLOAD_PASSWORD")
            }
        }
    }
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("playUpload")
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}
