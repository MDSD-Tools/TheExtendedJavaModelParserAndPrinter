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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.ComposedSwitch;

class ComposedParentPrinterSwitch extends ComposedSwitch<Boolean> {
	ComposedParentPrinterSwitch(BufferedWriter writer) {
		this.addSwitch(new AnnotationsPrinterSwitch(this, writer));
		this.addSwitch(new ArraysPrinterSwitch(this, writer));
		this.addSwitch(new ClassifiersPrinterSwitch(this, writer));
		this.addSwitch(new ContainersPrinterSwitch(this, writer));
		this.addSwitch(new ExpressionsPrinterSwitch(this, writer));
		this.addSwitch(new GenericsPrinterSwitch(this, writer));
		this.addSwitch(new ImportsPrinterSwitch(this, writer));
		this.addSwitch(new InstantiationsPrinterSwitch(this, writer));
		this.addSwitch(new LiteralsPrinterSwitch(this, writer));
		this.addSwitch(new MembersPrinterSwitch(this, writer));
		this.addSwitch(new ModifiersPrinterSwitch(this, writer));
		this.addSwitch(new ModulesPrinterSwitch(this, writer));
		this.addSwitch(new OperatorsPrinterSwitch(this, writer));
		this.addSwitch(new ParametersPrinterSwitch(this, writer));
		this.addSwitch(new ReferencesPrinterSwitch(this, writer));
		this.addSwitch(new StatementsPrinterSwitch(this, writer));
		this.addSwitch(new TypesPrinterSwitch(this, writer));
		this.addSwitch(new VariablesPrinterSwitch(this, writer));
	}
	
	@Override
	public Boolean doSwitch(EClass clazz, EObject obj) {
		return super.doSwitch(clazz, obj);
	}
}
