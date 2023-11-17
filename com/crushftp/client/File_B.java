/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class File_B
extends File {
    private static final long serialVersionUID = 1L;
    String root1 = "";
    String root2 = "";

    public File_B(File f, String root1, String root2) {
        super(f.getPath());
        this.root1 = root1;
        this.root2 = root2;
        boolean ok = false;
        RuntimeException e2 = null;
        try {
            File_S.validate(root1, this);
            ok = true;
        }
        catch (RuntimeException e) {
            e2 = e;
        }
        try {
            File_S.validate(root2, this);
            ok = true;
        }
        catch (RuntimeException e) {
            e2 = e;
        }
        if (!ok) {
            throw e2;
        }
    }

    public File_B(String s, String root1, String root2) {
        super(s);
        this.root1 = root1;
        this.root2 = root2;
    }

    public File_B(URI u, String root1, String root2) {
        super(u);
        this.root1 = root1;
        this.root2 = root2;
    }

    public File_B(File f) {
        super(f.getPath());
        this.root1 = System.getProperty("crushftp.user.root", "");
        this.root2 = System.getProperty("crushftp.system.root", "");
    }

    public File_B(File_S f) {
        super(f.getPath());
        this.root1 = System.getProperty("crushftp.user.root", "");
        this.root2 = System.getProperty("crushftp.system.root", "");
    }

    public File_B(File_U f) {
        super(f.getPath());
        this.root1 = System.getProperty("crushftp.user.root", "");
        this.root2 = System.getProperty("crushftp.system.root", "");
    }

    public File_B(String s) {
        super(s);
        this.root1 = System.getProperty("crushftp.user.root", "");
        this.root2 = System.getProperty("crushftp.system.root", "");
    }

    public File_B(URI u) {
        super(u);
        this.root1 = System.getProperty("crushftp.user.root", "");
        this.root2 = System.getProperty("crushftp.system.root", "");
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
        File[] files2 = (File_B[])stream.map(new Map_to_File_B()).toArray(new File_B_IntFucntion());
        stream.close();
        return files2;
    }

    public static File[] listRoots() {
        File[] files = File.listRoots();
        File[] files2 = new File_B[files.length];
        int x = 0;
        while (x < files.length) {
            files2[x] = new File_B(files[x], System.getProperty("crushftp.user.root", ""), System.getProperty("crushftp.system.root", ""));
            ++x;
        }
        return files2;
    }

    @Override
    public File getParentFile() {
        return new File_B(super.getParent(), this.root1, this.root2);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public class File_B_IntFucntion
    implements IntFunction<File_B[]> {
        @Override
        public File_B[] apply(int size) {
            return new File_B[size];
        }
    }

    public class Map_to_File_B
    implements Function<Path, File_B> {
        @Override
        public File_B apply(Path p) {
            return new File_B(p.toFile(), File_B.this.root1, File_B.this.root2);
        }
    }
}

