package jamopp.parser.jdt.singlefile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.emftext.language.java.JavaClasspath;
import org.emftext.language.java.containers.JavaRoot;
import org.emftext.language.java.resolver.CentralReferenceResolver;

import jamopp.parser.api.JaMoPPParserAPI;
import jamopp.proxy.IJavaContextDependentURIFragment;
import jamopp.proxy.IJavaContextDependentURIFragmentCollector;
import jamopp.resolution.bindings.CentralBindingBasedResolver;

public class JaMoPPJDTSingleFileParser implements JaMoPPParserAPI {
	private final String DEFAULT_ENCODING = StandardCharsets.UTF_8.toString();
	private ResourceSet resourceSet;
	private ArrayList<String> exclusionPatterns = new ArrayList<>();
	private static boolean isResolving = false;
	
	public void setExclusionPatterns(String...patterns) {
		exclusionPatterns.clear();
		for (String pa : patterns) {
			exclusionPatterns.add(pa);
		}
	}
	
	@Override
	public JavaRoot parse(String fileName, InputStream input) {
		this.setUpResourceSet();
		StringBuilder builder = new StringBuilder();
		String lineSep = System.getProperty("line.separator");
		try (InputStreamReader inReader = new InputStreamReader(input);
				BufferedReader buffReader = new BufferedReader(inReader)) {
			buffReader.lines().forEach(line -> builder.append(line + lineSep));
		} catch (IOException e) {
		}
		String src = builder.toString();
		ASTNode ast = parseFileWithJDT(src, fileName);
		OrdinaryCompilationUnitJDTASTVisitorAndConverter converter =
				new OrdinaryCompilationUnitJDTASTVisitorAndConverter();
		converter.setSource(src);
		ast.accept(converter);
		this.resourceSet = null;
		return converter.getConvertedElement();
	}
	
	private ASTNode parseFileWithJDT(String fileContent, String fileName) {
		ASTParser parser = setUpParser();
		parser.setUnitName(fileName);
		parser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
		parser.setSource(fileContent.toCharArray());
		return parser.createAST(null);
	}
	
	@Override
	public Resource parseFile(Path file) {
		this.setUpResourceSet();
		Resource result = this.parseFilesWithJDT(new String[] {}, new String[] { file.toAbsolutePath().toString() },
			new String[] { DEFAULT_ENCODING }).get(0).eResource();
		this.resourceSet = null;
		return result;
	}
	
	@Override
	public ResourceSet parseDirectory(Path dir) {
		this.setUpResourceSet();
		try {
			String[] sources = findSources(dir);
			String[] encodings = new String[sources.length];
			for (int index = 0; index < encodings.length; index++) {
				encodings[index] = DEFAULT_ENCODING;
			}
			String[] classpathEntries = findClasspathEntries(dir);
			System.out.println("Parsing");
			this.parseFilesWithJDT(classpathEntries, sources, encodings);
			System.out.println("Resolving");
			resolveBindings();
		} catch (IOException e) {
		}
		ResourceSet result = this.resourceSet;
		this.resourceSet = null;
		return result;
	}
	
	public String[] findSources(Path directory) throws IOException {
		return findFiles(directory, ".java");
	}
	
	public String[] findClasspathEntries(Path directory) throws IOException {
		return findFiles(directory, ".jar", ".zip");
	}
	
	private String[] findFiles(Path directory, String...extensions) throws IOException {
		return Files.walk(directory).filter(path -> Files.isRegularFile(path)
				&& testFileExtensions(path, extensions))
				.map(Path::toAbsolutePath).map(Path::toString).filter(this::testPathStringForExclusion)
				.toArray(i -> new String[i]);
	}
	
	private boolean testFileExtensions(Path file, String...extensions) {
		String fileName = file.getFileName().toString();
		boolean result = false;
		for (String ext : extensions) {
			result = result || fileName.endsWith(ext);
		}
		return result;
	}
	
	private boolean testPathStringForExclusion(String path) {
		return exclusionPatterns.stream().map(pattern -> !path.matches(pattern))
				.reduce(true, (i, j) -> i && j);
	}
	
	private List<JavaRoot> parseFilesWithJDT(String[] classpathEntries, String[] sources, String[] encodings) {
		ArrayList<JavaRoot> result = new ArrayList<>();
		ASTParser parser = setUpParser();
		for (String entry : classpathEntries) {
			JavaClasspath.get().registerZip(URI.createFileURI(entry));
		}
		parser.setEnvironment(classpathEntries, new String[] {}, new String[] {}, true);
		OrdinaryCompilationUnitJDTASTVisitorAndConverter converter =
				new OrdinaryCompilationUnitJDTASTVisitorAndConverter();
		parser.createASTs(sources, encodings, new String[] {}, new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit node) {
				URI fileURI = URI.createFileURI(sourceFilePath);
				IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE.setBaseURI(fileURI);
				node.accept(converter);
				JavaRoot root = converter.getConvertedElement();
				Resource newResource;
				if (root.eResource() == null) {
					newResource = JaMoPPJDTSingleFileParser.this.resourceSet.createResource(fileURI);
					newResource.getContents().add(root);
					JavaClasspath.get().registerJavaRoot(root, fileURI);
				} else {
					newResource = root.eResource();
					if (!newResource.getURI().toFileString().equals(sourceFilePath)) {
						newResource.setURI(fileURI);
						JavaClasspath.get().registerJavaRoot(root, fileURI);
					}
				}
				result.add(root);
			}
		}, null);
		return result;
	}
	
	private void setUpResourceSet() {
		if (this.resourceSet == null) {
			this.resourceSet = new ResourceSetImpl();
		}
	}
	
	private ASTParser setUpParser() {
		ASTParser parser = ASTParser.newParser(AST.JLS15);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		Map<String, String> compilerOptions = new HashMap<>();
		compilerOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_15);
		compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_15);
		compilerOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_15);
		parser.setCompilerOptions(compilerOptions);
		return parser;
	}
	
	public void resolveBindings() {
		if (isResolving) {
			return;
		}
		if (ParserOptions.TRUE_VALUE.equals(
				ParserOptions.RESOLVE_BINDINGS.getValue())) {
			isResolving = true;
			CentralReferenceResolver.GLOBAL_INSTANCE.setBindingBasedResolver(
					new CentralBindingBasedResolver(this.resourceSet));
			int oldSize;
			int newSize = this.resourceSet.getResources().size();
			HashSet<Resource> alreadyResolved = new HashSet<>();
			do {
				oldSize = newSize;
				List<Resource> resources = new ArrayList<>(this.resourceSet.getResources());
				resources.forEach(r -> {
					if (!alreadyResolved.contains(r)) {
						EcoreUtil.resolveAll(r);
						alreadyResolved.add(r);
					}
				});
				newSize = this.resourceSet.getResources().size();
			} while (oldSize != newSize
					&& ParserOptions.TRUE_VALUE.equals(
					ParserOptions.RESOLVE_ALL_BINDINGS.getValue()));
			CentralReferenceResolver.GLOBAL_INSTANCE.setBindingBasedResolver(null);
			Map<String, IJavaContextDependentURIFragment> fragments =
					IJavaContextDependentURIFragmentCollector.GLOBAL_INSTANCE
					.getContextDependentURIFragmentMap();
			fragments.values().forEach(v -> v.setBinding(null));
			isResolving = false;
		}
	}

	@Override
	public void setResourceSet(ResourceSet set) {
		this.resourceSet = set;
	}
}
