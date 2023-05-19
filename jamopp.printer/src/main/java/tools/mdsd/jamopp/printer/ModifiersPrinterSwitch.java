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

import org.eclipse.emf.ecore.util.Switch;

import tools.mdsd.jamopp.model.java.modifiers.Abstract;
import tools.mdsd.jamopp.model.java.modifiers.AnnotableAndModifiable;
import tools.mdsd.jamopp.model.java.modifiers.AnnotationInstanceOrModifier;
import tools.mdsd.jamopp.model.java.modifiers.Default;
import tools.mdsd.jamopp.model.java.modifiers.Final;
import tools.mdsd.jamopp.model.java.modifiers.Native;
import tools.mdsd.jamopp.model.java.modifiers.Private;
import tools.mdsd.jamopp.model.java.modifiers.Protected;
import tools.mdsd.jamopp.model.java.modifiers.Public;
import tools.mdsd.jamopp.model.java.modifiers.Static;
import tools.mdsd.jamopp.model.java.modifiers.Strictfp;
import tools.mdsd.jamopp.model.java.modifiers.Synchronized;
import tools.mdsd.jamopp.model.java.modifiers.Transient;
import tools.mdsd.jamopp.model.java.modifiers.Volatile;
import tools.mdsd.jamopp.model.java.modifiers.util.ModifiersSwitch;

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
