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

package jamopp.printer;

import java.io.BufferedWriter;
import java.io.IOException;

import org.emftext.language.java.expressions.Expression;
import org.emftext.language.java.modifiers.Modifier;
import org.emftext.language.java.statements.Assert;
import org.emftext.language.java.statements.Block;
import org.emftext.language.java.statements.Break;
import org.emftext.language.java.statements.CatchBlock;
import org.emftext.language.java.statements.Condition;
import org.emftext.language.java.statements.Continue;
import org.emftext.language.java.statements.DefaultSwitchCase;
import org.emftext.language.java.statements.DefaultSwitchRule;
import org.emftext.language.java.statements.DoWhileLoop;
import org.emftext.language.java.statements.EmptyStatement;
import org.emftext.language.java.statements.ExpressionStatement;
import org.emftext.language.java.statements.ForEachLoop;
import org.emftext.language.java.statements.ForLoop;
import org.emftext.language.java.statements.JumpLabel;
import org.emftext.language.java.statements.LocalVariableStatement;
import org.emftext.language.java.statements.NormalSwitchCase;
import org.emftext.language.java.statements.NormalSwitchRule;
import org.emftext.language.java.statements.Return;
import org.emftext.language.java.statements.Statement;
import org.emftext.language.java.statements.Switch;
import org.emftext.language.java.statements.SwitchCase;
import org.emftext.language.java.statements.SynchronizedBlock;
import org.emftext.language.java.statements.Throw;
import org.emftext.language.java.statements.TryBlock;
import org.emftext.language.java.statements.WhileLoop;
import org.emftext.language.java.statements.YieldStatement;
import org.emftext.language.java.statements.util.StatementsSwitch;

public class StatementsPrinterSwitch extends StatementsSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	StatementsPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseAssert(Assert element) {
		try {
			writer.append("assert ");
			parent.doSwitch(element.getCondition());
			if (element.getErrorMessage() != null) {
				writer.append(" : ");
				parent.doSwitch(element.getErrorMessage());
			}
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseBreak(Break element) {
		try {
			writer.append("break");
			if (element.getTarget() != null) {
				writer.append(" " + element.getTarget().getName());
			}
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseCatchBlock(CatchBlock element) {
		try {
			writer.append("catch(");
			parent.doSwitch(element.getParameter());
			writer.append(")");
			parent.doSwitch(element.getBlock());
		} catch (IOException e) {
		}
		return true;
	}	
	
	@Override
	public Boolean caseContinue(Continue element) {
		try {
			writer.append("continue");
			if (element.getTarget() != null) {
				writer.append(" " + element.getTarget().getName());	
			}
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseDoWhileLoop(DoWhileLoop element) {
		try {
			writer.append("do\n");
			parent.doSwitch(element.getStatement());
			writer.append("while (");
			parent.doSwitch(element.getCondition());
			writer.append(");\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseEmptyStatement(EmptyStatement element) {
		try {
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseCondition(Condition element) {
		try {
			writer.append("if (");
			parent.doSwitch(element.getCondition());
			writer.append(")\n");
			parent.doSwitch(element.getStatement());
			if (element.getElseStatement() != null) {
				writer.append("else\n");
				parent.doSwitch(element.getElseStatement());
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseExpressionStatement(ExpressionStatement element) {
		try {
			parent.doSwitch(element.getExpression());
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseForLoop(ForLoop element) {
		try {
			writer.append("for (");
			if (element.getInit() != null) {
				parent.doSwitch(element.getInit());
			}
			writer.append(" ; ");
			if (element.getCondition() != null) {
				parent.doSwitch(element.getCondition());
			}
			writer.append(" ; ");
			for (int index = 0; index < element.getUpdates().size(); index++) {
				parent.doSwitch(element.getUpdates().get(index));
				if (index < element.getUpdates().size() - 1) {
					writer.append(", ");
				}
			}
			writer.append(")\n");
			parent.doSwitch(element.getStatement());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseForEachLoop(ForEachLoop element) {
		try {
			writer.append("for (");
			parent.doSwitch(element.getNext());
			writer.append(" : ");
			parent.doSwitch(element.getCollection());
			writer.append(")\n");
			parent.doSwitch(element.getStatement());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseJumpLabel(JumpLabel element) {
		try {
			writer.append(element.getName() + ": ");
			parent.doSwitch(element.getStatement());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseLocalVariableStatement(LocalVariableStatement element) {
		try {
			parent.doSwitch(element.getVariable());
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseBlock(Block element) {
		try {
			for (Modifier m : element.getModifiers()) {
				parent.doSwitch(m);
			}
			writer.append("{\n");
			for (Statement s : element.getStatements()) {
				parent.doSwitch(s);
			}
			writer.append("}\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseReturn(Return element) {
		try {
			writer.append("return");
			if (element.getReturnValue() != null) {
				writer.append(" ");
				parent.doSwitch(element.getReturnValue());
			}
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	
	@Override
	public Boolean caseSwitch(Switch element) {
		try {
			writer.append("switch (");
			parent.doSwitch(element.getVariable());
			writer.append(") {\n");
			for (SwitchCase cas : element.getCases()) {
				parent.doSwitch(cas);
			}
			writer.append("}\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseDefaultSwitchCase(DefaultSwitchCase element) {
		try {
			writer.append("default: ");
			for (Statement s : element.getStatements()) {
				parent.doSwitch(s);
			}
			writer.append("\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseNormalSwitchCase(NormalSwitchCase element) {
		try {
			writer.append("case ");
			parent.doSwitch(element.getCondition());
			for (Expression expr : element.getAdditionalConditions()) {
				writer.append(", ");
				parent.doSwitch(expr);
			}
			writer.append(": ");
			for (Statement s : element.getStatements()) {
				parent.doSwitch(s);
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseDefaultSwitchRule(DefaultSwitchRule element) {
		try {
			writer.append("default -> ");
			for (Statement s : element.getStatements()) {
				parent.doSwitch(s);
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseNormalSwitchRule(NormalSwitchRule element) {
		try {
			writer.append("case ");
			parent.doSwitch(element.getCondition());
			for (Expression expr : element.getAdditionalConditions()) {
				writer.append(", ");
				parent.doSwitch(expr);
			}
			writer.append(" -> ");
			for (Statement s : element.getStatements()) {
				parent.doSwitch(s);
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseSynchronizedBlock(SynchronizedBlock element) {
		try {
			writer.append("synchronized (");
			parent.doSwitch(element.getLockProvider());
			writer.append(") ");
			parent.doSwitch(element.getBlock());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseThrow(Throw element) {
		try {
			writer.append("throw ");
			parent.doSwitch(element.getThrowable());
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseTryBlock(TryBlock element) {
		try {
			writer.append("try");
			if (element.getResources().size() > 0) {
				writer.append("(");
				parent.doSwitch(element.getResources().get(0));
				for (int index = 1; index < element.getResources().size(); index++) {
					writer.append("; ");
					parent.doSwitch(element.getResources().get(index));
				}
				writer.append(")");
			}
			writer.append(" ");
			parent.doSwitch(element.getBlock());
			for (CatchBlock cat : element.getCatchBlocks()) {
				caseCatchBlock(cat);
			}
			if (element.getFinallyBlock() != null) {
				writer.append("finally ");
				parent.doSwitch(element.getFinallyBlock());
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseYieldStatement(YieldStatement element) {
		try {
			writer.append("yield ");
			parent.doSwitch(element.getYieldExpression());
			writer.append(";\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseWhileLoop(WhileLoop element) {
		try {
			writer.append("while (");
			parent.doSwitch(element.getCondition());
			writer.append(")\n");
			parent.doSwitch(element.getStatement());
		} catch (IOException e) {
		}
		return true;
	}
}
