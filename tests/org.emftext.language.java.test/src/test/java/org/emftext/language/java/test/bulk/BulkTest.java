/*******************************************************************************
 * Copyright (c) 2006-2015
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Dresden, Amtsgericht Dresden, HRB 34001
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Dresden, Germany
 *      - initial API and implementation
 *   Martin Armbruster
 *      - Adaptation and extension for Java 7+
 ******************************************************************************/
package org.emftext.language.java.test.bulk;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.examples.Expander;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emftext.language.java.test.AbstractJaMoPPTests;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jamopp.parser.jdt.JaMoPPJDTParser;

@Disabled("Requires initialization of all submodules and dependency resolution.")
public class BulkTest extends AbstractJaMoPPTests {
	private final static String BASE_ZIP = "JaMoPP-BulkTest" + File.separator + "Tests" + File.separator
		+ "org.emftext.language.java.test.bulk" + File.separator + "input" + File.separator;
	private final static String END_ZIP = File.separator + "src.zip";
	private final static String END_SRC = File.separator + "src";
	private String inputFolder;
	private String generalInputFolder;
	
	@Test
	public void testAndroMDA_3_3() {
		inputFolder = "andromda-3.3";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testApacheAnt_1_8_1() {
		inputFolder = "apache-ant-1.8.1";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testApacheCommonsMath_1_2() {
		inputFolder = "apache-commons-math-1.2";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testApacheTomcat_6_0_18() {
		inputFolder = "apache-tomcat-6.0.18";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	@Disabled("Reprint of class EvaluateAction does not equal the original file.")
	public void testEclipse_3_4_1() {
		inputFolder = "eclipse-3.4.1";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testGWT_1_5_3() {
		inputFolder = "gwt-1.5.3";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testJacks_1_6_0_07() {
		inputFolder = "jacks_javac_1.6.0_07_passed";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testJBoss_5_0_0() {
		inputFolder = "jboss-5.0.0.GA";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testJDownloader_0_9579() {
		inputFolder = "jdownloader_0.9579";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testMantissa_7_2() {
		inputFolder = "mantissa-7.2";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	@Disabled("JDT AST creation runs into OutOfMemoryError.")
	public void testNetbeans_6_5_1() {
		inputFolder = "netbeans-6.5.1";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testSpingFramework_3_0_0_M1() {
		inputFolder = "spring-framework-3.0.0.M1";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testStruts_2_1_6() {
		inputFolder = "struts-2.1.6";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testXerces_2_9_1() {
		inputFolder = "Xerces-J-2.9.1";
		generalInputFolder = null;
		testProject();
	}
	
	@Test
	public void testTeaStore() {
		inputFolder = null;
		generalInputFolder = "TeaStore";
		testProject();
	}
	
	@Test
	public void testTeammates() {
		inputFolder = null;
		generalInputFolder = "teammates";
		testProject();
	}
	
	@Test
	public void testMicroservice() {
		inputFolder = null;
		generalInputFolder = "microservice";
		testProject();
	}
	
	@Test
	public void testESDA() {
		inputFolder = null;
		generalInputFolder = "esda";
		testProject();
	}
	
	@Override
	protected boolean isExcludedFromReprintTest(String filename) {
		return false;
	}

	@Override
	protected String getTestInputFolder() {
		return generalInputFolder == null ? BASE_ZIP + inputFolder : generalInputFolder;
	}
	
	private String getSrcZip() {
		return generalInputFolder == null ? BASE_ZIP + inputFolder + END_ZIP : null;
	}
	
	private void testProject() {
		JaMoPPJDTParser parser = new JaMoPPJDTParser();
		if (generalInputFolder == null) {
			decompressZipFile();
		}
		Path target = Paths.get(getTestInputFolder());
		try {
			String jacksPrefix = ".*?jacks\\_javac\\_1\\.6\\.0\\_07\\_passed.*?";
			Files.walk(target).filter(Files::isRegularFile)
			.filter(path -> path.endsWith("bin.jar") || path.endsWith("rt.jar") || path.endsWith("jsse.jar")
					|| path.endsWith("bin1.jar") || path.endsWith("bin2.jar") || path.endsWith("bin3.jar")
					|| path.endsWith("bin4.jar") || path.endsWith("jce.jar") || path.endsWith("sunjce_provider.jar")
					|| target.relativize(path).toString().contains(File.separator + "test" + File.separator)
					|| target.relativize(path).toString().contains(File.separator + "tests" + File.separator)
					|| path.toAbsolutePath().toString().matches(".*?apache\\-tomcat\\-6\\.0\\.18.*?WEB\\-INF.*?Clock2\\.java")
					// Extensive filtering for the Jacks_1_6_0_07 test case because these files contain duplicated classes
					// leading to false results.
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "QualifiedInterfaceMember\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T125r2\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T151222a13(a|b)\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T151222a(3|6a)\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T1585fe6\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T1585me(5|8)\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T1594rc4\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T71n4a\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T71n(5|6)a\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T71u4a\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T73(5|2)\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T813nc1\\.java")
					|| path.toAbsolutePath().toString().matches(jacksPrefix + "T814c2a\\.java"))
			.forEach(path -> {
				try {
					Files.delete(path);
				} catch (IOException e) {
				}
			});
		} catch (IOException e) {
			fail(e.getMessage());
		}
		ResourceSet set = parser.parseDirectory(target);
		try {
			this.testReprint(set);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	private void decompressZipFile() {
		Path file = Paths.get(getSrcZip());
		if (Files.exists(file)) {
			Path target = Paths.get(getTestInputFolder(), END_SRC);
			try {
				new Expander().expand(ArchiveStreamFactory.ZIP, file.toFile(), target.toFile());
			} catch (IOException | ArchiveException e) {
				fail(e.getMessage());
			}
		} else {
			fail("File " + file + " is not a zip file.");
		}
	}
}
