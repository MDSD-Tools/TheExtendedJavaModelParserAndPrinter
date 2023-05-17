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
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import jamopp.options.ParserOptions;
import jamopp.proxy.IJavaContextDependentURIFragmentCollector;

class ClassifierConverterUtility {
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.classifiers.ConcreteClassifier convertToConcreteClassifier(
			AbstractTypeDeclaration typeDecl) {
		org.emftext.language.java.classifiers.ConcreteClassifier result = null;
		if (typeDecl.getNodeType() == ASTNode.TYPE_DECLARATION) {
			result = convertToClassOrInterface((TypeDeclaration) typeDecl);
		} else if (typeDecl.getNodeType() == ASTNode.ANNOTATION_TYPE_DECLARATION) {
			result = org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createAnnotation();
			org.emftext.language.java.classifiers.ConcreteClassifier fR = result;
			typeDecl.bodyDeclarations().forEach(obj ->
				fR.getMembers().add(convertToInterfaceMember((BodyDeclaration) obj)));
		} else { // typeDecl.getNodeType() == ASTNode.ENUM_DECLARATION
			result = convertToEnum((EnumDeclaration) typeDecl);
		}
		org.emftext.language.java.classifiers.ConcreteClassifier finalResult = result;
		typeDecl.modifiers().forEach(obj -> finalResult.getAnnotationsAndModifiers().add(
			AnnotationInstanceOrModifierConverterUtility
			.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(typeDecl.getName(), result);
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, typeDecl);
		if (ParserOptions.PREFER_BINDING_CONVERSION.isTrue()) {
			ITypeBinding typeBinding = typeDecl.resolveBinding();
			if (typeBinding != null && !typeBinding.isRecovered()) {
				IPackageBinding packBinding = typeBinding.getPackage();
				if (packBinding != null && !packBinding.isRecovered()) {
					org.emftext.language.java.containers.Package packProxy =
						org.emftext.language.java.containers.ContainersFactory.eINSTANCE.createPackage();
					for (String nsPart : packBinding.getNameComponents()) {
						packProxy.getNamespaces().add(nsPart);
					}
					packProxy.setName("");
					IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE
							.registerContextDependentURIFragment(result,
							org.emftext.language.java.classifiers
							.ClassifiersPackage.Literals.CONCRETE_CLASSIFIER__PACKAGE,
							packBinding.getName(), packProxy, -1, packBinding);
					result.setPackage(packProxy);
				}
			}
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.classifiers.ConcreteClassifier
			convertToClassOrInterface(TypeDeclaration typeDecl) {
		org.emftext.language.java.classifiers.ConcreteClassifier result;
		if (typeDecl.isInterface()) {
			org.emftext.language.java.classifiers.Interface interfaceObj =
					org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createInterface();
			typeDecl.superInterfaceTypes().forEach(obj -> interfaceObj.getExtends().add(
					BaseConverterUtility.convertToTypeReference((Type) obj)));
			typeDecl.bodyDeclarations().forEach(obj ->
			interfaceObj.getMembers().add(convertToInterfaceMember((BodyDeclaration) obj)));
			result = interfaceObj;
		} else {
			org.emftext.language.java.classifiers.Class classObj =
					org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createClass();
			if (typeDecl.getSuperclassType() != null) {
				classObj.setExtends(BaseConverterUtility.convertToTypeReference(typeDecl.getSuperclassType()));
			}
			typeDecl.superInterfaceTypes().forEach(obj -> classObj.getImplements().add(
					BaseConverterUtility.convertToTypeReference((Type) obj)));
			typeDecl.bodyDeclarations().forEach(obj -> classObj.getMembers().add(
					convertToClassMember((BodyDeclaration) obj)));
			result = classObj;
		}
		typeDecl.typeParameters().forEach(obj -> result.getTypeParameters().add(
				convertToTypeParameter((TypeParameter) obj)));
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.classifiers.Enumeration convertToEnum(EnumDeclaration enumDecl) {
		org.emftext.language.java.classifiers.Enumeration result =
				org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createEnumeration();
		enumDecl.superInterfaceTypes().forEach(obj -> result.getImplements().add(
				BaseConverterUtility.convertToTypeReference((Type) obj)));
		enumDecl.enumConstants().forEach(obj -> result.getConstants().add(
				convertToEnumConstant((EnumConstantDeclaration) obj)));
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
		org.emftext.language.java.statements.Block result = StatementConverterUtility.convertToBlock(init.getBody());
		result.setName("");
		init.modifiers().forEach(obj -> result.getModifiers().add(AnnotationInstanceOrModifierConverterUtility
			.convertToModifier((Modifier) obj)));
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.Field convertToField(FieldDeclaration fieldDecl) {
		org.emftext.language.java.members.Field result =
				org.emftext.language.java.members.MembersFactory.eINSTANCE.createField();
		fieldDecl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(
			AnnotationInstanceOrModifierConverterUtility
			.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
		result.setTypeReference(BaseConverterUtility.convertToTypeReference(fieldDecl.getType()));
		BaseConverterUtility.convertToArrayDimensionsAndSet(fieldDecl.getType(), result.getTypeReference());
		VariableDeclarationFragment firstFragment = (VariableDeclarationFragment) fieldDecl.fragments().get(0);
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(firstFragment.getName(), result);
		if (firstFragment.getInitializer() != null) {
			result.setInitialValue(ExpressionConverterUtility.convertToExpression(firstFragment.getInitializer()));
		}
		for (int index = 1; index < fieldDecl.fragments().size(); index++) {
			result.getAdditionalFields().add(convertToAdditionalField(
					(VariableDeclarationFragment) fieldDecl.fragments().get(index),
					fieldDecl.getType()));
		}
		firstFragment.extraDimensions().forEach(obj ->
			BaseConverterUtility.convertToArrayDimensionAfterAndSet((Dimension) obj,
				result.getTypeReference()));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, fieldDecl);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.AdditionalField
			convertToAdditionalField(VariableDeclarationFragment frag, Type type) {
		org.emftext.language.java.members.AdditionalField result =
				org.emftext.language.java.members.MembersFactory.eINSTANCE.createAdditionalField();
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(frag.getName(), result);
		result.setTypeReference(BaseConverterUtility.convertToTypeReference(type));
		BaseConverterUtility.convertToArrayDimensionsAndSet(type, result.getTypeReference());
		frag.extraDimensions().forEach(obj -> BaseConverterUtility
				.convertToArrayDimensionAfterAndSet((Dimension) obj, result.getTypeReference()));
		if (frag.getInitializer() != null) {
			result.setInitialValue(ExpressionConverterUtility.convertToExpression(frag.getInitializer()));
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, frag);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.InterfaceMethod
			convertToInterfaceMethod(AnnotationTypeMemberDeclaration annDecl) {
		org.emftext.language.java.members.InterfaceMethod result =
				org.emftext.language.java.members.MembersFactory.eINSTANCE.createInterfaceMethod();
		annDecl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(
			AnnotationInstanceOrModifierConverterUtility
			.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
		result.setTypeReference(BaseConverterUtility.convertToTypeReference(annDecl.getType()));
		BaseConverterUtility.convertToArrayDimensionsAndSet(annDecl.getType(), result.getTypeReference());
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(annDecl.getName(), result);
		if (annDecl.getDefault() != null) {
			result.setDefaultValue(AnnotationInstanceOrModifierConverterUtility
					.convertToAnnotationValue(annDecl.getDefault()));
		}
		result.setStatement(org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createEmptyStatement());
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, annDecl);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.Member
			convertToInterfaceMethodOrConstructor(MethodDeclaration methodDecl) {
		if (methodDecl.isConstructor()) {
			return convertToClassMethodOrConstructor(methodDecl);
		} else {
			org.emftext.language.java.members.InterfaceMethod result =
					org.emftext.language.java.members.MembersFactory.eINSTANCE.createInterfaceMethod();
			methodDecl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(
				AnnotationInstanceOrModifierConverterUtility
				.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
			methodDecl.typeParameters().forEach(obj -> result.getTypeParameters().add(
					convertToTypeParameter((TypeParameter) obj)));
			result.setTypeReference(BaseConverterUtility.convertToTypeReference(methodDecl.getReturnType2()));
			BaseConverterUtility.convertToArrayDimensionsAndSet(methodDecl.getReturnType2(),
					result.getTypeReference());
			methodDecl.extraDimensions().forEach(obj -> BaseConverterUtility
					.convertToArrayDimensionAfterAndSet((Dimension) obj, result.getTypeReference()));
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(methodDecl.getName(), result);
			if (methodDecl.getReceiverType() != null) {
				result.getParameters().add(convertToReceiverParameter(methodDecl));
			}
			methodDecl.parameters().forEach(obj -> result.getParameters()
					.add(convertToParameter((SingleVariableDeclaration) obj)));
			methodDecl.thrownExceptionTypes().forEach(obj -> result.getExceptions().add(
				wrapInNamespaceClassifierReference(BaseConverterUtility.convertToTypeReference((Type) obj))));
			if (methodDecl.getBody() != null) {
				result.setStatement(StatementConverterUtility.convertToBlock(methodDecl.getBody()));
			} else {
				result.setStatement(org.emftext.language.java.statements
						.StatementsFactory.eINSTANCE.createEmptyStatement());
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, methodDecl);
			return result;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.Member convertToClassMethodOrConstructor(MethodDeclaration methodDecl) {
		if (methodDecl.isConstructor()) {
			org.emftext.language.java.members.Constructor result =
					org.emftext.language.java.members.MembersFactory.eINSTANCE.createConstructor();
			methodDecl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(
				AnnotationInstanceOrModifierConverterUtility
				.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
			methodDecl.typeParameters().forEach(obj -> result.getTypeParameters().add(
					convertToTypeParameter((TypeParameter) obj)));
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(methodDecl.getName(), result);
			if (methodDecl.getReceiverType() != null) {
				result.getParameters().add(convertToReceiverParameter(methodDecl));
			}
			methodDecl.parameters().forEach(obj -> result.getParameters()
					.add(convertToParameter((SingleVariableDeclaration) obj)));
			methodDecl.thrownExceptionTypes().forEach(obj -> result.getExceptions().add(
				wrapInNamespaceClassifierReference(BaseConverterUtility.convertToTypeReference((Type) obj))));
			result.setBlock(StatementConverterUtility.convertToBlock(methodDecl.getBody()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, methodDecl);
			return result;
		} else {
			org.emftext.language.java.members.ClassMethod result =
					org.emftext.language.java.members.MembersFactory.eINSTANCE.createClassMethod();
			methodDecl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(
				AnnotationInstanceOrModifierConverterUtility
				.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
			methodDecl.typeParameters().forEach(obj -> result.getTypeParameters()
					.add(convertToTypeParameter((TypeParameter) obj)));
			result.setTypeReference(BaseConverterUtility.convertToTypeReference(methodDecl.getReturnType2()));
			BaseConverterUtility.convertToArrayDimensionsAndSet(methodDecl.getReturnType2(),
					result.getTypeReference());
			methodDecl.extraDimensions().forEach(obj -> BaseConverterUtility
					.convertToArrayDimensionAfterAndSet((Dimension) obj, result.getTypeReference()));
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(methodDecl.getName(), result);
			if (methodDecl.getReceiverType() != null) {
				result.getParameters().add(convertToReceiverParameter(methodDecl));
			}
			methodDecl.parameters().forEach(obj -> result.getParameters().add(
					convertToParameter((SingleVariableDeclaration) obj)));
			methodDecl.thrownExceptionTypes().forEach(obj -> result.getExceptions().add(
				wrapInNamespaceClassifierReference(BaseConverterUtility.convertToTypeReference((Type) obj))));
			if (methodDecl.getBody() != null) {
				result.setStatement(StatementConverterUtility.convertToBlock(methodDecl.getBody()));
			} else {
				result.setStatement(org.emftext.language.java.statements.StatementsFactory
						.eINSTANCE.createEmptyStatement());
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, methodDecl);
			return result;
		}
	}
	
	private static org.emftext.language.java.types.NamespaceClassifierReference
			wrapInNamespaceClassifierReference(org.emftext.language.java.types.TypeReference ref) {
		if (ref instanceof org.emftext.language.java.types.NamespaceClassifierReference) {
			return (org.emftext.language.java.types.NamespaceClassifierReference) ref;
		} else if (ref instanceof org.emftext.language.java.types.ClassifierReference) {
			org.emftext.language.java.types.NamespaceClassifierReference result = org.emftext.language
					.java.types.TypesFactory.eINSTANCE.createNamespaceClassifierReference();
			result.getClassifierReferences().add((org.emftext.language.java.types.ClassifierReference) ref);
			return result;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.members.EnumConstant convertToEnumConstant(EnumConstantDeclaration enDecl) {
		org.emftext.language.java.members.EnumConstant result =
				org.emftext.language.java.members.MembersFactory.eINSTANCE.createEnumConstant();
		enDecl.modifiers().forEach(obj -> result.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
			.convertToAnnotationInstance((Annotation) obj)));
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(enDecl.getName(), result);
		enDecl.arguments().forEach(obj -> result.getArguments().add(
				ExpressionConverterUtility.convertToExpression((Expression) obj)));
		if (enDecl.getAnonymousClassDeclaration() != null) {
			result.setAnonymousClass(convertToAnonymousClass(enDecl.getAnonymousClassDeclaration()));
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, enDecl);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.generics.TypeParameter convertToTypeParameter(TypeParameter param) {
		org.emftext.language.java.generics.TypeParameter result =
				org.emftext.language.java.generics.GenericsFactory.eINSTANCE.createTypeParameter();
		param.modifiers().forEach(obj -> result.getAnnotations().add(
				AnnotationInstanceOrModifierConverterUtility.convertToAnnotationInstance((Annotation) obj)));
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(param.getName(), result);
		param.typeBounds().forEach(obj -> result.getExtendTypes().add(
				BaseConverterUtility.convertToTypeReference((Type) obj)));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, param);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.classifiers.AnonymousClass convertToAnonymousClass(AnonymousClassDeclaration anon) {
		org.emftext.language.java.classifiers.AnonymousClass result =
				org.emftext.language.java.classifiers.ClassifiersFactory.eINSTANCE.createAnonymousClass();
		anon.bodyDeclarations().forEach(obj -> result.getMembers().add(convertToClassMember((BodyDeclaration) obj)));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, anon);
		return result;
	}
	
	private static org.emftext.language.java.parameters.ReceiverParameter
			convertToReceiverParameter(MethodDeclaration methodDecl) {
		org.emftext.language.java.parameters.ReceiverParameter result =
				org.emftext.language.java.parameters.ParametersFactory.eINSTANCE.createReceiverParameter();
		result.setName("");
		result.setTypeReference(BaseConverterUtility.convertToTypeReference(methodDecl.getReceiverType()));
		if (methodDecl.getReceiverQualifier() != null) {
			result.setOuterTypeReference(BaseConverterUtility
					.convertToClassifierReference(methodDecl.getReceiverQualifier()));
		}
		result.setThisReference(org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createThis());
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.parameters.Parameter convertToParameter(SingleVariableDeclaration decl) {
		if (decl.isVarargs()) {
			org.emftext.language.java.parameters.VariableLengthParameter result = org.emftext.language
					.java.parameters.ParametersFactory.eINSTANCE.createVariableLengthParameter();
			decl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(
				AnnotationInstanceOrModifierConverterUtility
				.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
			result.setTypeReference(BaseConverterUtility.convertToTypeReference(decl.getType()));
			BaseConverterUtility.convertToArrayDimensionsAndSet(decl.getType(), result.getTypeReference());
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(decl.getName(), result);
			decl.extraDimensions().forEach(obj -> BaseConverterUtility
					.convertToArrayDimensionAfterAndSet((Dimension) obj, result.getTypeReference()));
			decl.varargsAnnotations().forEach(obj -> result.getAnnotations().add(
				AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, decl);
			return result;
		} else {
			return convertToOrdinaryParameter(decl);
		}
	}
	
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.parameters.OrdinaryParameter convertToOrdinaryParameter(SingleVariableDeclaration decl) {
		org.emftext.language.java.parameters.OrdinaryParameter result =
				org.emftext.language.java.parameters.ParametersFactory.eINSTANCE.createOrdinaryParameter();
		decl.modifiers().forEach(obj -> result.getAnnotationsAndModifiers().add(
			AnnotationInstanceOrModifierConverterUtility
			.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
		result.setTypeReference(BaseConverterUtility.convertToTypeReference(decl.getType()));
		BaseConverterUtility.convertToArrayDimensionsAndSet(decl.getType(), result.getTypeReference());
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(decl.getName(), result);
		decl.extraDimensions().forEach(obj -> BaseConverterUtility
				.convertToArrayDimensionAfterAndSet((Dimension) obj, result.getTypeReference()));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, decl);
		return result;
	}
}
