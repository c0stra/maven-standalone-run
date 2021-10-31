package foundation.fluent.api.maven.plugin;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "testng", requiresProject = false)
public class TestNGRunnerMojo extends AbstractStandaloneRunnerMojo {

    private static final String testNGClassName = "org.testng.TestNG";
    private static final String privateMain = "privateMain";
    private static final String iTestListenerClassName = "org.testng.ITestListener";

    @Parameter(property = "args")
    private String args;

    @Override
    void run(ClassLoader classLoader, Map<String, String> jarMap) throws Throwable {
        String[] args = augmentArgs(this.args, artifact, jarMap);
        Class<?> testNGClass = classLoader.loadClass(testNGClassName);
        Class<?> iTestListenerClass = classLoader.loadClass(iTestListenerClassName);
        getLog().info("Invoking TestNG with parameters: " + Arrays.deepToString(args));
        Object testNG = testNGClass.getMethod(privateMain, String[].class, iTestListenerClass).invoke(null, args, null);
        handleResult((int) testNG.getClass().getMethod("getStatus").invoke(testNG));
    }

    private void handleResult(int status) throws MojoFailureException {
        getLog().info("TestNG ended with exit code " + status);
        switch (status) {
            case 1: throw new MojoFailureException("There were failed tests.");
            case 2: throw new MojoFailureException("There were skipped tests.");
            default: throw new MojoFailureException("There were failed and skipped tests.");
            case 0: getLog().info("All tests passed.");
        }
    }

    private String[] augmentArgs(String commandLineArgs, String artifact, Map<String, String> jarMap) {
        String[] args = ArgParser.parse(commandLineArgs);
        Set<String> argsSet = Stream.of(args).collect(Collectors.toSet());
        if((argsSet.contains("-xmlpathinjar") && !argsSet.contains("-testjar")) || argsSet.isEmpty()) {
            args = Stream.concat(Stream.of(args), Stream.of("-testjar", artifact)).toArray(String[]::new);
        }
        for(int i = 0; i < args.length - 1; i++) {
            if("-testjar".equals(args[i])) {
                args[i+1] = jarMap.getOrDefault(args[i+1], args[i+1]);
            }
        }
        return args;
    }

}
