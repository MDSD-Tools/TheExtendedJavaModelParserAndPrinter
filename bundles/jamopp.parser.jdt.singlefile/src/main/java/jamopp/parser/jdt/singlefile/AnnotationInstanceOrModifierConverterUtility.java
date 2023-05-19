/*******************************************************************************
 * Copyright (c) 2020, Martin Armbruster
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Martin Armbruster
 *      - Initial implementation
 ******************************************************************************/

package jamopp.parser.jdt.singlefile;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import jamopp.proxy.IJavaContextDependentURIFragmentCollector;

class AnnotationInstanceOrModifierConverterUtility {
	static org.emftext.language.java.modifiers.AnnotationInstanceOrModifier
			converToModifierOrAnnotationInstance(IExtendedModifier mod) {
		if (mod.isModifier()) {
			return convertToModifier((Modifier) mod);
		} else { // mod.isAnnotation()
			return convertToAnnotationInstance((Annotation) mod);
		}
	}
	
	static org.emftext.language.java.modifiers.Modifier convertToModifier(Modifier mod) {
		org.emftext.language.java.modifiers.Modifier result = null;
		if (mod.isAbstract()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createAbstract();
		} else if (mod.isDefault()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createDefault();
		} else if (mod.isFinal()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createFinal();
		} else if (mod.isNative()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createNative();
		} else if (mod.isPrivate()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createPrivate();
		} else if (mod.isProtected()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createProtected();
		} else if (mod.isPublic()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createPublic();
		} else if (mod.isStatic()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createStatic();
		} else if (mod.isStrictfp()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createStrictfp();
		} else if (mod.isSynchronized()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createSynchronized();
		} else if (mod.isTransient()) {
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createTransient();
		} else { // mod.isVolatile()
			result = org.emftext.language.java.modifiers.ModifiersFactory.eINSTANCE.createVolatile();
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, mod);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.annotations.AnnotationInstance convertToAnnotationInstance(Annotation annot) {
		org.emftext.language.java.annotations.AnnotationInstance result =
				org.emftext.language.java.annotations.AnnotationsFactory.eINSTANCE.createAnnotationInstance();
		org.emftext.language.java.classifiers.Class proxyClass =
				org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createClass();
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(annot.getTypeName(), proxyClass);
		if (annot.getTypeName() instanceof QualifiedName) {
			BaseConverterUtility.convertToNamespacesAndSet(
				((QualifiedName) annot.getTypeName()).getQualifier(), result);
		}
		IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.registerContextDependentURIFragment(result,
			org.emftext.language.java.annotations.AnnotationsPackage.Literals.ANNOTATION_INSTANCE__ANNOTATION,
			proxyClass.getName(), proxyClass, -1, annot.resolveAnnotationBinding());
		result.setAnnotation(proxyClass);
		if (annot.isSingleMemberAnnotation()) {
			org.emftext.language.java.annotations.SingleAnnotationParameter param =
					org.emftext.language.java.annotations.AnnotationsFactory.eINSTANCE
				.createSingleAnnotationParameter();
			result.setParameter(param);
			SingleMemberAnnotation singleAnnot = (SingleMemberAnnotation) annot;
			param.setValue(convertToAnnotationValue(singleAnnot.getValue()));
		} else if (annot.isNormalAnnotation()) {
			org.emftext.language.java.annotations.AnnotationParameterList param =
					org.emftext.language.java.annotations.AnnotationsFactory.eINSTANCE
				.createAnnotationParameterList();
			result.setParameter(param);
			NormalAnnotation normalAnnot = (NormalAnnotation) annot;
			normalAnnot.values().forEach(obj -> {
				MemberValuePair memVal = (MemberValuePair) obj;
				org.emftext.language.java.annotations.AnnotationAttributeSetting attrSet =
						org.emftext.language.java.annotations.AnnotationsFactory.eINSTANCE
					.createAnnotationAttributeSetting();
				org.emftext.language.java.members.InterfaceMethod methodProxy = org.emftext.language
						.java.members.MembersFactory.eINSTANCE.createInterfaceMethod();
				BaseConverterUtility.convertToSimpleNameOnlyAndSet(memVal.getName(), methodProxy);
				IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.registerContextDependentURIFragment(
					attrSet, org.emftext.language.java.annotations.AnnotationsPackage.Literals
					.ANNOTATION_ATTRIBUTE_SETTING__ATTRIBUTE, methodProxy.getName(),
					methodProxy, -1, memVal.resolveMemberValuePairBinding());
				attrSet.setAttribute(methodProxy);
				attrSet.setValue(convertToAnnotationValue(memVal.getValue()));
				LayoutInformationConverter.convertToMinimalLayoutInformation(attrSet, memVal);
				param.getSettings().add(attrSet);
			});
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, annot);
		return result;
	}
	
	static org.emftext.language.java.annotations.AnnotationValue convertToAnnotationValue(Expression expr) {
		if (expr instanceof Annotation) {
			return convertToAnnotationInstance((Annotation) expr);
		} else if (expr.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
			return convertToArrayInitializer((ArrayInitializer) expr);
		}
		return (org.emftext.language.java.expressions.AssignmentExpressionChild)
			ExpressionConverterUtility.convertToExpression((Expression) expr);
	}
	
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.arrays.ArrayInitializer convertToArrayInitializer(ArrayInitializer arr) {
		org.emftext.language.java.arrays.ArrayInitializer result =
				org.emftext.language.java.arrays.ArraysFactory.eINSTANCE.createArrayInitializer();
		arr.expressions().forEach(obj -> {
			org.emftext.language.java.arrays.ArrayInitializationValue value = null;
			Expression expr = (Expression) obj;
			if (expr instanceof ArrayInitializer) {
				value = convertToArrayInitializer((ArrayInitializer) expr);
			} else if (expr instanceof Annotation) {
				value = convertToAnnotationInstance((Annotation) expr);
			} else {
				value = ExpressionConverterUtility.convertToExpression(expr);
			}
			result.getInitialValues().add(value);
		});
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
		return result;
	}
}
