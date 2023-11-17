/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class File_S
extends File {
    private static final long serialVersionUID = 1L;
    String root = "";
    public static Object log_lock = new Object();
    public static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss.SSS");

    public File_S(File f, String root) {
        super(f.getPath());
        this.root = root;
        File_S.validate(root, this);
    }

    public File_S(String s, String root) {
        super(s);
        this.root = root;
        File_S.validate(root, this);
    }

    public File_S(URI u, String root) {
        super(u);
        this.root = root;
        File_S.validate(root, this);
    }

    public File_S(File f) {
        super(f.getPath());
        this.root = System.getProperty("crushftp.server.root", "");
        File_S.validate(this.root, this);
    }

    public File_S(String s) {
        super(s);
        this.root = System.getProperty("crushftp.server.root", "");
        File_S.validate(this.root, this);
    }

    public File_S(URI u) {
        super(u);
        this.root = System.getProperty("crushftp.server.root", "");
        File_S.validate(this.root, this);
    }

    @Override
    public File[] listFiles() {
        try {
            return this.listFiles2();
        }
        catch (Exception e) {
            Common.log("SERVER", 1, e);
            return null;
        }
    }

    public File[] listFiles2() throws IOException {
        Stream stream = (Stream)Files.list(Paths.get(this.getPath(), new String[0])).parallel();
        File[] files2 = (File_S[])stream.map(new Map_to_File_S()).toArray(new File_S_IntFucntion());
        stream.close();
        return files2;
    }

    public static File[] listRoots() {
        File[] files = File.listRoots();
        if (files == null) {
            return null;
        }
        File[] files2 = new File_S[files.length];
        int x = 0;
        while (x < files.length) {
            files2[x] = new File_S(files[x], System.getProperty("crushftp.server.root", ""));
            ++x;
        }
        return files2;
    }

    @Override
    public File getParentFile() {
        File f = super.getParentFile();
        if (f == null) {
            return f;
        }
        return new File_S(f, this.root);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void validate(String root, File f) {
        if (root.equals("")) {
            return;
        }
        String s = f.getPath().replace('\\', '/');
        if (s.startsWith("./") || s.indexOf("../") >= 0 || !s.startsWith("/")) {
            try {
                s = f.getCanonicalPath().replace('\\', '/');
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        String[] roots = root.split(";");
        boolean found = false;
        int x = 0;
        while (x < roots.length) {
            if (s.startsWith(roots[x]) || ("/" + s).startsWith(roots[x]) || (String.valueOf(s) + "/").startsWith(roots[x]) || ("/" + s + "/").startsWith(roots[x])) {
                found = true;
            }
            ++x;
        }
        if (!found) {
            RuntimeException e = new RuntimeException("Invalid file location:" + s + " VERSUS root:\"" + root + "\"");
            if (System.getProperty("crushftp.server.file.warn", "true").equals("true")) {
                e.printStackTrace();
            }
            if (System.getProperty("crushftp.server.file.log", "false").equals("true")) {
                Object object = log_lock;
                synchronized (object) {
                    block26: {
                        RandomAccessFile raf = null;
                        try {
                            try {
                                raf = new RandomAccessFile("file_audit.log", "rw");
                                if (raf.length() > 0x6400000L) {
                                    System.out.println("" + e);
                                } else {
                                    raf.seek(raf.length());
                                    raf.write((String.valueOf(sdf.format(new Date())) + "|" + e + "\r\n").getBytes());
                                }
                            }
                            catch (Exception e1) {
                                e1.printStackTrace();
                                try {
                                    raf.close();
                                }
                                catch (IOException iOException) {}
                                break block26;
                            }
                        }
                        catch (Throwable throwable) {
                            try {
                                raf.close();
                            }
                            catch (IOException iOException) {
                                // empty catch block
                            }
                            throw throwable;
                        }
                        try {
                            raf.close();
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                    }
                }
            }
            if (System.getProperty("crushftp.server.file.strict", "false").equals("true")) {
                throw e;
            }
        }
    }

    public class File_S_IntFucntion
    implements IntFunction<File_S[]> {
        @Override
        public File_S[] apply(int size) {
            return new File_S[size];
        }
    }

    public class Map_to_File_S
    implements Function<Path, File_S> {
        @Override
        public File_S apply(Path p) {
            return new File_S(p.toFile(), File_S.this.root);
        }
    }
}

