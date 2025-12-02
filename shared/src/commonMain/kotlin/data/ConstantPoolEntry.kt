package data

/**
 * Constant pool entries (JVM Spec 4.4)
 * Sealed class hierarchy for the 17 constant pool entry types
 */
sealed class ConstantPoolEntry {
    abstract val tag: UByte
}

/**
 * CONSTANT_Utf8_info (tag=1)
 * Modified UTF-8 string
 */
data class ConstantUtf8(
    override val tag: UByte = 1u,
    val value: String,
) : ConstantPoolEntry()

/**
 * CONSTANT_Integer_info (tag=3)
 */
data class ConstantInteger(
    override val tag: UByte = 3u,
    val value: Int
) : ConstantPoolEntry()

/**
 * CONSTANT_Float_info (tag=4)
 */
data class ConstantFloat(
    override val tag: UByte = 4u,
    val value: Float
) : ConstantPoolEntry()

/**
 * CONSTANT_Long_info (tag=5)
 * Note: Takes 2 constant pool slots
 */
data class ConstantLong(
    override val tag: UByte = 5u,
    val value: Long
) : ConstantPoolEntry()

/**
 * CONSTANT_Double_info (tag=6)
 * Note: Takes 2 constant pool slots
 */
data class ConstantDouble(
    override val tag: UByte = 6u,
    val value: Double
) : ConstantPoolEntry()

/**
 * CONSTANT_Class_info (tag=7)
 */
data class ConstantClass(
    override val tag: UByte = 7u,
    val nameIndex: UShort  // Index to CONSTANT_Utf8
) : ConstantPoolEntry()

/**
 * CONSTANT_String_info (tag=8)
 */
data class ConstantString(
    override val tag: UByte = 8u,
    val stringIndex: UShort  // Index to CONSTANT_Utf8
) : ConstantPoolEntry()

/**
 * CONSTANT_Fieldref_info (tag=9)
 */
data class ConstantFieldref(
    override val tag: UByte = 9u,
    val classIndex: UShort,         // Index to CONSTANT_Class
    val nameAndTypeIndex: UShort    // Index to CONSTANT_NameAndType
) : ConstantPoolEntry()

/**
 * CONSTANT_Methodref_info (tag=10)
 */
data class ConstantMethodref(
    override val tag: UByte = 10u,
    val classIndex: UShort,
    val nameAndTypeIndex: UShort
) : ConstantPoolEntry()

/**
 * CONSTANT_InterfaceMethodref_info (tag=11)
 */
data class ConstantInterfaceMethodref(
    override val tag: UByte = 11u,
    val classIndex: UShort,
    val nameAndTypeIndex: UShort
) : ConstantPoolEntry()

/**
 * CONSTANT_NameAndType_info (tag=12)
 */
data class ConstantNameAndType(
    override val tag: UByte = 12u,
    val nameIndex: UShort,          // Index to CONSTANT_Utf8
    val descriptorIndex: UShort     // Index to CONSTANT_Utf8
) : ConstantPoolEntry()

/**
 * CONSTANT_MethodHandle_info (tag=15)
 */
data class ConstantMethodHandle(
    override val tag: UByte = 15u,
    val referenceKind: UByte,       // 1-9 (REF_getField, REF_invokeVirtual, etc.)
    val referenceIndex: UShort      // Index to appropriate constant
) : ConstantPoolEntry()

/**
 * CONSTANT_MethodType_info (tag=16)
 */
data class ConstantMethodType(
    override val tag: UByte = 16u,
    val descriptorIndex: UShort     // Index to CONSTANT_Utf8
) : ConstantPoolEntry()

/**
 * CONSTANT_Dynamic_info (tag=17)
 */
data class ConstantDynamic(
    override val tag: UByte = 17u,
    val bootstrapMethodAttrIndex: UShort,
    val nameAndTypeIndex: UShort
) : ConstantPoolEntry()

/**
 * CONSTANT_InvokeDynamic_info (tag=18)
 */
data class ConstantInvokeDynamic(
    override val tag: UByte = 18u,
    val bootstrapMethodAttrIndex: UShort,
    val nameAndTypeIndex: UShort
) : ConstantPoolEntry()

/**
 * CONSTANT_Module_info (tag=19)
 */
data class ConstantModule(
    override val tag: UByte = 19u,
    val nameIndex: UShort           // Index to CONSTANT_Utf8
) : ConstantPoolEntry()

/**
 * CONSTANT_Package_info (tag=20)
 */
data class ConstantPackage(
    override val tag: UByte = 20u,
    val nameIndex: UShort           // Index to CONSTANT_Utf8
) : ConstantPoolEntry()
