/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

public abstract class ClientUtils {
    public static String composeDfsRemotePathToFile(String server, String share, String file) {
        String result = "\\" + server + "\\" + share;
        if (null != file && !file.equals("")) {
            result = result + "\\" + (file.charAt(0) == '\\' ? file.substring(1) : file);
        }
        result = result + '\u0000';
        return result;
    }

    static String composeRemotePathToFile(String server, String share, String file) {
        String result = "\\\\" + server + "\\" + share;
        if (null != file) {
            result = result + "\\" + (file.charAt(0) == '\\' ? file.substring(1) : file);
        }
        return result;
    }

    public static String hostNameFromRemotePath(String path) {
        int t;
        int p;
        int len = path.length();
        for (p = 0; p < len && (path.charAt(p) == '\\' || path.charAt(p) == '/'); ++p) {
        }
        for (t = p; t < len && path.charAt(t) != '\\' && path.charAt(t) != '/'; ++t) {
        }
        return path.substring(p, t);
    }

    public static String shareNameFromRemotePath(String path, boolean pathStartsWithHost) {
        int idx;
        if (path.equals("")) {
            return path;
        }
        for (idx = 0; idx < path.length() && path.charAt(idx) == '\\'; ++idx) {
        }
        path = path.substring(idx);
        if (pathStartsWithHost) {
            idx = path.indexOf(92);
            if (-1 != idx) {
                path = path.substring(idx + 1);
            } else {
                return "";
            }
        }
        if (-1 != (idx = path.indexOf(92))) {
            path = path.substring(0, idx);
        }
        return path;
    }

    public static String shareNameFromRemotePath(String path) {
        return ClientUtils.shareNameFromRemotePath(path, true);
    }

    public static String fileNameFromRemotePath(String path, boolean stripBackslash, boolean pathStartsWithHost) {
        String shareName;
        int idx;
        if (path.equals("")) {
            return path;
        }
        for (idx = 0; idx < path.length() && path.charAt(idx) == '\\'; ++idx) {
        }
        String tmp = path.substring(idx);
        if (pathStartsWithHost) {
            String hostName = ClientUtils.hostNameFromRemotePath(path);
            tmp = tmp.substring(hostName.length() + 1);
        }
        if (0 == (tmp = tmp.substring((shareName = ClientUtils.shareNameFromRemotePath(path, pathStartsWithHost)).length())).length()) {
            return stripBackslash ? "" : "\\";
        }
        return tmp.substring(stripBackslash ? 1 : 0);
    }

    public static String fileNameFromRemotePath(String path, boolean stripBackslash) {
        return ClientUtils.fileNameFromRemotePath(path, stripBackslash, true);
    }

    public static String composePath(String dir, String file) {
        if (null == file || 0 == file.length()) {
            return dir;
        }
        return dir + "\\" + file;
    }

    public static String composeRemotePathToShare(String server, String share) {
        return "\\\\" + server + "\\" + share;
    }

    public static String filePathStripWildcards(String name) {
        return null;
    }

    public static String directoryFromPath(String name) {
        int position = name.lastIndexOf("\\");
        String dirName = name.substring(0, position);
        return dirName;
    }

    public static String filePathStripLastComponent(String origin) {
        int backslashIdx;
        int lastComponentIdx;
        int slashIdx = origin.lastIndexOf(47);
        int n = lastComponentIdx = slashIdx > (backslashIdx = origin.lastIndexOf(92)) ? slashIdx : backslashIdx;
        if (lastComponentIdx >= 0) {
            return origin.substring(0, lastComponentIdx);
        }
        return "";
    }

    public static String filePathGetLastComponent(String origin, boolean stripBackslash) {
        int backslashIdx;
        int lastComponentIdx;
        int slashIdx = origin.lastIndexOf(47);
        int n = lastComponentIdx = slashIdx > (backslashIdx = origin.lastIndexOf(92)) ? slashIdx : backslashIdx;
        if (lastComponentIdx >= 0) {
            int strip = stripBackslash ? 1 : 0;
            return origin.substring(lastComponentIdx + strip);
        }
        String slash = stripBackslash ? "" : "\\";
        return slash + origin;
    }

    public static String filePathStripNull(String path) {
        if (!path.equals("") && path.charAt(path.length() - 1) == '\u0000') {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}

