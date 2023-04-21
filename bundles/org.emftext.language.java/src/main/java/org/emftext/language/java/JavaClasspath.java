/*******************************************************************************
 * Copyright (c) 2006-2015
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Dresden, Amtsgericht Dresden, HRB 34001
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Dresden, Germany
 *      - initial API and implementation
 *   Martin Armbruster
 *      - Adaptation and extension for Java 7+
 *      - Extension for loading the standard library in Java 9+
 ******************************************************************************/

package org.emftext.language.java;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.emftext.language.java.classifiers.ClassifiersFactory;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.containers.JavaRoot;
import org.emftext.language.java.members.Member;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;

/**
 * This class is responsible for managing and retrieving Java resources to establish inter-model references between
 * different Java classes represented as EMF-models.
 */
public class JavaClasspath {
	private static JavaClasspath globalClasspath = null;
	private static HashMap<ResourceSet, JavaClasspath> classpaths = new HashMap<>();

	public static JavaClasspath get() {
		if (globalClasspath == null) {
			var localGlobalClasspath = new JavaClasspath(null);
			localGlobalClasspath.registerStdLib();
			globalClasspath = localGlobalClasspath;
		}
		return globalClasspath;
	}
	
	public static JavaClasspath get(EObject obj) {
		if (obj == null) {
			return get();
		}
		return get(obj.eResource());
	}
	
	public static JavaClasspath get(Resource resource) {
		if (resource == null) {
			return get();
		}
		return get(resource.getResourceSet());
	}
	
	public static JavaClasspath get(ResourceSet set) {
		if (set == null) {
			return get();
		}
		if (!classpaths.containsKey(set)) {
			classpaths.put(set, new JavaClasspath(set));
		}
		return classpaths.get(set);
	}
	
	public static void remove(EObject obj) {
		if (obj != null) {
			remove(obj.eResource());
		}
	}
	
	public static void remove(Resource resource) {
		if (resource != null) {
			remove(resource.getResourceSet());
		}
	}
	
	public static void remove(ResourceSet set) {
		if (set != null && classpaths.containsKey(set)) {
			classpaths.remove(set);
		}
	}

	private ResourceSet resourceSet;
	private boolean registerLocal = false;
	private Map<String, Set<String>> packageClassifierMap = new LinkedHashMap<>();
	
	private JavaClasspath(ResourceSet set) {
		resourceSet = set;
	}
	
	public void enableLocalRegistration() {
		registerLocal = true;
	}

	public void clear() {
		packageClassifierMap.clear();
		for (Iterator<Map.Entry<URI, URI>> iter = getURIMap().entrySet().iterator();
				iter.hasNext();) {
			String fileExt = iter.next().getKey().fileExtension();
			if (LogicalJavaURIGenerator.JAVA_FILE_EXTENSION_NAME.equals(fileExt)
				|| LogicalJavaURIGenerator.JAVA_CLASS_FILE_EXTENSION_NAME.equals(fileExt)
				|| LogicalJavaURIGenerator.JAVAXMI_FILE_EXTENSION_NAME.equals(fileExt)) {
				iter.remove();
			}
		}
	}
	
	public void registerJavaRoot(JavaRoot root, URI physicalUri) {
		if (root instanceof CompilationUnit) {
			registerClassifier((CompilationUnit) root, physicalUri);
		} else if (root instanceof org.emftext.language.java.containers.Package) {
			registerPackage((org.emftext.language.java.containers.Package) root, physicalUri);
		} else if (root instanceof org.emftext.language.java.containers.Module) {
			registerModule((org.emftext.language.java.containers.Module) root, physicalUri);
		}
	}

	public void registerPackage(org.emftext.language.java.containers.Package pack,
			URI physicalUri) {
		URI logicalURI = LogicalJavaURIGenerator.getPackageURI(pack.getNamespacesAsString());
		updateMapping(logicalURI, physicalUri);
		registerPackages(pack.getNamespaces().toArray(new String[0]));
	}
	
	public void registerPackage(String packageName, URI physicalUri) {
		updateMapping(LogicalJavaURIGenerator.getPackageURI(packageName), physicalUri);
		registerPackages(packageName.split("\\" + LogicalJavaURIGenerator.PACKAGE_SEPARATOR));
	}
	
	private void registerPackages(String[] packages) {
		StringBuilder parentName = new StringBuilder();
		registerPackage("");
		for (String p : packages) {
			parentName.append(p);
			parentName.append(LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
			String parentNameString = parentName.toString();
			registerPackage(parentNameString);
		}
	}

	public void registerModule(org.emftext.language.java.containers.Module module,
			URI physicalUri) {
		URI logicalURI = LogicalJavaURIGenerator.getModuleURI(module.getNamespacesAsString());
		updateMapping(logicalURI, physicalUri);
	}
	
	public void registerModule(String moduleName, URI physicalUri) {
		updateMapping(LogicalJavaURIGenerator.getModuleURI(moduleName), physicalUri);
	}
	
	public org.emftext.language.java.containers.Package getPackage(String packageName) {
		return null;
	}
	
	public org.emftext.language.java.containers.Module getModule(String moduleName) {
		return null;
	}
	
	public ConcreteClassifier getConcreteClassifier(String fullQualifiedClassifierName) {
		String actualName;
		if (fullQualifiedClassifierName.startsWith(LogicalJavaURIGenerator.PACKAGE_SEPARATOR)) {
			actualName = fullQualifiedClassifierName.substring(1);
		} else {
			actualName = fullQualifiedClassifierName;
		}
		return getProxyConcreteClassifier(actualName);
	}
	
	public Collection<ConcreteClassifier> getConcreteClassifiers(String packageName) {
		String actualPackName = checkPackageName(packageName);
		var contents = this.packageClassifierMap.entrySet().stream().filter(e -> {
			String k = e.getKey();
			if (k.equals(actualPackName)) {
				return true;
			} else if (actualPackName.equals(LogicalJavaURIGenerator.PACKAGE_SEPARATOR)) {
				return !k.contains(LogicalJavaURIGenerator.PACKAGE_SEPARATOR)
						&& k.contains(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR);
			} else if (k.startsWith(actualPackName)) {
				String kSub = k.substring(actualPackName.length());
				return !kSub.contains(LogicalJavaURIGenerator.PACKAGE_SEPARATOR)
						&& kSub.contains(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR);
			}
			return false;
		}).collect(Collectors.toList());
		var result = new ArrayList<ConcreteClassifier>();
		contents.forEach(e -> {
			e.getValue().stream().map(cc -> e.getKey() + cc).map(this::getProxyConcreteClassifiers)
				.filter(p -> p != null && !p.isEmpty()).forEach(p -> result.addAll(p));
		});
		if (this != get()) {
			result.addAll(get().getConcreteClassifiers(packageName));
		}
		return result;
	}
	
	private Collection<ConcreteClassifier> getProxyConcreteClassifiers(String fullQualifiedClassifierName) {
		if (fullQualifiedClassifierName.startsWith(LogicalJavaURIGenerator.PACKAGE_SEPARATOR)) {
			fullQualifiedClassifierName = fullQualifiedClassifierName.substring(1);
		}
		var result = new ArrayList<ConcreteClassifier>();
		int dotIdx = fullQualifiedClassifierName.lastIndexOf(LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
		int dollarIdx = fullQualifiedClassifierName.lastIndexOf(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR);
		if (dollarIdx != -1 && dollarIdx > dotIdx) {
			URI logURI = LogicalJavaURIGenerator.getClassifierURI(fullQualifiedClassifierName);
			String outerPack = fullQualifiedClassifierName;
			String innerClass = "";
			do {
				innerClass = outerPack.substring(dollarIdx + 1)
						+ LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR + innerClass;
				outerPack = outerPack.substring(0, dollarIdx);
				org.emftext.language.java.classifiers.Class proxy =
						ClassifiersFactory.eINSTANCE.createClass();
				((InternalEObject) proxy).eSetProxyURI(logURI);
				proxy.setName(innerClass);
				result.add(proxy);
				dollarIdx = outerPack.lastIndexOf(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR);
			} while (dollarIdx != -1 && dollarIdx > dotIdx);
		} else {
			result.add(getProxyConcreteClassifier(fullQualifiedClassifierName));
		}
		return result;
	}
	
	private ConcreteClassifier getProxyConcreteClassifier(String fullQualifiedClassifierName) {
		ConcreteClassifier result = ClassifiersFactory.eINSTANCE.createClass();
		((InternalEObject) result).eSetProxyURI(LogicalJavaURIGenerator.getClassifierURI(fullQualifiedClassifierName));
		result.setName(LogicalJavaURIGenerator.getSimpleClassName(fullQualifiedClassifierName));
		return result;
	}
	
	/**
	 * Registers all classes of the Java standard library (<code>src.zip</code> for Java 9+)
	 * located at <code>System.getProperty("java.home")</code>.
	 */
	public void registerStdLib() {
		String classpath = System.getProperty("java.home") + File.separator + "lib" + File.separator + "src.zip";
		internalRegisterJDK9AndUpSrcZip(URI.createFileURI(classpath));
	}
	
	/**
	 * Registers the standard library and JDK classes for JDK 9+ based on the "<JAVA_HOME>/lib/src.zip" file.
	 * 
	 * @param zipURI URI pointing to the zip file containing the source files of the JDK.
	 */
	private void internalRegisterJDK9AndUpSrcZip(URI zipURI) {
		registerZip(zipURI, true);
	}
	
	/**
	 * Registers all source and class files within a zip file.
	 * 
	 * @param zipURI URI pointing to the zip file.
	 */
	public void registerZip(URI zipURI) {
		registerZip(zipURI, false);
	}
	
	/**
	 * Registers all source and class files within a zip file.
	 * 
	 * @param zipURI URI pointing to the zip file containing source and class files.
	 * @param isJDK true if the zip file contains the sources of the JDK. false otherwise.
	 */
	private void registerZip(URI zipURI, boolean isJDK) {
		try (ZipFile zipFile = new ZipFile(zipURI.toFileString())) {
			
			zipFile.stream().filter(entry -> entry.getName().endsWith(LogicalJavaURIGenerator.JAVA_FILE_EXTENSION)
					|| entry.getName().endsWith(LogicalJavaURIGenerator.JAVA_CLASS_FILE_EXTENSION))
				.forEach(entry -> {
					
					String entryName = entry.getName();
					String uri = "archive:" + zipURI.toString() + "!/" + entryName;
					URI physicalURI = URI.createURI(uri);
					// The entry name has the form "<module name>/<package>/<file name>.java"
					// if the file is within the JDK where module name contains only "." as separator,
					// package contains "/" as separator and file name
					// is a simple name.
					
					if (isJDK && entryName.endsWith(LogicalJavaURIGenerator.JAVA_MODULE_FILE_NAME)) {
						// Remove the file name.
						String modName = entryName.substring(0, entryName.indexOf("/"));
						registerModule(modName, physicalURI);
					} else if (entryName.endsWith(LogicalJavaURIGenerator.JAVA_PACKAGE_FILE_NAME)) {
						// Extract the package part.
						int firstIndex = isJDK ? entryName.indexOf("/") + 1 : 0;
						int lastIndex = entryName.lastIndexOf("/");
						String packName = entryName.substring(firstIndex, lastIndex)
								.replace("/", LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
						registerPackage(packName, physicalURI);
					} else {
						// Remove the module name if the file is in the JDK.
						String fullName =
							(isJDK ? entryName.substring(entryName.indexOf("/") + 1) : entryName)
							.replace("/", LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
						
						int lastDotIndex = fullName.lastIndexOf(
								LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
						fullName = fullName.substring(0, lastDotIndex);
						int preLastDotIndex = fullName.lastIndexOf(
								LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
						
						String packageName = "";
						String className = fullName;
						
						if (preLastDotIndex >= 0) {
							packageName = fullName.substring(0, preLastDotIndex);
							className = fullName.substring(preLastDotIndex + 1);
						}
						registerClassifier(packageName, className, physicalURI);
					}
				});
			
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error in processing the zip file: " + zipURI.toFileString());
		}
	}
	
	private Set<String> registerPackage(String packageName) {
		packageName = checkPackageName(packageName);
		Set<String> classesInPackage = packageClassifierMap.get(packageName);
		if (classesInPackage == null) {
			classesInPackage = new HashSet<String>();
			packageClassifierMap.put(packageName, classesInPackage);
		}
		return classesInPackage;
	}
	
	private void registerPackage(String packageName, String className) {
		Set<String> classesInPackage = registerPackage(packageName);
	
		if (!classesInPackage.contains(className)) {
			classesInPackage.add(className);
		}
	}
	
	private void unRegisterPackage(String packageName, String className) {
		packageName = checkPackageName(packageName);
		if (packageClassifierMap.containsKey(packageName)) {
			packageClassifierMap.get(packageName).remove(className);
		}
	}
	
	private List<String> getPackageContents(String packageName) {
		packageName = checkPackageName(packageName);
		List<String> content = new UniqueEList<String>();
		if (packageClassifierMap.containsKey(packageName)) {
			content.addAll(packageClassifierMap.get(packageName));
		}
		return content;
	}
	
	public boolean existsPackage(String packageName) {
		packageName = checkPackageName(packageName);
		return packageClassifierMap.containsKey(packageName);
	}
	
	public Map<URI, URI> getURIMap() {
		return resourceSet != null && registerLocal
				? resourceSet.getURIConverter().getURIMap() : URIConverter.URI_MAP;
	}
	
	private void updateMapping(URI logicalURI, URI physicalURI) {
		getURIMap().put(logicalURI, physicalURI);
	}
	
	private void removeMapping(URI logicalURI) {
		getURIMap().remove(logicalURI);
	}
	
	/**
	 * Registers all classes defined in the given compilation unit.
	 *
	 * @param compilationUnit the given compilation unit.
	 * @param uri the physical URI of the compilation unit.
	 */
	public void registerClassifier(CompilationUnit compilationUnit, URI uri) {
		String packageName = LogicalJavaURIGenerator.packageName(compilationUnit);
		for (ConcreteClassifier classifier : compilationUnit.getClassifiers()) {
			registerClassifier(packageName, classifier.getName(), uri);
			registerInnerClassifiers(classifier, packageName, classifier.getName(), uri);
		}
	}

	/**
	 * Registers the classifier with the given name and package that is physically located at the given URI. If there is
	 * already a classifier registered for the given class name, the old one is replaced with the new one.
	 * 
	 * @param packageName
	 *            the name of the package that contains the classifier.
	 * @param classifierName
	 *            the simple name of the classifier.
	 * @param physicalURI
	 *            the URI where the classifier can be found (class or source file).
	 */
	public void registerClassifier(String packageName, String classifierName, URI physicalURI) {
		if (classifierName == null || physicalURI == null) {
			return;
		}
		packageName = checkPackageName(packageName);

		String innerName = classifierName;
		String outerName = "";
		String qualifiedName = packageName;

		int idx = classifierName.lastIndexOf(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR);
		if (idx >= 0) {
			// The classifier name contains a "$"
			int indexPlusOne = idx + 1;
			innerName = classifierName.substring(indexPlusOne);
			outerName = classifierName.substring(0, indexPlusOne);
			if (LogicalJavaURIGenerator.PACKAGE_SEPARATOR.equals(packageName)) {
				qualifiedName = outerName;
			} else {
				qualifiedName = packageName + outerName;
			}
		}

		synchronized (this) {
			registerPackage(qualifiedName, innerName);

			final String qualifiedClassifierName;
			if (LogicalJavaURIGenerator.PACKAGE_SEPARATOR.equals(packageName)) {
				qualifiedClassifierName = classifierName;
			} else {
				qualifiedClassifierName = packageName + classifierName;
			}

			URI logicalURI = LogicalJavaURIGenerator.getJavaFileResourceURI(qualifiedClassifierName);

			updateMapping(logicalURI, physicalURI);

			String outerPackage = qualifiedName;
			while (outerPackage.endsWith("$")) {
				// Make sure outer classes are registered;
				// This is required when class names contain $ symbols.
				outerPackage = outerPackage.substring(0, outerPackage.length() - 1);
				idx = outerPackage.lastIndexOf("$");
				if (idx == -1) {
					idx = outerPackage.lastIndexOf(".");
				}

				int indexPlusOne = idx + 1;
				String outerClassifier = outerPackage.substring(indexPlusOne);
				outerPackage = outerPackage.substring(0, indexPlusOne);
				outerPackage = checkPackageName(outerPackage);

				registerPackage(outerPackage, outerClassifier);
			}
			outerPackage = outerPackage.substring(0, outerPackage.length() - 1);
			if (outerPackage.contains(LogicalJavaURIGenerator.PACKAGE_SEPARATOR)) {
				registerPackages(outerPackage.split("\\" + LogicalJavaURIGenerator.PACKAGE_SEPARATOR));
			}
		}
	}

	private void registerInnerClassifiers(ConcreteClassifier classifier, String packageName, String className, URI uri) {
		for (Member innerCand : classifier.getMembers()) {
			if (innerCand instanceof ConcreteClassifier) {
				String newClassName = className + LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR
						+ innerCand.getName();
				registerClassifier(packageName, newClassName, uri);
				registerInnerClassifiers((ConcreteClassifier) innerCand, packageName, newClassName, uri);
			}
		}
	}

	/**
	 * Removes the classifier identified by its package and name from the class path.
	 *
	 * @param packageName name of the package.
	 * @param classifierName name of the classifier.
	 */
	public void unRegisterClassifier(String packageName, String classifierName) {
		if (classifierName == null || classifierName.equals("")) {
			return;
		}

		packageName = checkPackageName(packageName);

		String innerName = classifierName;
		String outerName = "";
		String qualifiedName = packageName;

		int idx = classifierName.lastIndexOf(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR);
		if (idx >= 0) {
			innerName = classifierName.substring(idx + 1);
			outerName = classifierName.substring(0, idx + 1);
			if (LogicalJavaURIGenerator.PACKAGE_SEPARATOR.equals(packageName)) {
				qualifiedName = outerName;
			} else {
				qualifiedName = packageName + outerName;
			}
		}

		synchronized (this) {
			unRegisterPackage(qualifiedName, innerName);

			String fullName = null;
			if (LogicalJavaURIGenerator.PACKAGE_SEPARATOR.equals(packageName)) {
				fullName = classifierName;
			} else {
				fullName = packageName + classifierName;
			}

			URI logicalUri = LogicalJavaURIGenerator.getJavaFileResourceURI(fullName);

			removeMapping(logicalUri);
		}
	}
	
	public boolean isPackageRegistered(String packageName) {
		packageName = checkPackageName(packageName);
		return this.packageClassifierMap.containsKey(packageName);
	}

	public boolean isRegistered(String fullQualifiedName) {
		int idx = fullQualifiedName.lastIndexOf(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR);
		if (idx == -1) {
			idx = fullQualifiedName.lastIndexOf(LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
		}
		String containerName = fullQualifiedName.substring(0, idx + 1);
		String classifierName = fullQualifiedName.substring(idx + 1);
		List<String> containerContent = getPackageContents(containerName);
		if (containerContent == null) {
			return false;
		}
		return containerContent.contains(classifierName);
	}
	
	private String checkPackageName(String packageName) {
		if (!packageName.endsWith(LogicalJavaURIGenerator.PACKAGE_SEPARATOR)
			&& !packageName.endsWith(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR)) {
			return packageName + ".";
		}
		return packageName;
	}
}
