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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

@SuppressWarnings("restriction")
class JDTBindingConverterUtility {
	static List<tools.mdsd.jamopp.model.java.types.TypeReference> convertToTypeReferences(ITypeBinding binding) {
		List<tools.mdsd.jamopp.model.java.types.TypeReference> result = new ArrayList<>();
		if (binding.isPrimitive()) {
			if (binding.getName().equals("int")) {
				result.add(tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createInt());
			} else if (binding.getName().equals("byte")) {
				result.add(tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createByte());
			} else if (binding.getName().equals("short")) {
				result.add(tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createShort());
			} else if (binding.getName().equals("long")) {
				result.add(tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createLong());
			} else if (binding.getName().equals("boolean")) {
				result.add(tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createBoolean());
			} else if (binding.getName().equals("double")) {
				result.add(tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createDouble());
			} else if (binding.getName().equals("float")) {
				result.add(tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createFloat());
			} else if (binding.getName().equals("void")) {
				result.add(tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createVoid());
			} else if (binding.getName().equals("char")) {
				result.add(tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createChar());
			}
		} else if (binding.isArray()) {
			return convertToTypeReferences(binding.getElementType());
		} else if (binding.isIntersectionType()) {
			for (ITypeBinding b : binding.getTypeBounds()) {
				result.addAll(convertToTypeReferences(b));
			}
		} else {
			tools.mdsd.jamopp.model.java.classifiers.Classifier classifier = JDTResolverUtility.getClassifier(binding);
			convertToNameAndSet(binding, classifier);
			tools.mdsd.jamopp.model.java.types.ClassifierReference ref = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createClassifierReference();
			if (binding.isParameterizedType()) {
				for (ITypeBinding b : binding.getTypeArguments()) {
					ref.getTypeArguments().add(convertToTypeArgument(b));
				}
			}
			ref.setTarget(classifier);
			result.add(ref);
		}
		return result;
	}
	
	static void convertToNameAndSet(ITypeBinding binding, tools.mdsd.jamopp.model.java.commons.NamedElement element) {
		String name = binding.getName();
		if (binding.isParameterizedType()) {
			name = name.substring(0, name.indexOf("<"));
		} else if (binding.isArray()) {
			name = name.substring(0, name.indexOf("["));
		}
		element.setName(name);
	}
	
	static tools.mdsd.jamopp.model.java.generics.TypeArgument convertToTypeArgument(ITypeBinding binding) {
		if (binding.isWildcardType()) {
			if (binding.getBound() == null) {
				return tools.mdsd.jamopp.model.java.generics.GenericsFactory.eINSTANCE.createUnknownTypeArgument();
			} else if (binding.isUpperbound()) {
				tools.mdsd.jamopp.model.java.generics.ExtendsTypeArgument result = tools.mdsd.jamopp.model.java.generics.GenericsFactory.eINSTANCE.createExtendsTypeArgument();
				result.setExtendType(convertToTypeReferences(binding.getBound()).get(0));
				convertToArrayDimensionsAndSet(binding, result);
				return result;
			} else {
				tools.mdsd.jamopp.model.java.generics.SuperTypeArgument result = tools.mdsd.jamopp.model.java.generics.GenericsFactory.eINSTANCE.createSuperTypeArgument();
				result.setSuperType(convertToTypeReferences(binding.getBound()).get(0));
				convertToArrayDimensionsAndSet(binding, result);
				return result;
			}
		} else {
			tools.mdsd.jamopp.model.java.generics.QualifiedTypeArgument result = tools.mdsd.jamopp.model.java.generics.GenericsFactory.eINSTANCE.createQualifiedTypeArgument();
			result.setTypeReference(convertToTypeReferences(binding).get(0));
			convertToArrayDimensionsAndSet(binding, result);
			return result;
		}
	}
	
	static void convertToArrayDimensionsAndSet(ITypeBinding binding, tools.mdsd.jamopp.model.java.arrays.ArrayTypeable arrDimContainer) {
		if (binding.isArray()) {
			for (int i = 0; i < binding.getDimensions(); i++) {
				arrDimContainer.getArrayDimensionsBefore().add(tools.mdsd.jamopp.model.java.arrays.ArraysFactory.eINSTANCE.createArrayDimension());
			}
		}
	}
	
	static tools.mdsd.jamopp.model.java.classifiers.ConcreteClassifier convertToConcreteClassifier(ITypeBinding binding,
			boolean extractAdditionalInformation) {
		binding = binding.getTypeDeclaration();
		tools.mdsd.jamopp.model.java.classifiers.ConcreteClassifier result = null;
		if (binding.isAnnotation()) {
			result = JDTResolverUtility.getAnnotation(binding);
		} else if (binding.isClass()) {
			tools.mdsd.jamopp.model.java.classifiers.Class resultClass = JDTResolverUtility.getClass(binding);
			if (resultClass.eContainer() == null) {
				try {
					if (binding.getSuperclass() != null) {
						resultClass.setExtends(convertToTypeReferences(binding.getSuperclass()).get(0));
					}
					for (ITypeBinding typeBind : binding.getInterfaces()) {
						resultClass.getImplements().addAll(convertToTypeReferences(typeBind));
					}
				} catch (AbortCompilation e) {
				}
			}
			result = resultClass;
		} else if (binding.isInterface()) {
			tools.mdsd.jamopp.model.java.classifiers.Interface resultInterface = JDTResolverUtility.getInterface(binding);
			if (resultInterface.eContainer() == null) {
				try {
					for (ITypeBinding typeBind : binding.getInterfaces()) {
						resultInterface.getExtends().addAll(convertToTypeReferences(typeBind));
					}
				} catch (AbortCompilation e) {
				}
			}
			result = resultInterface;
		} else {
			tools.mdsd.jamopp.model.java.classifiers.Enumeration resultEnum = JDTResolverUtility.getEnumeration(binding);
			if (resultEnum.eContainer() == null) {
				try {
					for (ITypeBinding typeBind : binding.getInterfaces()) {
						resultEnum.getImplements().addAll(convertToTypeReferences(typeBind));
					}
					if (extractAdditionalInformation) {
						for (IVariableBinding varBind : binding.getDeclaredFields()) {
							if (varBind.isEnumConstant()) {
								resultEnum.getConstants().add(convertToEnumConstant(varBind));
							}
						}
					}
				} catch (AbortCompilation e) {
				}
			}
			result = resultEnum;
		}
		result.setPackage(JDTResolverUtility.getPackage(binding.getPackage()));
		if (result.eContainer() == null) {
			if (extractAdditionalInformation) {
				try {
					for (IAnnotationBinding annotBind : binding.getAnnotations()) {
						result.getAnnotationsAndModifiers().add(convertToAnnotationInstance(annotBind));
					}
					for (ITypeBinding typeBind : binding.getTypeParameters()) {
						result.getTypeParameters().add(convertToTypeParameter(typeBind));
					}
				}
				catch (AbortCompilation e) {
				}
			}
			result.getAnnotationsAndModifiers().addAll(convertToModifiers(binding.getModifiers()));
			convertToNameAndSet(binding, result);
		}
		if (extractAdditionalInformation) {
			try {
				tools.mdsd.jamopp.model.java.members.Member member;
				for (IVariableBinding varBind : binding.getDeclaredFields()) {
					if (varBind.isEnumConstant()) {
						continue;
					}
					member = convertToField(varBind);
					if (!result.getMembers().contains(member)) {
						result.getMembers().add(member);
					}
				}
				for (IMethodBinding methBind : binding.getDeclaredMethods()) {
					if (methBind.isDefaultConstructor()) {
						continue;
					}
					if (methBind.isConstructor()) {
						member = convertToConstructor(methBind);
					} else {
						member = convertToMethod(methBind);
					}
					if (!result.getMembers().contains(member)) {
						result.getMembers().add(member);
					}
				}
				for (ITypeBinding typeBind : binding.getDeclaredTypes()) {
					member = convertToConcreteClassifier(typeBind, extractAdditionalInformation);
					if (!result.getMembers().contains(member)) {
						result.getMembers().add(member);
					}
				}
			} catch (AbortCompilation e) {
			}
		}
		return result;
	}
	
	private static tools.mdsd.jamopp.model.java.generics.TypeParameter convertToTypeParameter(ITypeBinding binding) {
		tools.mdsd.jamopp.model.java.generics.TypeParameter result = JDTResolverUtility.getTypeParameter(binding);
		if (result.eContainer() != null) {
			return result;
		}
		try {
			for (IAnnotationBinding annotBind : binding.getAnnotations()) {
				result.getAnnotations().add(convertToAnnotationInstance(annotBind));
			}
			for (ITypeBinding typeBind : binding.getTypeBounds()) {
				result.getExtendTypes().addAll(convertToTypeReferences(typeBind));
			}
		} catch (AbortCompilation e) {
		}
		convertToNameAndSet(binding, result);
		return result;
	}
	
	private static tools.mdsd.jamopp.model.java.references.Reference internalConvertToReference(ITypeBinding binding) {
		tools.mdsd.jamopp.model.java.references.IdentifierReference idRef = tools.mdsd.jamopp.model.java.references.ReferencesFactory.eINSTANCE.createIdentifierReference();
		idRef.setTarget(JDTResolverUtility.getClassifier(binding));
		if (binding.isNested()) {
			tools.mdsd.jamopp.model.java.references.Reference parentRef = internalConvertToReference(binding.getDeclaringClass());
			parentRef.setNext(idRef);
		}
		return idRef;
	}
	
	private static tools.mdsd.jamopp.model.java.references.Reference getTopReference(tools.mdsd.jamopp.model.java.references.Reference ref) {
		tools.mdsd.jamopp.model.java.references.Reference currentRef = ref;
		tools.mdsd.jamopp.model.java.references.Reference parentRef = ref.getPrevious();
		while (parentRef != null) {
			currentRef = parentRef;
			parentRef = currentRef.getPrevious();
		}
		return currentRef;
	}
	
	private static tools.mdsd.jamopp.model.java.members.Field convertToField(IVariableBinding binding) {
		tools.mdsd.jamopp.model.java.references.ReferenceableElement refElement = JDTResolverUtility.getReferencableElement(binding);
		if (refElement.eContainer() != null) {
			if (refElement instanceof tools.mdsd.jamopp.model.java.members.AdditionalField) {
				return (tools.mdsd.jamopp.model.java.members.Field)
					((tools.mdsd.jamopp.model.java.members.AdditionalField) refElement).eContainer();
			} else {
				return (tools.mdsd.jamopp.model.java.members.Field) refElement;
			}
		}
		tools.mdsd.jamopp.model.java.members.Field result = (tools.mdsd.jamopp.model.java.members.Field) refElement;
		result.getAnnotationsAndModifiers().addAll(convertToModifiers(binding.getModifiers()));
		try {
			for (IAnnotationBinding annotBind : binding.getAnnotations()) {
				result.getAnnotationsAndModifiers().add(convertToAnnotationInstance(annotBind));
			}
		} catch (AbortCompilation e) {
		}
		result.setName(binding.getName());
		result.setTypeReference(convertToTypeReferences(binding.getType()).get(0));
//		convertToArrayDimensionsAndSet(binding.getType(), result);
		if (binding.getConstantValue() != null) {
			result.setInitialValue(convertToPrimaryExpression(binding.getConstantValue()));
		}
		return result;
	}
	
	private static tools.mdsd.jamopp.model.java.members.EnumConstant convertToEnumConstant(IVariableBinding binding) {
		tools.mdsd.jamopp.model.java.members.EnumConstant result = JDTResolverUtility.getEnumConstant(binding);
		if (result.eContainer() != null) {
			return result;
		}
		try {
			for (IAnnotationBinding annotBind : binding.getAnnotations()) {
				result.getAnnotations().add(convertToAnnotationInstance(annotBind));
			}
		} catch (AbortCompilation e) {
		}
		result.setName(binding.getName());
		return result;
	}
	
	private static tools.mdsd.jamopp.model.java.members.Constructor convertToConstructor(IMethodBinding binding) {
		tools.mdsd.jamopp.model.java.members.Constructor result = JDTResolverUtility.getConstructor(binding);
		if (result.eContainer() != null) {
			return result;
		}
		result.getAnnotationsAndModifiers().addAll(convertToModifiers(binding.getModifiers()));
		try {
			for (IAnnotationBinding annotBind : binding.getAnnotations()) {
				result.getAnnotationsAndModifiers().add(convertToAnnotationInstance(annotBind));
			}
		} catch (AbortCompilation e) {
		}
		result.setName(binding.getName());
		try {
			for (ITypeBinding typeBind : binding.getTypeParameters()) {
				result.getTypeParameters().add(convertToTypeParameter(typeBind));
			}
		} catch (AbortCompilation e) {
		}
		if (binding.getDeclaredReceiverType() != null) {
			tools.mdsd.jamopp.model.java.parameters.ReceiverParameter param = tools.mdsd.jamopp.model.java.parameters.ParametersFactory.eINSTANCE.createReceiverParameter();
			param.setName("");
			param.setTypeReference(convertToTypeReferences(binding.getDeclaredReceiverType()).get(0));
			param.setOuterTypeReference(param.getTypeReference());
			param.setThisReference(tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createThis());
			result.getParameters().add(param);
		}
		for (int index = 0; index < binding.getParameterTypes().length; index++) {
			ITypeBinding typeBind = binding.getParameterTypes()[index];
			tools.mdsd.jamopp.model.java.parameters.Parameter param;
			if (binding.isVarargs() && index == binding.getParameterTypes().length - 1) {
				param = tools.mdsd.jamopp.model.java.parameters.ParametersFactory.eINSTANCE.createVariableLengthParameter();
			} else {
				param = tools.mdsd.jamopp.model.java.parameters.ParametersFactory.eINSTANCE.createOrdinaryParameter();
			}
			param.setName("param" + index);
			param.setTypeReference(convertToTypeReferences(typeBind).get(0));
//			convertToArrayDimensionsAndSet(typeBind, param);
			IAnnotationBinding[] binds = binding.getParameterAnnotations(index);
			try {
				for (IAnnotationBinding annotBind : binds) {
					param.getAnnotationsAndModifiers().add(convertToAnnotationInstance(annotBind));
				}
			} catch (AbortCompilation e) {
			}
			result.getParameters().add(param);
		}
		for (ITypeBinding typeBind : binding.getExceptionTypes()) {
			result.getExceptions().add(convertToNamespaceClassifierReference(typeBind));
		}
		return result;
	}
	
	private static tools.mdsd.jamopp.model.java.members.Method convertToMethod(IMethodBinding binding) {
		tools.mdsd.jamopp.model.java.members.Method result = JDTResolverUtility.getMethod(binding);
		if (result.eContainer() != null) {
			return result;
		}
		result.getAnnotationsAndModifiers().addAll(convertToModifiers(binding.getModifiers()));
		try {
			for (IAnnotationBinding annotBind : binding.getAnnotations()) {
				result.getAnnotationsAndModifiers().add(convertToAnnotationInstance(annotBind));
			}
		} catch (AbortCompilation e) {
		}
		result.setName(binding.getName());
		result.setTypeReference(convertToTypeReferences(binding.getReturnType()).get(0));
//		convertToArrayDimensionsAndSet(binding.getReturnType(), result);
		try {
			for (ITypeBinding typeBind : binding.getTypeParameters()) {
				result.getTypeParameters().add(convertToTypeParameter(typeBind));
			}
		} catch (AbortCompilation e) {
		}
		if (binding.getDeclaredReceiverType() != null) {
			tools.mdsd.jamopp.model.java.parameters.ReceiverParameter param = tools.mdsd.jamopp.model.java.parameters.ParametersFactory.eINSTANCE.createReceiverParameter();
			param.setTypeReference(convertToTypeReferences(binding.getDeclaredReceiverType()).get(0));
			param.setName("");
			param.setThisReference(tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createThis());
			result.getParameters().add(param);
		}
		for (int index = 0; index < binding.getParameterTypes().length; index++) {
			ITypeBinding typeBind = binding.getParameterTypes()[index];
			tools.mdsd.jamopp.model.java.parameters.Parameter param;
			if (binding.isVarargs() && index == binding.getParameterTypes().length - 1) {
				param = tools.mdsd.jamopp.model.java.parameters.ParametersFactory.eINSTANCE.createVariableLengthParameter();
			} else {
				param = tools.mdsd.jamopp.model.java.parameters.ParametersFactory.eINSTANCE.createOrdinaryParameter();
			}
			param.setName("param" + index);
			param.setTypeReference(convertToTypeReferences(typeBind).get(0));
//			convertToArrayDimensionsAndSet(typeBind, param);
			try {
				IAnnotationBinding[] binds = binding.getParameterAnnotations(index);
				for (IAnnotationBinding annotBind : binds) {
					param.getAnnotationsAndModifiers().add(convertToAnnotationInstance(annotBind));
				}
			} catch (AbortCompilation e) {
			}
			result.getParameters().add(param);
		}
		if (binding.getDefaultValue() != null) {
			((tools.mdsd.jamopp.model.java.members.InterfaceMethod) result).setDefaultValue(convertToAnnotationValue(binding.getDefaultValue()));
		}
		try {
			for (ITypeBinding typeBind : binding.getExceptionTypes()) {
				result.getExceptions().add(convertToNamespaceClassifierReference(typeBind));
			}
		} catch (AbortCompilation e) {
		}
		if (binding.getDeclaringClass().isInterface()) {
			boolean hasDefaultImpl = false;
			for (tools.mdsd.jamopp.model.java.modifiers.Modifier mod : result.getModifiers()) {
				if (mod instanceof tools.mdsd.jamopp.model.java.modifiers.Default) {
					hasDefaultImpl = true;
					break;
				}
			}
			if (!hasDefaultImpl) {
				result.setStatement(tools.mdsd.jamopp.model.java.statements.StatementsFactory.eINSTANCE.createEmptyStatement());
			}
		}
		return result;
	}
	
	private static tools.mdsd.jamopp.model.java.types.NamespaceClassifierReference convertToNamespaceClassifierReference(ITypeBinding binding) {
		tools.mdsd.jamopp.model.java.types.NamespaceClassifierReference ref = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createNamespaceClassifierReference();
		if (binding.getPackage() != null) {
			for (String nameComp : binding.getPackage().getNameComponents()) {
				ref.getNamespaces().add(nameComp);
			}
		}
		tools.mdsd.jamopp.model.java.types.ClassifierReference classRef = tools.mdsd.jamopp.model.java.types.TypesFactory.eINSTANCE.createClassifierReference();
		classRef.setTarget(JDTResolverUtility.getClassifier(binding));
		ref.getClassifierReferences().add(classRef);
		return ref;
	}
	
	private static tools.mdsd.jamopp.model.java.annotations.AnnotationInstance convertToAnnotationInstance(IAnnotationBinding binding) {
		tools.mdsd.jamopp.model.java.annotations.AnnotationInstance result = tools.mdsd.jamopp.model.java.annotations.AnnotationsFactory.eINSTANCE.createAnnotationInstance();
		tools.mdsd.jamopp.model.java.classifiers.Annotation resultClass = JDTResolverUtility.getAnnotation(binding.getAnnotationType());
		convertToNameAndSet(binding.getAnnotationType(), resultClass);
		result.setAnnotation(resultClass);
		if (binding.getDeclaredMemberValuePairs().length > 0) {
			tools.mdsd.jamopp.model.java.annotations.AnnotationParameterList params =
				tools.mdsd.jamopp.model.java.annotations.AnnotationsFactory.eINSTANCE.createAnnotationParameterList();
			for (IMemberValuePairBinding memBind : binding.getDeclaredMemberValuePairs()) {
				params.getSettings().add(convertToAnnotationAttributeSetting(memBind));
			}
			result.setParameter(params);
		}
		return result;
	}
	
	private static tools.mdsd.jamopp.model.java.annotations.AnnotationAttributeSetting convertToAnnotationAttributeSetting(
		IMemberValuePairBinding binding) {
		tools.mdsd.jamopp.model.java.annotations.AnnotationAttributeSetting result =
			tools.mdsd.jamopp.model.java.annotations.AnnotationsFactory.eINSTANCE.createAnnotationAttributeSetting();
		result.setAttribute(JDTResolverUtility.getInterfaceMethod(binding.getMethodBinding()));
		result.setValue(convertToAnnotationValue(binding.getValue()));
		return result;
	}
	
	private static tools.mdsd.jamopp.model.java.annotations.AnnotationValue convertToAnnotationValue(Object value) {
		if (value instanceof IVariableBinding) {
			IVariableBinding varBind = (IVariableBinding) value;
			tools.mdsd.jamopp.model.java.references.Reference parentRef = internalConvertToReference(varBind.getDeclaringClass());
			tools.mdsd.jamopp.model.java.references.IdentifierReference varRef = tools.mdsd.jamopp.model.java.references.ReferencesFactory.eINSTANCE.createIdentifierReference();
			varRef.setTarget(JDTResolverUtility.getEnumConstant(varBind));
			parentRef.setNext(varRef);
			return getTopReference(varRef);
		} else if (value instanceof IAnnotationBinding) {
			return convertToAnnotationInstance((IAnnotationBinding) value);
		} else if (value instanceof Object[]) {
			Object[] values = (Object[]) value;
			tools.mdsd.jamopp.model.java.arrays.ArrayInitializer initializer = tools.mdsd.jamopp.model.java.arrays.ArraysFactory.eINSTANCE.createArrayInitializer();
			for (int index = 0; index < values.length; index++) {
				initializer.getInitialValues().add((tools.mdsd.jamopp.model.java.arrays.ArrayInitializationValue)
					convertToAnnotationValue(values[index]));
			}
			return initializer;
		} else if (value instanceof ITypeBinding) {
			tools.mdsd.jamopp.model.java.references.Reference parentRef = internalConvertToReference((ITypeBinding) value);
			tools.mdsd.jamopp.model.java.references.ReflectiveClassReference classRef = tools.mdsd.jamopp.model.java.references.ReferencesFactory.eINSTANCE.createReflectiveClassReference();
			parentRef.setNext(classRef);
			return getTopReference(classRef);
		} else {
			return convertToPrimaryExpression(value);
		}
	}
	
	private static tools.mdsd.jamopp.model.java.expressions.PrimaryExpression convertToPrimaryExpression(Object value) {
		if (value instanceof String) {
			tools.mdsd.jamopp.model.java.references.StringReference ref = tools.mdsd.jamopp.model.java.references.ReferencesFactory.eINSTANCE.createStringReference();
			ref.setValue((String) value);
			return ref;
		} else if (value instanceof Boolean) {
			tools.mdsd.jamopp.model.java.literals.BooleanLiteral literal = tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createBooleanLiteral();
			literal.setValue((boolean) value);
			return literal;
		} else if (value instanceof Character) {
			tools.mdsd.jamopp.model.java.literals.CharacterLiteral literal = tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createCharacterLiteral();
			literal.setValue("\\u" + Integer.toHexString((Character) value));
			return literal;
		} else if (value instanceof Byte) {
			tools.mdsd.jamopp.model.java.literals.DecimalIntegerLiteral literal = tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createDecimalIntegerLiteral();;
			literal.setDecimalValue(BigInteger.valueOf((byte) value));
			return literal;
		} else if (value instanceof Short) {
			tools.mdsd.jamopp.model.java.literals.DecimalIntegerLiteral literal = tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createDecimalIntegerLiteral();
			literal.setDecimalValue(BigInteger.valueOf((short) value));
			return literal;
		} else if (value instanceof Integer) {
			tools.mdsd.jamopp.model.java.literals.DecimalIntegerLiteral literal = tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createDecimalIntegerLiteral();
			literal.setDecimalValue(BigInteger.valueOf((int) value));
			return literal;
		} else if (value instanceof Long) {
			tools.mdsd.jamopp.model.java.literals.DecimalLongLiteral literal = tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createDecimalLongLiteral();
			literal.setDecimalValue(BigInteger.valueOf((long) value));
			return literal;
		} else if (value instanceof Float) {
			tools.mdsd.jamopp.model.java.literals.DecimalFloatLiteral literal = tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createDecimalFloatLiteral();
			literal.setDecimalValue((float) value);
			return literal;
		} else if (value instanceof Double) {
			tools.mdsd.jamopp.model.java.literals.DecimalDoubleLiteral literal = tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createDecimalDoubleLiteral();
			literal.setDecimalValue((double) value);
			return literal;
		} else { // value == null
			tools.mdsd.jamopp.model.java.literals.NullLiteral literal = tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createNullLiteral();
			return literal;
		}
	}

	private static List<tools.mdsd.jamopp.model.java.modifiers.Modifier> convertToModifiers(int modifiers) {
		ArrayList<tools.mdsd.jamopp.model.java.modifiers.Modifier> result = new ArrayList<>();
		if (Modifier.isAbstract(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createAbstract());
		}
		if (Modifier.isDefault(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createDefault());
		}
		if (Modifier.isFinal(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createFinal());
		}
		if (Modifier.isNative(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createNative());
		}
		if (Modifier.isPrivate(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createPrivate());
		}
		if (Modifier.isProtected(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createProtected());
		}
		if (Modifier.isPublic(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createPublic());
		}
		if (Modifier.isStatic(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createStatic());
		}
		if (Modifier.isStrictfp(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createStrictfp());
		}
		if (Modifier.isSynchronized(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createSynchronized());
		}
		if (Modifier.isTransient(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createTransient());
		}
		if (Modifier.isVolatile(modifiers)) {
			result.add(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createVolatile());
		}
		return result;
	}
	
	static tools.mdsd.jamopp.model.java.containers.Package convertToPackage(IPackageBinding binding) {
		tools.mdsd.jamopp.model.java.containers.Package pack = JDTResolverUtility.getPackage(binding);
		pack.setModule(JDTResolverUtility.getModule(binding.getModule()));
		if (pack.getAnnotations().size() > 0) {
			return pack;
		}
		pack.getNamespaces().clear();
		for (String nameComp : binding.getNameComponents()) {
			pack.getNamespaces().add(nameComp);
		}
		pack.setName("");
		try {
			for (IAnnotationBinding annotBind : binding.getAnnotations()) {
				pack.getAnnotations().add(convertToAnnotationInstance(annotBind));
			}
		} catch (AbortCompilation e) {
		}
		return pack;
	}
	
	static tools.mdsd.jamopp.model.java.containers.Module convertToModule(IModuleBinding binding) {
		tools.mdsd.jamopp.model.java.containers.Module result = JDTResolverUtility.getModule(binding);
		if (result.eContents().size() > 0) {
			return result;
		}
		try {
			for (IAnnotationBinding annotBind : binding.getAnnotations()) {
				result.getAnnotations().add(convertToAnnotationInstance(annotBind));
			}
		} catch (AbortCompilation e) {
		}
		if (binding.isOpen()) {
			result.setOpen(tools.mdsd.jamopp.model.java.modifiers.ModifiersFactory.eINSTANCE.createOpen());
		}
		convertToNamespacesAndSet(binding.getName(), result);
		result.setName("");
		try {
			for(IPackageBinding packBind : binding.getExportedPackages()) {
				tools.mdsd.jamopp.model.java.modules.ExportsModuleDirective dir = tools.mdsd.jamopp.model.java.modules.ModulesFactory.eINSTANCE.createExportsModuleDirective();
				dir.setAccessablePackage(JDTResolverUtility.getPackage(packBind));
				String[] mods = binding.getExportedTo(packBind);
				for (String modName : mods) {
					tools.mdsd.jamopp.model.java.modules.ModuleReference ref = tools.mdsd.jamopp.model.java.modules.ModulesFactory.eINSTANCE.createModuleReference();
					ref.setTarget(JDTResolverUtility.getModule(modName));
					dir.getModules().add(ref);
				}
				result.getTarget().add(dir);
			}
			for (IPackageBinding packBind : binding.getOpenedPackages()) {
				tools.mdsd.jamopp.model.java.modules.OpensModuleDirective dir = tools.mdsd.jamopp.model.java.modules.ModulesFactory.eINSTANCE.createOpensModuleDirective();
				dir.setAccessablePackage(JDTResolverUtility.getPackage(packBind));
				String[] mods = binding.getOpenedTo(packBind);
				for (String modName : mods) {
					tools.mdsd.jamopp.model.java.modules.ModuleReference ref = tools.mdsd.jamopp.model.java.modules.ModulesFactory.eINSTANCE.createModuleReference();
					ref.setTarget(JDTResolverUtility.getModule(modName));
					dir.getModules().add(ref);
				}
				result.getTarget().add(dir);
			}
			for (IModuleBinding modBind : binding.getRequiredModules()) {
				tools.mdsd.jamopp.model.java.modules.RequiresModuleDirective dir = tools.mdsd.jamopp.model.java.modules.ModulesFactory.eINSTANCE.createRequiresModuleDirective();
				tools.mdsd.jamopp.model.java.containers.Module reqMod = JDTResolverUtility.getModule(modBind);
				tools.mdsd.jamopp.model.java.modules.ModuleReference ref = tools.mdsd.jamopp.model.java.modules.ModulesFactory.eINSTANCE.createModuleReference();
				ref.setTarget(reqMod);
				dir.setRequiredModule(ref);
				result.getTarget().add(dir);
			}
			for (ITypeBinding typeBind : binding.getUses()) {
				tools.mdsd.jamopp.model.java.modules.UsesModuleDirective dir = tools.mdsd.jamopp.model.java.modules.ModulesFactory.eINSTANCE.createUsesModuleDirective();
				dir.setTypeReference(convertToTypeReferences(typeBind).get(0));
				result.getTarget().add(dir);
			}
			for (ITypeBinding typeBind : binding.getServices()) {
				tools.mdsd.jamopp.model.java.modules.ProvidesModuleDirective dir = tools.mdsd.jamopp.model.java.modules.ModulesFactory.eINSTANCE.createProvidesModuleDirective();
				dir.setTypeReference(convertToTypeReferences(typeBind).get(0));
				for (ITypeBinding service : binding.getImplementations(typeBind)) {
					dir.getServiceProviders().addAll(convertToTypeReferences(service));
				}
				result.getTarget().add(dir);
			}
		} catch (AbortCompilation e) {
		}
		return result;
	}
	
	private static void convertToNamespacesAndSet(String namespaces, tools.mdsd.jamopp.model.java.commons.NamespaceAwareElement ele) {
		ele.getNamespaces().clear();
		String[] singleNamespaces = namespaces.split("\\.");
		for (String part : singleNamespaces) {
			ele.getNamespaces().add(part);
		}
	}
}
