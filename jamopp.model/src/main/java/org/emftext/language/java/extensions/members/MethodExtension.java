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
 *      - Extension of getStatements()
 *      - Adaptation and extension for Java 7+
 ******************************************************************************/
package org.emftext.language.java.extensions.members;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.emftext.language.java.classifiers.ConcreteClassifier;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.expressions.Expression;
import org.emftext.language.java.expressions.LambdaExpression;
import org.emftext.language.java.extensions.types.TypeReferenceExtension;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.parameters.OrdinaryParameter;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.parameters.ParametersFactory;
import org.emftext.language.java.parameters.ReceiverParameter;
import org.emftext.language.java.parameters.VariableLengthParameter;
import org.emftext.language.java.references.MethodCall;
import org.emftext.language.java.statements.Block;
import org.emftext.language.java.statements.Statement;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;
import org.emftext.language.java.util.TemporalUnknownType;

public class MethodExtension {
	
	/**
	 * Returns <code>true</code> if the given method matches the given call.
	 * 
	 * @param me the given method.
	 * @param methodCall the method call.
	 * @return true if the method matches. false otherwise.
	 */
	public static boolean isSomeMethodForCall(Method me, MethodCall methodCall) {
		return me.isMethodForCall(methodCall, false);
	}

	/**
	 * Returns <code>true</code> if the given {@link Method} <code>me</code> is
	 * a better match for the given method call than {@link Method}
	 * <code>otherMethod</code>.
	 * 
	 * @param me the given method.
	 * @param otherMethod the other method.
	 * @param methodCall the method call.
	 * @return true or false.
	 */
	public static boolean isBetterMethodForCall(Method me, Method otherMethod,
			MethodCall methodCall) {
		
		if (!me.isMethodForCall(methodCall, false)) {
			return false;
		}
		
		if (otherMethod.isMethodForCall(methodCall, true)) {
			if (me.isMethodForCall(methodCall, true)) {
				// We both match perfectly; lets compare our return types
				Type target = me.getTypeReference().getTarget();
				if (target instanceof ConcreteClassifier) {
					if (((ConcreteClassifier) target).getAllSuperClassifiers().contains(
							otherMethod.getTypeReference().getTarget())) {
						// I am the more concrete type
						return true;
					}
				}
			}
			
			//the other already matches perfectly; I am not better
			return false;
		}
		
		if (!otherMethod.isMethodForCall(methodCall, false)) {
			//I match, but the other does not
			return true;
		}
		//we both match, I am only better if I match perfectly <- 
		//TODO #763: this is not enough: we need to check for "nearest subtype" here
		return me.isMethodForCall(methodCall, true);
	}

	public static boolean isMethodForCall(Method me, MethodCall methodCall,
			boolean needsPerfectMatch) {
		
		EList<Type> argumentTypeList = methodCall.getArgumentTypes();
		EList<Parameter> parameterList = new BasicEList<Parameter>(me.getParameters());
		
		if (parameterList.size() > 0 && parameterList.get(0) instanceof ReceiverParameter) {
			parameterList.remove(0);
		}
		
		EList<Type> parameterTypeList = new BasicEList<Type>();
		for (Parameter parameter : parameterList)  {
			// Determine types before messing with the parameters
			TypeReference typeReference = parameter.getTypeReference();
			Type boundTarget = typeReference.getBoundTarget(methodCall);
			parameterTypeList.add(boundTarget);
		}

		if (!parameterList.isEmpty()) {
			Parameter lastParameter = parameterList.get(parameterList.size() - 1);
			Type lastParameterType  = parameterTypeList.get(parameterTypeList.size() - 1);
			if (lastParameter instanceof VariableLengthParameter) {
				// In case of variable length add/remove some parameters
				while (parameterList.size() < argumentTypeList.size()) {
					if (needsPerfectMatch) {
						return false;
					}
					parameterList.add(lastParameter);
					parameterTypeList.add(lastParameterType);
				}
				
				if (parameterList.size() > argumentTypeList.size()) {
					if (needsPerfectMatch) {
						return false;
					}
					parameterList.remove(lastParameter);
					parameterTypeList.remove(parameterTypeList.size() - 1);
				}
			}
		}
		
		if (parameterList.size() == argumentTypeList.size()) { 
			boolean parametersMatch = true;
			for (int i = 0; i < argumentTypeList.size() && parametersMatch; i++) {
				Parameter parameter = parameterList.get(i);
				Expression argument = methodCall.getArguments().get(i);

				Type parameterType = parameterTypeList.get(i);
				Type argumentType = argumentTypeList.get(i);
				
				if (argumentType == null || parameterType == null) {
					return false;
				}
				
				if (!parameterType.eIsProxy() || !argumentType.eIsProxy()) {
					if (argumentType instanceof TemporalUnknownType) {
						LambdaExpression lambda = argument instanceof LambdaExpression
								? (LambdaExpression) argument
								: argument.getFirstChildByType(LambdaExpression.class);
						if (lambda != null) {
							if (!(parameterType instanceof Interface)) {
								return false;
							}
							Method absMeth = ((Interface) parameterType)
									.getAbstractMethodOfFunctionalInterface();
							if (absMeth.getParameters().size()
									!= lambda.getParameters().getParameters().size()) {
								return false;
							}
						}
						continue;
					}
					long argumentArrayDimension = argument.getArrayDimension();
					if (needsPerfectMatch) {
						long parameterArrayDimension = parameter.getTypeReference().getArrayDimension();
						parametersMatch = parametersMatch
							&& argumentType.equalsType(argumentArrayDimension,
									parameterType, parameterArrayDimension);
					} else {
						parametersMatch = parametersMatch 
							&& argumentType.isSuperType(argumentArrayDimension,
									parameterType, parameter.getTypeReference());
					}
				} else {
					return false;
				}
			}
			return parametersMatch;
		}
		
		return false;
	}
	
	/**
	 * Checks if the signature, i. e., the parameter types and return type, of two methods match,
	 * independently of the method and parameter names. It is possible for one method to have specialized parameter
	 * and return types compared to the other method.
	 * 
	 * @param one the first method.
	 * @param two the second method which can have specialized types.
	 * @return true if the signatures match. false otherwise.
	 */
	public static boolean isSignatureMatching(Method one, Method two) {
		return isSignatureMatching(one, two, null);
	}
	
	/**
	 * Checks if the signature, i. e., the parameter types and return type, of two methods match,
	 * independently of the method and parameter names. It is possible for one method to have specialized parameter
	 * and return types compared to the other method.
	 * If the comparison is for finding the referenced method within a MethodReferenceExpression, e. g.,
	 * StringBuilder::append, where the type of the primary expression (in the example StringBuilder) can be
	 * part of the parameter types of the second method, the type can be given.
	 * 
	 * @param one the first method.
	 * @param two the second method which can have specialized types.
	 * @param twoFirstParameter the type in case of MethodReferenceExpressions
	 *                          which is counted as the first parameter type of the second method.
	 * @return true if the signatures match. false otherwise.
	 */
	public static boolean isSignatureMatching(Method one, Method two, Type twoFirstParameter) {
		EList<Parameter> parameterListOne = new BasicEList<>(one.getParameters());
		EList<Parameter> parameterListTwo = new BasicEList<>(two.getParameters());
		
		if (parameterListOne.size() > 0 && parameterListOne.get(0) instanceof ReceiverParameter) {
			parameterListOne.remove(0);
		}
		if (parameterListTwo.size() > 0 && parameterListTwo.get(0) instanceof ReceiverParameter) {
			parameterListTwo.remove(0);
		}
		
		if (twoFirstParameter != null) {
			OrdinaryParameter p = ParametersFactory.eINSTANCE.createOrdinaryParameter();
			p.setTypeReference(TypeReferenceExtension.convertToTypeReference(twoFirstParameter));
			parameterListTwo.add(0, p);
		}

		if (parameterListOne.size() == parameterListTwo.size()) { 
			boolean parametersMatch = true;
			for (int i = 0; i < parameterListOne.size() && parametersMatch; i++) {
				Parameter paramOne = parameterListOne.get(i);
				Parameter paramTwo = parameterListTwo.get(i);
				
				Type parameterType = paramOne.getTypeReference().getTarget();
				Type argumentType = paramTwo.getTypeReference().getTarget();
				
				if (argumentType == null || parameterType == null) {
					return false;
				}
				
				if (!parameterType.eIsProxy() || !argumentType.eIsProxy()) {
					parametersMatch = parametersMatch && parameterType.isSuperType(
						paramOne.getTypeReference().getArrayDimension(),
						argumentType, paramTwo.getTypeReference());
				} else {
					return false;
				}
			}
			
			Type target = one.getTypeReference().getTarget();
			parametersMatch = parametersMatch && target.isSuperType(one.getTypeReference().getArrayDimension(),
				two.getTypeReference().getTarget(), two.getTypeReference());
			
			return parametersMatch;
		}
		return false;
	}
	
	/**
	 * Returns a list of all statements within the block of a method.
	 * This is a legacy method to provide a stable and backwards-compatible API.
	 * 
	 * @param me the method for which the statements are obtained.
	 * @return the list of all statements.
	 * @deprecated Use getBlock().getStatements().
	 */
	@Deprecated
	public static EList<Statement> getStatements(Method me) {
		Block b = getBlock(me);
		return b != null ? b.getStatements() : new BasicEList<>();
	}
	
	/**
	 * Returns a block representing the body of a method.
	 * 
	 * @param me the method for which the body is returned.
	 * @return the block or null if the method has no implementation.
	 */
	public static Block getBlock(Method me) {
		if (me.getStatement() instanceof Block) {
			return (Block) me.getStatement();
		}
		return null;
	}
}
