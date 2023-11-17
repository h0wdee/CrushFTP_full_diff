/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import com.maverick.util.IOUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class ProcessUtils {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String executeCommand(String ... args) throws IOException {
        Process process = new ProcessBuilder(Arrays.asList(args)).start();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = process.getInputStream();){
            IOUtil.copy(in, out);
        }
        return new String(out.toByteArray(), "UTF-8");
    }
}

