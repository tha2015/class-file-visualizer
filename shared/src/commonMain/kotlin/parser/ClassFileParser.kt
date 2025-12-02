package parser

import data.*
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readDouble
import kotlinx.io.readFloat
import kotlinx.io.readUByte
import kotlinx.io.readUInt
import kotlinx.io.readUShort

// ===== Public API =====

/**
 * Parse a Java class file from bytes.
 * @param bytes The raw bytes of the .class file
 * @return Parsed ClassFile data model
 * @throws ParseException if the class file is malformed
 */
fun parseClassFile(bytes: ByteArray): ClassFile =
    Buffer().also { it.write(bytes) }.parseClassFile()

/**
 * Exception thrown when parsing fails
 */
class ParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

// ===== Core Parser =====

fun Source.parseClassFile(): ClassFile {
    val magic = readUInt()
    require(magic == 0xCAFEBABE.toUInt()) {
        "Invalid magic number: 0x${magic.toString(16)}"
    }

    val minorVersion = readUShort()
    val majorVersion = readUShort()
    val constantPool = parseConstantPool()
    val accessFlags = readUShort()
    val thisClass = readUShort()
    val superClass = readUShort()
    val interfaces = readUShortList()
    val fields = readMemberList { parseField(constantPool) }
    val methods = readMemberList { parseMethod(constantPool) }
    val attributes = parseAttributes(constantPool)

    return ClassFile(
        magic = magic,
        minorVersion = minorVersion,
        majorVersion = majorVersion,
        constantPool = constantPool,
        accessFlags = accessFlags,
        thisClass = thisClass,
        superClass = superClass,
        interfaces = interfaces,
        fields = fields,
        methods = methods,
        attributes = attributes
    )
}

// ===== Constant Pool =====

fun Source.parseConstantPool(): List<ConstantPoolEntry?> {
    val count = readUShort().toInt()
    val pool = mutableListOf<ConstantPoolEntry?>(null) // Index 0 reserved

    var i = 1
    while (i < count) {
        val entry = parseConstantPoolEntry()
        pool.add(entry)

        // Long and Double occupy 2 slots (JVM Spec 4.4.5)
        i += if (entry is ConstantLong || entry is ConstantDouble) {
            pool.add(null)
            2
        } else {
            1
        }
    }

    return pool
}

fun Source.parseConstantPoolEntry(): ConstantPoolEntry {
    val tag = readUByte()
    return when (tag.toInt()) {
        1 -> ConstantUtf8(tag, readModifiedUtf8())
        3 -> ConstantInteger(tag, readInt())
        4 -> ConstantFloat(tag, readFloat())
        5 -> ConstantLong(tag, readLong())
        6 -> ConstantDouble(tag, readDouble())
        7 -> ConstantClass(tag, readUShort())
        8 -> ConstantString(tag, readUShort())
        9 -> ConstantFieldref(tag, readUShort(), readUShort())
        10 -> ConstantMethodref(tag, readUShort(), readUShort())
        11 -> ConstantInterfaceMethodref(tag, readUShort(), readUShort())
        12 -> ConstantNameAndType(tag, readUShort(), readUShort())
        15 -> ConstantMethodHandle(tag, readUByte(), readUShort())
        16 -> ConstantMethodType(tag, readUShort())
        17 -> ConstantDynamic(tag, readUShort(), readUShort())
        18 -> ConstantInvokeDynamic(tag, readUShort(), readUShort())
        19 -> ConstantModule(tag, readUShort())
        20 -> ConstantPackage(tag, readUShort())
        else -> throw ParseException("Unknown constant pool tag: $tag")
    }
}

// ===== Members (Fields & Methods) =====

fun Source.parseField(constantPool: List<ConstantPoolEntry?>): FieldInfo =
    FieldInfo(
        accessFlags = readUShort(),
        nameIndex = readUShort(),
        descriptorIndex = readUShort(),
        attributes = parseAttributes(constantPool)
    )

fun Source.parseMethod(constantPool: List<ConstantPoolEntry?>): MethodInfo =
    MethodInfo(
        accessFlags = readUShort(),
        nameIndex = readUShort(),
        descriptorIndex = readUShort(),
        attributes = parseAttributes(constantPool)
    )

// ===== Attributes =====

fun Source.parseAttributes(constantPool: List<ConstantPoolEntry?>): List<AttributeInfo> {
    val count = readUShort().toInt()
    return List(count) { parseAttribute(constantPool) }
}

fun Source.parseAttribute(constantPool: List<ConstantPoolEntry?>): AttributeInfo {
    val nameIndex = readUShort()
    val length = readUInt()

    val name = constantPool.getUtf8(nameIndex)
        ?: throw ParseException("Invalid attribute name index: $nameIndex")

    return when (name) {
        "Code" -> parseCodeAttribute(nameIndex, constantPool)
        "ConstantValue" -> ConstantValueAttribute(nameIndex, readUShort())
        "StackMapTable" -> parseStackMapTableAttribute(nameIndex)
        "StackMap" -> parseStackMapAttribute(nameIndex)
        "Exceptions" -> parseExceptionsAttribute(nameIndex)
        "InnerClasses" -> parseInnerClassesAttribute(nameIndex)
        "EnclosingMethod" -> EnclosingMethodAttribute(nameIndex, readUShort(), readUShort())
        "Synthetic" -> SyntheticAttribute(nameIndex)
        "Signature" -> SignatureAttribute(nameIndex, readUShort())
        "SourceFile" -> SourceFileAttribute(nameIndex, readUShort())
        "SourceDebugExtension" -> SourceDebugExtensionAttribute(nameIndex, readByteArray(length.toInt()))
        "LineNumberTable" -> parseLineNumberTableAttribute(nameIndex)
        "LocalVariableTable" -> parseLocalVariableTableAttribute(nameIndex)
        "LocalVariableTypeTable" -> parseLocalVariableTypeTableAttribute(nameIndex)
        "Deprecated" -> DeprecatedAttribute(nameIndex)
        "RuntimeVisibleAnnotations" -> parseRuntimeVisibleAnnotationsAttribute(nameIndex)
        "RuntimeInvisibleAnnotations" -> parseRuntimeInvisibleAnnotationsAttribute(nameIndex)
        "RuntimeVisibleParameterAnnotations" -> parseRuntimeVisibleParameterAnnotationsAttribute(nameIndex)
        "RuntimeInvisibleParameterAnnotations" -> parseRuntimeInvisibleParameterAnnotationsAttribute(nameIndex)
        "RuntimeVisibleTypeAnnotations" -> parseRuntimeVisibleTypeAnnotationsAttribute(nameIndex)
        "RuntimeInvisibleTypeAnnotations" -> parseRuntimeInvisibleTypeAnnotationsAttribute(nameIndex)
        "AnnotationDefault" -> parseAnnotationDefaultAttribute(nameIndex)
        "BootstrapMethods" -> parseBootstrapMethodsAttribute(nameIndex)
        "MethodParameters" -> parseMethodParametersAttribute(nameIndex)
        "Module" -> parseModuleAttribute(nameIndex)
        "ModulePackages" -> parseModulePackagesAttribute(nameIndex)
        "ModuleMainClass" -> ModuleMainClassAttribute(nameIndex, readUShort())
        "NestHost" -> NestHostAttribute(nameIndex, readUShort())
        "NestMembers" -> parseNestMembersAttribute(nameIndex)
        "Record" -> parseRecordAttribute(nameIndex, constantPool)
        "PermittedSubclasses" -> parsePermittedSubclassesAttribute(nameIndex)
        else -> UnknownAttribute(nameIndex, readByteArray(length.toInt()))
    }
}

// ===== Specific Attribute Parsers =====

fun Source.parseCodeAttribute(nameIndex: UShort, constantPool: List<ConstantPoolEntry?>): CodeAttribute {
    val maxStack = readUShort()
    val maxLocals = readUShort()
    val codeLength = readUInt()
    val code = readByteArray(codeLength.toInt())
    val exceptionTableLength = readUShort().toInt()
    val exceptionTable = List(exceptionTableLength) {
        ExceptionHandler(
            startPc = readUShort(),
            endPc = readUShort(),
            handlerPc = readUShort(),
            catchType = readUShort()
        )
    }
    val attributes = parseAttributes(constantPool)

    return CodeAttribute(
        attributeNameIndex = nameIndex,
        maxStack = maxStack,
        maxLocals = maxLocals,
        code = code,
        exceptionTable = exceptionTable,
        attributes = attributes
    )
}

fun Source.parseStackMapTableAttribute(nameIndex: UShort): StackMapTableAttribute {
    val numberOfEntries = readUShort().toInt()
    val entries = List(numberOfEntries) { parseStackMapFrame() }
    return StackMapTableAttribute(nameIndex, entries)
}

fun Source.parseStackMapAttribute(nameIndex: UShort): StackMapAttribute {
    val numberOfEntries = readUShort().toInt()
    val entries = List(numberOfEntries) { parseOldStackMapFrame() }
    return StackMapAttribute(nameIndex, entries)
}

fun Source.parseOldStackMapFrame(): StackMapFrame {
    // Old StackMap format (pre-Java 6): uncompressed full frames
    // Structure: offset (u2), number_of_locals (u2), locals[], number_of_stack_items (u2), stack[]
    val offset = readUShort()
    val numberOfLocals = readUShort().toInt()
    val locals = List(numberOfLocals) { parseVerificationTypeInfo() }
    val numberOfStackItems = readUShort().toInt()
    val stack = List(numberOfStackItems) { parseVerificationTypeInfo() }

    // Convert to FullFrame (frame type 255) for unified representation
    return FullFrame(
        frameType = 255u,
        offsetDelta = offset,  // In old format, this is absolute offset, not delta
        locals = locals,
        stack = stack
    )
}

fun Source.parseStackMapFrame(): StackMapFrame {
    val frameType = readUByte()
    return when (frameType.toInt()) {
        in 0..63 -> SameFrame(frameType)
        in 64..127 -> SameLocals1StackItemFrame(frameType, parseVerificationTypeInfo())
        247 -> SameLocals1StackItemFrameExtended(frameType, readUShort(), parseVerificationTypeInfo())
        in 248..250 -> ChopFrame(frameType, readUShort())
        251 -> SameFrameExtended(frameType, readUShort())
        in 252..254 -> {
            val offsetDelta = readUShort()
            val k = frameType.toInt() - 251
            val locals = List(k) { parseVerificationTypeInfo() }
            AppendFrame(frameType, offsetDelta, locals)
        }
        255 -> {
            val offsetDelta = readUShort()
            val numberOfLocals = readUShort().toInt()
            val locals = List(numberOfLocals) { parseVerificationTypeInfo() }
            val numberOfStackItems = readUShort().toInt()
            val stack = List(numberOfStackItems) { parseVerificationTypeInfo() }
            FullFrame(frameType, offsetDelta, locals, stack)
        }
        else -> throw ParseException("Invalid stack map frame type: $frameType")
    }
}

fun Source.parseVerificationTypeInfo(): VerificationTypeInfo {
    val tag = readUByte()
    return when (tag.toInt()) {
        0 -> TopVariableInfo(tag)
        1 -> IntegerVariableInfo(tag)
        2 -> FloatVariableInfo(tag)
        3 -> DoubleVariableInfo(tag)
        4 -> LongVariableInfo(tag)
        5 -> NullVariableInfo(tag)
        6 -> UninitializedThisVariableInfo(tag)
        7 -> ObjectVariableInfo(tag, readUShort())
        8 -> UninitializedVariableInfo(tag, readUShort())
        else -> throw ParseException("Invalid verification type info tag: $tag")
    }
}

fun Source.parseExceptionsAttribute(nameIndex: UShort): ExceptionsAttribute {
    val numberOfExceptions = readUShort().toInt()
    val exceptionIndexTable = List(numberOfExceptions) { readUShort() }
    return ExceptionsAttribute(nameIndex, exceptionIndexTable)
}

fun Source.parseInnerClassesAttribute(nameIndex: UShort): InnerClassesAttribute {
    val numberOfClasses = readUShort().toInt()
    val classes = List(numberOfClasses) {
        InnerClassInfo(
            innerClassInfoIndex = readUShort(),
            outerClassInfoIndex = readUShort(),
            innerNameIndex = readUShort(),
            innerClassAccessFlags = readUShort()
        )
    }
    return InnerClassesAttribute(nameIndex, classes)
}

fun Source.parseLineNumberTableAttribute(nameIndex: UShort): LineNumberTableAttribute {
    val lineNumberTableLength = readUShort().toInt()
    val lineNumberTable = List(lineNumberTableLength) {
        LineNumberEntry(
            startPc = readUShort(),
            lineNumber = readUShort()
        )
    }
    return LineNumberTableAttribute(nameIndex, lineNumberTable)
}

fun Source.parseLocalVariableTableAttribute(nameIndex: UShort): LocalVariableTableAttribute {
    val localVariableTableLength = readUShort().toInt()
    val localVariableTable = List(localVariableTableLength) {
        LocalVariableEntry(
            startPc = readUShort(),
            length = readUShort(),
            nameIndex = readUShort(),
            descriptorIndex = readUShort(),
            index = readUShort()
        )
    }
    return LocalVariableTableAttribute(nameIndex, localVariableTable)
}

fun Source.parseLocalVariableTypeTableAttribute(nameIndex: UShort): LocalVariableTypeTableAttribute {
    val localVariableTypeTableLength = readUShort().toInt()
    val localVariableTypeTable = List(localVariableTypeTableLength) {
        LocalVariableTypeEntry(
            startPc = readUShort(),
            length = readUShort(),
            nameIndex = readUShort(),
            signatureIndex = readUShort(),
            index = readUShort()
        )
    }
    return LocalVariableTypeTableAttribute(nameIndex, localVariableTypeTable)
}

fun Source.parseRuntimeVisibleAnnotationsAttribute(nameIndex: UShort): RuntimeVisibleAnnotationsAttribute {
    val numAnnotations = readUShort().toInt()
    val annotations = List(numAnnotations) { parseAnnotation() }
    return RuntimeVisibleAnnotationsAttribute(nameIndex, annotations)
}

fun Source.parseRuntimeInvisibleAnnotationsAttribute(nameIndex: UShort): RuntimeInvisibleAnnotationsAttribute {
    val numAnnotations = readUShort().toInt()
    val annotations = List(numAnnotations) { parseAnnotation() }
    return RuntimeInvisibleAnnotationsAttribute(nameIndex, annotations)
}

fun Source.parseRuntimeVisibleParameterAnnotationsAttribute(nameIndex: UShort): RuntimeVisibleParameterAnnotationsAttribute {
    val numParameters = readUByte().toInt()
    val parameterAnnotations = List(numParameters) {
        val numAnnotations = readUShort().toInt()
        List(numAnnotations) { parseAnnotation() }
    }
    return RuntimeVisibleParameterAnnotationsAttribute(nameIndex, parameterAnnotations)
}

fun Source.parseRuntimeInvisibleParameterAnnotationsAttribute(nameIndex: UShort): RuntimeInvisibleParameterAnnotationsAttribute {
    val numParameters = readUByte().toInt()
    val parameterAnnotations = List(numParameters) {
        val numAnnotations = readUShort().toInt()
        List(numAnnotations) { parseAnnotation() }
    }
    return RuntimeInvisibleParameterAnnotationsAttribute(nameIndex, parameterAnnotations)
}

fun Source.parseRuntimeVisibleTypeAnnotationsAttribute(nameIndex: UShort): RuntimeVisibleTypeAnnotationsAttribute {
    val numAnnotations = readUShort().toInt()
    val annotations = List(numAnnotations) { parseTypeAnnotation() }
    return RuntimeVisibleTypeAnnotationsAttribute(nameIndex, annotations)
}

fun Source.parseRuntimeInvisibleTypeAnnotationsAttribute(nameIndex: UShort): RuntimeInvisibleTypeAnnotationsAttribute {
    val numAnnotations = readUShort().toInt()
    val annotations = List(numAnnotations) { parseTypeAnnotation() }
    return RuntimeInvisibleTypeAnnotationsAttribute(nameIndex, annotations)
}

fun Source.parseAnnotationDefaultAttribute(nameIndex: UShort): AnnotationDefaultAttribute {
    val defaultValue = parseElementValue()
    return AnnotationDefaultAttribute(nameIndex, defaultValue)
}

fun Source.parseBootstrapMethodsAttribute(nameIndex: UShort): BootstrapMethodsAttribute {
    val numBootstrapMethods = readUShort().toInt()
    val bootstrapMethods = List(numBootstrapMethods) {
        val bootstrapMethodRef = readUShort()
        val numBootstrapArguments = readUShort().toInt()
        val bootstrapArguments = List(numBootstrapArguments) { readUShort() }
        BootstrapMethod(bootstrapMethodRef, bootstrapArguments)
    }
    return BootstrapMethodsAttribute(nameIndex, bootstrapMethods)
}

fun Source.parseMethodParametersAttribute(nameIndex: UShort): MethodParametersAttribute {
    val parametersCount = readUByte().toInt()
    val parameters = List(parametersCount) {
        MethodParameter(
            nameIndex = readUShort(),
            accessFlags = readUShort()
        )
    }
    return MethodParametersAttribute(nameIndex, parameters)
}

fun Source.parseModuleAttribute(nameIndex: UShort): ModuleAttribute {
    val moduleNameIndex = readUShort()
    val moduleFlags = readUShort()
    val moduleVersionIndex = readUShort()

    val requiresCount = readUShort().toInt()
    val requires = List(requiresCount) {
        ModuleRequires(
            requiresIndex = readUShort(),
            requiresFlags = readUShort(),
            requiresVersionIndex = readUShort()
        )
    }

    val exportsCount = readUShort().toInt()
    val exports = List(exportsCount) {
        val exportsIndex = readUShort()
        val exportsFlags = readUShort()
        val exportsToCount = readUShort().toInt()
        val exportsToIndex = List(exportsToCount) { readUShort() }
        ModuleExports(exportsIndex, exportsFlags, exportsToIndex)
    }

    val opensCount = readUShort().toInt()
    val opens = List(opensCount) {
        val opensIndex = readUShort()
        val opensFlags = readUShort()
        val opensToCount = readUShort().toInt()
        val opensToIndex = List(opensToCount) { readUShort() }
        ModuleOpens(opensIndex, opensFlags, opensToIndex)
    }

    val usesCount = readUShort().toInt()
    val usesIndex = List(usesCount) { readUShort() }

    val providesCount = readUShort().toInt()
    val provides = List(providesCount) {
        val providesIndex = readUShort()
        val providesWithCount = readUShort().toInt()
        val providesWithIndex = List(providesWithCount) { readUShort() }
        ModuleProvides(providesIndex, providesWithIndex)
    }

    return ModuleAttribute(
        attributeNameIndex = nameIndex,
        moduleNameIndex = moduleNameIndex,
        moduleFlags = moduleFlags,
        moduleVersionIndex = moduleVersionIndex,
        requires = requires,
        exports = exports,
        opens = opens,
        uses = usesIndex,
        provides = provides
    )
}

fun Source.parseModulePackagesAttribute(nameIndex: UShort): ModulePackagesAttribute {
    val packageCount = readUShort().toInt()
    val packageIndex = List(packageCount) { readUShort() }
    return ModulePackagesAttribute(nameIndex, packageIndex)
}

fun Source.parseNestMembersAttribute(nameIndex: UShort): NestMembersAttribute {
    val numberOfClasses = readUShort().toInt()
    val classes = List(numberOfClasses) { readUShort() }
    return NestMembersAttribute(nameIndex, classes)
}

fun Source.parseRecordAttribute(nameIndex: UShort, constantPool: List<ConstantPoolEntry?>): RecordAttribute {
    val componentsCount = readUShort().toInt()
    val components = List(componentsCount) {
        RecordComponentInfo(
            nameIndex = readUShort(),
            descriptorIndex = readUShort(),
            attributes = parseAttributes(constantPool)
        )
    }
    return RecordAttribute(nameIndex, components)
}

fun Source.parsePermittedSubclassesAttribute(nameIndex: UShort): PermittedSubclassesAttribute {
    val numberOfClasses = readUShort().toInt()
    val classes = List(numberOfClasses) { readUShort() }
    return PermittedSubclassesAttribute(nameIndex, classes)
}

// ===== Annotation Parsers =====

fun Source.parseAnnotation(): data.Annotation {
    val typeIndex = readUShort()
    val numElementValuePairs = readUShort().toInt()
    val elementValuePairs = List(numElementValuePairs) {
        ElementValuePair(
            elementNameIndex = readUShort(),
            value = parseElementValue()
        )
    }
    return Annotation(typeIndex, elementValuePairs)
}

fun Source.parseElementValue(): ElementValue {
    val tag = readUByte()
    return when (tag.toInt().toChar()) {
        'B', 'C', 'D', 'F', 'I', 'J', 'S', 'Z', 's' -> {
            ConstValueIndex(tag.toInt().toChar(), readUShort())
        }
        'e' -> {
            EnumConstValue(tag.toInt().toChar(), readUShort(), readUShort())
        }
        'c' -> {
            ClassInfoIndex(tag.toInt().toChar(), readUShort())
        }
        '@' -> {
            AnnotationValue(tag.toInt().toChar(), parseAnnotation())
        }
        '[' -> {
            val numValues = readUShort().toInt()
            val values = List(numValues) { parseElementValue() }
            ArrayValue(tag.toInt().toChar(), values)
        }
        else -> throw ParseException("Invalid element value tag: ${tag.toInt().toChar()}")
    }
}

fun Source.parseTypeAnnotation(): TypeAnnotation {
    val targetType = readUByte()
    val targetInfo = parseTargetInfo(targetType)
    val typePath = parseTypePath()
    val typeIndex = readUShort()
    val numElementValuePairs = readUShort().toInt()
    val elementValuePairs = List(numElementValuePairs) {
        ElementValuePair(
            elementNameIndex = readUShort(),
            value = parseElementValue()
        )
    }
    return TypeAnnotation(targetType, targetInfo, typePath, typeIndex, elementValuePairs)
}

fun Source.parseTargetInfo(targetType: UByte): TargetInfo {
    return when (targetType.toInt()) {
        0x00, 0x01 -> TypeParameterTarget(readUByte())
        0x10 -> SupertypeTarget(readUShort())
        0x11, 0x12 -> TypeParameterBoundTarget(readUByte(), readUByte())
        0x13, 0x14, 0x15 -> EmptyTarget
        0x16 -> FormalParameterTarget(readUByte())
        0x17 -> ThrowsTarget(readUShort())
        0x40, 0x41 -> {
            val tableLength = readUShort().toInt()
            val table = List(tableLength) {
                LocalvarTargetEntry(readUShort(), readUShort(), readUShort())
            }
            LocalvarTarget(table)
        }
        0x42 -> CatchTarget(readUShort())
        0x43, 0x44, 0x45, 0x46 -> OffsetTarget(readUShort())
        0x47, 0x48, 0x49, 0x4A, 0x4B -> TypeArgumentTarget(readUShort(), readUByte())
        else -> throw ParseException("Invalid type annotation target type: 0x${targetType.toString(16)}")
    }
}

fun Source.parseTypePath(): TypePath {
    val pathLength = readUByte().toInt()
    val path = List(pathLength) {
        TypePathEntry(
            typePathKind = readUByte(),
            typeArgumentIndex = readUByte()
        )
    }
    return TypePath(path)
}

// ===== Helper Functions =====

/**
 * Read Modified UTF-8 string (JVM Spec 4.4.7)
 * Modified UTF-8 differs from standard UTF-8 in two ways:
 * 1. The null character (U+0000) is encoded as 0xC0 0x80
 * 2. Supplementary characters (U+10000 to U+10FFFF) are encoded using surrogate pairs
 */
fun Source.readModifiedUtf8(): String {
    val length = readUShort().toInt()
    val bytes = readByteArray(length)
    return decodeModifiedUtf8(bytes)
}

/**
 * Decode Modified UTF-8 bytes to String
 */
private fun decodeModifiedUtf8(bytes: ByteArray): String {
    val result = StringBuilder(bytes.size)
    var i = 0

    while (i < bytes.size) {
        val byte1 = bytes[i++].toInt() and 0xFF
        when {
            byte1 and 0x80 == 0 -> {
                // Single byte: 0xxxxxxx
                result.append(byte1.toChar())
            }
            byte1 and 0xE0 == 0xC0 -> {
                // Two bytes: 110xxxxx 10xxxxxx
                if (i >= bytes.size) throw ParseException("Truncated UTF-8 at offset ${i - 1}")
                val byte2 = bytes[i++].toInt() and 0xFF
                result.append((((byte1 and 0x1F) shl 6) or (byte2 and 0x3F)).toChar())
            }
            byte1 and 0xF0 == 0xE0 -> {
                // Three bytes: 1110xxxx 10xxxxxx 10xxxxxx
                if (i + 1 >= bytes.size) throw ParseException("Truncated UTF-8 at offset ${i - 1}")
                val byte2 = bytes[i++].toInt() and 0xFF
                val byte3 = bytes[i++].toInt() and 0xFF
                result.append((((byte1 and 0x0F) shl 12) or
                               ((byte2 and 0x3F) shl 6) or
                               (byte3 and 0x3F)).toChar())
            }
            else -> throw ParseException("Invalid UTF-8 byte: 0x${byte1.toString(16)} at offset ${i - 1}")
        }
    }

    return result.toString()
}

/**
 * Helper to get UTF-8 string from constant pool
 */
fun List<ConstantPoolEntry?>.getUtf8(index: UShort): String? =
    (getOrNull(index.toInt()) as? ConstantUtf8)?.value

/**
 * Helper to read list of UShorts prefixed by count
 */
fun Source.readUShortList(): List<UShort> {
    val count = readUShort().toInt()
    return List(count) { readUShort() }
}

/**
 * Helper to read counted list with custom parser
 */
inline fun <T> Source.readMemberList(parser: () -> T): List<T> {
    val count = readUShort().toInt()
    return List(count) { parser() }
}
