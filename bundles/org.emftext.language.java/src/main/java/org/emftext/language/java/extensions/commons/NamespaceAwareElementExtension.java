/*******************************************************************************
 * Copyright (c) 2006-2014
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
 ******************************************************************************/

package org.emftext.language.java.extensions.commons;

import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.LogicalJavaURIGenerator;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.commons.NamespaceAwareElement;

public class NamespaceAwareElementExtension {

	/**
	 * Converts the namespaces array of the given namespace aware element into a
	 * String representation using package (.) and class ($) delimiters. The
	 * method uses the JavaClasspath to determine for each element of the namespace
	 * if it identifies a package or a class.
	 * 
	 * @param me the given namespace aware element.
	 * @return single string representation of namespace.
	 */
	public static String getNamespacesAsString(NamespaceAwareElement me) {
		StringBuilder builder = new StringBuilder();
		for (int index = 0; index < me.getNamespaces().size(); index++) {
			builder.append(me.getNamespaces().get(index));
			builder.append(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR);
			if (JavaClasspath.get(me).existsPackage(builder.toString())) {
				continue;
			} else {
				builder.replace(builder.length() - 1, builder.length(),
						LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
			}
		}
		return builder.toString();
	}
	
	/**
	 * Assuming the namespace identifies a classifier, that classifier is
	 * returned.
	 * 
	 * @param me the namespace aware element.
	 * @return classifier at namespace.
	 */
	public static ConcreteClassifier getClassifierAtNamespaces(NamespaceAwareElement me) {
		String s = me.getNamespacesAsString();
		s = s.substring(0, s.length() - 1);
		return JavaClasspath.get(me).getConcreteClassifier(s);
	}
}
