---
sidebar_position: 2
sidebar_label: Parsing
---

# Parsing

The class `tools.mdsd.jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser` is responsible to get Java models from source code. It includes the following methods:

```java
public JavaRoot parse(String fileName, InputStream input);

public Resource parseFile(Path file);

public ResourceSet parseDirectory(Path dir);
```

It is able to parse models from an `InputStream`, single file or directory. In all three cases, the source code potentially including dependencies is parsed with the Eclipse Java Development Tools (JDT) Core into ASTs at first. Then, the ASTs are converted to Java model instances.

If a directory is given for parsing, the `JaMoPPJDTSingleFileParser` scans the whole directory and sub-directories for Java, class, jar, and zip files. All found Java files are parsed while all class, jar, and zip files are considered as dependencies and registered in the [`JavaClasspath`](./classpath.md). For cases in which the directory contains files that should be not parsed or registered, the `JaMoPPJDTSingleFileParser` provides the method `setExclusionPatterns`. It accepts multiple Java regular expression patterns that are tested against the absolute paths of all found files in the directory to parse. If the absolute path matches any regular expression, the corresponding file is excluded from parsing.

## Relation to the Resolution

Java code contains references between identifiers and their defining elements (e.g., an import of a class references this class). During parsing, there are different possibilities to handle and resolve these references so that the resulting models contain the expected references. The supported reference resolution variants are covered in [the next guide](./resolution.md).

The parser also provides options to configure it. As some of them are related to the reference resolution, the parser options are covered [in another guide](./options.md).
