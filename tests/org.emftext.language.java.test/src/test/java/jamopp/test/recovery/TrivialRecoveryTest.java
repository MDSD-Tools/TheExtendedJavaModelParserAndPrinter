/*******************************************************************************
 * Copyright (c) 2022, Martin Armbruster
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

package jamopp.test.recovery;

import java.nio.file.Paths;

import org.emftext.language.java.test.xmi.JavaXMISerializationTest;
import org.junit.jupiter.api.Test;

import jamopp.options.ParserOptions;
import jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser;
import jamopp.recovery.trivial.TrivialRecovery;

public class TrivialRecoveryTest extends JavaXMISerializationTest {
	@Test
	public void testRecovery() throws Exception {
		ParserOptions.CREATE_LAYOUT_INFORMATION.setValue(Boolean.FALSE);
		ParserOptions.PREFER_BINDING_CONVERSION.setValue(Boolean.TRUE);
		ParserOptions.REGISTER_LOCAL.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_BINDINGS.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES.setValue(Boolean.TRUE);
		ParserOptions.RESOLVE_ALL_BINDINGS.setValue(Boolean.FALSE);
		ParserOptions.RESOLVE_EVERYTHING.setValue(Boolean.FALSE);
		JaMoPPJDTSingleFileParser parser = new JaMoPPJDTSingleFileParser();
		parser.setExclusionPatterns(".*?/src/test/.*?", ".*?/src/client/.*?", ".*?/src/e2e/.*?", ".*?/src/lnp/.*?", ".*?/src/web/.*?");
		var set = parser.parseDirectory(Paths.get("teammates"));
		new TrivialRecovery(set).recover();
		set.getResources().forEach(resource -> this.assertResolveAllProxies(resource));
		super.testReprint(set);
		super.transferToXMI(set, true);
		super.compare(set);
	}
	
	@Override
	protected boolean isExcludedFromReprintTest(String name) {
		return false;
	}
}
