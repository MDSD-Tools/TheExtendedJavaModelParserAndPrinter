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

import org.emftext.language.java.expressions.AdditiveExpression;
import org.emftext.language.java.expressions.AndExpression;
import org.emftext.language.java.expressions.ArrayConstructorReferenceExpression;
import org.emftext.language.java.expressions.AssignmentExpression;
import org.emftext.language.java.expressions.CastExpression;
import org.emftext.language.java.expressions.ClassTypeConstructorReferenceExpression;
import org.emftext.language.java.expressions.ConditionalAndExpression;
import org.emftext.language.java.expressions.ConditionalExpression;
import org.emftext.language.java.expressions.ConditionalOrExpression;
import org.emftext.language.java.expressions.EqualityExpression;
import org.emftext.language.java.expressions.ExclusiveOrExpression;
import org.emftext.language.java.expressions.ExplicitlyTypedLambdaParameters;
import org.emftext.language.java.expressions.ExpressionList;
import org.emftext.language.java.expressions.ImplicitlyTypedLambdaParameters;
import org.emftext.language.java.expressions.InclusiveOrExpression;
import org.emftext.language.java.expressions.InstanceOfExpression;
import org.emftext.language.java.expressions.LambdaExpression;
import org.emftext.language.java.expressions.MultiplicativeExpression;
import org.emftext.language.java.expressions.NestedExpression;
import org.emftext.language.java.expressions.PrefixUnaryModificationExpression;
import org.emftext.language.java.expressions.PrimaryExpressionReferenceExpression;
import org.emftext.language.java.expressions.RelationExpression;
import org.emftext.language.java.expressions.ShiftExpression;
import org.emftext.language.java.expressions.SingleImplicitLambdaParameter;
import org.emftext.language.java.expressions.SuffixUnaryModificationExpression;
import org.emftext.language.java.expressions.UnaryExpression;
import org.emftext.language.java.expressions.util.ExpressionsSwitch;
import org.emftext.language.java.generics.GenericsPackage;
import org.emftext.language.java.operators.UnaryOperator;
import org.emftext.language.java.parameters.Parameter;
import org.emftext.language.java.references.ReferencesPackage;
import org.emftext.language.java.types.TypeReference;

class ExpressionsPrinterSwitch extends ExpressionsSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	ExpressionsPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseLambdaExpression(LambdaExpression element) {
		try {
			parent.doSwitch(element.getParameters());
			writer.append(" -> ");
			parent.doSwitch(element.getBody());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseSingleImplicitLambdaParameter(SingleImplicitLambdaParameter element) {
		try {
			writer.append(element.getParameters().get(0).getName());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseImplicitlyTypedLambdaParameters(ImplicitlyTypedLambdaParameters element) {
		try {
			writer.append("(");
			for (int index = 0; index < element.getParameters().size(); index++) {
				writer.append(element.getParameters().get(index).getName());
				if (index < element.getParameters().size() - 1) {
					writer.append(", ");
				}
			}
			writer.append(")");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseExplicitlyTypedLambdaParameters(ExplicitlyTypedLambdaParameters element) {
		try {
			writer.append("(");
			for (int index = 0; index < element.getParameters().size(); index++) {
				Parameter param = element.getParameters().get(index);
				parent.doSwitch(param);
				if (index < element.getParameters().size() - 1) {
					writer.append(", ");
				}
			}
			writer.append(")");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAssignmentExpression(AssignmentExpression element) {
		parent.doSwitch(element.getChild());
		if (element.getAssignmentOperator() != null) {
			parent.doSwitch(element.getAssignmentOperator());
			parent.doSwitch(element.getValue());
		}
		return true;
	}
	
	@Override
	public Boolean caseConditionalExpression(ConditionalExpression element) {
		try {
			parent.doSwitch(element.getChild());
			if (element.getExpressionIf() != null) {
				writer.append(" ? ");
				parent.doSwitch(element.getExpressionIf());
				writer.append(" : ");
				parent.doSwitch(element.getGeneralExpressionElse());
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	
	@Override
	public Boolean caseConditionalOrExpression(ConditionalOrExpression element) {
		try {
			parent.doSwitch(element.getChildren().get(0));
			for (int index = 1; index < element.getChildren().size(); index++) {
				writer.append(" || ");
				parent.doSwitch(element.getChildren().get(index));
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseConditionalAndExpression(ConditionalAndExpression element) {
		try {
			parent.doSwitch(element.getChildren().get(0));
			for (int index = 1; index < element.getChildren().size(); index++) {
				writer.append(" && ");
				parent.doSwitch(element.getChildren().get(index));
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseInclusiveOrExpression(InclusiveOrExpression element) {
		try {
			parent.doSwitch(element.getChildren().get(0));
			for (int index = 1; index < element.getChildren().size(); index++) {
				writer.append(" | ");
				parent.doSwitch(element.getChildren().get(index));
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseExclusiveOrExpression(ExclusiveOrExpression element) {
		try {
			parent.doSwitch(element.getChildren().get(0));
			for (int index = 1; index < element.getChildren().size(); index++) {
				writer.append(" ^ ");
				parent.doSwitch(element.getChildren().get(index));
			}
		} catch (IOException e) {
		}
		return true;
	}

	@Override
	public Boolean caseAndExpression(AndExpression element) {
		try {
			parent.doSwitch(element.getChildren().get(0));
			for (int index = 1; index < element.getChildren().size(); index++) {
				writer.append(" & ");
				parent.doSwitch(element.getChildren().get(index));
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseEqualityExpression(EqualityExpression element) {
		parent.doSwitch(element.getChildren().get(0));
		for (int index = 1; index < element.getChildren().size(); index++) {
			parent.doSwitch(element.getEqualityOperators().get(index - 1));
			parent.doSwitch(element.getChildren().get(index));
		}
		return true;
	}
	
	@Override
	public Boolean caseInstanceOfExpression(InstanceOfExpression element) {
		try {
			parent.doSwitch(element.getChild());
			writer.append(" instanceof ");
			parent.doSwitch(element.getTypeReference());
			element.getTypeReference().getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseRelationExpression(RelationExpression element) {
		parent.doSwitch(element.getChildren().get(0));
		for (int index = 1; index < element.getChildren().size(); index++) {
			parent.doSwitch(element.getRelationOperators().get(index - 1));
			parent.doSwitch(element.getChildren().get(index));
		}
		return true;
	}
	
	@Override
	public Boolean caseShiftExpression(ShiftExpression element) {
		parent.doSwitch(element.getChildren().get(0));
		for (int index = 1; index < element.getChildren().size(); index++) {
			parent.doSwitch(element.getShiftOperators().get(index - 1));
			parent.doSwitch(element.getChildren().get(index));
		}
		return true;
	}
	
	@Override
	public Boolean caseAdditiveExpression(AdditiveExpression element) {
		parent.doSwitch(element.getChildren().get(0));
		for (int index = 1; index < element.getChildren().size(); index++) {
			parent.doSwitch(element.getAdditiveOperators().get(index - 1));
			parent.doSwitch(element.getChildren().get(index));
		}
		return true;
	}
	
	@Override
	public Boolean caseMultiplicativeExpression(MultiplicativeExpression element) {
		parent.doSwitch(element.getChildren().get(0));
		for (int index = 1; index < element.getChildren().size(); index++) {
			parent.doSwitch(element.getMultiplicativeOperators().get(index - 1));
			parent.doSwitch(element.getChildren().get(index));
		}
		return true;
	}
	
	@Override
	public Boolean caseUnaryExpression(UnaryExpression element) {
		for (UnaryOperator op : element.getOperators()) {
			parent.doSwitch(op);
		}
		parent.doSwitch(element.getChild());
		return true;
	}
	
	@Override
	public Boolean casePrefixUnaryModificationExpression(PrefixUnaryModificationExpression element) {
		if (element.getOperator() != null) {
			parent.doSwitch(element.getOperator());
		}
		parent.doSwitch(element.getChild());
		return true;
	}
	
	@Override
	public Boolean caseSuffixUnaryModificationExpression(SuffixUnaryModificationExpression element) {
		parent.doSwitch(element.getChild());
		if (element.getOperator() != null) {
			parent.doSwitch(element.getOperator());
		}
		return true;
	}
	
	@Override
	public Boolean caseCastExpression(CastExpression element) {
		try {
			writer.append("(");
			parent.doSwitch(element.getTypeReference());
			element.getTypeReference().getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			for (TypeReference ref : element.getAdditionalBounds()) {
				writer.append(" & ");
				parent.doSwitch(ref);
			}
			writer.append(") ");
			parent.doSwitch(element.getGeneralChild());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean casePrimaryExpressionReferenceExpression(PrimaryExpressionReferenceExpression element) {
		try {
			parent.doSwitch(element.getChild());
			if (element.getMethodReference() != null) {
				writer.append("::");
				parent.doSwitch(GenericsPackage.Literals.CALL_TYPE_ARGUMENTABLE, element);
				parent.doSwitch(element.getMethodReference());
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseClassTypeConstructorReferenceExpression(ClassTypeConstructorReferenceExpression element) {
		try {
			parent.doSwitch(element.getTypeReference());
			writer.append("::");
			parent.doSwitch(GenericsPackage.Literals.CALL_TYPE_ARGUMENTABLE, element);
			writer.append("new");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseArrayConstructorReferenceExpression(ArrayConstructorReferenceExpression element) {
		try {
			parent.doSwitch(element.getTypeReference());
			element.getTypeReference().getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			writer.append("::new");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseNestedExpression(NestedExpression element) {
		try {
			writer.append("(");
			parent.doSwitch(element.getExpression());
			writer.append(")");
			parent.doSwitch(ReferencesPackage.Literals.REFERENCE, element);
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseExpressionList(ExpressionList element) {
		try {
			for (int index = 0; index < element.getExpressions().size(); index++) {
				parent.doSwitch(element.getExpressions().get(index));
				if (index < element.getExpressions().size() - 1) {
					writer.append(", ");
				}
			}
		} catch (IOException e) {
		}
		return true;
	}
}
