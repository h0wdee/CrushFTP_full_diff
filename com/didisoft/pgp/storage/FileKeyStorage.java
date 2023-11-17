/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.storage;

import com.didisoft.pgp.storage.IKeyStoreStorage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileKeyStorage
implements IKeyStoreStorage {
    private static int BUFFER_SIZE = 4096;
    private String fileName;

    public FileKeyStorage(String string) {
        this.fileName = string;
    }

    public String getFileName() {
        return this.fileName;
    }

    public InputStream getInputStream() throws IOException {
        File file = new File(this.fileName);
        if (!file.exists() || file.length() == 0L) {
            return null;
        }
        return new FileInputStream(this.fileName);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void store(InputStream inputStream, int n) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(this.fileName);
        try {
            byte[] byArray = new byte[BUFFER_SIZE];
            int n2 = -1;
            while ((n2 = inputStream.read(byArray, 0, byArray.length)) > 0) {
                ((OutputStream)fileOutputStream).write(byArray, 0, n2);
            }
        }
        finally {
            ((OutputStream)fileOutputStream).close();
            fileOutputStream = null;
        }
    }
}

