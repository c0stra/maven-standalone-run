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

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public abstract class AbstractStandaloneMojoBase extends AbstractMojo {

    @Component
    RepositorySystem system;

    @Parameter(defaultValue = "${session}", readonly = true)
    MavenSession session;

    @Parameter(property = "artifact", readonly = true, required = true)
    String artifact;

    @Parameter(property = "allowSnapshot")
    boolean allowSnapshot;

    public static Map<String, String> getArtifactJars(ArtifactResolutionResult artifact) {
        return artifact.getArtifacts().stream().collect(toMap(
                a -> a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion(),
                a -> a.getFile().getAbsolutePath()
        ));
    }

    public ArtifactResolutionResult resolveArtifact(String coordinates) throws MojoExecutionException {
        Artifact mainArtifact = system.createDependencyArtifact(dependency(coordinates));
        if(mainArtifact.isSnapshot() && !Boolean.getBoolean("allowSnapshot"))
            throw new MojoExecutionException("Snapshot artifact not allowed, but provided artifact " + mainArtifact + " is snapshot.");
        getLog().debug("Resolving dependencies for: " + mainArtifact);
        ArtifactResolutionResult result = resolveArtifact(mainArtifact);
        getLog().debug("Resolved dependencies for: " + mainArtifact);
        handleMissingArtifacts(result);
        return result;
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
