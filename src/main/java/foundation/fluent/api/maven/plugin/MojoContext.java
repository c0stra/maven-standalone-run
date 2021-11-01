package foundation.fluent.api.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MojoContext {

    private final AbstractStandaloneRunnerMojo mojo;
    private final AtomicReference<MojoExecutionException> mojoError = new AtomicReference<>();
    private final Map<String, String> artifactToJarMap;
    private final Log log;

    public MojoContext(AbstractStandaloneRunnerMojo mojo, Map<String, String> artifactToJarMap, Log log) {
        this.mojo = mojo;
        this.artifactToJarMap = artifactToJarMap;
        this.log = log;
    }

    public void execute(ClassLoader classLoader) throws MojoExecutionException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    mojo.run(getContextClassLoader(), artifactToJarMap);
                } catch (MojoExecutionException e) {
                    mojoError.set(e);
                } catch (Throwable e) {
                    e.printStackTrace();
                    mojoError.set(new MojoExecutionException("Execution failed with " + e, e));
                }
            }
        };
        thread.setName("Standalone plugin thread");
        thread.setContextClassLoader(classLoader);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MojoExecutionException throwable = mojoError.get();
        if(throwable != null) {
            throw throwable;
        }
    }

}
