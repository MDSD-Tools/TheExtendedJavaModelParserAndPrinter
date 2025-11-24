---
sidebar_position: 1
sidebar_label: Setup
---

# Setup

To setup your system / IDEs for developing the extended JaMoPP, you can follow these instructions.

## Eclipse IDE (for Code)

Currently, only the Eclipse IDE is supported for developing the extended JaMoPP.

1. The code requires Java 17 and the Eclipse Modeling Tools 2022-12 with the Maven plugin (needs manual installation) since the project is organized with pure Maven modules.

   Hint: You can install the Maven plugin by opening the software installation panel (menu "Help" -> "Install New Software"). Then, you can show a list of update sites for the field "Work with:". From this list, select "2022-12 - https://download.eclipse.org/releases/2022-12", search for "Maven", and select "M2E - Maven Integration for Eclipse" for installation. Afterward, proceed with the installation.
1. After Eclipse is prepared, the Maven modules of the extended JaMoPP can be easily imported from the top-level directory into Eclipse. However, the source code for the Java metamodel is not generated yet.
1. There are two possibilities to generate the source code for the metamodel.

   a. Run the build pipeline locally by executing the command `./mvnw clean package` (Linux) or `.\mvnw.cmd clean package` (Windows).
   
   b. In Eclipse, open the file `jamopp.model/src/main/resources/metamodel/java.genmodel`, and execute `Generate Model` (available on right click on "Java" if the EMF Generator editor is opened and only showing a tree view).

## For Documentation

The raw content of this documentation is located in the top-level directory `jamopp.doc`. It uses [Docusaurus](https://docusaurus.io/) as documentation framework.

1. Thus, the documentation requires Node.js 18.16.0 or higher installed on your system.
1. If it is installed, you can run `npm i` in the documentation directory to install the necessary dependencies.
1. Then, `npm start` opens the generated documentation in the browser (defaults to `http://localhost:3000`), which is automatically updated on changes in the documentation files.
