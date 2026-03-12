package tools.mdsd.jamopp.test.performance;

import java.io.IOException;
import java.nio.file.Paths;

public final class PerformanceTestStandaloneMain {
    private PerformanceTestStandaloneMain() {}

    public static void main(String[] args) {
        PerformanceTestExecutor executor = new PerformanceTestExecutor(Paths.get(""), Paths.get(""));
        try {
            executor.setupTestEnvironment();
            executor.measureTeaStoreSecondVariant();
            executor.measureTeaStoreWithOneLevelResolution();
            executor.measureTeaStoreFullResolution();
            executor.cleanEverything();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
