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
- Upgraded dependency versions to:
    - Apache Commons Bytecode Engineering Library 6.7.0
	- Apache Log4j 2 2.20.0 including the Log4j 1.x bridge

### Deprecated

### Removed

### Fixed

### Security

[Unreleased]: https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter/compare/releases/5.1.0...HEAD