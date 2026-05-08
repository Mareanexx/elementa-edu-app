package config

import org.gradle.api.JavaVersion

object BuildConfigConsts {
    const val APPLICATION_ID = "ru.mareanexx.elementa"
    const val COMPILE_SDK = 36
    const val MIN_SDK = 26
    const val TARGET_SDK = 34
    val COMPILE_JDK_VERSION = JavaVersion.VERSION_17
    const val KOTLIN_JVM_TARGET = "17"
    const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
}