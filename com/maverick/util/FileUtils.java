/*
 * Decompiled with CFR 0.152.
 */
package com.maverick.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {
    static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public static String convertBackslashToForwardSlash(String str) {
        return str.replace('\\', '/');
    }

    public static String checkStartsWithSlash(String str) {
        if (str.startsWith("/")) {
            return str;
        }
        return "/" + str;
    }

    public static String checkStartsWithNoSlash(String str) {
        if (str.startsWith("/")) {
            return str.substring(1);
        }
        return str;
    }

    public static String checkEndsWithSlash(String str) {
        if (str.endsWith("/")) {
            return str;
        }
        return str + "/";
    }

    public static String checkEndsWithNoSlash(String str) {
        if (str.endsWith("/")) {
            return str.substring(0, str.length() - 1);
        }
        return str;
    }

    public static String stripParentPath(String rootPath, String path) throws IOException {
        if (!(path = FileUtils.checkEndsWithSlash(path)).startsWith(rootPath = FileUtils.checkEndsWithSlash(rootPath))) {
            throw new IOException(path + " is not a child path of " + rootPath);
        }
        return path.substring(rootPath.length());
    }

    public static String stripFirstPathElement(String path) {
        int idx = (path = FileUtils.checkStartsWithNoSlash(path)).indexOf(47, 1);
        if (idx > -1) {
            return path.substring(idx);
        }
        return path;
    }

    public static String stripLastPathElement(String path) {
        int idx = (path = FileUtils.checkEndsWithNoSlash(path)).lastIndexOf(47);
        if (idx > -1) {
            return path.substring(0, idx);
        }
        return path;
    }

    public static String firstPathElement(String path) {
        int idx = (path = FileUtils.checkStartsWithNoSlash(path)).indexOf(47);
        if (idx > -1) {
            return path.substring(0, idx);
        }
        return path;
    }

    public static void closeQuietly(InputStream in) {
        try {
            if (in != null) {
                in.close();
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void closeQuietly(OutputStream out) {
        try {
            if (out != null) {
                out.close();
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void deleteFolder(File folder) {
        if (folder != null) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        FileUtils.deleteFolder(f);
                        continue;
                    }
                    f.delete();
                }
            }
            folder.delete();
        }
    }

    public static String formatSize(long size) {
        if (size <= 0L) {
            return "0";
        }
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int)(Math.log10(size) / Math.log10(1024.0));
        return new DecimalFormat("#,##0.#").format((double)size / Math.pow(1024.0, digitGroups)) + " " + units[digitGroups];
    }

    public static String formatLastModified(long lastModifiedTime) {
        return dateFormat.format(new Date(lastModifiedTime));
    }

    public static String getParentPath(String originalFilename) {
        int idx = (originalFilename = FileUtils.checkEndsWithNoSlash(originalFilename)).lastIndexOf(47);
        if (idx > -1) {
            originalFilename = originalFilename.substring(0, idx + 1);
        } else {
            idx = originalFilename.lastIndexOf(92);
            if (idx > -1) {
                originalFilename = originalFilename.substring(0, idx + 1);
            }
        }
        return originalFilename;
    }

    public static String stripPath(String originalFilename) {
        int idx = (originalFilename = FileUtils.checkEndsWithNoSlash(originalFilename)).lastIndexOf(47);
        if (idx > -1) {
            originalFilename = originalFilename.substring(idx + 1);
        } else {
            idx = originalFilename.lastIndexOf(92);
            if (idx > -1) {
                originalFilename = originalFilename.substring(idx + 1);
            }
        }
        return originalFilename;
    }

    public static String lastPathElement(String originalFilename) {
        int idx = originalFilename.lastIndexOf(47);
        if (idx > -1) {
            return originalFilename.substring(idx + 1);
        }
        return originalFilename;
    }

    public static boolean hasParents(String sourcePath) {
        return sourcePath.indexOf(47) > -1;
    }
}

