plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

/** Shared repentance wall (Firebase): -PwallProjectId=… or the WALL_PROJECT_ID env (a CI secret). Empty = wall off. */
fun wallSetting(property: String, env: String): String =
    (providers.gradleProperty(property).orNull ?: System.getenv(env) ?: "").trim()

// Release signing comes from the environment (GitHub secrets in CI, see docs/google-play.md).
// Without it the release build falls back to the debug key: fine for testing, rejected by Play.
val releaseKeystore = System.getenv("RELEASE_KEYSTORE_FILE")?.let { file(it) }?.takeIf { it.exists() }

android {
    namespace = "app.qurandua.android"
    // Google Play takes new apps and updates only with targetSdk 36 (Android 16) since 31 Aug 2026.
    compileSdk = 36

    defaultConfig {
        applicationId = "app.qurandua"
        minSdk = 24
        targetSdk = 36
        // Every upload to Play needs a bigger versionCode: CI passes its run number.
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = "1.0.0"
        // Every interface language the app has; anything else (library translations) is stripped.
        resourceConfigurations += listOf(
            "ru", "kk", "ky", "uz", "en", "ar", "es", "tr", "in", "id", "ur", "az", "tg", "tt", "fr", "de",
        )
        buildConfigField("String", "WALL_PROJECT_ID", "\"${wallSetting("wallProjectId", "WALL_PROJECT_ID")}\"")
        buildConfigField("String", "WALL_API_KEY", "\"${wallSetting("wallApiKey", "WALL_API_KEY")}\"")
    }

    signingConfigs {
        if (releaseKeystore != null) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName(if (releaseKeystore != null) "release" else "debug")
        }
    }

    androidResources {
        // quran.json is read as a stream at first launch; compressing it would slow that down.
        noCompress += "json"
    }

    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(libs.play.billing)
    implementation(libs.androidx.fragment)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
