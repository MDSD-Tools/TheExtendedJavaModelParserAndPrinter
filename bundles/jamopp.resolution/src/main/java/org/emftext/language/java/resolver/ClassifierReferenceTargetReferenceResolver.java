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
 *      - Extension with the treatment of "var"
 ******************************************************************************/
package org.emftext.language.java.resolver;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.LogicalJavaURIGenerator;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.expressions.Expression;
import org.emftext.language.java.expressions.NestedExpression;
import org.emftext.language.java.generics.TypeParameter;
import org.emftext.language.java.instantiations.NewConstructorCall;
import org.emftext.language.java.references.Reference;
import org.emftext.language.java.references.ReferencesPackage;
import org.emftext.language.java.resolver.decider.ConcreteClassifierDecider;
import org.emftext.language.java.resolver.decider.ScopedTreeWalker;
import org.emftext.language.java.resolver.decider.TypeParameterDecider;
import org.emftext.language.java.resolver.result.IJavaReferenceResolveResult;
import org.emftext.language.java.types.ClassifierReference;
import org.emftext.language.java.types.NamespaceClassifierReference;
import org.emftext.language.java.types.TypeReference;

public class ClassifierReferenceTargetReferenceResolver implements
	IJavaReferenceResolver<ClassifierReference, Classifier> {
	@Override
	public void resolve(String identifier, ClassifierReference container, EReference reference,
			int position, IJavaReferenceResolveResult<Classifier> result) {
		EObject startingPoint = null;
		EObject target = null;
		boolean hasNamespace = false;

		if (container.eContainer() instanceof NamespaceClassifierReference) {
			NamespaceClassifierReference ncr = (NamespaceClassifierReference) container.eContainer();
			int idx = ncr.getClassifierReferences().indexOf(container);
			if (idx > 0) {
				hasNamespace = true;
				startingPoint = ncr.getClassifierReferences().get(idx - 1).getTarget();
			} else {
				if (ncr.getNamespaces().size() > 0) {
					hasNamespace = true;
					EObject lastClassInNS = ConcreteClassifierDecider.resolveRelativeNamespace(
							ncr, 0, container, container, reference);
					if (lastClassInNS != null) {
						startingPoint = lastClassInNS;
					} else {
						//absolute class starting with package
						target = resolveFullQualifiedTypeReferences(identifier,
								ncr, container, reference);
					}
				}
			}
		}

		if (target == null) {
			//new constructor call can be part of reference chain
			if (startingPoint == null && (container.eContainer().eContainer() instanceof NewConstructorCall
				|| container.eContainer() instanceof NewConstructorCall)) {
				NewConstructorCall ncc = container.eContainer() instanceof NewConstructorCall
					? (NewConstructorCall) container.eContainer()
					: (NewConstructorCall) container.eContainer().eContainer();
				if (ncc.eContainmentFeature().equals(ReferencesPackage.Literals.REFERENCE__NEXT)) {
					startingPoint = ((Reference) ncc.eContainer()).getReferencedType();

					//a ncc can be encapsulated in nested expressions
					EObject outerContainer = ncc.eContainer();
					while (outerContainer instanceof NestedExpression) {
						Expression nestedExpression = ((NestedExpression) outerContainer).getExpression();
						if (nestedExpression instanceof Reference) {
							outerContainer = (Reference) nestedExpression;
						} else {
							break;
						}
					}

					if (outerContainer instanceof NewConstructorCall) {
						NewConstructorCall outerNcc = (NewConstructorCall) outerContainer;
						if (outerNcc.getAnonymousClass() != null) {
							startingPoint = outerNcc.getAnonymousClass();
						} else {
							startingPoint = outerNcc.getTypeReference().getTarget();
						}
					}
				}
			}

			if (startingPoint == null) {
				startingPoint = container;
			}

			if (hasNamespace) {
				if (startingPoint instanceof ConcreteClassifier) {
					for (ConcreteClassifier cand
							: ((ConcreteClassifier) startingPoint).getAllInnerClassifiers()) {
						if (identifier.equals(cand.getName())) {
							target = cand;
							break;
						}
					}
					if (target == null) {
						String potName = ((ConcreteClassifier) startingPoint).getQualifiedName()
								+ LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR + identifier;
						target = EcoreUtil.resolve(JavaClasspath.get(container)
								.getConcreteClassifier(potName), container);
						if (target.eIsProxy()) {
							potName = ((ConcreteClassifier) startingPoint).getQualifiedName()
									+ LogicalJavaURIGenerator.PACKAGE_SEPARATOR + identifier;
							target = EcoreUtil.resolve(JavaClasspath.get(container)
									.getConcreteClassifier(potName), container);
						}
					}
				} else if (startingPoint instanceof TypeParameter) {
					for (TypeReference extendsClassifierReference
							: ((TypeParameter) startingPoint).getExtendTypes()) {
						ConcreteClassifier extendsClassifier
							= (ConcreteClassifier) extendsClassifierReference.getTarget();
						for (ConcreteClassifier cand : extendsClassifier.getAllInnerClassifiers()) {
							if (identifier.equals(cand.getName())) {
								target = cand;
								break;
							}
						}
					}
				}

			} else {
				ScopedTreeWalker resolutionWalker = new ScopedTreeWalker(
						List.of(new ConcreteClassifierDecider(), new TypeParameterDecider()));
				target = resolutionWalker.walk(startingPoint, identifier, container, reference);
			}

		}
		
		if (target != null) {
			if (target.eIsProxy()) {
				target = EcoreUtil.resolve(target, container);
			}
			if (!target.eIsProxy()) {
				result.addMapping(identifier, (Classifier) target);
			}
		}
	}

	private EObject resolveFullQualifiedTypeReferences(String identifier,
			NamespaceClassifierReference ncr, EObject container, EReference reference) {

		int idx = ncr.getClassifierReferences().indexOf(container);
		if (ncr.getNamespaces().size() > 0 && idx == 0) {
			EObject target = null;
			StringBuilder builder = new StringBuilder();
			for (int i = ncr.getNamespaces().size() - 1; i >= -1; i--) {
				builder.delete(0, builder.length());
				for (int j = 0; j <= i; j++) {
					builder.append(ncr.getNamespaces().get(j));
					builder.append(LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
				}
				for (int j = i + 1; j < ncr.getNamespaces().size(); j++) {
					builder.append(ncr.getNamespaces().get(j));
					builder.append(LogicalJavaURIGenerator.CLASSIFIER_SEPARATOR);
				}
				builder.append(identifier);
				target = (ConcreteClassifier) EcoreUtil.resolve(
					JavaClasspath.get(ncr).getConcreteClassifier(builder.toString()), container);
				if (!target.eIsProxy()) {
					break;
				} else {
					target = null;
				}
			}

			return target;
		}
		return null;
	}
}
