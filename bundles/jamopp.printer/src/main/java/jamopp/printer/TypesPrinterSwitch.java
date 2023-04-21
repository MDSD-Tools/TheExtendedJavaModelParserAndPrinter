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

import org.emftext.language.java.LogicalJavaURIGenerator;
import org.emftext.language.java.annotations.AnnotationsPackage;
import org.emftext.language.java.generics.GenericsPackage;
import org.emftext.language.java.types.ClassifierReference;
import org.emftext.language.java.types.InferableType;
import org.emftext.language.java.types.NamespaceClassifierReference;
import org.emftext.language.java.types.PrimitiveType;
import org.emftext.language.java.types.util.TypesSwitch;

class TypesPrinterSwitch extends TypesSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	TypesPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseInferableType(InferableType element) {
		try {
			writer.append("var");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean casePrimitiveType(PrimitiveType element) {
		parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
		return true;
	}
	
	@Override
	public Boolean caseBoolean(org.emftext.language.java.types.Boolean element) {
		try {
			casePrimitiveType(element);
			writer.append("boolean");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseByte(org.emftext.language.java.types.Byte element) {
		try {
			casePrimitiveType(element);
			writer.append("byte");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseChar(org.emftext.language.java.types.Char element) {
		try {
			casePrimitiveType(element);
			writer.append("char");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseDouble(org.emftext.language.java.types.Double element) {
		try {
			casePrimitiveType(element);
			writer.append("double");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseFloat(org.emftext.language.java.types.Float element) {
		try {
			casePrimitiveType(element);
			writer.append("float");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseInt(org.emftext.language.java.types.Int element) {
		try {
			casePrimitiveType(element);
			writer.append("int");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseLong(org.emftext.language.java.types.Long element) {
		try {
			casePrimitiveType(element);
			writer.append("long");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseShort(org.emftext.language.java.types.Short element) {
		try {
			casePrimitiveType(element);
			writer.append("short");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseVoid(org.emftext.language.java.types.Void element) {
		try {
			casePrimitiveType(element);
			writer.append("void");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseNamespaceClassifierReference(NamespaceClassifierReference element) {
		try {
			String n = LogicalJavaURIGenerator.packageName(element);
			if (n.length() > 0) {
				n = n.substring(0, n.length() - 1);
			}
			writer.append(n);
			if (element.getNamespaces().size() > 0) {
				writer.append(LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
			}
			for (int index = 0; index < element.getClassifierReferences().size() - 1; index++) {
				parent.doSwitch(element.getClassifierReferences().get(index));
				writer.append(LogicalJavaURIGenerator.PACKAGE_SEPARATOR);
			}
			parent.doSwitch(element.getClassifierReferences().get(element.getClassifierReferences().size() - 1));
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseClassifierReference(ClassifierReference element) {
		try {
			parent.doSwitch(AnnotationsPackage.Literals.ANNOTABLE, element);
			writer.append(element.getTarget().getName());
			parent.doSwitch(GenericsPackage.Literals.TYPE_ARGUMENTABLE, element);
		} catch (IOException e) {
		}
		return true;
	}
}
