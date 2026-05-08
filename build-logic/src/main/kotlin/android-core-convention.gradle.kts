import config.BuildConfigConsts
import config.android
import config.baseAndroidConfig
import config.libs

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ru.mareanexx.core"
    baseAndroidConfig()

    defaultConfig {
        minSdk = BuildConfigConsts.MIN_SDK
        testOptions.targetSdk = BuildConfigConsts.TARGET_SDK

        testInstrumentationRunner = BuildConfigConsts.TEST_INSTRUMENTATION_RUNNER
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.material3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Data Store
    implementation(libs.datastore.preferences)

    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}