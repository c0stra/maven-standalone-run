package foundation.fluent.api.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static foundation.fluent.api.maven.plugin.RunnerUtils.invoke;

@Mojo(name = "main", requiresProject = false)
public class MainRunnerMojo extends AbstractStandaloneRunnerMojo {

    private static final String main = "main";

    @Parameter(property = "mainClass")
    private String mainClass;

    @Override
    void run(ClassLoader classLoader, Map<String, String> artifactJarMap) throws Throwable {
        String[] commandLineArgs = ArgParser.parse(args);
        if(mainClass == null) {
            mainClass = getMainClassFromManifest(artifactJarMap.getOrDefault(artifact, artifact));
        }
        getLog().info("Invoking: " + mainClass + "." + main + " with parameters: " + Arrays.deepToString(commandLineArgs));
        invoke(classLoader.loadClass(mainClass).getMethod(main, String[].class), null, (Object) commandLineArgs);
    }


    private String getMainClassFromManifest(String jar) throws IOException, MojoExecutionException {
        JarURLConnection jarURLConnection = (JarURLConnection) new URL("jar:file:" + jar + "!/").openConnection();
        Manifest manifest = jarURLConnection.getManifest();
        Attributes attributes = manifest.getMainAttributes();
        String mainClass = attributes.getValue("Main-Class");
        if(mainClass == null) {
            throw new MojoExecutionException("Main class not provided, and not defined in manifext of " + jar + ". Try specifying main class explicitly via -DmainClass={your main class}");
        }
        return mainClass;
    }
}
