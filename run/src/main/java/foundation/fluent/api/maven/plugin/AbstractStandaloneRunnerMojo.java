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

import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import static foundation.fluent.api.maven.plugin.ArgParser.parse;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

public abstract class AbstractStandaloneRunnerMojo extends AbstractStandaloneMojoBase {

    public static final String main = "main";

    @Parameter(property = "args")
    protected String args;

    @Parameter(property = "argFile")
    protected String argFile;

    abstract void run(ClassLoader classLoader, Map<String, String> artifact) throws Throwable;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ArtifactResolutionResult result = resolveArtifact(artifact);
        Map<String, String> artifactJars = getArtifactJars(result);
        getLog().debug("Artifact to jar mapping:\n" + artifactJars.entrySet().stream().map(e -> e.getKey() + " -> " + e.getValue()).collect(joining("\n")));
        new MojoContext(this, artifactJars).execute(classLoaderFor(result.getArtifacts()));
    }

    protected String[] args() {
        try {
            return concat(argFile == null ? empty() : Files.lines(Paths.get(argFile)), parse(args)).toArray(String[]::new);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    protected int run(ClassLoader classLoader, String mainClass, String... commandLineArgs) throws Throwable {
        getLog().info("Invoking: " + mainClass + "." + main + " with parameters: " + Arrays.deepToString(commandLineArgs));
        SecurityManager securityManager = System.getSecurityManager();
        System.setSecurityManager(new ExitCodeExtractor(securityManager));
        try {
            classLoader.loadClass(mainClass).getMethod(main, String[].class).invoke(null, (Object) commandLineArgs);
        } catch (ExitCodeExtractor.ExitCodeException e) {
            return e.getStatus();
        } catch (InvocationTargetException e) {
            if(e.getTargetException() instanceof ExitCodeExtractor.ExitCodeException) {
                return ((ExitCodeExtractor.ExitCodeException) e.getTargetException()).getStatus();
            }
            throw e.getTargetException();
        } finally {
            System.setSecurityManager(securityManager);
        }
        return 0;
    }

    protected String resolve(Map<String, String> artifactJars, String artifact) {
        getLog().debug("Resolving jar for: " + artifact);
        String jar = artifactJars.getOrDefault(artifact, artifact);
        getLog().debug(artifact + " -> " + jar);
        return jar;
    }

}
