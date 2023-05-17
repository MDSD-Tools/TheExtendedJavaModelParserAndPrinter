/*******************************************************************************
 * Copyright (c) 2006-2012
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

package org.emftext.language.java.test;

import java.math.BigInteger;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExportsDirective;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.ModuleModifier;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.OpensDirective;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ProvidesDirective;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.RequiresDirective;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.UsesDirective;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.eclipse.jdt.core.dom.YieldStatement;

/**
 * An extension of the JDT ASTMatcher that prints information whenever the
 * matching of two ASTs fails. In addition some normalizations are applied to
 * number literals to removed redundant characters that can cause comparison
 * failures even though the numbers are the same.
 */
public class TalkativeASTMatcher extends ASTMatcher {

	public TalkativeASTMatcher(boolean b) {
		super(b);
	}

	@Override
	public boolean match(AnnotationTypeDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(AnnotationTypeMemberDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(AnonymousClassDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ArrayAccess node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ArrayCreation node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ArrayInitializer node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ArrayType node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(AssertStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(Assignment node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(Block node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(BlockComment node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(BooleanLiteral node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(BreakStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(CastExpression node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(CatchClause node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(CharacterLiteral node, Object other) {
		if (!(other instanceof CharacterLiteral)) {
			return false;
		}
		CharacterLiteral o = (CharacterLiteral) other;
		String oToken = o.getEscapedValue();
		String nToken = node.getEscapedValue();

		// octal notation and escaped characters
		if (oToken.matches("\\'\\\\.+\\'")) {
			oToken = oToken.substring(1, oToken.length() - 1);
			oToken = "'" + CharacterEscaper.unescapeEscapedCharacters(oToken) + "'";
		}
		if (nToken.matches("\\'\\\\.+\\'")) {
			nToken = nToken.substring(1, nToken.length() - 1);
			nToken = "'" + CharacterEscaper.unescapeEscapedCharacters(nToken) + "'";
		}
		return setDiff(node, other, safeEquals(nToken, oToken));
	}

	@Override
	public boolean match(ClassInstanceCreation node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(CompilationUnit node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ConditionalExpression node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ConstructorInvocation node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ContinueStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(CreationReference node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(Dimension node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(DoStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(EmptyStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(EnhancedForStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(EnumConstantDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(EnumDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ExportsDirective node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ExpressionMethodReference node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ExpressionStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(FieldAccess node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(FieldDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ForStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(IfStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ImportDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(InfixExpression node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(Initializer node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(InstanceofExpression node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(IntersectionType node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(Javadoc node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(LabeledStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(LambdaExpression node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(LineComment node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(MarkerAnnotation node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(MemberRef node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(MemberValuePair node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(MethodDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(MethodInvocation node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(MethodRef node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(MethodRefParameter node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(Modifier node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ModuleDeclaration node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ModuleModifier node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(NameQualifiedType node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(NormalAnnotation node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(NullLiteral node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(NumberLiteral node, Object other) {
		String oToken = null;

		if (other instanceof PrefixExpression) {
			oToken = ((PrefixExpression) other).toString();
		} else if (other instanceof NumberLiteral) {
			oToken = ((NumberLiteral) other).getToken();
		} else {
			return setDiff(node, other, false);
		}
		String nToken = node.getToken();

		return setDiff(node, other, numberMatch(nToken, oToken));
	}

	private boolean numberMatch(String nToken, String oToken) {
		nToken = normalizeNumberToken(nToken);
		oToken = normalizeNumberToken(oToken);
		boolean equals = safeEquals(nToken, oToken);
		return equals;
	}

	private String normalizeNumberToken(String token) {
		if (token.contains("\\u")) {
			StringBuilder actualLiteral = new StringBuilder();
			for (int index = 0; index < token.length(); index++) {
				char currentChar = token.charAt(index);
				if (currentChar == '\\') {
					int codePoint = Integer.parseInt(token.substring(index + 2, index + 6), 16);
					actualLiteral.append(Character.toString(codePoint));
					index += 5;
				} else {
					actualLiteral.append(currentChar);
				}
			}
			token = actualLiteral.toString();
		}

		token = token.toLowerCase();
		token = token.replaceAll("_", "");

		if (token.startsWith("-0x")) {
			token = token.substring(1);
			return token = "-" + normalizeNumberToken(token);
		}

		if (token.startsWith("- ")) {
			token = "-" + token.substring(2);
		}

		if (token.endsWith("l")) {
			token = token.substring(0, token.length() - 1);
		}
		if (token.matches("0x[0-9,a-f]+")) {
			return new BigInteger(token.substring(2), 16).toString();
		}
		if (token.matches("0[0-9]+")) {
			return new BigInteger(token.substring(0), 8).toString();
		}
		if (token.matches("0b[0-1]+")) {
			return new BigInteger(token.substring(2), 2).toString();
		}

		if (token.endsWith("f")) {
			return "" + Float.parseFloat(token);
		}
		return "" + Double.parseDouble(token);
	}

	@Override
	public boolean match(OpensDirective node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(PackageDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ParameterizedType node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ParenthesizedExpression node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(PostfixExpression node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(PrefixExpression node, Object other) {

		if (other instanceof NumberLiteral) {
			String nToken = node.toString();
			String oToken = ((NumberLiteral) other).getToken();
			return setDiff(node, other, numberMatch(nToken, oToken));
		} else {
			return setDiff(node, other, super.match(node, other));
		}

	}

	@Override
	public boolean match(PrimitiveType node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ProvidesDirective node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(QualifiedName node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(QualifiedType node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(RequiresDirective node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ReturnStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SimpleName node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SimpleType node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SingleMemberAnnotation node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SingleVariableDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(StringLiteral node, Object other) {
		if (!(other instanceof StringLiteral)) {
			return setDiff(node, other, false);
		}
		StringLiteral o = (StringLiteral) other;

		String nString = node.getEscapedValue();
		String oString = o.getEscapedValue();

		// normalize escaped strings
		nString = CharacterEscaper.unescapeEscapedCharacters(nString);
		oString = CharacterEscaper.unescapeEscapedCharacters(oString);

		return setDiff(node, other, safeEquals(nString, oString));
	}

	@Override
	public boolean match(SuperConstructorInvocation node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SuperFieldAccess node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SuperMethodInvocation node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SuperMethodReference node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SwitchCase node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SwitchExpression node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SwitchStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(SynchronizedStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(TagElement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(TextElement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ThisExpression node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(ThrowStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(TryStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(TypeDeclaration node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(TypeDeclarationStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(TypeLiteral node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(TypeMethodReference node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(TypeParameter node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(UnionType node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(UsesDirective node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(VariableDeclarationExpression node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(VariableDeclarationFragment node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(VariableDeclarationStatement node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(WhileStatement node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(WildcardType node, Object other) {

		return setDiff(node, other, super.match(node, other));
	}

	@Override
	public boolean match(YieldStatement node, Object other) {
		return setDiff(node, other, super.match(node, other));
	}

	private String diff = "";

	private boolean setDiff(Object o1, Object o2, boolean result) {
		if (!result) {
			diff += ("\nORIGINAL: \n");
			if (o1 instanceof ASTNode) {
				diff += "(POSITION: " + ((ASTNode) o1).getStartPosition() + ")\n";
			}
			diff += (o1.toString());
			diff += ("\nREPRINT:\n");
			if (o2 instanceof ASTNode) {
				diff += "(POSITION: " + ((ASTNode) o2).getStartPosition() + ")\n";
			}
			diff += (o2.toString());
			diff += ("\n--------\n");
		}
		return result;
	}

	public String getDiff() {
		return diff;
	}

}
