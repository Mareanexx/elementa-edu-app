plugins {
    id("android-feature-convention")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ru.mareanexx.tests_impl"
}

dependencies {
}