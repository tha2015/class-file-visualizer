package parser

import api.parse
import api.withClassFile
import data.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test parsing a real HelloWorld.class file
 */
class HelloWorldTest {

    @Test
    fun testParseHelloWorldClass() {
        // Read the compiled HelloWorld.class file from test resources
        val classBytes = readClassFile("HelloWorld.class")

        // Parse it using the public API
        val classFile = parse(classBytes)

        // Verify magic number
        assertEquals(0xCAFEBABE.toUInt(), classFile.magic, "Magic number should be 0xCAFEBABE")

        // Verify it's a public class
        assertTrue(classFile.accessFlags.hasFlag(ClassAccessFlags.ACC_PUBLIC), "Should be public")

        withClassFile(classFile) {
            // Verify class name - much cleaner now!
            assertEquals("HelloWorld", classFile.thisClass().name(), "Class name should be HelloWorld")

            // Verify super class is java/lang/Object
            assertEquals("java/lang/Object", classFile.superClass().name(), "Super class should be java/lang/Object")

            // Verify interfaces (if any)
            val interfaces = classFile.interfaces()
            assertTrue(interfaces.isEmpty(), "HelloWorld implements no interfaces")

            // Verify it has methods (at least <init> and main)
            assertTrue(classFile.methods.size >= 2, "Should have at least 2 methods (constructor and main)")

            // Find the main method
            val mainMethod = classFile.methods.find { it.name() == "main" }
            assertNotNull(mainMethod, "Should have a main method")

            // Verify main method descriptor
            assertEquals("([Ljava/lang/String;)V", mainMethod.descriptor(),
                "Main method should have correct descriptor")

            // Verify main method is public static
            assertTrue(mainMethod.accessFlags.hasFlag(MethodAccessFlags.ACC_PUBLIC), "Main should be public")
            assertTrue(mainMethod.accessFlags.hasFlag(MethodAccessFlags.ACC_STATIC), "Main should be static")

            // Verify main method has Code attribute - cleaner API!
            val codeAttribute = mainMethod.code()
            assertNotNull(codeAttribute, "Main method should have Code attribute")
            assertTrue(codeAttribute.code.isNotEmpty(), "Main method should have bytecode")

            // Verify constructor exists - cleaner API!
            val constructors = classFile.methods.filter { it.isConstructor() }
            assertTrue(constructors.isNotEmpty(), "Should have a constructor")

            println("✅ Successfully parsed HelloWorld.class!")
            println("   Class version: ${classFile.majorVersion}.${classFile.minorVersion}")
            println("   Class name: ${classFile.thisClass().name()}")
            println("   Super class: ${classFile.superClass().name()}")
            println("   Source file: ${classFile.sourceFile() ?: "N/A"}")
            println("   Constant pool size: ${classFile.constantPool.size}")
            println("   Fields: ${classFile.fields.size}")
            println("   Methods: ${classFile.methods.size}")
            println("   Main method bytecode length: ${codeAttribute.code.size} bytes")
        }
    }

    /**
     * Helper function to read class file bytes from test resources (JVM-only)
     */
    private fun readClassFile(resourceName: String): ByteArray {
        val resourceStream = this::class.java.classLoader.getResourceAsStream(resourceName)
            ?: throw IllegalStateException("Could not find resource: $resourceName")
        return resourceStream.readBytes()
    }
}
