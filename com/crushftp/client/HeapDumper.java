/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;

public class HeapDumper {
    static HotSpotDiagnosticMXBean hotspotMBean = null;

    public String dump() {
        String filename = String.valueOf(System.getProperty("crushftp.home", "./")) + System.getProperty("appname", "CrushFTP").toLowerCase() + "_mem_dump" + Common.makeBoundary(3) + ".hprof";
        try {
            if (hotspotMBean == null) {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                hotspotMBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            }
            hotspotMBean.dumpHeap(filename, true);
        }
        catch (Exception e) {
            e.printStackTrace();
            Common.log("SERVER", 0, e);
        }
        return "Memory dumped to: " + filename;
    }
}

