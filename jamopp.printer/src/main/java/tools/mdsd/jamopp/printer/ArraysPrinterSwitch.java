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

package tools.mdsd.jamopp.printer;

import java.io.BufferedWriter;
import java.io.IOException;

import tools.mdsd.jamopp.model.java.annotations.AnnotationsPackage;
import tools.mdsd.jamopp.model.java.arrays.ArrayDimension;
import tools.mdsd.jamopp.model.java.arrays.ArrayInitializationValue;
import tools.mdsd.jamopp.model.java.arrays.ArrayInitializer;
import tools.mdsd.jamopp.model.java.arrays.ArrayInstantiationBySize;
import tools.mdsd.jamopp.model.java.arrays.ArrayInstantiationByValuesTyped;
import tools.mdsd.jamopp.model.java.arrays.ArrayInstantiationByValuesUntyped;
import tools.mdsd.jamopp.model.java.arrays.ArraySelector;
import tools.mdsd.jamopp.model.java.arrays.util.ArraysSwitch;
import tools.mdsd.jamopp.model.java.expressions.Expression;
import tools.mdsd.jamopp.model.java.generics.GenericsPackage;
import tools.mdsd.jamopp.model.java.references.ReferencesPackage;

class ArraysPrinterSwitch extends ArraysSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	ArraysPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseArrayDimension(ArrayDimension element) {
		try {
			if (element.getAnnotations().size() > 0) {
				writer.append(" ");
				parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			}
			writer.append("[] ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseArraySelector(ArraySelector element) {
		try {
			parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			writer.append("[");
			parent.doSwitch(element.getPosition());
			writer.append("]");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseArrayInstantiationBySize(ArrayInstantiationBySize element) {
		try {
			writer.append("new ");
			parent.doSwitch(element.getTypeReference());
			parent.doSwitch(GenericsPackage.Literals.TYPE_ARGUMENTABLE, element);
			writer.append(" ");
			for (Expression expr : element.getSizes()) {
				writer.append("[");
				parent.doSwitch(expr);
				writer.append("] ");
			}
			element.getTypeReference().getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			parent.doSwitch(ReferencesPackage.Literals.REFERENCE, element);
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseArrayInstantiationByValuesUntyped(ArrayInstantiationByValuesUntyped element) {
		caseArrayInitializer(element.getArrayInitializer());
		parent.doSwitch(ReferencesPackage.Literals.REFERENCE, element);
		return true;
	}
	
	@Override
	public Boolean caseArrayInstantiationByValuesTyped(ArrayInstantiationByValuesTyped element) {
		try {
			writer.append("new ");
			parent.doSwitch(element.getTypeReference());
			parent.doSwitch(GenericsPackage.Literals.TYPE_ARGUMENTABLE, element);
			element.getTypeReference().getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			writer.append(" ");
			caseArrayInitializer(element.getArrayInitializer());
			parent.doSwitch(ReferencesPackage.Literals.REFERENCE, element);
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseArrayInitializer(ArrayInitializer element) {
		try {
			writer.append("{");
			for (int index = 0; index < element.getInitialValues().size(); index++) {
				ArrayInitializationValue val = element.getInitialValues().get(index);
				parent.doSwitch(val);
				if (index < element.getInitialValues().size() - 1) {
					writer.append(", ");
				}
			}
			writer.append("}");
		} catch (IOException e) {
		}
		return true;
	}
}
