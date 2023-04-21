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

package jamopp.parser.jdt.singlefile;

import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.YieldStatement;

class StatementConverterUtility {
	private static HashSet<org.emftext.language.java.statements.JumpLabel> currentJumpLabels = new HashSet<>();
	
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.statements.Block convertToBlock(Block block) {
		org.emftext.language.java.statements.Block result =
				org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createBlock();
		result.setName("");
		block.statements().forEach(obj -> result.getStatements().add(convertToStatement((Statement) obj)));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, block);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	static org.emftext.language.java.statements.Statement convertToStatement(Statement statement) {
		if (statement.getNodeType() == ASTNode.ASSERT_STATEMENT) {
			AssertStatement assertSt = (AssertStatement) statement;
			org.emftext.language.java.statements.Assert result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createAssert();
			result.setCondition(ExpressionConverterUtility.convertToExpression(assertSt.getExpression()));
			if (assertSt.getMessage() != null) {
				result.setErrorMessage(ExpressionConverterUtility.convertToExpression(assertSt.getMessage()));
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, assertSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.BLOCK) {
			return convertToBlock((Block) statement);
		} else if (statement.getNodeType() == ASTNode.BREAK_STATEMENT) {
			BreakStatement breakSt = (BreakStatement) statement;
			org.emftext.language.java.statements.Break result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createBreak();
			if (breakSt.getLabel() != null) {
				org.emftext.language.java.statements.JumpLabel proxyTarget = currentJumpLabels.stream()
						.filter(label -> label.getName().equals(
								breakSt.getLabel().getIdentifier())).findFirst().get();
				result.setTarget(proxyTarget);
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, breakSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.CONTINUE_STATEMENT) {
			ContinueStatement conSt = (ContinueStatement) statement;
			org.emftext.language.java.statements.Continue result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createContinue();
			if (conSt.getLabel() != null) {
				org.emftext.language.java.statements.JumpLabel proxyTarget = currentJumpLabels.stream()
						.filter(label -> label.getName().equals(
								conSt.getLabel().getIdentifier())).findFirst().get();
				result.setTarget(proxyTarget);
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, conSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.DO_STATEMENT) {
			DoStatement doSt = (DoStatement) statement;
			org.emftext.language.java.statements.DoWhileLoop result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createDoWhileLoop();
			result.setCondition(ExpressionConverterUtility.convertToExpression(doSt.getExpression()));
			result.setStatement(convertToStatement(doSt.getBody()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, doSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.EMPTY_STATEMENT) {
			org.emftext.language.java.statements.EmptyStatement result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createEmptyStatement();
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, statement);
			return result;
		} else if (statement.getNodeType() == ASTNode.ENHANCED_FOR_STATEMENT) {
			EnhancedForStatement forSt = (EnhancedForStatement) statement;
			org.emftext.language.java.statements.ForEachLoop result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createForEachLoop();
			result.setNext(ClassifierConverterUtility.convertToOrdinaryParameter(forSt.getParameter()));
			result.setCollection(ExpressionConverterUtility.convertToExpression(forSt.getExpression()));
			result.setStatement(convertToStatement(forSt.getBody()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, forSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
			ExpressionStatement exprSt = (ExpressionStatement) statement;
			if (exprSt.getExpression().getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
				org.emftext.language.java.statements.LocalVariableStatement result = org.emftext.language
						.java.statements.StatementsFactory.eINSTANCE.createLocalVariableStatement();
				result.setVariable(convertToLocalVariable(
						(VariableDeclarationExpression) exprSt.getExpression()));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, exprSt);
				return result;
			} else {
				org.emftext.language.java.statements.ExpressionStatement result = org.emftext.language
						.java.statements.StatementsFactory.eINSTANCE.createExpressionStatement();
				result.setExpression(ExpressionConverterUtility.convertToExpression(exprSt.getExpression()));
				LayoutInformationConverter.convertToMinimalLayoutInformation(result, exprSt);
				return result;
			}
		} else if (statement.getNodeType() == ASTNode.FOR_STATEMENT) {
			ForStatement forSt = (ForStatement) statement;
			org.emftext.language.java.statements.ForLoop result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createForLoop();
			if (forSt.initializers().size() == 1
					&& forSt.initializers().get(0) instanceof VariableDeclarationExpression) {
				result.setInit(convertToLocalVariable(
						(VariableDeclarationExpression) forSt.initializers().get(0)));
			} else {
				org.emftext.language.java.expressions.ExpressionList ini = org.emftext.language.java
						.expressions.ExpressionsFactory.eINSTANCE.createExpressionList();
				forSt.initializers().forEach(obj -> ini.getExpressions().add(
						ExpressionConverterUtility.convertToExpression((Expression) obj)));
				result.setInit(ini);
			}
			if (forSt.getExpression() != null) {
				result.setCondition(ExpressionConverterUtility.convertToExpression(forSt.getExpression()));
			}
			forSt.updaters().forEach(obj -> result.getUpdates().add(
					ExpressionConverterUtility.convertToExpression((Expression) obj)));
			result.setStatement(convertToStatement(forSt.getBody()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, forSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.IF_STATEMENT) {
			IfStatement ifSt = (IfStatement) statement;
			org.emftext.language.java.statements.Condition result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createCondition();
			result.setCondition(ExpressionConverterUtility.convertToExpression(ifSt.getExpression()));
			result.setStatement(convertToStatement(ifSt.getThenStatement()));
			if (ifSt.getElseStatement() != null) {
				result.setElseStatement(convertToStatement(ifSt.getElseStatement()));
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, ifSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.LABELED_STATEMENT) {
			LabeledStatement labelSt = (LabeledStatement) statement;
			org.emftext.language.java.statements.JumpLabel result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createJumpLabel();
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(labelSt.getLabel(), result);
			currentJumpLabels.add(result);
			result.setStatement(convertToStatement(labelSt.getBody()));
			currentJumpLabels.remove(result);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, labelSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.RETURN_STATEMENT) {
			ReturnStatement retSt = (ReturnStatement) statement;
			org.emftext.language.java.statements.Return result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createReturn();
			if (retSt.getExpression() != null) {
				result.setReturnValue(ExpressionConverterUtility.convertToExpression(retSt.getExpression()));
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, retSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.SWITCH_STATEMENT) {
			return convertToSwitch((SwitchStatement) statement);
		} else if (statement.getNodeType() == ASTNode.SYNCHRONIZED_STATEMENT) {
			SynchronizedStatement synSt = (SynchronizedStatement) statement;
			org.emftext.language.java.statements.SynchronizedBlock result = org.emftext.language.java
					.statements.StatementsFactory.eINSTANCE.createSynchronizedBlock();
			result.setLockProvider(ExpressionConverterUtility.convertToExpression(synSt.getExpression()));
			result.setBlock(convertToBlock(synSt.getBody()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, synSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.THROW_STATEMENT) {
			ThrowStatement throwSt = (ThrowStatement) statement;
			org.emftext.language.java.statements.Throw result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createThrow();
			result.setThrowable(ExpressionConverterUtility.convertToExpression(throwSt.getExpression()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, throwSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.TRY_STATEMENT) {
			TryStatement trySt = (TryStatement) statement;
			org.emftext.language.java.statements.TryBlock result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createTryBlock();
			trySt.resources().forEach(obj -> {
				Expression resExpr = (Expression) obj;
				if (resExpr instanceof VariableDeclarationExpression) {
					result.getResources().add(convertToLocalVariable(
							(VariableDeclarationExpression) resExpr));
				} else {
					result.getResources().add((org.emftext.language.java.references
							.ElementReference) ReferenceConverterUtility.convertToReference(resExpr));
				}
			});
			result.setBlock(convertToBlock(trySt.getBody()));
			trySt.catchClauses().forEach(obj -> result.getCatchBlocks().add(convertToCatchBlock((CatchClause) obj)));
			if (trySt.getFinally() != null) {
				result.setFinallyBlock(convertToBlock(trySt.getFinally()));
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, trySt);
			return result;
		} else if (statement.getNodeType() == ASTNode.TYPE_DECLARATION_STATEMENT) {
			TypeDeclarationStatement declSt = (TypeDeclarationStatement) statement;
			return ClassifierConverterUtility.convertToConcreteClassifier(declSt.getDeclaration());
		} else if (statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			VariableDeclarationStatement varSt = (VariableDeclarationStatement) statement;
			org.emftext.language.java.statements.LocalVariableStatement result = org.emftext.language
					.java.statements.StatementsFactory.eINSTANCE.createLocalVariableStatement();
			org.emftext.language.java.variables.LocalVariable locVar =
					org.emftext.language.java.variables.VariablesFactory.eINSTANCE.createLocalVariable();
			varSt.modifiers().forEach(obj -> locVar.getAnnotationsAndModifiers().add(
				AnnotationInstanceOrModifierConverterUtility
				.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
			locVar.setTypeReference(BaseConverterUtility.convertToTypeReference(varSt.getType()));
			VariableDeclarationFragment frag = (VariableDeclarationFragment) varSt.fragments().get(0);
			BaseConverterUtility.convertToSimpleNameOnlyAndSet(frag.getName(), locVar);
			BaseConverterUtility.convertToArrayDimensionsAndSet(varSt.getType(), locVar.getTypeReference());
			frag.extraDimensions().forEach(obj -> BaseConverterUtility
					.convertToArrayDimensionAfterAndSet((Dimension) obj, locVar.getTypeReference()));
			if (frag.getInitializer() != null) {
				locVar.setInitialValue(ExpressionConverterUtility.convertToExpression(frag.getInitializer()));
			}
			for (int index = 1; index < varSt.fragments().size(); index++) {
				locVar.getAdditionalLocalVariables().add(convertToAdditionalLocalVariable(
						(VariableDeclarationFragment) varSt.fragments().get(index),
						varSt.getType()));
			}
			result.setVariable(locVar);
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, varSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.WHILE_STATEMENT) {
			WhileStatement whileSt = (WhileStatement) statement;
			org.emftext.language.java.statements.WhileLoop result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createWhileLoop();
			result.setCondition(ExpressionConverterUtility.convertToExpression(whileSt.getExpression()));
			result.setStatement(convertToStatement(whileSt.getBody()));
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, whileSt);
			return result;
		} else if (statement.getNodeType() == ASTNode.YIELD_STATEMENT) {
			YieldStatement yieldSt = (YieldStatement) statement;
			org.emftext.language.java.statements.YieldStatement result =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createYieldStatement();
			if (yieldSt.getExpression() != null) {
				result.setYieldExpression(ExpressionConverterUtility.convertToExpression(
						yieldSt.getExpression()));
			}
			LayoutInformationConverter.convertToMinimalLayoutInformation(result, yieldSt);
			return result;
		} else {
			org.emftext.language.java.statements.ExpressionStatement result = org.emftext.language
					.java.statements.StatementsFactory.eINSTANCE.createExpressionStatement();
			result.setExpression(ReferenceConverterUtility.convertToReference(statement));
			return result;
		}
	}
	
	private static org.emftext.language.java.statements.Switch convertToSwitch(SwitchStatement switchSt) {
		org.emftext.language.java.statements.Switch result =
				org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createSwitch();
		result.setVariable(ExpressionConverterUtility.convertToExpression(switchSt.getExpression()));
		convertToSwitchCasesAndSet(result, switchSt.statements());
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, switchSt);
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	static void convertToSwitchCasesAndSet(org.emftext.language.java.statements.Switch switchExprSt,
			List switchStatementList) {
		org.emftext.language.java.statements.SwitchCase currentCase = null;
		for (int index = 0; index < switchStatementList.size(); index++) {
			Statement st = (Statement) switchStatementList.get(index);
			if (st.getNodeType() == ASTNode.SWITCH_CASE) {
				currentCase = convertToSwitchCase((SwitchCase) st);
				switchExprSt.getCases().add(currentCase);
			} else if (currentCase instanceof org.emftext.language.java.statements.SwitchRule
					&& st.getNodeType() == ASTNode.YIELD_STATEMENT) {
				YieldStatement ys = (YieldStatement) st;
				org.emftext.language.java.statements.ExpressionStatement exprSt = org.emftext.language
						.java.statements.StatementsFactory.eINSTANCE.createExpressionStatement();
				exprSt.setExpression(ExpressionConverterUtility.convertToExpression(ys.getExpression()));
				currentCase.getStatements().add(exprSt);
			} else {
				currentCase.getStatements().add(convertToStatement(st));
			}
		}
	}
	
	private static org.emftext.language.java.statements.SwitchCase convertToSwitchCase(SwitchCase switchCase) {
		org.emftext.language.java.statements.SwitchCase result = null;
		if (switchCase.isSwitchLabeledRule() && switchCase.isDefault()) {
			result = org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createDefaultSwitchRule();
		} else if (switchCase.isSwitchLabeledRule() && !switchCase.isDefault()) {
			org.emftext.language.java.statements.NormalSwitchRule normalRule =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createNormalSwitchRule();
			normalRule.setCondition(ExpressionConverterUtility.convertToExpression(
					(Expression) switchCase.expressions().get(0)));
			for (int index = 1; index < switchCase.expressions().size(); index++) {
				Expression expr = (Expression) switchCase.expressions().get(index);
				normalRule.getAdditionalConditions().add(ExpressionConverterUtility.convertToExpression(expr));
			}
			result = normalRule;
		} else if (!switchCase.isSwitchLabeledRule() && switchCase.isDefault()) {
			result = org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createDefaultSwitchCase();
		} else { // !switchCase.isSwitchLabeledRule() && !switchCase.isDefault()
			org.emftext.language.java.statements.NormalSwitchCase normalCase =
					org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createNormalSwitchCase();
			normalCase.setCondition(ExpressionConverterUtility.convertToExpression(
					(Expression) switchCase.expressions().get(0)));
			for (int index = 1; index < switchCase.expressions().size(); index++) {
				Expression expr = (Expression) switchCase.expressions().get(index);
				normalCase.getAdditionalConditions().add(ExpressionConverterUtility.convertToExpression(expr));
			}
			result = normalCase;
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, switchCase);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.statements.CatchBlock convertToCatchBlock(CatchClause block) {
		org.emftext.language.java.statements.CatchBlock result =
				org.emftext.language.java.statements.StatementsFactory.eINSTANCE.createCatchBlock();
		org.emftext.language.java.parameters.CatchParameter param =
				org.emftext.language.java.parameters.ParametersFactory.eINSTANCE.createCatchParameter();
		SingleVariableDeclaration decl = block.getException();
		decl.modifiers().forEach(obj -> param.getAnnotationsAndModifiers().add(
			AnnotationInstanceOrModifierConverterUtility
			.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
		if (decl.getType().isUnionType()) {
			UnionType un = (UnionType) decl.getType();
			param.setTypeReference(BaseConverterUtility.convertToTypeReference((Type) un.types().get(0)));
			for (int index = 1; index < un.types().size(); index++) {
				param.getTypeReferences().add(BaseConverterUtility.convertToTypeReference(
						(Type) un.types().get(index)));
			}
		} else {
			param.setTypeReference(BaseConverterUtility.convertToTypeReference(decl.getType()));
		}
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(decl.getName(), param);
		result.setParameter(param);
		result.setBlock(convertToBlock(block.getBody()));
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, block);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.variables.AdditionalLocalVariable
			convertToAdditionalLocalVariable(VariableDeclarationFragment frag,
			Type type) {
		org.emftext.language.java.variables.AdditionalLocalVariable result =
				org.emftext.language.java.variables.VariablesFactory.eINSTANCE.createAdditionalLocalVariable();
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(frag.getName(), result);
		result.setTypeReference(BaseConverterUtility.convertToTypeReference(type));
		BaseConverterUtility.convertToArrayDimensionsAndSet(type, result.getTypeReference());
		frag.extraDimensions().forEach(obj -> BaseConverterUtility
				.convertToArrayDimensionAfterAndSet((Dimension) obj, result.getTypeReference()));
		if (frag.getInitializer() != null) {
			result.setInitialValue(ExpressionConverterUtility.convertToExpression(frag.getInitializer()));
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, frag);
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static org.emftext.language.java.variables.LocalVariable
			convertToLocalVariable(VariableDeclarationExpression expr) {
		org.emftext.language.java.variables.LocalVariable loc =
				org.emftext.language.java.variables.VariablesFactory.eINSTANCE.createLocalVariable();
		expr.modifiers().forEach(obj -> loc.getAnnotationsAndModifiers().add(AnnotationInstanceOrModifierConverterUtility
			.converToModifierOrAnnotationInstance((IExtendedModifier) obj)));
		loc.setTypeReference(BaseConverterUtility.convertToTypeReference(expr.getType()));
		VariableDeclarationFragment frag = (VariableDeclarationFragment) expr.fragments().get(0);
		BaseConverterUtility.convertToSimpleNameOnlyAndSet(frag.getName(), loc);
		BaseConverterUtility.convertToArrayDimensionsAndSet(expr.getType(), loc.getTypeReference());
		frag.extraDimensions().forEach(obj ->
			BaseConverterUtility.convertToArrayDimensionAfterAndSet((Dimension) obj, loc.getTypeReference()));
		if (frag.getInitializer() != null) {
			loc.setInitialValue(ExpressionConverterUtility.convertToExpression(frag.getInitializer()));
		}
		for (int index = 1; index < expr.fragments().size(); index++) {
			loc.getAdditionalLocalVariables().add(
					convertToAdditionalLocalVariable(
							(VariableDeclarationFragment) expr.fragments().get(index),
							expr.getType()));
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(loc, expr);
		return loc;
	}
}
