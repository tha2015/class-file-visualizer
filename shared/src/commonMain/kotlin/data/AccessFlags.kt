package data

/**
 * Access flags for classes (JVM Spec 4.1)
 */
object ClassAccessFlags {
    const val ACC_PUBLIC: UShort = 0x0001u
    const val ACC_FINAL: UShort = 0x0010u
    const val ACC_SUPER: UShort = 0x0020u
    const val ACC_INTERFACE: UShort = 0x0200u
    const val ACC_ABSTRACT: UShort = 0x0400u
    const val ACC_SYNTHETIC: UShort = 0x1000u
    const val ACC_ANNOTATION: UShort = 0x2000u
    const val ACC_ENUM: UShort = 0x4000u
    const val ACC_MODULE: UShort = 0x8000u
}

/**
 * Access flags for fields (JVM Spec 4.5)
 */
object FieldAccessFlags {
    const val ACC_PUBLIC: UShort = 0x0001u
    const val ACC_PRIVATE: UShort = 0x0002u
    const val ACC_PROTECTED: UShort = 0x0004u
    const val ACC_STATIC: UShort = 0x0008u
    const val ACC_FINAL: UShort = 0x0010u
    const val ACC_VOLATILE: UShort = 0x0040u
    const val ACC_TRANSIENT: UShort = 0x0080u
    const val ACC_SYNTHETIC: UShort = 0x1000u
    const val ACC_ENUM: UShort = 0x4000u
}

/**
 * Access flags for methods (JVM Spec 4.6)
 */
object MethodAccessFlags {
    const val ACC_PUBLIC: UShort = 0x0001u
    const val ACC_PRIVATE: UShort = 0x0002u
    const val ACC_PROTECTED: UShort = 0x0004u
    const val ACC_STATIC: UShort = 0x0008u
    const val ACC_FINAL: UShort = 0x0010u
    const val ACC_SYNCHRONIZED: UShort = 0x0020u
    const val ACC_BRIDGE: UShort = 0x0040u
    const val ACC_VARARGS: UShort = 0x0080u
    const val ACC_NATIVE: UShort = 0x0100u
    const val ACC_ABSTRACT: UShort = 0x0400u
    const val ACC_STRICT: UShort = 0x0800u
    const val ACC_SYNTHETIC: UShort = 0x1000u
}

/**
 * Utility extension to check if a flag is set
 */
fun UShort.hasFlag(flag: UShort): Boolean = (this and flag) != 0u.toUShort()

/**
 * Decode class access flags to human-readable string
 */
fun UShort.decodeClassAccessFlags(): String {
    val flags = mutableListOf<String>()
    if (hasFlag(ClassAccessFlags.ACC_PUBLIC)) flags.add("PUBLIC")
    if (hasFlag(ClassAccessFlags.ACC_FINAL)) flags.add("FINAL")
    if (hasFlag(ClassAccessFlags.ACC_SUPER)) flags.add("SUPER")
    if (hasFlag(ClassAccessFlags.ACC_INTERFACE)) flags.add("INTERFACE")
    if (hasFlag(ClassAccessFlags.ACC_ABSTRACT)) flags.add("ABSTRACT")
    if (hasFlag(ClassAccessFlags.ACC_SYNTHETIC)) flags.add("SYNTHETIC")
    if (hasFlag(ClassAccessFlags.ACC_ANNOTATION)) flags.add("ANNOTATION")
    if (hasFlag(ClassAccessFlags.ACC_ENUM)) flags.add("ENUM")
    if (hasFlag(ClassAccessFlags.ACC_MODULE)) flags.add("MODULE")
    return "$this (${flags.joinToString(" | ")})"
}

/**
 * Decode field access flags to human-readable string
 */
fun UShort.decodeFieldAccessFlags(): String {
    val flags = mutableListOf<String>()
    if (hasFlag(FieldAccessFlags.ACC_PUBLIC)) flags.add("PUBLIC")
    if (hasFlag(FieldAccessFlags.ACC_PRIVATE)) flags.add("PRIVATE")
    if (hasFlag(FieldAccessFlags.ACC_PROTECTED)) flags.add("PROTECTED")
    if (hasFlag(FieldAccessFlags.ACC_STATIC)) flags.add("STATIC")
    if (hasFlag(FieldAccessFlags.ACC_FINAL)) flags.add("FINAL")
    if (hasFlag(FieldAccessFlags.ACC_VOLATILE)) flags.add("VOLATILE")
    if (hasFlag(FieldAccessFlags.ACC_TRANSIENT)) flags.add("TRANSIENT")
    if (hasFlag(FieldAccessFlags.ACC_SYNTHETIC)) flags.add("SYNTHETIC")
    if (hasFlag(FieldAccessFlags.ACC_ENUM)) flags.add("ENUM")
    return "$this (${flags.joinToString(" | ")})"
}

/**
 * Decode method access flags to human-readable string
 */
fun UShort.decodeMethodAccessFlags(): String {
    val flags = mutableListOf<String>()
    if (hasFlag(MethodAccessFlags.ACC_PUBLIC)) flags.add("PUBLIC")
    if (hasFlag(MethodAccessFlags.ACC_PRIVATE)) flags.add("PRIVATE")
    if (hasFlag(MethodAccessFlags.ACC_PROTECTED)) flags.add("PROTECTED")
    if (hasFlag(MethodAccessFlags.ACC_STATIC)) flags.add("STATIC")
    if (hasFlag(MethodAccessFlags.ACC_FINAL)) flags.add("FINAL")
    if (hasFlag(MethodAccessFlags.ACC_SYNCHRONIZED)) flags.add("SYNCHRONIZED")
    if (hasFlag(MethodAccessFlags.ACC_BRIDGE)) flags.add("BRIDGE")
    if (hasFlag(MethodAccessFlags.ACC_VARARGS)) flags.add("VARARGS")
    if (hasFlag(MethodAccessFlags.ACC_NATIVE)) flags.add("NATIVE")
    if (hasFlag(MethodAccessFlags.ACC_ABSTRACT)) flags.add("ABSTRACT")
    if (hasFlag(MethodAccessFlags.ACC_STRICT)) flags.add("STRICT")
    if (hasFlag(MethodAccessFlags.ACC_SYNTHETIC)) flags.add("SYNTHETIC")
    return "$this (${flags.joinToString(" | ")})"
}
