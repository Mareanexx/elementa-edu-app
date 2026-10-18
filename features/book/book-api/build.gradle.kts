plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    `java-library`
}
kotlin { jvmToolchain(17) }
dependencies {
    api(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
