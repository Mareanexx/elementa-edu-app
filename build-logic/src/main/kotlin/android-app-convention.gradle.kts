import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import config.appDefaultConfig
import config.baseAndroidConfig
import config.libs

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

configure<BaseAppModuleExtension> {
    baseAndroidConfig()
    appDefaultConfig()

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable += "NullSafeMutableLiveData"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    // Navigation
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}