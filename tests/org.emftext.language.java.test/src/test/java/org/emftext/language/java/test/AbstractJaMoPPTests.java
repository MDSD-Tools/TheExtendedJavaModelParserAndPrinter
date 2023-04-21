/*******************************************************************************
 * Copyright (c) 2006-2013
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 *   Martin Armbruster
 *      - Adaptation and extension for Java 7+
 ******************************************************************************/

package org.emftext.language.java.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.classifiers.Annotation;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.Enumeration;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.commons.NamedElement;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.containers.JavaRoot;
import org.emftext.language.java.generics.TypeParameter;
import org.emftext.language.java.members.Constructor;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.MemberContainer;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.modifiers.Public;
import org.emftext.language.java.types.NamespaceClassifierReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import jamopp.options.ParserOptions;
import jamopp.resource.JavaResource2;
import jamopp.resource.JavaResource2Factory;

/**
 * Abstract superclass that provides some frequently used assert and helper methods.
 */
public abstract class AbstractJaMoPPTests {

	protected static final String TEST_OUTPUT_FOLDER = "output";

	@BeforeAll
	public static void initLogging() {
		Logger logger = Logger.getLogger("jamopp");
		logger.setLevel(Level.ALL);
//		ConsoleAppender ca = new ConsoleAppender(new PatternLayout("[%d{DATE}] %-5p: %c - %m%n"),
//				ConsoleAppender.SYSTEM_OUT);
//		logger.addAppender(ca);
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.FALSE);
	}

	@BeforeEach
	public final void initResourceFactory() {
		this.createNewResourceSet();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("java", new JavaResource2Factory());
		JavaClasspath.get().clear();
		JavaClasspath.get().registerStdLib();
	}
	
	@AfterEach
	public void tearDown() {
		for (Resource res : new ArrayList<>(testSet.getResources())) {
			res.unload();
			testSet.getResources().remove(res);
		}
	}

	protected void registerInClassPath(String file) throws Exception {
		String inputFolderPath = "." + File.separator + getTestInputFolder();
		File inputFolder = new File(inputFolderPath);
		File inputFile = new File(file);

		CompilationUnit cu = (CompilationUnit) parseResource(inputFile.getPath());

		inputFile = new File(inputFolder + File.separator + file);
		JavaClasspath.get().registerClassifier(cu, URI.createFileURI(inputFile.getAbsolutePath().toString()));
	}

	protected JavaRoot parseResource(String filename, String inputFolderName) throws Exception {
		File inputFolder = new File("." + File.separator + inputFolderName);
		File file = new File(inputFolder, filename);
		assertTrue(file.exists(), "File " + file + " must exist.");

		return loadResource(file.getAbsolutePath());
	}

	protected JavaRoot parseResource(ZipFile file, ZipEntry entry) throws Exception {
		return loadResource(URI.createURI("archive:file:///" + new File(".").getAbsoluteFile().toURI().getRawPath() + file.getName().replaceAll("\\\\", "/") + "!/" + entry.getName()));
	}

	protected JavaRoot loadResource(String filePath) throws Exception {
		URI uri = URI.createFileURI(filePath);
		return loadResource(uri);
	}

	protected JavaRoot loadResource(URI uri) throws Exception {
		JavaResource2 resource = (JavaResource2) getResourceSet().createResource(uri);
		resource.load(getLoadOptions());
		
		assertNoErrors(uri.toString(), resource);
		assertNoWarnings(uri.toString(), resource);
		
		EList<EObject> contents = resource.getContents();
		assertEquals(1, contents.size(), "The resource must have one content element.");
		
		EObject content = contents.get(0);
		assertTrue(content instanceof JavaRoot, "File '" + uri.toString() + "' was parsed to JavaRoot.");
		JavaRoot root = (JavaRoot) content;
		return root;
	}
	
	protected void addFileToClasspath(File file, ResourceSet resourceSet) throws Exception {
		JavaClasspath cp = JavaClasspath.get();
		String fullName = file.getPath().substring(getTestInputFolder().length() + 3, file.getPath().length() - 5);
		fullName = fullName.replace(File.separator, ".");
		int idx = fullName.lastIndexOf(".");
		String packageName;
		String classifierName;
		if (idx == -1) {
			packageName = "";
			classifierName = fullName;
		} else {
			packageName = fullName.substring(0, idx);
			classifierName = fullName.substring(idx + 1);			
		}
		cp.registerClassifier(packageName, classifierName, URI.createFileURI(file.getAbsolutePath()));
	}

	protected Map<? extends Object, ? extends Object> getLoadOptions() {
		Map<Object, Object> map = new HashMap<Object, Object>();
		return map;
	}

	private void assertNoErrors(String fileIdentifier, JavaResource2 resource) {
		List<Diagnostic> errors = new BasicEList<Diagnostic>(resource.getErrors());
		printErrors(fileIdentifier, errors);
		assertTrue(errors.isEmpty(), "The resource should be parsed without errors.");
	}

	private void assertNoWarnings(String fileIdentifier, JavaResource2 resource) {
		List<Diagnostic> warnings = resource.getWarnings();
		printWarnings(fileIdentifier, warnings);
		assertTrue(warnings.isEmpty(), "The resource should be parsed without warnings.");
	}

	protected void printErrors(String filename, List<Diagnostic> errors) {
		printDiagnostics(filename, errors, "Errors");
	}

	protected void printWarnings(String filename, List<Diagnostic> warnings) {
		printDiagnostics(filename, warnings, "Warnings");
	}

	private void printDiagnostics(String filename, List<Diagnostic> errors, String diagnosticType) {
		if (errors.size() == 0) {
			return;
		}
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(diagnosticType + " while parsing resource '" + filename + "':\n");
		for (Diagnostic diagnostic : errors) {
			String text = diagnostic.getMessage();
			buffer.append("\t" + text + "\n");
		}
		System.out.println(buffer.toString());
	}

	protected void parseAndReprint(ZipFile file, ZipEntry entry, String outputFolderName, String libFolderName)
			throws Exception {
		
		String entryName = entry.getName();
		String outputFileName = "./" + outputFolderName + File.separator + entryName;
		File outputFile = prepareOutputFile(outputFileName);
		URI archiveURI = URI.createURI("archive:file:///" + new File(".").getAbsoluteFile().toURI().getRawPath() + file.getName().replaceAll("\\\\", "/") + "!/" + entry.getName());

		ResourceSet resourceSet = getResourceSet();

		if (prefixUsedInZipFile()) {
			String prefix = entry.getName().substring(0, entry.getName().indexOf("/") + 1);
			registerLibs(libFolderName, JavaClasspath.get(), prefix);
		}

		Resource resource = resourceSet.createResource(archiveURI);
		resource.load(getLoadOptions());

		if (!ignoreSemanticErrors(entry.getName())) {
			// This will not work if external resources are not yet registered (order of tests)
			assertResolveAllProxies(resource);
		}

		if (isExcludedFromReprintTest(entry.getName())) {
			return;
		}

		resource.setURI(URI.createFileURI(outputFile.getAbsolutePath()));
		resource.save(null);

		assertTrue(outputFile.exists(), "File " + outputFile.getAbsolutePath() + " must exist.");
		compareTextContents(file.getInputStream(entry), entryName.endsWith("module-info.java"),
			new FileInputStream(outputFile), outputFile.getPath().endsWith("module-info.java"));
	}

	protected boolean prefixUsedInZipFile() {
		return false;
	}

	public void registerLibs(String libdir, JavaClasspath classpath, String prefix) throws IOException, CoreException  {
		File libFolder = new File("." + File.separator + libdir);
		List<File> allLibFiles = collectAllFilesRecursive(libFolder, "jar");
		for (File libFile : allLibFiles) {
			String libPath = libFile.getAbsolutePath();
			URI libURI = URI.createFileURI(libPath);
			classpath.registerZip(libURI);
		}
	}

	protected void parseAndReprint(String filename, String inputFolderName, String outputFolderName) throws Exception {
		String path = "." + File.separator + inputFolderName + File.separator + filename;
		File file = new File(path);
		parseAndReprint(file, inputFolderName, outputFolderName);
	}
	
	protected void testReprint(ResourceSet set) throws Exception {
		for (Resource res : new ArrayList<>(set.getResources())) {
			if (res instanceof JavaResource2) {
				testReprint((JavaResource2) res);
			}
		}
	}
	
	protected void testReprint(JavaResource2 resource) throws Exception {
		testReprint(resource, TEST_OUTPUT_FOLDER);
	}
	
	protected void testReprint(JavaResource2 resource, String outputFolderName) throws Exception {
		String inputFile = resource.getURI().toFileString();
		if (inputFile == null) {
			return;
		}
		Path input = Paths.get(inputFile);
		Path localDir = Paths.get(".").toAbsolutePath();
		String outputFileName = calculateOutputFilename(input.toFile(), localDir.relativize(input).getName(0).toString(),
			outputFolderName);
		File outputFile = prepareOutputFile(outputFileName);
		
		assertNoErrors(resource.getURI().toString(), resource);
		
		if (!ignoreSemanticErrors(inputFile)) {
			// This will not work if external resources are not yet registered (order of tests)
			assertResolveAllProxies(resource);
			// Default EMF validation should not fail
			assertModelValid(resource);
		}
		
		if (isExcludedFromReprintTest(inputFile)) {
			return;
		}
		
		resource.setURI(URI.createFileURI(outputFileName));
		resource.save(null);
		
		assertTrue(outputFile.exists(), "File " + outputFile.getAbsolutePath() + " exists.");
		
		compareTextContents(new FileInputStream(inputFile), inputFile.endsWith("module-info.java"),
			new FileInputStream(outputFile), outputFile.getPath().endsWith("module-info.java"));
	}

	protected void parseAndReprint(File file, String inputFolderName, String outputFolderName) throws Exception {
		File inputFile = file;
		assertTrue(inputFile.exists(), "File " + inputFile.getAbsolutePath() + " exists.");
		
		Resource resource = getResourceSet().createResource(URI.createFileURI(inputFile.getAbsolutePath().toString()));
		resource.load(getLoadOptions());
		
		testReprint((JavaResource2) resource, outputFolderName);
	}

	protected abstract boolean isExcludedFromReprintTest(String filename);

	protected boolean ignoreSemanticErrors(String filename) {
		return false;
	}

	private boolean compareTextContents(InputStream inputStream, boolean isModule,
			InputStream inputStream2, boolean is2Module) throws MalformedTreeException,
			BadLocationException, IOException {
		
		org.eclipse.jdt.core.dom.CompilationUnit unit1 = parseWithJDT(inputStream, isModule);
		removeJavadoc(unit1);

		org.eclipse.jdt.core.dom.CompilationUnit unit2 = parseWithJDT(inputStream2, is2Module);
		removeJavadoc(unit2);

		TalkativeASTMatcher matcher = new TalkativeASTMatcher(true);
		boolean result = unit1.subtreeMatch(matcher, unit2);

		assertTrue(result, "Reprint not equal: " + matcher.getDiff());

		return result;
	}

	private org.eclipse.jdt.core.dom.CompilationUnit parseWithJDT(InputStream inputStream, boolean isModule) {

		ASTParser jdtParser = ASTParser.newParser(AST.JLS15);
		char[] charArray = readTextContents(inputStream).toCharArray();
		jdtParser.setSource(charArray);
		
		if (isModule) {
			jdtParser.setUnitName("module-info.java");
		}

		Map<String, String> options = new HashMap<String, String>();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_15);
		jdtParser.setCompilerOptions(options);

		org.eclipse.jdt.core.dom.CompilationUnit result1 =
			(org.eclipse.jdt.core.dom.CompilationUnit) jdtParser.createAST(null);
		return result1;
	}

	private void removeJavadoc(org.eclipse.jdt.core.dom.CompilationUnit result1) {
		final List<Javadoc> javadocNodes = new ArrayList<Javadoc>();
		result1.accept(new ASTVisitor() {
			@Override
			public boolean visit(Javadoc node) {
				javadocNodes.add(node);
				return true;
			}
		});
		for (Javadoc node : javadocNodes) {
			node.delete();
		}
	}

	protected String calculateOutputFilename(File inputFile, String inputFolderName, String outputFolderName) {
		File inputPath = new File(".", inputFolderName);
		int trimOffset = inputPath.getAbsolutePath().length() - inputFolderName.length() - 2;
		File outputFolder = new File(".", outputFolderName);
		File outputFile = new File(outputFolder, inputFile.getAbsolutePath().substring(trimOffset));
		return outputFile.getAbsolutePath();
	}

	private File prepareOutputFile(String outputFileName) {
		File outputFile = new File(URI.createFileURI(outputFileName).toFileString());
		File parent = outputFile.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		return outputFile;
	}

	private String readTextContents(InputStream inputStream) {
		StringBuffer contents = new StringBuffer();
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			try {
				String line = null; // not declared within while loop
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return contents.toString();
	}

	protected List<File> collectAllFilesRecursive(File startFolder, String extension)
			throws CoreException {
		if (!startFolder.isDirectory()) {
			return Collections.emptyList();
		}
		try {
			return Files.walk(startFolder.toPath()).filter(p -> !p.endsWith(".svn"))
				.filter(p -> !p.endsWith(".git")).filter(Files::isRegularFile)
				.map(p -> p.toFile()).filter(f -> f.getName().endsWith(extension)).collect(Collectors.toList());
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	protected void assertClassTypeParameterCount(Member member, int expectedNumberOfTypeArguments) {
		assertType(member, org.emftext.language.java.classifiers.Class.class);
		org.emftext.language.java.classifiers.Class clazz = (org.emftext.language.java.classifiers.Class) member;
		List<TypeParameter> typeParameters = clazz.getTypeParameters();
		assertEquals(expectedNumberOfTypeArguments, typeParameters.size(),
			"Expected " + expectedNumberOfTypeArguments + " type parameter(s).");
	}

	protected void assertInterfaceTypeParameterCount(Member member, int expectedNumberOfTypeArguments) {
		assertType(member, Interface.class);
		Interface interfaze = (Interface) member;
		List<TypeParameter> typeParameters = interfaze.getTypeParameters();
		assertEquals(expectedNumberOfTypeArguments, typeParameters.size(),
			"Expected " + expectedNumberOfTypeArguments + " type parameter(s).");
	}

	protected void assertMethodThrowsCount(Member member, int expectedNumberOfThrownExceptions) {
		assertType(member, Method.class);
		Method method = (Method) member;
		List<NamespaceClassifierReference> exceptions = method.getExceptions();
		assertEquals(expectedNumberOfThrownExceptions, exceptions.size(),
			"Expected " + expectedNumberOfThrownExceptions + " exception(s).");
	}

	protected void assertMethodTypeParameterCount(Member member, int expectedNumberOfTypeArguments) {
		assertType(member, Method.class);
		Method method = (Method) member;
		List<TypeParameter> typeParameters = method.getTypeParameters();
		assertEquals(expectedNumberOfTypeArguments, typeParameters.size(),
			"Expected " + expectedNumberOfTypeArguments + " type parameter(s).");
	}

	protected void assertConstructorThrowsCount(Member member, int expectedNumberOfThrownExceptions) {
		assertType(member, Constructor.class);
		Constructor constructor = (Constructor) member;
		List<NamespaceClassifierReference> exceptions = constructor.getExceptions();
		assertEquals(expectedNumberOfThrownExceptions, exceptions.size(),
			"Expected " + expectedNumberOfThrownExceptions + " exception(s).");
	}

	protected void assertConstructorTypeParameterCount(Member member, int expectedNumberOfTypeArguments) {
		assertType(member, Constructor.class);
		Constructor constructor = (Constructor) member;
		List<TypeParameter> typeParameters = constructor.getTypeParameters();
		assertEquals(expectedNumberOfTypeArguments, typeParameters.size(),
			"Expected " + expectedNumberOfTypeArguments + " type parameter(s).");
	}

	protected void assertIsClass(Classifier classifier) {
		assertType(classifier, org.emftext.language.java.classifiers.Class.class);
	}

	protected void assertIsInterface(Classifier classifier) {
		assertType(classifier, Interface.class);
	}

	protected Constructor assertIsConstructor(Member member) {
		assertType(member, Constructor.class);
		return (Constructor) member;
	}

	protected void assertType(EObject object, Class<?> expectedType) {
		assertTrue(expectedType.isInstance(object),
			"The object should have type '" + expectedType.getSimpleName() + "', but was "
			+ object.getClass().getSimpleName());
	}

	protected void assertClassifierName(Classifier declaration, String expectedName) {
		assertEquals(expectedName, declaration.getName(),
			"The name of the declared classifier should equal '" + expectedName + "'");
	}

	protected void assertNumberOfClassifiers(CompilationUnit model, int expectedCount) {
		assertEquals(expectedCount, model.getClassifiers().size(),
			"The compilation unit should contain " + expectedCount + " classifier declaration(s).");
	}

	protected void assertModifierCount(Method method, int expectedNumberOfModifiers) {
		assertEquals(expectedNumberOfModifiers, method.getModifiers().size(),
				"Method '" + method.getName() + "' should have " + expectedNumberOfModifiers + " modifier(s).");
	}

	protected void assertIsPublic(Method method) {
		assertTrue(method.getModifiers().get(0) instanceof Public, "Method '" + method.getName() + "' should be public.");
	}

	protected Enumeration assertParsesToEnumeration(String typename) throws Exception {
		return assertParsesToEnumeration("", typename);
	}

	protected Enumeration assertParsesToEnumeration(String pkgFolder, String typename)
			throws Exception {
		return assertParsesToType(pkgFolder, typename, Enumeration.class);
	}
	
	protected Interface assertParsesToInterface(String typename) throws Exception {
		return assertParsesToInterface("", typename);
	}

	protected Interface assertParsesToInterface(String pkgFolder, String typename)
			throws Exception {
		return assertParsesToType(pkgFolder, typename, Interface.class);
	}

	protected Annotation assertParsesToAnnotation(String typename) throws Exception {
		return assertParsesToAnnotation("", typename);
	}
	
	protected Annotation assertParsesToAnnotation(String pkgFolder, String typename)
			throws Exception {
		return assertParsesToType(pkgFolder, typename, Annotation.class);
	}

	protected <T> T assertParsesToType(String pkgFolder, String typename, String folder,
			Class<T> expectedType) throws Exception {
		String filename = pkgFolder + File.separator + typename + ".java";
		JavaRoot model = parseResource(filename, folder);
		if (model instanceof CompilationUnit) {
			CompilationUnit cu = (CompilationUnit) model;
			assertNumberOfClassifiers(cu, 1);

			Classifier declaration = cu.getClassifiers().get(0);
			assertClassifierName(declaration, typename);
			assertType(declaration, expectedType);
			return expectedType.cast(declaration);
		} else {
			return null;
		}
	}

	protected <T> T assertParsesToType(String typename, Class<T> expectedType) 
			throws Exception {	
		return assertParsesToType("", typename, expectedType);
	}
	
	protected <T> T assertParsesToType(String pkgFolder, String typename, Class<T> expectedType)
			throws Exception {
		return assertParsesToType(pkgFolder, typename, getTestInputFolder(),
				expectedType);
	}

	protected abstract String getTestInputFolder();

	protected JavaRoot parseResource(String filename) throws Exception {
		return parseResource(filename, getTestInputFolder());
	}

	protected void parseAndReprint(String filename) throws Exception {
		parseAndReprint(filename, getTestInputFolder(), TEST_OUTPUT_FOLDER);
	}

	protected void parseAndReprint(File file) throws Exception {
		parseAndReprint(file, getTestInputFolder(), TEST_OUTPUT_FOLDER);
	}
	
	protected org.emftext.language.java.classifiers.Class assertParsesToClass(
			String typename) throws Exception {
		return assertParsesToClass("", typename);
	}

	protected org.emftext.language.java.classifiers.Class assertParsesToClass(
			String pkgFolder, String typename) throws Exception {
		return assertParsesToType(pkgFolder, typename,
				org.emftext.language.java.classifiers.Class.class);
	}

	protected void assertMemberCount(MemberContainer container, int expectedCount) {
		String name = container.toString();
		if (container instanceof NamedElement) {
			name = ((NamedElement) container).getName();
		}
		assertEquals(expectedCount, container.getMembers().size(),
				name + " should have " + expectedCount + " member(s).");
	}
	
	protected void assertParsesToClass(String typename, int expectedMembers)
			throws Exception, IOException, BadLocationException {
		assertParsesToClass("", typename, expectedMembers);
	}

	protected void assertParsesToClass(String pkgFolder, String typename, int expectedMembers)
			throws Exception, IOException, BadLocationException {
		String filename = typename + ".java";
		org.emftext.language.java.classifiers.Class clazz = assertParsesToClass(pkgFolder, typename);
		assertEquals(expectedMembers, clazz.getMembers().size(),
				typename + " should have " + expectedMembers + " member(s).");

		parseAndReprint(filename);
	}

	protected void assertResolveAllProxies(EObject element) {
		assertResolveAllProxies(element.eResource());
	}
	
	private ResourceSet testSet;
	
	private void createNewResourceSet() {
		testSet = new ResourceSetImpl();
		testSet.getLoadOptions().putAll(getLoadOptions());
	}

	protected ResourceSet getResourceSet() throws Exception {
		return testSet;
	}

	protected boolean assertResolveAllProxies(Resource resource) {
		StringBuffer msg = new StringBuffer();
		resource.getAllContents().forEachRemaining(obj -> {
			InternalEObject element = (InternalEObject) obj;
			assertFalse(element.eIsProxy(), "Can not resolve: " + element.eProxyURI());
			for (EObject crElement : element.eCrossReferences()) {
				crElement = EcoreUtil.resolve(crElement, resource);
				if (crElement.eIsProxy()) {
					msg.append("\nCan not resolve: " + ((InternalEObject) crElement).eProxyURI());
				}
			}
		});
		String finalMsg = msg.toString();
		assertFalse(finalMsg.length() != 0, finalMsg);
		return finalMsg.length() != 0;
	}
	

	protected void assertModelValid(Resource resource) {
		org.eclipse.emf.common.util.Diagnostic result = Diagnostician.INSTANCE.validate(resource.getContents().get(0));
		String msg = "EMF validation problems found:";
		for (org.eclipse.emf.common.util.Diagnostic childResult : result.getChildren()) {
			msg += "\n" + childResult.getMessage();
		}
		assertTrue(result.getChildren().isEmpty(), msg);
	}
	
	protected void assertModelValid(ResourceSet set) {
		StringBuilder builder = new StringBuilder();
		for (Resource res : new ArrayList<>(set.getResources())) {
			if (res.getContents().size() == 0) {
				continue;
			}
			org.eclipse.emf.common.util.Diagnostic result = Diagnostician.INSTANCE.validate(res.getContents().get(0));
			if (!result.getChildren().isEmpty()) {
				builder.append("EMF validation problems found in '");
				builder.append(res.getURI().toString());
				builder.append("':\n");
				for (org.eclipse.emf.common.util.Diagnostic childResult : result.getChildren()) {
					builder.append(childResult.getMessage());
					builder.append("\n");
				}
			}
		}
		assertTrue(builder.length() == 0, builder.toString());
	}
}
