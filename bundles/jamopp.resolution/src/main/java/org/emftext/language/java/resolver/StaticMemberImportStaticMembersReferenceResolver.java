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
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Enumeration;
import org.emftext.language.java.imports.StaticMemberImport;
import org.emftext.language.java.members.EnumConstant;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.modifiers.AnnotableAndModifiable;
import org.emftext.language.java.references.ReferenceableElement;
import org.emftext.language.java.resolver.result.IJavaReferenceResolveResult;

public class StaticMemberImportStaticMembersReferenceResolver implements
	IJavaReferenceResolver<StaticMemberImport, ReferenceableElement> {
	@Override
	public void resolve(String identifier, StaticMemberImport theImport, EReference reference,
			int position, IJavaReferenceResolveResult<ReferenceableElement> result) {
		ConcreteClassifier classifier = theImport.getClassifier();
		if (classifier.eIsProxy()) {
			classifier = (ConcreteClassifier) EcoreUtil.resolve(classifier, theImport.eResource());
		}
		if (classifier != null && !classifier.eIsProxy()) {
			for (Member member : classifier.getAllMembers(theImport)) {
				if (identifier.equals(member.getName()) && member instanceof ReferenceableElement) {
					if (member instanceof AnnotableAndModifiable) {
						if (member.eIsProxy()) {
							member = (Member) EcoreUtil.resolve(member, theImport);
						}
						if (((AnnotableAndModifiable) member).isStatic()) {
							result.addMapping(identifier, (ReferenceableElement) member);
						}
					}
				}
			}

			if (classifier instanceof Enumeration) {
				for (EnumConstant enumConstant : ((Enumeration) classifier).getConstants()) {
					if (identifier.equals(enumConstant.getName())) {
						result.addMapping(identifier, enumConstant);
						return;
					}
				}
			}
		}
	}
}
