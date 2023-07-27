package tools.mdsd.jamopp.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;

import tools.mdsd.jamopp.model.java.containers.CompilationUnit;
import tools.mdsd.jamopp.model.java.containers.JavaRoot;
import tools.mdsd.jamopp.model.java.containers.Package;

public class OutputUtility {
	public record TransferResult(ResourceSet targetSet, Map<Resource, Resource> sourceTargetMapping) {};
	
	public static TransferResult transferToOutput(ResourceSet sourceSet, String outputFolder, String fileExtension, boolean includeAllResources) {
		int emptyFileName = 0;
		
		ResourceSet targetSet = new ResourceSetImpl();
		HashMap<Resource, Resource> srcTrgMap = new HashMap<>();
		
		for (Resource javaResource : new ArrayList<>(sourceSet.getResources())) {
			if (javaResource.getContents().isEmpty()) {
				System.out.println("WARNING: Emtpy Resource: " + javaResource.getURI());
				continue;
			}
			if (!includeAllResources && !javaResource.getURI().isFile()) {
				continue;
			}
			
			JavaRoot root = (JavaRoot) javaResource.getContents().get(0);
			String outputFileName = "ERROR";
			if (root instanceof CompilationUnit cu) {
				outputFileName = cu.getNamespacesAsString().replace(".", File.separator) + File.separator;
				if (cu.getClassifiers().size() > 0) {
					outputFileName += cu.getClassifiers().get(0).getName();
				} else {
					outputFileName += emptyFileName++;
				}
			} else if (root instanceof Package) {
				outputFileName = root.getNamespacesAsString()
						.replace(".", File.separator) + File.separator + "package-info";
				if (outputFileName.startsWith(File.separator)) {
					outputFileName = outputFileName.substring(1);
				}
			} else if (root instanceof tools.mdsd.jamopp.model.java.containers.Module) {
				outputFileName = root.getNamespacesAsString()
						.replace(".", File.separator) + File.separator + "module-info";
			} else {
				fail();
			}
			
			File outputFile = new File("." + File.separator + outputFolder
					+ File.separator + outputFileName);
			URI fileURI = URI.createFileURI(outputFile.getAbsolutePath()).appendFileExtension(fileExtension);	
			
			Resource targetResource = targetSet.createResource(fileURI);
			if (targetResource instanceof XMIResource xmiResource) {
				xmiResource.setEncoding(StandardCharsets.UTF_8.toString());
			}
			targetResource.getContents().addAll(javaResource.getContents());
			srcTrgMap.put(javaResource, targetResource);
		}
		
		for (Resource targetResource : targetSet.getResources()) {
			try {
				targetResource.save(targetSet.getLoadOptions());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return new TransferResult(targetSet, srcTrgMap);
	}
}
