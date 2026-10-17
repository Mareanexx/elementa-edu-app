plugins {
    id("android-app-convention")
    // TODO: id("com.google.dagger.hilt.android")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(project(":common:network"))
    implementation(project(":common:ui"))
    implementation(project(":common:utils"))

    implementation(project(":core:database"))
    implementation(project(":core:datastore"))

    implementation(project(":features:auth:auth-api"))
    implementation(project(":features:auth:auth-impl"))

    implementation(project(":features:book:book-api"))
    implementation(project(":features:book:book-impl"))

    implementation(project(":features:collections:collections-api"))
    implementation(project(":features:collections:collections-impl"))

    implementation(project(":features:catalog:catalog-api"))
    implementation(project(":features:catalog:catalog-impl"))

    implementation(project(":features:reader:reader-api"))
    implementation(project(":features:reader:reader-impl"))

    implementation(project(":features:tests:tests-api"))
    implementation(project(":features:tests:tests-impl"))

    implementation(project(":features:user-library:user-library-api"))
    implementation(project(":features:user-library:user-library-impl"))

    implementation(project(":features:user-profile:user-profile-api"))
    implementation(project(":features:user-profile:user-profile-impl"))

    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.compiler)
}

android {
    defaultConfig {
        // Указать настоящий HTTPS-адрес после подключения сервера.
        buildConfigField("String", "AUTH_BASE_URL", "\"${providers.gradleProperty("elementa.authBaseUrl").getOrElse("https://api.elementa.example/")}\"")
    }
    buildTypes {
        debug { buildConfigField("boolean", "MOCK_AUTH", "true") }
        release { buildConfigField("boolean", "MOCK_AUTH", "false") }
    }
}
