@file:OptIn(KotlinPoetMetadataPreview::class)

package github.bb441db.forms

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import kotlinx.metadata.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.tools.Diagnostic
import kotlin.reflect.KProperty1

data class Options(
    val generateExtension: Boolean,
    val fileName: String,
    val className: String,
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

fun generate(element: TypeElement, processingEnvironment: ProcessingEnvironment): FileSpec {
    val immutableKmClass = element.toImmutableKmClass()
    if (!immutableKmClass.isData) {
        processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR, "Only data classes are supported", element)
    }

    val annotation = element.getAnnotation(github.bb441db.forms.annotations.Commitable::class.java)
    val options = Options(
        generateExtension = annotation.generateExtension,
        fileName = if (annotation.fileName.isNotEmpty()) annotation.fileName else element.simpleName.toString(),
        className = if (annotation.className.isNotEmpty()) annotation.className else "Commitable${element.simpleName}Impl"
    )

    return generate(
        element = element,
        immutableKmClass = immutableKmClass,
        processingEnvironment = processingEnvironment,
        options = options,
    ) {
        implementation(immutableKmClass)
    }
}

private fun generate(element: TypeElement,
                     immutableKmClass: ImmutableKmClass,
                     processingEnvironment: ProcessingEnvironment,
                     options: Options,
                     block: () -> CodeBlock = ::stub,
): FileSpec {
    val packageName = processingEnvironment.elementUtils.getPackageOf(element).qualifiedName.toString()

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

    val typeSpec = TypeSpec.classBuilder(options.className)
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
        .addFunction(generateToString())
        .addFunction(generateEquals())
        .build()



    return FileSpec
        .builder(packageName, options.fileName)
        .addType(typeSpec)
        .apply {
            if (options.generateExtension) {
                addFunction(generateCommitableExtension(immutableKmClass, options))
            }
        }
        .build()
}

private val ImmutableKmClass.typeName: TypeName get() {
    val typeVariables = this.typeVariables
    return if (typeVariables.isNotEmpty()) {
        className.parameterizedBy(typeVariables)
    } else {
        className
    }
}

private fun ImmutableKmType.typeName(typeParameters: List<ImmutableKmTypeParameter> = listOf()): TypeName {
    val className = when (val classifier = this.classifier) {
        is KmClassifier.Class -> ClassInspectorUtil.createClassName(classifier.name)
        is KmClassifier.TypeAlias -> ClassInspectorUtil.createClassName(classifier.name)
        is KmClassifier.TypeParameter -> TypeVariableName(typeParameters.first { it.id == classifier.id }.name)
    }

    return className.copy(nullable = this.isNullable)
}

private val ImmutableKmClass.className: ClassName get() = ClassInspectorUtil.createClassName(this.name)
private val ImmutableKmClass.typeVariables: List<TypeVariableName> get() = this.typeParameters.map {
    val bounds = it.bounds
    if (bounds == null) {
        TypeVariableName(it.name, variance = it.variance.asKModifier())
    } else {
        TypeVariableName(it.name, bounds, variance = it.variance.asKModifier())
    }
}

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

private fun stub(): CodeBlock {
    return CodeBlock.of("TODO(\"Stub\"")
}

private fun generateEquals(): FunSpec {
    return FunSpec
        .builder("equals")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("other", ANY.copy(nullable = true))
        .returns(BOOLEAN)
        .addCode(
            CodeBlock
                .builder()
                .beginControlFlow("if (%N is %T)", "other", commitableTypeName(STAR))
                .addStatement("return %N == %N.%N", COMMITABLE_MEMBER_STATE, "other", COMMITABLE_MEMBER_STATE)
                .endControlFlow()
                .addStatement("return false")
                .build()
        )
        .build()
}

private fun generateToString(): FunSpec {
    return FunSpec
        .builder("toString")
        .addModifiers(KModifier.OVERRIDE)
        .addCode(CodeBlock.of("return %N.%M()", COMMITABLE_MEMBER_STATE, MemberName("", "toString")))
        .build()
}

private fun generateCommitableExtension(immutableKmClass: ImmutableKmClass, options: Options): FunSpec {
    return FunSpec.builder("commitable")
        .receiver(immutableKmClass.typeName)
        .addTypeVariables(immutableKmClass.typeVariables)
        .returns(commitableTypeName(immutableKmClass.typeName))
        .addCode("return %N(%L)", options.className, "this")
        .build()
}

private fun implementation(immutableKmClass: ImmutableKmClass): CodeBlock {
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