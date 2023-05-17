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

package org.emftext.language.java.extensions.types;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emftext.language.java.arrays.ArrayTypeable;
import org.emftext.language.java.classifiers.Classifier;
import org.emftext.language.java.generics.ExtendsTypeArgument;
import org.emftext.language.java.generics.QualifiedTypeArgument;
import org.emftext.language.java.generics.SuperTypeArgument;
import org.emftext.language.java.generics.TypeArgument;
import org.emftext.language.java.generics.TypeParameter;
import org.emftext.language.java.references.ElementReference;
import org.emftext.language.java.references.MethodCall;
import org.emftext.language.java.references.Reference;
import org.emftext.language.java.references.ReferenceableElement;
import org.emftext.language.java.types.ClassifierReference;
import org.emftext.language.java.types.InferableType;
import org.emftext.language.java.types.NamespaceClassifierReference;
import org.emftext.language.java.types.PrimitiveType;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;
import org.emftext.language.java.types.TypesFactory;
import org.emftext.language.java.util.TemporalCompositeTypeReference;

public class TypeReferenceExtension {

	/**
	 * Returns the type referenced by this <code>TypeReference</code> considering
	 * all concrete subclasses of <code>TypeReference</code> used by the Java
	 * meta-model.
	 * 
	 * @param me the type reference to obtain the type from.
	 * @return the referenced type
	 */
	public static Type getTarget(TypeReference me) {
		return me.getBoundTarget(null);
	}

	/**
	 * Sets the type targeted by this type reference.
	 * 
	 * @param me   the type reference whose type is set.
	 * @param type the new type to set as target.
	 */
	public static void setTarget(TypeReference me, Classifier type) {
		if (type == null) {
			return;
		}

		if (type.eIsProxy()) {
			return;
		}

		if (me instanceof NamespaceClassifierReference) {
			NamespaceClassifierReference nsClassifierReference = (NamespaceClassifierReference) me;
			nsClassifierReference.getClassifierReferences().clear();
			nsClassifierReference.getNamespaces().clear();
			nsClassifierReference.getNamespaces().addAll(type.getContainingContainerName());
			ClassifierReference classifierRef = TypesFactory.eINSTANCE.createClassifierReference();
			classifierRef.setTarget(type);
			nsClassifierReference.getClassifierReferences().add(classifierRef);
		} else if (me instanceof ClassifierReference) {
			ClassifierReference ref = (ClassifierReference) me;
			ref.setTarget(type);
		} else if (me instanceof InferableType) {
			InferableType ref = (InferableType) me;
			ref.getArrayDimensionsBefore().clear();
			ref.getActualTargets().clear();
			ClassifierReference newRef = TypesFactory.eINSTANCE.createClassifierReference();
			newRef.setTarget(type);
			ref.getActualTargets().add(newRef);
		}
	}

	/**
	 * Returns the type referenced by this <code>TypeReference</code> considering
	 * all concrete subclasses of <code>TypeReference</code> used by the Java
	 * meta-model. If type parameters are bound in the given reference, the bound
	 * type will be returned instead of the parameter.
	 * 
	 * @param me        the type reference to obtain the type for.
	 * @param reference the context of the type reference.
	 * @return the referenced type.
	 */
	public static Type getBoundTarget(TypeReference me, Reference reference) {
		TypeReference ref = me.getBoundTargetReference(reference);
		if (ref != null) {
			if (ref instanceof TemporalCompositeTypeReference) {
				return ((TemporalCompositeTypeReference) ref).asType();
			} else if (ref instanceof PrimitiveType) {
				return (PrimitiveType) ref;
			}
			return ref.getTarget();
		}
		return null;
	}

	public static TypeReference getBoundTargetReference(TypeReference me, Reference reference) {
		TypeReference type = null;
		if (me instanceof ClassifierReference || me instanceof NamespaceClassifierReference) {
			ClassifierReference classifierRef = me.getPureClassifierReference();
			if (classifierRef != null) {
				type = classifierRef;
			}

			if (reference instanceof MethodCall) {
				MethodCall potentialCloneCall = (MethodCall) reference;
				// clone returns the type of the cloned type in the case of arrays.
				ReferenceableElement potentialCloneCallTarget = potentialCloneCall.getTarget();
				if (potentialCloneCallTarget != null && !potentialCloneCallTarget.eIsProxy()
						&& "clone".equals(potentialCloneCallTarget.getName())) {
					if (potentialCloneCall.getPrevious() instanceof ElementReference) {
						ElementReference prevRef = (ElementReference) potentialCloneCall.getPrevious();
						if (prevRef.getTarget() instanceof ArrayTypeable
								&& ((ArrayTypeable) prevRef.getTarget())
								.getArrayDimension() > 0) {
							type = prevRef.getReferencedTypeReference();
						}
					}
				}
			}
		} else if (me instanceof PrimitiveType) {
			return (PrimitiveType) me;
		}

		// Resolve parameter to real type.
		if (type != null && type.getTarget() instanceof TypeParameter) {
			type = ((TypeParameter) type.getTarget()).getBoundTypeReference(me, reference);
		}

		if (type != null && type.getTarget() != null && type.getTarget().eIsProxy()) {
			// This may happen when, e.g., a super type is resolved. It is ok.
			return null;
		}

		return type;
	}

	public static TypeReference getTypeReferenceOfTypeArgument(TypeReference ref, int index) {
		ClassifierReference actualRef = ref.getPureClassifierReference();
		if (actualRef == null) {
			return null;
		}
		TypeArgument arg = actualRef.getTypeArguments().get(index);
		return getTypeReferenceOfTypeArgument(arg);
	}

	/**
	 * Returns the type reference for a type argument. If the type argument is a
	 * QualifiedTypeArgument, the returned type reference is the qualified type. If
	 * the type argument is a SuperTypeArgument, the lower bound is returned. If the
	 * type argument is an ExtendsTypeArgument, the upper bound is returned. If the
	 * type argument is an UnknownTypeArgument, null is returned.
	 * 
	 * @param arg the type argument.
	 * @return the type reference for the type argument or null.
	 */
	public static TypeReference getTypeReferenceOfTypeArgument(TypeArgument arg) {
		if (arg instanceof QualifiedTypeArgument) {
			QualifiedTypeArgument qual = (QualifiedTypeArgument) arg;
			return qual.getTypeReference();
		} else if (arg instanceof ExtendsTypeArgument) {
			ExtendsTypeArgument qual = (ExtendsTypeArgument) arg;
			return qual.getExtendType();
		} else if (arg instanceof SuperTypeArgument) {
			SuperTypeArgument qual = (SuperTypeArgument) arg;
			return qual.getSuperType();
		}
		return null;
	}

	public static TypeReference convertToTypeReference(EObject obj) {
		if (obj instanceof TypeReference) {
			return (TypeReference) obj;
		} else if (obj instanceof Classifier) {
			ClassifierReference ref = TypesFactory.eINSTANCE.createClassifierReference();
			ref.setTarget((Classifier) obj);
			return ref;
		}
		return null;
	}

	public static TypeReference clone(TypeReference me) {
		if (me.eContainer() == null) {
			return me;
		}
		return EcoreUtil.copy(me);
	}

	/**
	 * Extracts the (possibly nested) ClassifierReference (if any) from a
	 * TypeReference.
	 * 
	 * @param me the TypeReference to extract the ClassifierReference from.
	 * @return the extracted ClassifierReference.
	 */
	public static ClassifierReference getPureClassifierReference(TypeReference me) {
		ClassifierReference classifierReference = null;
		if (me instanceof ClassifierReference) {
			classifierReference = (ClassifierReference) me;
		}

		if (me instanceof NamespaceClassifierReference) {
			NamespaceClassifierReference nsClassifierReference = (NamespaceClassifierReference) me;
			if (!nsClassifierReference.getClassifierReferences().isEmpty()) {
				int lastIndex = nsClassifierReference.getClassifierReferences().size() - 1;
				classifierReference = nsClassifierReference.getClassifierReferences().get(lastIndex);
			}
		}

		if (me instanceof InferableType) {
			InferableType t = (InferableType) me;
			if (t.getActualTargets().size() > 0) {
				return t.getActualTargets().get(0).getPureClassifierReference();
			}
		}

		return classifierReference;
	}
}
