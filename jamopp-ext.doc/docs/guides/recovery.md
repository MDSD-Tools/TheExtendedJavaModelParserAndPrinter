---
sidebar_position: 5
sidebar_label: Recovery
---

# Recovering Missing Dependencies

While the [reference resolution variants](./resolution.md) provide means to resolve references, it is not always possible to resolve them. In particular, if dependencies or source code files are missing, no reference resolution variant is able to find referenced Java elements within these parts, resulting in *unresolvable references and proxy objects*. To support these cases, the extended JaMoPP provides the following recovery strategies for missing dependencies. They are also applicable to unresolved references and proxy objects, which could still be resolved.

## Trivial Recovery

The class `tools.mdsd.jamopp.recovery.trivial.TrivialRecovery` provides the first recovery strategy with the following constructor / method.

```java
public TrivialRecovery(ResourceSet set);

public void recover();
```

The `TrivialRecovery` object is initialized with a `ResourceSet`, containing all models for which the recovery will be executed. The method `recover` executes the actual recovery, in which all proxy objects are replaced by new, valid model elements. The recovery does neither perform any reference resolution nor delegate to a reference resolution variant.

The goal of the trivial recovery is to generate valid and complete models (i.e., models conforming to the metamodel without proxy objects) with little overhead.[^1] Thus, it does not consider any context, leading to inaccuracies in the models when compared to original Java source code. For every proxy object, a new element of the same type and with the same name is created as replacement (for completeness of the models). In addition, these elements are collected in an artificial class element and model (for validity of the models).

## Advanced Recovery Strategies

Two advanced recovery strategies are currently proposed without any implementation.[^2] As soon as they are implemented, their documentation will be added. Compared to the trivial recovery, the advanced recovery strategies consider the context of model elements for a more accurate recovery.

By [parsing](./parsing.md) Java source code, [resolving references](./resolution.md), and recovering missing dependencies, valid and complete Java models can be generated, which can be [printed](./printing.md) again to Java source code.

[^1]: Armbruster, Martin; Mazkatli, Manar; Koziolek, Anne (2023): Recovering Missing Dependencies in Java Models. Softwaretechnik-Trends Band 43, Heft 4. Gesellschaft für Informatik e.V.. ISSN: 0720-8928. [Online](https://dl.gi.de/items/173da504-68a4-4b9b-ac39-a4d67f5fd65c).
[^2]: Armbruster, Martin (2022-07-28): Parsing and Printing Java 7-15 by Extending an Existing Metamodel. Technical report. Karlsruhe Institute of Technology. [DOI](https://doi.org/10.5445/IR/1000149186).
