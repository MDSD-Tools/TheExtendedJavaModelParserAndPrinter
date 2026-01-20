---
sidebar_position: 4
sidebar_label: Parser Options
---

# Configuring the Parsing with Parser Options

In the extended JaMoPP, references can be divided into the following categories:

* *Code references*, which connect identifiers and their related Java elements within the parsed source code.
* *Direct dependency references*, which directly connect identifiers within the parsed source code to Java elements in dependencies.
* *Transitive dependency references*, which concern references within dependencies and without identifiers in the parsed source code (i.e., they are indirectly referenced over direct dependecy references and thus transitive).

The extended JaMoPP provides several parser options in the enum `tools.mdsd.jamopp.options.ParserOptions` to configure the behavior of the parsing and reference resolution. These options are currently supported:

* `RESOLVE_BINDINGS`: Controls the [binding-based resolution](./resolution.md#third-variant). If the option is `true`, bindings are used to resolve references directly after the parsing and before returning the result, enabling the third reference resolution variant for code and direct dependency references. Defaults to `true`.
* `RESOLVE_BINDINGS_OF_INFERABLE_TYPES`: If this option is `true`, [bindings](./resolution.md#eclipse-jdt-bindings) for variables with `var` as type are utilized to resolve the variables' actual type. Defaults to `true`.
* `RESOLVE_ALL_BINDINGS`: If the [binding-based resolution](./resolution.md#third-variant) is executed by `RESOLVE_BINDINGS` set to `true` and this option is `true`, references in dependencies are transitively and recursively resolved with bindings. Defaults to `true`.
* `PREFER_BINDING_CONVERSION`: If the [binding-based resolution](./resolution.md#third-variant) is executed, and if the reference resolution encounters a reference for which both a binding and a mapping to a physical file is available, this option controls if the file is loaded to generate a model or the binding is converted to a model. If the option is `true`, bindings are converted without loading the file. Otherwise, the file is loaded. Defaults to `true`.
* `RESOLVE_EVERYTHING`: If this option is `true`, references without bindings are resolved during the [binding-based resolution](./resolution.md#third-variant) with the [second reference resolution variant](./resolution.md#second-variant) (before returning the models from parsing). Defaults to `false`.
* `REGISTER_LOCAL`: If this option is `true`, the mapping between logical and physical URIs in the [`JavaClasspath`](./classpath.md) are only stored for the `ResourceSet` of a corresponding model element. Defaults to `false`.
* `CREATE_LAYOUT_INFORMATION`: If this option is `true`, minimal layout information form the original parsed source code is stored for every model element during the parsing. If the option is not set or set to `false`, no layout information is created and preserved. Defaults to `true`.
