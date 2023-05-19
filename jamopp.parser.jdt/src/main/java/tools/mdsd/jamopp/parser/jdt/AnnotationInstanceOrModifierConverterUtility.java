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

package tools.mdsd.jamopp.parser.jdt;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

class AnnotationInstanceOrModifierConverterUtility {
	static tools.mdsd.jamopp.model.java.modifiers.AnnotationInstanceOrModifier converToModifierOrAnnotationInstance(IExtendedModifier mod) {
		if (mod.isModifier()) {
			return convertToModifier((Modifier) mod);
		} else { // mod.isAnnotation()
			return convertToAnnotationInstance((Annotation) mod);
		}
	}
	
	static tools.mdsd.jamopp.model.java.modifiers.Modifier convertToModifier(Modifier mod) {
		tools.mdsd.jamopp.model.java.modifiers.Modifier result = null;
		if (mod.isAbstract()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createAbstract();
		} else if (mod.isDefault()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createDefault();
		} else if (mod.isFinal()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createFinal();
		} else if (mod.isNative()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createNative();
		} else if (mod.isPrivate()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createPrivate();
		} else if (mod.isProtected()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createProtected();
		} else if (mod.isPublic()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createPublic();
		} else if (mod.isStatic()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createStatic();
		} else if (mod.isStrictfp()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createStrictfp();
		} else if (mod.isSynchronized()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createSynchronized();
		} else if (mod.isTransient()) {
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createTransient();
		} else { // mod.isVolatile()
			result = tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createVolatile();
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, mod);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	static tools.mdsd.jamopp.model.java.annotations.AnnotationInstance convertToAnnotationInstance(Annotation annot) {
		tools.mdsd.jamopp.model.java.annotations.AnnotationInstance result = tools.mdsd.jamopp.model.java.annotations.AnnotationsFactory.eINSTANCE.createAnnotationInstance();
		BaseConverterUtility.convertToNamespacesAndSet(annot.getTypeName(), result);
		tools.mdsd.jamopp.model.java.classifiers.Annotation proxyClass;
		IAnnotationBinding binding = annot.resolveAnnotationBinding();
		if (binding == null) {
			proxyClass = JDTResolverUtility.getAnnotation(annot.getTypeName().getFullyQualifiedName());
		} else {
			proxyClass = JDTResolverUtility.getAnnotation(binding.getAnnotationType()); 
		}
		result.setAnnotation(proxyClass);
		if (annot.isSingleMemberAnnotation()) {
			tools.mdsd.jamopp.model.java.annotations.SingleAnnotationParameter param = tools.mdsd.jamopp.model.java.annotations.AnnotationsFactory.eINSTANCE
				.createSingleAnnotationParameter();
			result.setParameter(param);
			SingleMemberAnnotation singleAnnot = (SingleMemberAnnotation) annot;
			TypeInstructionSeparationUtility.addSingleAnnotationParameter(singleAnnot.getValue(), param);
		} else if (annot.isNormalAnnotation()) {
			tools.mdsd.jamopp.model.java.annotations.AnnotationParameterList param = tools.mdsd.jamopp.model.java.annotations.AnnotationsFactory.eINSTANCE
				.createAnnotationParameterList();
			result.setParameter(param);
			NormalAnnotation normalAnnot = (NormalAnnotation) annot;
			normalAnnot.values().forEach(obj -> {
				MemberValuePair memVal = (MemberValuePair) obj;
				tools.mdsd.jamopp.model.java.annotations.AnnotationAttributeSetting attrSet = tools.mdsd.jamopp.model.java.annotations.AnnotationsFactory.eINSTANCE
					.createAnnotationAttributeSetting();
				tools.mdsd.jamopp.model.java.members.InterfaceMethod methodProxy;
				if (memVal.resolveMemberValuePairBinding() != null) {
					 methodProxy = JDTResolverUtility.getInterfaceMethod(memVal.resolveMemberValuePairBinding().getMethodBinding());
				} else {
					methodProxy = JDTResolverUtility.getInterfaceMethod(memVal.getName().getIdentifier());
					if (!proxyClass.getMembers().contains(methodProxy)) {
						proxyClass.getMembers().add(methodProxy);
					}
				}
				BaseConverterUtility.convertToSimpleNameOnlyAndSet(memVal.getName(), methodProxy);
				attrSet.setAttribute(methodProxy);
				TypeInstructionSeparationUtility.addAnnotationAttributeSetting(memVal.getValue(), attrSet);
				LayoutInformationConverter.convertToMinimalLayoutInformation(attrSet, memVal);
				param.getSettings().add(attrSet);
			});
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, annot);
		return result;
	}
	
	static tools.mdsd.jamopp.model.java.annotations.AnnotationValue convertToAnnotationValue(Expression expr) {
		if (expr instanceof Annotation) {
			return convertToAnnotationInstance((Annotation) expr);
		} else if (expr.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
			return convertToArrayInitializer((ArrayInitializer) expr);
		} else {
			return (tools.mdsd.jamopp.model.java.expressions.AssignmentExpressionChild)
				ExpressionConverterUtility.convertToExpression((Expression) expr);
		}
	}
	
	@SuppressWarnings("unchecked")
	static tools.mdsd.jamopp.model.java.arrays.ArrayInitializer convertToArrayInitializer(ArrayInitializer arr) {
		tools.mdsd.jamopp.model.java.arrays.ArrayInitializer result = tools.mdsd.jamopp.model.java.arrays.ArraysFactory.eINSTANCE.createArrayInitializer();
		arr.expressions().forEach(obj -> {
			tools.mdsd.jamopp.model.java.arrays.ArrayInitializationValue value = null;
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
