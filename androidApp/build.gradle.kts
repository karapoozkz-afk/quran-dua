plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "app.qurandua.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "app.qurandua"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        // The app ships every language; no resource stripping.
        resourceConfigurations += listOf("ru", "kk", "en", "ar", "es", "tr", "in", "id", "ur")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Replace with a real signing config before publishing.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    androidResources {
        // quran.json is read as a stream at first launch; compressing it would slow that down.
        noCompress += "json"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // kotlinx-datetime uses java.time, which minSdk 24 only has through desugaring.
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(17)
    }

    sourceSets.getByName("main") {
        kotlin.srcDir("src/main/kotlin")
    }
}

dependencies {
    implementation(project(":shared"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.datasource)
    implementation(libs.media3.database)
    implementation(libs.androidx.core)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
