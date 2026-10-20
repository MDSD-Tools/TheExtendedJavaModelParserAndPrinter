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

import tools.mdsd.jamopp.model.java.classifiers.Annotation;
import tools.mdsd.jamopp.model.java.classifiers.AnonymousClass;
import tools.mdsd.jamopp.model.java.classifiers.Enumeration;
import tools.mdsd.jamopp.model.java.classifiers.Implementor;
import tools.mdsd.jamopp.model.java.classifiers.Interface;
import tools.mdsd.jamopp.model.java.classifiers.util.ClassifiersSwitch;
import tools.mdsd.jamopp.model.java.generics.GenericsPackage;
import tools.mdsd.jamopp.model.java.members.EnumConstant;
import tools.mdsd.jamopp.model.java.members.MembersPackage;
import tools.mdsd.jamopp.model.java.modifiers.ModifiersPackage;

class ClassifiersPrinterSwitch extends ClassifiersSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	ClassifiersPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}

	@Override
	public Boolean caseClass(tools.mdsd.jamopp.model.java.classifiers.Class element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			writer.append("class " + element.getName());
			parent.doSwitch(GenericsPackage.Literals.TYPE_PARAMETRIZABLE, element);
			writer.append(" ");
			if (element.getExtends() != null) {
				writer.append("extends ");
				parent.doSwitch(element.getExtends());
				writer.append(" ");
			}
			caseImplementor(element);
			writer.append("{\n");
			parent.doSwitch(MembersPackage.Literals.MEMBER_CONTAINER, element);
			writer.append("}\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAnonymousClass(AnonymousClass element) {
		try {
			writer.append("{\n");
			parent.doSwitch(MembersPackage.Literals.MEMBER_CONTAINER, element);
			writer.append("}\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseImplementor(Implementor element) {
		try {
			if (element.getImplements().size() > 0) {
				writer.append("implements ");
				parent.doSwitch(element.getImplements().get(0));
				for (int index = 1; index < element.getImplements().size(); index++) {
					writer.append(", ");
					parent.doSwitch(element.getImplements().get(index));
				}
				writer.append(" ");
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseEnumeration(Enumeration element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			writer.append("enum " + element.getName() + " ");
			caseImplementor(element);
			writer.append("{\n");
			for (EnumConstant enc : element.getConstants()) {
				parent.doSwitch(enc);
				writer.append(",\n");
			}
			if (element.getMembers().size() > 0) {
				writer.append(";\n\n");
				parent.doSwitch(MembersPackage.Literals.MEMBER_CONTAINER, element);
			}
			writer.append("}\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseInterface(Interface element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			writer.append("interface " + element.getName());
			parent.doSwitch(GenericsPackage.Literals.TYPE_PARAMETRIZABLE, element);
			writer.append(" ");
			if (element.getExtends().size() > 0) {
				writer.append("extends ");
				parent.doSwitch(element.getExtends().get(0));
				for (int index = 1; index < element.getExtends().size(); index++) {
					writer.append(", ");
					parent.doSwitch(element.getExtends().get(index));
				}
				writer.append(" ");
			}
			writer.append("{\n");
			parent.doSwitch(MembersPackage.Literals.MEMBER_CONTAINER, element);
			writer.append("}\n");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAnnotation(Annotation element) {
		try {
			parent.doSwitch(ModifiersPackage.Literals.ANNOTABLE_AND_MODIFIABLE, element);
			writer.append("@interface " + element.getName() + " {\n");
			parent.doSwitch(MembersPackage.Literals.MEMBER_CONTAINER, element);
			writer.append("}\n");
		} catch (IOException e) {
		}
		return true;
	}
}
