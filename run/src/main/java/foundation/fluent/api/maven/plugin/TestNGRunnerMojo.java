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

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

@Mojo(name = "testng", requiresProject = false)
public class TestNGRunnerMojo extends AbstractStandaloneRunnerMojo {

    private static final String testNGClassName = "org.testng.TestNG";

    @Override
    void run(ClassLoader classLoader, Map<String, String> jarMap) throws Throwable {
        handleResult(run(classLoader, testNGClassName, augmentArgs(artifact, jarMap)));
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

    private String[] augmentArgs(String artifact, Map<String, String> jarMap) {
        String[] args = defaultArgs(args(), artifact);
        for(int i = 0; i < args.length - 1; i++) {
            if("-testjar".equals(args[i])) {
                args[i+1] = resolve(jarMap, args[i+1]);
            }
        }
        return args;
    }

    static String[] defaultArgs(String[] args, String artifact) {
        return needsTestJar(args) ? concat(Stream.of(args), Stream.of("-testjar", withoutVersion(artifact))).toArray(String[]::new) : args;
    }

    private static final Set<String> scope = Stream.of(
            "-testclass",
            "-methods"
    ).collect(toSet());

    private static boolean needsTestJar(String[] args) {
        int nonIgnored = 0;
        for(int i = 0; i < args.length; i++) {
            if(scope.contains(args[i]) || !args[i].startsWith("-")) {
                nonIgnored++;
            } else {
                i++;
            }
        }
        Set<String> argsSet = Stream.of(args).collect(toSet());
        return !argsSet.contains("-testjar") && (argsSet.contains("-xmlpathinjar") || nonIgnored == 0);
    }

    private static String withoutVersion(String artifact) {
        return Stream.of(artifact.split(":")).limit(2).collect(joining(":"));
    }

    /*
Example of TestNG usage:

You need to specify at least one testng.xml, one class or one method
Usage: <main class> [options] The XML suite files to run
  Options:
    -alwaysrunlisteners
      Should MethodInvocation Listeners be run even for skipped methods
      Default: true
    -configfailurepolicy
      Configuration failure policy (skip or continue)
    -d
      Output directory
    -dataproviderthreadcount
      Number of threads to use when running data providers
    -dependencyinjectorfactory
      The dependency injector factory implementation that TestNG should use.
    -excludegroups
      Comma-separated list of group names to  exclude
    -failwheneverythingskipped
      Should TestNG fail execution if all tests were skipped and nothing was
      run.
      Default: false
    -groups
      Comma-separated list of group names to be run
    -junit
      JUnit mode
      Default: false
    -listener
      List of .class files or list of class names implementing ITestListener
      or ISuiteListener
    -methods
      Comma separated of test methods
      Default: []
    -methodselectors
      List of .class files or list of class names implementing IMethodSelector
    -mixed
      Mixed mode - autodetect the type of current test and run it with
      appropriate runner
      Default: false
    -objectfactory
      List of .class files or list of class names implementing
      ITestRunnerFactory
    -overrideincludedmethods
      Comma separated fully qualified class names of listeners that should be
      skipped from being wired in via Service Loaders.
      Default: false
    -parallel
      Parallel mode (methods, tests or classes)
      Possible Values: [tests, methods, classes, instances, none]
    -port
      The port
    -reporter
      Extended configuration for custom report listener
    -spilistenerstoskip
      Comma separated fully qualified class names of listeners that should be
      skipped from being wired in via Service Loaders.
      Default: <empty string>
    -suitename
      Default name of test suite, if not specified in suite definition file or
      source code
    -suitethreadpoolsize
      Size of the thread pool to use to run suites
      Default: 1
    -testclass
      The list of test classes
    -testjar
      A jar file containing the tests
    -testname
      Default name of test, if not specified in suitedefinition file or source
      code
    -testnames
      The list of test names to run
    -testrunfactory, -testRunFactory
      The factory used to create tests
    -threadcount
      Number of threads to use when running tests in parallel
    -threadpoolfactoryclass
      The threadpool executor factory implementation that TestNG should use.
    -usedefaultlisteners
      Whether to use the default listeners
      Default: true
    -log, -verbose
      Level of verbosity
    -xmlpathinjar
      The full path to the xml file inside the jar file (only valid if
      -testjar was specified)
      Default: testng.xml
     */
}
