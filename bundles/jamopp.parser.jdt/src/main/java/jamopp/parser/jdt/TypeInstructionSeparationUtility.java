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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

public class TypeInstructionSeparationUtility {
	private static HashMap<Block, org.emftext.language.java.members.Method> methods = new HashMap<>();
	private static HashMap<Block, org.emftext.language.java.members.Constructor> constructors = new HashMap<>();
	private static HashMap<Expression, org.emftext.language.java.members.Field> fields = new HashMap<>();
	private static HashMap<Expression, org.emftext.language.java.members.AdditionalField> addFields = new HashMap<>();
	private static HashMap<Block, org.emftext.language.java.statements.Block> initializers = new HashMap<>();
	private static HashMap<Expression, org.emftext.language.java.members.InterfaceMethod> annotationMethods = new HashMap<>();
	private static HashMap<Expression, org.emftext.language.java.annotations.SingleAnnotationParameter> singleAnnotations = new HashMap<>();
	private static HashMap<Expression, org.emftext.language.java.annotations.AnnotationAttributeSetting> annotationSetting = new HashMap<>();
	private static HashSet<EObject> visitedObjects = new HashSet<>();
	
	static void addMethod(Block block, org.emftext.language.java.members.Method method) {
		methods.put(block, method);
	}
	
	static void addConstructor(Block block, org.emftext.language.java.members.Constructor constructor) {
		constructors.put(block, constructor);
	}
	
	static void addField(Expression initializer, org.emftext.language.java.members.Field field) {
		fields.put(initializer, field);
	}
	
	static void addAdditionalField(Expression initializer, org.emftext.language.java.members.AdditionalField field) {
		addFields.put(initializer, field);
	}
	
	static void addInitializer(Block block, org.emftext.language.java.statements.Block correspondingBlock) {
		initializers.put(block, correspondingBlock);
	}
	
	static void addAnnotationMethod(Expression value, org.emftext.language.java.members.InterfaceMethod method) {
		annotationMethods.put(value, method);
	}
	
	static void addSingleAnnotationParameter(Expression value, org.emftext.language.java.annotations.SingleAnnotationParameter param) {
		singleAnnotations.put(value, param);
	}
	
	static void addAnnotationAttributeSetting(Expression value, org.emftext.language.java.annotations.AnnotationAttributeSetting setting) {
		annotationSetting.put(value, setting);
	}
	
	@SuppressWarnings("unchecked")
	static void convertAll() {
		int oldSize;
		int newSize = methods.size() + constructors.size() + fields.size() + addFields.size() + initializers.size() + annotationMethods.size()
			+ singleAnnotations.size() + annotationSetting.size();
		do {
			oldSize = newSize;
			HashMap<Block, org.emftext.language.java.members.Method> clonedMethods =
				(HashMap<Block, org.emftext.language.java.members.Method>) methods.clone();
			Iterator<Block> iter = clonedMethods.keySet().iterator();
			while (iter.hasNext()) {
				if (visitedObjects.contains(clonedMethods.get(iter.next()))) {
					iter.remove();
				}
			}
			clonedMethods.forEach((b, m) -> {
				visitedObjects.add(m);
				m.setStatement(StatementConverterUtility.convertToBlock(b));
			});
			HashMap<Block, org.emftext.language.java.members.Constructor> clonedConstructors =
				(HashMap<Block, org.emftext.language.java.members.Constructor>) constructors.clone();
			iter = clonedConstructors.keySet().iterator();
			while (iter.hasNext()) {
				if (visitedObjects.contains(clonedConstructors.get(iter.next()))) {
					iter.remove();
				}
			}
			clonedConstructors.forEach((b, c) -> {
				visitedObjects.add(c);
				c.setBlock(StatementConverterUtility.convertToBlock(b));
			});
			HashMap<Expression, org.emftext.language.java.members.Field> clonedFields =
				(HashMap<Expression, org.emftext.language.java.members.Field>) fields.clone();
			Iterator<Expression> exprIter = clonedFields.keySet().iterator();
			while (exprIter.hasNext()) {
				if (visitedObjects.contains(clonedFields.get(exprIter.next()))) {
					exprIter.remove();
				}
			}
			clonedFields.forEach((expr, f) -> {
				visitedObjects.add(f);
				f.setInitialValue(ExpressionConverterUtility.convertToExpression(expr));
			});
			HashMap<Expression, org.emftext.language.java.members.AdditionalField> clonedAddFields =
				(HashMap<Expression, org.emftext.language.java.members.AdditionalField>) addFields.clone();
			exprIter = clonedAddFields.keySet().iterator();
			while (exprIter.hasNext()) {
				if (visitedObjects.contains(clonedAddFields.get(exprIter.next()))) {
					exprIter.remove();
				}
			}
			clonedAddFields.forEach((expr, f) -> {
				visitedObjects.add(f);
				f.setInitialValue(ExpressionConverterUtility.convertToExpression(expr));
			});
			HashMap<Block, org.emftext.language.java.statements.Block> clonedInitializers =
				(HashMap<Block, org.emftext.language.java.statements.Block>) initializers.clone();
			iter = clonedInitializers.keySet().iterator();
			while (iter.hasNext()) {
				if (visitedObjects.contains(clonedInitializers.get(iter.next()))) {
					iter.remove();
				}
			}
			clonedInitializers.forEach((b1, b2) -> {
				visitedObjects.add(b2);
				JDTResolverUtility.prepareNextUid();
				b1.statements().forEach(obj -> b2.getStatements().add(
					StatementConverterUtility.convertToStatement((Statement) obj)));
			});
			HashMap<Expression, org.emftext.language.java.members.InterfaceMethod> clonedAnnotationMethods
				= (HashMap<Expression, org.emftext.language.java.members.InterfaceMethod>) annotationMethods.clone();
			exprIter = clonedAnnotationMethods.keySet().iterator();
			while (exprIter.hasNext()) {
				if (visitedObjects.contains(clonedAnnotationMethods.get(exprIter.next()))) {
					exprIter.remove();
				}
			}
			clonedAnnotationMethods.forEach((expr, m) -> {
				visitedObjects.add(m);
				m.setDefaultValue(AnnotationInstanceOrModifierConverterUtility.convertToAnnotationValue(expr));
			});
			HashMap<Expression, org.emftext.language.java.annotations.SingleAnnotationParameter> clonedSingleAnnotations =
				(HashMap<Expression, org.emftext.language.java.annotations.SingleAnnotationParameter>) singleAnnotations.clone();
			exprIter = clonedSingleAnnotations.keySet().iterator();
			while (exprIter.hasNext()) {
				if (visitedObjects.contains(clonedSingleAnnotations.get(exprIter.next()))) {
					exprIter.remove();
				}
			}
			clonedSingleAnnotations.forEach((expr, sap) -> {
				visitedObjects.add(sap);
				sap.setValue(AnnotationInstanceOrModifierConverterUtility.convertToAnnotationValue(expr));
			});
			HashMap<Expression, org.emftext.language.java.annotations.AnnotationAttributeSetting> clonedAnnotationSetting =
				(HashMap<Expression, org.emftext.language.java.annotations.AnnotationAttributeSetting>) annotationSetting.clone();
			exprIter = clonedAnnotationSetting.keySet().iterator();
			while (exprIter.hasNext()) {
				if (visitedObjects.contains(clonedAnnotationSetting.get(exprIter.next()))) {
					exprIter.remove();
				}
			}
			clonedAnnotationSetting.forEach((expr, aas) -> {
				visitedObjects.add(aas);
				aas.setValue(AnnotationInstanceOrModifierConverterUtility.convertToAnnotationValue(expr));
			});
			newSize = methods.size() + constructors.size() + fields.size() + addFields.size() + initializers.size()
				+ annotationMethods.size() + singleAnnotations.size() + annotationSetting.size();
		} while (newSize != oldSize);
		
		methods.clear();
		constructors.clear();
		fields.clear();
		addFields.clear();
		initializers.clear();
		annotationMethods.clear();
		singleAnnotations.clear();
		annotationSetting.clear();
		visitedObjects.clear();
	}
}
