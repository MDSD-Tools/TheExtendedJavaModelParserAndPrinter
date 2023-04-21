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
import org.emftext.language.java.literals.BinaryIntegerLiteral;
import org.emftext.language.java.literals.BinaryLongLiteral;
import org.emftext.language.java.literals.BooleanLiteral;
import org.emftext.language.java.literals.CharacterLiteral;
import org.emftext.language.java.literals.DecimalDoubleLiteral;
import org.emftext.language.java.literals.DecimalFloatLiteral;
import org.emftext.language.java.literals.DecimalIntegerLiteral;
import org.emftext.language.java.literals.DecimalLongLiteral;
import org.emftext.language.java.literals.HexDoubleLiteral;
import org.emftext.language.java.literals.HexFloatLiteral;
import org.emftext.language.java.literals.HexIntegerLiteral;
import org.emftext.language.java.literals.HexLongLiteral;
import org.emftext.language.java.literals.NullLiteral;
import org.emftext.language.java.literals.OctalIntegerLiteral;
import org.emftext.language.java.literals.OctalLongLiteral;
import org.emftext.language.java.literals.Super;
import org.emftext.language.java.literals.This;
import org.emftext.language.java.literals.util.LiteralsSwitch;

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
