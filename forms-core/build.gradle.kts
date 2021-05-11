import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

val compileKotlin: KotlinCompile by tasks

compileKotlin.kotlinOptions.freeCompilerArgs += "-Xallow-kotlin-package"