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

import org.emftext.language.java.classifiers.Annotation;
import org.emftext.language.java.classifiers.AnonymousClass;
import org.emftext.language.java.classifiers.Enumeration;
import org.emftext.language.java.classifiers.Implementor;
import org.emftext.language.java.classifiers.Interface;
import org.emftext.language.java.classifiers.util.ClassifiersSwitch;
import org.emftext.language.java.generics.GenericsPackage;
import org.emftext.language.java.members.EnumConstant;
import org.emftext.language.java.members.MembersPackage;
import org.emftext.language.java.modifiers.ModifiersPackage;

class ClassifiersPrinterSwitch extends ClassifiersSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	ClassifiersPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}

	@Override
	public Boolean caseClass(org.emftext.language.java.classifiers.Class element) {
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
