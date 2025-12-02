package serialization

import data.*
import kotlinx.serialization.json.*

/**
 * Escape HTML entities to prevent rendering issues in json-viewer
 */
private fun String.escapeHtml(): String = this
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
    .replace("'", "&#39;")

/**
 * Converts a ClassFile to JSON with inline dereferencing of constant pool entries.
 * Each u2 index field gets a corresponding *_deref field with the resolved entry.
 */
fun ClassFile.toJson(): JsonObject = buildJsonObject {
    put("magic", "0x${magic.toString(16).uppercase()}")
    put("minorVersion", minorVersion.toInt())
    put("majorVersion", majorVersion.toInt())

    // Constant Pool
    put("constantPoolCount", constantPool.size)
    put("constantPool", JsonArray(constantPool.mapIndexed { index, entry ->
        if (entry == null) JsonNull else serializeConstantPoolEntry(index, entry, constantPool)
    }))

    // Access Flags
    put("accessFlags", accessFlags.decodeClassAccessFlags())

    // This Class
    put("thisClass", thisClass.toInt())
    put("thisClass_deref", serializeConstantPoolEntry(thisClass.toInt(), constantPool[thisClass.toInt()], constantPool))

    // Super Class
    put("superClass", superClass.toInt())
    put("superClass_deref", serializeConstantPoolEntry(superClass.toInt(), constantPool[superClass.toInt()], constantPool))

    // Interfaces
    put("interfacesCount", interfaces.size)
    put("interfaces", JsonArray(interfaces.map { interfaceIndex ->
        buildJsonObject {
            put("index", interfaceIndex.toInt())
            put("index_deref", serializeConstantPoolEntry(interfaceIndex.toInt(), constantPool[interfaceIndex.toInt()], constantPool))
        }
    }))

    // Fields
    put("fieldsCount", fields.size)
    put("fields", JsonArray(fields.map { it.toJson(constantPool) }))

    // Methods
    put("methodsCount", methods.size)
    put("methods", JsonArray(methods.map { it.toJson(constantPool) }))

    // Attributes
    put("attributesCount", attributes.size)
    put("attributes", JsonArray(attributes.map { it.toJson(constantPool) }))
}

private fun serializeConstantPoolEntry(index: Int, entry: ConstantPoolEntry?, pool: List<ConstantPoolEntry?>): JsonElement {
    if (entry == null) return JsonNull

    return buildJsonObject {
        put("index", index)

        when (entry) {
            is ConstantUtf8 -> {
                put("tag", "CONSTANT_Utf8")
                put("value", entry.value.escapeHtml())
            }
            is ConstantInteger -> {
                put("tag", "CONSTANT_Integer")
                put("value", entry.value)
            }
            is ConstantFloat -> {
                put("tag", "CONSTANT_Float")
                put("value", entry.value)
            }
            is ConstantLong -> {
                put("tag", "CONSTANT_Long")
                put("value", entry.value)
            }
            is ConstantDouble -> {
                put("tag", "CONSTANT_Double")
                put("value", entry.value)
            }
            is ConstantClass -> {
                put("tag", "CONSTANT_Class")
                put("nameIndex", entry.nameIndex.toInt())
                put("nameIndex_deref", serializeConstantPoolEntry(entry.nameIndex.toInt(), pool[entry.nameIndex.toInt()], pool))
            }
            is ConstantString -> {
                put("tag", "CONSTANT_String")
                put("stringIndex", entry.stringIndex.toInt())
                put("stringIndex_deref", serializeConstantPoolEntry(entry.stringIndex.toInt(), pool[entry.stringIndex.toInt()], pool))
            }
            is ConstantFieldref -> {
                put("tag", "CONSTANT_Fieldref")
                put("classIndex", entry.classIndex.toInt())
                put("classIndex_deref", serializeConstantPoolEntry(entry.classIndex.toInt(), pool[entry.classIndex.toInt()], pool))
                put("nameAndTypeIndex", entry.nameAndTypeIndex.toInt())
                put("nameAndTypeIndex_deref", serializeConstantPoolEntry(entry.nameAndTypeIndex.toInt(), pool[entry.nameAndTypeIndex.toInt()], pool))
            }
            is ConstantMethodref -> {
                put("tag", "CONSTANT_Methodref")
                put("classIndex", entry.classIndex.toInt())
                put("classIndex_deref", serializeConstantPoolEntry(entry.classIndex.toInt(), pool[entry.classIndex.toInt()], pool))
                put("nameAndTypeIndex", entry.nameAndTypeIndex.toInt())
                put("nameAndTypeIndex_deref", serializeConstantPoolEntry(entry.nameAndTypeIndex.toInt(), pool[entry.nameAndTypeIndex.toInt()], pool))
            }
            is ConstantInterfaceMethodref -> {
                put("tag", "CONSTANT_InterfaceMethodref")
                put("classIndex", entry.classIndex.toInt())
                put("classIndex_deref", serializeConstantPoolEntry(entry.classIndex.toInt(), pool[entry.classIndex.toInt()], pool))
                put("nameAndTypeIndex", entry.nameAndTypeIndex.toInt())
                put("nameAndTypeIndex_deref", serializeConstantPoolEntry(entry.nameAndTypeIndex.toInt(), pool[entry.nameAndTypeIndex.toInt()], pool))
            }
            is ConstantNameAndType -> {
                put("tag", "CONSTANT_NameAndType")
                put("nameIndex", entry.nameIndex.toInt())
                put("nameIndex_deref", serializeConstantPoolEntry(entry.nameIndex.toInt(), pool[entry.nameIndex.toInt()], pool))
                put("descriptorIndex", entry.descriptorIndex.toInt())
                put("descriptorIndex_deref", serializeConstantPoolEntry(entry.descriptorIndex.toInt(), pool[entry.descriptorIndex.toInt()], pool))
            }
            is ConstantMethodHandle -> {
                put("tag", "CONSTANT_MethodHandle")
                put("referenceKind", entry.referenceKind.toInt())
                put("referenceIndex", entry.referenceIndex.toInt())
                put("referenceIndex_deref", serializeConstantPoolEntry(entry.referenceIndex.toInt(), pool[entry.referenceIndex.toInt()], pool))
            }
            is ConstantMethodType -> {
                put("tag", "CONSTANT_MethodType")
                put("descriptorIndex", entry.descriptorIndex.toInt())
                put("descriptorIndex_deref", serializeConstantPoolEntry(entry.descriptorIndex.toInt(), pool[entry.descriptorIndex.toInt()], pool))
            }
            is ConstantInvokeDynamic -> {
                put("tag", "CONSTANT_InvokeDynamic")
                put("bootstrapMethodAttrIndex", entry.bootstrapMethodAttrIndex.toInt())
                put("nameAndTypeIndex", entry.nameAndTypeIndex.toInt())
                put("nameAndTypeIndex_deref", serializeConstantPoolEntry(entry.nameAndTypeIndex.toInt(), pool[entry.nameAndTypeIndex.toInt()], pool))
            }
            is ConstantModule -> {
                put("tag", "CONSTANT_Module")
                put("nameIndex", entry.nameIndex.toInt())
                put("nameIndex_deref", serializeConstantPoolEntry(entry.nameIndex.toInt(), pool[entry.nameIndex.toInt()], pool))
            }
            is ConstantPackage -> {
                put("tag", "CONSTANT_Package")
                put("nameIndex", entry.nameIndex.toInt())
                put("nameIndex_deref", serializeConstantPoolEntry(entry.nameIndex.toInt(), pool[entry.nameIndex.toInt()], pool))
            }
            is ConstantDynamic -> {
                put("tag", "CONSTANT_Dynamic")
                put("bootstrapMethodAttrIndex", entry.bootstrapMethodAttrIndex.toInt())
                put("nameAndTypeIndex", entry.nameAndTypeIndex.toInt())
                put("nameAndTypeIndex_deref", serializeConstantPoolEntry(entry.nameAndTypeIndex.toInt(), pool[entry.nameAndTypeIndex.toInt()], pool))
            }
        }
    }
}

private fun FieldInfo.toJson(pool: List<ConstantPoolEntry?>): JsonObject = buildJsonObject {
    put("accessFlags", accessFlags.decodeFieldAccessFlags())
    put("nameIndex", nameIndex.toInt())
    put("nameIndex_deref", serializeConstantPoolEntry(nameIndex.toInt(), pool[nameIndex.toInt()], pool))
    put("descriptorIndex", descriptorIndex.toInt())
    put("descriptorIndex_deref", serializeConstantPoolEntry(descriptorIndex.toInt(), pool[descriptorIndex.toInt()], pool))
    put("attributesCount", attributes.size)
    put("attributes", JsonArray(attributes.map { it.toJson(pool) }))
}

private fun MethodInfo.toJson(pool: List<ConstantPoolEntry?>): JsonObject = buildJsonObject {
    put("accessFlags", accessFlags.decodeMethodAccessFlags())
    put("nameIndex", nameIndex.toInt())
    put("nameIndex_deref", serializeConstantPoolEntry(nameIndex.toInt(), pool[nameIndex.toInt()], pool))
    put("descriptorIndex", descriptorIndex.toInt())
    put("descriptorIndex_deref", serializeConstantPoolEntry(descriptorIndex.toInt(), pool[descriptorIndex.toInt()], pool))
    put("attributesCount", attributes.size)
    put("attributes", JsonArray(attributes.map { it.toJson(pool) }))
}

private fun AttributeInfo.toJson(pool: List<ConstantPoolEntry?>): JsonObject = buildJsonObject {
    put("attributeNameIndex", attributeNameIndex.toInt())
    put("attributeNameIndex_deref", serializeConstantPoolEntry(attributeNameIndex.toInt(), pool[attributeNameIndex.toInt()], pool))

    when (this@toJson) {
        is CodeAttribute -> {
            put("attributeLength", code.size + 12)
            put("maxStack", maxStack.toInt())
            put("maxLocals", maxLocals.toInt())
            put("codeLength", code.size)
            put("code", code.joinToString("") { byte ->
                val hex = (byte.toInt() and 0xFF).toString(16).uppercase()
                if (hex.length == 1) "0$hex" else hex
            })
            put("exceptionTableLength", exceptionTable.size)
            put("exceptionTable", JsonArray(emptyList()))
            put("attributesCount", attributes.size)
            put("attributes", JsonArray(attributes.map { it.toJson(pool) }))
        }
        is SourceFileAttribute -> {
            put("attributeLength", 2)
            put("sourceFileIndex", sourceFileIndex.toInt())
            put("sourceFileIndex_deref", serializeConstantPoolEntry(sourceFileIndex.toInt(), pool[sourceFileIndex.toInt()], pool))
        }
        is ConstantValueAttribute -> {
            put("attributeLength", 2)
            put("constantValueIndex", constantValueIndex.toInt())
            put("constantValueIndex_deref", serializeConstantPoolEntry(constantValueIndex.toInt(), pool[constantValueIndex.toInt()], pool))
        }
        is LineNumberTableAttribute -> {
            put("attributeLength", 2 + lineNumberTable.size * 4)
            put("lineNumberTableLength", lineNumberTable.size)
            put("lineNumberTable", JsonArray(lineNumberTable.map { entry ->
                buildJsonObject {
                    put("startPc", entry.startPc.toInt())
                    put("lineNumber", entry.lineNumber.toInt())
                }
            }))
        }
        is LocalVariableTableAttribute -> {
            put("attributeLength", 2 + localVariableTable.size * 10)
            put("localVariableTableLength", localVariableTable.size)
            put("localVariableTable", JsonArray(localVariableTable.map { entry ->
                buildJsonObject {
                    put("startPc", entry.startPc.toInt())
                    put("length", entry.length.toInt())
                    put("nameIndex", entry.nameIndex.toInt())
                    put("nameIndex_deref", serializeConstantPoolEntry(entry.nameIndex.toInt(), pool[entry.nameIndex.toInt()], pool))
                    put("descriptorIndex", entry.descriptorIndex.toInt())
                    put("descriptorIndex_deref", serializeConstantPoolEntry(entry.descriptorIndex.toInt(), pool[entry.descriptorIndex.toInt()], pool))
                    put("index", entry.index.toInt())
                }
            }))
        }
        is LocalVariableTypeTableAttribute -> {
            put("attributeLength", 2 + localVariableTypeTable.size * 10)
            put("localVariableTypeTableLength", localVariableTypeTable.size)
            put("localVariableTypeTable", JsonArray(localVariableTypeTable.map { entry ->
                buildJsonObject {
                    put("startPc", entry.startPc.toInt())
                    put("length", entry.length.toInt())
                    put("nameIndex", entry.nameIndex.toInt())
                    put("nameIndex_deref", serializeConstantPoolEntry(entry.nameIndex.toInt(), pool[entry.nameIndex.toInt()], pool))
                    put("signatureIndex", entry.signatureIndex.toInt())
                    put("signatureIndex_deref", serializeConstantPoolEntry(entry.signatureIndex.toInt(), pool[entry.signatureIndex.toInt()], pool))
                    put("index", entry.index.toInt())
                }
            }))
        }
        is ExceptionsAttribute -> {
            put("attributeLength", 2 + exceptionIndexTable.size * 2)
            put("numberOfExceptions", exceptionIndexTable.size)
            put("exceptionIndexTable", JsonArray(exceptionIndexTable.map { exceptionIndex ->
                buildJsonObject {
                    put("exceptionIndex", exceptionIndex.toInt())
                    put("exceptionIndex_deref", serializeConstantPoolEntry(exceptionIndex.toInt(), pool[exceptionIndex.toInt()], pool))
                }
            }))
        }
        is SignatureAttribute -> {
            put("attributeLength", 2)
            put("signatureIndex", signatureIndex.toInt())
            put("signatureIndex_deref", serializeConstantPoolEntry(signatureIndex.toInt(), pool[signatureIndex.toInt()], pool))
        }
        is DeprecatedAttribute -> {
            put("attributeLength", 0)
            put("deprecated", true)
        }
        is SyntheticAttribute -> {
            put("attributeLength", 0)
            put("synthetic", true)
        }
        is BootstrapMethodsAttribute -> {
            put("attributeLength", 2 + bootstrapMethods.sumOf { 4 + it.bootstrapArguments.size * 2 })
            put("numBootstrapMethods", bootstrapMethods.size)
            put("bootstrapMethods", JsonArray(bootstrapMethods.map { method ->
                buildJsonObject {
                    put("bootstrapMethodRef", method.bootstrapMethodRef.toInt())
                    put("bootstrapMethodRef_deref", serializeConstantPoolEntry(method.bootstrapMethodRef.toInt(), pool[method.bootstrapMethodRef.toInt()], pool))
                    put("numBootstrapArguments", method.bootstrapArguments.size)
                    put("bootstrapArguments", JsonArray(method.bootstrapArguments.map { argIndex ->
                        buildJsonObject {
                            put("argumentIndex", argIndex.toInt())
                            put("argumentIndex_deref", serializeConstantPoolEntry(argIndex.toInt(), pool[argIndex.toInt()], pool))
                        }
                    }))
                }
            }))
        }
        is InnerClassesAttribute -> {
            put("numberOfClasses", classes.size)
            put("classes", JsonArray(classes.map { info ->
                buildJsonObject {
                    put("innerClassInfoIndex", info.innerClassInfoIndex.toInt())
                    put("innerClassInfoIndex_deref", serializeConstantPoolEntry(info.innerClassInfoIndex.toInt(), pool[info.innerClassInfoIndex.toInt()], pool))
                    put("outerClassInfoIndex", info.outerClassInfoIndex.toInt())
                    put("outerClassInfoIndex_deref", serializeConstantPoolEntry(info.outerClassInfoIndex.toInt(), pool[info.outerClassInfoIndex.toInt()], pool))
                    put("innerNameIndex", info.innerNameIndex.toInt())
                    put("innerNameIndex_deref", serializeConstantPoolEntry(info.innerNameIndex.toInt(), pool[info.innerNameIndex.toInt()], pool))
                    put("innerClassAccessFlags", info.innerClassAccessFlags.decodeClassAccessFlags())
                }
            }))
        }
        is EnclosingMethodAttribute -> {
            put("attributeLength", 4)
            put("classIndex", classIndex.toInt())
            put("classIndex_deref", serializeConstantPoolEntry(classIndex.toInt(), pool[classIndex.toInt()], pool))
            put("methodIndex", methodIndex.toInt())
            put("methodIndex_deref", serializeConstantPoolEntry(methodIndex.toInt(), pool[methodIndex.toInt()], pool))
        }
        is SourceDebugExtensionAttribute -> {
            put("attributeLength", debugExtension.size)
            put("debugExtension", debugExtension.decodeToString().escapeHtml())
        }
        is MethodParametersAttribute -> {
            put("parametersCount", parameters.size)
            put("parameters", JsonArray(parameters.map { param ->
                buildJsonObject {
                    put("nameIndex", param.nameIndex.toInt())
                    put("nameIndex_deref", serializeConstantPoolEntry(param.nameIndex.toInt(), pool[param.nameIndex.toInt()], pool))
                    put("accessFlags", param.accessFlags.toInt())
                }
            }))
        }
        is NestHostAttribute -> {
            put("attributeLength", 2)
            put("hostClassIndex", hostClassIndex.toInt())
            put("hostClassIndex_deref", serializeConstantPoolEntry(hostClassIndex.toInt(), pool[hostClassIndex.toInt()], pool))
        }
        is NestMembersAttribute -> {
            put("numberOfClasses", classes.size)
            put("classes", JsonArray(classes.map { classIndex ->
                buildJsonObject {
                    put("classIndex", classIndex.toInt())
                    put("classIndex_deref", serializeConstantPoolEntry(classIndex.toInt(), pool[classIndex.toInt()], pool))
                }
            }))
        }
        is PermittedSubclassesAttribute -> {
            put("numberOfClasses", classes.size)
            put("classes", JsonArray(classes.map { classIndex ->
                buildJsonObject {
                    put("classIndex", classIndex.toInt())
                    put("classIndex_deref", serializeConstantPoolEntry(classIndex.toInt(), pool[classIndex.toInt()], pool))
                }
            }))
        }
        is ModuleMainClassAttribute -> {
            put("attributeLength", 2)
            put("mainClassIndex", mainClassIndex.toInt())
            put("mainClassIndex_deref", serializeConstantPoolEntry(mainClassIndex.toInt(), pool[mainClassIndex.toInt()], pool))
        }
        is ModulePackagesAttribute -> {
            put("packageCount", packageIndex.size)
            put("packageIndex", JsonArray(packageIndex.map { pkgIndex ->
                buildJsonObject {
                    put("packageIndex", pkgIndex.toInt())
                    put("packageIndex_deref", serializeConstantPoolEntry(pkgIndex.toInt(), pool[pkgIndex.toInt()], pool))
                }
            }))
        }
        is StackMapTableAttribute -> {
            put("numberOfEntries", entries.size)
            put("note", "StackMapTable entries are complex - showing count only")
            put("entries", JsonArray(entries.map { JsonPrimitive("StackMapFrame") }))
        }
        is StackMapAttribute -> {
            put("numberOfEntries", entries.size)
            put("note", "StackMap entries are complex - showing count only")
            put("entries", JsonArray(entries.map { JsonPrimitive("StackMapFrame") }))
        }
        is RuntimeVisibleAnnotationsAttribute -> {
            put("numAnnotations", annotations.size)
            put("note", "Annotations are complex structures - showing count only")
            put("annotations", JsonArray(annotations.map { JsonPrimitive("Annotation") }))
        }
        is RuntimeInvisibleAnnotationsAttribute -> {
            put("numAnnotations", annotations.size)
            put("note", "Annotations are complex structures - showing count only")
            put("annotations", JsonArray(annotations.map { JsonPrimitive("Annotation") }))
        }
        is RuntimeVisibleParameterAnnotationsAttribute -> {
            put("numParameters", parameterAnnotations.size)
            put("note", "Parameter annotations are complex structures - showing count only")
            put("parameterAnnotations", JsonArray(parameterAnnotations.map { JsonPrimitive("${it.size} annotations") }))
        }
        is RuntimeInvisibleParameterAnnotationsAttribute -> {
            put("numParameters", parameterAnnotations.size)
            put("note", "Parameter annotations are complex structures - showing count only")
            put("parameterAnnotations", JsonArray(parameterAnnotations.map { JsonPrimitive("${it.size} annotations") }))
        }
        is RuntimeVisibleTypeAnnotationsAttribute -> {
            put("numAnnotations", annotations.size)
            put("note", "Type annotations are complex structures - showing count only")
            put("annotations", JsonArray(annotations.map { JsonPrimitive("TypeAnnotation") }))
        }
        is RuntimeInvisibleTypeAnnotationsAttribute -> {
            put("numAnnotations", annotations.size)
            put("note", "Type annotations are complex structures - showing count only")
            put("annotations", JsonArray(annotations.map { JsonPrimitive("TypeAnnotation") }))
        }
        is AnnotationDefaultAttribute -> {
            put("note", "Annotation default values are complex structures - not fully serialized")
            put("hasDefaultValue", true)
        }
        is ModuleAttribute -> {
            put("moduleNameIndex", moduleNameIndex.toInt())
            put("moduleNameIndex_deref", serializeConstantPoolEntry(moduleNameIndex.toInt(), pool[moduleNameIndex.toInt()], pool))
            put("moduleFlags", moduleFlags.toInt())
            put("moduleVersionIndex", moduleVersionIndex.toInt())
            if (moduleVersionIndex.toInt() != 0) {
                put("moduleVersionIndex_deref", serializeConstantPoolEntry(moduleVersionIndex.toInt(), pool[moduleVersionIndex.toInt()], pool))
            }
            put("requiresCount", requires.size)
            put("exportsCount", exports.size)
            put("opensCount", opens.size)
            put("usesCount", uses.size)
            put("providesCount", provides.size)
            put("note", "Module details are complex - showing counts only")
        }
        is RecordAttribute -> {
            put("componentsCount", components.size)
            put("note", "Record components are complex - showing count only")
            put("components", JsonArray(components.map { JsonPrimitive("RecordComponent") }))
        }
        is UnknownAttribute -> {
            put("attributeLength", info.size)
            put("info", "Binary data (${info.size} bytes)")
        }
        else -> {
            // Fallback for any new attribute types
            put("attributeType", this@toJson::class.simpleName ?: "Unknown")
            put("note", "This attribute type is not yet fully serialized")
        }
    }
}
