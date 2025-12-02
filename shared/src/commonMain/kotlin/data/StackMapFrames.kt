package data

/**
 * Stack map frame types (JVM Spec 4.7.4)
 * Used for type checking during verification
 */
sealed class StackMapFrame {
    abstract val frameType: UByte
}

/**
 * same_frame
 * Frame type: 0-63
 * Represents a frame with exactly the same locals as the previous frame and empty stack
 */
data class SameFrame(
    override val frameType: UByte  // 0-63
) : StackMapFrame()

/**
 * same_locals_1_stack_item_frame
 * Frame type: 64-127
 * Same locals, stack has one entry
 */
data class SameLocals1StackItemFrame(
    override val frameType: UByte,  // 64-127
    val stack: VerificationTypeInfo
) : StackMapFrame()

/**
 * same_locals_1_stack_item_frame_extended
 * Frame type: 247
 * Same as same_locals_1_stack_item_frame but with explicit offset_delta
 */
data class SameLocals1StackItemFrameExtended(
    override val frameType: UByte = 247u,
    val offsetDelta: UShort,
    val stack: VerificationTypeInfo
) : StackMapFrame()

/**
 * chop_frame
 * Frame type: 248-250
 * Frame where current locals are the same as locals in previous frame,
 * except that the k last locals are absent (k = 251 - frame_type)
 */
data class ChopFrame(
    override val frameType: UByte,  // 248-250
    val offsetDelta: UShort
) : StackMapFrame()

/**
 * same_frame_extended
 * Frame type: 251
 * Same as same_frame but with explicit offset_delta
 */
data class SameFrameExtended(
    override val frameType: UByte = 251u,
    val offsetDelta: UShort
) : StackMapFrame()

/**
 * append_frame
 * Frame type: 252-254
 * Frame where current locals are the same as locals in previous frame,
 * except that k additional locals are defined (k = frame_type - 251)
 */
data class AppendFrame(
    override val frameType: UByte,  // 252-254
    val offsetDelta: UShort,
    val locals: List<VerificationTypeInfo>  // Size is frame_type - 251
) : StackMapFrame()

/**
 * full_frame
 * Frame type: 255
 * Full frame with complete specification of locals and stack
 */
data class FullFrame(
    override val frameType: UByte = 255u,
    val offsetDelta: UShort,
    val locals: List<VerificationTypeInfo>,
    val stack: List<VerificationTypeInfo>
) : StackMapFrame()

/**
 * Unknown stack map frame type
 * Used for future frame types not yet defined in this parser
 * Provides forward compatibility by preserving unrecognized frames
 */
data class UnknownStackMapFrame(
    override val frameType: UByte,
    val data: ByteArray  // Raw bytes of the frame (excluding frame type)
) : StackMapFrame() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UnknownStackMapFrame
        if (frameType != other.frameType) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = frameType.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

/**
 * Verification type information (JVM Spec 4.7.4)
 * Represents the type of a local variable or operand stack entry
 */
sealed class VerificationTypeInfo {
    abstract val tag: UByte
}

/**
 * Top_variable_info
 * Tag: 0
 * Indicates that the local variable has the verification type top
 */
data class TopVariableInfo(
    override val tag: UByte = 0u
) : VerificationTypeInfo()

/**
 * Integer_variable_info
 * Tag: 1
 * Indicates that the location contains the verification type int
 */
data class IntegerVariableInfo(
    override val tag: UByte = 1u
) : VerificationTypeInfo()

/**
 * Float_variable_info
 * Tag: 2
 * Indicates that the location contains the verification type float
 */
data class FloatVariableInfo(
    override val tag: UByte = 2u
) : VerificationTypeInfo()

/**
 * Double_variable_info
 * Tag: 3
 * Indicates that the location contains the verification type double
 */
data class DoubleVariableInfo(
    override val tag: UByte = 3u
) : VerificationTypeInfo()

/**
 * Long_variable_info
 * Tag: 4
 * Indicates that the location contains the verification type long
 */
data class LongVariableInfo(
    override val tag: UByte = 4u
) : VerificationTypeInfo()

/**
 * Null_variable_info
 * Tag: 5
 * Indicates that location contains the verification type null
 */
data class NullVariableInfo(
    override val tag: UByte = 5u
) : VerificationTypeInfo()

/**
 * UninitializedThis_variable_info
 * Tag: 6
 * Indicates that the location contains the verification type uninitializedThis
 */
data class UninitializedThisVariableInfo(
    override val tag: UByte = 6u
) : VerificationTypeInfo()

/**
 * Object_variable_info
 * Tag: 7
 * Indicates that the location contains an instance of the class represented by the constant pool entry
 */
data class ObjectVariableInfo(
    override val tag: UByte = 7u,
    val cpoolIndex: UShort  // Index to CONSTANT_Class
) : VerificationTypeInfo()

/**
 * Uninitialized_variable_info
 * Tag: 8
 * Indicates that the location contains the verification type uninitialized(Offset)
 * The offset indicates the offset of the new instruction that created the object
 */
data class UninitializedVariableInfo(
    override val tag: UByte = 8u,
    val offset: UShort
) : VerificationTypeInfo()

/**
 * Unknown verification type info
 * Used for future verification types not yet defined in this parser
 * Provides forward compatibility by preserving unrecognized types
 */
data class UnknownVerificationTypeInfo(
    override val tag: UByte,
    val data: ByteArray  // Raw bytes (excluding tag)
) : VerificationTypeInfo() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UnknownVerificationTypeInfo
        if (tag != other.tag) return false
        if (!data.contentEquals(other.data)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
