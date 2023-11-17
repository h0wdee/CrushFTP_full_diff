/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh;

import com.maverick.ssh.Shell;
import com.maverick.ssh.ShellMatcher;
import com.maverick.ssh.ShellReader;
import com.maverick.ssh.ShellTimeoutException;
import com.maverick.ssh.ShellWriter;
import com.maverick.ssh.SshException;
import com.maverick.ssh.SshIOException;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShellController
implements ShellReader,
ShellWriter {
    protected Shell shell;
    protected ShellMatcher matcher = null;
    protected int readlimit = 32768;
    protected InputStream in;
    static Logger log = LoggerFactory.getLogger(ShellController.class);

    ShellController(Shell shell, ShellMatcher matcher, InputStream in) {
        this.shell = shell;
        this.matcher = matcher;
        this.in = in;
    }

    public void setMatcher(ShellMatcher matcher) {
        this.matcher = matcher;
    }

    @Override
    public void interrupt() throws IOException {
        this.shell.type(new String(new char[]{'\u0003'}));
    }

    @Override
    public synchronized void type(String string) throws IOException {
        this.shell.type(string);
    }

    @Override
    public synchronized void carriageReturn() throws IOException {
        this.shell.carriageReturn();
    }

    @Override
    public synchronized void typeAndReturn(String string) throws IOException {
        this.shell.typeAndReturn(string);
    }

    public synchronized boolean expect(String pattern) throws ShellTimeoutException, SshException {
        return this.expect(pattern, false, 0L, 0L);
    }

    public synchronized boolean expect(String pattern, boolean consumeRemainingLine) throws ShellTimeoutException, SshException {
        return this.expect(pattern, consumeRemainingLine, 0L, 0L);
    }

    public synchronized boolean expect(String pattern, long timeout) throws ShellTimeoutException, SshException {
        return this.expect(pattern, false, timeout, 0L);
    }

    public synchronized boolean expect(String pattern, boolean consumeRemainingLine, long timeout) throws ShellTimeoutException, SshException {
        return this.expect(pattern, consumeRemainingLine, timeout, 0L);
    }

    public synchronized boolean expectNextLine(String pattern) throws ShellTimeoutException, SshException {
        return this.expect(pattern, false, 0L, 1L);
    }

    public synchronized boolean expectNextLine(String pattern, boolean consumeRemainingLine) throws ShellTimeoutException, SshException {
        return this.expect(pattern, consumeRemainingLine, 0L, 1L);
    }

    public synchronized boolean expectNextLine(String pattern, boolean consumeRemainingLine, long timeout) throws ShellTimeoutException, SshException {
        return this.expect(pattern, consumeRemainingLine, timeout, 1L);
    }

    public synchronized boolean expect(String pattern, boolean consumeRemainingLine, long timeout, long maxLines) throws ShellTimeoutException, SshException {
        StringBuffer line = new StringBuffer();
        long time = System.currentTimeMillis();
        long lines = 0L;
        while (System.currentTimeMillis() - time < timeout || timeout == 0L) {
            if (maxLines > 0L && lines >= maxLines) {
                return false;
            }
            try {
                int ch = this.in.read();
                if (ch == -1) {
                    return false;
                }
                if (ch != 10 && ch != 13) {
                    line.append((char)ch);
                }
                switch (this.matcher.matches(line.toString(), pattern)) {
                    case CONTENT_DOES_NOT_MATCH: {
                        return false;
                    }
                    case CONTENT_MATCHES: {
                        if (log.isDebugEnabled()) {
                            log.debug("Matched: [" + pattern + "] " + line.toString());
                        }
                        if (consumeRemainingLine && ch != 10 && ch != -1) {
                            while (ch != 10 && ch != -1) {
                            }
                        }
                        if (log.isDebugEnabled()) {
                            log.debug("Shell output: " + line.toString());
                        }
                        return true;
                    }
                }
                if (ch != 10) continue;
                ++lines;
                if (log.isDebugEnabled()) {
                    log.debug("Shell output: " + line.toString());
                }
                line.delete(0, line.length());
            }
            catch (SshIOException e) {
                if (e.getRealException().getReason() == 21) continue;
                throw e.getRealException();
            }
            catch (IOException e) {
                throw new SshException(e);
            }
        }
        throw new ShellTimeoutException();
    }

    public boolean isActive() {
        return this.shell.inStartup();
    }

    @Override
    public synchronized String readLine() throws SshException, ShellTimeoutException {
        return this.readLine(0L);
    }

    @Override
    public synchronized String readLine(long timeout) throws SshException, ShellTimeoutException {
        if (!this.isActive()) {
            return null;
        }
        StringBuffer line = new StringBuffer();
        long time = System.currentTimeMillis();
        do {
            try {
                int ch = this.in.read();
                if (ch == -1 || ch == 10) {
                    if (line.length() == 0 && ch == -1) {
                        return null;
                    }
                    return line.toString();
                }
                if (ch == 10 || ch == 13) continue;
                line.append((char)ch);
            }
            catch (SshIOException e) {
                if (e.getRealException().getReason() == 21) continue;
                throw e.getRealException();
            }
            catch (IOException e) {
                throw new SshException(e);
            }
        } while (System.currentTimeMillis() - time < timeout || timeout == 0L);
        throw new ShellTimeoutException();
    }

    public int getReadlimit() {
        return this.readlimit;
    }

    public void setReadlimit(int readlimit) {
        this.readlimit = readlimit;
    }
}

