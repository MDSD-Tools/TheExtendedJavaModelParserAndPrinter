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

package jamopp.options;

import java.util.EnumMap;

/**
 * A container for values of ParserOptions.
 * 
 * @author Martin Armbruster
 */
public final class ParserOptionsValueContainer {
	private static ParserOptionsValueContainer instance;
	private EnumMap<ParserOptions, Object> values;
	
	private ParserOptionsValueContainer() {
		values = new EnumMap<>(ParserOptions.class);
	}
	
	public Object getValue(ParserOptions option) {
		return values.get(option);
	}
	
	public void setValue(ParserOptions option, Object value) {
		values.put(option, value);
	}
	
	public static ParserOptionsValueContainer getInstance() {
		if (instance == null) {
			var localInstance = new ParserOptionsValueContainer();
			localInstance.setValue(ParserOptions.RESOLVE_ALL_BINDINGS, Boolean.TRUE);
			localInstance.setValue(ParserOptions.RESOLVE_BINDINGS, Boolean.TRUE);
			localInstance.setValue(ParserOptions.RESOLVE_BINDINGS_OF_INFERABLE_TYPES, Boolean.TRUE);
			localInstance.setValue(ParserOptions.CREATE_LAYOUT_INFORMATION, Boolean.TRUE);
			localInstance.setValue(ParserOptions.PREFER_BINDING_CONVERSION, Boolean.TRUE);
			instance = localInstance;
		}
		return instance;
	}
}
