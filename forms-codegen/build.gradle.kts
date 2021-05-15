import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

dependencies {
    implementation(project(":forms-core"))

    implementation(kotlin("stdlib-jdk8"))

    implementation("com.squareup:kotlinpoet:1.8.0")
    implementation("com.squareup:kotlinpoet-metadata:1.8.0")
    implementation("com.squareup:kotlinpoet-metadata-specs:1.8.0")
    implementation("com.squareup:kotlinpoet-classinspector-elements:1.8.0")

    testImplementation("org.jetbrains:annotations:19.0.0")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.3.5")
}

fun properties(name: String): Properties {
    val properties = Properties()
    properties.load(project.file(name).inputStream())
    return properties
}

tasks.test {
    useJUnitPlatform()
    options {
        properties("local.properties").forEach { (key, value) ->
            if (value != null) {
                systemProperty(key.toString(), value.toString())
            }
        }
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
    freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}