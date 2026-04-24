package tools.mdsd.jamopp.test.performance;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;
import org.eclipse.jgit.api.errors.GitAPIException;

import tools.mdsd.jamopp.model.java.JavaClasspath;
import tools.mdsd.jamopp.resource.JavaResource2Factory;
import tools.mdsd.jamopp.test.performance.monitor.MemoryMonitor;
import tools.mdsd.jamopp.test.performance.stepwise.StepwisePerformanceExecutor;

public final class PerformanceTestStandaloneMain {
    public final static Path DEFAULT_INPUT_PATH = Paths.get("target", "src-bulk", "TeaStore");
    public final static Path DEFAULT_OUTPUT_PATH = Paths.get("target", "tests", "output_performance");
    private final static Path MAIN_ROOT_PATH = Paths.get("jamopp.tests").toAbsolutePath();
    private final static String OPTION_NAME_STEPWISE_TEST = "stepwise";
    private final static String OPTION_NAME_FULL_TEST = "full";

    private PerformanceTestStandaloneMain() {}

    public static void setupRegistries() {
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("java", new JavaResource2Factory());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		JavaClasspath.get().clear();
		JavaClasspath.get().registerStdLib();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("json", new JsonResourceFactory());
    }

    public static void main(String[] args) {
        var actualOutputDirectory = MAIN_ROOT_PATH.resolve(DEFAULT_OUTPUT_PATH);
        var memoryMonitor = new MemoryMonitor(actualOutputDirectory.resolve("mem.txt"));
        memoryMonitor.initialize();

        setupRegistries();

        if (args.length == 1 && args[0].equals(OPTION_NAME_STEPWISE_TEST)) {
            StepwisePerformanceExecutor executor = new StepwisePerformanceExecutor();
            try {
                executor.measurePerformance("teastore-stepwise", MAIN_ROOT_PATH.resolve(DEFAULT_INPUT_PATH),
                    actualOutputDirectory);
                memoryMonitor.stop();
                memoryMonitor.readDataAndCreateChart();
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
            return;
        }

        PerformanceTestExecutor executor = new PerformanceTestExecutor(MAIN_ROOT_PATH.resolve(DEFAULT_INPUT_PATH),
            actualOutputDirectory);
        if (!(args.length == 1 && args[0].equals(OPTION_NAME_FULL_TEST))) {
            executor.setNumberOfRepetitions(1);
        }

        try {
            executor.setupTestEnvironment();
            executor.measureTeaStoreSecondVariant();
            executor.measureTeaStoreWithOneLevelResolution();
            executor.measureTeaStoreFullResolution();
            executor.cleanEverything();
            memoryMonitor.stop();
            memoryMonitor.readDataAndCreateChart();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
