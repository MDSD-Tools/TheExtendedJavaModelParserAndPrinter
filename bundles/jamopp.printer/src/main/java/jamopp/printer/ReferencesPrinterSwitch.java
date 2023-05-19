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

import org.emftext.language.java.annotations.AnnotationsPackage;
import org.emftext.language.java.arrays.ArraySelector;
import org.emftext.language.java.generics.GenericsPackage;
import org.emftext.language.java.references.Argumentable;
import org.emftext.language.java.references.IdentifierReference;
import org.emftext.language.java.references.MethodCall;
import org.emftext.language.java.references.PrimitiveTypeReference;
import org.emftext.language.java.references.Reference;
import org.emftext.language.java.references.ReflectiveClassReference;
import org.emftext.language.java.references.SelfReference;
import org.emftext.language.java.references.StringReference;
import org.emftext.language.java.references.TextBlockReference;
import org.emftext.language.java.references.util.ReferencesSwitch;

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
			if (element.getTarget() instanceof org.emftext.language.java.containers.Package) {
				org.emftext.language.java.containers.Package pack = (org.emftext.language.java.containers.Package)
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
