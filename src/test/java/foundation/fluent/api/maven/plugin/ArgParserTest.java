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
