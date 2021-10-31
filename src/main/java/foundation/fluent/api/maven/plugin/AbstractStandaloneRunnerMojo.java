package foundation.fluent.api.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.repository.RepositorySystem;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.stream.Collectors.toMap;

public abstract class AbstractStandaloneRunnerMojo extends AbstractMojo {

    @Component
    RepositorySystem system;

    @Parameter(defaultValue = "${session}", readonly = true)
    MavenSession session;

    @Parameter(property = "artifact", readonly = true)
    String artifact;

    abstract void run(ClassLoader classLoader, Map<String, String> artifact) throws Throwable;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        AtomicReference<MojoExecutionException> mojoError = new AtomicReference<>();
        ArtifactResolutionResult result = resolveArtifact(artifact);
        ClassLoader classLoader = classLoaderFor(result.getArtifacts());
        Map<String, String> jarMap = result.getArtifacts().stream().collect(toMap(
                a -> a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion(),
                a -> a.getFile().getAbsolutePath()
        ));
        Thread thread = new Thread(new MojoRunnable(this, mojoError, classLoader, jarMap));
        thread.setName("Standalone plugin thread");
        thread.setContextClassLoader(classLoader);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MojoExecutionException throwable = mojoError.get();
        if(throwable != null) {
            throw throwable;
        }
    }

    private ClassLoader classLoaderFor(Set<Artifact> artifacts) {
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

    private Dependency dependency(String coordinates) throws MojoExecutionException {
        if(coordinates == null)
            throw new MojoExecutionException("Artifact coordinates not set");
        String[] parts = coordinates.split(":");
        if(parts.length != 3)
            throw new MojoExecutionException("Invalid coordinates: " + coordinates + ". Expected format: groupId:artifactId:version");
        Dependency dependency = new Dependency();
        dependency.setGroupId(parts[0]);
        dependency.setArtifactId(parts[1]);
        dependency.setVersion(parts[2]);
        return dependency;
    }

    private ArtifactResolutionResult resolveArtifact(String coordinates) throws MojoExecutionException {
        Artifact mainArtifact = system.createDependencyArtifact(dependency(coordinates));
        if(mainArtifact.isSnapshot() && !Boolean.getBoolean("allowSnapshot"))
            throw new MojoExecutionException("Snapshot artifact not allowed, but provided artifact " + mainArtifact + " is snapshot.");
        getLog().debug("Resolving dependencies for: " + mainArtifact);
        ArtifactResolutionResult result = resolveArtifact(mainArtifact);
        getLog().debug("Resolved dependencies for: " + mainArtifact);
        handleMissingArtifacts(result);
        return result;
    }

    private void handleMissingArtifacts(ArtifactResolutionResult result) throws MojoExecutionException {
        if(result.hasMissingArtifacts()) {
            for(Artifact missing : result.getMissingArtifacts()) {
                getLog().error("Unable to resolve: " + missing);
            }
            throw new MojoExecutionException("Unable to resolve dependencies: " + result.getMissingArtifacts());
        }
    }

    private ArtifactResolutionResult resolveArtifact(Artifact artifact) {
        return system.resolve(new ArtifactResolutionRequest()
                .setArtifact(artifact)
                .setResolveTransitively(true)
                .setLocalRepository(session.getLocalRepository())
                .setRemoteRepositories(session.getRequest().getRemoteRepositories())
                .setManagedVersionMap(session.getCurrentProject().getManagedVersionMap())
                .setResolveRoot(true));
    }
}
