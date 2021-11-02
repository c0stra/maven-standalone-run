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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

/**
 * As maven cannot simply accept some remainder as free form command line arguments to be passed to it's goal,
 * the arguments must be passed within a system property (-Dargs=parameters). So the plugins will receive such
 * parameters within one system property, and they need to be properly escaped and parsed, taking the escaping
 * into account.
 *
 * The parser recognizes space delimited parameters. To allow special characters, it allows double/single quoting
 * and escaping a single character by backslash.
 *
 * The parser uses very simple recursive descent algorithm.
 */
public class ArgParser {

    private static final String[] emptyString = {};

    private ArgParser() {}

    public static Stream<String> parse(String commandLine) {
        if(commandLine == null)
            return Stream.empty();
        List<StringBuilder> result = new ArrayList<>();
        StringReader reader = new StringReader(commandLine);
        try {
            while(parseArg(reader, result)) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.stream().map(Object::toString);
    }


    private static boolean parseArg(StringReader input, List<StringBuilder> result) throws IOException {
        for(int c = input.read(); c > 0; c = input.read()) {
            if(!Character.isSpaceChar(c)) switch(c) {
                case '"': return parseValue(input.read(), input, result, i -> i == '"');
                case '\'': return parseValue(input.read(), input, result, i -> i == '\'');
                default: return parseValue(c, input, result, Character::isSpaceChar);
            }
        }
        return false;
    }

    private static boolean parseValue(int c, StringReader reader, List<StringBuilder> result, IntPredicate end) throws IOException {
        StringBuilder builder = new StringBuilder();
        result.add(builder);
        while(!end.test(c)) {
            if(c <= 0) return false;
            if(c == '\\') c = reader.read();
            builder.append((char) c);
            c = reader.read();
        }
        return true;
    }
}
