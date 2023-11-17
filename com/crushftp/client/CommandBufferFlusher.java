/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import java.util.Properties;
import java.util.Vector;

public class CommandBufferFlusher
implements Runnable {
    int interval = 10;
    public static final Vector commandBuffer = new Vector();

    public CommandBufferFlusher(int interval) {
        this.interval = interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public void run() {
        while (true) {
            try {
                CommandBufferFlusher.flushBuffer();
            }
            catch (Exception e) {
                Common.log("SERVER", 1, e);
            }
            if (this.interval == 0) {
                try {
                    Thread.sleep(100L);
                }
                catch (Exception exception) {}
                continue;
            }
            try {
                Thread.sleep(this.interval * 1000);
            }
            catch (Exception exception) {
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static void flushBuffer() {
        Vector vector = commandBuffer;
        synchronized (vector) {
            block5: while (true) {
                if (commandBuffer.size() <= 0) {
                    return;
                }
                Vector<String> paths = new Vector<String>();
                String app = "";
                String val = "";
                String param = "";
                int x = commandBuffer.size() - 1;
                while (x >= 0) {
                    Properties p = (Properties)commandBuffer.elementAt(x);
                    Common.log("SERVER", 2, "OS COMMAND:" + p);
                    if (val.equals("")) {
                        app = p.getProperty("app");
                        param = p.getProperty("param", "");
                        val = p.getProperty("val");
                        paths.addElement(app);
                        if (!param.equals("")) {
                            paths.addElement(param);
                        }
                        paths.addElement(val);
                    }
                    if (app.equals(p.getProperty("app")) && val.equals(p.getProperty("val")) && param.equals(p.getProperty("param", ""))) {
                        commandBuffer.remove(p);
                        paths.addElement(Common.replace_str(p.getProperty("path"), "&", "\\&"));
                    }
                    if (paths.toString().length() > 800) break;
                    --x;
                }
                try {
                    Common.log("SERVER", 2, paths.toString());
                    String[] c = new String[paths.size()];
                    int x2 = 0;
                    while (true) {
                        if (x2 >= paths.size()) {
                            Process proc = Runtime.getRuntime().exec(c);
                            proc.waitFor();
                            proc.destroy();
                            continue block5;
                        }
                        c[x2] = paths.elementAt(x2).toString();
                        ++x2;
                    }
                }
                catch (Exception e) {
                    Common.log("SERVER", 2, e);
                    Common.log("SERVER", 2, paths.toString());
                    continue;
                }
                break;
            }
        }
    }
}

