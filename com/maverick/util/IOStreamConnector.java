/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.Vector;

public class IOStreamConnector {
    private InputStream in = null;
    private OutputStream out = null;
    private Thread thread;
    private long bytes;
    private boolean closeInput = true;
    private boolean closeOutput = true;
    boolean running = false;
    boolean closed = false;
    Throwable lastError;
    public static final int DEFAULT_BUFFER_SIZE = 32768;
    int BUFFER_SIZE = 32768;
    protected Vector<IOStreamConnectorListener> listenerList = new Vector();

    public IOStreamConnector() {
    }

    public IOStreamConnector(InputStream in, OutputStream out) {
        this.connect(in, out);
    }

    public void close() {
        if (this.thread == null) {
            this.closed = true;
        }
        this.running = false;
        if (this.thread != null) {
            this.thread.interrupt();
        }
    }

    public Throwable getLastError() {
        return this.lastError;
    }

    public void setCloseInput(boolean closeInput) {
        this.closeInput = closeInput;
    }

    public void setCloseOutput(boolean closeOutput) {
        this.closeOutput = closeOutput;
    }

    public void setBufferSize(int numbytes) {
        if (numbytes <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than zero!");
        }
        this.BUFFER_SIZE = numbytes;
    }

    public void connect(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
        this.thread = new Thread(new IOStreamConnectorThread());
        this.thread.setDaemon(true);
        this.thread.setName("IOStreamConnector " + in.toString() + ">>" + out.toString());
        this.thread.start();
    }

    public long getBytes() {
        return this.bytes;
    }

    public boolean isClosed() {
        return this.closed;
    }

    public void addListener(IOStreamConnectorListener l) {
        this.listenerList.addElement(l);
    }

    public void removeListener(IOStreamConnectorListener l) {
        this.listenerList.removeElement(l);
    }

    public static interface IOStreamConnectorListener {
        public void connectorClosed(IOStreamConnector var1);

        public void connectorTimeout(IOStreamConnector var1);

        public void dataTransfered(byte[] var1, int var2);
    }

    class IOStreamConnectorThread
    implements Runnable {
        IOStreamConnectorThread() {
        }

        @Override
        public void run() {
            byte[] buffer = new byte[IOStreamConnector.this.BUFFER_SIZE];
            int read = 0;
            IOStreamConnector.this.running = true;
            while (IOStreamConnector.this.running) {
                try {
                    read = IOStreamConnector.this.in.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        IOStreamConnector.this.out.write(buffer, 0, read);
                        IOStreamConnector.this.bytes = IOStreamConnector.this.bytes + (long)read;
                        IOStreamConnector.this.out.flush();
                        for (int i = 0; i < IOStreamConnector.this.listenerList.size(); ++i) {
                            IOStreamConnector.this.listenerList.elementAt(i).dataTransfered(buffer, read);
                        }
                        continue;
                    }
                    if (read >= 0) continue;
                    IOStreamConnector.this.running = false;
                }
                catch (InterruptedIOException ex) {
                    for (int i = 0; i < IOStreamConnector.this.listenerList.size(); ++i) {
                        IOStreamConnector.this.listenerList.elementAt(i).connectorTimeout(IOStreamConnector.this);
                    }
                }
                catch (Throwable ioe) {
                    if (!IOStreamConnector.this.running) continue;
                    IOStreamConnector.this.lastError = ioe;
                    IOStreamConnector.this.running = false;
                }
            }
            if (IOStreamConnector.this.closeInput) {
                try {
                    IOStreamConnector.this.in.close();
                }
                catch (IOException ioe) {
                    // empty catch block
                }
            }
            if (IOStreamConnector.this.closeOutput) {
                try {
                    IOStreamConnector.this.out.close();
                }
                catch (IOException ioe) {
                    // empty catch block
                }
            }
            IOStreamConnector.this.closed = true;
            for (int i = 0; i < IOStreamConnector.this.listenerList.size(); ++i) {
                IOStreamConnector.this.listenerList.elementAt(i).connectorClosed(IOStreamConnector.this);
            }
            IOStreamConnector.this.thread = null;
        }
    }
}

