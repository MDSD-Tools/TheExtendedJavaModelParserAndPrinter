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
import tools.mdsd.jamopp.model.java.arrays.ArraySelector;
import tools.mdsd.jamopp.model.java.generics.GenericsPackage;
import tools.mdsd.jamopp.model.java.references.Argumentable;
import tools.mdsd.jamopp.model.java.references.IdentifierReference;
import tools.mdsd.jamopp.model.java.references.MethodCall;
import tools.mdsd.jamopp.model.java.references.PrimitiveTypeReference;
import tools.mdsd.jamopp.model.java.references.Reference;
import tools.mdsd.jamopp.model.java.references.ReflectiveClassReference;
import tools.mdsd.jamopp.model.java.references.SelfReference;
import tools.mdsd.jamopp.model.java.references.StringReference;
import tools.mdsd.jamopp.model.java.references.TextBlockReference;
import tools.mdsd.jamopp.model.java.references.util.ReferencesSwitch;

public class ReferencesPrinterSwitch extends ReferencesSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	ReferencesPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseArgumentable(Argumentable element) {
		try {
			writer.append("(");
			for (int index = 0; index < element.getArguments().size(); index++) {
				parent.doSwitch(element.getArguments().get(index));
				if (index < element.getArguments().size() - 1) {
					writer.append(", ");
				}
			}
			writer.append(")");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseTextBlockReference(TextBlockReference element) {
		try {
			writer.append("\"\"\"\n");
			writer.append(element.getValue());
			writer.append("\n\"\"\"");
			caseReference(element);
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseReflectiveClassReference(ReflectiveClassReference element) {
		try {
			writer.append("class");
			caseReference(element);
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean casePrimitiveTypeReference(PrimitiveTypeReference element) {
		parent.doSwitch(element.getPrimitiveType());
		element.getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
		element.getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
		caseReference(element);
		return true;
	}
	
	@Override
	public Boolean caseStringReference(StringReference element) {
		try {
			writer.append("\"" + element.getValue() + "\"");
			caseReference(element);
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseSelfReference(SelfReference element) {
		parent.doSwitch(element.getSelf());
		caseReference(element);
		return true;
	}

	@Override
	public Boolean caseIdentifierReference(IdentifierReference element) {
		try {
			parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			if (element.getTarget() instanceof tools.mdsd.jamopp.model.java.containers.Package) {
				tools.mdsd.jamopp.model.java.containers.Package pack = (tools.mdsd.jamopp.model.java.containers.Package)
					element.getTarget();
				writer.append(pack.getNamespaces().get(pack.getNamespaces().size() - 1));
			} else {
				writer.append(element.getTarget().getName());
			}
			parent.doSwitch(GenericsPackage.Literals.TYPE_ARGUMENTABLE, element);
			element.getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
			element.getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
			caseReference(element);
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseMethodCall(MethodCall element) {
		try {
			parent.doSwitch(GenericsPackage.Literals.CALL_TYPE_ARGUMENTABLE, element);
			writer.append(element.getTarget().getName());
			caseArgumentable(element);
			caseReference(element);
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseReference(Reference element) {
		try {
			for (ArraySelector sel : element.getArraySelectors()) {
				parent.doSwitch(sel);
			}
			if (element.getNext() != null) {
				writer.append(".");
				parent.doSwitch(element.getNext());
			}
		} catch (IOException e) {
		}
		return true;
	}
}
