/*******************************************************************************
 * Copyright (c) 2006-2013
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/

package tools.mdsd.jamopp.test.xmi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import tools.mdsd.jamopp.options.ParserOptions;
import tools.mdsd.jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser;
import tools.mdsd.jamopp.test.AbstractJaMoPPTests;
import tools.mdsd.jamopp.test.OutputUtility;

public class JavaXMISerializationTest extends AbstractJaMoPPTests {

	protected static final String TEST_INPUT_FOLDER_NAME = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "input";
	protected static String TEST_OUTPUT_FOLDER_NAME = "target" + File.separator + "tests" + File.separator + "output";
	
	@BeforeAll
	public static void generalSetup() {
		AbstractJaMoPPTests.initLogging();
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.TRUE);
	}
	
	@Disabled("Fails in pipeline, but no locally.")
	@Test
	public void testXMISerialization() throws Exception {
		String[] excludings = {".*?UnicodeIdentifiers.java"};
		File inputFolder = new File("./" + TEST_INPUT_FOLDER_NAME);
		List<File> allTestFiles = collectAllFilesRecursive(inputFolder, "java");
		allTestFiles.removeIf(f -> {
			for (String ex : excludings) {
				if (f.getName().matches(ex)) {
					return true;
				}
			}
			return false;
		});

		JaMoPPJDTSingleFileParser parser = new JaMoPPJDTSingleFileParser();
		parser.setResourceSet(getResourceSet());
		parser.setExclusionPatterns(excludings);
		parser.parseDirectory(inputFolder.toPath());
		
		EcoreUtil.resolveAll(getResourceSet());
		for (Resource res : new ArrayList<>(getResourceSet().getResources())) {
			this.assertResolveAllProxies(res);
		}

		ResourceSet targetSet = transferToXMI(getResourceSet(), false);
		
		compare(targetSet);
	}
	
	protected ResourceSet transferToXMI(ResourceSet sourceSet, boolean includeAllResources) throws Exception {
		return OutputUtility.transferToOutput(sourceSet, TEST_OUTPUT_FOLDER_NAME, "xmi", includeAllResources).targetSet();
	}
	
	protected void compare(ResourceSet rs) throws Exception {
		for (var xmiResource : new ArrayList<>(rs.getResources())) {
		
			assertNotNull(xmiResource);
			if (xmiResource.getContents().isEmpty()) {
				continue;
			}
			EObject root = xmiResource.getContents().get(0);
			
			//reload
			ResourceSet reloadeSet = super.getResourceSet();
			Resource reloadedResource = null;
			try {
				reloadedResource = reloadeSet.getResource(xmiResource.getURI(), true);
			} catch (Exception e) {
				fail(e.getClass() +  ": " + e.getMessage());
				return;
			}
			assertResolveAllProxies(reloadedResource);
			for (Diagnostic d : reloadedResource.getErrors()) {
				System.out.println(d.getMessage());
			}
			assertTrue(reloadedResource.getErrors().isEmpty(), "Parsed XMI contains errors");
			
			EObject reloadedRoot = reloadedResource.getContents().get(0);
		    EqualityHelper equalityHelper = new EqualityHelper() {
				private static final long serialVersionUID = 1L;
	
				@Override
		    	public boolean equals(EObject eObject1, EObject eObject2) {
		    		boolean result = super.equals(eObject1, eObject2);
		    		if (!result) {
		    			System.out.println("Not equal: " + eObject1 + " != " + eObject2);
		    		}
		    		return result;
		    	}
		    	
		    	@Override
		    	protected boolean haveEqualFeature(EObject eObject1,
		    			EObject eObject2, EStructuralFeature feature) {
		    		if (feature.isTransient()) {
		    			//ignore transient features
		    			return true;
		    		}
		    		return super.haveEqualFeature(eObject1, eObject2, feature);
		    	}
		    };
		    
		    assertTrue(equalityHelper.equals(root, reloadedRoot), "Original and reloaded XMI are not equal");
		}
	}
	
	private ResourceSet sharedRS;
	
	@Override
	protected ResourceSet getResourceSet() throws Exception {
		if (sharedRS == null) {
			sharedRS = super.getResourceSet();
		}
		return sharedRS;
	}

	@Override
	protected boolean isExcludedFromReprintTest(String filename) {
		return true;
	}

	@Override
	protected String getTestInputFolder() {
		return TEST_INPUT_FOLDER_NAME;
	}
}
