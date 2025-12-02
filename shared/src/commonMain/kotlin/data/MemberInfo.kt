package data

/**
 * Field information (JVM Spec 4.5)
 */
data class FieldInfo(
    val accessFlags: UShort,
    val nameIndex: UShort,          // Index to CONSTANT_Utf8
    val descriptorIndex: UShort,    // Index to CONSTANT_Utf8
    val attributes: List<AttributeInfo>
)

/**
 * Method information (JVM Spec 4.6)
 */
data class MethodInfo(
    val accessFlags: UShort,
    val nameIndex: UShort,          // Index to CONSTANT_Utf8
    val descriptorIndex: UShort,    // Index to CONSTANT_Utf8
    val attributes: List<AttributeInfo>
)
