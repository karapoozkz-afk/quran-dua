import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

// Upload key for Google Play. Never committed: it comes from keystore.properties next to this
// file (local builds) or from environment variables (GitHub Actions secrets). See RELEASE.md.
val keystoreProps = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
fun signingValue(prop: String, env: String): String? =
    keystoreProps.getProperty(prop) ?: System.getenv(env)?.takeIf { it.isNotBlank() }

val uploadStoreFile = signingValue("storeFile", "BIBLIYA_KEYSTORE_FILE")
val hasUploadKey = uploadStoreFile != null && file(uploadStoreFile).exists()

android {
    namespace = "app.bibliya.android"
    compileSdk = 36

    defaultConfig {
        // The package name is permanent once the app is on Google Play. Change it before the first upload, never after.
        applicationId = "app.bibliya"
        minSdk = 26
        // Google Play requires API 36 for new apps and updates since 31 August 2026.
        targetSdk = 36
        // Every upload to Google Play needs a higher versionCode; CI passes the run number.
        versionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
        versionName = "1.0.0"
    }

    androidResources {
        // Only the two shipped languages; library translations for others would be dead weight.
        localeFilters += listOf("ru", "en")
        // The Bible assets are read as a stream at first launch; compressing them would slow that down.
        noCompress += "json"
    }

    signingConfigs {
        if (hasUploadKey) {
            create("upload") {
                storeFile = file(uploadStoreFile!!)
                storePassword = signingValue("storePassword", "BIBLIYA_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "BIBLIYA_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "BIBLIYA_KEY_PASSWORD")
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
            // Without the upload key the release bundle stays unsigned, which Google Play rejects.
            // Never fall back to the debug key: Play refuses debug-signed uploads.
            if (hasUploadKey) signingConfig = signingConfigs.getByName("upload")
        }
        // The Play build exactly (R8 shrinking and obfuscation), signed with the debug key so an emulator can
        // install it. Only CI's screen walk uses it; it is never uploaded to Google Play.
        create("releaseTest") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".rtest"
            matchingFallbacks += listOf("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Room and kotlinx-datetime pull in newer java.* APIs; desugaring keeps them working on Android 8.
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(17)
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
    implementation(libs.androidx.core)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
