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

package tools.mdsd.jamopp.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EObjectWithInverseResolvingEList;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import tools.mdsd.jamopp.model.java.JavaClasspath;
import tools.mdsd.jamopp.model.java.LogicalJavaURIGenerator;
import tools.mdsd.jamopp.model.java.containers.CompilationUnit;
import tools.mdsd.jamopp.model.java.containers.JavaRoot;
import tools.mdsd.jamopp.model.java.containers.Origin;
import tools.mdsd.jamopp.model.java.members.MemberContainer;
import tools.mdsd.jamopp.parser.bcel.ClassFileModelLoader;
import tools.mdsd.jamopp.parser.jdt.singlefile.JaMoPPJDTSingleFileParser;
import tools.mdsd.jamopp.printer.JaMoPPPrinter;
import tools.mdsd.jamopp.proxy.IJavaContextDependentURIFragment;
import tools.mdsd.jamopp.proxy.IJavaContextDependentURIFragmentCollector;
import tools.mdsd.jamopp.resolution.resolver.CentralReferenceResolver;
import tools.mdsd.jamopp.resolution.resolver.result.IJavaElementMapping;
import tools.mdsd.jamopp.resolution.resolver.result.IJavaReferenceMapping;
import tools.mdsd.jamopp.resolution.resolver.result.IJavaReferenceResolveResult;
import tools.mdsd.jamopp.resolution.resolver.result.IJavaURIMapping;

public class JavaResource2 extends XMIResourceImpl {
	public static final String JAVAXMI_FILE_EXTENSION = LogicalJavaURIGenerator.JAVAXMI_FILE_EXTENSION_NAME;
	private static final Logger LOGGER = Logger.getLogger("jamopp." + JavaResource2.class.getSimpleName());
	private Map<String, IJavaContextDependentURIFragment> internalURIFragmentMap =
			IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE
			.getContextDependentURIFragmentMap();

	public JavaResource2() {
		super();
	}

	public JavaResource2(URI uri) {
		super(uri);
	}

	@Override
	public void doLoad(InputStream input, Map<?, ?> options) throws IOException {
		if (this.getURI().fileExtension().equals(JAVAXMI_FILE_EXTENSION)) {
			super.doLoad(input, options);
			return;
		}
		IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.setBaseURI(getURI());
		EObject result = null;
		URI physicalURI;
		if (this.getURI().isFile()) {
			physicalURI = this.getURI();
		} else {
			physicalURI = JavaClasspath.get(this).getURIMap().get(this.getURI());
		}
		if (physicalURI == null) {
			LOGGER.error(this.getURI() + " has no physical URI.");
			throw new IllegalStateException("There has to be a physical URI.");
		}
		LOGGER.debug("Loading " + physicalURI);
		String extension = physicalURI.fileExtension();
		if (extension.equals("class")) {
			try {
				result = new ClassFileModelLoader().parse(input, "");
				JavaClasspath.get(this).registerJavaRoot((JavaRoot) result, physicalURI);
				this.getContents().add(result);
			} catch (IOException e) {
			}
		} else {
			JaMoPPJDTSingleFileParser api = new JaMoPPJDTSingleFileParser();
			api.setResourceSet(this.getResourceSet());
			result = api.parse(this.getURI().toString(), input);
			JavaRoot root = (JavaRoot) result;
			root.setOrigin(physicalURI.isFile() ? Origin.FILE : Origin.ARCHIVE);
			JavaClasspath.get(this).registerJavaRoot(root, physicalURI);
			this.getContents().add(result);
			api.setResourceSet(this.getResourceSet());
			api.resolveBindings();
		}
	}

	@Override
	public void doSave(OutputStream output, Map<?, ?> options) throws IOException {
		if (this.getURI().fileExtension().equals(JAVAXMI_FILE_EXTENSION)) {
			super.doSave(output, options);
		} else {
			this.getContents().forEach(object -> {
				if (object instanceof JavaRoot) {
					JaMoPPPrinter.print((JavaRoot) object, output);
				}
			});
		}
	}

	@Override
	public org.eclipse.emf.ecore.EObject getEObject(String id) {
		if (internalURIFragmentMap.containsKey(id)) {
			IJavaContextDependentURIFragment uriFragment = internalURIFragmentMap.get(id);
			IJavaReferenceResolveResult<? extends EObject> result = null;
			// Catch and report all exceptions that occur during proxy resolving.
			try {
				result = CentralReferenceResolver.GLOBAL_INSTANCE.resolve(uriFragment);
			} catch (Exception e) {
				String message = "An expection occured while resolving the proxy for: "
						+ id + ". (" + e.toString() + ")";
//				System.err.println(message);
				e.printStackTrace();
			}
			if (result == null) {
				// The resolving did call itself.
				return null;
			}
			if (!result.wasResolved()) {
				attachResolveError(result, uriFragment.getProxy());
				return null;
			} else {
				EObject proxy = uriFragment.getProxy();
				// Remove an error that might have been added by an earlier attempt.
				removeResolveError(result, proxy);
				attachResolveWarnings(result, proxy);
				IJavaReferenceMapping<? extends EObject> mapping = result.getMappings().iterator().next();
				EObject resultElement = getResultElement(uriFragment, mapping, proxy, result.getErrorMessage());
				this.internalURIFragmentMap.remove(id);
				return resultElement;
			}
		} else {
			return super.getEObject(id);
		}
	}

	@Override
	protected EObject getEObject(List<String> uriFragmentPath) {
		int size = uriFragmentPath.size();
		EObject eObject = getEObjectForURIFragmentRootSegment(size == 0 ? "" : uriFragmentPath.get(0));
		for (int i = 1; i < size && eObject != null; ++i) {
			String uriFragment = uriFragmentPath.get(i);
			if (eObject instanceof MemberContainer
					&& uriFragment.startsWith(LogicalJavaURIGenerator.CLASSIFIERS_SUB_PATH_PREFIX)) {

				MemberContainer memberContainer = (MemberContainer) eObject;
				String name = uriFragment.substring(LogicalJavaURIGenerator.CLASSIFIERS_SUB_PATH_PREFIX.length(),
						uriFragment.length() - 2);
				eObject = memberContainer.getContainedClassifier(name);
			} else if (eObject instanceof CompilationUnit
					&& uriFragment.startsWith(LogicalJavaURIGenerator.CLASSIFIERS_ROOT_PATH_PREFIX)) {

				CompilationUnit compilationUnit = (CompilationUnit) eObject;
				String name = uriFragment.substring(LogicalJavaURIGenerator.CLASSIFIERS_ROOT_PATH_PREFIX.length(),
						uriFragment.length() - 2);
				eObject = compilationUnit.getContainedClassifier(name);
				int j = i + 1;
				while (j < size && eObject == null) {
					// this is required for classifiers with '$' in their names
					String subUriFragment = uriFragmentPath.get(j);
					String subName = subUriFragment.substring(
							LogicalJavaURIGenerator.CLASSIFIERS_SUB_PATH_PREFIX.length(),
							subUriFragment.length() - 2);
					name = name + "$" + subName;
					eObject = compilationUnit.getContainedClassifier(name);
					if (eObject == null) {
						eObject = compilationUnit.getContainedClassifier(subName);
					}
					if (eObject != null) {
						i = j;
					} else {
						j++;
					}
				}

			} else {
				eObject = ((InternalEObject) eObject).eObjectForURIFragmentSegment(uriFragmentPath.get(i));
			}
		}

		return eObject;
	}

	private EObject getResultElement(IJavaContextDependentURIFragment uriFragment,
			IJavaReferenceMapping<? extends EObject> mapping, EObject proxy, final String errorMessage) {
				// TODO
		// if (mapping instanceof IJavaURIMapping) {
		// 	URI uri = ((IJavaURIMapping) mapping).getTarget();
		// 	if (uri != null) {
		// 		EObject result = null;
		// 		try {
		// 			result = this.getResourceSet().getEObject(uri, true);
		// 		} catch (Exception e) {
		// 			// We can catch exceptions here because EMF will try to resolve again and
		// 			// handles the exception.
		// 		}
		// 		if (result == null || result.eIsProxy()) {
		// 			// Unable to resolve: attach error.
		// 			if (errorMessage == null) {
		// 				assert false;
		// 			} else {
		// 				getErrors().add(new SimpleDiagnostic(errorMessage, proxy));
		// 			}
		// 		}
		// 		return result;
		// 	}
		// 	return null;
		// } else
		if (mapping instanceof IJavaElementMapping<?>) {
			EObject element = ((IJavaElementMapping<? extends EObject>) mapping).getTarget();
			EReference reference = uriFragment.getReference();
			EReference oppositeReference = uriFragment.getReference().getEOpposite();
			if (!uriFragment.getReference().isContainment() && oppositeReference != null) {
				if (reference.isMany()) {
					// Avoids duplicated entries in the reference caused by adding to the
					// oppositeReference.
					@SuppressWarnings("unchecked")
					EObjectWithInverseResolvingEList.ManyInverse<EObject> list =
						(EObjectWithInverseResolvingEList.ManyInverse<EObject>) element
							.eGet(oppositeReference, false);
					list.basicAdd(uriFragment.getContainer(), null);
				} else {
					uriFragment.getContainer().eSet(uriFragment.getReference(), element);
				}
			}
			return element;
		} else {
			assert false;
			return null;
		}
	}

	private void removeResolveError(IJavaReferenceResolveResult<?> result, EObject proxy) {
		String errorMessage = result.getErrorMessage();
		for (Iterator<Diagnostic> iter = getErrors().iterator(); iter.hasNext();) {
			Diagnostic diag = iter.next();
			if (diag instanceof SimpleDiagnostic && ((SimpleDiagnostic) diag).corresponding == proxy) {
				iter.remove();
			}
		}
	}

	private void attachResolveError(IJavaReferenceResolveResult<?> result, EObject proxy) {
		assert result != null;
		final String errorMessage = result.getErrorMessage();
		if (errorMessage == null) {
			assert false;
		} else {
			getErrors().add(new SimpleDiagnostic(errorMessage, proxy));
		}
	}

	private void attachResolveWarnings(IJavaReferenceResolveResult<? extends EObject> result, EObject proxy) {
		assert result != null;
		assert result.wasResolved();
		if (result.wasResolved()) {
			for (IJavaReferenceMapping<? extends EObject> mapping : result.getMappings()) {
				final String warningMessage = mapping.getWarning();
				if (warningMessage == null) {
					continue;
				}
				getWarnings().add(new SimpleDiagnostic(warningMessage));
			}
		}
	}

	public class SimpleDiagnostic implements Resource.Diagnostic {
		private String message;
		private EObject corresponding;

		SimpleDiagnostic(String message) {
			this(message, null);
		}

		SimpleDiagnostic(String message, EObject correspond) {
			this.message = message;
			corresponding = correspond;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public String getLocation() {
			return JavaResource2.this.getURI().toString();
		}

		@Override
		public int getLine() {
			return 0;
		}

		@Override
		public int getColumn() {
			return 0;
		}
	}
}
