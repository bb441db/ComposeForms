package github.bb441db.forms

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import kotlinx.metadata.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.tools.Diagnostic
import kotlin.reflect.KProperty1

data class Options(
    val generateExtension: Boolean,
)

private val VALUE_TYPE_VARIABLE_NAME = TypeVariableName("Value")
private const val COMMITABLE_MEMBER_STATE = "state"
private const val COMMITABLE_PARAMETER_PROP = "prop"
private const val COMMITABLE_PARAMETER_VALUE = "newValue"
private const val COMMITABLE_COMMIT_FN = "commit"

private fun commitableTypeName(bound: TypeName): TypeName {
    return ClassName(Commitable::class.java.packageName, Commitable::class.java.simpleName)
        .parameterizedBy(bound)
}

private fun kProperty1TypeName(bound: TypeName): TypeName {
    return ClassName(KProperty1::class.java.packageName, KProperty1::class.java.simpleName)
        .parameterizedBy(bound, VALUE_TYPE_VARIABLE_NAME)
}

@KotlinPoetMetadataPreview
fun generate(element: TypeElement, processingEnvironment: ProcessingEnvironment): FileSpec {
    val immutableKmClass = element.toImmutableKmClass()
    if (!immutableKmClass.isData) {
        processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, "Only data classes are supported", element)
    }

    val inspector = ElementsClassInspector.create(
        processingEnvironment.elementUtils,
        processingEnvironment.typeUtils
    ) as ElementsClassInspector

    val annotation = element.getAnnotation(github.bb441db.forms.annotations.Commitable::class.java)
    val options = Options(
        generateExtension = annotation.generateExtension
    )

    return generate(
        element = element,
        immutableKmClass = immutableKmClass,
        processingEnvironment = processingEnvironment,
        inspector = inspector,
        options = options,
    ) {
        implementation(immutableKmClass)
    }
}

@KotlinPoetMetadataPreview
private fun generate(element: TypeElement,
                     immutableKmClass: ImmutableKmClass,
                     processingEnvironment: ProcessingEnvironment,
                     inspector: ElementsClassInspector,
                     options: Options,
                     block: () -> CodeBlock = ::stub,
): FileSpec {
    val packageName = processingEnvironment.elementUtils.getPackageOf(element).qualifiedName.toString()
    val name = element.simpleName.toString()
    val implementationName = "Commitable${name}Impl"

    val commitFunSpec = FunSpec.builder(COMMITABLE_COMMIT_FN)
        .addModifiers(KModifier.OVERRIDE)
        .addTypeVariable(VALUE_TYPE_VARIABLE_NAME)
        .addParameter(COMMITABLE_PARAMETER_PROP, kProperty1TypeName(immutableKmClass.typeName))
        .addParameter(COMMITABLE_PARAMETER_VALUE, VALUE_TYPE_VARIABLE_NAME)
        .returns(commitableTypeName(immutableKmClass.typeName))
        .apply {
            if (immutableKmClass.typeParameters.isNotEmpty()) {
                addAnnotation(
                    AnnotationSpec
                        .builder(Suppress::class)
                        .addMember("%S", "UNCHECKED_CAST")
                        .build()
                )
            }
        }
        .addCode(block())
        .build()

    val typeSpec = TypeSpec.classBuilder(implementationName)
        .addModifiers(KModifier.DATA)
        .apply {
            if (options.generateExtension) {
                addModifiers(KModifier.PRIVATE)
            }
        }
        .addTypeVariables(immutableKmClass.typeVariables)
        .addSuperinterface(commitableTypeName(immutableKmClass.typeName))
        .primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addParameter(COMMITABLE_MEMBER_STATE, immutableKmClass.typeName, KModifier.OVERRIDE)
                .build()
        )
        .addProperty(
            PropertySpec
                .builder(COMMITABLE_MEMBER_STATE, immutableKmClass.typeName)
                .initializer(COMMITABLE_MEMBER_STATE)
                .build()
        )
        .addFunction(commitFunSpec)
        .build()



    return FileSpec
        .builder(packageName, implementationName)
        .addType(typeSpec)
        .apply {
            if (options.generateExtension) {
                addFunction(
                    FunSpec.builder("commitable")
                        .receiver(immutableKmClass.typeName)
                        .addTypeVariables(immutableKmClass.typeVariables)
                        .returns(commitableTypeName(immutableKmClass.typeName))
                        .addCode("return %N(%L)", implementationName, "this")
                        .build()
                )
            }
        }
        .build()
}

@KotlinPoetMetadataPreview
private val ImmutableKmClass.typeName: TypeName get() {
    val typeVariables = this.typeVariables
    return if (typeVariables.isNotEmpty()) {
        className.parameterizedBy(typeVariables)
    } else {
        className
    }
}

@KotlinPoetMetadataPreview
private fun ImmutableKmType.typeName(typeParameters: List<ImmutableKmTypeParameter> = listOf()): TypeName {
    val className = when (val classifier = this.classifier) {
        is KmClassifier.Class -> ClassInspectorUtil.createClassName(classifier.name)
        is KmClassifier.TypeAlias -> ClassInspectorUtil.createClassName(classifier.name)
        is KmClassifier.TypeParameter -> TypeVariableName(typeParameters.first { it.id == classifier.id }.name)
    }

    return className.copy(nullable = this.isNullable)
}

@KotlinPoetMetadataPreview
private val ImmutableKmClass.className: ClassName get() = ClassInspectorUtil.createClassName(this.name)
@KotlinPoetMetadataPreview
private val ImmutableKmClass.typeVariables: List<TypeVariableName> get() = this.typeParameters.map {
    val bounds = it.bounds
    if (bounds == null) {
        TypeVariableName(it.name, variance = it.variance.asKModifier())
    } else {
        TypeVariableName(it.name, bounds, variance = it.variance.asKModifier())
    }
}

@KotlinPoetMetadataPreview
private val ImmutableKmTypeParameter.bounds: TypeName?
    get() {
        val first = this.upperBounds.firstOrNull() ?: return null
        return when (val classifier = first.classifier) {
            is KmClassifier.Class -> ClassInspectorUtil.createClassName(classifier.name)
            is KmClassifier.TypeParameter -> return null
            is KmClassifier.TypeAlias -> return null
        }
    }

private fun KmVariance.asKModifier(): KModifier? {
    return when (this) {
        KmVariance.INVARIANT -> null
        KmVariance.IN -> KModifier.IN
        KmVariance.OUT -> KModifier.OUT
    }
}

@KotlinPoetMetadataPreview
private fun stub(): CodeBlock {
    return CodeBlock.of("TODO(\"Stub\"")
}

@KotlinPoetMetadataPreview
private fun implementation(immutableKmClass: ImmutableKmClass, ): CodeBlock {
    val kmFun = immutableKmClass.functions.first { it.name == "copy" }
    return CodeBlock.builder()
        .beginControlFlow("return when (%N)", "prop")
        .apply {
            for (value in kmFun.valueParameters) {
                val name = value.name
                val type = value.type ?: throw Exception("vararg not supported")
                addStatement("%T::%N -> %N(%N = %N.%N(%N = %N as %T))",
                    /* Type name */ immutableKmClass.typeName,
                    /* Prop name */ name,
                    /* Copy fn name */ "copy",
                    /* Copy data named param */ COMMITABLE_MEMBER_STATE,
                    /* data member name */ COMMITABLE_MEMBER_STATE,
                    /* Copy fn name */ "copy",
                    /* prop member name */ name,
                    /* prop member name */ COMMITABLE_PARAMETER_VALUE,
                    /* prop type name */ type.typeName(immutableKmClass.typeParameters)
                )
            }
        }
        .addStatement("else -> %L", "this")
        .endControlFlow()
        .build()
}