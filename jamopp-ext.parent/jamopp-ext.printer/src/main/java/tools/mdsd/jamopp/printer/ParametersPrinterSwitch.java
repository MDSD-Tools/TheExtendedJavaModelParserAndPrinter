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
import tools.mdsd.jamopp.model.java.generics.GenericsPackage;
import tools.mdsd.jamopp.model.java.modifiers.ModifiersPackage;
import tools.mdsd.jamopp.model.java.parameters.CatchParameter;
import tools.mdsd.jamopp.model.java.parameters.OrdinaryParameter;
import tools.mdsd.jamopp.model.java.parameters.Parameter;
import tools.mdsd.jamopp.model.java.parameters.Parametrizable;
import tools.mdsd.jamopp.model.java.parameters.ReceiverParameter;
import tools.mdsd.jamopp.model.java.parameters.VariableLengthParameter;
import tools.mdsd.jamopp.model.java.parameters.util.ParametersSwitch;
import tools.mdsd.jamopp.model.java.types.TypeReference;

class ParametersPrinterSwitch extends ParametersSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	ParametersPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseParametrizable(Parametrizable element) {
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
	public Boolean caseReceiverParameter(ReceiverParameter element) {
		try {
			parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			parent.doSwitch(element.getTypeReference());
			parent.doSwitch(GenericsPackage.Literals.TYPE_ARGUMENTABLE, element);
			writer.append(" ");
			if (element.getOuterTypeReference() != null) {
				parent.doSwitch(element.getOuterTypeReference());
				writer.append(".");
			}
			writer.append("this");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseOrdinaryParameter(OrdinaryParameter element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			parent.doSwitch(element.getTypeReference());
			parent.doSwitch(GenericsPackage.Literals.TYPE_ARGUMENTABLE, element);
			element.getTypeReference().getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			writer.append(" " + element.getName());
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseVariableLengthParameter(VariableLengthParameter element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			parent.doSwitch(element.getTypeReference());
			parent.doSwitch(GenericsPackage.Literals.TYPE_ARGUMENTABLE, element);
			element.getTypeReference().getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			element.getTypeReference().getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			writer.append(" ");
			parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			writer.append(" ..." + element.getName());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseCatchParameter(CatchParameter element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			parent.doSwitch(element.getTypeReference());
			if (element.getTypeReferences().size() > 0) {
				for (TypeReference ref : element.getTypeReferences()) {
					writer.append(" | ");
					parent.doSwitch(ref);
				}
			}
			writer.append(" " + element.getName());
		} catch (IOException e) {
		}
		return true;
	}
}
