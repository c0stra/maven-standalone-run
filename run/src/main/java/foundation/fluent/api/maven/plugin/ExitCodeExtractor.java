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

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

public class ExitCodeExtractor extends SecurityManager {

    private final SecurityManager delegate;

    public ExitCodeExtractor(SecurityManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public void checkExit(int status) {
        throw new ExitCodeException(status);
    }

    @Override
    public void checkPermission(Permission perm) {
        if(delegate != null)
            delegate.checkPermission(perm);
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if(delegate != null)
            delegate.checkPermission(perm, context);
    }

    @Override
    public void checkCreateClassLoader() {
        if(delegate != null)
            delegate.checkCreateClassLoader();
    }

    @Override
    public void checkAccess(Thread t) {
        if(delegate != null)
            delegate.checkAccess(t);
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        if(delegate != null)
            delegate.checkAccess(g);
    }

    @Override
    public void checkExec(String cmd) {
        if(delegate != null)
            delegate.checkExec(cmd);
    }

    @Override
    public void checkLink(String lib) {
        if(delegate != null)
            delegate.checkLink(lib);
    }

    @Override
    public void checkRead(FileDescriptor fd) {
        if(delegate != null)
            delegate.checkRead(fd);
    }

    @Override
    public void checkRead(String file) {
        if(delegate != null)
            delegate.checkRead(file);
    }

    @Override
    public void checkRead(String file, Object context) {
        if(delegate != null)
            delegate.checkRead(file, context);
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if(delegate != null)
            delegate.checkWrite(fd);
    }

    @Override
    public void checkWrite(String file) {
        if(delegate != null)
            delegate.checkWrite(file);
    }

    @Override
    public void checkDelete(String file) {
        if(delegate != null)
            delegate.checkDelete(file);
    }

    @Override
    public void checkConnect(String host, int port) {
        if(delegate != null)
            delegate.checkConnect(host, port);
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        if(delegate != null)
            delegate.checkConnect(host, port, context);
    }

    @Override
    public void checkListen(int port) {
        if(delegate != null)
            delegate.checkListen(port);
    }

    @Override
    public void checkAccept(String host, int port) {
        if(delegate != null)
            delegate.checkAccept(host, port);
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        if(delegate != null)
            delegate.checkMulticast(maddr);
    }

    @Override
    public void checkMulticast(InetAddress maddr, byte ttl) {
        if(delegate != null)
            delegate.checkMulticast(maddr, ttl);
    }

    @Override
    public void checkPropertiesAccess() {
        if(delegate != null)
            delegate.checkPropertiesAccess();
    }

    @Override
    public void checkPropertyAccess(String key) {
        if(delegate != null)
            delegate.checkPropertyAccess(key);
    }

    @Override
    public boolean checkTopLevelWindow(Object window) {
        if(delegate != null)
            return delegate.checkTopLevelWindow(window);
        else
            return true;
    }

    @Override
    public void checkPrintJobAccess() {
        if(delegate != null)
            delegate.checkPrintJobAccess();
    }

    @Override
    public void checkSystemClipboardAccess() {
        if(delegate != null)
            delegate.checkSystemClipboardAccess();
    }

    @Override
    public void checkAwtEventQueueAccess() {
        if(delegate != null)
            delegate.checkAwtEventQueueAccess();
    }

    @Override
    public void checkPackageAccess(String pkg) {
        if(delegate != null)
            delegate.checkPackageAccess(pkg);
    }

    @Override
    public void checkPackageDefinition(String pkg) {
        if(delegate != null)
            delegate.checkPackageDefinition(pkg);
    }

    @Override
    public void checkSetFactory() {
        if(delegate != null)
            delegate.checkSetFactory();
    }

    @Override
    public void checkMemberAccess(Class<?> clazz, int which) {
        if(delegate != null)
            delegate.checkMemberAccess(clazz, which);
    }

    @Override
    public void checkSecurityAccess(String target) {
        if(delegate != null)
            delegate.checkSecurityAccess(target);
    }

    public static class ExitCodeException extends SecurityException {
        private final int status;
        public ExitCodeException(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

}
