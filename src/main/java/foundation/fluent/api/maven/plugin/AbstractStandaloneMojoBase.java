/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2021, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package foundation.fluent.api.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.repository.RepositorySystem;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
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
        if(mainArtifact.isSnapshot() && !allowSnapshot)
            throw new MojoExecutionException("Snapshot artifact not allowed, but provided artifact " + mainArtifact + " is snapshot.");
        getLog().debug("Resolving dependencies for: " + mainArtifact);
        ArtifactResolutionResult result = resolveArtifact(mainArtifact);
        getLog().debug("Resolved dependencies for: " + mainArtifact);
        handleMissingArtifacts(result);
        return result;
    }

    private static Dependency dependency(String coordinates) throws MojoExecutionException {
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

    public URL[] classPathUrls(Collection<Artifact> artifacts) {
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
        return urls;
    }

    protected ClassLoader classLoaderFor(Collection<Artifact> artifacts) {
        return URLClassLoader.newInstance(classPathUrls(artifacts));
    }

}
