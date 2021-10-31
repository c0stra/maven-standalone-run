package foundation.fluent.api.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class MojoRunnable implements Runnable {

    private final AbstractStandaloneRunnerMojo mojo;
    private final AtomicReference<MojoExecutionException> mojoError;
    private final ClassLoader mojoClassLoader;
    private final Map<String, String> artifactToJarMap;

    public MojoRunnable(AbstractStandaloneRunnerMojo mojo, AtomicReference<MojoExecutionException> mojoError, ClassLoader mojoClassLoader, Map<String, String> artifactToJarMap) {
        this.mojo = mojo;
        this.mojoError = mojoError;
        this.mojoClassLoader = mojoClassLoader;
        this.artifactToJarMap = artifactToJarMap;
    }

    @Override
    public void run() {
        try {
            mojo.run(mojoClassLoader, artifactToJarMap);
        } catch (MojoExecutionException e) {
            mojoError.set(e);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            targetException.printStackTrace();
            StackTraceElement methodOnStack = findMethodOnStack(targetException);
            mojoError.set(new MojoExecutionException("Invocation of method " + methodOnStack.getClassName() + "." + methodOnStack.getMethodName() + " failed, throwing: " + targetException, e));
        } catch (Throwable e) {
            e.printStackTrace();
            mojoError.set(new MojoExecutionException("Execution failed with " + e, e));
        }
    }

    private StackTraceElement findMethodOnStack(Throwable throwable) {
        List<String> end = Stream.of(
                getClass().getPackage().getName(),
                mojo.getClass().getPackage().getName(),
                "java.lang.reflect",
                "sun.reflect",
                Thread.class.getName()
        ).collect(toList());
        int length = throwable.getStackTrace().length;
        return range(0, length)
                .mapToObj(i -> throwable.getStackTrace()[length - 1 - i])
                .filter(e -> end.stream().noneMatch(e.getClassName()::startsWith))
                .findFirst().orElse(null);
    }

}
