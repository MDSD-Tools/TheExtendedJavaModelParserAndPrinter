/*******************************************************************************
 * Copyright (c) 2020, Martin Armbruster
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

package tools.mdsd.jamopp.parser.jdt.singlefile;

import java.math.BigInteger;

import org.eclipse.jdt.core.dom.NumberLiteral;

class NumberLiteralConverterUtility {
	private static final String HEX_PREFIX = "0x";
	private static final String BIN_PREFIX = "0b";
	private static final String OCT_PREFIX = "0";
	private static final String LONG_SUFFIX = "l";
	private static final String FLOAT_SUFFIX = "f";
	private static final String DOUBLE_SUFFIX = "d";
	private static final String DECIMAL_EXPONENT = "e";
	private static final String HEX_EXPONENT = "p";
	private static final int BIN_BASE = 2;
	private static final int HEX_BASE = 16;
	private static final int DEC_BASE = 10;
	private static final int OCT_BASE = 8;
	private static final String UNDER_SCORE = "_";
	
	static tools.mdsd.jamopp.model.java.literals.Literal convertToLiteral(NumberLiteral literal) {
		tools.mdsd.jamopp.model.java.literals.Literal result = null;
		String string = literal.getToken();
		if (string.contains("\\u")) {
			StringBuilder actualLiteral = new StringBuilder();
			for (int index = 0; index < string.length(); index++) {
				char currentChar = string.charAt(index);
				if (currentChar == '\\') {
					int codePoint = Integer.parseInt(string.substring(index + 2, index + 6), 16);
					actualLiteral.append(Character.toString(codePoint));
					index += 5;
				} else {
					actualLiteral.append(currentChar);
				}
			}
			string = actualLiteral.toString();
		}
		string = string.replaceAll(UNDER_SCORE, "");
		string = string.toLowerCase();
		if (string.startsWith(BIN_PREFIX) && string.endsWith(LONG_SUFFIX)) {
			tools.mdsd.jamopp.model.java.literals.BinaryLongLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createBinaryLongLiteral();
			lit.setBinaryValue(new BigInteger(string.substring(BIN_PREFIX.length(),
					string.length() - LONG_SUFFIX.length()), BIN_BASE));
			result = lit;
		} else if (string.startsWith(BIN_PREFIX)) {
			tools.mdsd.jamopp.model.java.literals.BinaryIntegerLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createBinaryIntegerLiteral();
			lit.setBinaryValue(new BigInteger(string.substring(BIN_PREFIX.length()), BIN_BASE));
			result = lit;
		} else if (string.contains(HEX_EXPONENT) && string.endsWith(FLOAT_SUFFIX)) {
			tools.mdsd.jamopp.model.java.literals.HexFloatLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createHexFloatLiteral();
			lit.setHexValue(Float.parseFloat(string.substring(0, string.length() - FLOAT_SUFFIX.length())));
			result = lit;
		} else if (string.contains(HEX_EXPONENT)) {
			if (string.endsWith(DOUBLE_SUFFIX)) {
				string = string.substring(0, string.length() - DOUBLE_SUFFIX.length());
			}
			tools.mdsd.jamopp.model.java.literals.HexDoubleLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createHexDoubleLiteral();
			lit.setHexValue(Double.parseDouble(string));
			result = lit;
		} else if (string.startsWith(HEX_PREFIX) && string.endsWith(LONG_SUFFIX)) {
			tools.mdsd.jamopp.model.java.literals.HexLongLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createHexLongLiteral();
			lit.setHexValue(new BigInteger(string.substring(HEX_PREFIX.length(),
					string.length() - LONG_SUFFIX.length()), HEX_BASE));
			result = lit;
		} else if (string.startsWith(HEX_PREFIX)) {
			tools.mdsd.jamopp.model.java.literals.HexIntegerLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createHexIntegerLiteral();
			lit.setHexValue(new BigInteger(string.substring(HEX_PREFIX.length()), HEX_BASE));
			result = lit;
		} else if (string.endsWith(FLOAT_SUFFIX)) {
			tools.mdsd.jamopp.model.java.literals.DecimalFloatLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createDecimalFloatLiteral();
			lit.setDecimalValue(Float.parseFloat(string.substring(0, string.length() - FLOAT_SUFFIX.length())));
			result = lit;
		} else if (string.contains(".") || string.contains(DECIMAL_EXPONENT) || string.endsWith(DOUBLE_SUFFIX)) {
			if (string.endsWith(DOUBLE_SUFFIX)) {
				string = string.substring(0, string.length() - DOUBLE_SUFFIX.length());
			}
			tools.mdsd.jamopp.model.java.literals.DecimalDoubleLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createDecimalDoubleLiteral();
			lit.setDecimalValue(Double.parseDouble(string));
			result = lit;
		} else if (string.equals(OCT_PREFIX + LONG_SUFFIX)
				|| (!string.startsWith(OCT_PREFIX) && string.endsWith(LONG_SUFFIX))) {
			tools.mdsd.jamopp.model.java.literals.DecimalLongLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createDecimalLongLiteral();
			lit.setDecimalValue(new BigInteger(string.substring(0,
					string.length() - LONG_SUFFIX.length()), DEC_BASE));
			result = lit;
		} else if (string.equals("0") || !string.startsWith(OCT_PREFIX)) {
			tools.mdsd.jamopp.model.java.literals.DecimalIntegerLiteral lit = tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createDecimalIntegerLiteral();
			lit.setDecimalValue(new BigInteger(string, DEC_BASE));
			result = lit;
		} else if (string.endsWith(LONG_SUFFIX)) {
			tools.mdsd.jamopp.model.java.literals.OctalLongLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createOctalLongLiteral();
			lit.setOctalValue(new BigInteger(string.substring(OCT_PREFIX.length(),
					string.length() - LONG_SUFFIX.length()), OCT_BASE));
			result = lit;
		} else {
			tools.mdsd.jamopp.model.java.literals.OctalIntegerLiteral lit =
					tools.mdsd.jamopp.model.java.literals.LiteralsFactory.eINSTANCE.createOctalIntegerLiteral();
			lit.setOctalValue(new BigInteger(string.substring(OCT_PREFIX.length()), OCT_BASE));
			result = lit;
		}
		LayoutInformationConverter.convertToMinimalLayoutInformation(result, literal);
		return result;
	}
}
