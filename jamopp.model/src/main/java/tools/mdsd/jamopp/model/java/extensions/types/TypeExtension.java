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
 ******************************************************************************/
package tools.mdsd.jamopp.model.java.extensions.types;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import tools.mdsd.jamopp.model.java.JavaClasspath;
import tools.mdsd.jamopp.model.java.arrays.ArrayTypeable;
import tools.mdsd.jamopp.model.java.classifiers.Annotation;
import tools.mdsd.jamopp.model.java.classifiers.AnonymousClass;
import tools.mdsd.jamopp.model.java.classifiers.Classifier;
import tools.mdsd.jamopp.model.java.classifiers.ConcreteClassifier;
import tools.mdsd.jamopp.model.java.classifiers.Interface;
import tools.mdsd.jamopp.model.java.generics.TypeParameter;
import tools.mdsd.jamopp.model.java.members.Member;
import tools.mdsd.jamopp.model.java.parameters.VariableLengthParameter;
import tools.mdsd.jamopp.model.java.types.PrimitiveType;
import tools.mdsd.jamopp.model.java.types.Type;
import tools.mdsd.jamopp.model.java.types.TypeReference;
import tools.mdsd.jamopp.model.java.types.TypedElement;
import tools.mdsd.jamopp.model.java.util.TemporalCompositeClassifier;

public class TypeExtension {

	/**
	 * @param me this type.
	 * @param arrayDimension dimension of this type.
	 * @param otherType the other type.
	 * @param otherArrayDimension dimension of the other type.
	 * @return if both types are equal.
	 */
	public static boolean equalsType(Type me, long arrayDimension, Type otherType, long otherArrayDimension) {
		Type lOtherType = otherType;
		Type _this = me;
		// comparison for type parameters
		if (_this instanceof TypeParameter) {
			TypeParameter typeParameter = (TypeParameter) _this;
			for (TypeReference referencedType : typeParameter.getExtendTypes()) {
				if (referencedType.getTarget() != null && !referencedType.getTarget().eIsProxy()
						&& referencedType.getTarget().equalsType(
								arrayDimension, lOtherType, otherArrayDimension)) {
					return true;
				}
			}
			if (typeParameter.getExtendTypes().isEmpty()) {
				if (me.getObjectClass().equalsType(arrayDimension, lOtherType, otherArrayDimension)) {
					return true;
				}
			}
		}
		if (lOtherType instanceof TypeParameter) {
			TypeParameter typeParameter = (TypeParameter) lOtherType;
			for (TypeReference referencedType : typeParameter.getExtendTypes()) {
				if (referencedType.getTarget() != null && !referencedType.getTarget().eIsProxy()
						&& me.equalsType(arrayDimension,
								referencedType.getTarget(), otherArrayDimension)) {
					return true;
				}
			}
			if (typeParameter.getExtendTypes().isEmpty()) {
				if (me.equalsType(arrayDimension, me.getObjectClass(), otherArrayDimension)) {
					return true;
				}
			}
		}

		// do comparison on the classifier level
		if (_this instanceof PrimitiveType) {
			_this = ((PrimitiveType) _this).wrapPrimitiveType();
		}
		if (lOtherType instanceof PrimitiveType) {
			lOtherType = ((PrimitiveType) lOtherType).wrapPrimitiveType();
		}

		if (arrayDimension == otherArrayDimension && lOtherType instanceof Classifier && _this instanceof Classifier
				&& (lOtherType.equals(_this))) {
			return true;
		}

		return false;
	}

	/**
	 * @param me this type.
	 * @param arrayDimension dimension of this type.
	 * @param otherType the other type.
	 * @param otherArrayType dimension of the other type.
	 * @return if the other type is equal to me or a super type of me.
	 */
	public static boolean isSuperType(Type me, long arrayDimension, Type otherType, ArrayTypeable otherArrayType) {
		Type lOtherType = otherType;

		if (lOtherType == null) {
			return false;
		}

		Type _this = me;

		if (_this instanceof TemporalCompositeClassifier || lOtherType instanceof TemporalCompositeClassifier) {
			EList<Type> _thisTypeList = new UniqueEList<Type>();
			EList<Type> otherTypeList = new UniqueEList<Type>();
			if (_this instanceof TemporalCompositeClassifier) {
				for (EObject aType : ((TemporalCompositeClassifier) _this).getSuperTypes()) {
					_thisTypeList.add((Type) aType);
				}
			} else {
				_thisTypeList.add(_this);
			}
			if (lOtherType instanceof TemporalCompositeClassifier) {
				for (EObject aType : ((TemporalCompositeClassifier) lOtherType).getSuperTypes()) {
					otherTypeList.add((Type) aType);
				}
			} else {
				otherTypeList.add(_this);
			}

			for (Type one_thisType : _thisTypeList) {
				for (Type oneOtherType : otherTypeList) {
					boolean result = one_thisType.isSuperType(arrayDimension, oneOtherType, otherArrayType);
					if (result) {
						return true;
					}
				}
			}
			return false;
		}

		// if I am a void, I am of every type
		if (_this.equals(me.getLibClass("Void"))) {
			return true;
		}

		// if the other is Object I am a subtype in any case (also array dimensions do
		// not matter)
		if (lOtherType.equals(me.getObjectClass())) {
			return true;
		}

		// String, primitives, and arrays are serializable
		ConcreteClassifier serializableClass = (ConcreteClassifier) EcoreUtil
				.resolve(JavaClasspath.get(me).getConcreteClassifier("java.io.Serializable"), _this);
		if (lOtherType.equals(serializableClass)) {
			if (_this.equals(serializableClass)) {
				return true;
			} else if (_this.equals(me.getStringClass())) {
				return true;
			} else if (_this instanceof PrimitiveType) {
				return true;
			} else if (arrayDimension > 0) {
				// all arrays are serializable
				return true;
			}
		}

		// if one of us is a parameter to the best of my knowledge, we might match
		if (_this instanceof TypeParameter) {
			return true;
		}
		if (lOtherType instanceof TypeParameter) {
			return true;
		}

		// if array dimensions do not match, I am no subtype
		boolean isTypeParameter = false;
		if (otherArrayType instanceof TypedElement) {
			Type type = ((TypedElement) otherArrayType).getTypeReference().getTarget();
			isTypeParameter = type instanceof TypeParameter;
		}
		boolean isVariableLengthParameter = otherArrayType instanceof VariableLengthParameter
				|| (otherArrayType != null && otherArrayType.eContainer() != null
						&& otherArrayType.eContainer() instanceof VariableLengthParameter);

		long otherArrayDim = 0;
		if (otherArrayType != null) {
			otherArrayDim = otherArrayType.getArrayDimension();
		}

		if (isTypeParameter && isVariableLengthParameter) {
			if (arrayDimension != otherArrayDim && arrayDimension != otherArrayDim - 1
					&& arrayDimension < otherArrayDim) {

				return false;
			}
		} else if (isTypeParameter) {
			if (arrayDimension < otherArrayDim) {
				return false;
			}
		} else if (isVariableLengthParameter) {
			if (arrayDimension != otherArrayDim && arrayDimension != otherArrayDim - 1) {
				return false;
			}
		} else {
			if (arrayDimension != otherArrayDim) {
				return false;
			}
		}

		// annotations
		if (_this instanceof Annotation && (lOtherType.equals(me.getAnnotationInterface())
				|| ((ConcreteClassifier) _this).getAllSuperClassifiers().contains(me.getAnnotationInterface()))) {
			return true;
		}

		// do comparison on the classifier level
		if (_this instanceof PrimitiveType) {
			_this = ((PrimitiveType) _this).wrapPrimitiveType();
		}
		if (lOtherType instanceof PrimitiveType) {
			lOtherType = ((PrimitiveType) lOtherType).wrapPrimitiveType();
		}

		// compare in type hierarchy
		if (lOtherType instanceof ConcreteClassifier && _this instanceof ConcreteClassifier && (lOtherType.equals(_this)
				|| ((ConcreteClassifier) _this).getAllSuperClassifiers().contains(lOtherType))) {
			return true;
		}

		if (lOtherType instanceof ConcreteClassifier && _this instanceof AnonymousClass
				&& ((AnonymousClass) _this).getAllSuperClassifiers().contains(lOtherType)) {

			return true;
		}

		// everything can be implicitly casted to CharSequence, so I match when the
		// other type is a CharSequence
		Interface charSequenceClass = me.getLibInterface("CharSequence");

		if (lOtherType instanceof ConcreteClassifier) {
			if (lOtherType.equals(charSequenceClass)
					|| ((ConcreteClassifier) lOtherType)
					.getAllSuperClassifiers().contains(charSequenceClass)) {
				return true;
			}
		}

		// there are some specifics for primitive types not reflected in the type
		// hierarchy
		if (lOtherType instanceof tools.mdsd.jamopp.model.java.classifiers.Class) {
			PrimitiveType primitiveType = ((tools.mdsd.jamopp.model.java.classifiers.Class) lOtherType)
					.unWrapPrimitiveType();
			if (primitiveType == null) {
				return false;
			}
			lOtherType = primitiveType;
		}

		// FIXME This is duplicate
		if (_this instanceof tools.mdsd.jamopp.model.java.classifiers.Class) {
			PrimitiveType primitiveType = ((tools.mdsd.jamopp.model.java.classifiers.Class) _this).unWrapPrimitiveType();
			if (primitiveType == null) {
				return false;
			}
			_this = primitiveType;
		}

		if (_this instanceof tools.mdsd.jamopp.model.java.types.Boolean) {
			return lOtherType instanceof tools.mdsd.jamopp.model.java.types.Boolean;
		}
		if (_this instanceof tools.mdsd.jamopp.model.java.types.Byte
				|| _this instanceof tools.mdsd.jamopp.model.java.types.Int
				|| _this instanceof tools.mdsd.jamopp.model.java.types.Short
				|| _this instanceof tools.mdsd.jamopp.model.java.types.Long
				|| _this instanceof tools.mdsd.jamopp.model.java.types.Char) {
			return lOtherType instanceof tools.mdsd.jamopp.model.java.types.Byte
					|| lOtherType instanceof tools.mdsd.jamopp.model.java.types.Int
					|| lOtherType instanceof tools.mdsd.jamopp.model.java.types.Short
					|| lOtherType instanceof tools.mdsd.jamopp.model.java.types.Long
					|| lOtherType instanceof tools.mdsd.jamopp.model.java.types.Char
					|| lOtherType instanceof tools.mdsd.jamopp.model.java.types.Float
					|| lOtherType instanceof tools.mdsd.jamopp.model.java.types.Double;
		}
		if (_this instanceof tools.mdsd.jamopp.model.java.types.Float
				|| _this instanceof tools.mdsd.jamopp.model.java.types.Double) {
			return lOtherType instanceof tools.mdsd.jamopp.model.java.types.Float
					|| lOtherType instanceof tools.mdsd.jamopp.model.java.types.Double;
		}

		return false;
	}

	public static EList<Member> getAllMembers(Type me) {
		// method has to be specified in subclasses
		throw new UnsupportedOperationException();
	}
}
