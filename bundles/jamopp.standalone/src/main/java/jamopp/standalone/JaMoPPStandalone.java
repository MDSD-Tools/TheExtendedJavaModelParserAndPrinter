package jamopp.standalone;


import java.io.File;
import java.util.ArrayList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.emftext.language.java.containers.CompilationUnit;
import org.emftext.language.java.containers.JavaRoot;
import org.emftext.language.java.containers.Package;

import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emftext.language.java.containers.ContainersFactory;

import jamopp.parser.api.JaMoPPParserAPI;
import jamopp.parser.jdt.JaMoPPJDTParser;
import jamopp.resource.JavaResource2Factory;

/**
 * Class for the stand alone usage of JaMoPP Palladio 
 * 
 * How to use:
 * - Input any URI (absolute or relative file path/ Directory/ Archive) via INPUT 
 * - If you want to output any xmi library files define ENABLE_OUTPUT_OF_LIBRARY_FILES as true 
 * - The xmi output will be generated and saved in ./standalone_output including its package hierarchy
 * 
 * If you have Problems opening the .xmi file with the Ecore Model Editor make sure you installed the Standalone version as an Ecplise Plugin 
 */

public class JaMoPPStandalone {
	
	
	
	private static final String INPUT = "";
	private static final boolean ENABLE_OUTPUT_OF_LIBRARY_FILES = false; 
	
	

    public static void main(String[] agrs) throws Exception {
    	
    	 JavaRoot root = ContainersFactory.eINSTANCE.createEmptyModel();  
		 Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("java", new JavaResource2Factory());
		 Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		 JaMoPPParserAPI parser = new JaMoPPJDTParser();
		 ResourceSet rs = parser.parseUri(URI.createURI(INPUT));
	     EcoreUtil.resolveAll(rs);
	     String outputFileName = "";
		
		 
		for (Resource javaResource : new ArrayList<Resource>(rs.getResources())) {	
			
			if (javaResource.getContents().isEmpty()) {
				
				System.out.println("WARNING: Emtpy Resource: " + javaResource.getURI());
				continue;
			}	
		
			if(!javaResource.getURI().scheme().equals("file") && !ENABLE_OUTPUT_OF_LIBRARY_FILES) {
				continue; 
			}
			
			File outputFile = new File("." + File.separator + "./standalone_output" + File.separator + checkScheme(javaResource));
			outputFile.getParentFile().mkdirs();
			
			URI xmiFileURI = URI.createFileURI(outputFile.getAbsolutePath()).appendFileExtension("xmi");	
			Resource xmiResource = rs.createResource(xmiFileURI);
			xmiResource.getContents().addAll(javaResource.getContents());
		}
		
		
		for (Resource xmiResource : rs.getResources()) {
			if (xmiResource instanceof XMIResource) {
				try {
					xmiResource.save(rs.getLoadOptions());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
    }
    
    

    private static String checkScheme (Resource javaResource) {
    	 int emptyFileName = 0;
		 String outputFileName = "";
		 JavaRoot root = ContainersFactory.eINSTANCE.createEmptyModel(); 
		
		root = (JavaRoot) javaResource.getContents().get(0);
		
		if (root instanceof CompilationUnit) {
			outputFileName = root.getNamespacesAsString().replace(".", File.separator) + File.separator;
			CompilationUnit cu = (CompilationUnit) root;
			if (cu.getClassifiers().size() > 0) {
				outputFileName += cu.getClassifiers().get(0).getName();
			} else {
				outputFileName += emptyFileName++;
			}

		} else if (root instanceof Package) {
			outputFileName = root.getNamespacesAsString().replace(".", File.separator) + File.separator + "package-info";
			if (outputFileName.startsWith(File.separator)) {
				outputFileName = outputFileName.substring(1);
			}
		} else if (root instanceof org.emftext.language.java.containers.Module) {
			outputFileName = root.getNamespacesAsString().replace(".", File.separator) + File.separator + "module-info";
		} 
		return outputFileName;
    	}
		}
    
    
    
  
	