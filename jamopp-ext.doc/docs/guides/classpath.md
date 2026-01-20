---
sidebar_position: 1
sidebar_label: Classpath
---

# The `JavaClasspath`

As outlined in later guides, the extended JaMoPP can load additional files after parsing the actual code. In this case, the physical locations of the files are required, but not known. Therefore, the class `tools.mdsd.jamopp.model.java.JavaClasspath` provides means to handle the physical location of a file (in the form of an [URI](https://www.rfc-editor.org/rfc/rfc3986.html)) by storing a mapping of a logical URI to the physical one.

## Registering Elements

There are several methods to register files and model elements in the `JavaClasspath` with their physical URI, for example:

```java
public void registerJavaRoot(JavaRoot root, URI physicalURI);

public void registerClassifier(CompilationUnit compilationUnit, URI uri);

public void registerZip(URI zipURI);

public void registerStdLib();
```

The URI mapping is stored for modules, packages, classifiers, and inner classifiers. As a consequence, the second method establishes the mapping for all classifiers within the given compilation unit. This is usually done by the parser during the parsing process. In contrast, the third method enables the registration of code in (possibly external) zip or jar files (e.g., in the case of dependencies).

Moreover, the Java standard library can be registered with the fourth method as it is usually not part of the parsed code. When registering the Java standard library, the `JavaClasspath` looks up the `java.home` environment variable to find the source code of the standard library becauseof changes in Java 9. With the introduction of modules in Java 9, the format of the compiled Java standard library binaries became [implementation-dependent](https://openjdk.org/jeps/220) so that the extended JaMoPP does not consider them (compared to the original JaMoPP) and uses the source code instead.

## Logical URIs

While the concrete physical URI and format depends on the file location, the logical URI is built according to these patterns:

* For modules: `pathmap:/javamodule/`&lt;`module name`&gt;`/module-info.java`
* For packages: `pathmap:/javapackage/`&lt;`package name`&gt;`/package-info.java`
* For classifiers: `pathmap:/javaclass/`&lt;`full qualified classifier name`&gt;`.java`

## Local Registrations

The `JavaClasspath` provides a global instance and 'local' ones for `ResourceSet`s:

```java
public static JavaClasspath get();

public static JavaClasspath get(EObject obj);

public static JavaClasspath get(Resource resource);

public static JavaClasspath get(ResourceSet set);
```

The first method returns always the global instance. The other ones return the global instance if the given object is `null`. Otherwise, the second and third method delegate to the fourth method which returns the local instance specific for the given `ResourceSet`.

The differentiation between global and local instances allow the global and local registration of URI mappings. As example, if a project should be parsed in two different versions, their models can live in two different `ResourceSet`s at the same time. Additionally, the URI mappings for the dependencies can be set speficially for the different versions so that no conflicts arise if they have differing dependencies or dependency versions. However, the Java standard library or other common dependencies can be put into the global instance to re-use the mapping information.

## Getting Elements

:::info
The following paragraphs describe internally used methods. However, they are part of the `JavaClasspath`.
:::

The `JavaClasspath` also supports the retrieval of classifiers by providing methods to get [proxy objects](./resolution.md#emf-proxy-objects) for `ConcreteClassifier`s. Internally, the `JavaClasspath` maintains a mapping between potential classifier names and package names in which the classifiers are potentially located. Because `$` is a valid character in classifier names (cf., [Chapter 3.8 in the JLS 21](https://docs.oracle.com/javase/specs/jls/se21/jls21.pdf)) and used as separator between outer and inner classes in class file names (cf., [Chapter 13.1 in the JLS 21](https://docs.oracle.com/javase/specs/jls/se21/jls21.pdf)), the extended JaMoPP contains special handling for `$` which leads to a sometimes inaccurate mapping. From this mapping, potential full qualified classifier names are calculated and turned into proxy objects which can be used to look for the actual classifiers.
