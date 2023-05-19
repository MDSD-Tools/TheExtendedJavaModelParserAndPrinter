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

package tools.mdsd.jamopp.parser.jdt.singlefile;

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
import org.eclipse.jdt.core.dom.ITypeBinding;
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

import tools.mdsd.jamopp.options.ParserOptions;
import tools.mdsd.jamopp.resolution.bindings.JDTBindingConverterUtility;

class ExpressionConverterUtility {
	@SuppressWarnings("unchecked")
	static tools.mdsd.jamopp.model.java.expressions.Expression convertToExpression(Expression expr) {
		if (expr.getNodeType() == ASTNode.ASSIGNMENT) {
			Assignment assign = (Assignment) expr;
			tools.mdsd.jamopp.model.java.expressions.AssignmentExpression result =
					tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory
				.eINSTANCE.createAssignmentExpression();
			result.setChild((tools.mdsd.jamopp.model.java.expressions.AssignmentExpressionChild)
					convertToExpression(assign.getLeftHandSide()));
			result.setAssignmentOperator(convertToAssignmentOperator(assign.getOperator()));
			result.setValue(convertToExpression(assign.getRightHandSide()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
			return result;
		} else if (expr.getNodeType() == ASTNode.CONDITIONAL_EXPRESSION) {
			return convertToConditionalExpression((ConditionalExpression) expr);
		} else if (expr.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression infix = (InfixExpression) expr;
			if (infix.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
				tools.mdsd.jamopp.model.java.expressions.ConditionalOrExpression result;
				tools.mdsd.jamopp.model.java.expressions.Expression ex = convertToExpression(infix.getLeftOperand());
				if (ex instanceof tools.mdsd.jamopp.model.java.expressions.ConditionalOrExpression) {
					result = (tools.mdsd.jamopp.model.java.expressions.ConditionalOrExpression) ex;
				} else {
					result = tools.mdsd.jamopp.model.java.expressions
							.ExpressionsFactory.eINSTANCE.createConditionalOrExpression();
					result.getChildren().add((tools.mdsd.jamopp.model.java.expressions
							.ConditionalOrExpressionChild) ex);
				}
				result.getChildren().add((tools.mdsd.jamopp.model.java.expressions
						.ConditionalOrExpressionChild) convertToExpression(infix.getRightOperand()));
				infix.extendedOperands().forEach(obj -> result.getChildren().add(
					(tools.mdsd.jamopp.model.java.expressions.ConditionalOrExpressionChild)
					convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, infix);
				return result;
			} else if (infix.getOperator() == InfixExpression.Operator.CONDITIONAL_AND) {
				tools.mdsd.jamopp.model.java.expressions.ConditionalAndExpression result;
				tools.mdsd.jamopp.model.java.expressions.Expression ex = convertToExpression(infix.getLeftOperand());
				if (ex instanceof tools.mdsd.jamopp.model.java.expressions.ConditionalAndExpression) {
					result = (tools.mdsd.jamopp.model.java.expressions.ConditionalAndExpression) ex;
				} else {
					result = tools.mdsd.jamopp.model.java.expressions
							.ExpressionsFactory.eINSTANCE.createConditionalAndExpression();
					result.getChildren().add((tools.mdsd.jamopp.model.java.expressions
							.ConditionalAndExpressionChild) ex);
				}
				result.getChildren().add((tools.mdsd.jamopp.model.java.expressions
						.ConditionalAndExpressionChild) convertToExpression(infix.getRightOperand()));
				infix.extendedOperands().forEach(obj -> result.getChildren().add(
					(tools.mdsd.jamopp.model.java.expressions.ConditionalAndExpressionChild)
					convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, infix);
				return result;
			} else if (infix.getOperator() == InfixExpression.Operator.OR) {
				tools.mdsd.jamopp.model.java.expressions.InclusiveOrExpression result;
				tools.mdsd.jamopp.model.java.expressions.Expression ex = convertToExpression(infix.getLeftOperand());
				if (ex instanceof tools.mdsd.jamopp.model.java.expressions.InclusiveOrExpression) {
					result = (tools.mdsd.jamopp.model.java.expressions.InclusiveOrExpression) ex;
				} else {
					result = tools.mdsd.jamopp.model.java.expressions
							.ExpressionsFactory.eINSTANCE.createInclusiveOrExpression();
					result.getChildren().add((tools.mdsd.jamopp.model.java.expressions
							.InclusiveOrExpressionChild) ex);
				}
				result.getChildren().add((tools.mdsd.jamopp.model.java.expressions
						.InclusiveOrExpressionChild) convertToExpression(infix.getRightOperand()));
				infix.extendedOperands().forEach(obj -> result.getChildren().add(
					(tools.mdsd.jamopp.model.java.expressions.InclusiveOrExpressionChild)
					convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, infix);
				return result;
			} else if (infix.getOperator() == InfixExpression.Operator.XOR) {
				tools.mdsd.jamopp.model.java.expressions.ExclusiveOrExpression result;
				tools.mdsd.jamopp.model.java.expressions.Expression ex = convertToExpression(infix.getLeftOperand());
				if (ex instanceof tools.mdsd.jamopp.model.java.expressions.ExclusiveOrExpression) {
					result = (tools.mdsd.jamopp.model.java.expressions.ExclusiveOrExpression) ex;
				} else {
					result = tools.mdsd.jamopp.model.java.expressions
							.ExpressionsFactory.eINSTANCE.createExclusiveOrExpression();
					result.getChildren().add((tools.mdsd.jamopp.model.java.expressions
							.ExclusiveOrExpressionChild) ex);
				}
				result.getChildren().add((tools.mdsd.jamopp.model.java.expressions
						.ExclusiveOrExpressionChild) convertToExpression(infix.getRightOperand()));
				infix.extendedOperands().forEach(obj -> result.getChildren().add(
					(tools.mdsd.jamopp.model.java.expressions.ExclusiveOrExpressionChild)
					convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, infix);
				return result;
			} else if (infix.getOperator() == InfixExpression.Operator.AND) {
				tools.mdsd.jamopp.model.java.expressions.AndExpression result;
				tools.mdsd.jamopp.model.java.expressions.Expression ex = convertToExpression(infix.getLeftOperand());
				if (ex instanceof tools.mdsd.jamopp.model.java.expressions.AndExpression) {
					result = (tools.mdsd.jamopp.model.java.expressions.AndExpression) ex;
				} else {
					result = tools.mdsd.jamopp.model.java.expressions
							.ExpressionsFactory.eINSTANCE.createAndExpression();
					result.getChildren().add((tools.mdsd.jamopp.model.java.expressions
							.AndExpressionChild) ex);					
				}
				result.getChildren().add((tools.mdsd.jamopp.model.java.expressions
						.AndExpressionChild) convertToExpression(infix.getRightOperand()));
				infix.extendedOperands().forEach(obj -> result.getChildren().add(
					(tools.mdsd.jamopp.model.java.expressions.AndExpressionChild)
					convertToExpression((Expression) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, infix);
				return result;
			} else if (infix.getOperator() == InfixExpression.Operator.EQUALS
					|| infix.getOperator() == InfixExpression.Operator.NOT_EQUALS) {
				return convertToEqualityExpression(infix);
			} else if (infix.getOperator() == InfixExpression.Operator.GREATER
				|| infix.getOperator() == InfixExpression.Operator.GREATER_EQUALS
				|| infix.getOperator() == InfixExpression.Operator.LESS
				|| infix.getOperator() == InfixExpression.Operator.LESS_EQUALS) {
				return convertToRelationExpression(infix);
			} else if (infix.getOperator() == InfixExpression.Operator.LEFT_SHIFT
				|| infix.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_SIGNED
				|| infix.getOperator() == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
				return convertToShiftExpression(infix);
			} else if (infix.getOperator() == InfixExpression.Operator.PLUS
					|| infix.getOperator() == InfixExpression.Operator.MINUS) {
				return convertToAdditiveExpression(infix);
			} else if (infix.getOperator() == InfixExpression.Operator.TIMES
				|| infix.getOperator() == InfixExpression.Operator.DIVIDE
				|| infix.getOperator() == InfixExpression.Operator.REMAINDER) {
				return convertToMultiplicativeExpression(infix);
			}
		} else if (expr.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION) {
			InstanceofExpression castedExpr = (InstanceofExpression) expr;
			tools.mdsd.jamopp.model.java.expressions.InstanceOfExpression result = tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory.eINSTANCE.createInstanceOfExpression();
			result.setChild((tools.mdsd.jamopp.model.java.expressions
					.InstanceOfExpressionChild) convertToExpression(castedExpr.getLeftOperand()));
			result.setTypeReference(BaseConverterUtility.convertToTypeReference(castedExpr.getRightOperand()));
			BaseConverterUtility.convertToArrayDimensionsAndSet(castedExpr.getRightOperand(), result.getTypeReference());
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, castedExpr);
			return result;
		} else if (expr.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression prefixExpr = (PrefixExpression) expr;
			if (prefixExpr.getOperator() == PrefixExpression.Operator.COMPLEMENT
				|| prefixExpr.getOperator() == PrefixExpression.Operator.NOT
				|| prefixExpr.getOperator() == PrefixExpression.Operator.PLUS
				|| prefixExpr.getOperator() == PrefixExpression.Operator.MINUS) {
				return convertToUnaryExpression(prefixExpr);
			} else if (prefixExpr.getOperator() == PrefixExpression.Operator.DECREMENT
					|| prefixExpr.getOperator() == PrefixExpression.Operator.INCREMENT) {
				tools.mdsd.jamopp.model.java.expressions.PrefixUnaryModificationExpression result =
						tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory
					.eINSTANCE.createPrefixUnaryModificationExpression();
				if (prefixExpr.getOperator() == PrefixExpression.Operator.DECREMENT) {
					result.setOperator(tools.mdsd.jamopp.model.java.operators
							.OperatorsFactory.eINSTANCE.createMinusMinus());
				} else {
					result.setOperator(tools.mdsd.jamopp.model.java.operators
							.OperatorsFactory.eINSTANCE.createPlusPlus());
				}
				result.setChild((tools.mdsd.jamopp.model.java.expressions
						.UnaryModificationExpressionChild) convertToExpression(prefixExpr.getOperand()));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, prefixExpr);
				return result;
			}
		} else if (expr.getNodeType() == ASTNode.POSTFIX_EXPRESSION) {
			PostfixExpression postfixExpr = (PostfixExpression) expr;
			tools.mdsd.jamopp.model.java.expressions.SuffixUnaryModificationExpression result =
					tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory
				.eINSTANCE.createSuffixUnaryModificationExpression();
			if (postfixExpr.getOperator() == PostfixExpression.Operator.DECREMENT) {
				result.setOperator(tools.mdsd.jamopp.model.java.operators
						.OperatorsFactory.eINSTANCE.createMinusMinus());
			} else {
				result.setOperator(tools.mdsd.jamopp.model.java.operators
						.OperatorsFactory.eINSTANCE.createPlusPlus());
			}
			result.setChild((tools.mdsd.jamopp.model.java.expressions
					.UnaryModificationExpressionChild) convertToExpression(postfixExpr.getOperand()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, postfixExpr);
			return result;
		} else if (expr.getNodeType() == ASTNode.CAST_EXPRESSION) {
			CastExpression castExpr = (CastExpression) expr;
			tools.mdsd.jamopp.model.java.expressions.CastExpression result =
				tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory
				.eINSTANCE.createCastExpression();
			if (castExpr.getType().isIntersectionType()) {
				IntersectionType interType = (IntersectionType) castExpr.getType();
				result.setTypeReference(BaseConverterUtility
						.convertToTypeReference((Type) interType.types().get(0)));
				BaseConverterUtility.convertToArrayDimensionsAndSet((Type) interType.types().get(0),
						result.getTypeReference());
				for (int index = 1; index < interType.types().size(); index++) {
					result.getAdditionalBounds().add(BaseConverterUtility
							.convertToTypeReference((Type) interType.types().get(index)));
				}
			} else {
				result.setTypeReference(BaseConverterUtility.convertToTypeReference(castExpr.getType()));
				BaseConverterUtility.convertToArrayDimensionsAndSet(castExpr.getType(),
						result.getTypeReference());
			}
			result.setGeneralChild(convertToExpression(castExpr.getExpression()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, castExpr);
			return result;
		} else if (expr.getNodeType() == ASTNode.SWITCH_EXPRESSION) {
			SwitchExpression switchExpr = (SwitchExpression) expr;
			tools.mdsd.jamopp.model.java.statements.Switch result =
					tools.mdsd.jamopp.model.java.statements.StatementsFactory.eINSTANCE.createSwitch();
			result.setVariable(convertToExpression(switchExpr.getExpression()));
			StatementConverterUtility.convertToSwitchCasesAndSet(result, switchExpr.statements());
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, switchExpr);
			return result;
		} else if (expr instanceof MethodReference) {
			return convertToMethodReferenceExpression((MethodReference) expr);
		} else if (expr.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
			LambdaExpression lambda = (LambdaExpression) expr;
			tools.mdsd.jamopp.model.java.expressions.LambdaExpression result =
					tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory
				.eINSTANCE.createLambdaExpression();
			if (lambda.parameters().size() > 0 && lambda.parameters().get(0) instanceof VariableDeclarationFragment) {
				tools.mdsd.jamopp.model.java.expressions.ImplicitlyTypedLambdaParameters param;
				if (!lambda.hasParentheses()) {
					param = tools.mdsd.jamopp.model.java.expressions
							.ExpressionsFactory.eINSTANCE.createSingleImplicitLambdaParameter();
				} else {
					param = tools.mdsd.jamopp.model.java.expressions
							.ExpressionsFactory.eINSTANCE.createImplicitlyTypedLambdaParameters();
				}
				lambda.parameters().forEach(obj -> {
					VariableDeclarationFragment frag = (VariableDeclarationFragment) obj;
					tools.mdsd.jamopp.model.java.parameters.OrdinaryParameter nextParam = tools.mdsd.jamopp.model.java.parameters.ParametersFactory.eINSTANCE.createOrdinaryParameter();
					tools.mdsd.jamopp.model.java.types.InferableType type = tools.mdsd.jamopp.model.java.types
							.TypesFactory.eINSTANCE.createInferableType();
					IVariableBinding varBind = frag.resolveBinding();
					ITypeBinding c = varBind == null ? null : varBind.getType();
					if (c != null && !c.isRecovered()
							&& ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.isTrue()) {
						type.getActualTargets().addAll(
								JDTBindingConverterUtility.convertToTypeReferences(c));
					}
					nextParam.setTypeReference(type);
					nextParam.setName(frag.getName().getIdentifier());
					LayoutInformationConverter.convertToMinimalLayoutInformation(nextParam, frag);
					param.getParameters().add(nextParam);
				});
				result.setParameters(param);
			} else {
				tools.mdsd.jamopp.model.java.expressions.ExplicitlyTypedLambdaParameters param =
					tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory
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
	
	private static tools.mdsd.jamopp.model.java.operators.AssignmentOperator
			convertToAssignmentOperator(Assignment.Operator op) {
		if (op == Assignment.Operator.ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignment();
		} else if (op == Assignment.Operator.BIT_AND_ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignmentAnd();
		} else if (op == Assignment.Operator.BIT_OR_ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignmentOr();
		} else if (op == Assignment.Operator.BIT_XOR_ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignmentExclusiveOr();
		} else if (op == Assignment.Operator.DIVIDE_ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignmentDivision();
		} else if (op == Assignment.Operator.LEFT_SHIFT_ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignmentLeftShift();
		} else if (op == Assignment.Operator.MINUS_ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignmentMinus();
		} else if (op == Assignment.Operator.PLUS_ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignmentPlus();
		} else if (op == Assignment.Operator.REMAINDER_ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignmentModulo();
		} else if (op == Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignmentRightShift();
		} else if (op == Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE
					.createAssignmentUnsignedRightShift();
		} else { // op == Assignment.Operator.TIMES_ASSIGN
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAssignmentMultiplication();
		}
	}
	
	static tools.mdsd.jamopp.model.java.expressions.ConditionalExpression
			convertToConditionalExpression(ConditionalExpression expr) {
		tools.mdsd.jamopp.model.java.expressions.ConditionalExpression result =
				tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory.eINSTANCE.createConditionalExpression();
		result.setChild((tools.mdsd.jamopp.model.java.expressions.ConditionalExpressionChild)
				convertToExpression(expr.getExpression()));
		result.setExpressionIf(convertToExpression(expr.getThenExpression()));
		result.setGeneralExpressionElse(convertToExpression(expr.getElseExpression()));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static tools.mdsd.jamopp.model.java.expressions.EqualityExpression
			convertToEqualityExpression(InfixExpression expr) {
		tools.mdsd.jamopp.model.java.expressions.EqualityExpression result =
				tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory.eINSTANCE.createEqualityExpression();
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
	
	private static tools.mdsd.jamopp.model.java.operators.EqualityOperator
			convertToEqualityOperator(InfixExpression.Operator op) {
		if (op == InfixExpression.Operator.EQUALS) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createEqual();
		} else if (op == InfixExpression.Operator.NOT_EQUALS) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createNotEqual();
		}
		return null;
	}
	
	private static void mergeEqualityExpressionAndExpression(
			tools.mdsd.jamopp.model.java.expressions.EqualityExpression eqExpr,
			tools.mdsd.jamopp.model.java.expressions.Expression potChild) {
		if (potChild instanceof tools.mdsd.jamopp.model.java.expressions.EqualityExpressionChild) {
			eqExpr.getChildren().add((tools.mdsd.jamopp.model.java.expressions.EqualityExpressionChild) potChild);
		} else {
			tools.mdsd.jamopp.model.java.expressions.EqualityExpression expr =
					(tools.mdsd.jamopp.model.java.expressions.EqualityExpression) potChild;
			eqExpr.getChildren().addAll(expr.getChildren());
			eqExpr.getEqualityOperators().addAll(expr.getEqualityOperators());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static tools.mdsd.jamopp.model.java.expressions.RelationExpression
			convertToRelationExpression(InfixExpression expr) {
		tools.mdsd.jamopp.model.java.expressions.RelationExpression result =
				tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory.eINSTANCE.createRelationExpression();
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
	
	private static tools.mdsd.jamopp.model.java.operators.RelationOperator
			convertToRelationOperator(InfixExpression.Operator op) {
		if (op == InfixExpression.Operator.GREATER) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createGreaterThan();
		} else if (op == InfixExpression.Operator.GREATER_EQUALS) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createGreaterThanOrEqual();
		} else if (op == InfixExpression.Operator.LESS) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createLessThan();
		} else if (op == InfixExpression.Operator.LESS_EQUALS) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createLessThanOrEqual();
		}
		return null;
	}
	
	private static void mergeRelationExpressionAndExpression(
			tools.mdsd.jamopp.model.java.expressions.RelationExpression relExpr,
			tools.mdsd.jamopp.model.java.expressions.Expression potChild) {
		if (potChild instanceof tools.mdsd.jamopp.model.java.expressions.RelationExpressionChild) {
			relExpr.getChildren().add((tools.mdsd.jamopp.model.java.expressions.RelationExpressionChild) potChild);
		} else {
			tools.mdsd.jamopp.model.java.expressions.RelationExpression expr =
					(tools.mdsd.jamopp.model.java.expressions.RelationExpression) potChild;
			relExpr.getChildren().addAll(expr.getChildren());
			relExpr.getRelationOperators().addAll(expr.getRelationOperators());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static tools.mdsd.jamopp.model.java.expressions.ShiftExpression convertToShiftExpression(InfixExpression expr) {
		tools.mdsd.jamopp.model.java.expressions.ShiftExpression result =
				tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory.eINSTANCE.createShiftExpression();
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
	
	private static tools.mdsd.jamopp.model.java.operators.ShiftOperator convertToShiftOperator(InfixExpression.Operator op) {
		if (op == InfixExpression.Operator.LEFT_SHIFT) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createLeftShift();
		} else if (op == InfixExpression.Operator.RIGHT_SHIFT_SIGNED) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createRightShift();
		} else if (op == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createUnsignedRightShift();
		}
		return null;
	}
	
	private static void mergeShiftExpressionAndExpression(
			tools.mdsd.jamopp.model.java.expressions.ShiftExpression shiftExpr,
			tools.mdsd.jamopp.model.java.expressions.Expression potChild) {
		if (potChild instanceof tools.mdsd.jamopp.model.java.expressions.ShiftExpressionChild) {
			shiftExpr.getChildren().add((tools.mdsd.jamopp.model.java.expressions.ShiftExpressionChild) potChild);
		} else {
			tools.mdsd.jamopp.model.java.expressions.ShiftExpression expr =
					(tools.mdsd.jamopp.model.java.expressions.ShiftExpression) potChild;
			shiftExpr.getChildren().addAll(expr.getChildren());
			shiftExpr.getShiftOperators().addAll(expr.getShiftOperators());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static tools.mdsd.jamopp.model.java.expressions.AdditiveExpression
			convertToAdditiveExpression(InfixExpression expr) {
		tools.mdsd.jamopp.model.java.expressions.AdditiveExpression result =
				tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory.eINSTANCE.createAdditiveExpression();
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
	
	private static tools.mdsd.jamopp.model.java.operators.AdditiveOperator
			convertToAdditiveOperator(InfixExpression.Operator op) {
		if (op == InfixExpression.Operator.PLUS) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAddition();
		} else if (op == InfixExpression.Operator.MINUS) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createSubtraction();
		}
		return null;
	}
	
	private static void mergeAdditiveExpressionAndExpression(
			tools.mdsd.jamopp.model.java.expressions.AdditiveExpression addExpr,
			tools.mdsd.jamopp.model.java.expressions.Expression potChild) {
		if (potChild instanceof tools.mdsd.jamopp.model.java.expressions.AdditiveExpressionChild) {
			addExpr.getChildren().add((tools.mdsd.jamopp.model.java.expressions.AdditiveExpressionChild) potChild);
		} else {
			tools.mdsd.jamopp.model.java.expressions.AdditiveExpression expr =
					(tools.mdsd.jamopp.model.java.expressions.AdditiveExpression) potChild;
			addExpr.getChildren().addAll(expr.getChildren());
			addExpr.getAdditiveOperators().addAll(expr.getAdditiveOperators());
		}
	}
	
	@SuppressWarnings("unchecked")
	private static tools.mdsd.jamopp.model.java.expressions.MultiplicativeExpression
			convertToMultiplicativeExpression(InfixExpression expr) {
		tools.mdsd.jamopp.model.java.expressions.MultiplicativeExpression result = tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory.eINSTANCE.createMultiplicativeExpression();
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
	
	private static tools.mdsd.jamopp.model.java.operators.MultiplicativeOperator
			convertToMultiplicativeOperator(InfixExpression.Operator op) {
		if (op == InfixExpression.Operator.TIMES) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createMultiplication();
		} else if (op == InfixExpression.Operator.DIVIDE) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createDivision();
		} else if (op == InfixExpression.Operator.REMAINDER) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createRemainder();
		}
		return null;
	}
	
	private static void mergeMultiplicativeExpressionAndExpression(
			tools.mdsd.jamopp.model.java.expressions.MultiplicativeExpression mulExpr,
			tools.mdsd.jamopp.model.java.expressions.Expression potChild) {
		if (potChild instanceof tools.mdsd.jamopp.model.java.expressions.MultiplicativeExpressionChild) {
			mulExpr.getChildren().add((tools.mdsd.jamopp.model.java.expressions.MultiplicativeExpressionChild) potChild);
		} else {
			tools.mdsd.jamopp.model.java.expressions.MultiplicativeExpression expr =
					(tools.mdsd.jamopp.model.java.expressions.MultiplicativeExpression) potChild;
			mulExpr.getChildren().addAll(expr.getChildren());
			mulExpr.getMultiplicativeOperators().addAll(expr.getMultiplicativeOperators());
		}
	}
	
	private static tools.mdsd.jamopp.model.java.expressions.UnaryExpression convertToUnaryExpression(PrefixExpression expr) {
		tools.mdsd.jamopp.model.java.expressions.UnaryExpression result =
				tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory.eINSTANCE.createUnaryExpression();
		result.getOperators().add(convertToUnaryOperator(expr.getOperator()));
		tools.mdsd.jamopp.model.java.expressions.Expression potChild = convertToExpression(expr.getOperand());
		if (potChild instanceof tools.mdsd.jamopp.model.java.expressions.UnaryExpressionChild) {
			result.setChild((tools.mdsd.jamopp.model.java.expressions.UnaryExpressionChild) potChild);
		} else {
			tools.mdsd.jamopp.model.java.expressions.UnaryExpression secRes =
					(tools.mdsd.jamopp.model.java.expressions.UnaryExpression) potChild;
			result.getOperators().addAll(secRes.getOperators());
			result.setChild(secRes.getChild());
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
		return result;
	}
	
	private static tools.mdsd.jamopp.model.java.operators.UnaryOperator convertToUnaryOperator(PrefixExpression.Operator op) {
		if (op == PrefixExpression.Operator.COMPLEMENT) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createComplement();
		} else if (op == PrefixExpression.Operator.NOT) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createNegate();
		} else if (op == PrefixExpression.Operator.PLUS) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createAddition();
		} else if (op == PrefixExpression.Operator.MINUS) {
			return tools.mdsd.jamopp.model.java.operators.OperatorsFactory.eINSTANCE.createSubtraction();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static tools.mdsd.jamopp.model.java.expressions.MethodReferenceExpression
			convertToMethodReferenceExpression(MethodReference ref) {
		if (ref.getNodeType() == ASTNode.CREATION_REFERENCE) {
			CreationReference crRef = (CreationReference) ref;
			if (crRef.getType().isArrayType()) {
				tools.mdsd.jamopp.model.java.expressions.ArrayConstructorReferenceExpression result =
					tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory
					.eINSTANCE.createArrayConstructorReferenceExpression();
				result.setTypeReference(BaseConverterUtility.convertToTypeReference(crRef.getType()));
				BaseConverterUtility.convertToArrayDimensionsAndSet(crRef.getType(), result.getTypeReference());
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, crRef);
				return result;
			} else {
				tools.mdsd.jamopp.model.java.expressions.ClassTypeConstructorReferenceExpression result =
					tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory
					.eINSTANCE.createClassTypeConstructorReferenceExpression();
				result.setTypeReference(BaseConverterUtility.convertToTypeReference(crRef.getType()));
				crRef.typeArguments().forEach(obj -> result.getCallTypeArguments().add(
						BaseConverterUtility.convertToTypeArgument((Type) obj)));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, crRef);
				return result;
			}
		} else {
			tools.mdsd.jamopp.model.java.expressions.PrimaryExpressionReferenceExpression result =
				tools.mdsd.jamopp.model.java.expressions.ExpressionsFactory
				.eINSTANCE.createPrimaryExpressionReferenceExpression();
			if (ref.getNodeType() == ASTNode.TYPE_METHOD_REFERENCE) {
				TypeMethodReference typeRef = (TypeMethodReference) ref;
				result.setChild(ReferenceConverterUtility.convertToReference(typeRef.getType()));
				typeRef.typeArguments().forEach(obj -> result.getCallTypeArguments().add(
						BaseConverterUtility.convertToTypeArgument((Type) obj)));
				result.setMethodReference(ReferenceConverterUtility.convertToReference(typeRef.getName()));
			} else if (ref.getNodeType() == ASTNode.SUPER_METHOD_REFERENCE) {
				SuperMethodReference superRef = (SuperMethodReference) ref;
				if (superRef.getQualifier() != null) {
					tools.mdsd.jamopp.model.java.references.Reference child =
							ReferenceConverterUtility.convertToReference(superRef.getQualifier());
					tools.mdsd.jamopp.model.java.references.SelfReference lastPart = tools.mdsd.jamopp.model.java.references.ReferencesFactory.eINSTANCE.createSelfReference();
					lastPart.setSelf(tools.mdsd.jamopp.model.java.literals
							.LiteralsFactory.eINSTANCE.createSuper());
					tools.mdsd.jamopp.model.java.references.Reference part = child;
					tools.mdsd.jamopp.model.java.references.Reference next = child.getNext();
					while (next != null) {
						part = next;
						next = part.getNext();
					}
					part.setNext(lastPart);
					result.setChild(child);
				} else {
					tools.mdsd.jamopp.model.java.references.SelfReference child = tools.mdsd.jamopp.model.java.references.ReferencesFactory.eINSTANCE.createSelfReference();
					child.setSelf(tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createSuper());
					result.setChild(child);
				}
				superRef.typeArguments().forEach(obj -> result.getCallTypeArguments().add(
						BaseConverterUtility.convertToTypeArgument((Type) obj)));
				result.setMethodReference(ReferenceConverterUtility.convertToReference(superRef.getName()));
			} else if (ref.getNodeType() == ASTNode.EXPRESSION_METHOD_REFERENCE) {
				ExpressionMethodReference exprRef = (ExpressionMethodReference) ref;
				result.setChild((tools.mdsd.jamopp.model.java.expressions
						.MethodReferenceExpressionChild) convertToExpression(exprRef.getExpression()));
				exprRef.typeArguments().forEach(obj -> result.getCallTypeArguments().add(
						BaseConverterUtility.convertToTypeArgument((Type) obj)));
				result.setMethodReference(ReferenceConverterUtility.convertToReference(exprRef.getName()));
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, ref);
			return result;
		}
	}
	
	private static tools.mdsd.jamopp.model.java.expressions.PrimaryExpression convertToPrimaryExpression(Expression expr) {
		if (expr.getNodeType() == ASTNode.BOOLEAN_LITERAL) {
			BooleanLiteral lit = (BooleanLiteral) expr;
			tools.mdsd.jamopp.model.java.literals.BooleanLiteral result =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createBooleanLiteral();
			result.setValue(lit.booleanValue());
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, lit);
			return result;
		} else if (expr.getNodeType() == ASTNode.NULL_LITERAL) {
			tools.mdsd.jamopp.model.java.literals.NullLiteral result =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createNullLiteral();
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, expr);
			return result;
		} else if (expr.getNodeType() == ASTNode.CHARACTER_LITERAL) {
			CharacterLiteral lit = (CharacterLiteral) expr;
			tools.mdsd.jamopp.model.java.literals.CharacterLiteral result =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createCharacterLiteral();
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
