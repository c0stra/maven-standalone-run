package foundation.fluent.api.maven.plugin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;

public class ArgParser {

    private static final String[] emptyString = {};

    private ArgParser() {}

    public static String[] parse(String commandLine) {
        if(commandLine == null)
            return emptyString;
        List<StringBuilder> result = new ArrayList<>();
        StringReader reader = new StringReader(commandLine);
        try {
            while(parseArg(reader, result)) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.stream().map(Object::toString).toArray(String[]::new);
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
