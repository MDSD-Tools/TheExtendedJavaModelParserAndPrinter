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
package org.emftext.language.java.extensions.references;

import org.emftext.language.java.classifiers.AnonymousClass;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Enumeration;
import org.emftext.language.java.expressions.NestedExpression;
import org.emftext.language.java.extensions.types.TypeReferenceExtension;
import org.emftext.language.java.generics.ExtendsTypeArgument;
import org.emftext.language.java.generics.QualifiedTypeArgument;
import org.emftext.language.java.generics.SuperTypeArgument;
import org.emftext.language.java.generics.TypeArgument;
import org.emftext.language.java.generics.TypeArgumentable;
import org.emftext.language.java.generics.TypeParameter;
import org.emftext.language.java.instantiations.ExplicitConstructorCall;
import org.emftext.language.java.literals.Literal;
import org.emftext.language.java.literals.Super;
import org.emftext.language.java.literals.This;
import org.emftext.language.java.members.AdditionalField;
import org.emftext.language.java.members.EnumConstant;
import org.emftext.language.java.references.ElementReference;
import org.emftext.language.java.references.PrimitiveTypeReference;
import org.emftext.language.java.references.Reference;
import org.emftext.language.java.references.ReferenceableElement;
import org.emftext.language.java.references.ReflectiveClassReference;
import org.emftext.language.java.references.SelfReference;
import org.emftext.language.java.references.StringReference;
import org.emftext.language.java.references.TextBlockReference;
import org.emftext.language.java.types.ClassifierReference;
import org.emftext.language.java.types.InferableType;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;
import org.emftext.language.java.types.TypedElement;
import org.emftext.language.java.util.TemporalCompositeTypeReference;
import org.emftext.language.java.variables.AdditionalLocalVariable;

public class ReferenceExtension {

	public static Reference getPrevious(Reference me) {
		if (me.eContainer() instanceof Reference) { 
			Reference container = (Reference) me.eContainer(); 
			if (me.equals(container.getNext())) {
				return container;
			}
		}
		return null;
	}
	
	/**
	 * Determines the {@link Type} of the reference. That is, either the type to
	 * which the reference points directly, or the type of the element to which
	 * the reference points.
	 * 
	 * @param me the given reference.
	 * @return the determined type
	 */
	public static Type getReferencedType(Reference me) {
		TypeReference ref = getReferencedTypeReference(me);
		if (ref != null) {
			ref = ref.getBoundTargetReference(me);
		}
		if (ref instanceof TemporalCompositeTypeReference) {
			return ((TemporalCompositeTypeReference) ref).asType();
		}
		return ref == null ? null : ref.getTarget();
	}
	
	public static TypeReference getReferencedTypeReference(Reference me) {
		if (me.getActualTargets().size() > 0) {
			return me.getActualTargets().get(0);
		}
		
		if (me instanceof Literal) {
			return TypeReferenceExtension.convertToTypeReference(((Literal) me).getType());
		}

		TypeReference type = null;

		if (me instanceof TypedElement) {
			// Referenced element points to a type.
			type = ((TypedElement) me).getTypeReference();
		} else if (me instanceof SelfReference) {
			// Element points to this or super.
			TypeReference thisClassRef = null;
			Type thisClass = null;
			if (me.getPrevious() != null) {
				thisClassRef = me.getPrevious().getReferencedTypeReference();
				thisClass = thisClassRef.getTarget();
			} else {
				AnonymousClass anonymousContainer = me.getContainingAnonymousClass();
				if (anonymousContainer != null) {
					thisClass = anonymousContainer;
				} else {
					thisClass = me.getContainingConcreteClassifier();
				}
			}
			
			// Find super class if "self" is "super".
			if (((SelfReference) me).getSelf() instanceof Super) {
				if (thisClass instanceof org.emftext.language.java.classifiers.Class) {
					return TypeReferenceExtension.convertToTypeReference(
						((org.emftext.language.java.classifiers.Class) thisClass).getSuperClass());
				}
				if (thisClass instanceof AnonymousClass) {
					return TypeReferenceExtension.convertToTypeReference(
							((AnonymousClass) thisClass).getSuperClassifier());
				}
			}
			
			type = thisClassRef != null ? thisClassRef : TypeReferenceExtension.convertToTypeReference(thisClass);
		} else if (me instanceof ReflectiveClassReference) {
			// Element points to the object's class object.
			return TypeReferenceExtension.convertToTypeReference(me.getClassClass());
		} else if (me instanceof ElementReference) {
			// Referenced element points to an element with a type.
			ReferenceableElement target = (ReferenceableElement) ((ElementReference) me).getTarget();
			
			if (target == null) {
				return null;
			}
			if (target.eIsProxy()) {
				type = null;
			}
			
			// Navigate through AdditionalLocalVariable or Field
			if (target instanceof AdditionalLocalVariable) {
				target = (ReferenceableElement) target.eContainer();
			}
			if (target instanceof AdditionalField) {
				target = (ReferenceableElement) target.eContainer();
			}
			if (target instanceof TypedElement) {
				type = ((TypedElement) target).getTypeReference();
				if (type != null && !(type instanceof InferableType)) {
					TypeReference clonedType = TypeReferenceExtension.clone(type);
					clonedType = resolveAllTypeParameters(clonedType, (ElementReference) me, type);
					if (clonedType != null) {
						me.getActualTargets().add(clonedType);
						type = clonedType;
					}
				}
			} else if (target instanceof Type /*e.g. Annotation*/) {
				return TypeReferenceExtension.convertToTypeReference((Type) target);
			} else if (target instanceof EnumConstant) {
				type = TypeReferenceExtension.convertToTypeReference((Enumeration) target.eContainer());
			}
		}
		// Strings may also appear as reference.
		else if (me instanceof StringReference || me instanceof TextBlockReference) {
			return TypeReferenceExtension.convertToTypeReference(me.getStringClass());
		} else if (me instanceof NestedExpression) {
			type = ((NestedExpression) me).getExpression().getOneTypeReference(false);
		} else if (me instanceof PrimitiveTypeReference) {
			type = ((PrimitiveTypeReference) me).getPrimitiveType();
		} else if (me instanceof ExplicitConstructorCall) {
			ExplicitConstructorCall exCall = (ExplicitConstructorCall) me;
			ConcreteClassifier thisClass = me.getContainingConcreteClassifier();
			if (exCall.getCallTarget() instanceof This) {
				return TypeReferenceExtension.convertToTypeReference(thisClass);
			} else {
				if (thisClass instanceof org.emftext.language.java.classifiers.Class) {
					return TypeReferenceExtension.convertToTypeReference(
							((org.emftext.language.java.classifiers.Class)
									thisClass).getSuperClass());
				}
			}
		} else {
			assert false;
		}
		return type;
	}
	
	private static TypeReference resolveAllTypeParameters(TypeReference type, ElementReference me, TypeReference original) {
		Type t = original.getTarget();
		if (t instanceof TypeParameter) {
			TypeReference boundRef = ((TypeParameter) t).getBoundTypeReference(original, me);
			if (boundRef == null || boundRef.getTarget() == null || boundRef.getTarget().equals(t)) {
				return null;
			}
			return TypeReferenceExtension.clone(boundRef);
		} else {
			ClassifierReference classRef = type.getPureClassifierReference();
			ClassifierReference originalClassRef = original.getPureClassifierReference();
			if (classRef instanceof TypeArgumentable) {
				TypeArgumentable typeArg = (TypeArgumentable) classRef;
				TypeArgumentable originalTypeArg = (TypeArgumentable) originalClassRef;
				for (int index = 0; index < typeArg.getTypeArguments().size(); index++) {
					TypeArgument typeArgument = typeArg.getTypeArguments().get(index);
					TypeArgument originalTypeArgument = originalTypeArg.getTypeArguments().get(index);
					TypeReference argRef = TypeReferenceExtension
							.getTypeReferenceOfTypeArgument(typeArgument);
					TypeReference originalArgRef = TypeReferenceExtension
							.getTypeReferenceOfTypeArgument(originalTypeArgument);
					if (argRef != null) {
						TypeReference resolved = resolveAllTypeParameters(argRef, me, originalArgRef);
						if (resolved != null) {
							resolved = TypeReferenceExtension.clone(resolved);
							setTypeReferenceOfTypeArgument(typeArgument, resolved);
						} else {
							return null;
						}
					}
				}
			}
		}
		return type;
	}
	
	private static void setTypeReferenceOfTypeArgument(TypeArgument arg, TypeReference ref) {
		if (arg instanceof QualifiedTypeArgument) {
			((QualifiedTypeArgument) arg).setTypeReference(ref);
		} else if (arg instanceof SuperTypeArgument) {
			((SuperTypeArgument) arg).setSuperType(ref);
		} else if (arg instanceof ExtendsTypeArgument) {
			((ExtendsTypeArgument) arg).setExtendType(ref);
		}
	}
}
