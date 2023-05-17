/*******************************************************************************
 * Copyright (c) 2006-2012
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
package org.emftext.language.java;

import java.util.regex.Pattern;
import org.eclipse.emf.common.util.URI;
import org.emftext.language.java.commons.NamespaceAwareElement;

/**
 * This class provides functionality to construct unique logical URIs 
 * for Java classes, packages and modules represented as EMF-models.
 */
public class LogicalJavaURIGenerator {
	
	/**
	 * URI scheme for the logical URIs.
	 */
	public static final String JAVA_URI_SCHEME = "pathmap";
	
	/**
	 * Logical URI segment for classifiers.
	 */
	public static final String JAVA_URI_SEGMENT_CLASSIFIER = "javaclass";
	
	/**
	 * Logical URI segment for packages.
	 */
	public static final String JAVA_URI_SEGMENT_PACKAGE = "javapackage";
	
	/**
	 * Logical URI segment for modules.
	 */
	public static final String JAVA_URI_SEGMENT_MODULE = "javamodule";

	/**
	 * Pathmap (URI scheme + first segment) for Java classes.
	 */
	public static final String JAVA_CLASSIFIER_PATHMAP = JAVA_URI_SCHEME + ":/" + JAVA_URI_SEGMENT_CLASSIFIER + "/";

	/**
	 * Pathmap (URI scheme + first segment) for Java classes.
	 */
	public static final String JAVA_PACKAGE_PATHMAP = JAVA_URI_SCHEME + ":/" + JAVA_URI_SEGMENT_PACKAGE + "/";
	
	/**
	 * Pathmap (URI scheme + first segment) for Java modules.
	 */
	public static final String JAVA_MODULE_PATHMAP = JAVA_URI_SCHEME + ":/" + JAVA_URI_SEGMENT_MODULE + "/";

	/**
	 * Start of a URI fragment part pointing at a classifier contained in a
	 * compilation unit.
	 */
	public static final String CLASSIFIERS_ROOT_PATH_PREFIX = "@classifiers[name='";

	/**
	 * Start of a URI fragment part pointing at a classifier contained as member in
	 * another classifier.
	 */
	public static final String CLASSIFIERS_SUB_PATH_PREFIX  = "@members[name='";

	/**
	 * End of a URI fragment part.
	 */
	public static final String CLASSIFIERS_PATH_SUFIX       = "']";

	/**
	 * Java's separator for package names (.).
	 */
	public static final String PACKAGE_SEPARATOR    = ".";

	/**
	 * Java's separator for classifier names ($).
	 */
	public static final String CLASSIFIER_SEPARATOR = "$";
	
	/**
	 * The simple file extension for Java files without a preceding dot.
	 */
	public static final String JAVA_FILE_EXTENSION_NAME = "java";

	/**
	 * Java's file extension (.java).
	 */
	public static final String JAVA_FILE_EXTENSION = PACKAGE_SEPARATOR + JAVA_FILE_EXTENSION_NAME;
	
	/**
	 * The simple file extension for class files without the preceding dot.
	 */
	public static final String JAVA_CLASS_FILE_EXTENSION_NAME = "class";

	/**
	 * Java's class file extension (.class).
	 */
	public static final String JAVA_CLASS_FILE_EXTENSION = PACKAGE_SEPARATOR + JAVA_CLASS_FILE_EXTENSION_NAME;
	
	/**
	 * The simple file extension for javaxmi files without the preceding dot.
	 */
	public static final String JAVAXMI_FILE_EXTENSION_NAME = "javaxmi";
	
	/**
	 * File extension for javaxmi files.
	 */
	public static final String JAVAXMI_FILE_EXTENSION = PACKAGE_SEPARATOR + JAVAXMI_FILE_EXTENSION_NAME;
	
	/**
	 * File name of a module declaration.
	 */
	public static final String JAVA_MODULE_FILE_NAME = "module-info.java";
	
	/**
	 * File name of a package declaration.
	 */
	public static final String JAVA_PACKAGE_FILE_NAME = "package-info.java";
	
	/**
	 * We cache this regular expression because it is used very frequently.
	 */
	private static final Pattern CLASSIFIER_SEPARATOR_REGEX_PATTERN = Pattern.compile("\\" + CLASSIFIER_SEPARATOR);

	/**
	 * Constructs a logical URI from a fully qualified classifier name
	 * pointing to the resource containing the classifier.
	 *
	 * @param fullQualifiedName the fully qualified classifier name.
	 * @return the logical URI for the classifier.
	 */
	public static URI getJavaFileResourceURI(String fullQualifiedName) {
		StringBuilder logicalUriString = new StringBuilder(JAVA_CLASSIFIER_PATHMAP);
		logicalUriString.append(fullQualifiedName);
		logicalUriString.append(JAVA_FILE_EXTENSION);

		return URI.createURI(logicalUriString.toString());
	}

	/**
	 * Constructs an URI from a fully qualified classifier name
	 * pointing at the file containing the classifier and the classifier
	 * itself inside the EMF-model constructed from that resource.
	 *
	 * @param fullQualifiedName
	 * @return the logical URI for the classifier
	 */
	public static URI getClassifierURI(String fullQualifiedName) {
		URI logicalUri = getJavaFileResourceURI(fullQualifiedName);

		String classesPart = fullQualifiedName;
		int idx = fullQualifiedName.lastIndexOf(PACKAGE_SEPARATOR);
		if (idx >= 0) {
			classesPart = classesPart.substring(idx + 1);
		}

		String[] classNames = CLASSIFIER_SEPARATOR_REGEX_PATTERN.split(classesPart, -1);

		StringBuilder uriFragment = new StringBuilder();
		for (int i = 0; i < classNames.length; i++) {
			if (i == 0) {
				uriFragment.append("//");
				uriFragment.append(CLASSIFIERS_ROOT_PATH_PREFIX);
			} else {
				uriFragment.append("/");
				uriFragment.append(CLASSIFIERS_SUB_PATH_PREFIX);
			}
			uriFragment.append(classNames[i]);
			uriFragment.append(CLASSIFIERS_PATH_SUFIX);
		}
		logicalUri = logicalUri.appendFragment(uriFragment.toString());
		return logicalUri;
	}

	/**
	 * Returns a simple name (i.e., last segment) for a given fully
	 * qualified name
	 *
	 * @param fullQualifiedName
	 * @return simple name string
	 */
	public static String getSimpleClassName(String fullQualifiedName) {
		int idx1 = fullQualifiedName.lastIndexOf(PACKAGE_SEPARATOR);
		int idx2 = fullQualifiedName.lastIndexOf(CLASSIFIER_SEPARATOR);
		if (idx1 == -1 && idx2 == -1) {
			return fullQualifiedName;
		}
		if (idx1 > idx2) {
			return fullQualifiedName.substring(idx1 + 1);
		} else {
			return fullQualifiedName.substring(idx2 + 1);
		}
	}
	
	/**
	 * Creates a logical URI for modules.
	 * 
	 * @param moduleName the name of the module.
	 * @return the logical URI for a module.
	 */
	public static URI getModuleURI(String moduleName) {
		StringBuilder uri = new StringBuilder();
		uri.append(JAVA_MODULE_PATHMAP);
		uri.append(moduleName);
		uri.append("/");
		uri.append(JAVA_MODULE_FILE_NAME);
		return URI.createURI(uri.toString());
	}
	
	public static URI getPackageURI(String packageName) {
		StringBuilder uri = new StringBuilder();
		uri.append(JAVA_PACKAGE_PATHMAP);
		uri.append(packageName);
		uri.append("/");
		uri.append(JAVA_PACKAGE_FILE_NAME);
		return URI.createURI(uri.toString());
	}

	/**
	 * Constructs a single string representation of the given element's
	 * namespace, assuming that the namespace points at a package
	 * (and not a classifier).
	 *
	 * @param nsaElement
	 * @return the namespace.
	 */
	public static String packageName(NamespaceAwareElement nsaElement) {
		StringBuilder builder = new StringBuilder();
		nsaElement.getNamespaces().forEach(s -> {
			builder.append(s);
			builder.append(LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
		});
		return builder.toString();
	}
}
