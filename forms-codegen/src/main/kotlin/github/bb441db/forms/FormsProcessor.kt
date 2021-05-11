package github.bb441db.forms

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import github.bb441db.forms.annotations.Commitable
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@KotlinPoetMetadataPreview
class FormsProcessor : AbstractProcessor() {
    override fun process(elements: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {
        env
            .getElementsAnnotatedWith(Commitable::class.java)
            .filterIsInstance<TypeElement>()
            .map(::generate)
            .forEach { file ->
                file.writeTo(processingEnv.filer)
            }


        return true
    }

    private fun generate(element: TypeElement): FileSpec {
        return generate(element, processingEnv)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Commitable::class.java.name)
    }
}