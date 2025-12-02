package api

import data.*
import parser.parseClassFile

// ===== Public API =====

/**
 * Parse a Java class file from bytes.
 *
 * @param bytes Raw bytes of a .class file
 * @return Parsed ClassFile data structure
 * @throws parser.ParseException if the class file is malformed
 */
fun parse(bytes: ByteArray): ClassFile = parseClassFile(bytes)

/**
 * Access class file data with convenient extension functions for resolving
 * constant pool references.
 *
 * Usage:
 * ```
 * val classFile = parse(bytes)
 * withClassFile(classFile) {
 *     val className = classFile.thisClass().name()
 *     val methods = classFile.methods.filter { it.name() == "main" }
 * }
 * ```
 *
 * @param classFile The parsed class file to access
 * @param block Lambda with ClassFileScope receiver providing extension functions
 * @return Result of the block
 */
inline fun <T> withClassFile(classFile: ClassFile, block: ClassFileScope.() -> T): T =
    ClassFileScope(classFile).block()

// ===== ClassFileScope =====

/**
 * Scope that provides extension functions for convenient access to class file data.
 * Use via [withClassFile] function.
 */
class ClassFileScope(val classFile: ClassFile) {

    /**
     * Get a constant pool entry by index with type checking
     */
    inline fun <reified T : ConstantPoolEntry> fromConstantPool(index: UShort): T {
        val entry = classFile.constantPool[index.toInt()] as? T
            ?: error("Invalid entry at $index: expected ${T::class.simpleName}")
        return entry
    }

    // ===== ClassFile Extensions =====

    /**
     * Get the resolved ConstantClass for this class
     */
    fun ClassFile.thisClass() = fromConstantPool<ConstantClass>(thisClass)

    /**
     * Get the resolved ConstantClass for the super class
     */
    fun ClassFile.superClass() = fromConstantPool<ConstantClass>(superClass)

    /**
     * Get all resolved interface ConstantClass entries
     */
    fun ClassFile.interfaces() = interfaces.map { fromConstantPool<ConstantClass>(it) }

    /**
     * Get the source file name from SourceFileAttribute if present
     */
    fun ClassFile.sourceFile(): String? {
        val sourceFileAttr = attributes.filterIsInstance<SourceFileAttribute>().firstOrNull()
        return sourceFileAttr?.let { fromConstantPool<ConstantUtf8>(it.sourceFileIndex).value }
    }

    // ===== ConstantClass Extensions =====

    fun ConstantClass.name() = fromConstantPool<ConstantUtf8>(nameIndex).value

    // ===== ConstantString Extensions =====

    fun ConstantString.value() = fromConstantPool<ConstantUtf8>(stringIndex).value

    // ===== FieldInfo Extensions =====

    fun FieldInfo.name() = fromConstantPool<ConstantUtf8>(nameIndex).value

    fun FieldInfo.descriptor() = fromConstantPool<ConstantUtf8>(descriptorIndex).value

    /**
     * Get the constant value of this field, if present (for static final fields)
     */
    fun FieldInfo.constantValue(): Any? {
        val constantValueAttr = attributes.filterIsInstance<ConstantValueAttribute>().firstOrNull()
        return constantValueAttr?.let {
            when (val entry = classFile.constantPool[it.constantValueIndex.toInt()]) {
                is ConstantInteger -> entry.value
                is ConstantFloat -> entry.value
                is ConstantLong -> entry.value
                is ConstantDouble -> entry.value
                is ConstantString -> fromConstantPool<ConstantUtf8>(entry.stringIndex).value
                else -> null
            }
        }
    }

    // ===== MethodInfo Extensions =====

    fun MethodInfo.name() = fromConstantPool<ConstantUtf8>(nameIndex).value

    fun MethodInfo.descriptor() = fromConstantPool<ConstantUtf8>(descriptorIndex).value

    /**
     * Check if this method is a constructor
     */
    fun MethodInfo.isConstructor() = name() == "<init>"

    /**
     * Check if this method is a static initializer
     */
    fun MethodInfo.isStaticInitializer() = name() == "<clinit>"

    /**
     * Get the Code attribute of this method, if present
     */
    fun MethodInfo.code(): CodeAttribute? = attributes.filterIsInstance<CodeAttribute>().firstOrNull()

    // ===== ConstantMethodref/Fieldref/InterfaceMethodref Extensions =====

    fun ConstantMethodref.classInfo() = fromConstantPool<ConstantClass>(classIndex)

    fun ConstantMethodref.nameAndType() = fromConstantPool<ConstantNameAndType>(nameAndTypeIndex)

    fun ConstantFieldref.classInfo() = fromConstantPool<ConstantClass>(classIndex)

    fun ConstantFieldref.nameAndType() = fromConstantPool<ConstantNameAndType>(nameAndTypeIndex)

    fun ConstantInterfaceMethodref.classInfo() = fromConstantPool<ConstantClass>(classIndex)

    fun ConstantInterfaceMethodref.nameAndType() = fromConstantPool<ConstantNameAndType>(nameAndTypeIndex)

    // ===== ConstantNameAndType Extensions =====

    fun ConstantNameAndType.name() = fromConstantPool<ConstantUtf8>(nameIndex).value

    fun ConstantNameAndType.descriptor() = fromConstantPool<ConstantUtf8>(descriptorIndex).value

    // ===== AttributeInfo Extensions =====

    fun AttributeInfo.name() = fromConstantPool<ConstantUtf8>(attributeNameIndex).value
}
