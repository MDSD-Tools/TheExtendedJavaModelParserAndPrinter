# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html) beginning with version [Unreleased].

## [Unreleased]

### Added

- Trivial recovery strategy to generate model elements for unresolved proxy objects
- Parser: `TextBlock`s are converted to `TextBockReference`s so that model elements are generated for text blocks

### Changed

- Minimum required Java version: Java 17
- Supported Eclipse version: 2022-12
- Third variant: bindings of parameters and local variables are also resolved
- Migration to MDSD Tools organization
    - Converted Maven Tycho-based build to pure Maven build
    - Unification and renaming of packages to: `tools.mdsd.jamopp`
    - Renaming of meta-model namespace URI to: `https://mdsd.tools/jamopp/java`
- Upgraded dependency versions to:
    - Apache Commons Bytecode Engineering Library 6.7.0
	- Apache Log4j 2 2.20.0 including the Log4j 1.x bridge

### Deprecated

### Removed

### Fixed

### Security

## [5.1.0] - 2022-01-21

From here, independent development by the Modelling for Continuous Software Engineering (MCSE) group at the Institute of Information Security and Dependability (KASTEL) at the Karlsruher Institute of Technology (KIT) resulting in the following changes.

### Added

- Meta-model support for features of Java 7-15:
    - Java 7
        - Diamond in class instance creation expressions
        - `try`-with-resources statements
        - Multi-`catch`
        - Binary integer literals
    - Java 8
        - Lambda expressions
        - Method reference expressions
        - Receiver parameters
        - Default interface methods
        - Further positions of annotations
    - Java 9
        - Modules
    - Java 10/11
        - `var` as local variable type
    - Java 14
        - Switch expressions
    - Java 15
        - Text blocks
- Meta-model extensions:
    - `origin` field in the `JavaRoot` to represent the source (source file, class file, file from an archive, binding) of a Java model
    - `Reference`s include an optional list of `TypeReference`s to store concrete types and type arguments in contexts in which the types are usually inferred
- Manual printer implementation
- Parser implementation based on Eclipse JDT which converts the AST to a model
- First variant for the reference resolution: Eclipse JDT bindings are used to directly set references
- Third variant for the reference resolution (as combination of first and second one): proxy objects which are set for references can be resolved after the parsing (based on the Eclipse JDT bindings) or on demand

### Changed

- Minimum required Java version: Java 11
- Supported Eclipse version: 2021-12
- Update to Apache Commons Byte Code Engineering Library 6.5.0
- Meta-model: `TypeReference` inherits from `ArrayTypeable` so that a type can store information about array dimensions and if it is an array
- Turned the extended reference resolution mechanism from the original version into the second variant for the reference resolution: proxy objects which are set for references are resolved on demand
- Standalone version is based on the first variant

### Removed

- Java syntax definition in CS specification language and the generated parser and printer

### Known Issues

- First variant: ends in `StackOverflowException` because of an endless loop, also affects standalone version

## Original JaMoPP Version - before 2019

Originally, developed by DevBoost GmbH and Software Technology Group, Dresden University of Technology. It consists of the development up to version [1.4.0] and the [development after 1.4.0] resulting in the following features.

- Ecore-based meta-model for the Java programming language covering the syntax up to and including Java 6
- Definition of Java syntax in CS specification language of EMFText to generate a parser (Java source code -> Java model) and printer (Java model -> Java source code)
- Loader for class files which generates Java models from class files (based on Apache Commons Byte Code Engineering Library 6.2.0)
- Extended reference resolution mechanisms of generated EMFText code to connect Java models by language-specific links
- Integration into the Eclipse IDE
- Tests for the parsing (checks if generated model contains the expected elements)
- Tests for the parsing and printing (checks if printed code is equal to parsed code by parsing both source codes with Eclipse JDT and comparing the resulting ASTs)

[Unreleased]: https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter/compare/releases/5.1.0...HEAD
[5.1.0]: https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter/compare/8bc07...releases/5.1.0
[development after 1.4.0]: https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter/compare/e46b0...8bc07
[1.4.0]: https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter/commit/e46b0003803a8ccda7c3aa380ff2c759937d1ccb
