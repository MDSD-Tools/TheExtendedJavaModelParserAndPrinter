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

package jamopp.parser.api;

import java.io.InputStream;
import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emftext.language.java.containers.JavaRoot;

/**
 * This interface provides an API for parsers that read Java source files and convert them to Java model instances.
 */
public interface JaMoPPParserAPI {
	/**
	 * Reads an InputStream and parses its content into a Java model instance.
	 * 
	 * @param fileName name of the Java source file which will be read.
	 * @param input the InputStream to read.
	 * @return the created Java model instance or null if the InputStream could not be read.
	 */
	JavaRoot parse(String fileName, InputStream input);
	
	/**
	 * Reads a file and parses its content into a Java model instance.
	 * 
	 * @param file the Java source file.
	 * @return the created Java model instance contained in its associated Resource instance or null if the file could not be read.
	 */
	Resource parseFile(Path file);
	
	/**
	 * Visits all files and directories in a directory and parses all found Java source files.
	 * It is assumed that the given directory is a Java source folder containing sub-directories representing
	 * a Java package hierarchy.
	 * 
	 * @param directory the directory to search for Java source files.
	 * @return a ResourceSet containing all parsed source files with their associated Resources.
	 */
	ResourceSet parseDirectory(Path directory);
	
	/**
	 * Sets the ResourceSet that is used to create Resources if new Resource instances are needed. If no ResourceSet is
	 * provided, a ResourceSet is created.
	 * 
	 * @param set the ResourceSet.
	 */
	void setResourceSet(ResourceSet set);
	
	default ResourceSet parseUri(URI uri) {
		if (uri.isFile()) { 
			return parseFile(Path.of(uri.toFileString())).getResourceSet();
		}
		
		if (uri.hasPath()) { 
			return parseDirectory(Path.of(uri.path()));
	}	
		return null; 
}
}
