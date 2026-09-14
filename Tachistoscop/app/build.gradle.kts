plugins {
    id("com.android.application")
}

android {
    namespace = "ro.tachistoscop.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "ro.tachistoscop.app"
        minSdk = 23
        targetSdk = 36
        versionCode = 40
        versionName = "3.0.0-alpha1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
