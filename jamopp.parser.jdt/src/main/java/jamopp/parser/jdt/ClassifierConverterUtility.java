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

package jamopp.parser.jdt;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

class ClassifierConverterUtility {
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.classifiers.ConcreteClassifier convertToConcreteClassifier(AbstractTypeDeclaration typeDecl) {
		org.emftext.language.java.classifiers.ConcreteClassifier result = null;
		if (typeDecl.getNodeType() == ASTNode.TYPE_DECLARATION) {
			result = convertToClassOrInterface((TypeDeclaration) typeDecl);
		} else if (typeDecl.getNodeType() == ASTNode.ANNOTATION_TYPE_DECLARATION) {
			result = JDTResolverUtility.getAnnotation(typeDecl.resolveBinding());
			org.emftext.language.java.classifiers.ConcreteClassifier fR = result;
			typeDecl.bodyDeclarations().forEach(obj -> fR.getMembers().add(convertToInterfaceMember((BodyDeclaration) obj)));
		} else { // typeDecl.getNodeType() == ASTNode.ENUM_DECLARATION
			result = convertToEnum((EnumDeclaration) typeDecl);
		}
		org.emftext.language.java.classifiers.ConcreteClassifier finalResult = result;
		typeDecl.modifiers().forEach(obj -> finalResult.getAnnotationsAndModifiers().add(AnnotationInstanceOrModifierConverterUtility
			.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(typeDecl.getName(), result);
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, typeDecl);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.classifiers.ConcreteClassifier convertToClassOrInterface(TypeDeclaration typeDecl) {
		if (typeDecl.isInterface()) {
			org.emftext.language.java.classifiers.Interface interfaceObj = JDTResolverUtility.getInterface(typeDecl.resolveBinding());
			typeDecl.typeParameters().forEach(obj -> interfaceObj.getTypeParameters().add(convertToTypeParameter((TypeParameter) obj)));
			typeDecl.superInterfaceTypes().forEach(obj -> interfaceObj.getExtends().add(BaseConverterUtility.convertToTypeReference((Type) obj)));
			typeDecl.bodyDeclarations().forEach(obj -> interfaceObj.getMembers().add(convertToInterfaceMember((BodyDeclaration) obj)));
			return interfaceObj;
		} else {
			org.emftext.language.java.classifiers.Class classObj = JDTResolverUtility.getClass(typeDecl.resolveBinding());
			typeDecl.typeParameters().forEach(obj -> classObj.getTypeParameters().add(convertToTypeParameter((TypeParameter) obj)));
			if (typeDecl.getSuperclassType() != null) {
				classObj.setExtends(BaseConverterUtility.convertToTypeReference(typeDecl.getSuperclassType()));
			}
			typeDecl.superInterfaceTypes().forEach(obj -> classObj.getImplements().add(BaseConverterUtility.convertToTypeReference((Type) obj)));
			typeDecl.bodyDeclarations().forEach(obj -> classObj.getMembers().add(convertToClassMember((BodyDeclaration) obj)));
			return classObj;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.classifiers.Enumeration convertToEnum(EnumDeclaration enumDecl) {
		org.emftext.language.java.classifiers.Enumeration result = JDTResolverUtility.getEnumeration(enumDecl.resolveBinding());
		enumDecl.superInterfaceTypes().forEach(obj -> result.getImplements().add(BaseConverterUtility.convertToTypeReference((Type) obj)));
		enumDecl.enumConstants().forEach(obj -> result.getConstants().add(convertToEnumConstant((EnumConstantDeclaration) obj)));
		enumDecl.bodyDeclarations().forEach(obj -> result.getMembers().add(convertToClassMember((BodyDeclaration) obj)));
		return result;
	}
	
	private static org.emftext.language.java.members.Member convertToInterfaceMember(BodyDeclaration body) {
		if (body.getNodeType() == ASTNode.METHOD_DECLARATION) {
			return convertToInterfaceMethodOrConstructor((MethodDeclaration) body);
		} else {
			return convertToClassMember(body);
		}
	}
	
	private static org.emftext.language.java.members.Member convertToClassMember(BodyDeclaration body) {
		if (body instanceof AbstractTypeDeclaration) {
			return convertToConcreteClassifier((AbstractTypeDeclaration) body);
		} else if (body.getNodeType() == ASTNode.INITIALIZER) {
			return convertToBlock((Initializer) body);
		} else if (body.getNodeType() == ASTNode.FIELD_DECLARATION) {
			return convertToField((FieldDeclaration) body);
		} else if (body.getNodeType() == ASTNode.METHOD_DECLARATION) {
			return convertToClassMethodOrConstructor((MethodDeclaration) body);
		} else if (body.getNodeType() == ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION) {
			return convertToInterfaceMethod((AnnotationTypeMemberDeclaration) body);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.statements.Block convertToBlock(Initializer init) {
		org.emftext.language.java.statements.Block result = org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createBlock();
		result.setName("");
		TypeInstructionSeparationUtility.addInitializer(init.getBody(), result);
		init.modifiers().forEach(obj -> result.getModifiers().add(AnnotationInstanceOrModifierConverterUtility
			.convertToModifier((Modifier) obj)));
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.Field convertToField(FieldDeclaration fieldDecl) {
		VariableDeclarationFragment firstFragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);
		org.emftext.language.java.members.Field result;
		IVariableBinding binding = firstFragment.resolveBinding();
		if (binding != null) {
			result = JDTResolverUtility.getField(binding);
		} else {
			result = JDTResolverUtility.getField(firstFragment.getName().getIdentifier());
		}
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(firstFragment.getName(), result);
		fieldDecl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(AnnotationInstanceOrModifierConverterUtility
			.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
		result.setTypeReference(BaseConverterUtility.convertToTypeReference(fieldDecl.getType()));
//		BaseConverterUtility.convertToArrayDimensionsAndSet(fieldDecl.getType(), result);
//		firstFragment.extraDimensions().forEach(obj -> BaseConverterUtility.convertToArrayDimensionAfterAndSet((Dimension) obj, result));
		if (firstFragment.getInitializer() != null) {
			TypeInstructionSeparationUtility.addField(firstFragment.getInitializer(), result);
		}
		for (int index = 1; index < fieldDecl.fragments().size(); index++) {
			result.getAdditionalFields().add(convertToAdditionalField((VariableDeclarationFragment) fieldDecl.fragments().get(index)));
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, fieldDecl);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.AdditionalField convertToAdditionalField(VariableDeclarationFragment frag) {
		org.emftext.language.java.members.AdditionalField result;
		IVariableBinding binding = frag.resolveBinding();
		if (binding != null) {
			result = JDTResolverUtility.getAdditionalField(binding);
		} else {
			result = JDTResolverUtility.getAdditionalField(frag.getName().getIdentifier());
		}
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(frag.getName(), result);
//		frag.extraDimensions().forEach(obj -> BaseConverterUtility.convertToArrayDimensionAfterAndSet((Dimension) obj, result));
		if (frag.getInitializer() != null) {
			TypeInstructionSeparationUtility.addAdditionalField(frag.getInitializer(), result);
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, frag);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.InterfaceMethod convertToInterfaceMethod(AnnotationTypeMemberDeclaration annDecl) {
		IMethodBinding binding = annDecl.resolveBinding();
		org.emftext.language.java.members.InterfaceMethod result;
		if (binding != null) {
			result = JDTResolverUtility.getInterfaceMethod(binding);
		} else {
			result = JDTResolverUtility.getInterfaceMethod(annDecl.getName().getIdentifier());
		}
		annDecl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(AnnotationInstanceOrModifierConverterUtility
			.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
		result.setTypeReference(BaseConverterUtility.convertToTypeReference(annDecl.getType()));
//		BaseConverterUtility.convertToArrayDimensionsAndSet(annDecl.getType(), result);
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(annDecl.getName(), result);
		if (annDecl.getDefault() != null) {
			TypeInstructionSeparationUtility.addAnnotationMethod(annDecl.getDefault(), result);
		}
		result.setStatement(org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createEmptyStatement());
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, annDecl);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.Member convertToInterfaceMethodOrConstructor(MethodDeclaration methodDecl) {
		if (methodDecl.isConstructor()) {
			return convertToClassMethodOrConstructor(methodDecl);
		} else {
			org.emftext.language.java.members.InterfaceMethod result;
			IMethodBinding binding = methodDecl.resolveBinding();
			if (binding == null) {
				result = JDTResolverUtility.getInterfaceMethod(methodDecl.getName().getIdentifier());
			} else {
				result = JDTResolverUtility.getInterfaceMethod(binding);
			}
			methodDecl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(AnnotationInstanceOrModifierConverterUtility
				.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
			methodDecl.typeParameters().forEach(obj -> result.getTypeParameters().add(convertToTypeParameter((TypeParameter) obj)));
			result.setTypeReference(BaseConverterUtility.convertToTypeReference(methodDecl.getReturnType2()));
//			BaseConverterUtility.convertToArrayDimensionsAndSet(methodDecl.getReturnType2(), result);
//			methodDecl.extraDimensions().forEach(obj -> BaseConverterUtility.convertToArrayDimensionAfterAndSet((Dimension) obj, result));
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(methodDecl.getName(), result);
			if (methodDecl.getReceiverType() != null) {
				result.getParameters().add(convertToReceiverParameter(methodDecl));
			}
			methodDecl.parameters().forEach(obj -> result.getParameters().add(convertToParameter((SingleVariableDeclaration) obj)));
			methodDecl.thrownExceptionTypes().forEach(obj -> result.getExceptions().add(
				wrapInNamespaceClassifierReference(BaseConverterUtility.convertToTypeReference((Type) obj))));
			if (methodDecl.getBody() != null) {
				TypeInstructionSeparationUtility.addMethod(methodDecl.getBody(), result);
			} else {
				result.setStatement(org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createEmptyStatement());
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, methodDecl);
			return result;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.Member convertToClassMethodOrConstructor(MethodDeclaration methodDecl) {
		if (methodDecl.isConstructor()) {
			org.emftext.language.java.members.Constructor result;
			IMethodBinding binding = methodDecl.resolveBinding();
			if (binding == null) {
				result = JDTResolverUtility.getConstructor(methodDecl.getName().getIdentifier());
			} else {
				result = JDTResolverUtility.getConstructor(binding);
			}
			methodDecl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(AnnotationInstanceOrModifierConverterUtility
				.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
			methodDecl.typeParameters().forEach(obj -> result.getTypeParameters().add(convertToTypeParameter((TypeParameter) obj)));
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(methodDecl.getName(), result);
			if (methodDecl.getReceiverType() != null) {
				result.getParameters().add(convertToReceiverParameter(methodDecl));
			}
			methodDecl.parameters().forEach(obj -> result.getParameters().add(convertToParameter((SingleVariableDeclaration) obj)));
			methodDecl.thrownExceptionTypes().forEach(obj -> result.getExceptions().add(
				wrapInNamespaceClassifierReference(BaseConverterUtility.convertToTypeReference((Type) obj))));
			TypeInstructionSeparationUtility.addConstructor(methodDecl.getBody(), result);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, methodDecl);
			return result;
		} else {
			org.emftext.language.java.members.ClassMethod result;
			IMethodBinding binding = methodDecl.resolveBinding();
			if (binding != null) {
				result = JDTResolverUtility.getClassMethod(binding);
			} else {
				result = JDTResolverUtility.getClassMethod(methodDecl.getName().getIdentifier());
			}
			methodDecl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(AnnotationInstanceOrModifierConverterUtility
				.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
			methodDecl.typeParameters().forEach(obj -> result.getTypeParameters().add(convertToTypeParameter((TypeParameter) obj)));
			result.setTypeReference(BaseConverterUtility.convertToTypeReference(methodDecl.getReturnType2()));
//			BaseConverterUtility.convertToArrayDimensionsAndSet(methodDecl.getReturnType2(), result);
//			methodDecl.extraDimensions().forEach(obj -> BaseConverterUtility.convertToArrayDimensionAfterAndSet((Dimension) obj, result));
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(methodDecl.getName(), result);
			if (methodDecl.getReceiverType() != null) {
				result.getParameters().add(convertToReceiverParameter(methodDecl));
			}
			methodDecl.parameters().forEach(obj -> result.getParameters().add(convertToParameter((SingleVariableDeclaration) obj)));
			methodDecl.thrownExceptionTypes().forEach(obj -> result.getExceptions().add(
				wrapInNamespaceClassifierReference(BaseConverterUtility.convertToTypeReference((Type) obj))));
			if (methodDecl.getBody() != null) {
				TypeInstructionSeparationUtility.addMethod(methodDecl.getBody(), result);
			} else {
				result.setStatement(org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createEmptyStatement());
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, methodDecl);
			return result;
		}
	}
	
	private static org.emftext.language.java.types.NamespaceClassifierReference wrapInNamespaceClassifierReference(org.emftext.language.java.types.TypeReference ref) {
		if (ref instanceof org.emftext.language.java.types.NamespaceClassifierReference) {
			return (org.emftext.language.java.types.NamespaceClassifierReference) ref;
		} else if (ref instanceof org.emftext.language.java.types.ClassifierReference) {
			org.emftext.language.java.types.NamespaceClassifierReference result = org.emftext.language.java.types.TypesFactory.eINSTANCE.createNamespaceClassifierReference();
			result.getClassifierReferences().add((org.emftext.language.java.types.ClassifierReference) ref);
			return result;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.EnumConstant convertToEnumConstant(EnumConstantDeclaration enDecl) {
		org.emftext.language.java.members.EnumConstant result;
		IVariableBinding binding = enDecl.resolveVariable();
		if (binding == null) {
			result = JDTResolverUtility.getEnumConstant(enDecl.getName().getIdentifier());
		} else {
			result = JDTResolverUtility.getEnumConstant(binding);
		}
		enDecl.modifiers().forEach(obj -> result.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
			.convertToAnnotationInstance((Annotation) obj)));
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(enDecl.getName(), result);
		enDecl.arguments().forEach(obj -> result.getArguments().add(ExpressionConverterUtility.convertToExpression((Expression) obj)));
		if (enDecl.getAnonymousClassDeclaration() != null) {
			result.setAnonymousClass(convertToAnonymousClass(enDecl.getAnonymousClassDeclaration()));
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, enDecl);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.generics.TypeParameter convertToTypeParameter(TypeParameter param) {
		org.emftext.language.java.generics.TypeParameter result = JDTResolverUtility.getTypeParameter(param.resolveBinding());
		param.modifiers().forEach(obj -> result.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility.convertToAnnotationInstance((Annotation) obj)));
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(param.getName(), result);
		param.typeBounds().forEach(obj -> result.getExtendTypes().add(BaseConverterUtility.convertToTypeReference((Type) obj)));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, param);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.classifiers.AnonymousClass convertToAnonymousClass(AnonymousClassDeclaration anon) {
		ITypeBinding binding = anon.resolveBinding();
		org.emftext.language.java.classifiers.AnonymousClass result;
		if (binding != null) {
			result = JDTResolverUtility.getAnonymousClass(binding);
		} else {
			result = JDTResolverUtility.getAnonymousClass("" + anon.hashCode());
		}
		anon.bodyDeclarations().forEach(obj -> result.getMembers().add(convertToClassMember((BodyDeclaration) obj)));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, anon);
		return result;
	}
	
	private static org.emftext.language.java.parameters.ReceiverParameter convertToReceiverParameter(MethodDeclaration methodDecl) {
		org.emftext.language.java.parameters.ReceiverParameter result = org.emftext.language.java.parameters.ParametersFactory.eINSTANCE.createReceiverParameter();
		result.setName("");
		result.setTypeReference(BaseConverterUtility.convertToTypeReference(methodDecl.getReceiverType()));
		if (methodDecl.getReceiverQualifier() != null) {
			result.setOuterTypeReference(BaseConverterUtility.convertToClassifierReference(methodDecl.getReceiverQualifier()));
		}
		result.setThisReference(org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createThis());
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.parameters.Parameter convertToParameter(SingleVariableDeclaration decl) {
		if (decl.isVarargs()) {
			org.emftext.language.java.parameters.VariableLengthParameter result = JDTResolverUtility.getVariableLengthParameter(decl.resolveBinding());
			decl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(AnnotationInstanceOrModifierConverterUtility
				.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
			result.setTypeReference(BaseConverterUtility.convertToTypeReference(decl.getType()));
//			BaseConverterUtility.convertToArrayDimensionsAndSet(decl.getType(), result);
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(decl.getName(), result);
//			decl.extraDimensions().forEach(obj -> BaseConverterUtility.convertToArrayDimensionAfterAndSet((Dimension) obj, result));
			decl.varargsAnnotations().forEach(obj -> result.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, decl);
			return result;
		} else {
			return convertToOrdinaryParameter(decl);
		}
	}
	
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.parameters.OrdinaryParameter convertToOrdinaryParameter(SingleVariableDeclaration decl) {
		org.emftext.language.java.parameters.OrdinaryParameter result = JDTResolverUtility.getOrdinaryParameter(decl.resolveBinding());
		decl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(AnnotationInstanceOrModifierConverterUtility
			.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
		result.setTypeReference(BaseConverterUtility.convertToTypeReference(decl.getType()));
//		BaseConverterUtility.convertToArrayDimensionsAndSet(decl.getType(), result);
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(decl.getName(), result);
//		decl.extraDimensions().forEach(obj -> BaseConverterUtility.convertToArrayDimensionAfterAndSet((Dimension) obj, result));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, decl);
		return result;
	}
}
