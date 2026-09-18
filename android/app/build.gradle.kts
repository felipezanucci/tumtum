plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Número de build vindo do CI (GitHub Actions) — cada APK novo instala por cima do anterior.
val buildNumber = System.getenv("TUMTUM_VERSION_CODE")?.toIntOrNull() ?: 1

android {
    namespace = "cc.tumtum.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "cc.tumtum.app"
        minSdk = 28
        // API 36 (Android 16). The Play Console refuses a bundle below it since
        // 18/09 — "esse nível precisa ser de pelo menos 36". Nothing in the app
        // depends on the behaviour changes it brings: no orientation lock, so
        // the large-screen resizability rule is a no-op; edge-to-edge is already
        // enabled and every screen pads for the system bars; the capture service
        // already declares foregroundServiceType="connectedDevice" with the
        // permission to match.
        targetSdk = 36
        versionCode = buildNumber
        versionName = "1.0-b$buildNumber"
    }

    signingConfigs {
        // Chave de TESTE, commitada de propósito para builds de sideload no CI.
        // Nunca usar para publicar na Play Store.
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        // The upload key, kept out of the repository (decision log,
        // 2026-09-17). CI decodes it from a secret into a temp file and
        // points these four variables at it; Play App Signing holds the
        // definitive key, so this one is recoverable if lost.
        create("release") {
            val keystorePath = System.getenv("TUMTUM_UPLOAD_KEYSTORE_PATH")
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("TUMTUM_UPLOAD_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("TUMTUM_UPLOAD_KEY_ALIAS")
                keyPassword = System.getenv("TUMTUM_UPLOAD_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // No shrinking yet: Room, Compose and a foreground service are
            // exactly what an untested R8 pass breaks silently, and this
            // block has never been built. Turn it on only after a minified
            // build has captured a night on a phone (one-app-plan, Etapa 4).
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.health.connect)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    // Android ships org.json, but the stub on the unit-test classpath throws
    // on every call. AccessToken reads a JWT payload with it. Test-only.
    testImplementation(libs.json)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// A release build without the key would be an unsigned bundle nobody can
// use, so it fails here naming what is missing, not in the Play Console.
tasks.matching { it.name in listOf("bundleRelease", "assembleRelease", "packageRelease") }.configureEach {
    doFirst {
        if (System.getenv("TUMTUM_UPLOAD_KEYSTORE_PATH") == null) {
            throw GradleException(
                "Release build needs the upload key: set TUMTUM_UPLOAD_KEYSTORE_PATH, " +
                    "_KEYSTORE_PASSWORD, _KEY_ALIAS and _KEY_PASSWORD (see .github/workflows/build-app.yml)."
            )
        }
    }
}
