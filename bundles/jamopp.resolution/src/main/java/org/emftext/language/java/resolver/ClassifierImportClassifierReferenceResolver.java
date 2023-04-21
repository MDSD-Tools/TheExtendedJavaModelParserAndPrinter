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
 ******************************************************************************/
package org.emftext.language.java.resolver;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.LogicalJavaURIGenerator;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.imports.Import;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.resolver.result.IJavaReferenceResolveResult;

public class ClassifierImportClassifierReferenceResolver implements
	IJavaReferenceResolver<Import, ConcreteClassifier> {
	@Override
	public void resolve(String identifier, Import theImport, EReference reference, int position,
			IJavaReferenceResolveResult<ConcreteClassifier> result) {
		ConcreteClassifier importedClassifier = theImport.getImportedClassifier(identifier);
		if (importedClassifier != null) {
			if (importedClassifier.eIsProxy()) {
				importedClassifier = (ConcreteClassifier) EcoreUtil.resolve(importedClassifier, theImport);
				if (importedClassifier.eIsProxy()) {
					StringBuilder builder = new StringBuilder();
					for (int index = 0; index < theImport.getNamespaces().size(); index++) {
						builder.append(theImport.getNamespaces().get(index));
						builder.append(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR);
						if (JavaClasspath.get(theImport).isPackageRegistered(builder.toString())) {
							continue;
						}
						builder.replace(builder.length() - 1, builder.length(),
								LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
						if (JavaClasspath.get(theImport).isPackageRegistered(builder.toString())) {
							continue;
						}
						builder.delete(builder.length() - 1, builder.length());
						importedClassifier = (ConcreteClassifier) EcoreUtil.resolve(
								JavaClasspath.get(theImport).getConcreteClassifier(
										builder.toString()), theImport);
						if (importedClassifier.eIsProxy()) {
							break;
						}
						for (int j = index + 1; j < theImport.getNamespaces().size(); j++) {
							for (Member m : importedClassifier.getMembers()) {
								if (m instanceof ConcreteClassifier
										&& m.getName().equals(
											theImport.getNamespaces().get(j))) {
									importedClassifier = (ConcreteClassifier) m;
									break;
								}
							}
						}
						for (Member m : importedClassifier.getMembers()) {
							if (m instanceof ConcreteClassifier
									&& m.getName().equals(identifier)) {
								importedClassifier = (ConcreteClassifier) m;
								break;
							}
						}
						break;
					}
				}
			}
			if (!importedClassifier.eIsProxy()) {
				result.addMapping(identifier, importedClassifier);
			}
		}
	}
}
