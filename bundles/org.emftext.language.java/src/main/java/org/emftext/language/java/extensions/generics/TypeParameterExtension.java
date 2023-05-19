/*******************************************************************************
 * Copyright (c) 2006-2014
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
package org.emftext.language.java.extensions.generics;

import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.commons.Commentable;
import org.emftext.language.java.expressions.AssignmentExpression;
import org.emftext.language.java.expressions.CastExpression;
import org.emftext.language.java.expressions.ConditionalExpression;
import org.emftext.language.java.expressions.ExplicitlyTypedLambdaParameters;
import org.emftext.language.java.expressions.Expression;
import org.emftext.language.java.expressions.LambdaExpression;
import org.emftext.language.java.expressions.MethodReferenceExpression;
import org.emftext.language.java.expressions.NestedExpression;
import org.emftext.language.java.extensions.types.TypeReferenceExtension;
import org.emftext.language.java.generics.QualifiedTypeArgument;
import org.emftext.language.java.generics.TypeArgument;
import org.emftext.language.java.generics.TypeArgumentable;
import org.emftext.language.java.generics.TypeParameter;
import org.emftext.language.java.generics.TypeParametrizable;
import org.emftext.language.java.instantiations.NewConstructorCall;
import org.emftext.language.java.literals.Super;
import org.emftext.language.java.members.AdditionalField;
import org.emftext.language.java.members.Field;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.modifiers.AnnotableAndModifiable;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.references.ElementReference;
import org.emftext.language.java.references.MethodCall;
import org.emftext.language.java.references.Reference;
import org.emftext.language.java.references.ReferenceableElement;
import org.emftext.language.java.references.ReflectiveClassReference;
import org.emftext.language.java.references.SelfReference;
import org.emftext.language.java.types.ClassifierReference;
import org.emftext.language.java.types.InferableType;
import org.emftext.language.java.types.PrimitiveType;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;
import org.emftext.language.java.types.TypedElement;
import org.emftext.language.java.util.TemporalCompositeTypeReference;
import org.emftext.language.java.variables.AdditionalLocalVariable;
import org.emftext.language.java.variables.LocalVariable;

public class TypeParameterExtension {
	
	/**
	 * @param me the type parameter.
	 * @return all super classifiers.
	 */
	public static EList<ConcreteClassifier> getAllSuperClassifiers(TypeParameter me) {
		EList<ConcreteClassifier> result = new UniqueEList<ConcreteClassifier>();
		for (TypeReference typeRef : me.getExtendTypes()) {
			Type type = typeRef.getTarget();
			if (type instanceof ConcreteClassifier) {
				ConcreteClassifier concreteClassifier = (ConcreteClassifier) type;
				result.add(concreteClassifier);
			}
			if (type instanceof Classifier) {
				Classifier classifier = (Classifier) type;
				result.addAll(classifier.getAllSuperClassifiers());	
			}
		}		
		
		return result;
	}
	
	/**
	 * Returns all members of the given classifier including inner classes and 
	 * all members of super types (extended classes and implemented interfaces).
	 * 
	 * @param me the type parameter.
	 * @param context to check protected visibility.
	 * @return member list.
	 */
	public static EList<Member> getAllMembers(TypeParameter me, Commentable context) {
		EList<Member> memberList = new UniqueEList<Member>();

		UniqueEList<Type> possiblyVisibleSuperClassifier = new UniqueEList<Type>();
		for (TypeReference typeReference : me.getExtendTypes()) {
			Type target = typeReference.getTarget();
			possiblyVisibleSuperClassifier.add(target);
		}
		
		for (ConcreteClassifier superClassifier : me.getAllSuperClassifiers()) {
			for (Member member : superClassifier.getMembers()) {
				if (member instanceof AnnotableAndModifiable) {					
					AnnotableAndModifiable modifiable = (AnnotableAndModifiable) member;

					if (!modifiable.isHidden(context)) {
						memberList.add(member);
					} else if (possiblyVisibleSuperClassifier.contains(superClassifier)) {
						memberList.add(member);
					}
				} else {
					memberList.add(member);
				}
			}
			memberList.addAll(superClassifier.getDefaultMembers());
		}
		return memberList;
	}
	
	/**
	 * Returns the type bound to the given parameter in the context of the given
	 * reference.
	 * 
	 * @param me the type parameter.
	 * @param typeReference
	 * @param reference the context.
	 * @return bound type or parameter if not bound.
	 */
	public static Type getBoundType(TypeParameter me,
			TypeReference typeReference, Reference reference) {
		TypeReference ref = me.getBoundTypeReference(typeReference, reference);
		if (ref != null) {
			if (ref instanceof TemporalCompositeTypeReference) {
				return ((TemporalCompositeTypeReference) ref).asType();
			}
			return ref.getTarget();
		}
		return null;
	}

	public static TypeReference getBoundTypeReference(TypeParameter me, TypeReference typeReference,
			Reference reference) {
		EList<TypeReference> resultList = new BasicEList<>();
		
		TypeParametrizable typeParameterDeclarator = (TypeParametrizable) me.eContainer();
		Reference parentReference = null;
		EList<TypeReference> prevTypeList = new UniqueEList<>();
		
		if (reference != null
				&& reference.getPrevious() instanceof NestedExpression) {
			
			NestedExpression nestedExpression = (NestedExpression) reference.getPrevious();
			Expression expression = null;
			Expression nestedExpressionExpression = nestedExpression.getExpression();
			if (nestedExpressionExpression instanceof Reference) {
				expression = nestedExpressionExpression;
			} else if (nestedExpressionExpression instanceof ConditionalExpression) {
				ConditionalExpression conditionalExpression = (ConditionalExpression) nestedExpressionExpression;
				expression = conditionalExpression.getExpressionIf();
			}
			
			if (expression instanceof Reference) {
				Reference expressionReference = (Reference) expression;
				// Navigate down references
				while (expressionReference.getNext() != null) {
					expressionReference = expressionReference.getNext();
				}
				
				parentReference = expressionReference;
				TypeReference prevType = nestedExpressionExpression.getOneTypeReference(false);
				if (prevType instanceof TemporalCompositeTypeReference) {
					for (TypeReference nextSuperType : ((TemporalCompositeTypeReference) prevType)
							.getTypeReferences()) {
						prevTypeList.add(nextSuperType);
					}
				} else {
					prevTypeList.add(prevType);
				}
			} else if (nestedExpressionExpression instanceof CastExpression) {
				CastExpression castExpression = (CastExpression) nestedExpressionExpression;
				prevTypeList.add(castExpression.getTypeReference());
				for (TypeReference typeRef : castExpression.getAdditionalBounds()) {
					prevTypeList.add(typeRef);
				}
			}
		} else if (reference != null && reference.getPrevious() != null) {
			parentReference = reference.getPrevious();
			while (parentReference instanceof SelfReference) {
				if (((SelfReference) parentReference).getSelf() instanceof Super) {
					if (parentReference.eContainer() instanceof Reference) {
						parentReference = (Reference) parentReference.eContainer();
					} else {
						ConcreteClassifier containingClassifier =
								reference.getContainingConcreteClassifier();
						if (containingClassifier != null) {
							prevTypeList.add(TypeReferenceExtension
									.convertToTypeReference(containingClassifier));
						}
						parentReference = null;
					}
				} else {
					break;
				}
			}
			
			if (parentReference != null) {
				TypeReference prevType = parentReference.getReferencedTypeReference();
				if (prevType instanceof TemporalCompositeTypeReference) {
					for (TypeReference aType : ((TemporalCompositeTypeReference) prevType)
							.getTypeReferences()) {
						prevTypeList.add(aType);
					}
				} else {
					prevTypeList.add(prevType);
				}
			}
		} else if (reference != null) {
			// Previous type is one of the containing classes which can still bind
			// by inheritance.
			ConcreteClassifier containingClassifier = reference.getContainingConcreteClassifier();
			while (containingClassifier != null) {
				prevTypeList.add(TypeReferenceExtension.convertToTypeReference(containingClassifier));
				EObject container = containingClassifier.eContainer();
				if (container instanceof Commentable) {
					Commentable commentableContainer = (Commentable) container;
					containingClassifier = commentableContainer.getContainingConcreteClassifier();
				} else {
					containingClassifier = null;
				}
			}
		}
		
		for (TypeReference prevType : prevTypeList) {
			if (prevType == null) {
				continue;
			}
			int typeParameterIndex = -1;
			if (typeParameterDeclarator instanceof ConcreteClassifier) {
				typeParameterIndex = typeParameterDeclarator.getTypeParameters().indexOf(me);
				if (reference != null) {
					ClassifierReference classifierReference = null;
					if (parentReference instanceof ElementReference) {
						ElementReference parentElementReference = (ElementReference) parentReference;
						ReferenceableElement prevReferenced = parentElementReference.getTarget();
						if (prevReferenced instanceof TypedElement) {
							TypeReference prevTypeReference = parentElementReference
									.getReferencedTypeReference();
							if (prevTypeReference != null) {
								classifierReference = prevTypeReference
										.getPureClassifierReference();
							}
						}
					}
					
					if (parentReference instanceof TypedElement) {
						// e.g. New Constructor Call
						TypeReference prevParentReference = ((TypedElement) parentReference)
								.getTypeReference();
						if (prevParentReference != null) {
							classifierReference = prevParentReference.getPureClassifierReference();
						}
					}
					
					EList<TypeArgument> typeArgumentList;
					if (prevType instanceof TypeArgumentable) {
						typeArgumentList = ((TypeArgumentable) prevType).getTypeArguments();
					} else if (classifierReference != null) {
						typeArgumentList = classifierReference.getTypeArguments();
					} else {
						typeArgumentList = ECollections.emptyEList();
					}
					
					if (typeParameterIndex < typeArgumentList.size())  {
						TypeArgument arg = typeArgumentList.get(typeParameterIndex);
						TypeReference theTypeRef = TypeReferenceExtension
								.getTypeReferenceOfTypeArgument(arg);
						if (theTypeRef != null) {
							TypeReference theType = theTypeRef
									.getBoundTargetReference(parentReference);
							resultList.add(0, theType);
						}
					}
					
					Type prevTypeTarget = prevType.getTarget();
					if (prevTypeTarget instanceof ConcreteClassifier) {
						// Bound through inheritance?
						int idx = 0;
						for (ClassifierReference superClassifierReference
								: ((ConcreteClassifier) prevTypeTarget)
								.getSuperTypeReferences()) {
							if (typeParameterIndex < superClassifierReference
									.getTypeArguments().size())  {
								// Is this an argument for the correct class?
								if (typeParameterDeclarator.equals(
										superClassifierReference.getTarget())) {					 
									TypeArgument arg = superClassifierReference
											.getTypeArguments()
											.get(typeParameterIndex);
									if (arg instanceof QualifiedTypeArgument) {
										TypeReference argRef =
												((QualifiedTypeArgument) arg)
												.getTypeReference();
										if (idx > 0 || (idx == 0
												&& resultList.isEmpty())) {
											resultList.add(idx, argRef);
											idx++;
										}
									}
								}
	
							}
						}
						
						
			
					} else if (prevTypeTarget instanceof TypeParameter) {
						// The previous type parameter, although unbound,
						// may contain type restrictions through extends.
						resultList.add(prevType);
						for (TypeReference extendedRef : ((TypeParameter) prevTypeTarget)
								.getExtendTypes()) {
							ConcreteClassifier extended = (ConcreteClassifier)
									extendedRef.getTarget();
							int idx = ((TypeParametrizable) prevTypeTarget.eContainer())
									.getTypeParameters().indexOf(prevTypeTarget);
							if (extended.getTypeParameters().size() > idx) {
								// Also, add more precise bindings from extensions.
								resultList.add(TypeReferenceExtension.convertToTypeReference(
										extended.getTypeParameters().get(idx)));
							}
						}
					}
				}
				if (reference != null && reference.eContainer() instanceof ReflectiveClassReference) {
					if (reference.eContainer().eContainer() instanceof Reference) {
						// The ".class" instantiation implicitly binds the T parameter
						// of java.lang.Class to the class itself.
						resultList.add(0, ((Reference) reference.eContainer().eContainer())
								.getReferencedTypeReference());
					}
				}
			}
		}
		
		if (typeParameterDeclarator instanceof Method) {
			if (reference instanceof MethodCall) {
				Method method = (Method) typeParameterDeclarator;
				MethodCall methodCall = (MethodCall) reference;
				inferTypeReferenceFromMethod(me, typeReference, reference,
						method, methodCall, resultList, parentReference);
			}
		}
		
		// Remove nulls.
		for (Iterator<?> it = resultList.iterator(); it.hasNext();) {
			if (it.next() == null) {
				it.remove();
			}
		}
		
		if (resultList.isEmpty()
				|| (resultList.size() == 1 && !(resultList.get(0) instanceof InferableType)
				&& resultList.get(0).getTarget() != null && resultList.get(0).getTarget().equals(me))) {
			if (me.getExtendTypes().size() > 0 && typeParameterDeclarator instanceof ConcreteClassifier) {
				TypeReference result = me.getExtendTypes().get(0);
				return result;
			}
			return TypeReferenceExtension.convertToTypeReference(me);
		} else if (resultList.size() == 1) {
			return resultList.get(0);
		} else {
			TemporalCompositeTypeReference temp = new TemporalCompositeTypeReference();
			for (TypeReference aResult : resultList) {
				if (aResult instanceof TemporalCompositeTypeReference) {
					// Flatten.
					temp.getTypeReferences().addAll(
							((TemporalCompositeTypeReference) aResult).getTypeReferences());
				} else {
					temp.getTypeReferences().add(aResult);	
				}
			}
			temp.getTypeReferences().add(TypeReferenceExtension.convertToTypeReference(me));
			return temp;
		}
	}
	
	private static void inferTypeReferenceFromMethod(TypeParameter me, TypeReference typeReference,
			Reference reference, Method method, MethodCall methodCall, EList<TypeReference> resultList,
			Reference parentReference) {
		if (!method.equals(methodCall.getTarget())) {
			return;
		}
		if (method.getTypeParameters().size() == methodCall.getCallTypeArguments().size()) {
			TypeArgument typeArgument = methodCall.getCallTypeArguments()
					.get(method.getTypeParameters().indexOf(me));
			resultList.add(0, TypeReferenceExtension.getTypeReferenceOfTypeArgument(typeArgument)
					.getBoundTargetReference(parentReference)); 
		}

		// Class type parameter
		int idx = method.getParameters().indexOf(typeReference.eContainer());
		
		// Method type parameter
		if (idx == -1) {
			int hasIgnoredLambdaParameterIdx = -1;
			for (Parameter parameter : method.getParameters()) {
				int oldIdx = idx;
				for (TypeArgument typeArgument : parameter.getTypeArguments()) {
					TypeReference argRef = TypeReferenceExtension
							.getTypeReferenceOfTypeArgument(typeArgument);
					if (argRef != null && me.equals(argRef.getTarget())) {
						idx = method.getParameters().indexOf(parameter);
					}
				}
				ClassifierReference paramTypeReference = parameter
						.getTypeReference().getPureClassifierReference();
				if (paramTypeReference != null) {
					for (TypeArgument typeArgument : paramTypeReference.getTypeArguments()) {
						TypeReference argRef = TypeReferenceExtension.
								getTypeReferenceOfTypeArgument(typeArgument);
						if (argRef != null && me.equals(argRef.getTarget())) {
							idx = method.getParameters().indexOf(parameter);
						}
					}
					Type paramType = paramTypeReference.getTarget();
					if (paramType instanceof TypeParameter
							&& ((TypeParameter) paramType).equals(me)) {
						idx = method.getParameters().indexOf(parameter);
					}
				}
				Expression arg = methodCall.getArguments().get(method.getParameters().indexOf(parameter));
				LambdaExpression lambda = arg instanceof LambdaExpression
						? (LambdaExpression) arg
						: arg.getFirstChildByType(LambdaExpression.class);
				if (lambda != null) {
					if (!(lambda.getParameters() instanceof ExplicitlyTypedLambdaParameters)) {
						if (oldIdx != idx) {
							hasIgnoredLambdaParameterIdx = idx;
							idx = oldIdx;
						}
					}
				}
			}
			if (hasIgnoredLambdaParameterIdx > -1 && idx == -1) {
				idx = hasIgnoredLambdaParameterIdx;
			}
		}
		
		if (idx < methodCall.getArguments().size() && idx >= 0) {
			Expression argument = methodCall.getArguments().get(idx);
			Parameter parameter = method.getParameters().get(idx);
			ClassifierReference parameterType = parameter.getTypeReference().getPureClassifierReference();
			if (argument instanceof NewConstructorCall) {
				ClassifierReference argumentType = ((NewConstructorCall) argument)
						.getTypeReference().getPureClassifierReference();
				if (argumentType != null
						&& parameterType.getTypeArguments().size()
							== argumentType.getTypeArguments().size()) {
					for (TypeArgument typeArgument : parameterType.getTypeArguments()) {
						TypeReference argRef = TypeReferenceExtension.
								getTypeReferenceOfTypeArgument(typeArgument);
						if (argRef != null && argRef.getTarget().equals(me)) {
							resultList.add(0, TypeReferenceExtension.
									getTypeReferenceOfTypeArgument(argumentType
									.getTypeArguments().get(
									parameterType.getTypeArguments().indexOf(typeArgument))));
						}
					}
				}
				if (argumentType != null && parameterType.getTarget() instanceof TypeParameter) {
					resultList.add(0, argumentType);
				}
			} else if (parameterType != null && argument instanceof Reference) {
				Reference argReference = (Reference) argument;
				
				while (argReference.getNext() instanceof Reference
						&& !(argReference.getNext() instanceof ReflectiveClassReference)) {
					argReference = argReference.getNext();
				}

				if (argReference instanceof ElementReference) {
					ElementReference elementReference = (ElementReference) argReference;
					while (elementReference.getNext() instanceof ElementReference) {
						elementReference = (ElementReference) elementReference.getNext();
					}
					if (elementReference.getTarget() instanceof TypedElement) {
						TypeReference typeRef = ((TypedElement) elementReference.getTarget())
								.getTypeReference();
						if (typeRef != null) {
							ClassifierReference argumentType = typeRef.getPureClassifierReference();
							if (argumentType != null
									&& parameterType.getTypeArguments().size()
									== argumentType.getTypeArguments().size()) {
								for (TypeArgument typeArgument
										: parameterType.getTypeArguments()) {
									if (typeArgument instanceof QualifiedTypeArgument) {
										if (((QualifiedTypeArgument) typeArgument)
												.getTypeReference().getTarget()
												.equals(me)) {
											int idx2 = parameterType
													.getTypeArguments()
													.indexOf(typeArgument);
											resultList.add(0, TypeReferenceExtension
													.getTypeReferenceOfTypeArgument(
													argumentType.getTypeArguments().get(idx2)));
										}
									}
								}
							}
							if (parameterType.getTarget().equals(me)) {
								if (argumentType != null) {
									resultList.add(0, argumentType);
								} else {
									resultList.add(0, typeRef);
								}
							}
						}
					} else if (elementReference.getNext() == null) {
						if (parameter.getTypeReference().getTarget().equals(me)) {
							resultList.add(elementReference.getReferencedTypeReference());
						}
					}
					if (elementReference.getNext() instanceof ReflectiveClassReference) {
						if (parameterType.getTypeArguments().size() == 1) {
							for (TypeArgument typeArgument : parameterType.getTypeArguments()) {
								if (typeArgument instanceof QualifiedTypeArgument) {
									if (((QualifiedTypeArgument) typeArgument)
											.getTypeReference()
											.getTarget().equals(me)) {
										resultList.add(0, elementReference
												.getReferencedTypeReference());
									}
								}
							}
						}
					}
				} else {
					if (parameterType.getTarget() instanceof TypeParameter) {
						while (argReference.getNext() instanceof Reference) {
							argReference = argReference.getNext();
						}
						resultList.add(0, ((Reference) argReference).getReferencedTypeReference());
					}
				}
			} else {
				LambdaExpression lambda = argument instanceof LambdaExpression
						? (LambdaExpression) argument
						: argument.getFirstChildByType(LambdaExpression.class);
				if (lambda != null) {
					Interface narrowedParameterType = (Interface) parameterType.getTarget();
					TypeParameter correspondingTypeParameter = null;
					for (int typeArgIndex = 0; typeArgIndex
							< parameterType.getTypeArguments().size(); typeArgIndex++) {
						TypeArgument typeArg = parameterType.getTypeArguments().get(typeArgIndex);
						TypeReference typeArgRef = TypeReferenceExtension
								.getTypeReferenceOfTypeArgument(typeArg);
						if (typeArgRef.getTarget().equals(me)) {
							correspondingTypeParameter = narrowedParameterType
									.getTypeParameters().get(typeArgIndex);
						}
					}
					Method correspondingMethod = narrowedParameterType
							.getAbstractMethodOfFunctionalInterface();
					Type potT = correspondingMethod.getTypeReference().getTarget();
					if (potT.equals(correspondingTypeParameter)) {
						Type ret = lambda.getReturnType(null);
						if (ret != null) {
							resultList.add(0, TypeReferenceExtension.convertToTypeReference(ret));
						}
					} else {
						for (int cmIndex = 0; cmIndex < correspondingMethod.getParameters().size();
								cmIndex++) {
							potT = correspondingMethod.getParameters().get(cmIndex)
									.getTypeReference().getTarget();
							if (potT.equals(correspondingTypeParameter)) {
								TypeReference potRes = lambda.getParameters().getParameters()
										.get(cmIndex).getTypeReference();
								if (!(potRes instanceof InferableType)) {
									resultList.add(0, potRes);
								}
							}
						}
					}
				} else {
					MethodReferenceExpression methodRef = argument instanceof MethodReferenceExpression
							? (MethodReferenceExpression) argument
							: argument.getFirstChildByType(MethodReferenceExpression.class);
					if (methodRef != null) {
					} else {
						resultList.add(0, argument.getOneTypeReference(false));
					}
				}
			}
		}
		
		// Return type
		if (method.equals(typeReference.eContainer())) {
			// Bound by the type of a method argument?
			HashSet<Classifier> allSuperTypes = new HashSet<>();
			for (Parameter parameter : method.getParameters()) {
				if (me.equals(parameter.getTypeReference().getTarget())) {
					idx = method.getParameters().indexOf(parameter);
					Type originalArgType = methodCall.getArguments().get(idx).getType();
					Classifier argumentType;
					if (originalArgType instanceof PrimitiveType) {
						argumentType = ((PrimitiveType) originalArgType).wrapPrimitiveType();
					} else {
						argumentType = (Classifier) originalArgType;
					}
					allSuperTypes.add(argumentType);
					allSuperTypes.addAll(argumentType.getAllSuperClassifiers());
				}
			}
			// All types given by all bindings.
			for (Classifier classifier : allSuperTypes) {
				resultList.add(TypeReferenceExtension.convertToTypeReference(classifier));
			}
		}
		
		if (resultList.isEmpty()) {
			resultList.addAll(inferTypeFromContext(me, typeReference, reference, method));
		}
	}
	
	/**
	 * Tries to infer a bound type for an unbound type parameter declared for a method. The bound is inferred for a
	 * method call and from the context of the method call, i. e., another method call contains the method call
	 * or the result of the method call is assigned to a variable with an explicit type.
	 * 
	 * @param me the type parameter to find a bound for.
	 * @param typeReference
	 * @param reference context of the type parameter.
	 * @param method the method for which the type parameter is declared.
	 * @return a list of potential bounds.
	 */
	private static BasicEList<TypeReference> inferTypeFromContext(TypeParameter me, TypeReference typeReference,
			Reference reference, Method method) {
		BasicEList<TypeReference> resultList = new BasicEList<>();
		if (me.getExtendTypes().size() != 0) {
			return resultList;
		}
		Reference prevReference = reference;
		while (prevReference.getPrevious() != null) {
			prevReference = prevReference.getPrevious();
		}
		EObject contained = prevReference;
		EObject container = contained.eContainer();
		while (container != null && !(container instanceof MethodCall)
				&& !(container instanceof AssignmentExpression)
				&& !(container instanceof CastExpression)
				&& container instanceof Expression) {
			contained = container;
			container = contained.eContainer();
		}
		if (container instanceof MethodCall) {
			// Method call is contained within another method call.
			MethodCall containingCall = (MethodCall) container;
			int index = containingCall.getArguments().indexOf(contained);
			Method containingMethod = (Method) containingCall.getTarget();
			if (containingMethod != null && !containingMethod.eIsProxy()) {
				TypeReference typeRef = ((Method) containingCall.getTarget())
						.getParameters().get(index).getTypeReference();
				if (typeRef instanceof TypeArgumentable) {
					int indx = method.getTypeParameters().indexOf(me);
					TypeArgumentable argumentable = (TypeArgumentable) typeRef;
					if (argumentable.getTypeArguments().size() > indx) {
						TypeArgument typeArg = argumentable.getTypeArguments().get(indx);
						TypeReference argRef = TypeReferenceExtension
								.getTypeReferenceOfTypeArgument(typeArg);
						if (argRef != null) {
							resultList.add(0, argRef.getBoundTargetReference(containingCall));
						}
					} else {
						resultList.add(typeRef.getBoundTargetReference(containingCall));
					}
				} else {
					resultList.add(typeRef.getBoundTargetReference(containingCall));
				}
			}
		} else {
			// Result of the method call is assigned to a variable with a type.
			TypeReference ref = null;
			if (container instanceof LocalVariable) {
				ref = ((LocalVariable) container).getTypeReference();
			} else if (container instanceof AdditionalLocalVariable) {
				ref = ((AdditionalLocalVariable) container).getTypeReference();
			} else if (container instanceof Field) {
				ref = ((Field) container).getTypeReference();
			} else if (container instanceof AdditionalField) {
				ref = ((AdditionalField) container).getTypeReference();
			} else if (container instanceof AssignmentExpression) {
				ref = ((AssignmentExpression) container).getChild().getOneTypeReference(false);
			} else if (container instanceof CastExpression) {
				ref = ((CastExpression) container).getTypeReference();
			}
			if (ref != null) {
				TypeReference potType = searchForTypeParameter(me, method.getTypeReference(), ref);
				if (potType != null) {
					resultList.add(potType);
				}
			}
		}
		return resultList;
	}
	
	/**
	 * Looks into a type reference to find the use of a type parameter while finding an instantiation in
	 * another type reference. Type arguments are also considered.
	 * 
	 * @param me the type parameter which is looked for.
	 * @param searchReference type reference in which the type parameter is searched.
	 * @param targetReference type reference from which the instantiation is taken.
	 * @return the found type or null if no type could be found.
	 */
	private static TypeReference searchForTypeParameter(TypeParameter me, TypeReference searchReference,
			TypeReference targetReference) {
		if (searchReference == null || targetReference == null) {
			return null;
		}
		if (searchReference.getTarget().equals(me)) {
			return targetReference;
		} else if (searchReference instanceof TypeArgumentable
				&& targetReference instanceof TypeArgumentable) {
			TypeArgumentable typeArg = (TypeArgumentable) searchReference;
			TypeArgumentable targetArg = (TypeArgumentable) targetReference;
			for (int i = 0; i < typeArg.getTypeArguments().size(); i++) {
				TypeArgument arg = typeArg.getTypeArguments().get(i);
				TypeReference refOfArg = TypeReferenceExtension.getTypeReferenceOfTypeArgument(arg);
				TypeReference potResult = searchForTypeParameter(me, refOfArg,
						TypeReferenceExtension.getTypeReferenceOfTypeArgument(
								targetArg.getTypeArguments().get(i)));
				if (potResult != null) {
					return potResult;
				}
			}
		}
		return null;
	}
}
