package data

/**
 * Annotation structure (JVM Spec 4.7.16)
 */
data class Annotation(
    val typeIndex: UShort,                              // Index to CONSTANT_Utf8 (field descriptor)
    val elementValuePairs: List<ElementValuePair>
)

/**
 * Element-value pair in an annotation
 */
data class ElementValuePair(
    val elementNameIndex: UShort,                       // Index to CONSTANT_Utf8
    val value: ElementValue
)

/**
 * Element value in an annotation (JVM Spec 4.7.16.1)
 * Tag meanings:
 * - B, C, D, F, I, J, S, Z: primitive types
 * - s: String
 * - e: enum constant
 * - c: class
 * - @: annotation
 * - [: array
 */
sealed class ElementValue {
    abstract val tag: Char
}

/**
 * Constant value (primitive or String)
 * Tags: B, C, D, F, I, J, S, Z, s
 */
data class ConstValueIndex(
    override val tag: Char,
    val constValueIndex: UShort                         // Index to constant pool
) : ElementValue()

/**
 * Enum constant value
 * Tag: e
 */
data class EnumConstValue(
    override val tag: Char = 'e',
    val typeNameIndex: UShort,                          // Index to CONSTANT_Utf8 (field descriptor)
    val constNameIndex: UShort                          // Index to CONSTANT_Utf8 (simple name)
) : ElementValue()

/**
 * Class literal value
 * Tag: c
 */
data class ClassInfoIndex(
    override val tag: Char = 'c',
    val classInfoIndex: UShort                          // Index to CONSTANT_Utf8 (return descriptor)
) : ElementValue()

/**
 * Nested annotation value
 * Tag: @
 */
data class AnnotationValue(
    override val tag: Char = '@',
    val annotation: Annotation
) : ElementValue()

/**
 * Array of element values
 * Tag: [
 */
data class ArrayValue(
    override val tag: Char = '[',
    val values: List<ElementValue>
) : ElementValue()

/**
 * Type annotation structure (JVM Spec 4.7.20)
 * Used for JSR 308 type annotations
 */
data class TypeAnnotation(
    val targetType: UByte,
    val targetInfo: TargetInfo,
    val targetPath: TypePath,
    val typeIndex: UShort,                              // Index to CONSTANT_Utf8
    val elementValuePairs: List<ElementValuePair>
)

/**
 * Target info for type annotations (JVM Spec 4.7.20)
 * This is a union type with different structures based on targetType
 */
sealed class TargetInfo

/**
 * type_parameter_target
 * Used by: 0x00 (class), 0x01 (method)
 */
data class TypeParameterTarget(
    val typeParameterIndex: UByte
) : TargetInfo()

/**
 * supertype_target
 * Used by: 0x10 (class extends/implements)
 */
data class SupertypeTarget(
    val supertypeIndex: UShort
) : TargetInfo()

/**
 * type_parameter_bound_target
 * Used by: 0x11 (class type parameter bound), 0x12 (method type parameter bound)
 */
data class TypeParameterBoundTarget(
    val typeParameterIndex: UByte,
    val boundIndex: UByte
) : TargetInfo()

/**
 * empty_target
 * Used by: 0x13 (field), 0x14 (method return), 0x15 (method receiver)
 */
data object EmptyTarget : TargetInfo()

/**
 * formal_parameter_target
 * Used by: 0x16 (method formal parameter)
 */
data class FormalParameterTarget(
    val formalParameterIndex: UByte
) : TargetInfo()

/**
 * throws_target
 * Used by: 0x17 (throws clause)
 */
data class ThrowsTarget(
    val throwsTypeIndex: UShort
) : TargetInfo()

/**
 * localvar_target
 * Used by: 0x40 (local variable), 0x41 (resource variable)
 */
data class LocalvarTarget(
    val table: List<LocalvarTargetEntry>
) : TargetInfo()

data class LocalvarTargetEntry(
    val startPc: UShort,
    val length: UShort,
    val index: UShort
)

/**
 * catch_target
 * Used by: 0x42 (exception parameter)
 */
data class CatchTarget(
    val exceptionTableIndex: UShort
) : TargetInfo()

/**
 * offset_target
 * Used by: 0x43 (instanceof), 0x44 (new), 0x45 (method reference new), 0x46 (method reference identifier)
 */
data class OffsetTarget(
    val offset: UShort
) : TargetInfo()

/**
 * type_argument_target
 * Used by: 0x47-0x4B (cast, constructor argument, method argument, constructor type argument, method type argument)
 */
data class TypeArgumentTarget(
    val offset: UShort,
    val typeArgumentIndex: UByte
) : TargetInfo()

/**
 * Type path for type annotations (JVM Spec 4.7.20.2)
 */
data class TypePath(
    val path: List<TypePathEntry>
)

/**
 * Single step in a type path
 */
data class TypePathEntry(
    val typePathKind: UByte,            // 0=array, 1=nested, 2=wildcard bound, 3=type argument
    val typeArgumentIndex: UByte        // Used only for kind 3
)
