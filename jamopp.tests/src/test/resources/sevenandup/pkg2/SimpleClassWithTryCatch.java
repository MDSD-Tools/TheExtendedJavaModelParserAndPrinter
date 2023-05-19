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

package pkg2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class SimpleClassWithTryCatch {

	private final BufferedReader br = new BufferedReader(null);
	
	public void tryCatch() {
		
		try (FileReader reader = new FileReader("./test.txt")) {
			reader.read();
		} catch (IOException e) {
		}
		finally {
		}
		
		try (var r = new FileReader("./test.txt")) {
			r.read();
		} catch (IOException e) {
		}
		
		try (br; FileReader reader = new FileReader("./test.txt");) {
			br.readLine();
		} catch (IOException e) {
		}
		
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
		}
		
		try (Inner i = new Inner()) {
		}
		
		try (Inner i = new Inner()) {
		}
		finally {
		}
	}
	
	class Inner implements AutoCloseable {
		public void close() {
		}
	}
}
