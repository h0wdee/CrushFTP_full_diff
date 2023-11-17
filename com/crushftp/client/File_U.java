/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class File_U
extends File {
    private static final long serialVersionUID = 1L;
    String root = "";

    public File_U(File f, String root) {
        super(f.getPath());
        this.root = root;
        File_S.validate(root, this);
    }

    public File_U(String s, String root) {
        super(s);
        this.root = root;
        File_S.validate(root, this);
    }

    public File_U(URI u, String root) {
        super(u);
        this.root = root;
        File_S.validate(root, this);
    }

    public File_U(File f) {
        super(f.getPath());
        this.root = System.getProperty("crushftp.user.root", "");
        File_S.validate(this.root, this);
    }

    public File_U(String s) {
        super(s);
        this.root = System.getProperty("crushftp.user.root", "");
        File_S.validate(this.root, this);
    }

    public File_U(URI u) {
        super(u);
        this.root = System.getProperty("crushftp.user.root", "");
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
        File[] files2 = (File_U[])stream.map(new Map_to_File_U()).toArray(new File_U_IntFucntion());
        stream.close();
        return files2;
    }

    public static File[] listRoots() {
        File[] files = File.listRoots();
        if (files == null) {
            return null;
        }
        File[] files2 = new File_U[files.length];
        int x = 0;
        while (x < files.length) {
            files2[x] = new File_U(files[x], System.getProperty("crushftp.user.root", ""));
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
        return new File_U(f, this.root);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public class File_U_IntFucntion
    implements IntFunction<File_U[]> {
        @Override
        public File_U[] apply(int size) {
            return new File_U[size];
        }
    }

    public class Map_to_File_U
    implements Function<Path, File_U> {
        @Override
        public File_U apply(Path p) {
            return new File_U(p.toFile(), File_U.this.root);
        }
    }
}

