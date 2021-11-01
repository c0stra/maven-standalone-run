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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ArgParserTest {

    private final List<Object[]> data = new ArrayList<>();

    {
        parsingOf("").shouldReturn();
        parsingOf("a").shouldReturn("a");
        parsingOf("a b").shouldReturn("a", "b");
        parsingOf("'a b'").shouldReturn("a b");
        parsingOf("\"a b\"").shouldReturn("a b");
    }

    @DataProvider
    public Iterator<Object[]> data() {
        return data.iterator();
    }

    @Test(dataProvider = "data")
    public void parsingTest(Parameters parameters) {
        Assert.assertEquals(ArgParser.parse(parameters.commandLine), parameters.expectedArgs);
    }

    private Def parsingOf(String commandLine) {
        return expectedArgs -> data.add(new Object[]{new Parameters(commandLine, expectedArgs)});
    }

    interface Def {
        void shouldReturn(String... expectedArgs);
    }

    public static class Parameters {
        private final String commandLine;
        private final String[] expectedArgs;

        public Parameters(String commandLine, String[] expectedArgs) {
            this.commandLine = commandLine;
            this.expectedArgs = expectedArgs;
        }

        @Override
        public String toString() {
            return "parsing of \"" + commandLine + "\" should return " + Arrays.deepToString(expectedArgs);
        }
    }
}
