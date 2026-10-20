---
sidebar_position: 3
sidebar_label: Release Notes
---

# Release Notes

This document summarizes the most important changes for all releases. For further changes, we also provide a [CHANGELOG](https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter/blob/develop/CHANGELOG.md).

## Version 6.0.0 (Upcoming)

- Added the trivial recovery strategy to generate model elements for proxy objects
- Migrated to MDSD Tools organization:
    - Unified and renamed packages to `tools.mdsd.jamopp`
    - Renamed and versioned the meta-model namespace URIs to `https://mdsd.tools/jamopp/6.0.0/java`
    - Added a first user / developer documentation
- Set minimum required Java version to Java 17
- Supports Eclipse 2022-12
- Known Issue: first variant always returns an empty model (temporary fix for the `StackOverflowException`)

## Version 5.1

- Started independent development
- Extended meta-model to support Java 7-15
- Replaced generated parser and printer implementations by manual ones
- Added three variants for the reference resolution:
    1. References are directly set during parsing
    2. Extended resolution mechanism from the original version
    3. Combination of 1. and 2. by resolving some references during parsing
- Set minimum required Java version to Java 11
- Supports Eclipse 2021-12
- Known Issue: first variant ends in a `StackOverflowException`

## Original Version

- Added Ecore-based meta-model for the Java programming language covering the syntax up to and including Java 6
- Added automatically generated parser (Java code -> model) and printer (Model -> Java code) implementations
- Added loader for class files (Class file -> model)
- Added Java-specific reference resolution mechanism to connect Java models (e.g., by imports)
