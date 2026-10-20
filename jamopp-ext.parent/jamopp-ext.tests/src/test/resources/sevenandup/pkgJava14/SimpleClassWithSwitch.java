/*******************************************************************************
 * Copyright (c) 2019-2020, Martin Armbruster
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

package pkgJava14;

public class SimpleClassWithSwitch {	
	public void switchStatements() {
		switch("test") {
			case "Test": break;
			case "test": var i = 0; break;
			default:
		}
		SwitchEnum d = SwitchEnum.A;
		switch(d) {
			case A, B: System.out.println("A");
			case C: System.out.println("B, C"); break;
			case D: System.out.println("D");
			default: break;
		}
		switch(d) {
			case A, B -> System.out.println("A");
			case C -> {System.out.println("B, C");}
			case D -> System.out.println("D");
			default -> throw new NullPointerException();
		}
	}
	
	public void switchExpressions() {
		int k = 0;
		switch (k) {
			case 0 -> {}
			case 13094309 -> System.out.println("H");
			default -> throw new IllegalArgumentException();
		};
		SwitchEnum b = SwitchEnum.H;
		var u = switch(b) {
			case A, B, C -> "A";
			case D -> "";
			case E -> b;
			case F -> SwitchEnum.H;
			case G -> 0;
			case H -> 2.0;
		};
		var m = switch(b) {
			case A, B, C: yield "A";
			case D: yield "";
			case E: yield b;
			case F: yield SwitchEnum.H;
			case G: yield 0;
			case H: System.out.println("H"); yield 2.0;
		};
		m.toString();
	}
	
	public enum SwitchEnum {
		A, B, C, D, E, F, G, H;
	}
}
