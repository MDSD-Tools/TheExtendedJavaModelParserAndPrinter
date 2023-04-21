/*******************************************************************************
 * Copyright (c) 2019-2020, Martin Armbruster
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

package org.emftext.language.java.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.containers.JavaRoot;
import org.emftext.language.java.containers.Module;
import org.emftext.language.java.expressions.ArrayConstructorReferenceExpression;
import org.emftext.language.java.expressions.AssignmentExpression;
import org.emftext.language.java.expressions.ClassTypeConstructorReferenceExpression;
import org.emftext.language.java.expressions.Expression;
import org.emftext.language.java.expressions.LambdaExpression;
import org.emftext.language.java.expressions.PrimaryExpressionReferenceExpression;
import org.emftext.language.java.instantiations.NewConstructorCall;
import org.emftext.language.java.literals.BinaryIntegerLiteral;
import org.emftext.language.java.literals.BinaryLongLiteral;
import org.emftext.language.java.members.ClassMethod;
import org.emftext.language.java.members.Constructor;
import org.emftext.language.java.members.Member;
import org.emftext.language.java.members.Method;
import org.emftext.language.java.modifiers.Default;
import org.emftext.language.java.modifiers.Modifier;
import org.emftext.language.java.modifiers.Static;
import org.emftext.language.java.parameters.CatchParameter;
import org.emftext.language.java.parameters.ReceiverParameter;
import org.emftext.language.java.references.IdentifierReference;
import org.emftext.language.java.references.MethodCall;
import org.emftext.language.java.statements.Block;
import org.emftext.language.java.statements.DefaultSwitchRule;
import org.emftext.language.java.statements.EmptyStatement;
import org.emftext.language.java.statements.ExpressionStatement;
import org.emftext.language.java.statements.LocalVariableStatement;
import org.emftext.language.java.statements.NormalSwitchCase;
import org.emftext.language.java.statements.NormalSwitchRule;
import org.emftext.language.java.statements.Statement;
import org.emftext.language.java.statements.Switch;
import org.emftext.language.java.statements.SwitchCase;
import org.emftext.language.java.statements.TryBlock;
import org.emftext.language.java.statements.YieldStatement;
import org.emftext.language.java.types.InferableType;
import org.emftext.language.java.types.Type;
import org.emftext.language.java.types.TypeReference;
import org.emftext.language.java.variables.LocalVariable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jamopp.options.ParserOptions;

/**
 * Test class for the features of Java 7+.
 * 
 * @author Martin Armbruster
 */
public class JavaSevenAndUpTest extends AbstractJaMoPPTests {
	@Override
	public String getTestInputFolder() {
		return "src-sevenandup";
	}
	
	@Override
	public boolean isExcludedFromReprintTest(String file) {
		return false;
	}
	
	@Test
	public void testModuleInfo() {
		try {
			this.registerInClassPath("pkg2" + File.separator + "SimpleInterfaceWithDefaultMethods.java");
			String file = "module-info.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, Module.class);
			assertEquals(6, ((Module) root).getTarget().size());
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testPackageInfo() {
		try {
			String file = "simplepackage" + File.separator + "package-info.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, org.emftext.language.java.containers.Package.class);
			org.emftext.language.java.containers.Package pRoot = (org.emftext.language.java.containers.Package) root;
			assertEquals(1, pRoot.getNamespaces().size());
			assertEquals("simplepackage", pRoot.getNamespaces().get(0));
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSimpleClassWithLambdaExpressions() {
		try {
			String file = "pkg2" + File.separator + "SimpleClassWithLambdaExpressions.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit unit = (CompilationUnit) root;
			this.assertNumberOfClassifiers(unit, 1);
			org.emftext.language.java.classifiers.Class classifier = unit.getContainedClass();
			this.assertClassifierName(classifier, "SimpleClassWithLambdaExpressions");
			this.assertMemberCount(classifier, 2);
			for (Member m : classifier.getMembers()) {
				if (m instanceof Interface) {
					assertEquals("I", m.getName());
					assertEquals(1, ((Interface) m).getAnnotationInstances().size());
				} else if (m instanceof Method) {
					assertEquals("lambdas", m.getName());
					this.assertIsPublic((Method) m);
					this.assertType(((Method) m).getTypeReference(),
							org.emftext.language.java.types.Void.class);
					for (Statement s : ((Method) m).getBlock().getStatements()) {
						assertTrue(s instanceof ExpressionStatement
								|| s instanceof LocalVariableStatement);
						if (s instanceof ExpressionStatement) {
							ExpressionStatement castedS = (ExpressionStatement) s;
							this.assertType(castedS.getExpression(), AssignmentExpression.class);
							AssignmentExpression expr = (AssignmentExpression)
									castedS.getExpression();
							this.assertType(expr.getValue(), LambdaExpression.class);
						} else if (s instanceof LocalVariableStatement) {
							LocalVariableStatement castedS = (LocalVariableStatement) s;
							this.assertType(castedS.getVariable().getInitialValue(),
									LambdaExpression.class);
						}
					}
				}
			}
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSimpleClassWithLiterals() {
		try {
			String file = "pkg2" + File.separator + "SimpleClassWithLiterals.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit unit = (CompilationUnit) root;
			this.assertNumberOfClassifiers(unit, 1);
			org.emftext.language.java.classifiers.Class classifier = unit.getContainedClass();
			assertEquals(1, classifier.getMembers().size());
			Method m = (Method) classifier.getMembers().get(0);
			for (Statement s : m.getBlock().getStatements()) {
				assertTrue(s instanceof LocalVariableStatement || s instanceof ExpressionStatement);
				if (s instanceof LocalVariableStatement) {
					LocalVariableStatement castedS = (LocalVariableStatement) s;
					assertTrue(castedS.getVariable().getInitialValue() instanceof BinaryIntegerLiteral
							|| castedS.getVariable().getInitialValue() instanceof BinaryLongLiteral);
				} else if (s instanceof ExpressionStatement) {
					AssignmentExpression expr = ((AssignmentExpression)
							((ExpressionStatement) s).getExpression());
					assertTrue(expr.getValue() instanceof BinaryIntegerLiteral
							|| expr.getValue() instanceof BinaryLongLiteral);
				}
			}
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSimpleClassWithTryCatch() {
		try {
			String file = "pkg2" + File.separator + "SimpleClassWithTryCatch.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit unit = (CompilationUnit) root;
			this.assertNumberOfClassifiers(unit, 1);
			org.emftext.language.java.classifiers.Class classifier = unit.getContainedClass();
			this.assertMemberCount(classifier, 3);
			Method method = (Method) classifier.getMembers().get(1);
			this.assertIsPublic(method);
			assertEquals("tryCatch", method.getName());
			assertEquals(6, method.getBlock().getStatements().size());
			TryBlock tryBlock = (TryBlock) method.getBlock().getStatements().get(0);
			assertEquals(1, tryBlock.getBlock().getStatements().size());
			assertEquals(1, tryBlock.getResources().size());
			assertEquals(1, tryBlock.getCatchBlocks().size());
			assertTrue(tryBlock.getFinallyBlock() != null);
			tryBlock = (TryBlock) method.getBlock().getStatements().get(1);
			assertEquals(1, tryBlock.getBlock().getStatements().size());
			assertEquals(1, tryBlock.getResources().size());
			assertEquals(1, tryBlock.getCatchBlocks().size());
			assertTrue(tryBlock.getFinallyBlock() == null);
			tryBlock = (TryBlock) method.getBlock().getStatements().get(2);
			assertEquals(1, tryBlock.getBlock().getStatements().size());
			assertEquals(2, tryBlock.getResources().size());
			assertEquals(1, tryBlock.getCatchBlocks().size());
			assertTrue(tryBlock.getFinallyBlock() == null);
			tryBlock = (TryBlock) method.getBlock().getStatements().get(3);
			assertEquals(1, tryBlock.getBlock().getStatements().size());
			assertEquals(0, tryBlock.getResources().size());
			assertEquals(1, tryBlock.getCatchBlocks().size());
			this.assertType(tryBlock.getCatchBlocks().get(0).getParameter(), CatchParameter.class);
			CatchParameter catchParam = (CatchParameter) tryBlock.getCatchBlocks().get(0).getParameter();
			assertEquals(3, catchParam.getTypeReferences().size());
			assertTrue(tryBlock.getFinallyBlock() == null);
			tryBlock = (TryBlock) method.getBlock().getStatements().get(4);
			assertEquals(0, tryBlock.getBlock().getStatements().size());
			assertEquals(1, tryBlock.getResources().size());
			assertEquals(0, tryBlock.getCatchBlocks().size());
			assertTrue(tryBlock.getFinallyBlock() == null);
			tryBlock = (TryBlock) method.getBlock().getStatements().get(5);
			assertEquals(0, tryBlock.getBlock().getStatements().size());
			assertEquals(1, tryBlock.getResources().size());
			assertEquals(0, tryBlock.getCatchBlocks().size());
			assertTrue(tryBlock.getFinallyBlock() != null);
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSimpleInterfaceWithDefaultMethods() {
		try {
			String file = "pkg2" + File.separator + "SimpleInterfaceWithDefaultMethods.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit unit = (CompilationUnit) root;
			this.assertNumberOfClassifiers(unit, 1);
			Interface classifier = unit.getContainedInterface();
			this.assertMemberCount(classifier, 5);
			int numberOfDefaultOrStaticMethods = 0;
			for (Member member : classifier.getMembers()) {
				if (member instanceof Method) {
					Method method = (Method) member;
					boolean hasStaticOrDefaultModifier = false;
					for (Modifier modifier : method.getModifiers()) {
						if (modifier instanceof Static || modifier instanceof Default) {
							hasStaticOrDefaultModifier = true;
							numberOfDefaultOrStaticMethods++;
						}
					}
					if (hasStaticOrDefaultModifier) {
						this.assertType(method.getStatement(), Block.class);
					} else {
						this.assertType(method.getStatement(), EmptyStatement.class);
					}
				}
			}
			assertEquals(3, numberOfDefaultOrStaticMethods);
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSimpleClassWithDiamondTypeArguments() {
		try {
			String file = "simplepackage" + File.separator + "SimpleClassWithDiamondTypeArguments.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit unit = (CompilationUnit) root;
			this.assertNumberOfClassifiers(unit, 1);
			org.emftext.language.java.classifiers.Class classifier = unit.getContainedClass();
			this.assertMemberCount(classifier, 2);
			Method method = (Method) classifier.getMembers().get(0);
			assertEquals(6, method.getBlock().getStatements().size());
			for (Statement s : method.getBlock().getStatements()) {
				assertTrue(s instanceof LocalVariableStatement || s instanceof ExpressionStatement);
				if (s instanceof LocalVariableStatement) {
					LocalVariableStatement castedS = (LocalVariableStatement) s;
					this.assertType(castedS.getVariable().getInitialValue(), NewConstructorCall.class);
				} else {
					this.assertType(((ExpressionStatement) s).getExpression(), NewConstructorCall.class);
				}
			}
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSimpleClassWithMethodReferenceExpressions() {
		try {
			String file = "simplepackage" + File.separator + "SimpleClassWithMethodReferenceExpressions.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit unit = (CompilationUnit) root;
			this.assertNumberOfClassifiers(unit, 1);
			org.emftext.language.java.classifiers.Class classifier = unit.getContainedClass();
			this.assertMemberCount(classifier, 2);
			Method method = (Method) classifier.getMembers().get(0);
			assertEquals(8, method.getBlock().getStatements().size());
			LocalVariableStatement locStat = (LocalVariableStatement) method.getBlock().getStatements().get(0);
			this.assertType(locStat.getVariable().getInitialValue(), PrimaryExpressionReferenceExpression.class);
			locStat = (LocalVariableStatement) method.getBlock().getStatements().get(1);
			this.assertType(locStat.getVariable().getInitialValue(), ClassTypeConstructorReferenceExpression.class);
			AssignmentExpression expr = (AssignmentExpression)
					((ExpressionStatement) method.getBlock().getStatements().get(2)).getExpression();
			this.assertType(expr.getValue(), ClassTypeConstructorReferenceExpression.class);
			expr = (AssignmentExpression) ((ExpressionStatement)
					method.getBlock().getStatements().get(3)).getExpression();
			this.assertType(expr.getValue(), ClassTypeConstructorReferenceExpression.class);
			expr = (AssignmentExpression) ((ExpressionStatement)
					method.getBlock().getStatements().get(4)).getExpression();
			this.assertType(expr.getValue(), PrimaryExpressionReferenceExpression.class);
			locStat = (LocalVariableStatement) method.getBlock().getStatements().get(5);
			this.assertType(locStat.getVariable().getInitialValue(), PrimaryExpressionReferenceExpression.class);
			locStat = (LocalVariableStatement) method.getBlock().getStatements().get(6);
			this.assertType(locStat.getVariable().getInitialValue(), ArrayConstructorReferenceExpression.class);
			expr = (AssignmentExpression) ((ExpressionStatement)
					method.getBlock().getStatements().get(7)).getExpression();
			this.assertType(expr.getValue(), ArrayConstructorReferenceExpression.class);
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSimpleClassWithReceiverParameters() {
		try {
			String file = "simplepackage" + File.separator + "SimpleClassWithReceiverParameters.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit unit = (CompilationUnit) root;
			this.assertNumberOfClassifiers(unit, 1);
			org.emftext.language.java.classifiers.Class classifier = unit.getContainedClass();
			this.assertMemberCount(classifier, 7);
			for (Member member : classifier.getMembers()) {
				if (member instanceof Constructor) {
					assertTrue(0 == ((Constructor) member).getParameters().size()
							|| 1 == ((Constructor) member).getParameters().size());
				} else if (member instanceof Method) {
					Method method = (Method) member;
					this.assertType(method.getParameters().get(0), ReceiverParameter.class);
					for (int i = 1; i < method.getParameters().size(); i++) {
						assertFalse(method.getParameters().get(i) instanceof ReceiverParameter);
					}
				} else if (member instanceof org.emftext.language.java.classifiers.Class) {
					org.emftext.language.java.classifiers.Class innerClass =
							(org.emftext.language.java.classifiers.Class) member;
					this.assertMemberCount(innerClass, 3);
					for (Member innerMember : innerClass.getMembers()) {
						if (innerMember instanceof Constructor) {
							this.assertType(((Constructor) innerMember)
									.getParameters().get(0), ReceiverParameter.class);
						} else if (innerMember instanceof Method) {
							this.assertType(((Method) innerMember)
									.getParameters().get(0), ReceiverParameter.class);
						} else {
							fail("There should be no other member.");
						}
					}
				} else {
					fail("There should be no other member.");
				}
			}
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSimpleClassWithVar() {
		try {
			String file = "simplepackage" + File.separator + "SimpleClassWithVar.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit unit = (CompilationUnit) root;
			this.assertNumberOfClassifiers(unit, 1);
			org.emftext.language.java.classifiers.Class classifier = unit.getContainedClass();
			this.assertMemberCount(classifier, 6);
			for (Member member : classifier.getMembers()) {
				if (member instanceof Method) {
					Method method = (Method) member;
					assertEquals(8, method.getBlock().getStatements().size());
					LocalVariableStatement locStat = (LocalVariableStatement)
							method.getBlock().getStatements().get(0);
					this.assertType(locStat.getVariable().getTypeReference(), InferableType.class);
				}
			}
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSimpleClassWithRestrictedKeywords() {
		try {
			String file = "simplepackage" + File.separator + "SimpleClassWithRestrictedKeywords.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testIntersectionTypeWithTypeArguments() {
		try {
			String file = "pkgJava14" + File.separator + "IntersectionTypeWithTypeArguments.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit cu = (CompilationUnit) root;
			Member m = cu.getClassifiers().get(0).getMembers().get(0);
			this.assertType(m, ClassMethod.class);
			ClassMethod method = (ClassMethod) m;
			LocalVariableStatement locStat = (LocalVariableStatement) method.getBlock().getStatements().get(0);
			TypeReference typeRef = locStat.getVariable().getTypeReference();
			this.assertType(typeRef, InferableType.class);
			InferableType inferType = (InferableType) typeRef;
			int expectedTypes = 0;
			if (ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.isTrue()) {
				expectedTypes = 1;
			}
			assertEquals(expectedTypes, inferType.getActualTargets().size());
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Disabled("JDT puts break statements into Blocks on the right side of a SwitchRule.")
	@Test
	public void testSimpleClassWithSwitch() {
		try {
			String file = "pkgJava14" + File.separator + "SimpleClassWithSwitch.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit cu = (CompilationUnit) root;
			Member m = cu.getClassifiers().get(0).getMembers().get(1);
			this.assertType(m, ClassMethod.class);
			ClassMethod method = (ClassMethod) m;
			assertEquals(7, method.getBlock().getStatements().size());
			LocalVariableStatement locStat = (LocalVariableStatement) method.getBlock().getStatements().get(5);
			Expression e = locStat.getVariable().getInitialValue();
			this.assertType(e, Switch.class);
			Switch swith = (Switch) e;
			SwitchCase ca = swith.getCases().get(0);
			this.assertType(ca, NormalSwitchCase.class);
			NormalSwitchCase nca = (NormalSwitchCase) ca;
			assertNotNull(nca.getCondition());
			assertEquals(2, nca.getAdditionalConditions().size());
			this.assertType(nca.getStatements().get(0), YieldStatement.class);
			swith = (Switch) method.getBlock().getStatements().get(1);
			this.assertType(swith.getCases().get(0), NormalSwitchRule.class);
			this.assertType(swith.getCases().get(2), DefaultSwitchRule.class);
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testClassWithReferences() {
		try {
			String file = "pkgJava14" + File.separator + "ClassWithReferences.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testClassWithReferences2() {
		try {
			String file = "pkgJava14" + File.separator + "ClassWithReferences2.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testClassWithReferences3() {
		try {
			String file = "pkgJava14" + File.separator + "ClassWithReferences3.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testClassWithMoreReferences() {
		try {
			String file = "pkgJava14" + File.separator + "ClassWithMoreReferences.java";
			JavaRoot root = this.parseResource(file);
			this.assertType(root, CompilationUnit.class);
			CompilationUnit cu = (CompilationUnit) root;
			ClassMethod cm = (ClassMethod) cu.getClassifiers().get(0).getMembers().get(5);
			assertEquals("m", cm.getName());
			this.assertType(cm.getStatement(), Block.class);
			Block cmBlock = (Block) cm.getStatement();
			this.assertType(cmBlock.getStatements().get(0), LocalVariableStatement.class);
			LocalVariableStatement locStat = (LocalVariableStatement) cmBlock.getStatements().get(0);
			TypeReference ref = locStat.getVariable().getTypeReference();
			this.assertType(ref, InferableType.class);
			Type type = ref.getTarget();
			this.assertType(type, Interface.class);
			Interface inter = (Interface) type;
			assertEquals("Runnable", inter.getName());
			this.assertType(cmBlock.getStatements().get(
					cmBlock.getStatements().size() - 1), ExpressionStatement.class);
			ExpressionStatement exprStat = (ExpressionStatement)
					cmBlock.getStatements().get(cmBlock.getStatements().size() - 1);
			this.assertType(exprStat.getExpression(), IdentifierReference.class);
			IdentifierReference idRef = (IdentifierReference) exprStat.getExpression();
			assertEquals("a", idRef.getTarget().getName());
			this.assertType(idRef.getTarget(), LocalVariable.class);
			LocalVariable var = (LocalVariable) idRef.getTarget();
			type = var.getTypeReference().getTarget();
			this.assertType(type, org.emftext.language.java.classifiers.Class.class);
			org.emftext.language.java.classifiers.Class cl = (org.emftext.language.java.classifiers.Class) type;
			assertEquals("String", cl.getName());
			this.assertType(idRef.getNext(), MethodCall.class);
			MethodCall call = (MethodCall) idRef.getNext();
			assertEquals("charAt", call.getTarget().getName());
			assertEquals(1, call.getArguments().size());
			this.assertResolveAllProxies(root);
			this.parseAndReprint(file);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
