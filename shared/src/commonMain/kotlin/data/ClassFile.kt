package data

/**
 * ClassFile structure (JVM Spec 4.1)
 * Root data structure representing a Java class file
 */
data class ClassFile(
    val magic: UInt,                                // Must be 0xCAFEBABE
    val minorVersion: UShort,
    val majorVersion: UShort,
    val constantPool: List<ConstantPoolEntry?>,     // Index 0 is null (reserved), indices 1-n
    val accessFlags: UShort,
    val thisClass: UShort,                          // Index into constant pool (CONSTANT_Class)
    val superClass: UShort,                         // Index into constant pool (0 for java.lang.Object)
    val interfaces: List<UShort>,                   // Indices into constant pool (CONSTANT_Class)
    val fields: List<FieldInfo>,
    val methods: List<MethodInfo>,
    val attributes: List<AttributeInfo>
)
