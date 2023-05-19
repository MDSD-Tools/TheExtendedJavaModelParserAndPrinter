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

/**
 * Options for the single file parser.
 * 
 * @author Martin Armbruster
 */
public enum ParserOptions {
	/**
	 * If the binding-based resolution is executed by RESOLVE_BINDINGS and this
	 * option is true, references are transitively and recursively resolved with
	 * bindings.
	 */
	RESOLVE_ALL_BINDINGS,
	/**
	 * Controls the binding-based resolution. If the option is true, bindings are
	 * used to resolve references directly after the parsing.
	 */
	RESOLVE_BINDINGS,
	/**
	 * If this option is true, the bindings of variables with var as type are used
	 * to resolve the type.
	 */
	RESOLVE_BINDINGS_OF_INFERABLE_TYPES,
	/**
	 * If this option is true, a minimal layout information is stored for every
	 * model element during the parsing. If the option is not set or set to false,
	 * no layout information is created.
	 */
	CREATE_LAYOUT_INFORMATION,
	/**
	 * If the binding-based resolution is executed and the reference resolution
	 * encounters a reference for which a binding is available and a mapping to a
	 * physical file, this option controls if the file is loaded or the binding is
	 * converted. If the option is true, bindings are converted without loading the
	 * file. Otherwise, the file is loaded.
	 */
	PREFER_BINDING_CONVERSION,
	/**
	 * If this option is true, references without bindings are resolved during the
	 * binding-based resolution.
	 */
	RESOLVE_EVERYTHING,
	/**
	 * If this option is true, the mapping between logical and physical URIs in the
	 * JavaClasspath are only stored for the ResourceSet of a corresponding model
	 * element.
	 */
	REGISTER_LOCAL;

	public void setValue(Object v) {
		ParserOptionsValueContainer.getInstance().setValue(this, v);
	}

	public Object getValue() {
		return ParserOptionsValueContainer.getInstance().getValue(this);
	}

	public boolean isTrue() {
		return getValue() == Boolean.TRUE;
	}
}
