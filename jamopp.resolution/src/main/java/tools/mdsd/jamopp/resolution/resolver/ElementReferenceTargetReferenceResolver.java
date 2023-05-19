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
package tools.mdsd.jamopp.resolution.resolver;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

import tools.mdsd.jamopp.model.java.JavaClasspath;
import tools.mdsd.jamopp.model.java.classifiers.ConcreteClassifier;
import tools.mdsd.jamopp.model.java.classifiers.Interface;
import tools.mdsd.jamopp.model.java.expressions.Expression;
import tools.mdsd.jamopp.model.java.expressions.ExpressionsPackage;
import tools.mdsd.jamopp.model.java.expressions.NestedExpression;
import tools.mdsd.jamopp.model.java.expressions.PrimaryExpressionReferenceExpression;
import tools.mdsd.jamopp.model.java.extensions.members.MethodExtension;
import tools.mdsd.jamopp.model.java.instantiations.NewConstructorCall;
import tools.mdsd.jamopp.model.java.members.Field;
import tools.mdsd.jamopp.model.java.members.Member;
import tools.mdsd.jamopp.model.java.members.Method;
import tools.mdsd.jamopp.model.java.references.ElementReference;
import tools.mdsd.jamopp.model.java.references.IdentifierReference;
import tools.mdsd.jamopp.model.java.references.PackageReference;
import tools.mdsd.jamopp.model.java.references.Reference;
import tools.mdsd.jamopp.model.java.references.ReferenceableElement;
import tools.mdsd.jamopp.model.java.references.ReferencesPackage;
import tools.mdsd.jamopp.model.java.types.PrimitiveType;
import tools.mdsd.jamopp.model.java.types.Type;
import tools.mdsd.jamopp.model.java.util.TemporalCompositeClassifier;
import tools.mdsd.jamopp.resolution.resolver.decider.ConcreteClassifierDecider;
import tools.mdsd.jamopp.resolution.resolver.decider.EnumConstantDecider;
import tools.mdsd.jamopp.resolution.resolver.decider.FieldDecider;
import tools.mdsd.jamopp.resolution.resolver.decider.LocalVariableDecider;
import tools.mdsd.jamopp.resolution.resolver.decider.MethodDecider;
import tools.mdsd.jamopp.resolution.resolver.decider.PackageDecider;
import tools.mdsd.jamopp.resolution.resolver.decider.ParameterDecider;
import tools.mdsd.jamopp.resolution.resolver.decider.ScopedTreeWalker;
import tools.mdsd.jamopp.resolution.resolver.decider.TypeParameterDecider;
import tools.mdsd.jamopp.resolution.resolver.result.IJavaReferenceResolveResult;

public class ElementReferenceTargetReferenceResolver implements
	IJavaReferenceResolver<ElementReference, ReferenceableElement> {
	@Override
	public void resolve(String identifier, ElementReference container, EReference reference,
			int position, IJavaReferenceResolveResult<ReferenceableElement> result) {
		EObject startingPoint = null;
		EObject alternativeStartingPoint = null;
		EObject target = null;
		Reference parentReference = null;

		if (container.eContainingFeature().equals(ReferencesPackage.Literals.REFERENCE__NEXT)) {
			//a follow up reference: different scope
			parentReference = (Reference) container.eContainer();

			startingPoint = parentReference.getReferencedType();
			
			if (parentReference instanceof NestedExpression) {
				alternativeStartingPoint = ((NestedExpression) parentReference
						).getExpression().getAlternativeType();
			}

			//do not search on primitive types but their class representation
			if (startingPoint instanceof PrimitiveType) {
				startingPoint = ((PrimitiveType) startingPoint).wrapPrimitiveType();
			}

			if (parentReference instanceof NestedExpression) {
				startingPoint = (((NestedExpression) parentReference).getExpression()).getType();
			}

			//special case: anonymous class in constructor call
			while (parentReference instanceof NestedExpression) {
				Expression nestedExpression = ((NestedExpression) parentReference).getExpression();
				if (nestedExpression instanceof Reference) {
					parentReference = (Reference) nestedExpression;
				} else {
					parentReference = null;
				}
			}
			if (parentReference instanceof NewConstructorCall
					&& ((NewConstructorCall) parentReference).getAnonymousClass() != null) {
				startingPoint = ((NewConstructorCall) parentReference).getAnonymousClass();
			}
		}
		
		if (container.eContainingFeature().equals(
				ExpressionsPackage.Literals.PRIMARY_EXPRESSION_REFERENCE_EXPRESSION__METHOD_REFERENCE)) {
			PrimaryExpressionReferenceExpression parent = (PrimaryExpressionReferenceExpression)
				container.eContainer();
			ConcreteClassifier classifier = (ConcreteClassifier) parent.getChild().getType();
			Type targetType = parent.getTargetType();
			if (classifier != null && targetType != null
					&& !targetType.eIsProxy() && targetType instanceof Interface) {
				Method functionalMethod = ((Interface) targetType).getAbstractMethodOfFunctionalInterface();
				for (Member mem : classifier.getAllMembers(classifier)) {
					if (mem.getName().equals(identifier) && mem instanceof Method) {
						if (functionalMethod.isSignatureMatching((Method) mem)) {
							target = mem;
							break;
						}
						if (MethodExtension.isSignatureMatching(
								functionalMethod, (Method) mem, classifier)) {
							target = mem;
							break;
						}
					}
				}
			} else {
				return;
			}
		}
		
		if (startingPoint == null) {
			startingPoint = container;
		}

		if (startingPoint instanceof TemporalCompositeClassifier) {
			for (EObject superType : ((TemporalCompositeClassifier) startingPoint).getSuperTypes()) {
				target = searchFromStartingPoint(identifier, container, reference, superType);
				if (target != null) {
					break;
				}
			}
		} else if (target == null) {
			target = searchFromStartingPoint(identifier, container, reference, startingPoint);
		}
		
		if (target == null && alternativeStartingPoint != null && !alternativeStartingPoint.equals(startingPoint)) {
			target = searchFromStartingPoint(identifier, container, reference, alternativeStartingPoint);
		}
		
		if (target == null) {
			target = checkPrimitiveTypeReference(identifier, container);
		}

		if (target != null) {
			if (target.eIsProxy()) {
				target = EcoreUtil.resolve(target, container);
			}
			if (!target.eIsProxy()) {
				if (target instanceof PackageReference
						|| target instanceof Field && target.eContainer() == null
							&& ((Field) target).getName().equals("length")) {
					container.setContainedTarget((ReferenceableElement) target);
				}
				result.addMapping(identifier, (ReferenceableElement) target);
			}
		}
	}

	private EObject searchFromStartingPoint(String identifier, ElementReference container,
			EReference reference, EObject startingPoint) {
		ScopedTreeWalker resolutionWalker = new ScopedTreeWalker(List.of(new EnumConstantDecider(),
				new FieldDecider(), new LocalVariableDecider(), new ParameterDecider(), new MethodDecider(),
				new ConcreteClassifierDecider(), new TypeParameterDecider(), new PackageDecider()));
		return resolutionWalker.walk(startingPoint, identifier, container, reference);
	}
	
	private ConcreteClassifier checkPrimitiveTypeReference(String identifier, ElementReference ref) {
		if (ref instanceof IdentifierReference) {
			String potClassifierName = null;
			switch (identifier) {
				case "void":
					potClassifierName = Void.class.getCanonicalName();
					break;
				case "int":
					potClassifierName = Integer.class.getCanonicalName();
					break;
				case "short":
					potClassifierName = Short.class.getCanonicalName();
					break;
				case "byte":
					potClassifierName = Byte.class.getCanonicalName();
					break;
				case "long":
					potClassifierName = Long.class.getCanonicalName();
					break;
				case "float":
					potClassifierName = Float.class.getCanonicalName();
					break;
				case "double":
					potClassifierName = Double.class.getCanonicalName();
					break;
				case "boolean":
					potClassifierName = Boolean.class.getCanonicalName();
					break;
				default:
					return null;
			}
			return (ConcreteClassifier) EcoreUtil.resolve(
					JavaClasspath.get(ref).getConcreteClassifier(potClassifierName), ref);
		}
		return null;
	}
}
