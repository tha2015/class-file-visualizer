# Java Class File Visualizer

An educational tool for exploring the internal structure of Java `.class` files. Upload any compiled Java class file and see its anatomy in an interactive, hierarchical format.

[🚀 Live Demo](https://tha2015.github.io/class-file-visualizer/)

## 🎯 Purpose

This tool helps software engineers, JVM enthusiasts, and students understand the binary structure of Java class files by visualizing:

- **Class metadata**: Version, access flags, class/superclass names
- **Constant pool**: All 17 constant types with automatic dereferencing
- **Fields and methods**: With access flags decoded (PUBLIC, STATIC, etc.)
- **Bytecode**: Hexadecimal representation of method code
- **Attributes**: 30+ attribute types including LineNumberTable, LocalVariableTable, BootstrapMethods, and more

## ✨ Features

- **🔍 Complete parsing**: Supports the full JVM class file format (Java SE 21+)
- **🔗 Automatic dereferencing**: Constant pool references are resolved inline (e.g., `nameIndex_deref`)
- **📚 Educational annotations**: Access flags decoded as human-readable strings (e.g., `"33 (PUBLIC | SUPER)"`)
- **📊 Interactive JSON viewer**: Collapse/expand nodes to explore specific areas
- **🌐 Client-side only**: No server uploads - all processing happens in your browser
- **🛡️ HTML escaping**: Safely handles special characters like `<init>` method names

## 🚀 Usage

### Web Interface

1. Visit the [live demo](https://tha2015.github.io/class-file-visualizer/)
2. Click "Choose File" and select a `.class` file
3. Explore the class file structure in the interactive JSON viewer

### As a Library

```kotlin
import api.parse
import serialization.toJson
import kotlinx.serialization.json.Json

// Parse a class file
val classFile = parse(classFileBytes)

// Convert to JSON
val jsonObject = classFile.toJson()
val jsonString = Json.encodeToString(jsonObject)
```

## 📖 What You'll Learn

By using this tool, you'll understand:

- How Java compiles classes into bytecode
- The structure of the constant pool and how it optimizes storage
- How access flags work (public, private, static, final, etc.)
- Method descriptors and type signatures
- Debug information (line numbers, local variables)
- Modern Java features (lambdas via BootstrapMethods, sealed classes, records, modules)

## 🏗️ Architecture

- **Kotlin Multiplatform**: Shared parsing logic for JVM and JavaScript
- **kotlinx-io**: Efficient binary parsing
- **kotlinx-serialization-json**: Clean JSON generation with DSL
- **Web Components**: Interactive JSON viewer (andypf-json-viewer)

## 🧪 Example Output

```json
{
  "magic": "0xCAFEBABE",
  "majorVersion": 61,
  "minorVersion": 0,
  "accessFlags": "33 (PUBLIC | SUPER)",
  "thisClass": 2,
  "thisClass_deref": {
    "tag": "CONSTANT_Class",
    "nameIndex": 3,
    "nameIndex_deref": {
      "tag": "CONSTANT_Utf8",
      "value": "HelloWorld"
    }
  },
  "methods": [
    {
      "accessFlags": "9 (PUBLIC | STATIC)",
      "nameIndex_deref": {
        "tag": "CONSTANT_Utf8",
        "value": "main"
      },
      "descriptorIndex_deref": {
        "tag": "CONSTANT_Utf8",
        "value": "([Ljava/lang/String;)V"
      }
    }
  ]
}
```

## 🔬 Building from Source

```bash
# Build the JavaScript bundle
./gradlew :frontend:jsBrowserProductionWebpack

# Run development server
./gradlew :frontend:jsBrowserDevelopmentRun

# Run tests
./gradlew test
```

## 📚 References

- [JVM Specification - Chapter 4: The class File Format](https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html)
- [Java Class File Format](https://en.wikipedia.org/wiki/Java_class_file)

## 📄 License

Apache License 2.0 - See LICENSE file for details

## 🤝 Contributing

Contributions are welcome! Feel free to open issues or submit pull requests.
