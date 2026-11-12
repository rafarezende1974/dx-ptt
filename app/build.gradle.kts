plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "br.com.dxlabs.dxptt"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.com.dxlabs.dxptt"
        minSdk = 29
        targetSdk = 36
        versionCode = 3
        versionName = "0.2.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-ktx:1.9.2")
    // Data Layer do Wear OS: o PttMessageService recebe apertou/soltou do relogio.
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
}
