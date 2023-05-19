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

import tools.mdsd.jamopp.model.java.generics.GenericsPackage;
import tools.mdsd.jamopp.model.java.instantiations.ExplicitConstructorCall;
import tools.mdsd.jamopp.model.java.instantiations.NewConstructorCall;
import tools.mdsd.jamopp.model.java.instantiations.NewConstructorCallWithInferredTypeArguments;
import tools.mdsd.jamopp.model.java.instantiations.util.InstantiationsSwitch;
import tools.mdsd.jamopp.model.java.references.ReferencesPackage;

class InstantiationsPrinterSwitch extends InstantiationsSwitch<Boolean> {
	private ComposedParentPrinterSwitch parent;
	private BufferedWriter writer;
	
	InstantiationsPrinterSwitch(ComposedParentPrinterSwitch parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseNewConstructorCall(NewConstructorCall element) {
		try {
			writer.append("new ");
			parent.doSwitch(GenericsPackage.Literals.CALL_TYPE_ARGUMENTABLE, element);
			writer.append(" ");
			parent.doSwitch(element.getTypeReference());
			if (element instanceof NewConstructorCallWithInferredTypeArguments) {
				writer.append("<>");
			} else {
				parent.doSwitch(GenericsPackage.Literals.TYPE_ARGUMENTABLE, element);
			}
			parent.doSwitch(ReferencesPackage.Literals.ARGUMENTABLE, element);
			if (element.getAnonymousClass() != null) {
				parent.doSwitch(element.getAnonymousClass());
			}
			parent.doSwitch(ReferencesPackage.Literals.REFERENCE, element);
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseNewConstructorCallWithInferredTypeArguments(NewConstructorCallWithInferredTypeArguments element) {
		return caseNewConstructorCall(element);
	}
	
	@Override
	public Boolean caseExplicitConstructorCall(ExplicitConstructorCall element) {
		parent.doSwitch(GenericsPackage.Literals.CALL_TYPE_ARGUMENTABLE, element);
		parent.doSwitch(element.getCallTarget());
		parent.doSwitch(ReferencesPackage.Literals.ARGUMENTABLE, element);
		parent.doSwitch(ReferencesPackage.Literals.REFERENCE, element);
		return true;
	}
}
