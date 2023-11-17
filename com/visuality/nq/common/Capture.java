/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.CaptureInternal;
import com.visuality.nq.common.TraceLog;
import java.util.LinkedList;
import java.util.Queue;

public abstract class Capture {
    protected static final String ENABLECAPTUREPACKETS = "ENABLECAPTUREPACKETS";
    protected static volatile boolean doExit = false;
    protected static int fileNumber = 0;
    public static final boolean RECEIVING = true;
    public static final boolean SENDING = false;
    static Thread captureSpoolerThread = null;
    protected static CaptureInternal.CaptureSpooler captureSpooler = new CaptureInternal.CaptureSpooler();
    protected static volatile Queue queue = null;
    protected static boolean loggingEnabled = false;

    public static void start() {
        TraceLog.get().enter(200);
        if (!loggingEnabled && null == queue) {
            queue = new LinkedList();
            fileNumber = 0;
            loggingEnabled = true;
            Capture.startCaptureThread();
            doExit = false;
        }
        TraceLog.get().exit(200);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void stop() {
        TraceLog.get().enter(200);
        loggingEnabled = false;
        doExit = true;
        if (null != queue) {
            Queue queue = Capture.queue;
            synchronized (queue) {
                Capture.queue.notify();
            }
        }
        TraceLog.get().exit(200);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected static void startCaptureThread() {
        CaptureInternal.CaptureSpooler captureSpooler = Capture.captureSpooler;
        synchronized (captureSpooler) {
            if (null == captureSpoolerThread) {
                doExit = false;
                captureSpoolerThread = new Thread((Runnable)Capture.captureSpooler, "Capture");
                captureSpoolerThread.start();
            }
        }
    }
}

