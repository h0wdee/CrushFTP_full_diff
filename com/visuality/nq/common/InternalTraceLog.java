/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.common;

import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.Utility;
import com.visuality.nq.config.Config;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class InternalTraceLog
extends TraceLog {
    private int msgNum = 0;
    private ArrayBlockingQueue outputs = new ArrayBlockingQueue(5);
    private Spooler spooler = new Spooler();
    private Thread spoolThread = new Thread((Runnable)this.spooler, "TraceLog");
    private Queue queue = new LinkedList();
    private Boolean doExit = false;
    private static int stackDepth = 0;
    private static final int INITIALOPQUEUE = 5;
    private int fileNumber = 1;
    private int jvmId = 0;
    private String basicFileName = "default.log";
    private String basicPath = "";
    private int maxRecordsInFile = 10000;
    private int recordsInFile = 0;
    private PrintStream fileOut = null;
    private int maxFiles = 20;
    private File logDir;
    private boolean isInternalFileLogStarted = false;
    private boolean isInternalConsoleLogStarted = false;
    private boolean useFile;
    private static final String SEMI_COLON = ";";

    public InternalTraceLog() {
        this.preConstructor();
        if (0 != this.maxRecordsInFile && 0 != this.maxFiles) {
            this.spoolThread.setPriority(1);
            this.spoolThread.start();
            try {
                this.useFile = Config.jnq.getBool("LOGTOFILE");
                if (this.useFile) {
                    this.fetchFilename();
                    this.startFile(this.basicFileName);
                    this.isInternalFileLogStarted = true;
                }
            }
            catch (NqException e) {
                System.err.println("Error initializing logger; " + e.getMessage());
                Config.jnq.setNE("LOGTOFILE", false);
            }
            try {
                if (Config.jnq.getBool("LOGTOCONSOLE")) {
                    this.outputs.add(System.out);
                    this.isInternalConsoleLogStarted = true;
                }
            }
            catch (NqException e) {
                System.err.println("Error initializing logger; " + e.getMessage());
                Config.jnq.setNE("LOGTOCONSOLE", false);
            }
        }
    }

    private void fetchFilename() {
        try {
            String tmpPath = Config.jnq.getString("LOGFILE");
            int pos = 0;
            int tmpPos = tmpPath.lastIndexOf("\\");
            if (tmpPos != -1) {
                pos = tmpPos + 1;
            } else {
                tmpPos = tmpPath.lastIndexOf("/");
                if (tmpPos != -1) {
                    pos = tmpPos + 1;
                }
            }
            this.basicFileName = tmpPath.substring(pos);
            this.basicPath = tmpPath.substring(0, tmpPath.length() - this.basicFileName.length());
        }
        catch (NqException e) {
            System.err.println("Logging error; Log file name not defined, using the default; " + e.getMessage());
        }
    }

    public InternalTraceLog(String fileName) {
        this.preConstructor();
        if (0 != this.maxRecordsInFile && 0 != this.maxFiles) {
            this.spoolThread.setPriority(1);
            this.spoolThread.start();
            this.startFile(fileName);
        }
    }

    private void preConstructor() {
        Config.Param param;
        try {
            this.maxRecordsInFile = Config.jnq.getInt("LOGMAXRECORDSINFILE");
        }
        catch (NqException e) {
            System.err.println(e.getMessage() + "; using default value");
            param = (Config.Param)Config.jnq.getParamCache().get("LOGMAXRECORDSINFILE");
            this.maxRecordsInFile = (Integer)param.getDefaultValue();
        }
        try {
            this.maxFiles = Config.jnq.getInt("LOGMAXFILES");
        }
        catch (NqException e) {
            System.err.println(e.getMessage() + "; using default value");
            param = (Config.Param)Config.jnq.getParamCache().get("LOGMAXFILES");
            this.maxFiles = (Integer)param.getDefaultValue();
        }
    }

    private void startFile(String fileName) {
        File f = new File(this.basicPath);
        if (!f.exists()) {
            this.basicPath = "";
        }
        if (this.useFile) {
            this.jvmId = 1;
            while (true) {
                this.logDir = new File(this.basicPath + this.jvmId + ".log");
                if (!this.logDir.exists()) break;
                ++this.jvmId;
            }
            this.logDir.mkdir();
            try {
                this.tryFile(fileName);
            }
            catch (Exception e) {
                System.err.println("Logging error; Log file name not defined, using the default; " + e.getMessage());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addOrRemoveFileFromOutput(boolean isDoAdd) {
        if (null == this.fileOut) {
            return;
        }
        ArrayBlockingQueue arrayBlockingQueue = this.outputs;
        synchronized (arrayBlockingQueue) {
            if (isDoAdd) {
                if (this.outputs.contains(this.fileOut)) {
                    return;
                }
                this.outputs.add(this.fileOut);
            } else {
                this.outputs.remove(this.fileOut);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean tryFile(String fileName) {
        if (this.useFile) {
            ArrayBlockingQueue arrayBlockingQueue = this.outputs;
            synchronized (arrayBlockingQueue) {
                try {
                    File f = new File(this.logDir, fileName);
                    if (f.exists()) {
                        try {
                            f.delete();
                        }
                        catch (Exception e) {
                            return false;
                        }
                    }
                    if (null != this.fileOut) {
                        this.outputs.remove(this.fileOut);
                    }
                    FileOutputStream file = new FileOutputStream(f);
                    this.fileOut = new PrintStream(file);
                    this.outputs.add(this.fileOut);
                    return true;
                }
                catch (FileNotFoundException e) {
                    System.err.println("Unable to start trace log into '" + fileName + "' - " + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    public boolean canLog(int level) {
        return (this.isInternalConsoleLogStarted || this.isInternalFileLogStarted) && level <= this.getThreshold();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void log(String type, int level, int status, String text) {
        if (0 == this.maxRecordsInFile || 0 == this.maxFiles || null == this.spoolThread) {
            return;
        }
        if (level > this.threshold) {
            return;
        }
        if (!this.isInternalFileLogStarted) {
            try {
                this.useFile = Config.jnq.getBool("LOGTOFILE");
                if (this.useFile) {
                    this.isInternalFileLogStarted = true;
                    this.fetchFilename();
                    this.startFile(this.basicFileName);
                }
            }
            catch (NqException e) {
                System.err.println("Log file name not defined; error = " + e);
                Config.jnq.setNE("LOGTOFILE", false);
            }
        } else {
            try {
                if (!Config.jnq.getBool("LOGTOFILE")) {
                    this.addOrRemoveFileFromOutput(false);
                    this.isInternalFileLogStarted = false;
                } else if (Config.jnq.getBool("LOGTOFILE") && !this.outputs.contains(this.basicFileName)) {
                    this.addOrRemoveFileFromOutput(true);
                    this.isInternalFileLogStarted = true;
                }
            }
            catch (NqException e) {
                System.err.println("Log file name not defined; error = " + e);
                Config.jnq.setNE("LOGTOFILE", false);
            }
        }
        ArrayBlockingQueue e = this.outputs;
        synchronized (e) {
            if (!this.isInternalConsoleLogStarted) {
                try {
                    if (Config.jnq.getBool("LOGTOCONSOLE")) {
                        this.outputs.add(System.out);
                        this.isInternalConsoleLogStarted = true;
                    }
                }
                catch (NqException e2) {}
            } else {
                try {
                    if (!Config.jnq.getBool("LOGTOCONSOLE") && this.outputs.contains(System.out)) {
                        this.outputs.remove(System.out);
                        this.isInternalConsoleLogStarted = false;
                    } else if (Config.jnq.getBool("LOGTOCONSOLE") && !this.outputs.contains(System.out)) {
                        this.outputs.add(System.out);
                        this.isInternalConsoleLogStarted = true;
                    }
                }
                catch (NqException e3) {
                    System.err.println("Log file name not defined, using the default");
                }
            }
        }
        if (!this.isInternalConsoleLogStarted && !this.isInternalFileLogStarted) {
            return;
        }
        StringBuilder outText = new StringBuilder(type + SEMI_COLON);
        outText.append(SEMI_COLON);
        outText.append(Thread.currentThread().getId());
        outText.append(SEMI_COLON);
        outText.append(Utility.getCurrentTimeInSec());
        outText.append(SEMI_COLON);
        outText.append(level);
        StackTraceElement[] stack = new Throwable().getStackTrace();
        int depth = stackDepth;
        depth = this.calculateDepth(depth, stack);
        if (stack.length > depth && depth >= 0) {
            outText.append(SEMI_COLON);
            outText.append(stack[depth].getFileName());
            outText.append(SEMI_COLON);
            outText.append(stack[depth].getMethodName());
            outText.append(SEMI_COLON);
            outText.append(stack[depth].getLineNumber());
        }
        if (type.equals("E")) {
            outText.append(SEMI_COLON);
            outText.append(status);
        }
        outText.append(SEMI_COLON);
        outText.append(text);
        this.addToPrintQueue(outText.toString());
    }

    private int calculateDepth(int depth, StackTraceElement[] stack) {
        if (depth >= stack.length && (depth = stack.length - 1) < 0) {
            return depth;
        }
        if (stack[depth].getFileName().equals("InternalTraceLog.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("TraceLog.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("NqException.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("SmbException.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("ClientException.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("NetbiosException.java")) {
            ++depth;
        }
        if (stack[depth].getFileName().equals("TestException.java")) {
            ++depth;
        }
        return depth;
    }

    private void logOut(String text) {
        ++this.recordsInFile;
        if (this.recordsInFile >= this.maxRecordsInFile && this.fileOut != null) {
            this.recordsInFile = 0;
            this.fileOut.close();
            this.tryFile(0 == this.fileNumber ? this.basicFileName : this.fileNumber + "_" + this.basicFileName);
            ++this.fileNumber;
            if (this.fileNumber >= this.maxFiles) {
                String fileToDel = this.fileNumber == this.maxFiles + 1 ? this.basicFileName : this.fileNumber - this.maxFiles - 1 + "_" + this.basicFileName;
                new File(this.logDir, fileToDel).delete();
            }
        }
        Iterator it = this.outputs.iterator();
        StringBuilder sb = new StringBuilder();
        sb.append(text.substring(0, 2));
        sb.append(this.msgNum++);
        sb.append(text.substring(2));
        String outputLine = sb.toString();
        while (it.hasNext()) {
            PrintStream ps = (PrintStream)it.next();
            ps.println(outputLine);
        }
    }

    private void closeLogFile() {
        if (null != this.fileOut) {
            this.fileOut.close();
        }
    }

    public void error(String text, int level, int status) {
        this.log("E", level, status, text);
    }

    public void enter(int level) {
        this.log("F", level, 0, "");
    }

    public void enter(Object obj, int level) {
        if (!this.canLog(level)) {
            return;
        }
        if (null == obj) {
            this.log("F", level, 0, "null");
            return;
        }
        if (obj instanceof String) {
            this.log("F", level, 0, (String)obj);
        } else {
            this.log("F", level, 0, obj.toString());
        }
    }

    public void exit(int level) {
        this.log("L", level, 0, "");
    }

    public void exit(Object obj, int level) {
        if (!this.canLog(level)) {
            return;
        }
        if (null == obj) {
            this.log("L", level, 0, "null");
            return;
        }
        if (obj instanceof String) {
            this.log("L", level, 0, (String)obj);
        } else {
            this.log("L", level, 0, obj.toString());
        }
    }

    public synchronized void start(int level) {
        if (null == this.spoolThread) {
            this.doExit = false;
            this.spoolThread = new Thread(this.spooler);
            this.spoolThread.setPriority(1);
            this.spoolThread.start();
        }
        this.log("S", level, 0, "");
    }

    public synchronized void stop(int level) {
        this.log("T", level, 0, "");
        this.terminate();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void terminate() {
        this.doExit = true;
        this.isInternalFileLogStarted = false;
        Queue queue = this.queue;
        synchronized (queue) {
            this.queue.notify();
        }
        this.spoolThread = null;
        this.closeLogFile();
    }

    boolean areStackTraceElementsIdentical(StackTraceElement a, StackTraceElement b) {
        if (null == a || null == b) {
            return false;
        }
        return a.getClassName().equals(b.getClassName()) && a.getMethodName().equals(b.getMethodName());
    }

    public void caught(Exception ex, int level) {
        if (level > this.threshold) {
            return;
        }
        StackTraceElement[] stack = ex.getStackTrace();
        StackTraceElement element = this.whereAmIInStackTrace();
        String prefix = "L";
        this.caughtHeader(level);
        int stackStartLevel = 0;
        if (ex instanceof NqException) {
            NqException e = (NqException)ex;
            if (0 != e.level) {
                stackStartLevel = e.level - 1;
            }
        }
        for (int i = stackStartLevel; i < stack.length; ++i) {
            if (null != element && this.areStackTraceElementsIdentical(element, stack[i])) {
                if (!(ex instanceof NqException)) break;
                NqException e = (NqException)ex;
                e.level = i + 1;
                break;
            }
            String className = stack[i].getClassName();
            if (!className.contains("visuality")) break;
            StringBuilder outText = new StringBuilder(prefix + SEMI_COLON);
            outText.append(SEMI_COLON);
            outText.append(Thread.currentThread().getId());
            outText.append(SEMI_COLON);
            outText.append(System.currentTimeMillis());
            outText.append(SEMI_COLON);
            outText.append(level);
            outText.append(SEMI_COLON);
            outText.append(stack[i].getFileName());
            outText.append(SEMI_COLON);
            outText.append(stack[i].getMethodName());
            outText.append(SEMI_COLON);
            outText.append(stack[i].getLineNumber());
            outText.append(SEMI_COLON);
            this.addToPrintQueue(outText.toString());
        }
        this.caughtFooter(level);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addToPrintQueue(String outText) {
        Queue queue = this.queue;
        synchronized (queue) {
            if (!this.doExit.booleanValue()) {
                this.queue.add(outText);
                this.queue.notify();
            }
        }
    }

    private void caughtHeader(int level) {
        this.log("I", level, 0, "Beginning stacktrace from caught==>");
    }

    private void caughtFooter(int level) {
        this.log("I", level, 0, "<== End stacktrace from caught");
    }

    public void message(String text, int level) {
        this.log("I", level, 0, text);
    }

    public void message(Object obj, int level) {
        if (!this.canLog(level)) {
            return;
        }
        if (null == obj) {
            this.log("I", level, 0, "null");
            return;
        }
        if (obj instanceof String) {
            this.log("I", level, 0, (String)obj);
        } else {
            this.log("I", level, 0, obj.toString());
        }
    }

    public void message(String text, Object obj, int level) {
        if (!this.canLog(level)) {
            return;
        }
        if (null == obj) {
            this.log("I", level, 0, text + "null");
            return;
        }
        if (obj instanceof String) {
            this.log("I", level, 0, text + obj);
        } else {
            this.log("I", level, 0, text + obj.toString());
        }
    }

    private StackTraceElement whereAmIInStackTrace() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        int newDepth = this.calculateDepth(2, stack);
        if (0 > newDepth) {
            return null;
        }
        return stack[newDepth];
    }

    public int getMaxRecordsInFile() {
        return this.maxRecordsInFile;
    }

    public void setMaxRecordsInFile(int maxRecordsInFile) {
        this.maxRecordsInFile = maxRecordsInFile;
    }

    public int getMsgNum() {
        return this.msgNum;
    }

    static {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stack.length; ++i) {
            if (!stack[i].getClassName().equals(InternalTraceLog.class.getName())) continue;
            stackDepth = i + 1;
            break;
        }
    }

    private class Spooler
    implements Runnable {
        private Spooler() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void run() {
            while (!InternalTraceLog.this.doExit.booleanValue()) {
                Queue queue = InternalTraceLog.this.queue;
                synchronized (queue) {
                    while (!InternalTraceLog.this.queue.isEmpty()) {
                        String msg = (String)InternalTraceLog.this.queue.poll();
                        if (null == msg) continue;
                        InternalTraceLog.this.logOut(msg);
                    }
                    try {
                        InternalTraceLog.this.queue.wait(10000L);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                }
            }
        }
    }
}

