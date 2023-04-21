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
package org.emftext.language.java.extensions.expressions;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.emftext.language.java.arrays.ArrayInstantiationBySize;
import org.emftext.language.java.arrays.ArrayTypeable;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.expressions.AdditiveExpression;
import org.emftext.language.java.expressions.AndExpression;
import org.emftext.language.java.expressions.AssignmentExpression;
import org.emftext.language.java.expressions.CastExpression;
import org.emftext.language.java.expressions.ConditionalAndExpression;
import org.emftext.language.java.expressions.ConditionalExpression;
import org.emftext.language.java.expressions.ConditionalOrExpression;
import org.emftext.language.java.expressions.EqualityExpression;
import org.emftext.language.java.expressions.ExclusiveOrExpression;
import org.emftext.language.java.expressions.Expression;
import org.emftext.language.java.expressions.InclusiveOrExpression;
import org.emftext.language.java.expressions.InstanceOfExpression;
import org.emftext.language.java.expressions.LambdaExpression;
import org.emftext.language.java.expressions.MethodReferenceExpression;
import org.emftext.language.java.expressions.MultiplicativeExpression;
import org.emftext.language.java.expressions.NestedExpression;
import org.emftext.language.java.expressions.PrimaryExpression;
import org.emftext.language.java.expressions.RelationExpression;
import org.emftext.language.java.expressions.ShiftExpression;
import org.emftext.language.java.expressions.UnaryExpression;
import org.emftext.language.java.extensions.types.TypeReferenceExtension;
import org.emftext.language.java.instantiations.Instantiation;
import org.emftext.language.java.literals.Literal;
import org.emftext.language.java.members.AdditionalField;
import org.emftext.language.java.members.Constructor;
import org.emftext.language.java.members.Field;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.parameters.VariableLengthParameter;
import org.emftext.language.java.references.ElementReference;
import org.emftext.language.java.references.MethodCall;
import org.emftext.language.java.references.Reference;
import org.emftext.language.java.references.ReferenceableElement;
import org.emftext.language.java.statements.Block;
import org.emftext.language.java.statements.Return;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;
import org.emftext.language.java.types.TypedElement;
import org.emftext.language.java.util.TemporalCompositeTypeReference;
import org.emftext.language.java.util.TemporalUnknownType;
import org.emftext.language.java.variables.AdditionalLocalVariable;
import org.emftext.language.java.variables.LocalVariable;

public class ExpressionExtension {

	/**
	 * Returns the type of the expression considering all concrete subtypes of the
	 * Expression.
	 * 
	 * @param me the expression.
	 * @return type of expression
	 */
	public static Type getType(Expression me) {
		return me.getOneType(false);
	}

	public static Type getAlternativeType(Expression me) {
		return me.getOneType(true);
	}

	public static Type getOneType(Expression me, boolean alternative) {
		TypeReference ref = getOneTypeReference(me, alternative);
		if (ref instanceof TemporalCompositeTypeReference) {
			return ((TemporalCompositeTypeReference) ref).asType();
		}
		return ref == null ? null : ref.getTarget();
	}

	public static TypeReference getOneTypeReference(Expression me, boolean alternative) {
		org.emftext.language.java.classifiers.Class stringClass = me.getStringClass();

		TypeReference type = null;

		if (me instanceof Reference) {
			Reference reference = (Reference) me;
			// navigate down references
			while (reference.getNext() != null) {
				reference = reference.getNext();
			}
			type = reference.getReferencedTypeReference();
		} else if (me instanceof Literal) {
			type = TypeReferenceExtension.convertToTypeReference(((Literal) me).getType());
		} else if (me instanceof CastExpression) {
			return getTypeReference((CastExpression) me);
		} else if (me instanceof AssignmentExpression) {
			type = ((AssignmentExpression) me).getChild().getOneTypeReference(alternative);
		} else if (me instanceof ConditionalExpression && ((ConditionalExpression) me).getExpressionIf() != null) {
			if (alternative) {
				type = ((ConditionalExpression) me).getExpressionElse().getOneTypeReference(alternative);
			} else {
				type = ((ConditionalExpression) me).getExpressionIf().getOneTypeReference(alternative);
			}
		} else if (me instanceof EqualityExpression || me instanceof RelationExpression
				|| me instanceof ConditionalOrExpression || me instanceof ConditionalAndExpression
				|| me instanceof InstanceOfExpression) {
			type = TypeReferenceExtension.convertToTypeReference(me.getLibClass("Boolean"));
		} else if (me instanceof AdditiveExpression || me instanceof MultiplicativeExpression
				|| me instanceof InclusiveOrExpression || me instanceof ExclusiveOrExpression
				|| me instanceof AndExpression || me instanceof ShiftExpression) {

			if (me instanceof AdditiveExpression) {
				AdditiveExpression additiveExpression = (AdditiveExpression) me;
				for (Expression subExp : additiveExpression.getChildren()) {
					if (stringClass.equals(subExp.getOneType(alternative))) {
						// special case: string concatenation
						return TypeReferenceExtension.convertToTypeReference(stringClass);
					}
				}
			}

			@SuppressWarnings("unchecked")
			Expression subExp = ((EList<Expression>) me.eGet(me.eClass().getEStructuralFeature("children"))).get(0);

			return subExp.getOneTypeReference(alternative);
		} else if (me instanceof UnaryExpression) {
			Expression subExp = ((UnaryExpression) me).getChild();

			return subExp.getOneTypeReference(alternative);
		} else if (me instanceof LambdaExpression) {
			LambdaExpression lambdExpr = (LambdaExpression) me;
			EObject container = lambdExpr;
			while (!(container.eContainer() instanceof MethodCall
					|| container.eContainer() instanceof AssignmentExpression
					|| container.eContainer() instanceof LocalVariable
					|| container.eContainer() instanceof AdditionalLocalVariable
					|| container.eContainer() instanceof Return
					|| container.eContainer() instanceof CastExpression
					|| container.eContainer() instanceof Field
					|| container.eContainer() instanceof AdditionalField
					|| container.eContainer() instanceof Instantiation)) {
				container = container.eContainer();
			}
			if (container.eContainer() instanceof MethodCall) {
				MethodCall call = (MethodCall) container.eContainer();
				Method m = (Method) call.getTarget();
				if (m.eIsProxy()) {
					return TypeReferenceExtension.convertToTypeReference(new TemporalUnknownType(lambdExpr));
				}
				return m.getParameters().get(call.getArguments().indexOf(container)).getTypeReference();
			} else if (container.eContainer() instanceof AssignmentExpression) {
				AssignmentExpression assExpr = (AssignmentExpression) container.eContainer();
				return assExpr.getChild().getOneTypeReference(alternative);
			} else if (container.eContainer() instanceof LocalVariable) {
				return ((LocalVariable) container.eContainer()).getTypeReference();
			} else if (container.eContainer() instanceof AdditionalLocalVariable) {
				return ((AdditionalLocalVariable) container.eContainer()).getTypeReference();
			} else if (container.eContainer() instanceof Field) {
				return ((Field) container.eContainer()).getTypeReference();
			} else if (container.eContainer() instanceof AdditionalField) {
				return ((AdditionalField) container.eContainer().eContainer()).getTypeReference();
			} else if (container.eContainer() instanceof Return) {
				while (!(container instanceof Method)) {
					container = container.eContainer();
				}
				return ((Method) container).getTypeReference();
			} else if (container.eContainer() instanceof CastExpression) {
				return ((CastExpression) container.eContainer()).getOneTypeReference(alternative);
			} else if (container.eContainer() instanceof Instantiation) {
				Instantiation inst = (Instantiation) container.eContainer();
				ConcreteClassifier t = (ConcreteClassifier) inst.getReferencedType();
				for (Member mem : t.getMembers()) {
					if (mem instanceof Constructor) {
						Constructor aCon = (Constructor) mem;
						if (aCon.getParameters().size() == inst.getArguments().size()) {
							TypeReference ref = aCon.getParameters().get(
									inst.getArguments().indexOf(container))
									.getTypeReference();
							if (ref.getTarget()
									instanceof org.emftext.language.java.classifiers
									.Interface) {
								return ref;
							}
						}
					}
				}
			}
		} else if (me instanceof MethodReferenceExpression) {
			return ((MethodReferenceExpression) me).getTargetTypeReference();
		} else {
			for (TreeIterator<EObject> i = me.eAllContents(); i.hasNext();) {
				EObject next = i.next();
				TypeReference nextType = null;

				if (next instanceof PrimaryExpression) {

					if (next instanceof Reference) {
						Reference ref = (Reference) next;
						// Navigate down references.
						while (ref.getNext() != null) {
							ref = ref.getNext();
						}
						next = ref;
					}
					if (next instanceof Literal) {
						nextType = TypeReferenceExtension.convertToTypeReference(
								((Literal) next).getType());
					} else if (next instanceof CastExpression) {
						nextType = getTypeReference((CastExpression) next);
					} else {
						nextType = ((Reference) next).getReferencedTypeReference();
					}
					i.prune();

				}
				if (nextType != null) {
					type = nextType;
					// In the special case that this is an expression with
					// some String included, everything is converted to String.
					if (stringClass.equals(type.getTarget())) {
						break;
					}
				}
			}
		}
		// type can be null in cases of unresolved/unresolvable proxies.
		return type;
	}

	private static TypeReference getTypeReference(CastExpression castExpr) {
		if (castExpr.getAdditionalBounds().size() > 0) {
			TemporalCompositeTypeReference tempClass = new TemporalCompositeTypeReference();
			tempClass.getTypeReferences().add(castExpr.getTypeReference());
			for (TypeReference ref : castExpr.getAdditionalBounds()) {
				tempClass.getTypeReferences().add(ref);
			}
			return tempClass;
		} else {
			return castExpr.getTypeReference();
		}
	}

	public static long getArrayDimension(Expression me) {
		long size = 0;
		ArrayTypeable arrayType = null;
		if (me instanceof NestedExpression && ((NestedExpression) me).getNext() == null) {
			return ((NestedExpression) me).getExpression().getArrayDimension()
					- ((NestedExpression) me).getArraySelectors().size();
		}
		if (me instanceof ConditionalExpression && ((ConditionalExpression) me).getExpressionIf() != null) {
			return ((ConditionalExpression) me).getExpressionIf().getArrayDimension();
		}
		if (me instanceof AssignmentExpression) {
			Expression value = ((AssignmentExpression) me).getValue();
			if (value == null) {
				return 0;
			}
			return value.getArrayDimension();
		}
		if (me instanceof InstanceOfExpression || me instanceof MethodReferenceExpression) {
			return 0;
		}
		if (me instanceof Reference) {
			Reference reference = (Reference) me;
			while (reference.getNext() != null) {
				reference = reference.getNext();
			}
			// an array clone? -> dimension defined by cloned array
			if (reference instanceof ElementReference && reference.getPrevious() != null) {
				ReferenceableElement target = ((ElementReference) reference).getTarget();
				if (target instanceof Method) {
					if ("clone".equals(((Method) target).getName())) {
						reference = (Reference) reference.eContainer();
					}
				}
			}

			if (reference instanceof ElementReference) {
				ElementReference elementReference = (ElementReference) reference;
				if (elementReference.getTarget() instanceof TypedElement) {
					arrayType = ((TypedElement) elementReference.getTarget()).getTypeReference();
				}
			} else if (me instanceof ArrayTypeable) {
				size += ((ArrayTypeable) me).getArrayDimensionsBefore().size()
						+ ((ArrayTypeable) me).getArrayDimensionsAfter().size();
				if (me instanceof VariableLengthParameter) {
					size++;
				}
			}
			size -= reference.getArraySelectors().size();
		} else if (me instanceof ArrayTypeable) {
			size += ((ArrayTypeable) me).getArrayDimensionsBefore().size()
					+ ((ArrayTypeable) me).getArrayDimensionsAfter().size();
			if (me instanceof VariableLengthParameter) {
				size++;
			}
		}

		if (me instanceof LambdaExpression) {
			LambdaExpression expr = (LambdaExpression) me;
			if (expr.getBody() instanceof LambdaExpression) {
				return 0;
			} else if (expr.getBody() instanceof Expression) {
				return getArrayDimension((Expression) expr.getBody());
			} else {
				Block b = (Block) expr.getBody();
				EList<Return> returns = b.getChildrenByType(Return.class);
				if (returns.size() == 0 || returns.get(0).getReturnValue() == null) {
					return 0;
				}
				return returns.get(0).getReturnValue().getArrayDimension();
			}
		}

		if (me instanceof ArrayInstantiationBySize) {
			size += ((ArrayInstantiationBySize) me).getSizes().size();
		}

		if (arrayType != null) {
			size += arrayType.getArrayDimension();
		}

		return size;
	}
}
