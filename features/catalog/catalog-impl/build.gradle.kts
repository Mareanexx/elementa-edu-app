plugins {
    id("android-feature-convention")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}
android { namespace = "ru.mareanexx.catalog_impl" }
dependencies {
    implementation(project(":common:ui"))
    implementation(project(":features:catalog:catalog-api"))
    implementation(project(":features:book:book-api"))
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.okhttp)
    debugImplementation(project(":features:collections:collections-api"))
    debugImplementation(project(":features:collections:collections-impl"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver)
}
