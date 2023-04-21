/*******************************************************************************
 * Copyright (c) 2020, Martin Armbruster
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Armbruster
 *      - Initial implementation
 ******************************************************************************/

package jamopp.parser.jdt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.containers.JavaRoot;

import jamopp.parser.api.JaMoPPParserAPI;

public class JaMoPPJDTParser implements JaMoPPParserAPI {
	private final String DEFAULT_ENCODING = StandardCharsets.UTF_8.toString();
	private ResourceSet resourceSet;

	@Override
	public JavaRoot parse(String fileName, InputStream input) {
		this.setUpResourceSet();
		StringBuilder builder = new StringBuilder();
		String lineSep = System.getProperty("line.separator");
		try(InputStreamReader inReader = new InputStreamReader(input); BufferedReader buffReader = new BufferedReader(inReader)) {
			buffReader.lines().forEach(line -> builder.append(line + lineSep));
		} catch (IOException e) {
		}
		String src = builder.toString();
		ASTNode ast = parseFileWithJDT(src, fileName);
		OrdinaryCompilationUnitJDTASTVisitorAndConverter converter = new OrdinaryCompilationUnitJDTASTVisitorAndConverter();
		converter.setSource(src);
		ast.accept(converter);
		TypeInstructionSeparationUtility.convertAll();
		JDTResolverUtility.completeResolution();
		this.resourceSet = null;
		return converter.getConvertedElement();
	}
	
	private ASTNode parseFileWithJDT(String fileContent, String fileName) {
		ASTParser parser = setUpParser();
		parser.setUnitName(fileName);
		parser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
		parser.setSource(fileContent.toCharArray());
		return parser.createAST(null);
	}
	
	@Override
	public Resource parseFile(Path file) {
		this.setUpResourceSet();
		Resource result = this.parseFilesWithJDT(new String[] {}, new String[] { file.toAbsolutePath().toString() },
			new String[] { DEFAULT_ENCODING }).get(0).eResource();
		this.resourceSet = null;
		return result;
	}
	
	@Override
	public ResourceSet parseDirectory(Path dir) {
		this.setUpResourceSet();
		try {
			String[] sources = Files.walk(dir).filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith("java"))
				.map(Path::toAbsolutePath).map(Path::toString).toArray(i -> new String[i]);
			String[] encodings = new String[sources.length];
			for (int index = 0; index < encodings.length; index++) {
				encodings[index] = DEFAULT_ENCODING;
			}
			String[] classpathEntries = Files.walk(dir).filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith("jar"))
				.map(Path::toAbsolutePath).map(Path::toString).toArray(i -> new String[i]);
			this.parseFilesWithJDT(classpathEntries, sources, encodings);
		} catch (IOException e) {
		}
		ResourceSet result = this.resourceSet;
		this.resourceSet = null;
		return result;
	}
	
	private List<JavaRoot> parseFilesWithJDT(String[] classpathEntries, String[] sources, String[] encodings) {
		ArrayList<JavaRoot> result = new ArrayList<>();
		ASTParser parser = setUpParser();
		parser.setEnvironment(classpathEntries, new String[] {}, new String[] {}, true);
		OrdinaryCompilationUnitJDTASTVisitorAndConverter converter = new OrdinaryCompilationUnitJDTASTVisitorAndConverter();
		parser.createASTs(sources, encodings, new String[] {}, new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit node) {
				node.accept(converter);
				JavaRoot root = converter.getConvertedElement();
				Resource newResource;
				if (root.eResource() == null) {
					newResource = JaMoPPJDTParser.this.resourceSet.createResource(URI.createFileURI(sourceFilePath));
					newResource.getContents().add(root);
					JavaClasspath.get(resourceSet).registerJavaRoot(root, newResource.getURI());
				} else {
					newResource = root.eResource();
					if (!newResource.getURI().toFileString().equals(sourceFilePath)) {
						newResource.setURI(URI.createFileURI(sourceFilePath));
						JavaClasspath.get(resourceSet).registerJavaRoot(root, newResource.getURI());
					}
				}
				result.add(root);
			}
		}, null);
		TypeInstructionSeparationUtility.convertAll();
		JDTResolverUtility.completeResolution();
		for (Resource res : new ArrayList<>(this.resourceSet.getResources())) {
			if (res.getContents().isEmpty()) {
				try {
					res.delete(this.resourceSet.getLoadOptions());
				} catch (IOException e) {
				}
			}
		}
		return result;
	}
	
	private void setUpResourceSet() {
		if (this.resourceSet == null) {
			this.resourceSet = new ResourceSetImpl();
		}
		JDTResolverUtility.setResourceSet(this.resourceSet);
	}
	
	private ASTParser setUpParser() {
		ASTParser parser = ASTParser.newParser(AST.JLS14);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		Map<String, String> options = new HashMap<>();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_14);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_14);
		parser.setCompilerOptions(options);
		return parser;
	}

	@Override
	public void setResourceSet(ResourceSet set) {
		this.resourceSet = set;
	}
}
