import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":forms-core"))

    implementation(kotlin("stdlib-jdk8"))

    implementation("com.squareup:kotlinpoet:1.8.0")
    implementation("com.squareup:kotlinpoet-metadata:1.8.0")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.8.0")
    implementation("com.squareup:kotlinpoet-classinspector-elements:1.8.0")
}


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}