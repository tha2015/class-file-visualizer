package data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Dummy test to verify test infrastructure works
 */
class DummyTest {

    @Test
    fun testBasicAssertion() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testConstantPoolEntryCreation() {
        val entry = ConstantInteger(tag = 3u, value = 42)

        assertEquals(3u.toUByte(), entry.tag)
        assertEquals(42, entry.value)
    }

    @Test
    fun testAccessFlagUtility() {
        val publicFlag: UShort = 0x0001u

        assertTrue(publicFlag.hasFlag(ClassAccessFlags.ACC_PUBLIC))
    }

    @Test
    fun testClassFileStructure() {
        val classFile = ClassFile(
            magic = 0xCAFEBABE.toUInt(),
            minorVersion = 0u,
            majorVersion = 61u,
            constantPool = listOf(null),
            accessFlags = 0x0021u,
            thisClass = 1u,
            superClass = 0u,
            interfaces = emptyList(),
            fields = emptyList(),
            methods = emptyList(),
            attributes = emptyList()
        )

        assertEquals(0xCAFEBABE.toUInt(), classFile.magic)
        assertEquals(61u, classFile.majorVersion)
    }
}
