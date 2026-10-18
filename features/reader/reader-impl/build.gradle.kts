plugins {
    id("android-feature-convention")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}
android { namespace = "ru.mareanexx.reader_impl" }
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
dependencies {
    implementation(project(":common:ui"))
    implementation(project(":features:reader:reader-api"))
    implementation(project(":features:book:book-api"))
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.webkit)
    implementation(libs.jsoup)
    implementation(libs.okhttp)
    ksp(libs.androidx.room.compiler)
    debugImplementation(project(":features:collections:collections-api"))
    debugImplementation(project(":features:collections:collections-impl"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver)
}
