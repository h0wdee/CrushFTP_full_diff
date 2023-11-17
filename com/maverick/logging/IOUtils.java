/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOUtils {
    public static int BUFFER_SIZE = 8192;

    public static void copy(InputStream in, OutputStream out) throws IOException {
        IOUtils.copy(in, out, -1L);
    }

    public static void copy(InputStream in, OutputStream out, long count) throws IOException {
        IOUtils.copy(in, out, count, BUFFER_SIZE);
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
        if (list != null) {
            for (int i = 0; i < list.length; ++i) {
                if (IOUtils.delTree(new File(file, list[i]))) continue;
                return false;
            }
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
                IOUtils.recurseDeleteDirectory(f);
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
                    IOUtils.copyFile(f, f2);
                    continue;
                }
                IOUtils.copyFile(f, to);
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
            IOUtils.closeStream(in);
            IOUtils.closeStream(out);
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

    public static void writeUTF8StringToStream(OutputStream out, String string) throws UnsupportedEncodingException, IOException {
        IOUtils.writeStringToStream(out, string, "UTF-8");
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

    public static void writeUTF8StringToFile(File file, String string) throws UnsupportedEncodingException, IOException {
        IOUtils.writeStringToFile(file, string, "UTF-8");
    }

    public static void writeStringToFile(File file, String string, String charset) throws UnsupportedEncodingException, IOException {
        try (FileOutputStream out = new FileOutputStream(file);){
            IOUtils.writeStringToStream(out, string, charset);
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

    public static String toByteSize(double t) {
        return IOUtils.toByteSize(t, 2);
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

    public static byte[] sha1Digest(File file) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        return IOUtils.sha1Digest(new FileInputStream(file));
    }

    /*
     * Loose catch block
     */
    public static byte[] sha1Digest(InputStream in) throws NoSuchAlgorithmException, IOException {
        try {
            try (DigestOutputStream out = new DigestOutputStream(new OutputStream(){

                @Override
                public void write(int b) {
                }
            }, MessageDigest.getInstance("SHA-1"));){
                IOUtils.copy(in, out);
                byte[] byArray = out.getMessageDigest().digest();
                return byArray;
            }
            {
                catch (Throwable throwable) {
                    throw throwable;
                }
            }
        }
        finally {
            IOUtils.closeStream(in);
        }
    }

    public static void closeStream(Closeable obj) {
        if (IOUtils.isNull(obj)) {
            return;
        }
        try {
            obj.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static boolean isNull(Object obj) {
        return obj == null;
    }

    private static int getExtensionIndex(String filename) {
        int idx = filename.lastIndexOf(46);
        return idx;
    }

    public static String getFilenameExtension(String filename) {
        int idx = IOUtils.getExtensionIndex(filename);
        if (idx > -1) {
            return filename.substring(idx + 1);
        }
        return null;
    }

    public static String getFilenameWithoutExtension(String filename) {
        int idx = IOUtils.getExtensionIndex(filename);
        if (idx > -1) {
            return filename.substring(0, idx);
        }
        return filename;
    }

    public static void rollover(File logFile, int maxFiles) {
        String fileExtension = IOUtils.getFilenameExtension(logFile.getName());
        String fileName = IOUtils.getFilenameWithoutExtension(logFile.getName());
        fileExtension = !IOUtils.isNull(fileExtension) ? String.format(".%s", fileExtension) : "";
        File parentDir = logFile.getParentFile();
        File lastFile = null;
        for (int i = maxFiles; i >= 1; --i) {
            File backup = new File(parentDir, String.format("%s.%d%s", fileName, i, fileExtension));
            if (backup.exists()) {
                if (i == maxFiles) {
                    backup.delete();
                } else {
                    backup.renameTo(lastFile);
                }
            }
            lastFile = backup;
        }
        logFile.renameTo(lastFile);
    }

    public static String readUTF8StringFromFile(File file) throws IOException {
        return IOUtils.readStringFromFile(file, "UTF-8");
    }

    public static String readUTF8StringFromStream(InputStream in) throws IOException {
        return IOUtils.readStringFromStream(in, "UTF-8");
    }

    public static String readStringFromFile(File file, String charset) throws UnsupportedEncodingException, IOException {
        try (FileInputStream in = new FileInputStream(file);){
            String string = IOUtils.readStringFromStream(in, charset);
            return string;
        }
    }

    public static String readStringFromStream(InputStream in, String charset) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();){
            IOUtils.copy(in, out);
            String string = new String(out.toByteArray(), charset);
            return string;
        }
    }

    public static InputStream toInputStream(String value, String charset) throws IOException {
        return new ByteArrayInputStream(value.getBytes(charset));
    }
}

