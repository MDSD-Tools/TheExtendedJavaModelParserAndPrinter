---
sidebar_position: 4
sidebar_label: Publications
---

# Publications

On this page, we provide an overview of publications about and related to the (extended) JaMoPP.

## Original Authors

The following publications were written by the original authors of JaMoPP.

* Heidenreich, F., Johannes, J., Seifert, M., Wende, C. (2010). Closing the Gap between Modelling and Java. In: van den Brand, M., Gašević, D., Gray, J. (eds) Software Language Engineering. SLE 2009. Lecture Notes in Computer Science, vol 5969. Springer, Berlin, Heidelberg. [DOI](https://doi.org/10.1007/978-3-642-12107-4_25) and [author's version on GitHub](https://github.com/DevBoost/JaMoPP/blob/master/Doc/org.emftext.language.java.doc/publications/2009_SLE_JaMoPP.pdf).

   In this paper, the authors describe the concept behind JaMoPP to close the gap between modeling and programming languages. Thus, they provide an explicit Java metamodel, automatically generated parser and printer, and a reference resolution mechanism.
* Florian Heidenreich, Jendrik Johannes, Mirko Seifert, and Christian Wende (2009-09-10): JaMoPP: The Java Model Parser and Printer. Technical report. Technische Universität Dresden, Fakultät Informatik. [Author's version on GitHub](https://github.com/DevBoost/JaMoPP/blob/master/Doc/org.emftext.language.java.doc/publications/2009_JaMoPP_The_Java_Model_Parser_and_Printer.pdf).

   This technical report describes the concept of JaMoPP and how it can be applied in different examples.
* Florian Heidenreich, Jendrik Johannes, Mirko Seifert, and Christian Wende: Construct to Reconstruct – Reverse Engineering Java Code with JaMoPP. In: Proc. of the International Workshop on Reverse Engineering Models from Software Artifacts (R.E.M.’08), 2008. [Author's version on GitHub](https://github.com/DevBoost/JaMoPP/blob/master/Doc/org.emftext.language.java.doc/publications/2009_REM_JaMoPPReverse.pdf).

   This paper considers the application of JaMoPP in reverse engineering.
* Florian Heidenreich, Jendrik Johannes, Jan Reimann, Mirko Seifert, Christian Wende, Christian Werner, Claas Wilke, and Uwe Aßmann: Model-driven Modernisation of Java Programs with JaMoPP. In: Proc. of First International Workshop on Model-Driven Software Migration (MDSM’11), 2011. [Original publication](https://ceur-ws.org/Vol-708/) and [author's version on GitHub](https://github.com/DevBoost/JaMoPP/blob/master/Doc/org.emftext.language.java.doc/publications/2011_MDSM_JaMoPPModernise.pdf).

   In this paper, the authors apply JaMoPP to modernize Java code.

## Extended JaMoPP

The following publications were written by the authors of the extended JaMoPP, which is developed independently from the original JaMoPP and authors.

* Armbruster, Martin (2022-07-28): Parsing and Printing Java 7-15 by Extending an Existing Metamodel. Technical report. Karlsruhe Institute of Technology. [DOI](https://doi.org/10.5445/IR/1000149186).

   This technical report describes the first extensions to the original JaMoPP by introducing metamodel extensions, different [reference resolution variants](./guides/resolution.md), and manual parser and printer implementations.
* Armbruster, Martin; Mazkatli, Manar; Koziolek, Anne (2023): Recovering Missing Dependencies in Java Models. Softwaretechnik-Trends Band 43, Heft 4. Gesellschaft für Informatik e.V.. ISSN: 0720-8928. [Online](https://dl.gi.de/items/173da504-68a4-4b9b-ac39-a4d67f5fd65c).

   This paper introduces the [trivial recovery](./guides/recovery.md#trivial-recovery).

## Related Publications

The following publications are related to JaMoPP.

* Heidenreich, F., Johannes, J., Karol, S., Seifert, M., Wende, C. (2009). Derivation and Refinement of Textual Syntax for Models. In: Paige, R.F., Hartman, A., Rensink, A. (eds) Model Driven Architecture - Foundations and Applications. ECMDA-FA 2009. Lecture Notes in Computer Science, vol 5562. Springer, Berlin, Heidelberg. [DOI](https://doi.org/10.1007/978-3-642-02674-4_9) and [author's version on GitHub](https://github.com/DevBoost/EMFText/blob/master/Core/Doc/org.emftext.doc/publication/2009_ECMDA_EMFText.pdf).

   In this paper, the authors of the original JaMoPP version introduce EMFText, with which the parser and printer of the original JaMoPP was automatically generated.
* (2012-09-27): emftext USER GUIDE. [Online](https://github.com/DevBoost/EMFText/blob/master/Core/Doc/org.emftext.doc/pdf/EMFTextGuide.pdf).

   A user guide to EMFText, with which the parser and printer of the original JaMoPP was automatically generated.
* Armbruster, Martin (2024): Performance Factors of Proxy Objects in the Eclipse Modeling Framework. Softwaretechnik-Trends Band 44, Heft 4. Gesellschaft für Informatik e.V.. ISSN: 0720-8928. [Online](https://dl.gi.de/items/8c36b6ad-5687-45a4-81d5-a424843e6885).

   This paper describes proxy objects in the Eclipse Modeling Framework and investigates factors influencing the performance of proxy resolution.
* Mazkatli, M., Monschein, D., Armbruster, M. et al. Continuous integration of architectural performance models with parametric dependencies – the CIPM approach. Autom Softw Eng 32, 54 (2025). [DOI](https://doi.org/10.1007/s10515-025-00521-9).

   In this paper, the extended JaMoPP is applied within the Continuous Integration of architectural Performance Models (CIPM) approach, which keeps source code, architecture-level performance models, and runtime measurements of a software system consistent.

:::info
Did you use the (extended) JaMoPP in your publication, or do you know a publication, in which the (extended) JaMoPP is used? Then, let us know and open an [issue on GitHub](https://github.com/MDSD-Tools/TheExtendedJavaModelParserAndPrinter/issues). We are open to add further direct and related publications.
:::
