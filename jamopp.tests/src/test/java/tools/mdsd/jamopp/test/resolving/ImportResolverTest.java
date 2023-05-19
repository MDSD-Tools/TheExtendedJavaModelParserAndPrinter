/*******************************************************************************
 * Copyright (c) 2014
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Benjamin Klatt
 *******************************************************************************/

package tools.mdsd.jamopp.test.resolving;


import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;

import org.eclipse.emf.common.util.EList;
import org.junit.jupiter.api.Test;

import tools.mdsd.jamopp.model.java.classifiers.ConcreteClassifier;
import tools.mdsd.jamopp.model.java.containers.JavaRoot;
import tools.mdsd.jamopp.model.java.imports.Import;

/**
 * Test case to test the resolving of imports and imported classifiers.
 */
public class ImportResolverTest extends AbstractResolverTestCase {

    private static String BASE_PATH = "importResolverTest" + File.separator;

    /**
     * A test to parse and resolve a class containing an import of a Java Standard Library
     * Enumeration.
     *
     * @throws Exception
     *             Any error during code parsing or resolving.
     */
    @Test
    public void testResolveClassifiersOfImport() throws Exception {
        JavaRoot javaRoot = parseResource(BASE_PATH + "JavaUtilEnumerationImport.java");
        EList<Import> imports = javaRoot.getImports();

        ConcreteClassifier classifier = imports.get(0).getClassifier();

        assertFalse(classifier.eIsProxy(), "Failed to resolve classifier (Enum BigDecimalLayoutForm)");
    }
}
