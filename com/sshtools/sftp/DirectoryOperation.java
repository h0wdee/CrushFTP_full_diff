/*
 * Decompiled with CFR 0.152.
 */
package com.sshtools.sftp;

import com.maverick.sftp.SftpFile;
import com.maverick.sftp.SftpStatusException;
import com.maverick.ssh.SshException;
import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class DirectoryOperation {
    Vector unchangedFiles = new Vector();
    Vector newFiles = new Vector();
    Vector updatedFiles = new Vector();
    Vector deletedFiles = new Vector();
    Vector recursedDirectories = new Vector();
    Hashtable failedTransfers = new Hashtable();

    void addNewFile(File f) {
        this.newFiles.addElement(f);
    }

    void addFailedTransfer(File f, SftpStatusException ex) {
        this.failedTransfers.put(f, ex);
    }

    void addUpdatedFile(File f) {
        this.updatedFiles.addElement(f);
    }

    void addDeletedFile(File f) {
        this.deletedFiles.addElement(f);
    }

    void addUnchangedFile(File f) {
        this.unchangedFiles.addElement(f);
    }

    void addNewFile(SftpFile f) {
        this.newFiles.addElement(f);
    }

    void addFailedTransfer(SftpFile f, SftpStatusException ex) {
        this.failedTransfers.put(f, ex);
    }

    void addUpdatedFile(SftpFile f) {
        this.updatedFiles.addElement(f);
    }

    void addDeletedFile(SftpFile f) {
        this.deletedFiles.addElement(f);
    }

    void addUnchangedFile(SftpFile f) {
        this.unchangedFiles.addElement(f);
    }

    public Vector getNewFiles() {
        return this.newFiles;
    }

    public Vector getUpdatedFiles() {
        return this.updatedFiles;
    }

    public Vector getUnchangedFiles() {
        return this.unchangedFiles;
    }

    public Vector getDeletedFiles() {
        return this.deletedFiles;
    }

    public Hashtable getFailedTransfers() {
        return this.failedTransfers;
    }

    public boolean containsFile(File f) {
        return this.unchangedFiles.contains(f) || this.newFiles.contains(f) || this.updatedFiles.contains(f) || this.deletedFiles.contains(f) || this.recursedDirectories.contains(f) || this.failedTransfers.containsKey(f);
    }

    public boolean containsFile(SftpFile f) {
        return this.unchangedFiles.contains(f) || this.newFiles.contains(f) || this.updatedFiles.contains(f) || this.deletedFiles.contains(f) || this.recursedDirectories.contains(f.getAbsolutePath()) || this.failedTransfers.containsKey(f);
    }

    public void addDirectoryOperation(DirectoryOperation op, File f) {
        this.addAll(op.getUpdatedFiles(), this.updatedFiles);
        this.addAll(op.getNewFiles(), this.newFiles);
        this.addAll(op.getUnchangedFiles(), this.unchangedFiles);
        this.addAll(op.getDeletedFiles(), this.deletedFiles);
        Enumeration e = op.failedTransfers.keys();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            this.failedTransfers.put(obj, op.failedTransfers.get(obj));
        }
        this.recursedDirectories.addElement(f);
    }

    void addAll(Vector source, Vector dest) {
        Enumeration e = source.elements();
        while (e.hasMoreElements()) {
            dest.addElement(e.nextElement());
        }
    }

    public int getFileCount() {
        return this.newFiles.size() + this.updatedFiles.size();
    }

    public void addDirectoryOperation(DirectoryOperation op, String file) {
        this.addAll(op.getUpdatedFiles(), this.updatedFiles);
        this.addAll(op.getNewFiles(), this.newFiles);
        this.addAll(op.getUnchangedFiles(), this.unchangedFiles);
        this.addAll(op.getDeletedFiles(), this.deletedFiles);
        Enumeration e = op.failedTransfers.keys();
        while (e.hasMoreElements()) {
            Object obj = e.nextElement();
            this.failedTransfers.put(obj, op.failedTransfers.get(obj));
        }
        this.recursedDirectories.addElement(file);
    }

    public long getTransferSize() throws SftpStatusException, SshException {
        SftpFile sftpfile;
        File file;
        Object obj;
        long size = 0L;
        Enumeration e = this.newFiles.elements();
        while (e.hasMoreElements()) {
            obj = e.nextElement();
            if (obj instanceof File) {
                file = (File)obj;
                if (!file.isFile()) continue;
                size += file.length();
                continue;
            }
            if (!(obj instanceof SftpFile) || !(sftpfile = (SftpFile)obj).isFile()) continue;
            size += sftpfile.getAttributes().getSize().longValue();
        }
        e = this.updatedFiles.elements();
        while (e.hasMoreElements()) {
            obj = e.nextElement();
            if (obj instanceof File) {
                file = (File)obj;
                if (!file.isFile()) continue;
                size += file.length();
                continue;
            }
            if (!(obj instanceof SftpFile) || !(sftpfile = (SftpFile)obj).isFile()) continue;
            size += sftpfile.getAttributes().getSize().longValue();
        }
        return size;
    }
}

