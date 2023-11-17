/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.maverick.ssh;

import com.maverick.ssh.ExecutorOperationListener;
import com.maverick.ssh.ExecutorServiceProvider;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ExecutorOperationSupport<T extends ExecutorServiceProvider> {
    static Logger log = LoggerFactory.getLogger(ExecutorOperationSupport.class);
    LinkedList<Runnable> subsystemOperations = new LinkedList();
    OperationTask subsystemTask = new OperationTask();
    Future<?> operationFuture = null;
    boolean shutdown = false;
    boolean shuttingDown = false;
    List<ExecutorOperationListener> listeners = new ArrayList<ExecutorOperationListener>();

    public abstract T getContext();

    public synchronized void addTask(Runnable r) {
        if (this.shutdown && log.isDebugEnabled()) {
            log.debug("Caller is attempting to run a task but the executor has been shutdown");
        }
        if (log.isTraceEnabled()) {
            log.trace("Adding task to " + this.getName());
        }
        this.subsystemTask.addTask(r);
        this.taskAdded();
    }

    protected abstract String getName();

    protected synchronized void clearQueue() {
        this.subsystemOperations.clear();
    }

    protected void taskAdded() {
    }

    protected void startTask(Runnable r) {
    }

    public int getOperationsCount() {
        return this.subsystemOperations.size();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addOperationListener(ExecutorOperationListener listener) {
        OperationTask operationTask = this.subsystemTask;
        synchronized (operationTask) {
            this.listeners.add(listener);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeOperationListener(ExecutorOperationListener listener) {
        OperationTask operationTask = this.subsystemTask;
        synchronized (operationTask) {
            this.listeners.remove(listener);
        }
    }

    public synchronized void cleanupOperations(final Runnable ... onComplete) {
        if (!this.shuttingDown) {
            if (log.isTraceEnabled()) {
                log.trace("Submitting clean up operation to executor service");
            }
            this.getContext().getExecutorService().submit(new Runnable(){

                @Override
                public void run() {
                    if (ExecutorOperationSupport.this.operationFuture != null) {
                        if (log.isTraceEnabled()) {
                            log.trace("Cleaning up operations");
                        }
                        try {
                            if (log.isTraceEnabled()) {
                                log.trace("Waiting for operations to complete");
                            }
                            ExecutorOperationSupport.this.operationFuture.get();
                            if (log.isTraceEnabled()) {
                                log.trace("All operations have completed");
                            }
                        }
                        catch (InterruptedException interruptedException) {
                        }
                        catch (ExecutionException executionException) {
                            // empty catch block
                        }
                    }
                    try {
                        for (Runnable r : onComplete) {
                            r.run();
                        }
                    }
                    catch (Throwable throwable) {
                        // empty catch block
                    }
                    ExecutorOperationSupport.this.subsystemTask.shutdown();
                    ExecutorOperationSupport.this.shutdown = true;
                }
            });
        }
    }

    protected void startExecution() {
    }

    protected void endExecution() {
    }

    class OperationTask
    implements Runnable {
        boolean running = false;

        OperationTask() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            if (log.isTraceEnabled()) {
                log.trace("Operation task is starting");
            }
            do {
                this.executeAllTasks();
                OperationTask operationTask = this;
                synchronized (operationTask) {
                    this.running = !ExecutorOperationSupport.this.subsystemOperations.isEmpty();
                }
            } while (this.running);
            if (log.isTraceEnabled()) {
                log.trace("Operation task has ended");
            }
        }

        public synchronized void shutdown() {
            if (log.isTraceEnabled()) {
                log.trace("Received shutdown notification");
            }
            ExecutorOperationSupport.this.shuttingDown = true;
            this.notifyAll();
        }

        public synchronized void addTask(Runnable r) {
            ExecutorOperationSupport.this.subsystemOperations.addLast(r);
            this.addedTask(r);
            if (!this.running) {
                this.running = true;
                if (log.isTraceEnabled()) {
                    log.trace("Starting new subsystem task");
                }
                try {
                    ExecutorOperationSupport.this.operationFuture = ExecutorOperationSupport.this.getContext().getExecutorService().submit(ExecutorOperationSupport.this.subsystemTask);
                }
                catch (RejectedExecutionException e) {
                    log.error("The executor rejected the task");
                }
            } else {
                this.notifyAll();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void executeAllTasks() {
            ExecutorOperationSupport.this.startExecution();
            while (!ExecutorOperationSupport.this.subsystemOperations.isEmpty()) {
                Runnable r = null;
                OperationTask operationTask = this;
                synchronized (operationTask) {
                    r = ExecutorOperationSupport.this.subsystemOperations.removeFirst();
                }
                if (r != null) {
                    ExecutorOperationSupport.this.startTask(r);
                    try {
                        r.run();
                    }
                    catch (Throwable t) {
                        log.error("Caught exception in operation remainingTasks=" + ExecutorOperationSupport.this.subsystemOperations.size(), t);
                    }
                    this.completedTask(r);
                    continue;
                }
                if (!log.isWarnEnabled()) continue;
                log.warn("Unexpected null task in operation queue");
            }
            ExecutorOperationSupport.this.endExecution();
        }

        protected synchronized void addedTask(Runnable r) {
            for (ExecutorOperationListener l : ExecutorOperationSupport.this.listeners) {
                try {
                    l.addedTask(r);
                }
                catch (Throwable throwable) {}
            }
        }

        protected synchronized void completedTask(Runnable r) {
            for (ExecutorOperationListener l : ExecutorOperationSupport.this.listeners) {
                try {
                    l.completedTask(r);
                }
                catch (Throwable throwable) {}
            }
        }
    }
}

