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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public abstract class AbstractStandaloneRunnerMojo extends AbstractStandaloneMojoBase {

    public static final String main = "main";

    @Parameter(property = "args")
    protected String args;

    abstract void run(ClassLoader classLoader, Map<String, String> artifact) throws Throwable;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ArtifactResolutionResult result = resolveArtifact(artifact);
        new MojoContext(this, getArtifactJars(result)).execute(classLoaderFor(result.getArtifacts()));
    }

    protected int run(ClassLoader classLoader, String mainClass, String... commandLineArgs) throws Throwable {
        getLog().info("Invoking: " + mainClass + "." + main + " with parameters: " + Arrays.deepToString(commandLineArgs));
        SecurityManager securityManager = System.getSecurityManager();
        System.setSecurityManager(new ExitCodeExtractor());
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
}
