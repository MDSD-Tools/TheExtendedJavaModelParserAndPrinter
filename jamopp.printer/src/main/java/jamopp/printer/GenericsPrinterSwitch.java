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
import org.emftext.language.java.generics.CallTypeArgumentable;
import org.emftext.language.java.generics.ExtendsTypeArgument;
import org.emftext.language.java.generics.QualifiedTypeArgument;
import org.emftext.language.java.generics.SuperTypeArgument;
import org.emftext.language.java.generics.TypeArgument;
import org.emftext.language.java.generics.TypeArgumentable;
import org.emftext.language.java.generics.TypeParameter;
import org.emftext.language.java.generics.TypeParametrizable;
import org.emftext.language.java.generics.UnknownTypeArgument;
import org.emftext.language.java.generics.util.GenericsSwitch;

class GenericsPrinterSwitch extends GenericsSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	GenericsPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseTypeArgumentable(TypeArgumentable element) {
		try {
			if (element.getTypeArguments().size() > 0) {
				writer.append("<");
				parent.doSwitch(element.getTypeArguments().get(0));
				for (int index = 1; index < element.getTypeArguments().size(); index++) {
					writer.append(", ");
					parent.doSwitch(element.getTypeArguments().get(index));
				}
				writer.append(">");
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseQualifiedTypeArgument(QualifiedTypeArgument element) {
		parent.doSwitch(element.getTypeReference());
		return null;
	}
	
	@Override
	public Boolean caseUnknownTypeArgument(UnknownTypeArgument element) {
		try {
			parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			writer.append("?");
		} catch (IOException e) {
		}
		return null;
	}
	
	@Override
	public Boolean caseSuperTypeArgument(SuperTypeArgument element) {
		try {
			parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			writer.append("? super ");
			parent.doSwitch(element.getSuperType());
		} catch (IOException e) {
		}
		return null;
	}
	
	@Override
	public Boolean caseExtendsTypeArgument(ExtendsTypeArgument element) {
		try {
			parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			writer.append("? extends ");
			parent.doSwitch(element.getExtendType());
		} catch (IOException e) {
		}
		return null;
	}
	
	@Override
	public Boolean caseTypeArgument(TypeArgument element) {
		element.getArrayDimensionsBefore().forEach(dim -> parent.doSwitch(dim));
		element.getArrayDimensionsAfter().forEach(dim -> parent.doSwitch(dim));
		return true;
	}
	
	@Override
	public Boolean caseCallTypeArgumentable(CallTypeArgumentable element) {
		try {
			if (element.getCallTypeArguments().size() > 0) {
				writer.append("<");
				parent.doSwitch(element.getCallTypeArguments().get(0));
				for (int index = 1; index < element.getCallTypeArguments().size(); index++) {
					writer.append(", ");
					parent.doSwitch(element.getCallTypeArguments().get(index));
				}
				writer.append(">");
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseTypeParametrizable(TypeParametrizable element) {
		try {
			if (element.getTypeParameters().size() > 0) {
				writer.append("<");
				caseTypeParameter(element.getTypeParameters().get(0));
				for (int index = 1; index < element.getTypeParameters().size(); index++) {
					writer.append(", ");
					caseTypeParameter(element.getTypeParameters().get(index));
				}
				writer.append("> ");
			}
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseTypeParameter(TypeParameter element) {
		try {
			parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			writer.append(element.getName());
			if (element.getExtendTypes().size() > 0) {
				writer.append(" extends ");
				parent.doSwitch(element.getExtendTypes().get(0));
				for (int index = 1; index < element.getExtendTypes().size(); index++) {
					writer.append(" & ");
					parent.doSwitch(element.getExtendTypes().get(index));
				}
			}
		} catch (IOException e) {
		}
		return true;
	}
}
