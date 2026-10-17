plugins {
    id("android-feature-convention")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ru.mareanexx.collections_impl"
}

dependencies {
    implementation(project(":common:ui"))
    implementation(project(":features:collections:collections-api"))
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.okhttp)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver)
}