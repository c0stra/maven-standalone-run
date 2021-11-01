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
