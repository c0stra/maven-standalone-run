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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static foundation.fluent.api.maven.plugin.ArgParser.parse;

/**
 * Maven goal to run a main class from a provided maven artifact (or it's dependencies).
 *
 * One doesn't have to have any local pom file or sources. This plugin doesn't require a project. It will simply
 * fetch the artifact jar and all it's dependencies directly from maven repository, and invoke a main method,
 * either provided explicitly via parameter -DmainClass or defined in the manifest of the jar.
 */
@Mojo(name = "main", requiresProject = false, requiresDirectInvocation = true)
public class MainRunnerMojo extends AbstractStandaloneRunnerMojo {

    @Parameter(property = "mainClass")
    private String mainClass;

    @Override
    void run(ClassLoader classLoader, Map<String, String> artifactJarMap) throws Throwable {
        if(mainClass == null) {
            mainClass = getMainClassFromManifest(artifactJarMap.getOrDefault(artifact, artifact));
        }
        int exitCode = run(classLoader, mainClass, parse(args));
        if(exitCode != 0)
            throw new MojoFailureException(mainClass + "." + main + " exited with exit code " + exitCode);
    }


    private static String getMainClassFromManifest(String jar) throws IOException, MojoExecutionException {
        JarURLConnection jarURLConnection = (JarURLConnection) new URL("jar:file:" + jar + "!/").openConnection();
        Manifest manifest = jarURLConnection.getManifest();
        Attributes attributes = manifest.getMainAttributes();
        String mainClass = attributes.getValue("Main-Class");
        if(mainClass == null) {
            throw new MojoExecutionException("Main class not provided, and not defined in manifest of " + jar + ". Try specifying main class explicitly via -DmainClass={your main class}");
        }
        return mainClass;
    }
}
