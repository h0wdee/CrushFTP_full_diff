/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.WRunnable;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Worker {
    static ExecutorService executor = Executors.newCachedThreadPool();
    public static long lastDump = 0L;
    public static Vector availableWorkers = new Vector();
    public static Vector busyWorkers = new Vector();
    public static Properties thread_lookup = new Properties();

    public static boolean startWorker(Runnable q) throws IOException {
        return Worker.startWorker(q, null);
    }

    public static boolean startWorker(Runnable q1, final String threadName) throws IOException {
        if (busyWorkers.size() > Integer.parseInt(System.getProperty("crushftp.max_threads", "800")) - 50) {
            try {
                Thread.sleep(500L);
            }
            catch (InterruptedException interruptedException) {}
        }
        while (busyWorkers.size() > Integer.parseInt(System.getProperty("crushftp.max_threads", "800"))) {
            Common.log("SERVER", 0, "No threads left!  Busy:" + busyWorkers.size() + " Available:" + availableWorkers.size() + " Max:" + Integer.parseInt(System.getProperty("crushftp.max_threads", "800")) + " CPU and OpenFiles:" + Common.getCpuUsage());
            if (System.currentTimeMillis() - lastDump > 20000L) {
                lastDump = System.currentTimeMillis();
                Common.log("SERVER", 0, Common.dumpStack("No more workers:" + System.getProperty("crushftp.max_threads", "800")));
                lastDump = System.currentTimeMillis();
            }
            try {
                Thread.sleep(1000L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        while (true) {
            Object o;
            if ((o = thread_lookup.get(Thread.currentThread())) != null && o instanceof WRunnable && !(q1 instanceof WRunnable)) {
                q1 = new WRunnable(q1, (WRunnable)o);
            }
            final Runnable q = q1;
            try {
                executor.execute(new Runnable(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public void run() {
                        block24: {
                            try {
                                try {
                                    Vector vector = busyWorkers;
                                    synchronized (vector) {
                                        if (availableWorkers.size() > 0) {
                                            busyWorkers.addElement(availableWorkers.remove(0));
                                        } else {
                                            busyWorkers.addElement("");
                                        }
                                    }
                                    if (threadName != null) {
                                        Thread.currentThread().setName(threadName);
                                    } else {
                                        Thread.currentThread().setName("Worker:active...unamed thread");
                                    }
                                    thread_lookup.put(Thread.currentThread(), q);
                                    q.run();
                                }
                                catch (Throwable t) {
                                    t.printStackTrace();
                                    Common.log("SERVER", 1, "WORKER_FAILED:" + t);
                                    Common.log("SERVER", 1, t);
                                    thread_lookup.remove(Thread.currentThread());
                                    Thread.currentThread().setName("Worker:Idle");
                                    Vector vector = busyWorkers;
                                    synchronized (vector) {
                                        if (busyWorkers.size() > 0) {
                                            availableWorkers.addElement(busyWorkers.remove(0));
                                        }
                                        break block24;
                                    }
                                }
                            }
                            catch (Throwable throwable) {
                                thread_lookup.remove(Thread.currentThread());
                                Thread.currentThread().setName("Worker:Idle");
                                Vector vector = busyWorkers;
                                synchronized (vector) {
                                    if (busyWorkers.size() > 0) {
                                        availableWorkers.addElement(busyWorkers.remove(0));
                                    }
                                }
                                throw throwable;
                            }
                            thread_lookup.remove(Thread.currentThread());
                            Thread.currentThread().setName("Worker:Idle");
                            Vector vector = busyWorkers;
                            synchronized (vector) {
                                if (busyWorkers.size() > 0) {
                                    availableWorkers.addElement(busyWorkers.remove(0));
                                }
                            }
                        }
                    }
                });
            }
            catch (Throwable t) {
                Common.log("SERVER", 0, "Thread start failed!  Retrying...  Busy:" + busyWorkers.size() + " Available:" + availableWorkers.size() + " Max:" + Integer.parseInt(System.getProperty("crushftp.max_threads", "800")) + " CPU and OpenFiles:" + Common.getCpuUsage());
                Common.log("SERVER", 0, t);
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedException) {}
                continue;
            }
            break;
        }
        return true;
    }
}

