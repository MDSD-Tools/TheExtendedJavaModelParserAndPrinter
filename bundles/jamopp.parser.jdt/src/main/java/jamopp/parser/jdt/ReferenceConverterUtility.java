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
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;

class ReferenceConverterUtility {
	static org.emftext.language.java.references.Reference convertToReference(Expression expr) {
		return walkUp(internalConvertToReference(expr));
	}
	
	private static org.emftext.language.java.references.Reference walkUp(org.emftext.language.java.references.Reference ref) {
		org.emftext.language.java.references.Reference result = ref;
		org.emftext.language.java.references.Reference parent = result.getPrevious();
		while (parent != null) {
			result = parent;
			parent = result.getPrevious();
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.references.Reference internalConvertToReference(Expression expr) {
		if (expr instanceof Annotation) {
			return AnnotationInstanceOrModifierConverterUtility.convertToAnnotationInstance((Annotation) expr);
		} else if (expr.getNodeType() == ASTNode.ARRAY_ACCESS) {
			ArrayAccess arr = (ArrayAccess) expr;
			org.emftext.language.java.references.Reference parent = internalConvertToReference(arr.getArray());
			org.emftext.language.java.arrays.ArraySelector selector = org.emftext.language.java.arrays.ArraysFactory.eINSTANCE.createArraySelector();
			selector.setPosition(ExpressionConverterUtility.convertToExpression(arr.getIndex()));
			parent.getArraySelectors().add(selector);
			return parent;
		} else if (expr.getNodeType() == ASTNode.ARRAY_CREATION) {
			ArrayCreation arr = (ArrayCreation) expr;
			if (arr.getInitializer() != null) {
				org.emftext.language.java.arrays.ArrayInstantiationByValuesTyped result = org.emftext.language.java.arrays.ArraysFactory.eINSTANCE.createArrayInstantiationByValuesTyped();
				result.setTypeReference(BaseConverterUtility.convertToTypeReference(arr.getType()));
//				BaseConverterUtility.convertToArrayDimensionsAndSet(arr.getType(), result);
				result.setArrayInitializer(AnnotationInstanceOrModifierConverterUtility.convertToArrayInitializer(arr.getInitializer()));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
				return result;
			} else {
				org.emftext.language.java.arrays.ArrayInstantiationBySize result = org.emftext.language.java.arrays.ArraysFactory.eINSTANCE.createArrayInstantiationBySize();
				result.setTypeReference(BaseConverterUtility.convertToTypeReference(arr.getType()));
//				BaseConverterUtility.convertToArrayDimensionsAndSet(arr.getType(), result, arr.dimensions().size());
				arr.dimensions().forEach(obj -> result.getSizes().add(ExpressionConverterUtility.convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
				return result;
			}
		} else if (expr.getNodeType() == ASTNode.ARRAY_INITIALIZER) {
			org.emftext.language.java.arrays.ArrayInstantiationByValuesUntyped result = org.emftext.language.java.arrays.ArraysFactory.eINSTANCE.createArrayInstantiationByValuesUntyped();
			result.setArrayInitializer(AnnotationInstanceOrModifierConverterUtility.convertToArrayInitializer((ArrayInitializer) expr));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
			return result;
		} else if (expr.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation arr = (ClassInstanceCreation) expr;
			org.emftext.language.java.instantiations.NewConstructorCall result;
			if (arr.getType().isParameterizedType() && ((ParameterizedType) arr.getType()).typeArguments().size() == 0) {
				result = org.emftext.language.java.instantiations.InstantiationsFactory.eINSTANCE.createNewConstructorCallWithInferredTypeArguments();
			} else {
				result = org.emftext.language.java.instantiations.InstantiationsFactory.eINSTANCE.createNewConstructorCall();
			}
			arr.typeArguments().forEach(obj -> result.getCallTypeArguments().add(BaseConverterUtility.convertToTypeArgument((Type) obj)));
			result.setTypeReference(BaseConverterUtility.convertToTypeReference(arr.getType()));
			arr.arguments().forEach(obj -> result.getArguments().add(ExpressionConverterUtility.convertToExpression((Expression) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
			if (arr.getAnonymousClassDeclaration() != null) {
				result.setAnonymousClass(ClassifierConverterUtility.convertToAnonymousClass(arr.getAnonymousClassDeclaration()));
			}
			if (arr.getExpression() != null) {
				org.emftext.language.java.references.Reference parent = internalConvertToReference(arr.getExpression());
				parent.setNext(result);
			}
			return result;
		} else if (expr.getNodeType() == ASTNode.FIELD_ACCESS) {
			FieldAccess arr = (FieldAccess) expr;
			org.emftext.language.java.references.Reference parent = internalConvertToReference(arr.getExpression());
			org.emftext.language.java.references.IdentifierReference result = convertToIdentifierReference(arr.getName());
			parent.setNext(result);
			return result;
		} else if (expr.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return convertToMethodCall((MethodInvocation) expr);
		} else if (expr.getNodeType() == ASTNode.QUALIFIED_NAME) {
			QualifiedName arr = (QualifiedName) expr;
			org.emftext.language.java.references.IdentifierReference result = convertToIdentifierReference(arr.getName());
			org.emftext.language.java.references.Reference parent = internalConvertToReference(arr.getQualifier());
			parent.setNext(result);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
			return result;
		} else if (expr.getNodeType() == ASTNode.SIMPLE_NAME) {
			return convertToIdentifierReference((SimpleName) expr);
		} else if (expr.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			ParenthesizedExpression arr = (ParenthesizedExpression) expr;
			org.emftext.language.java.expressions.NestedExpression result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createNestedExpression();
			result.setExpression(ExpressionConverterUtility.convertToExpression(arr.getExpression()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
			return result;
		} else if (expr.getNodeType() == ASTNode.STRING_LITERAL) {
			StringLiteral arr = (StringLiteral) expr;
			org.emftext.language.java.references.StringReference result = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createStringReference();
			result.setValue(arr.getEscapedValue().substring(1, arr.getEscapedValue().length() - 1));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
			return result;
		} else if (expr.getNodeType() == ASTNode.SUPER_FIELD_ACCESS) {
			SuperFieldAccess arr = (SuperFieldAccess) expr;
			org.emftext.language.java.references.SelfReference partOne = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createSelfReference();
			partOne.setSelf(org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createSuper());
			if (arr.getQualifier() != null) {
				org.emftext.language.java.references.Reference parent = internalConvertToReference(arr.getQualifier());
				parent.setNext(partOne);
			}
			org.emftext.language.java.references.IdentifierReference partTwo = convertToIdentifierReference(arr.getName());
			partOne.setNext(partTwo);
			return partTwo;
		} else if (expr.getNodeType() == ASTNode.SUPER_METHOD_INVOCATION) {
			SuperMethodInvocation arr = (SuperMethodInvocation) expr;
			org.emftext.language.java.references.SelfReference partOne = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createSelfReference();
			partOne.setSelf(org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createSuper());
			if (arr.getQualifier() != null) {
				org.emftext.language.java.references.Reference parent = internalConvertToReference(arr.getQualifier());
				parent.setNext(partOne);
			}
			org.emftext.language.java.references.MethodCall partTwo = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createMethodCall();
			arr.typeArguments().forEach(obj -> partTwo.getCallTypeArguments().add(BaseConverterUtility.convertToTypeArgument((Type) obj)));
			arr.arguments().forEach(obj -> partTwo.getArguments().add(ExpressionConverterUtility.convertToExpression((Expression) obj)));
			org.emftext.language.java.members.Method proxy;
			if (arr.getName().resolveBinding() != null) {
				proxy = JDTResolverUtility.getMethod((IMethodBinding) arr.getName().resolveBinding());
			} else {
				proxy = JDTResolverUtility.getClassMethod(arr.getName().getIdentifier());
				proxy.setName(arr.getName().getIdentifier());
			}
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(arr.getName(), proxy);
			partTwo.setTarget(proxy);
			partOne.setNext(partTwo);
			LayoutInformationConverter.convertToMinimalLayoutInformation(partTwo, arr);
			return partTwo;
		} else if (expr.getNodeType() == ASTNode.THIS_EXPRESSION) {
			ThisExpression arr = (ThisExpression) expr;
			org.emftext.language.java.references.SelfReference result = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createSelfReference();
			result.setSelf(org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createThis());
			if (arr.getQualifier() != null) {
				org.emftext.language.java.references.Reference parent = internalConvertToReference(arr.getQualifier());
				parent.setNext(result);
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
			return result;
		} else if (expr.getNodeType() == ASTNode.TYPE_LITERAL) {
			TypeLiteral arr = (TypeLiteral) expr;
			org.emftext.language.java.references.ReflectiveClassReference result = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createReflectiveClassReference();
			org.emftext.language.java.references.Reference parent = internalConvertToReference(arr.getType());
			parent.setNext(result);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
			return result;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.references.MethodCall convertToMethodCall(MethodInvocation arr) {
		org.emftext.language.java.references.Reference parent = null;
		if (arr.getExpression() != null) {
			parent = internalConvertToReference(arr.getExpression());
		}
		org.emftext.language.java.references.MethodCall result = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createMethodCall();
		arr.typeArguments().forEach(obj -> result.getCallTypeArguments().add(BaseConverterUtility.convertToTypeArgument((Type) obj)));
		arr.arguments().forEach(obj -> result.getArguments().add(ExpressionConverterUtility.convertToExpression((Expression) obj)));
		IMethodBinding methBind = arr.resolveMethodBinding();
		org.emftext.language.java.members.Method methodProxy = null;
		if (methBind != null) {
			 methodProxy = JDTResolverUtility.getMethod(methBind);
		} else {
			methodProxy = JDTResolverUtility.getClassMethod(arr.getName().getIdentifier());
			methodProxy.setName(arr.getName().getIdentifier());
		}
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(arr.getName(), methodProxy);
		result.setTarget(methodProxy);
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
		if (parent != null) {
			parent.setNext(result);
		}
		return result;
	}
	
	private static org.emftext.language.java.references.IdentifierReference convertToIdentifierReference(Name name) {
		if (name.isSimpleName()) {
			return convertToIdentifierReference((SimpleName) name);
		} else { // name.isQualifiedName()
			QualifiedName qualifiedName = (QualifiedName) name;
			org.emftext.language.java.references.IdentifierReference parent = convertToIdentifierReference(qualifiedName.getQualifier());
			org.emftext.language.java.references.IdentifierReference child = convertToIdentifierReference(qualifiedName.getName());
			parent.setNext(child);
			return child;
		}
	}
	
	private static org.emftext.language.java.references.IdentifierReference convertToIdentifierReference(SimpleName name) {
		org.emftext.language.java.references.IdentifierReference result = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createIdentifierReference();
		IBinding b = name.resolveBinding();
		org.emftext.language.java.references.ReferenceableElement target = null;
		if (b == null || b.isRecovered()) {
			target = JDTResolverUtility.getReferenceableElementByNameMatching(name.getIdentifier());
		} else if (b instanceof ITypeBinding) {
			target = JDTResolverUtility.getClassifier((ITypeBinding) b);
		} else if (b instanceof IVariableBinding) {
			target = JDTResolverUtility.getReferencableElement((IVariableBinding) b);
		} else if (b instanceof IMethodBinding) {
			target = JDTResolverUtility.getMethod((IMethodBinding) b);
		} else if (b instanceof IPackageBinding) {
			target = JDTResolverUtility.getPackage((IPackageBinding) b);
		} else {
			target = JDTResolverUtility.getReferenceableElementByNameMatching(name.getIdentifier());
		}
		target.setName(name.getIdentifier());
		result.setTarget(target);
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, name);
		return result;
	}
	
	static org.emftext.language.java.references.Reference convertToReference(Type t) {
		return walkUp(internalConvertToReference(t));
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.references.Reference internalConvertToReference(Type t) {
		if (t.isNameQualifiedType()) {
			NameQualifiedType nqType = (NameQualifiedType) t;
			org.emftext.language.java.references.IdentifierReference parent = convertToIdentifierReference(nqType.getQualifier());
			org.emftext.language.java.references.IdentifierReference child = convertToIdentifierReference(nqType.getName());
			parent.setNext(child);
			nqType.annotations().forEach(obj -> child.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(child, nqType);
			return child;
		} else if (t.isQualifiedType()) {
			QualifiedType qType = (QualifiedType) t;
			org.emftext.language.java.references.Reference parent = internalConvertToReference(qType.getQualifier());
			org.emftext.language.java.references.IdentifierReference child = convertToIdentifierReference(qType.getName());
			qType.annotations().forEach(obj -> child.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			parent.setNext(child);
			LayoutInformationConverter.convertToMinimalLayoutInformation(child, qType);
			return child;
		} else if (t.isSimpleType()) {
			SimpleType sType = (SimpleType) t;
			org.emftext.language.java.references.IdentifierReference result = convertToIdentifierReference(sType.getName());
			sType.annotations().forEach(obj -> result.getAnnotations().add(AnnotationInstanceOrModifierConverterUtility
				.convertToAnnotationInstance((Annotation) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, sType);
			return result;
		} else if (t.isPrimitiveType()) {
			org.emftext.language.java.types.TypeReference typeRef = BaseConverterUtility.convertToTypeReference(t);
			org.emftext.language.java.references.PrimitiveTypeReference temp = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createPrimitiveTypeReference();
			temp.setPrimitiveType((org.emftext.language.java.types.PrimitiveType) typeRef);
			temp.getLayoutInformations().addAll(typeRef.getLayoutInformations());
			return temp;
		} else if (t.isArrayType()) {
			ArrayType arr = (ArrayType) t;
			org.emftext.language.java.references.Reference result = internalConvertToReference(arr.getElementType());
			if (arr.getElementType().isPrimitiveType()) {
				org.emftext.language.java.references.PrimitiveTypeReference primRef =
					(org.emftext.language.java.references.PrimitiveTypeReference) result;
				BaseConverterUtility.convertToArrayDimensionsAndSet(arr, primRef);
			} else {
				org.emftext.language.java.references.IdentifierReference idRef =
					(org.emftext.language.java.references.IdentifierReference) result;
				BaseConverterUtility.convertToArrayDimensionsAndSet(arr, idRef);
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, arr);
			return result;
		}
		return null;
	}
	
	static org.emftext.language.java.references.Reference convertToReference(Statement st) {
		return walkUp(internalConvertToReference(st));
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.references.Reference internalConvertToReference(Statement st) {
		if (st.getNodeType() == ASTNode.CONSTRUCTOR_INVOCATION) {
			ConstructorInvocation invoc = (ConstructorInvocation) st;
			org.emftext.language.java.instantiations.ExplicitConstructorCall result = org.emftext.language.java.instantiations.InstantiationsFactory.eINSTANCE.createExplicitConstructorCall();
			invoc.typeArguments().forEach(obj -> result.getCallTypeArguments().add(BaseConverterUtility.convertToTypeArgument((Type) obj)));
			result.setCallTarget(org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createThis());
			invoc.arguments().forEach(obj -> result.getArguments().add(ExpressionConverterUtility.convertToExpression((Expression) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, invoc);
			return result;
		} else if (st.getNodeType() == ASTNode.SUPER_CONSTRUCTOR_INVOCATION) {
			SuperConstructorInvocation invoc = (SuperConstructorInvocation) st;
			org.emftext.language.java.instantiations.ExplicitConstructorCall result = org.emftext.language.java.instantiations.InstantiationsFactory.eINSTANCE.createExplicitConstructorCall();
			invoc.typeArguments().forEach(obj -> result.getCallTypeArguments().add(BaseConverterUtility.convertToTypeArgument((Type) obj)));
			result.setCallTarget(org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createSuper());
			invoc.arguments().forEach(obj -> result.getArguments().add(ExpressionConverterUtility.convertToExpression((Expression) obj)));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, invoc);
			if (invoc.getExpression() != null) {
				org.emftext.language.java.references.Reference parent = internalConvertToReference(invoc.getExpression());
				parent.setNext(result);
			}
			return result;
		}
		return null;
	}
}
