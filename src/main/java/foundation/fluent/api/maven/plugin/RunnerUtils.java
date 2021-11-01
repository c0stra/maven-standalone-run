package foundation.fluent.api.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RunnerUtils {

    public static Object invoke(Method method, Object on, Object... args) throws IllegalAccessException, MojoExecutionException {
        try {
            return method.invoke(on, args);
        } catch (InvocationTargetException e) {
            throw new MojoExecutionException("Invocation of method " + method.getDeclaringClass().getCanonicalName() + "." + method.getName() + " failed, throwing: " + e.getTargetException(), e);
        }
    }

}
