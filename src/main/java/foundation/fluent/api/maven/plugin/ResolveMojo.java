package foundation.fluent.api.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.function.BiConsumer;

@Mojo(name = "resolve", requiresProject = false)
public class ResolveMojo extends AbstractStandaloneMojoBase {

    @Override
    public void execute() throws MojoExecutionException {
        getArtifactJars(resolveArtifact(artifact)).forEach(
                new BiConsumer<String, String>() {
                    @Override
                    public void accept(String artifact, String jar) {
                        System.out.println("Resolved: " + artifact + " -> " + jar);
                    }
                }
        );
    }

}
