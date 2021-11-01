package foundation.fluent.api.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;

public abstract class AbstractStandaloneRunnerMojo extends AbstractStandaloneMojoBase {

    @Parameter(property = "args")
    protected String args;

    abstract void run(ClassLoader classLoader, Map<String, String> artifact) throws Throwable;

    @Override
    public void execute() throws MojoExecutionException {
        ArtifactResolutionResult result = resolveArtifact(artifact);
        new MojoContext(this, getArtifactJars(result), getLog()).execute(classLoaderFor(result.getArtifacts()));
    }

    private ClassLoader classLoaderFor(Collection<Artifact> artifacts) {
        URL[] urls = new URL[artifacts.size()];
        int i = 0;
        getLog().debug("Adding to classpath: ");
        for(Artifact artifact : artifacts) try {
            urls[i] = artifact.getFile().toURI().toURL();
            getLog().debug("" + urls[i]);
            i++;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return URLClassLoader.newInstance(urls);
    }

}
