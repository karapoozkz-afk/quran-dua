plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    jvmToolchain(17)

    androidTarget()
    // The iOS targets compile the same commonMain code; the SwiftUI app comes later.
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            // Imported in Swift as `import Shared`; entry point is QuranDuaSdk.
            baseName = "Shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            api(libs.kotlinx.datetime)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("androidUnitTest").dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "app.qurandua.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    // The content tests read the JSON assets the app ships.
    testOptions.unitTests.all { it.workingDir = projectDir }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Room's compiler runs for every target that has a Room database.
    listOf(
        "kspAndroid",
        "kspIosX64",
        "kspIosArm64",
        "kspIosSimulatorArm64",
    ).forEach { add(it, libs.room.compiler) }
}
