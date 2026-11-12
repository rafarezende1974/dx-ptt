plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    // Mesmo applicationId do app do celular: a Data Layer do Wear OS
    // so entrega mensagens entre apps de mesmo pacote.
    namespace = "br.com.dxlabs.dxptt"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.com.dxlabs.dxptt"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
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
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
}
