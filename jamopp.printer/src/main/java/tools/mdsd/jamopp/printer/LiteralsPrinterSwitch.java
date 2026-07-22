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

import tools.mdsd.jamopp.model.java.literals.BinaryIntegerLiteral;
import tools.mdsd.jamopp.model.java.literals.BinaryLongLiteral;
import tools.mdsd.jamopp.model.java.literals.BooleanLiteral;
import tools.mdsd.jamopp.model.java.literals.CharacterLiteral;
import tools.mdsd.jamopp.model.java.literals.DecimalDoubleLiteral;
import tools.mdsd.jamopp.model.java.literals.DecimalFloatLiteral;
import tools.mdsd.jamopp.model.java.literals.DecimalIntegerLiteral;
import tools.mdsd.jamopp.model.java.literals.DecimalLongLiteral;
import tools.mdsd.jamopp.model.java.literals.HexDoubleLiteral;
import tools.mdsd.jamopp.model.java.literals.HexFloatLiteral;
import tools.mdsd.jamopp.model.java.literals.HexIntegerLiteral;
import tools.mdsd.jamopp.model.java.literals.HexLongLiteral;
import tools.mdsd.jamopp.model.java.literals.NullLiteral;
import tools.mdsd.jamopp.model.java.literals.OctalIntegerLiteral;
import tools.mdsd.jamopp.model.java.literals.OctalLongLiteral;
import tools.mdsd.jamopp.model.java.literals.Super;
import tools.mdsd.jamopp.model.java.literals.This;
import tools.mdsd.jamopp.model.java.literals.util.LiteralsSwitch;

public class LiteralsPrinterSwitch extends LiteralsSwitch<Boolean> {
	private Switch<Boolean> parent;
	private BufferedWriter writer;
	
	LiteralsPrinterSwitch(Switch<Boolean> parent, BufferedWriter writer) {
		this.parent = parent;
		this.writer = writer;
	}
	
	@Override
	public Boolean caseBooleanLiteral(BooleanLiteral element) {
		try {
			writer.append(Boolean.toString(element.isValue()));
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseCharacterLiteral(CharacterLiteral element) {
		try {
			writer.append("'" + element.getValue() + "'");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseNullLiteral(NullLiteral element) {
		try {
			writer.append("null");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseDecimalFloatLiteral(DecimalFloatLiteral element) {
		try {
			writer.append(Float.toString(element.getDecimalValue()) + "F");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseHexFloatLiteral(HexFloatLiteral element) {
		try {
			writer.append(Float.toHexString(element.getHexValue()) + "F");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseDecimalDoubleLiteral(DecimalDoubleLiteral element) {
		try {
			writer.append(Double.toString(element.getDecimalValue()) + "D");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseHexDoubleLiteral(HexDoubleLiteral element) {
		try {
			writer.append(Double.toHexString(element.getHexValue()) + "D");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseDecimalIntegerLiteral(DecimalIntegerLiteral element) {
		try {
			writer.append(element.getDecimalValue().toString());
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseHexIntegerLiteral(HexIntegerLiteral element) {
		try {
			writer.append("0x" + element.getHexValue().toString(16));
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseOctalIntegerLiteral(OctalIntegerLiteral element) {
		try {
			writer.append("0" + element.getOctalValue().toString(8));
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseBinaryIntegerLiteral(BinaryIntegerLiteral element) {
		try {
			writer.append("0b" + element.getBinaryValue().toString(2));
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseDecimalLongLiteral(DecimalLongLiteral element) {
		try {
			writer.append(element.getDecimalValue().toString() + "L");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseHexLongLiteral(HexLongLiteral element) {
		try {
			writer.append("0x" + element.getHexValue().toString(16) + "L");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseOctalLongLiteral(OctalLongLiteral element) {
		try {
			writer.append("0" + element.getOctalValue().toString(8) + "L");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseBinaryLongLiteral(BinaryLongLiteral element) {
		try {
			writer.append("0b" + element.getBinaryValue().toString(2) + "L");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseThis(This element) {
		try {
			writer.append("this");
		} catch (IOException e) {
		}
		return true;
	}
	
	@Override
	public Boolean caseSuper(Super element) {
		try {
			writer.append("super");
		} catch (IOException e) {
		}
		return true;
	}
}
