package data

/**
 * Attribute information (JVM Spec 4.7)
 * Sealed class for 30+ predefined attribute types
 */
sealed class AttributeInfo {
    abstract val attributeNameIndex: UShort
}

/**
 * Unknown/generic attribute
 * Used for attributes not recognized by this parser
 */
data class UnknownAttribute(
    override val attributeNameIndex: UShort,
    val info: ByteArray
) : AttributeInfo() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UnknownAttribute
        if (attributeNameIndex != other.attributeNameIndex) return false
        if (!info.contentEquals(other.info)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = attributeNameIndex.hashCode()
        result = 31 * result + info.contentHashCode()
        return result
    }
}

/**
 * ConstantValue attribute (JVM Spec 4.7.2)
 * Represents the value of a constant field
 */
data class ConstantValueAttribute(
    override val attributeNameIndex: UShort,
    val constantValueIndex: UShort
) : AttributeInfo()

/**
 * Code attribute (JVM Spec 4.7.3)
 * Contains the bytecode for a method
 */
data class CodeAttribute(
    override val attributeNameIndex: UShort,
    val maxStack: UShort,
    val maxLocals: UShort,
    val code: ByteArray,
    val exceptionTable: List<ExceptionHandler>,
    val attributes: List<AttributeInfo>
) : AttributeInfo() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CodeAttribute
        if (attributeNameIndex != other.attributeNameIndex) return false
        if (maxStack != other.maxStack) return false
        if (maxLocals != other.maxLocals) return false
        if (!code.contentEquals(other.code)) return false
        if (exceptionTable != other.exceptionTable) return false
        if (attributes != other.attributes) return false
        return true
    }

    override fun hashCode(): Int {
        var result = attributeNameIndex.hashCode()
        result = 31 * result + maxStack.hashCode()
        result = 31 * result + maxLocals.hashCode()
        result = 31 * result + code.contentHashCode()
        result = 31 * result + exceptionTable.hashCode()
        result = 31 * result + attributes.hashCode()
        return result
    }
}

/**
 * Exception handler entry in Code attribute
 */
data class ExceptionHandler(
    val startPc: UShort,
    val endPc: UShort,
    val handlerPc: UShort,
    val catchType: UShort  // 0 = finally, else index to CONSTANT_Class
)

/**
 * StackMapTable attribute (JVM Spec 4.7.4)
 * Used for type checking during verification (Java 6+)
 * Uses compressed frame encoding
 */
data class StackMapTableAttribute(
    override val attributeNameIndex: UShort,
    val entries: List<StackMapFrame>
) : AttributeInfo()

/**
 * StackMap attribute (Pre-Java 6, class version < 50)
 * Old uncompressed format of stack map frames
 * Superseded by StackMapTable in Java 6
 */
data class StackMapAttribute(
    override val attributeNameIndex: UShort,
    val entries: List<StackMapFrame>
) : AttributeInfo()

/**
 * Exceptions attribute (JVM Spec 4.7.5)
 * Lists checked exceptions a method may throw
 */
data class ExceptionsAttribute(
    override val attributeNameIndex: UShort,
    val exceptionIndexTable: List<UShort>  // Indices to CONSTANT_Class
) : AttributeInfo()

/**
 * InnerClasses attribute (JVM Spec 4.7.6)
 * Records inner class relationships
 */
data class InnerClassesAttribute(
    override val attributeNameIndex: UShort,
    val classes: List<InnerClassInfo>
) : AttributeInfo()

data class InnerClassInfo(
    val innerClassInfoIndex: UShort,
    val outerClassInfoIndex: UShort,
    val innerNameIndex: UShort,
    val innerClassAccessFlags: UShort
)

/**
 * EnclosingMethod attribute (JVM Spec 4.7.7)
 * Identifies enclosing method for local/anonymous classes
 */
data class EnclosingMethodAttribute(
    override val attributeNameIndex: UShort,
    val classIndex: UShort,
    val methodIndex: UShort
) : AttributeInfo()

/**
 * Synthetic attribute (JVM Spec 4.7.8)
 * Marks compiler-generated elements
 */
data class SyntheticAttribute(
    override val attributeNameIndex: UShort
) : AttributeInfo()

/**
 * Signature attribute (JVM Spec 4.7.9)
 * Stores generic type signatures
 */
data class SignatureAttribute(
    override val attributeNameIndex: UShort,
    val signatureIndex: UShort
) : AttributeInfo()

/**
 * SourceFile attribute (JVM Spec 4.7.10)
 * Records source file name
 */
data class SourceFileAttribute(
    override val attributeNameIndex: UShort,
    val sourceFileIndex: UShort
) : AttributeInfo()

/**
 * SourceDebugExtension attribute (JVM Spec 4.7.11)
 * Contains debugging information for non-Java languages
 */
data class SourceDebugExtensionAttribute(
    override val attributeNameIndex: UShort,
    val debugExtension: ByteArray
) : AttributeInfo() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SourceDebugExtensionAttribute
        if (attributeNameIndex != other.attributeNameIndex) return false
        if (!debugExtension.contentEquals(other.debugExtension)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = attributeNameIndex.hashCode()
        result = 31 * result + debugExtension.contentHashCode()
        return result
    }
}

/**
 * LineNumberTable attribute (JVM Spec 4.7.12)
 * Maps bytecode offsets to source line numbers
 */
data class LineNumberTableAttribute(
    override val attributeNameIndex: UShort,
    val lineNumberTable: List<LineNumberEntry>
) : AttributeInfo()

data class LineNumberEntry(
    val startPc: UShort,
    val lineNumber: UShort
)

/**
 * LocalVariableTable attribute (JVM Spec 4.7.13)
 * Describes local variable names and types
 */
data class LocalVariableTableAttribute(
    override val attributeNameIndex: UShort,
    val localVariableTable: List<LocalVariableEntry>
) : AttributeInfo()

data class LocalVariableEntry(
    val startPc: UShort,
    val length: UShort,
    val nameIndex: UShort,
    val descriptorIndex: UShort,
    val index: UShort
)

/**
 * LocalVariableTypeTable attribute (JVM Spec 4.7.14)
 * Provides generic type information for local variables
 */
data class LocalVariableTypeTableAttribute(
    override val attributeNameIndex: UShort,
    val localVariableTypeTable: List<LocalVariableTypeEntry>
) : AttributeInfo()

data class LocalVariableTypeEntry(
    val startPc: UShort,
    val length: UShort,
    val nameIndex: UShort,
    val signatureIndex: UShort,
    val index: UShort
)

/**
 * Deprecated attribute (JVM Spec 4.7.15)
 * Marks deprecated elements
 */
data class DeprecatedAttribute(
    override val attributeNameIndex: UShort
) : AttributeInfo()

/**
 * RuntimeVisibleAnnotations attribute (JVM Spec 4.7.16)
 * Stores runtime-visible annotations
 */
data class RuntimeVisibleAnnotationsAttribute(
    override val attributeNameIndex: UShort,
    val annotations: List<Annotation>
) : AttributeInfo()

/**
 * RuntimeInvisibleAnnotations attribute (JVM Spec 4.7.17)
 * Stores runtime-invisible annotations
 */
data class RuntimeInvisibleAnnotationsAttribute(
    override val attributeNameIndex: UShort,
    val annotations: List<Annotation>
) : AttributeInfo()

/**
 * RuntimeVisibleParameterAnnotations attribute (JVM Spec 4.7.18)
 * Stores runtime-visible parameter annotations
 */
data class RuntimeVisibleParameterAnnotationsAttribute(
    override val attributeNameIndex: UShort,
    val parameterAnnotations: List<List<Annotation>>
) : AttributeInfo()

/**
 * RuntimeInvisibleParameterAnnotations attribute (JVM Spec 4.7.19)
 * Stores runtime-invisible parameter annotations
 */
data class RuntimeInvisibleParameterAnnotationsAttribute(
    override val attributeNameIndex: UShort,
    val parameterAnnotations: List<List<Annotation>>
) : AttributeInfo()

/**
 * RuntimeVisibleTypeAnnotations attribute (JVM Spec 4.7.20)
 * Stores runtime-visible type annotations (JSR 308)
 */
data class RuntimeVisibleTypeAnnotationsAttribute(
    override val attributeNameIndex: UShort,
    val annotations: List<TypeAnnotation>
) : AttributeInfo()

/**
 * RuntimeInvisibleTypeAnnotations attribute (JVM Spec 4.7.21)
 * Stores runtime-invisible type annotations
 */
data class RuntimeInvisibleTypeAnnotationsAttribute(
    override val attributeNameIndex: UShort,
    val annotations: List<TypeAnnotation>
) : AttributeInfo()

/**
 * AnnotationDefault attribute (JVM Spec 4.7.22)
 * Specifies default value for annotation element
 */
data class AnnotationDefaultAttribute(
    override val attributeNameIndex: UShort,
    val defaultValue: ElementValue
) : AttributeInfo()

/**
 * BootstrapMethods attribute (JVM Spec 4.7.23)
 * Bootstrap method table for invokedynamic and dynamic constants
 */
data class BootstrapMethodsAttribute(
    override val attributeNameIndex: UShort,
    val bootstrapMethods: List<BootstrapMethod>
) : AttributeInfo()

data class BootstrapMethod(
    val bootstrapMethodRef: UShort,
    val bootstrapArguments: List<UShort>
)

/**
 * MethodParameters attribute (JVM Spec 4.7.24)
 * Stores parameter names and access flags
 */
data class MethodParametersAttribute(
    override val attributeNameIndex: UShort,
    val parameters: List<MethodParameter>
) : AttributeInfo()

data class MethodParameter(
    val nameIndex: UShort,
    val accessFlags: UShort
)

/**
 * Module attribute (JVM Spec 4.7.25)
 * Describes module declarations
 */
data class ModuleAttribute(
    override val attributeNameIndex: UShort,
    val moduleNameIndex: UShort,
    val moduleFlags: UShort,
    val moduleVersionIndex: UShort,
    val requires: List<ModuleRequires>,
    val exports: List<ModuleExports>,
    val opens: List<ModuleOpens>,
    val uses: List<UShort>,
    val provides: List<ModuleProvides>
) : AttributeInfo()

data class ModuleRequires(
    val requiresIndex: UShort,
    val requiresFlags: UShort,
    val requiresVersionIndex: UShort
)

data class ModuleExports(
    val exportsIndex: UShort,
    val exportsFlags: UShort,
    val exportsToIndex: List<UShort>
)

data class ModuleOpens(
    val opensIndex: UShort,
    val opensFlags: UShort,
    val opensToIndex: List<UShort>
)

data class ModuleProvides(
    val providesIndex: UShort,
    val providesWithIndex: List<UShort>
)

/**
 * ModulePackages attribute (JVM Spec 4.7.26)
 * Lists all packages in a module
 */
data class ModulePackagesAttribute(
    override val attributeNameIndex: UShort,
    val packageIndex: List<UShort>
) : AttributeInfo()

/**
 * ModuleMainClass attribute (JVM Spec 4.7.27)
 * Specifies main class for a module
 */
data class ModuleMainClassAttribute(
    override val attributeNameIndex: UShort,
    val mainClassIndex: UShort
) : AttributeInfo()

/**
 * NestHost attribute (JVM Spec 4.7.28)
 * Points to nest host class for nest-based access control
 */
data class NestHostAttribute(
    override val attributeNameIndex: UShort,
    val hostClassIndex: UShort
) : AttributeInfo()

/**
 * NestMembers attribute (JVM Spec 4.7.29)
 * Lists nest member classes
 */
data class NestMembersAttribute(
    override val attributeNameIndex: UShort,
    val classes: List<UShort>
) : AttributeInfo()

/**
 * Record attribute (JVM Spec 4.7.30)
 * Contains record component information
 */
data class RecordAttribute(
    override val attributeNameIndex: UShort,
    val components: List<RecordComponentInfo>
) : AttributeInfo()

data class RecordComponentInfo(
    val nameIndex: UShort,
    val descriptorIndex: UShort,
    val attributes: List<AttributeInfo>
)

/**
 * PermittedSubclasses attribute (JVM Spec 4.7.31)
 * Lists permitted subclasses for sealed classes
 */
data class PermittedSubclassesAttribute(
    override val attributeNameIndex: UShort,
    val classes: List<UShort>
) : AttributeInfo()
