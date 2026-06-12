plugins {
    id("android-core-data-convention")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ru.mareanexx.database"
}

dependencies {
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
}
