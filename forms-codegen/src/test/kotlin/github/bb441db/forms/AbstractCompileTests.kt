@file:OptIn(KotlinPoetMetadataPreview::class)

package github.bb441db.forms

import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import java.io.ByteArrayOutputStream

abstract class AbstractCompileTests {
    fun compile(vararg files: SourceFile, debug: Boolean = DEBUG): KotlinCompilation.Result {
        return KotlinCompilation()
            .apply {
                sources = listOf(*files)
                annotationProcessors = listOf(FormsProcessor())

                inheritClassPath = true

                messageOutputStream = if (debug) System.out else ByteArrayOutputStream()
            }
            .compile()
            .also {
                if (debug) {
                    for (file in it.sourcesGeneratedByAnnotationProcessor) {
                        if (file.absolutePath.endsWith(".kt")) {
                            file.inputStream().transferTo(System.out)
                        }
                    }
                }
            }
    }

    fun compile(fileName: String, @Language("kotlin") source: String, debug: Boolean = DEBUG): KotlinCompilation.Result {
        return compile(SourceFile.kotlin("$fileName.kt", source, trimIndent = true), debug = debug)
    }

    fun KotlinCompilation.Result.sourceWithName(name: String): String {
        return this.sourcesGeneratedByAnnotationProcessor
            .firstOrNull { it.name == "$name.kt" }
            ?.inputStream()
            ?.reader()
            ?.readText()
            .orEmpty()
    }

    private companion object {
        val DEBUG = System.getProperty("compileTests.debug") == "true"
    }
}