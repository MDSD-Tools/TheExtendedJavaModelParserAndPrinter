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

package jamopp.printer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.emf.ecore.EClass;
import org.emftext.language.java.commons.Commentable;
import org.emftext.language.java.containers.ContainersPackage;
import org.emftext.language.java.containers.JavaRoot;

/**
 * This class provides methods to print JaMoPP model instances.
 */
public final class JaMoPPPrinter {
	/**
	 * Private constructor to avoid instantiation.
	 */
	private JaMoPPPrinter() {
	}
	
	/**
	 * Prints a model instance into an OutputStream.
	 * 
	 * @param root the model instance to print.
	 * @param output the output for printing.
	 */
	public static void print(JavaRoot root, OutputStream output) {
		print((Commentable) root, output);
	}
	
	/**
	 * Prints an arbitrary Java model element into an OutputStream.
	 * 
	 * @param element the Java model element.
	 * @param output the output for printing.
	 */
	public static void print(Commentable element, OutputStream output) {
		try (OutputStreamWriter outWriter = new OutputStreamWriter(output, StandardCharsets.UTF_8);
				BufferedWriter buffWriter = new BufferedWriter(outWriter)) {
			internalPrint(element, element instanceof JavaRoot ? ContainersPackage.Literals.JAVA_ROOT
					: element.eClass(), buffWriter);
		} catch (IOException e) {
		}
	}
	
	/**
	 * Prints a model instance into a file.
	 * 
	 * @param root the model instance to print.
	 * @param file the file for printing.
	 */
	public static void print(JavaRoot root, Path file) {
		try (BufferedWriter writer = Files.newBufferedWriter(file)) {
			internalPrint(root, ContainersPackage.Literals.JAVA_ROOT, writer);
		} catch (IOException e) {
		}
	}
	
	private static void internalPrint(Commentable root, EClass rootClass, BufferedWriter writer) {
		new ComposedParentPrinterSwitch(writer).doSwitch(rootClass, root);
	}
}
