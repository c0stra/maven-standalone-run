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

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static foundation.fluent.api.maven.plugin.RunnerUtils.invoke;

@Mojo(name = "testng", requiresProject = false)
public class TestNGRunnerMojo extends AbstractStandaloneRunnerMojo {

    private static final String testNGClassName = "org.testng.TestNG";
    private static final String privateMain = "privateMain";
    private static final String iTestListenerClassName = "org.testng.ITestListener";

    @Override
    void run(ClassLoader classLoader, Map<String, String> jarMap) throws Throwable {
        String[] args = augmentArgs(this.args, artifact, jarMap);
        Class<?> iTestListenerClass = classLoader.loadClass(iTestListenerClassName);
        Object listener = null; //classLoader.loadClass("org.testng.reporters.TextListener").getConstructor().newInstance();
        getLog().info("Invoking TestNG with parameters: " + Arrays.deepToString(args));
        Object testNG = invoke(classLoader.loadClass(testNGClassName).getMethod(privateMain, String[].class, iTestListenerClass), null, args, listener);
        handleResult((int) invoke(testNG.getClass().getMethod("getStatus"), testNG));
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
