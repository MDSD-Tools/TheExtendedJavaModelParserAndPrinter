/**
 * Copyright (c) 2020-2024
 * Modelling for Continuous Software Engineering (MCSE) group,
 *     Institute of Information Security and Dependability (KASTEL),
 *     Karlsruhe Institute of Technology (KIT).
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   MCSE, KASTEL, KIT
 *      - Initial implementation
 */

package tools.mdsd.jamopp.standalone;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import tools.mdsd.jamopp.model.java.containers.CompilationUnit;
import tools.mdsd.jamopp.model.java.containers.JavaRoot;
import tools.mdsd.jamopp.model.java.containers.Package;
import tools.mdsd.jamopp.model.java.containers.Module;
import tools.mdsd.jamopp.options.ParserOptions;
import tools.mdsd.jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser;
import tools.mdsd.jamopp.recovery.trivial.TrivialRecovery;
import tools.mdsd.jamopp.resource.JavaResource2Factory;

/**
 * Class for the stand alone / CLI usage of the extended JaMoPP. 
 * 
 * How to use:
 * - Input any URI (absolute or relative file path/ Directory/ Archive) via INPUT 
 * - If you want to output any xmi library files define ENABLE_OUTPUT_OF_LIBRARY_FILES as true 
 * - The xmi output will be generated and saved in ./standalone_output including its package hierarchy
 * 
 * If you have Problems opening the .xmi file with the Ecore Model Editor make sure you installed the Standalone version as an Ecplise Plugin 
 */
public class JaMoPPStandalone {
    public static void main(String[] args) {
    	if (args.length == 1 && args[0].equals("help")) {
    		System.out.println("""
Extended JaMoPP CLI - How to Use:
help - Displays this help message.
<input> <output> [one] - Parses all Java files within the given <input> directory and outputs the model(s) into the <output> directory as XMI file(s).
                         For every Java file, one model is created, which is complete: no proxy objects remain.
                         If the optional flag [one] is given, all Java models are put into one XMI file. Otherwise, every model is output as one XMI file.
                         In this case, the CLI creates a directory structure within the <output> directory which conforms to the Java package structure.
""");
    		return;
    	}
    	if (args.length != 2 && args.length != 3) {
    		System.err.println("Did not receive enough arguments. Exiting...");
    		return;
    	}
    	
    	String inputDirectory = args[0];
    	Path input = Paths.get(inputDirectory);
    	String outputDirectory = args[1];
    	Path output = Paths.get(outputDirectory);
    	boolean hasOneFlag = args.length != 2 && args[2].equals("one");
    	
    	if (Files.notExists(input)) {
    		System.err.format("The given input directory '%s' does not exist. Exiting...", inputDirectory);
    		return;
    	}
    	if (!Files.isDirectory(input)) {
    		System.err.format("The given input path '%s' is not a directory. Exiting...", inputDirectory);
    		return;
    	}
    	if (Files.notExists(output)) {
    		try {
    			Files.createDirectories(output);
    		} catch (IOException ioException) {
    			System.err.format("Could not create output directory because: %s", ioException.getMessage());
    			return;
    		}
    	} else if (Files.exists(output) && !Files.isDirectory(output)) {
    		System.err.format("The output directory %s exists, but is not a directory. Exiting...", outputDirectory);
    		return;
    	}
    	
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("java", new JavaResource2Factory());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		 
		ParserOptions.CREATE_LAYOUT_INFORMATION.setValue(Boolean.TRUE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.TRUE);
		ParserOptions.REGISTER_LOCAL.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.FALSE);
		
		System.out.format("---%nExtended JaMoPP CLI%n---%n%nWelcome. Starting parsing...%n");
		JaMoPPJDTSingleFileParser parser = new JaMoPPJDTSingleFileParser();
		long parsingTime = System.currentTimeMillis();
		ResourceSet rs = parser.parseDirectory(input);
		new TrivialRecovery(rs).recover();
		parsingTime = System.currentTimeMillis() - parsingTime;
		System.out.println("Finished parsing. Outputting the models...");

		long outputTime = System.currentTimeMillis();
		if (hasOneFlag) {
			var oneResourceSet = new ResourceSetImpl();
			var oneResource = oneResourceSet.createResource(
				URI.createFileURI(
					output.resolve("OneJavaModel.xmi").toAbsolutePath().toString()
				)
			);
			for (Resource javaResource : rs.getResources()) {
				oneResource.getContents().addAll(javaResource.getContents());
			}
			try {
				oneResource.save(null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			ResourceSet xmiSet = new ResourceSetImpl();
			for (Resource javaResource : rs.getResources()) {
				if (javaResource.getContents().isEmpty()) {
					System.out.println("Skipping emtpy resource: " + javaResource.getURI());
					continue;
				}
	
				Path outputFile = output;
				JavaRoot root = (JavaRoot) javaResource.getContents().get(0);
				for (var nSpace : root.getNamespaces()) {
					outputFile = outputFile.resolve(nSpace);
				}
				if (root instanceof CompilationUnit cu) {
					if (cu.getClassifiers().size() > 0) {
						outputFile = outputFile.resolve(cu.getClassifiers().get(0).getName() + ".xmi");
					} else {
						System.out.format("Skipping empty compilation unit: %s", cu.getNamespacesAsString());
						continue;
					}
				} else if (root instanceof Package) {
					outputFile = outputFile.resolve("packag-info.xmi");
				} else if (root instanceof Module) {
					outputFile = outputFile.resolve("module-info.xmi");
				}
				
				URI xmiFileURI = URI.createFileURI(outputFile.toAbsolutePath().toString());	
				XMIResource xmiResource = (XMIResource) xmiSet.createResource(xmiFileURI);
				xmiResource.setEncoding(StandardCharsets.UTF_8.toString());
				xmiResource.getContents().addAll(javaResource.getContents());
			}
			
			for (Resource xmiResource : xmiSet.getResources()) {
				try {
					xmiResource.save(null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		outputTime = System.currentTimeMillis() - outputTime;
		
		System.out.println("Finished outputting.");
		System.out.format("Parsing took %d ms, and outputting took %d ms.%n", parsingTime, outputTime);
		System.out.println("Thank you for choosing the extended JaMoPP. Good bye.");
	}
}