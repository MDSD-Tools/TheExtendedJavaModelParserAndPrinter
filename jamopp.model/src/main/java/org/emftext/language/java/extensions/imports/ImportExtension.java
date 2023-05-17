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
package org.emftext.language.java.extensions.imports;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Enumeration;
import org.emftext.language.java.commons.NamedElement;
import org.emftext.language.java.imports.Import;

public class ImportExtension {

	/**
	 * Returns the classifier with the given name 
	 * located in the namespace defined by the import.
	 * 
	 * @param me the import.
	 * @param name the name of the classifier
	 * @return imported classifier (proxy)
	 */
	public static ConcreteClassifier getImportedClassifier(Import me, String name) {
		String containerName = me.getNamespacesAsString();
		if (containerName == null) {
			return null;
		}
		
		String fullQualifiedName = containerName + name;
		return me.getConcreteClassifier(fullQualifiedName);
	}
	
	/**
	 * Returns all imported members assuming the import's namespace
	 * identifies a classifier.
	 * 
	 * @param me the import.
	 * @return list of imported classifiers (proxies)
	 */
	public static EList<NamedElement> getImportedMembers(Import me) {
		ConcreteClassifier concreteClassifier = me.getClassifier();
		
		if (concreteClassifier == null || concreteClassifier.eIsProxy()) {
			return ECollections.emptyEList();
		}
		
		EList<NamedElement> result = new UniqueEList<NamedElement>();
		result.addAll(concreteClassifier.getAllMembers(me));
		if (concreteClassifier instanceof Enumeration) {
			Enumeration enumeration = (Enumeration) concreteClassifier;
			result.addAll(enumeration.getConstants());
		}
		
		return result;
	}
	
	/**
	 * Returns a list of imported classifiers assuming the import's namespace
	 * identifies a package.
	 * 
	 * @param me the import.
	 * @return imported classifier (proxy)
	 */
	public static EList<ConcreteClassifier> getImportedClassifiers(Import me) {
		String containerName = me.getNamespacesAsString();
		if (containerName == null) {
			return ECollections.emptyEList();
		}
		
		return me.getConcreteClassifiers(containerName, "*");
	}
}
