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
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

class ExpressionConverterUtility {
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.expressions.Expression convertToExpression(Expression expr) {
		if (expr.getNodeType() == ASTNode.ASSIGNMENT) {
			Assignment assign = (Assignment) expr;
			org.emftext.language.java.expressions.AssignmentExpression result = org.emftext.language.java.expressions.ExpressionsFactory
				.eINSTANCE.createAssignmentExpression();
			result.setChild((org.emftext.language.java.expressions.AssignmentExpressionChild) convertToExpression(assign.getLeftHandSide()));
			result.setAssignmentOperator(convertToAssignmentOperator(assign.getOperator()));
			result.setValue(convertToExpression(assign.getRightHandSide()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
			return result;
		} else if (expr.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION) {
			return convertToConditionalExpression((ConditionalExpression) expr);
		} else if (expr.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression infix = (InfixExpression) expr;
			if (infix.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
				org.emftext.language.java.expressions.ConditionalOrExpression result;
				org.emftext.language.java.expressions.Expression ex = convertToExpression(infix.getLeftOperand());
				if (ex instanceof org.emftext.language.java.expressions.ConditionalOrExpression) {
					result = (org.emftext.language.java.expressions.ConditionalOrExpression) ex;
				} else {
					result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createConditionalOrExpression();
					result.getChildren().add((org.emftext.language.java.expressions.ConditionalOrExpressionChild) ex);
				}
				result.getChildren().add((org.emftext.language.java.expressions.ConditionalOrExpressionChild) convertToExpression(infix.getRightOperand()));
				infix.extendedOperands().forEach(obj -> result.getChildren().add((org.emftext.language.java.expressions.ConditionalOrExpressionChild)
					convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, infix);
				return result;
			} else if (infix.getOperator() == InfixExpression.Operator.CONDITIONAL_AND) {
				org.emftext.language.java.expressions.ConditionalAndExpression result;
				org.emftext.language.java.expressions.Expression ex = convertToExpression(infix.getLeftOperand());
				if (ex instanceof org.emftext.language.java.expressions.ConditionalAndExpression) {
					result = (org.emftext.language.java.expressions.ConditionalAndExpression) ex;
				} else {
					result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createConditionalAndExpression();
					result.getChildren().add((org.emftext.language.java.expressions.ConditionalAndExpressionChild) ex);
				}
				result.getChildren().add((org.emftext.language.java.expressions.ConditionalAndExpressionChild) convertToExpression(infix.getRightOperand()));
				infix.extendedOperands().forEach(obj -> result.getChildren().add((org.emftext.language.java.expressions.ConditionalAndExpressionChild)
					convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, infix);
				return result;
			} else if (infix.getOperator() == InfixExpression.Operator.OR) {
				org.emftext.language.java.expressions.InclusiveOrExpression result;
				org.emftext.language.java.expressions.Expression ex = convertToExpression(infix.getLeftOperand());
				if (ex instanceof org.emftext.language.java.expressions.InclusiveOrExpression) {
					result = (org.emftext.language.java.expressions.InclusiveOrExpression) ex;
				} else {
					result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createInclusiveOrExpression();
					result.getChildren().add((org.emftext.language.java.expressions.InclusiveOrExpressionChild) ex);
				}
				result.getChildren().add((org.emftext.language.java.expressions.InclusiveOrExpressionChild) convertToExpression(infix.getRightOperand()));
				infix.extendedOperands().forEach(obj -> result.getChildren().add((org.emftext.language.java.expressions.InclusiveOrExpressionChild)
					convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, infix);
				return result;
			} else if (infix.getOperator() == InfixExpression.Operator.XOR) {
				org.emftext.language.java.expressions.ExclusiveOrExpression result;
				org.emftext.language.java.expressions.Expression ex = convertToExpression(infix.getLeftOperand());
				if (ex instanceof org.emftext.language.java.expressions.ExclusiveOrExpression) {
					result = (org.emftext.language.java.expressions.ExclusiveOrExpression) ex;
				} else {
					result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createExclusiveOrExpression();
					result.getChildren().add((org.emftext.language.java.expressions.ExclusiveOrExpressionChild) ex);
				}
				result.getChildren().add((org.emftext.language.java.expressions.ExclusiveOrExpressionChild) convertToExpression(infix.getRightOperand()));
				infix.extendedOperands().forEach(obj -> result.getChildren().add((org.emftext.language.java.expressions.ExclusiveOrExpressionChild)
					convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, infix);
				return result;
			} else if (infix.getOperator() == InfixExpression.Operator.AND) {
				org.emftext.language.java.expressions.AndExpression result;
				org.emftext.language.java.expressions.Expression ex = convertToExpression(infix.getLeftOperand());
				if (ex instanceof org.emftext.language.java.expressions.AndExpression) {
					result = (org.emftext.language.java.expressions.AndExpression) ex;
				} else {
					result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createAndExpression();
					result.getChildren().add((org.emftext.language.java.expressions.AndExpressionChild) ex);					
				}
				result.getChildren().add((org.emftext.language.java.expressions.AndExpressionChild) convertToExpression(infix.getRightOperand()));
				infix.extendedOperands().forEach(obj -> result.getChildren().add((org.emftext.language.java.expressions.AndExpressionChild)
					convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, infix);
				return result;
			} else if (infix.getOperator() == InfixExpression.Operator.EQUALS || infix.getOperator() == InfixExpression.Operator.NOT_EQUALS) {
				return convertToEqualityExpression(infix);
			} else if (infix.getOperator() == InfixExpression.Operator.GREATER || infix.getOperator() == InfixExpression.Operator.GREATER_EQUALS
				|| infix.getOperator() == InfixExpression.Operator.LESS || infix.getOperator() == InfixExpression.Operator.LESS_EQUALS) {
				return convertToRelationExpression(infix);
			} else if (infix.getOperator() == InfixExpression.Operator.LEFT_SHIFT || infix.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_SIGNED
				|| infix.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
				return convertToShiftExpression(infix);
			} else if (infix.getOperator() == InfixExpression.Operator.PLUS || infix.getOperator() == InfixExpression.Operator.MINUS) {
				return convertToAdditiveExpression(infix);
			} else if (infix.getOperator() == InfixExpression.Operator.TIMES || infix.getOperator() == InfixExpression.Operator.DIVIDE
				|| infix.getOperator() == InfixExpression.Operator.REMAINDER) {
				return convertToMultiplicativeExpression(infix);
			}
		} else if (expr.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION) {
			InstanceofExpression castedExpr = (InstanceofExpression) expr;
			org.emftext.language.java.expressions.InstanceOfExpression result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createInstanceOfExpression();
			result.setChild((org.emftext.language.java.expressions.InstanceOfExpressionChild) convertToExpression(castedExpr.getLeftOperand()));
			result.setTypeReference(BaseConverterUtility.convertToTypeReference(castedExpr.getRightOperand()));
//			BaseConverterUtility.convertToArrayDimensionsAndSet(castedExpr.getRightOperand(), result);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, castedExpr);
			return result;
		} else if (expr.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression prefixExpr = (PrefixExpression) expr;
			if (prefixExpr.getOperator() == PrefixExpression.Operator.COMPLEMENT || prefixExpr.getOperator() == PrefixExpression.Operator.NOT
				|| prefixExpr.getOperator() == PrefixExpression.Operator.PLUS || prefixExpr.getOperator() == PrefixExpression.Operator.MINUS) {
				return convertToUnaryExpression(prefixExpr);
			} else if (prefixExpr.getOperator() == PrefixExpression.Operator.DECREMENT || prefixExpr.getOperator() == PrefixExpression.Operator.INCREMENT) {
				org.emftext.language.java.expressions.PrefixUnaryModificationExpression result = org.emftext.language.java.expressions.ExpressionsFactory
					.eINSTANCE.createPrefixUnaryModificationExpression();
				if (prefixExpr.getOperator() == PrefixExpression.Operator.DECREMENT) {
					result.setOperator(org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createMinusMinus());
				} else {
					result.setOperator(org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createPlusPlus());
				}
				result.setChild((org.emftext.language.java.expressions.UnaryModificationExpressionChild) convertToExpression(prefixExpr.getOperand()));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, prefixExpr);
				return result;
			}
		} else if (expr.getNodeType() == ASTNode.POSTFIX_EXPRESSION) {
			PostfixExpression postfixExpr = (PostfixExpression) expr;
			org.emftext.language.java.expressions.SuffixUnaryModificationExpression result = org.emftext.language.java.expressions.ExpressionsFactory
				.eINSTANCE.createSuffixUnaryModificationExpression();
			if (postfixExpr.getOperator() == PostfixExpression.Operator.DECREMENT) {
				result.setOperator(org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createMinusMinus());
			} else {
				result.setOperator(org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createPlusPlus());
			}
			result.setChild((org.emftext.language.java.expressions.UnaryModificationExpressionChild) convertToExpression(postfixExpr.getOperand()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, postfixExpr);
			return result;
		} else if (expr.getNodeType() == ASTNode.CAST_EXPRESSION) {
			CastExpression castExpr = (CastExpression) expr;
			org.emftext.language.java.expressions.CastExpression result = org.emftext.language.java.expressions.ExpressionsFactory
				.eINSTANCE.createCastExpression();
			if (castExpr.getType().isIntersectionType()) {
				IntersectionType interType = (IntersectionType) castExpr.getType();
				result.setTypeReference(BaseConverterUtility.convertToTypeReference((Type) interType.types().get(0)));
//				BaseConverterUtility.convertToArrayDimensionsAndSet((Type) interType.types().get(0), result);
				for (int index = 1; index < interType.types().size(); index++) {
					result.getAdditionalBounds().add(BaseConverterUtility.convertToTypeReference((Type) interType.types().get(index)));
				}
			} else {
				result.setTypeReference(BaseConverterUtility.convertToTypeReference(castExpr.getType()));
//				BaseConverterUtility.convertToArrayDimensionsAndSet(castExpr.getType(), result);
			}
			result.setGeneralChild(convertToExpression(castExpr.getExpression()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, castExpr);
			return result;
		} else if (expr.getNodeType() == ASTNode.SWITCH_EXPRESSION) {
			SwitchExpression switchExpr = (SwitchExpression) expr;
			org.emftext.language.java.statements.Switch result = org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createSwitch();
			result.setVariable(convertToExpression(switchExpr.getExpression()));
			StatementConverterUtility.convertToSwitchCasesAndSet(result, switchExpr.statements());
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, switchExpr);
			return result;
		} else if (expr instanceof MethodReference) {
			return convertToMethodReferenceExpression((MethodReference) expr);
		} else if (expr.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
			LambdaExpression lambda = (LambdaExpression) expr;
			org.emftext.language.java.expressions.LambdaExpression result = org.emftext.language.java.expressions.ExpressionsFactory
				.eINSTANCE.createLambdaExpression();
			if (lambda.parameters().size() > 0 && lambda.parameters().get(0) instanceof VariableDeclarationFragment) {
				org.emftext.language.java.expressions.ImplicitlyTypedLambdaParameters param;
				if (!lambda.hasParentheses()) {
					param = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createSingleImplicitLambdaParameter();
				} else {
					param = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createImplicitlyTypedLambdaParameters();
				}
				lambda.parameters().forEach(obj -> {
					VariableDeclarationFragment frag = (VariableDeclarationFragment) obj;
					IVariableBinding binding = frag.resolveBinding();
					org.emftext.language.java.parameters.OrdinaryParameter nextParam;
					if (binding != null) {
						nextParam = JDTResolverUtility.getOrdinaryParameter(binding);
						nextParam.setTypeReference(JDTBindingConverterUtility.convertToTypeReferences(
							binding.getType()).get(0));
					} else {
						nextParam = JDTResolverUtility.getOrdinaryParameter(frag.getName().getIdentifier() + frag.hashCode());
						nextParam.setTypeReference(org.emftext.language.java.types.TypesFactory.eINSTANCE.createVoid());
					}
					nextParam.setName(frag.getName().getIdentifier());
					param.getParameters().add(nextParam);
				});
				result.setParameters(param);
			} else {
				org.emftext.language.java.expressions.ExplicitlyTypedLambdaParameters param = org.emftext.language.java.expressions.ExpressionsFactory
					.eINSTANCE.createExplicitlyTypedLambdaParameters();
				lambda.parameters().forEach(obj -> param.getParameters().add(
					ClassifierConverterUtility.convertToOrdinaryParameter((SingleVariableDeclaration) obj)));
				result.setParameters(param);
			}
			if (lambda.getBody() instanceof Expression) {
				result.setBody(convertToExpression((Expression) lambda.getBody()));
			} else {
				result.setBody(StatementConverterUtility.convertToBlock((Block) lambda.getBody()));
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, lambda);
			return result;
		} else {
			return convertToPrimaryExpression(expr);
		}
		return null;
	}
	
	private static org.emftext.language.java.operators.AssignmentOperator convertToAssignmentOperator(Assignment.Operator op) {
		if (op == Assignment.Operator.ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignment();
		} else if (op == Assignment.Operator.BIT_AND_ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentAnd();
		} else if (op == Assignment.Operator.BIT_OR_ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentOr();
		} else if (op == Assignment.Operator.BIT_XOR_ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentExclusiveOr();
		} else if (op == Assignment.Operator.DIVIDE_ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentDivision();
		} else if (op == Assignment.Operator.LEFT_SHIFT_ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentLeftShift();
		} else if (op == Assignment.Operator.MINUS_ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentMinus();
		} else if (op == Assignment.Operator.PLUS_ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentPlus();
		} else if (op == Assignment.Operator.REMAINDER_ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentModulo();
		} else if (op == Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentRightShift();
		} else if (op == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentUnsignedRightShift();
		} else { // op == Assignment.Operator.TIMES_ASSIGN
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAssignmentMultiplication();
		}
	}
	
	static org.emftext.language.java.expressions.ConditionalExpression convertToConditionalExpression(ConditionalExpression expr) {
		org.emftext.language.java.expressions.ConditionalExpression result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createConditionalExpression();
		result.setChild((org.emftext.language.java.expressions.ConditionalExpressionChild) convertToExpression(expr.getExpression()));
		result.setExpressionIf(convertToExpression(expr.getThenExpression()));
		result.setGeneralExpressionElse(convertToExpression(expr.getElseExpression()));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.expressions.EqualityExpression convertToEqualityExpression(InfixExpression expr) {
		org.emftext.language.java.expressions.EqualityExpression result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createEqualityExpression();
		mergeEqualityExpressionAndExpression(result, convertToExpression(expr.getLeftOperand()));
		result.getEqualityOperators().add(convertToEqualityOperator(expr.getOperator()));
		mergeEqualityExpressionAndExpression(result, convertToExpression(expr.getRightOperand()));
		expr.extendedOperands().forEach(obj -> {
			result.getEqualityOperators().add(convertToEqualityOperator(expr.getOperator()));
			mergeEqualityExpressionAndExpression(result, convertToExpression((Expression) obj));
		});
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
		return result;
	}
	
	private static org.emftext.language.java.operators.EqualityOperator convertToEqualityOperator(InfixExpression.Operator op) {
		if (op == InfixExpression.Operator.EQUALS) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createEqual();
		} else if (op == InfixExpression.Operator.NOT_EQUALS) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createNotEqual();
		}
		return null;
	}
	
	private static void mergeEqualityExpressionAndExpression(org.emftext.language.java.expressions.EqualityExpression eqExpr, org.emftext.language.java.expressions.Expression potChild) {
		if (potChild instanceof org.emftext.language.java.expressions.EqualityExpressionChild) {
			eqExpr.getChildren().add((org.emftext.language.java.expressions.EqualityExpressionChild) potChild);
		} else {
			org.emftext.language.java.expressions.EqualityExpression expr = (org.emftext.language.java.expressions.EqualityExpression) potChild;
			eqExpr.getChildren().addAll(expr.getChildren());
			eqExpr.getEqualityOperators().addAll(expr.getEqualityOperators());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.expressions.RelationExpression convertToRelationExpression(InfixExpression expr) {
		org.emftext.language.java.expressions.RelationExpression result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createRelationExpression();
		mergeRelationExpressionAndExpression(result, convertToExpression(expr.getLeftOperand()));
		result.getRelationOperators().add(convertToRelationOperator(expr.getOperator()));
		mergeRelationExpressionAndExpression(result, convertToExpression(expr.getRightOperand()));
		expr.extendedOperands().forEach(obj -> {
			result.getRelationOperators().add(convertToRelationOperator(expr.getOperator()));
			mergeRelationExpressionAndExpression(result, convertToExpression((Expression) obj));
		});
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
		return result;
	}
	
	private static org.emftext.language.java.operators.RelationOperator convertToRelationOperator(InfixExpression.Operator op) {
		if (op == InfixExpression.Operator.GREATER) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createGreaterThan();
		} else if (op == InfixExpression.Operator.GREATER_EQUALS) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createGreaterThanOrEqual();
		} else if (op == InfixExpression.Operator.LESS) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createLessThan();
		} else if (op == InfixExpression.Operator.LESS_EQUALS) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createLessThanOrEqual();
		}
		return null;
	}
	
	private static void mergeRelationExpressionAndExpression(org.emftext.language.java.expressions.RelationExpression relExpr, org.emftext.language.java.expressions.Expression potChild) {
		if (potChild instanceof org.emftext.language.java.expressions.RelationExpressionChild) {
			relExpr.getChildren().add((org.emftext.language.java.expressions.RelationExpressionChild) potChild);
		} else {
			org.emftext.language.java.expressions.RelationExpression expr = (org.emftext.language.java.expressions.RelationExpression) potChild;
			relExpr.getChildren().addAll(expr.getChildren());
			relExpr.getRelationOperators().addAll(expr.getRelationOperators());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.expressions.ShiftExpression convertToShiftExpression(InfixExpression expr) {
		org.emftext.language.java.expressions.ShiftExpression result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createShiftExpression();
		mergeShiftExpressionAndExpression(result, convertToExpression(expr.getLeftOperand()));
		result.getShiftOperators().add(convertToShiftOperator(expr.getOperator()));
		mergeShiftExpressionAndExpression(result, convertToExpression(expr.getRightOperand()));
		expr.extendedOperands().forEach(obj -> {
			result.getShiftOperators().add(convertToShiftOperator(expr.getOperator()));
			mergeShiftExpressionAndExpression(result, convertToExpression((Expression) obj));
		});
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
		return result;
	}
	
	private static org.emftext.language.java.operators.ShiftOperator convertToShiftOperator(InfixExpression.Operator op) {
		if (op == InfixExpression.Operator.LEFT_SHIFT) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createLeftShift();
		} else if (op == InfixExpression.Operator.RIGHT_SHIFT_SIGNED) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createRightShift();
		} else if (op == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createUnsignedRightShift();
		}
		return null;
	}
	
	private static void mergeShiftExpressionAndExpression(org.emftext.language.java.expressions.ShiftExpression shiftExpr, org.emftext.language.java.expressions.Expression potChild) {
		if (potChild instanceof org.emftext.language.java.expressions.ShiftExpressionChild) {
			shiftExpr.getChildren().add((org.emftext.language.java.expressions.ShiftExpressionChild) potChild);
		} else {
			org.emftext.language.java.expressions.ShiftExpression expr = (org.emftext.language.java.expressions.ShiftExpression) potChild;
			shiftExpr.getChildren().addAll(expr.getChildren());
			shiftExpr.getShiftOperators().addAll(expr.getShiftOperators());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.expressions.AdditiveExpression convertToAdditiveExpression(InfixExpression expr) {
		org.emftext.language.java.expressions.AdditiveExpression result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createAdditiveExpression();
		mergeAdditiveExpressionAndExpression(result, convertToExpression(expr.getLeftOperand()));
		result.getAdditiveOperators().add(convertToAdditiveOperator(expr.getOperator()));
		mergeAdditiveExpressionAndExpression(result, convertToExpression(expr.getRightOperand()));
		expr.extendedOperands().forEach(obj -> {
			result.getAdditiveOperators().add(convertToAdditiveOperator(expr.getOperator()));
			mergeAdditiveExpressionAndExpression(result, convertToExpression((Expression) obj));
		});
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
		return result;
	}
	
	private static org.emftext.language.java.operators.AdditiveOperator convertToAdditiveOperator(InfixExpression.Operator op) {
		if (op == InfixExpression.Operator.PLUS) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAddition();
		} else if (op == InfixExpression.Operator.MINUS) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createSubtraction();
		}
		return null;
	}
	
	private static void mergeAdditiveExpressionAndExpression(org.emftext.language.java.expressions.AdditiveExpression addExpr, org.emftext.language.java.expressions.Expression potChild) {
		if (potChild instanceof org.emftext.language.java.expressions.AdditiveExpressionChild) {
			addExpr.getChildren().add((org.emftext.language.java.expressions.AdditiveExpressionChild) potChild);
		} else {
			org.emftext.language.java.expressions.AdditiveExpression expr = (org.emftext.language.java.expressions.AdditiveExpression) potChild;
			addExpr.getChildren().addAll(expr.getChildren());
			addExpr.getAdditiveOperators().addAll(expr.getAdditiveOperators());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.expressions.MultiplicativeExpression convertToMultiplicativeExpression(InfixExpression expr) {
		org.emftext.language.java.expressions.MultiplicativeExpression result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createMultiplicativeExpression();
		mergeMultiplicativeExpressionAndExpression(result, convertToExpression(expr.getLeftOperand()));
		result.getMultiplicativeOperators().add(convertToMultiplicativeOperator(expr.getOperator()));
		mergeMultiplicativeExpressionAndExpression(result, convertToExpression(expr.getRightOperand()));
		expr.extendedOperands().forEach(obj -> {
			result.getMultiplicativeOperators().add(convertToMultiplicativeOperator(expr.getOperator()));
			mergeMultiplicativeExpressionAndExpression(result, convertToExpression((Expression) obj));
		});
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
		return result;
	}
	
	private static org.emftext.language.java.operators.MultiplicativeOperator convertToMultiplicativeOperator(InfixExpression.Operator op) {
		if (op == InfixExpression.Operator.TIMES) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createMultiplication();
		} else if (op == InfixExpression.Operator.DIVIDE) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createDivision();
		} else if (op == InfixExpression.Operator.REMAINDER) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createRemainder();
		}
		return null;
	}
	
	private static void mergeMultiplicativeExpressionAndExpression(org.emftext.language.java.expressions.MultiplicativeExpression mulExpr, org.emftext.language.java.expressions.Expression potChild) {
		if (potChild instanceof org.emftext.language.java.expressions.MultiplicativeExpressionChild) {
			mulExpr.getChildren().add((org.emftext.language.java.expressions.MultiplicativeExpressionChild) potChild);
		} else {
			org.emftext.language.java.expressions.MultiplicativeExpression expr = (org.emftext.language.java.expressions.MultiplicativeExpression) potChild;
			mulExpr.getChildren().addAll(expr.getChildren());
			mulExpr.getMultiplicativeOperators().addAll(expr.getMultiplicativeOperators());
		}
	}
	
	private static org.emftext.language.java.expressions.UnaryExpression convertToUnaryExpression(PrefixExpression expr) {
		org.emftext.language.java.expressions.UnaryExpression result = org.emftext.language.java.expressions.ExpressionsFactory.eINSTANCE.createUnaryExpression();
		result.getOperators().add(convertToUnaryOperator(expr.getOperator()));
		org.emftext.language.java.expressions.Expression potChild = convertToExpression(expr.getOperand());
		if (potChild instanceof org.emftext.language.java.expressions.UnaryExpressionChild) {
			result.setChild((org.emftext.language.java.expressions.UnaryExpressionChild) potChild);
		} else {
			org.emftext.language.java.expressions.UnaryExpression secRes = (org.emftext.language.java.expressions.UnaryExpression) potChild;
			result.getOperators().addAll(secRes.getOperators());
			result.setChild(secRes.getChild());
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
		return result;
	}
	
	private static org.emftext.language.java.operators.UnaryOperator convertToUnaryOperator(PrefixExpression.Operator op) {
		if (op == PrefixExpression.Operator.COMPLEMENT) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createComplement();
		} else if (op == PrefixExpression.Operator.NOT) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createNegate();
		} else if (op == PrefixExpression.Operator.PLUS) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createAddition();
		} else if (op == PrefixExpression.Operator.MINUS) {
			return org.emftext.language.java.operators.OperatorsFactory.eINSTANCE.createSubtraction();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.expressions.MethodReferenceExpression convertToMethodReferenceExpression(MethodReference ref) {
		if (ref.getNodeType() == ASTNode.CREATION_REFERENCE) {
			CreationReference crRef = (CreationReference) ref;
			if (crRef.getType().isArrayType()) {
				org.emftext.language.java.expressions.ArrayConstructorReferenceExpression result = org.emftext.language.java.expressions.ExpressionsFactory
					.eINSTANCE.createArrayConstructorReferenceExpression();
				result.setTypeReference(BaseConverterUtility.convertToTypeReference(crRef.getType()));
//				BaseConverterUtility.convertToArrayDimensionsAndSet(crRef.getType(), result);
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, crRef);
				return result;
			} else {
				org.emftext.language.java.expressions.ClassTypeConstructorReferenceExpression result = org.emftext.language.java.expressions.ExpressionsFactory
					.eINSTANCE.createClassTypeConstructorReferenceExpression();
				result.setTypeReference(BaseConverterUtility.convertToTypeReference(crRef.getType()));
				crRef.typeArguments().forEach(obj -> result.getCallTypeArguments().add(BaseConverterUtility.convertToTypeArgument((Type) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, crRef);
				return result;
			}
		} else {
			org.emftext.language.java.expressions.PrimaryExpressionReferenceExpression result = org.emftext.language.java.expressions.ExpressionsFactory
				.eINSTANCE.createPrimaryExpressionReferenceExpression();
			if (ref.getNodeType() == ASTNode.TYPE_METHOD_REFERENCE) {
				TypeMethodReference typeRef = (TypeMethodReference) ref;
				result.setChild(ReferenceConverterUtility.convertToReference(typeRef.getType()));
				typeRef.typeArguments().forEach(obj -> result.getCallTypeArguments().add(BaseConverterUtility.convertToTypeArgument((Type) obj)));
				result.setMethodReference(ReferenceConverterUtility.convertToReference(typeRef.getName()));
			} else if (ref.getNodeType() == ASTNode.SUPER_METHOD_REFERENCE) {
				SuperMethodReference superRef = (SuperMethodReference) ref;
				if (superRef.getQualifier() != null) {
					org.emftext.language.java.references.Reference child = ReferenceConverterUtility.convertToReference(superRef.getQualifier());
					org.emftext.language.java.references.SelfReference lastPart = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createSelfReference();
					lastPart.setSelf(org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createSuper());
					org.emftext.language.java.references.Reference part = child;
					org.emftext.language.java.references.Reference next = child.getNext();
					while (next != null) {
						part = next;
						next = part.getNext();
					}
					part.setNext(lastPart);
					result.setChild(child);
				} else {
					org.emftext.language.java.references.SelfReference child = org.emftext.language.java.references.ReferencesFactory.eINSTANCE.createSelfReference();
					child.setSelf(org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createSuper());
					result.setChild(child);
				}
				superRef.typeArguments().forEach(obj -> result.getCallTypeArguments().add(BaseConverterUtility.convertToTypeArgument((Type) obj)));
				result.setMethodReference(ReferenceConverterUtility.convertToReference(superRef.getName()));
			} else if (ref.getNodeType() == ASTNode.EXPRESSION_METHOD_REFERENCE) {
				ExpressionMethodReference exprRef = (ExpressionMethodReference) ref;
				result.setChild((org.emftext.language.java.expressions.MethodReferenceExpressionChild) convertToExpression(exprRef.getExpression()));
				exprRef.typeArguments().forEach(obj -> result.getCallTypeArguments().add(BaseConverterUtility.convertToTypeArgument((Type) obj)));
				result.setMethodReference(ReferenceConverterUtility.convertToReference(exprRef.getName()));
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, ref);
			return result;
		}
	}
	
	private static org.emftext.language.java.expressions.PrimaryExpression convertToPrimaryExpression(Expression expr) {
		if (expr.getNodeType() == ASTNode.BOOLEAN_LITERAL) {
			BooleanLiteral lit = (BooleanLiteral) expr;
			org.emftext.language.java.literals.BooleanLiteral result = org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createBooleanLiteral();
			result.setValue(lit.booleanValue());
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, lit);
			return result;
		} else if (expr.getNodeType() == ASTNode.NULL_LITERAL) {
			org.emftext.language.java.literals.NullLiteral result = org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createNullLiteral();
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
			return result;
		} else if (expr.getNodeType() == ASTNode.CHARACTER_LITERAL) {
			CharacterLiteral lit = (CharacterLiteral) expr;
			org.emftext.language.java.literals.CharacterLiteral result = org.emftext.language.java.literals.LiteralsFactory.eINSTANCE.createCharacterLiteral();
			result.setValue(lit.getEscapedValue().substring(1, lit.getEscapedValue().length() - 1));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, lit);
			return result;
		} else if (expr.getNodeType() == ASTNode.NUMBER_LITERAL) {
			return NumberLiteralConverterUtility.convertToLiteral((NumberLiteral) expr);
		} else {
			return ReferenceConverterUtility.convertToReference(expr);
		}
	}
}
