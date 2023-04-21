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

class BaseConverterUtility {
	static org.emftext.language.java.types.TypeReference convertToClassifierOrNamespaceClassifierReference(Name name) {
		if (name.isSimpleName()) {
			return convertToClassifierReference((SimpleName) name);
		} else { // name.isQualifiedName()
			QualifiedName qualifiedName = (QualifiedName) name;
			org.emftext.language.java.types.NamespaceClassifierReference ref = org.emftext.language.java.types.TypesFactory.eINSTANCE.createNamespaceClassifierReference();
			if (name.resolveBinding() == null) {
				ref.getClassifierReferences().add(convertToClassifierReference(qualifiedName.getName()));
				convertToNamespacesAndSet(qualifiedName.getQualifier(), ref);
				return ref;
			}
			Name qualifier = qualifiedName.getQualifier();
			SimpleName simpleName = qualifiedName.getName();
			while (simpleName != null && simpleName.resolveBinding() instanceof ITypeBinding) {
				ref.getClassifierReferences().add(0, convertToClassifierReference(simpleName));
				if (qualifier == null) {
					simpleName = null;
				} else if (qualifier.isSimpleName()) {
					simpleName = (SimpleName) qualifier;
					qualifier = null;
				} else {
					simpleName = ((QualifiedName) qualifier).getName();
					qualifier = ((QualifiedName) qualifier).getQualifier();
				}
			}
			if (simpleName != null && !(simpleName.resolveBinding() instanceof ITypeBinding)) {
				convertToNamespacesAndSet(simpleName, ref);
			}
			if (qualifier != null) {
				convertToNamespacesAndSet(qualifier, ref);
			}
			return ref;
		}
	}
	
	static org.emftext.language.java.types.ClassifierReference convertToClassifierReference(SimpleName simpleName) {
		org.emftext.language.java.types.ClassifierReference ref = org.emftext.language.java.types.TypesFactory.eINSTANCE.createClassifierReference();
		ITypeBinding binding = (ITypeBinding) simpleName.resolveBinding();
		org.emftext.language.java.classifiers.Classifier proxy;
		if (binding == null || binding.isRecovered()) {
			proxy = JDTResolverUtility.getClass(simpleName.getIdentifier());
		} else {
			proxy = JDTResolverUtility.getClassifier(binding);
		}
		proxy.setName(simpleName.getIdentifier());
		ref.setTarget(proxy);
		return ref;
	}
	
	static void convertToNamespacesAndSimpleNameAndSet(Name name, org.emftext.language.java.commons.NamespaceAwareElement namespaceElement,
		org.emftext.language.java.commons.NamedElement namedElement) {
		if (name.isSimpleName()) {
			namedElement.setName(((SimpleName) name).getIdentifier());
		} else if (name.isQualifiedName()) {
			QualifiedName qualifiedName = (QualifiedName) name;
			namedElement.setName(qualifiedName.getName().getIdentifier());
			convertToNamespacesAndSet(qualifiedName.getQualifier(), namespaceElement);
		}
	}
	
	static void convertToNamespacesAndSet(Name name, org.emftext.language.java.commons.NamespaceAwareElement namespaceElement) {
		if (name.isSimpleName()) {
			SimpleName simpleName = (SimpleName) name;
			namespaceElement.getNamespaces().add(0, simpleName.getIdentifier());
		} else if (name.isQualifiedName()) {
			QualifiedName qualifiedName = (QualifiedName) name;
			namespaceElement.getNamespaces().add(0, qualifiedName.getName().getIdentifier());
			convertToNamespacesAndSet(qualifiedName.getQualifier(), namespaceElement);
		}
	}
	
	static void convertToSimpleNameOnlyAndSet(Name name, org.emftext.language.java.commons.NamedElement namedElement) {
		if (name.isSimpleName()) {
			SimpleName simpleName = (SimpleName) name;
			namedElement.setName(simpleName.getIdentifier());
		} else { // name.isQualifiedName()
			QualifiedName qualifiedName = (QualifiedName) name;
			namedElement.setName(qualifiedName.getName().getIdentifier());
		}
	}
	
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.types.TypeReference convertToTypeReference(Type t) {
		if (t.isPrimitiveType()) {
			PrimitiveType primType = (PrimitiveType) t;
			org.emftext.language.java.types.PrimitiveType convertedType;
			if (primType.getPrimitiveTypeCode() == PrimitiveType.BOOLEAN) {
				convertedType = org.emftext.language.java.types.TypesFactory.eINSTANCE.createBoolean();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.BYTE) {
				convertedType = org.emftext.language.java.types.TypesFactory.eINSTANCE.createByte();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.CHAR) {
				convertedType = org.emftext.language.java.types.TypesFactory.eINSTANCE.createChar();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.DOUBLE) {
				convertedType = org.emftext.language.java.types.TypesFactory.eINSTANCE.createDouble();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.FLOAT) {
				convertedType = org.emftext.language.java.types.TypesFactory.eINSTANCE.createFloat();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.INT) {
				convertedType = org.emftext.language.java.types.TypesFactory.eINSTANCE.createInt();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.LONG) {
				convertedType = org.emftext.language.java.types.TypesFactory.eINSTANCE.createLong();
			} else if (primType.getPrimitiveTypeCode() == PrimitiveType.SHORT) {
				convertedType = org.emftext.language.java.types.TypesFactory.eINSTANCE.createShort();
			} else { // primType.getPrimitiveTypeCode() == PrimitiveType.VOID
				convertedType = org.emftext.language.java.types.TypesFactory.eINSTANCE.createVoid();
			}
			primType.annotations().forEach(obj -> convertedType.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(convertedType, primType);
			return convertedType;
		} else if (t.isVar()) {
			org.emftext.language.java.types.InferableType ref = org.emftext.language.java.types.TypesFactory.eINSTANCE.createInferableType();
			ITypeBinding binding = t.resolveBinding();
			if (binding != null) {
				ref.getActualTargets().addAll(JDTBindingConverterUtility.convertToTypeReferences(binding));
				if (binding.isArray()) {
					JDTBindingConverterUtility.convertToArrayDimensionsAndSet(binding, ref);
				} else if (binding.isIntersectionType() && binding.getTypeBounds()[0].isArray()) {
					JDTBindingConverterUtility.convertToArrayDimensionsAndSet(binding.getTypeBounds()[0], ref);
				}
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(ref, t);
			return ref;
		} else if (t.isArrayType()) {
			ArrayType arrT = (ArrayType) t;
			return convertToTypeReference(arrT.getElementType());
		} else if (t.isSimpleType()) {
			SimpleType simT = (SimpleType) t;
			org.emftext.language.java.types.TypeReference ref;
			if (simT.annotations().size() > 0) {
				org.emftext.language.java.types.ClassifierReference tempRef = convertToClassifierReference((SimpleName) simT.getName());
				simT.annotations().forEach(obj -> tempRef.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
					.convertToAnnotationInstance((Annotation) obj)));
				ref = tempRef;
			} else {
				ref = convertToClassifierOrNamespaceClassifierReference(simT.getName());
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(ref, simT);
			return ref;
		} else if (t.isQualifiedType()) {
			QualifiedType qualType = (QualifiedType) t;
			org.emftext.language.java.types.NamespaceClassifierReference result;
			org.emftext.language.java.types.TypeReference parentRef = convertToTypeReference(qualType.getQualifier());
			if (parentRef instanceof org.emftext.language.java.types.ClassifierReference) {
				result = org.emftext.language.java.types.TypesFactory.eINSTANCE.createNamespaceClassifierReference();
				result.getClassifierReferences().add((org.emftext.language.java.types.ClassifierReference) parentRef);
			} else { // parentRef instanceof org.emftext.language.java.types.NamespaceClassifierReference
				result = (org.emftext.language.java.types.NamespaceClassifierReference) parentRef;
			}
			org.emftext.language.java.types.ClassifierReference childRef = convertToClassifierReference(qualType.getName());
			qualType.annotations().forEach(obj -> childRef.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			result.getClassifierReferences().add(childRef);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, qualType);
			return result;
		} else if (t.isNameQualifiedType()) {
			NameQualifiedType nqT = (NameQualifiedType) t;
			org.emftext.language.java.types.NamespaceClassifierReference result;
			org.emftext.language.java.types.TypeReference parentRef = convertToClassifierOrNamespaceClassifierReference(nqT.getQualifier());
			if (parentRef instanceof org.emftext.language.java.types.ClassifierReference) {
				result = org.emftext.language.java.types.TypesFactory.eINSTANCE.createNamespaceClassifierReference();
				result.getClassifierReferences().add((org.emftext.language.java.types.ClassifierReference) parentRef);
			} else {
				result = (org.emftext.language.java.types.NamespaceClassifierReference) parentRef;
			}
			org.emftext.language.java.types.ClassifierReference child = convertToClassifierReference(nqT.getName());
			nqT.annotations().forEach(obj -> child.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			result.getClassifierReferences().add(child);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, nqT);
			return result;
		} else if (t.isParameterizedType()) {
			ParameterizedType paramT = (ParameterizedType) t;
			org.emftext.language.java.types.TypeReference ref = convertToTypeReference(paramT.getType());
			org.emftext.language.java.types.ClassifierReference container;
			if (ref instanceof org.emftext.language.java.types.ClassifierReference) {
				container = (org.emftext.language.java.types.ClassifierReference) ref;
			} else {
				org.emftext.language.java.types.NamespaceClassifierReference containerContainer = (org.emftext.language.java.types.NamespaceClassifierReference) ref;
				container = containerContainer.getClassifierReferences().get(containerContainer.getClassifierReferences().size() - 1);
			}
			paramT.typeArguments().forEach(obj -> container.getTypeArguments().add(convertToTypeArgument((Type) obj)));
			return ref;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.generics.TypeArgument convertToTypeArgument(Type t) {
		if (t.isWildcardType()) {
			WildcardType wildType = (WildcardType) t;
			if (wildType.getBound() == null) {
				org.emftext.language.java.generics.UnknownTypeArgument result = org.emftext.language.java.generics.GenericsFactory.eINSTANCE.createUnknownTypeArgument();
				wildType.annotations().forEach(obj -> result.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
					.convertToAnnotationInstance((Annotation) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, wildType);
				return result;
			} else if (wildType.isUpperBound()) {
				org.emftext.language.java.generics.ExtendsTypeArgument result = org.emftext.language.java.generics.GenericsFactory.eINSTANCE.createExtendsTypeArgument();
				wildType.annotations().forEach(obj -> result.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
					.convertToAnnotationInstance((Annotation) obj)));
				result.setExtendType(convertToTypeReference(wildType.getBound()));
				convertToArrayDimensionsAndSet(wildType.getBound(), result);
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, wildType);
				return result;
			} else {
				org.emftext.language.java.generics.SuperTypeArgument result = org.emftext.language.java.generics.GenericsFactory.eINSTANCE.createSuperTypeArgument();
				wildType.annotations().forEach(obj -> result.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
					.convertToAnnotationInstance((Annotation) obj)));
				result.setSuperType(convertToTypeReference(wildType.getBound()));
				convertToArrayDimensionsAndSet(wildType.getBound(), result);
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, wildType);
				return result;
			}
		} else {
			org.emftext.language.java.generics.QualifiedTypeArgument result = org.emftext.language.java.generics.GenericsFactory.eINSTANCE.createQualifiedTypeArgument();
			result.setTypeReference(convertToTypeReference(t));
			convertToArrayDimensionsAndSet(t, result);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, t);
			return result;
		}
	}
	
	static void convertToArrayDimensionsAndSet(Type t, org.emftext.language.java.arrays.ArrayTypeable arrDimContainer) {
		convertToArrayDimensionsAndSet(t, arrDimContainer, 0);
	}
	
	static void convertToArrayDimensionsAndSet(Type t, org.emftext.language.java.arrays.ArrayTypeable arrDimContainer,
			int ignoreDimensions) {
		if (t.isArrayType()) {
			ArrayType arrT = (ArrayType) t;
			for (int i = ignoreDimensions; i < arrT.dimensions().size(); i++) {
				arrDimContainer.getArrayDimensionsBefore().add(convertToArrayDimension((Dimension)
					arrT.dimensions().get(i)));
			}
		}
	}
	
	static void convertToArrayDimensionAfterAndSet(Dimension dim, org.emftext.language.java.arrays.ArrayTypeable arrDimContainer) {
		arrDimContainer.getArrayDimensionsAfter().add(convertToArrayDimension(dim));
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.arrays.ArrayDimension convertToArrayDimension(Dimension dim) {
		org.emftext.language.java.arrays.ArrayDimension result = org.emftext.language.java.arrays.ArraysFactory.eINSTANCE.createArrayDimension();
		dim.annotations().forEach(annot -> result.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
			.convertToAnnotationInstance((Annotation) annot)));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, dim);
		return result;
	}
}
