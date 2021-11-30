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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static org.testng.Assert.*;

public class TestNGRunnerMojoTest {

    @Test(dataProvider = "testJarNeeded")
    public void whenTestJarNeededThenItShouldBeAdded(String[] args, String[] expected) {
        assertEquals(TestNGRunnerMojo.defaultArgs(args, "com.example:project:1.0"), expected);
    }

    @Test(dataProvider = "testJarNotNeeded")
    public void whenTestJarNotNeededThenItShouldNotBeAdded(String[] args) {
        assertEquals(TestNGRunnerMojo.defaultArgs(args, "com.example:project:1.0"), args);
    }


    @DataProvider
    public static Object[][] testJarNeeded() {
        return new Object[][]{
                adding(),
                adding("-listener", "com.example.Listener"),
                adding("-xmlpathinjar", "other.xml")
        };
    }

    @DataProvider
    public static Object[][] testJarNotNeeded() {
        return new Object[][]{
                asIs("suite.xml"),
                asIs("-testclass", "com.example.Test"),
                asIs("-methods", "com.example.Test.method"),
                asIs("-testjar", "com.example:other")
        };
    }


    public static Object[] asIs(String... a) {
        return new Object[]{a};
    }

    public static Object[] adding(String... a) {
        return new Object[]{a, Stream.concat(Stream.of(a), Stream.of("-testjar", "com.example:project")).toArray(String[]::new)};
    }
}
