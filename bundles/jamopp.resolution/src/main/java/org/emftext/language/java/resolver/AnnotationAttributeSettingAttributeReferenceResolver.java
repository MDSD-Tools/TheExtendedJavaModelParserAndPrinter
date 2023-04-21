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

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.emftext.language.java.annotations.AnnotationAttributeSetting;
import org.emftext.language.java.members.InterfaceMethod;
import org.emftext.language.java.resolver.decider.InterfaceMethodDecider;
import org.emftext.language.java.resolver.decider.ScopedTreeWalker;
import org.emftext.language.java.resolver.result.IJavaReferenceResolveResult;

/**
 * Resolves the attribute, i. e., the interface method, of an set attribute within an annotation.
 */
public class AnnotationAttributeSettingAttributeReferenceResolver implements
	IJavaReferenceResolver<AnnotationAttributeSetting, InterfaceMethod> {
	@Override
	public void resolve(String identifier, AnnotationAttributeSetting container, EReference reference,
			int position, IJavaReferenceResolveResult<InterfaceMethod> result) {

		EObject startingPoint = container.getContainingAnnotationInstance().getAnnotation();

		ScopedTreeWalker resolutionWalker = new ScopedTreeWalker(List.of(new InterfaceMethodDecider()));
		EObject target = resolutionWalker.walk(startingPoint, identifier, container, reference);

		if (target != null) {
			result.addMapping(identifier, (InterfaceMethod) target);
		}
	}
}
