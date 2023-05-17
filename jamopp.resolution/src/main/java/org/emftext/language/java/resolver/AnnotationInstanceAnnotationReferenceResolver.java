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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.annotations.AnnotationInstance;
import org.emftext.language.java.classifiers.Annotation;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.resolver.decider.ConcreteClassifierDecider;
import org.emftext.language.java.resolver.decider.ScopedTreeWalker;
import org.emftext.language.java.resolver.result.IJavaReferenceResolveResult;

public class AnnotationInstanceAnnotationReferenceResolver implements
	IJavaReferenceResolver<AnnotationInstance, Classifier> {
	@Override
	public void resolve(String identifier, AnnotationInstance annotationInstance, EReference reference,
			int position, IJavaReferenceResolveResult<Classifier> result) {
		EObject startingPoint = annotationInstance;
		EObject target =  null;

		if (annotationInstance.getNamespaces().size() > 0) {
			EObject lastClassInNS = ConcreteClassifierDecider.resolveRelativeNamespace(
					annotationInstance, 0, annotationInstance, annotationInstance, reference);
			if (lastClassInNS != null) {
				startingPoint = lastClassInNS;
			} else {
				//absolute class starting with package
				target = resolveFullQualifiedAnnotationReferences(identifier, annotationInstance);
			}
		}

		if (target == null) {
			ScopedTreeWalker resolutionWalker = new ScopedTreeWalker(List.of(new ConcreteClassifierDecider()));
			target = resolutionWalker.walk(startingPoint, identifier, annotationInstance, reference);
		}

		if (target != null) {
			result.addMapping(identifier, (Classifier) target);
		}
	}

	private EObject resolveFullQualifiedAnnotationReferences(String identifier,
			AnnotationInstance annotationInstance) {

		if (annotationInstance.getNamespaces().size() > 0) {
			String containerName = annotationInstance.getNamespacesAsString();
			ConcreteClassifier target = (ConcreteClassifier) EcoreUtil.resolve(
					JavaClasspath.get(annotationInstance).getConcreteClassifier(containerName
						+ identifier),
						annotationInstance.eResource());

			if (target instanceof Annotation) {
				return target;
			}
		}
		return null;
	}
}
