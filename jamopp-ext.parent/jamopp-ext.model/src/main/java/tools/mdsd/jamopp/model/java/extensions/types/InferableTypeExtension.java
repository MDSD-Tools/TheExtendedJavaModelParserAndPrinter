/*******************************************************************************
 * Copyright (c) 2021, Martin Armbruster
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

package tools.mdsd.jamopp.model.java.extensions.types;

import org.eclipse.emf.ecore.EObject;

import tools.mdsd.jamopp.model.java.classifiers.Interface;
import tools.mdsd.jamopp.model.java.expressions.Expression;
import tools.mdsd.jamopp.model.java.expressions.LambdaExpression;
import tools.mdsd.jamopp.model.java.expressions.LambdaParameters;
import tools.mdsd.jamopp.model.java.generics.TypeParameter;
import tools.mdsd.jamopp.model.java.generics.TypeParametrizable;
import tools.mdsd.jamopp.model.java.instantiations.Instantiation;
import tools.mdsd.jamopp.model.java.members.Method;
import tools.mdsd.jamopp.model.java.references.MethodCall;
import tools.mdsd.jamopp.model.java.references.Reference;
import tools.mdsd.jamopp.model.java.types.InferableType;
import tools.mdsd.jamopp.model.java.types.Type;
import tools.mdsd.jamopp.model.java.types.TypeReference;
import tools.mdsd.jamopp.model.java.util.TemporalCompositeTypeReference;
import tools.mdsd.jamopp.model.java.variables.LocalVariable;

public class InferableTypeExtension {
	public static TypeReference getBoundTargetReference(InferableType me, Reference reference) {
		if (me.getActualTargets().size() == 0) {
			TypeReference initType = null;
			if (me.eContainer() instanceof LocalVariable) {
				LocalVariable loc = (LocalVariable) me.eContainer();
				TypeReference ref = loc.getInitialValue().getOneTypeReference(false);
				if (ref == null) {
					return null;
				}
				if (ref instanceof TemporalCompositeTypeReference) {
					TemporalCompositeTypeReference tempRef = (TemporalCompositeTypeReference) ref;
					for (TypeReference inRef : tempRef.getTypeReferences()) {
						me.getActualTargets().add(TypeReferenceExtension.clone(inRef));
					}
					return tempRef;
				}
				me.getActualTargets().add(TypeReferenceExtension.clone(ref));
				return ref;
			} else if (me.eContainer() != null && me.eContainer().eContainer() != null
					&& me.eContainer().eContainer() instanceof LambdaParameters) {
				LambdaExpression lambExpr = (LambdaExpression) me.eContainer().eContainer().eContainer();
				TypeReference lambdaType = lambExpr.getOneTypeReference(false);
				Type lambdaTypeTarget = lambdaType.getTarget();
				if (!(lambdaTypeTarget instanceof Interface)) {
					return initType;
				}
				Method m = ((Interface) lambdaTypeTarget).getAbstractMethodOfFunctionalInterface();
				EObject container = lambExpr.eContainer();
				while (container instanceof Expression && (
						!(container instanceof MethodCall) && !(container instanceof Instantiation))) {
					container = container.eContainer();
				}
				if (container instanceof MethodCall || container instanceof Instantiation) {
					initType = m.getParameters().get(
							lambExpr.getParameters().getParameters().indexOf(me.eContainer()))
								.getTypeReference().getBoundTargetReference(
										(Reference) container);
				} else {
					initType = m.getParameters().get(
						lambExpr.getParameters().getParameters().indexOf(me.eContainer()))
							.getTypeReference().getBoundTargetReference(null);
				}
				initType = TypeReferenceExtension.clone(initType);
				Type initTypeTarget = initType.getTarget();
				if (initTypeTarget instanceof TypeParameter) {
					if (initTypeTarget.eContainer().equals(lambdaTypeTarget)) {
						int index = ((TypeParametrizable) lambdaTypeTarget)
								.getTypeParameters().indexOf(initTypeTarget);
						initType = TypeReferenceExtension.getTypeReferenceOfTypeArgument(
								lambdaType, index);
						if (initType != null) {
							initTypeTarget = initType.getTarget();
							if (initTypeTarget instanceof TypeParameter) {
								initType = TypeReferenceExtension.clone(
										initType.getBoundTargetReference(
												container instanceof Reference
													? (Reference) container
															: null));
							}
						}
					}
				}
			}
			if (initType != null) {
				if (initType instanceof TemporalCompositeTypeReference) {
					for (TypeReference obj : ((TemporalCompositeTypeReference) initType)
							.getTypeReferences()) {
						me.getActualTargets().add(TypeReferenceExtension.clone(obj));
					}
				} else {
					me.getActualTargets().add(TypeReferenceExtension.clone(initType));
				}
				return initType;
			}
		} else if (me.getActualTargets().size() == 1) {
			return me.getActualTargets().get(0).getBoundTargetReference(reference);
		} else {
			TemporalCompositeTypeReference result = new TemporalCompositeTypeReference();
			for (TypeReference ref : me.getActualTargets()) {
				result.getTypeReferences().add(ref.getBoundTargetReference(reference));
			}
			return result;
		}
		return null;
	}
}
