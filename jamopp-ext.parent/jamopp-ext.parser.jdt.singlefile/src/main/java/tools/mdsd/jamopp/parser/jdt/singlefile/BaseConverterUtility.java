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

package tools.mdsd.jamopp.parser.jdt.singlefile;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WildcardType;

import tools.mdsd.jamopp.options.ParserOptions;
import tools.mdsd.jamopp.proxy.IJavaContextDependentURIFragmentCollector;
import tools.mdsd.jamopp.resolution.bindings.JDTBindingConverterUtility;

class BaseConverterUtility {
	static tools.mdsd.jamopp.model.java.types.TypeReference convertToClassifierOrNamespaceClassifierReference(Name name) {
		if (name.isSimpleName()) {
			return convertToClassifierReference((SimpleName) name);
		} else { // name.isQualifiedName()
			QualifiedName qualifiedName = (QualifiedName) name;
			tools.mdsd.jamopp.model.java.types.NamespaceClassifierReference ref = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createNamespaceClassifierReference();
			ref.getClassifierReferences().add(convertToClassifierReference(qualifiedName.getName()));
			convertToNamespacesAndSet(qualifiedName.getQualifier(), ref);
			return ref;
		}
	}
	
	static tools.mdsd.jamopp.model.java.types.ClassifierReference convertToClassifierReference(SimpleName simpleName) {
		tools.mdsd.jamopp.model.java.types.ClassifierReference ref =
				tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createClassifierReference();
		tools.mdsd.jamopp.model.java.classifiers.Class proxy =
				tools.mdsd.jamopp.model.java.classifiers.ClassifiersFactory.eINSTANCE.createClass();
		proxy.setName(simpleName.getIdentifier());
		IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.registerContextDependentURIFragment(
			ref, tools.mdsd.jamopp.model.java.types.TypesPackage.Literals.CLASSIFIER_REFERENCE__TARGET,
			proxy.getName(), proxy, -1, simpleName.resolveBinding());
		ref.setTarget(proxy);
		return ref;
	}
	
	static void convertToNamespacesAndSimpleNameAndSet(Name name,
			tools.mdsd.jamopp.model.java.commons.NamespaceAwareElement namespaceElement,
			tools.mdsd.jamopp.model.java.commons.NamedElement namedElement) {
		if (name.isSimpleName()) {
			namedElement.setName(((SimpleName) name).getIdentifier());
		} else if (name.isQualifiedName()) {
			QualifiedName qualifiedName = (QualifiedName) name;
			namedElement.setName(qualifiedName.getName().getIdentifier());
			convertToNamespacesAndSet(qualifiedName.getQualifier(), namespaceElement);
		}
	}
	
	static void convertToNamespacesAndSet(Name name,
			tools.mdsd.jamopp.model.java.commons.NamespaceAwareElement namespaceElement) {
		if (name.isSimpleName()) {
			SimpleName simpleName = (SimpleName) name;
			namespaceElement.getNamespaces().add(0, simpleName.getIdentifier());
		} else if (name.isQualifiedName()) {
			QualifiedName qualifiedName = (QualifiedName) name;
			namespaceElement.getNamespaces().add(0, qualifiedName.getName().getIdentifier());
			convertToNamespacesAndSet(qualifiedName.getQualifier(), namespaceElement);
		}
	}
	
	static void convertToSimpleNameOnlyAndSet(Name name, tools.mdsd.jamopp.model.java.commons.NamedElement namedElement) {
		if (name.isSimpleName()) {
			SimpleName simpleName = (SimpleName) name;
			namedElement.setName(simpleName.getIdentifier());
		} else { // name.isQualifiedName()
			QualifiedName qualifiedName = (QualifiedName) name;
			namedElement.setName(qualifiedName.getName().getIdentifier());
		}
	}
	
	@SuppressWarnings("unchecked")
	static tools.mdsd.jamopp.model.java.types.TypeReference convertToTypeReference(Type t) {
		if (t.isPrimitiveType()) {
			PrimitiveType primType = (PrimitiveType) t;
			tools.mdsd.jamopp.model.java.types.PrimitiveType convertedType;
			if (primType.getPrimitiveTypeCode() == PrimitiveType.BOOLEAN) {
				convertedType = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createBoolean();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.BYTE) {
				convertedType = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createByte();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.CHAR) {
				convertedType = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createChar();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.DOUBLE) {
				convertedType = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createDouble();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.FLOAT) {
				convertedType = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createFloat();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.INT) {
				convertedType = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createInt();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.LONG) {
				convertedType = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createLong();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.SHORT) {
				convertedType = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createShort();
			} else { // primType.getPrimitiveTypeCode() == PrimitiveType.VOID
				convertedType = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createVoid();
			}
			primType.annotations().forEach(obj -> convertedType.getAnnotations().add(
				AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedType, primType);
			return convertedType;
		} else if (t.isVar()) {
			tools.mdsd.jamopp.model.java.types.InferableType ref =
					tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createInferableType();
			ITypeBinding b = t.resolveBinding();
			if (b != null && !b.isRecovered()
					&& ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.isTrue()) {
				ref.getActualTargets().addAll(JDTBindingConverterUtility
						.convertToTypeReferences(b));
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(ref, t);
			return ref;
		} else if (t.isArrayType()) {
			ArrayType arrT = (ArrayType) t;
			return convertToTypeReference(arrT.getElementType());
		} else if (t.isSimpleType()) {
			SimpleType simT = (SimpleType) t;
			tools.mdsd.jamopp.model.java.types.TypeReference ref;
			if (simT.annotations().size() > 0) {
				tools.mdsd.jamopp.model.java.types.ClassifierReference tempRef =
						convertToClassifierReference((SimpleName) simT.getName());
				simT.annotations().forEach(obj -> tempRef.getAnnotations().add(
					AnnotationInstanceOrModifierConverterUtility
					.convertToAnnotationInstance((Annotation) obj)));
				ref = tempRef;
			} else {
				ref = convertToClassifierOrNamespaceClassifierReference(simT.getName());
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(ref, simT);
			return ref;
		} else if (t.isQualifiedType()) {
			QualifiedType qualType = (QualifiedType) t;
			tools.mdsd.jamopp.model.java.types.NamespaceClassifierReference result;
			tools.mdsd.jamopp.model.java.types.TypeReference parentRef = convertToTypeReference(qualType.getQualifier());
			if (parentRef instanceof tools.mdsd.jamopp.model.java.types.ClassifierReference) {
				result = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE
						.createNamespaceClassifierReference();
				result.getClassifierReferences().add(
						(tools.mdsd.jamopp.model.java.types.ClassifierReference) parentRef);
			} else { // parentRef instanceof tools.mdsd.jamopp.model.java.types.NamespaceClassifierReference
				result = (tools.mdsd.jamopp.model.java.types.NamespaceClassifierReference) parentRef;
			}
			tools.mdsd.jamopp.model.java.types.ClassifierReference childRef =
					convertToClassifierReference(qualType.getName());
			qualType.annotations().forEach(obj -> childRef.getAnnotations().add(
				AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			result.getClassifierReferences().add(childRef);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, qualType);
			return result;
		} else if (t.isNameQualifiedType()) {
			NameQualifiedType nqT = (NameQualifiedType) t;
			tools.mdsd.jamopp.model.java.types.NamespaceClassifierReference result = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createNamespaceClassifierReference();
			tools.mdsd.jamopp.model.java.types.TypeReference parentRef =
					convertToClassifierOrNamespaceClassifierReference(nqT.getQualifier());
			if (parentRef instanceof tools.mdsd.jamopp.model.java.types.ClassifierReference) {
				result = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE
						.createNamespaceClassifierReference();
				result.getClassifierReferences().add(
						(tools.mdsd.jamopp.model.java.types.ClassifierReference) parentRef);
			} else {
				result = (tools.mdsd.jamopp.model.java.types.NamespaceClassifierReference) parentRef;
			}
			tools.mdsd.jamopp.model.java.types.ClassifierReference child = convertToClassifierReference(nqT.getName());
			nqT.annotations().forEach(obj -> child.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			result.getClassifierReferences().add(child);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, nqT);
			return result;
		} else if (t.isParameterizedType()) {
			ParameterizedType paramT = (ParameterizedType) t;
			tools.mdsd.jamopp.model.java.types.TypeReference ref = convertToTypeReference(paramT.getType());
			tools.mdsd.jamopp.model.java.types.ClassifierReference container;
			if (ref instanceof tools.mdsd.jamopp.model.java.types.ClassifierReference) {
				container = (tools.mdsd.jamopp.model.java.types.ClassifierReference) ref;
			} else {
				tools.mdsd.jamopp.model.java.types.NamespaceClassifierReference containerContainer =
						(tools.mdsd.jamopp.model.java.types.NamespaceClassifierReference) ref;
				container = containerContainer.getClassifierReferences()
						.get(containerContainer.getClassifierReferences().size() - 1);
			}
			paramT.typeArguments().forEach(obj -> container.getTypeArguments()
					.add(convertToTypeArgument((Type) obj)));
			return ref;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	static tools.mdsd.jamopp.model.java.generics.TypeArgument convertToTypeArgument(Type t) {
		if (t.isWildcardType()) {
			WildcardType wildType = (WildcardType) t;
			if (wildType.getBound() == null) {
				tools.mdsd.jamopp.model.java.generics.UnknownTypeArgument result = tools.mdsd.jamopp.model.java.generics.GenericsFactory.eINSTANCE.createUnknownTypeArgument();
				wildType.annotations().forEach(obj -> result.getAnnotations().add(
					AnnotationInstanceOrModifierConverterUtility
					.convertToAnnotationInstance((Annotation) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, wildType);
				return result;
			} else if (wildType.isUpperBound()) {
				tools.mdsd.jamopp.model.java.generics.ExtendsTypeArgument result = tools.mdsd.jamopp.model.java.generics.GenericsFactory.eINSTANCE.createExtendsTypeArgument();
				wildType.annotations().forEach(obj -> result.getAnnotations().add(
					AnnotationInstanceOrModifierConverterUtility
					.convertToAnnotationInstance((Annotation) obj)));
				result.setExtendType(convertToTypeReference(wildType.getBound()));
				convertToArrayDimensionsAndSet(wildType.getBound(), result);
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, wildType);
				return result;
			} else {
				tools.mdsd.jamopp.model.java.generics.SuperTypeArgument result = tools.mdsd.jamopp.model.java.generics.GenericsFactory.eINSTANCE.createSuperTypeArgument();
				wildType.annotations().forEach(obj -> result.getAnnotations().add(
					AnnotationInstanceOrModifierConverterUtility
					.convertToAnnotationInstance((Annotation) obj)));
				result.setSuperType(convertToTypeReference(wildType.getBound()));
				convertToArrayDimensionsAndSet(wildType.getBound(), result);
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, wildType);
				return result;
			}
		} else {
			tools.mdsd.jamopp.model.java.generics.QualifiedTypeArgument result = tools.mdsd.jamopp.model.java.generics.GenericsFactory.eINSTANCE.createQualifiedTypeArgument();
			result.setTypeReference(convertToTypeReference(t));
			convertToArrayDimensionsAndSet(t, result);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, t);
			return result;
		}
	}
	
	static void convertToArrayDimensionsAndSet(Type t, tools.mdsd.jamopp.model.java.arrays.ArrayTypeable arrDimContainer) {
		convertToArrayDimensionsAndSet(t, arrDimContainer, 0);
	}
	
	static void convertToArrayDimensionsAndSet(Type t, tools.mdsd.jamopp.model.java.arrays.ArrayTypeable arrDimContainer,
			int ignoreDimensions) {
		if (t.isArrayType()) {
			ArrayType arrT = (ArrayType) t;
			for (int i = ignoreDimensions; i < arrT.dimensions().size(); i++) {
				arrDimContainer.getArrayDimensionsBefore().add(convertToArrayDimension((Dimension)
					arrT.dimensions().get(i)));
			}
		}
	}
	
	static void convertToArrayDimensionAfterAndSet(Dimension dim,
			tools.mdsd.jamopp.model.java.arrays.ArrayTypeable arrDimContainer) {
		arrDimContainer.getArrayDimensionsAfter().add(convertToArrayDimension(dim));
	}
	
	@SuppressWarnings("unchecked")
	private static tools.mdsd.jamopp.model.java.arrays.ArrayDimension convertToArrayDimension(Dimension dim) {
		tools.mdsd.jamopp.model.java.arrays.ArrayDimension result =
				tools.mdsd.jamopp.model.java.arrays.ArraysFactory.eINSTANCE.createArrayDimension();
		dim.annotations().forEach(annot -> result.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
			.convertToAnnotationInstance((Annotation) annot)));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, dim);
		return result;
	}
}
