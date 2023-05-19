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

import org.eclipse.emf.ecore.util.Switch;
import org.emftext.language.java.modifiers.Abstract;
import org.emftext.language.java.modifiers.AnnotableAndModifiable;
import org.emftext.language.java.modifiers.AnnotationInstanceOrModifier;
import org.emftext.language.java.modifiers.Default;
import org.emftext.language.java.modifiers.Final;
import org.emftext.language.java.modifiers.Native;
import org.emftext.language.java.modifiers.Private;
import org.emftext.language.java.modifiers.Protected;
import org.emftext.language.java.modifiers.Public;
import org.emftext.language.java.modifiers.Static;
import org.emftext.language.java.modifiers.Strictfp;
import org.emftext.language.java.modifiers.Synchronized;
import org.emftext.language.java.modifiers.Transient;
import org.emftext.language.java.modifiers.Volatile;
import org.emftext.language.java.modifiers.util.ModifiersSwitch;

public class ModifiersPrinterSwitch extends ModifiersSwitch<Boolean> {
	private Switch<Boolean> parent;
	private BufferedWriter writer;
	
	ModifiersPrinterSwitch(Switch<Boolean> parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseAbstract(Abstract element) {
		try {
			writer.append("abstract ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseFinal(Final element) {
		try {
			writer.append("final ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseNative(Native element) {
		try {
			writer.append("native ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseProtected(Protected element) {
		try {
			writer.append("protected ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean casePrivate(Private element) {
		try {
			writer.append("private ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean casePublic(Public element) {
		try {
			writer.append("public ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseStatic(Static element) {
		try {
			writer.append("static ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseStrictfp(Strictfp element) {
		try {
			writer.append("strictfp ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseSynchronized(Synchronized element) {
		try {
			writer.append("synchronized ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseTransient(Transient element) {
		try {
			writer.append("transient ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseVolatile(Volatile element) {
		try {
			writer.append("volatile ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseDefault(Default element) {
		try {
			writer.append("default ");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseAnnotableAndModifiable(AnnotableAndModifiable element) {
		for (AnnotationInstanceOrModifier el : element.getAnnotationsAndModifiers()) {
			parent.doSwitch(el);
		}
		return true;
	}
}
