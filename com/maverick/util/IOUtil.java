/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOUtil {
    public static int BUFFER_SIZE = 8192;

    public static void copy(InputStream in, OutputStream out) throws IOException {
        IOUtil.copy(in, out, -1L);
    }

    public static void copy(InputStream in, OutputStream out, long count) throws IOException {
        IOUtil.copy(in, out, count, BUFFER_SIZE);
    }

    public static void copy(InputStream in, OutputStream out, long count, int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        int i = bufferSize;
        if (count >= 0L) {
            while (count > 0L && (i = count < (long)bufferSize ? in.read(buffer, 0, (int)count) : in.read(buffer, 0, bufferSize)) != -1) {
                count -= (long)i;
                out.write(buffer, 0, i);
            }
        } else {
            while ((i = in.read(buffer, 0, bufferSize)) >= 0) {
                out.write(buffer, 0, i);
            }
        }
    }

    public static boolean closeStream(InputStream in) {
        try {
            if (in != null) {
                in.close();
            }
            return true;
        }
        catch (IOException ioe) {
            return false;
        }
    }

    public static boolean closeStream(OutputStream out) {
        try {
            if (out != null) {
                out.close();
            }
            return true;
        }
        catch (IOException ioe) {
            return false;
        }
    }

    public static boolean delTree(File file) {
        if (file.isFile()) {
            return file.delete();
        }
        String[] list = file.list();
        for (int i = 0; i < list.length; ++i) {
            if (IOUtil.delTree(new File(file, list[i]))) continue;
            return false;
        }
        return true;
    }

    public static void recurseDeleteDirectory(File dir) {
        String[] files = dir.list();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; ++i) {
            File f = new File(dir, files[i]);
            if (f.isDirectory()) {
                IOUtil.recurseDeleteDirectory(f);
            }
            f.delete();
        }
        dir.delete();
    }

    public static void copyFile(File from, File to) throws IOException {
        if (from.isDirectory()) {
            if (!to.exists()) {
                to.mkdir();
            }
            String[] children = from.list();
            for (int i = 0; i < children.length; ++i) {
                File f = new File(from, children[i]);
                if (f.getName().equals(".") || f.getName().equals("..")) continue;
                if (f.isDirectory()) {
                    File f2 = new File(to, f.getName());
                    IOUtil.copyFile(f, f2);
                    continue;
                }
                IOUtil.copyFile(f, to);
            }
        } else if (from.isFile() && (to.isDirectory() || to.isFile())) {
            int read;
            if (to.isDirectory()) {
                to = new File(to, from.getName());
            }
            FileInputStream in = new FileInputStream(from);
            FileOutputStream out = new FileOutputStream(to);
            byte[] buf = new byte[32678];
            while ((read = in.read(buf)) > -1) {
                out.write(buf, 0, read);
            }
            IOUtil.closeStream(in);
            IOUtil.closeStream(out);
        }
    }

    public static int readyFully(InputStream in, byte[] buf) throws IOException {
        int r;
        int c = 0;
        do {
            if ((r = in.read(buf, c, buf.length - c)) != -1) continue;
            if (c != 0) break;
            c = -1;
            break;
        } while ((c += r) < buf.length);
        return c;
    }

    public static void writeStringToStream(OutputStream out, String string, String charset) throws UnsupportedEncodingException, IOException {
        try {
            out.write(string.getBytes(charset));
            out.flush();
        }
        finally {
            out.close();
        }
    }

    public static void writeStringToFile(File file, String string, String charset) throws UnsupportedEncodingException, IOException {
        try (FileOutputStream out = new FileOutputStream(file);){
            IOUtil.writeStringToStream(out, string, charset);
        }
    }

    public static String toUTF8String(InputStream in) throws IOException {
        return IOUtil.toString(in, "UTF-8");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String toString(InputStream in, String charset) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            IOUtil.copy(in, out);
            IOUtil.closeStream(in);
            String string = new String(out.toByteArray(), charset);
            return string;
        }
        finally {
            IOUtil.closeStream(out);
        }
    }

    public static Long fromByteSize(String val) {
        if (val.matches("\\d+")) {
            return Long.parseLong(val);
        }
        Pattern p = Pattern.compile("(\\d+)(.*)");
        Matcher m = p.matcher(val);
        if (!m.matches()) {
            throw new IllegalArgumentException(String.format("Invalid input %s", val));
        }
        String n = m.group(1);
        String t = m.group(2);
        t = t.toUpperCase();
        Long v = Long.parseLong(n);
        switch (t) {
            case "P": 
            case "PB": {
                return v * 1000L * 1000L * 1000L * 1000L * 1000L;
            }
            case "T": 
            case "TB": {
                return v * 1000L * 1000L * 1000L * 1000L;
            }
            case "G": 
            case "GB": {
                return v * 1000L * 1000L * 1000L;
            }
            case "M": 
            case "MB": {
                return v * 1000L * 1000L;
            }
            case "K": 
            case "KB": {
                return v * 1000L;
            }
        }
        throw new IllegalArgumentException(String.format("Invalid input %s", val));
    }

    public static void writeBytesToFile(byte[] value, File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        try {
            ((OutputStream)out).write(value);
        }
        finally {
            IOUtil.closeStream(out);
        }
    }

    public static String toByteSize(double t) {
        return IOUtil.toByteSize(t, 2);
    }

    public static String toByteSize(double t, int decimalPlaces) {
        if (decimalPlaces < 0) {
            throw new IllegalArgumentException("Number of decimal places must be > 0");
        }
        String[] sizes = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        int idx = 0;
        double x = t;
        while (x / 1000.0 >= 1.0) {
            ++idx;
            x /= 1000.0;
        }
        return String.format("%." + decimalPlaces + "f%s", x, sizes[idx]);
    }
}

