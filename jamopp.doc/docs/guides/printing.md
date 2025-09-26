---
sidebar_position: 6
sidebar_label: Printing
---

# Printing

With the class `tools.mdsd.jamopp.printer.JaMoPPPrinter`, the extended JaMoPP contains a printer for outputting Java models as source code. It offers the following methods.

```java
void print(JavaRoot root, OutputStream output);

void print(Commentable element, OutputStream output);

void print(JavaRoot root, Path file);
```

The first method prints the source code for the given root element into the `OutputStream`. Similarly, the second method prints the source code for an arbitrary model element into an `OutputStream`. At last, the third method prints the source code for a root element into a file, given by a `Path`. The printed source code is not pretty printed.

:::caution
The printer assumes that the given models are valid.
:::
