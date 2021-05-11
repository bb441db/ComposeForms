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

private fun commitableTypeName(bound: TypeName): TypeName {
    return ClassName(Commitable::class.java.packageName, Commitable::class.java.simpleName)
        .parameterizedBy(bound)
}

private fun kProperty1TypeName(bound: TypeName): TypeName {
    return ClassName(KProperty1::class.java.packageName, KProperty1::class.java.simpleName)
        .parameterizedBy(bound, TypeVariableName("V"))
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
        implementation(element, immutableKmClass, processingEnvironment, inspector)
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

    val commitFunSpec = FunSpec.builder("commit")
        .addModifiers(KModifier.OVERRIDE)
        .addTypeVariable(TypeVariableName("V"))
        .addParameter("prop", kProperty1TypeName(immutableKmClass.typeName))
        .addParameter("value", TypeVariableName("V"))
        .returns(commitableTypeName(immutableKmClass.typeName))
        .addCode(block())
        .build()

    val typeSpec = TypeSpec.classBuilder(implementationName)
        .addModifiers(KModifier.DATA)
        .apply {
            if (options.generateExtension) {
                addModifiers(KModifier.PRIVATE)
            }
        }
        .addSuperinterface(commitableTypeName(immutableKmClass.typeName))
        .primaryConstructor(FunSpec.constructorBuilder().addParameter("data", immutableKmClass.typeName, KModifier.OVERRIDE).build())
        .addProperty(PropertySpec.builder("data", immutableKmClass.typeName).initializer("data").build())
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
private val ImmutableKmType.typeName: TypeName get() {
    val className = when (val classifier = this.classifier) {
        is KmClassifier.Class -> ClassInspectorUtil.createClassName(classifier.name)
        is KmClassifier.TypeAlias -> ClassInspectorUtil.createClassName(classifier.name)
        is KmClassifier.TypeParameter -> throw Exception("TypeParameter not supported here.")
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
private fun implementation(
    element: TypeElement,
    immutableKmClass: ImmutableKmClass,
    processingEnvironment: ProcessingEnvironment,
    inspector: ElementsClassInspector,
): CodeBlock {
    val copyFn = element.enclosedElements
        .filterIsInstance<ExecutableElement>()
        .first {
            it.internalName == "copy" && it.returnType == element.asType()
        }
    val kmFun = immutableKmClass.functions.first { it.signature == copyFn.jvmMethodSignature(processingEnvironment.typeUtils) }
    return CodeBlock.builder()
        .beginControlFlow("return when (%N)", "prop")
        .apply {
            for (value in kmFun.valueParameters) {
                val name = value.name
                val type = value.type ?: throw Exception("vararg not supported")
                // CodeGenExample::foo -> copy(data = data.copy(foo = value as Boolean))
                //    CodeGenExample::bar -> copy(data = data.copy(bar = value as String))
                //    CodeGenExample::fooBar -> copy(data = data.copy(fooBar = value as String?))
                addStatement("%T::%N -> %N(%N = %N.%N(%N = %N as %T))",
                    /* Type name */ element.asClassName(),
                    /* Prop name */ name,
                    /* Copy fn name */ "copy",
                    /* Copy data named param */ "data",
                    /* data member name */ "data",
                    /* Copy fn name */ "copy",
                    /* prop member name */ name,
                    /* prop member name */ "value",
                    /* prop type name */ type.typeName
                )
            }
        }
        .addStatement("else -> %L", "this")
        .endControlFlow()
        .build()
}

@KotlinPoetMetadataPreview
fun ImmutableKmFunction.paramsWithTypes(): List<Pair<ImmutableKmValueParameter, ImmutableKmTypeParameter>> {
    return this.valueParameters.mapIndexed { index, valueParameter ->
        valueParameter to typeParameters[index]
    }
}

@KotlinPoetMetadataPreview
fun ExecutableElement.paramsWithTypes(): List<Pair<VariableElement, TypeParameterElement>> {
    return this.parameters.mapIndexed { index, valueParameter ->
        valueParameter to typeParameters[index]
    }
}