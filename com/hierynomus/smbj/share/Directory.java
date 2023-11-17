/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.share;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.msfscc.FileInformationClass;
import com.hierynomus.msfscc.fileinformation.FileDirectoryQueryableInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileInformation;
import com.hierynomus.msfscc.fileinformation.FileInformationFactory;
import com.hierynomus.mssmb2.SMB2FileId;
import com.hierynomus.mssmb2.SMB2Header;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.mssmb2.messages.SMB2QueryDirectoryRequest;
import com.hierynomus.mssmb2.messages.SMB2QueryDirectoryResponse;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Directory
extends DiskEntry
implements Iterable<FileIdBothDirectoryInformation> {
    Directory(SMB2FileId fileId, DiskShare diskShare, String fileName) {
        super(fileId, diskShare, fileName);
    }

    public List<FileIdBothDirectoryInformation> list() throws SMBApiException {
        return this.list(FileIdBothDirectoryInformation.class);
    }

    public <F extends FileDirectoryQueryableInformation> List<F> list(Class<F> informationClass) throws SMBApiException {
        return this.list(informationClass, null);
    }

    public <F extends FileDirectoryQueryableInformation> List<F> list(Class<F> informationClass, String searchPattern) {
        ArrayList<F> fileList = new ArrayList<F>();
        Iterator<F> iterator = this.iterator(informationClass, searchPattern);
        while (iterator.hasNext()) {
            fileList.add(iterator.next());
        }
        return fileList;
    }

    @Override
    public Iterator<FileIdBothDirectoryInformation> iterator() {
        return this.iterator(FileIdBothDirectoryInformation.class);
    }

    public <F extends FileDirectoryQueryableInformation> Iterator<F> iterator(Class<F> informationClass) {
        return this.iterator(informationClass, null);
    }

    public <F extends FileDirectoryQueryableInformation> Iterator<F> iterator(Class<F> informationClass, String searchPattern) {
        return new DirectoryIterator<F>(informationClass, searchPattern);
    }

    @Override
    public SMB2FileId getFileId() {
        return this.fileId;
    }

    public String toString() {
        return String.format("Directory{fileId=%s, fileName='%s'}", this.fileId, this.fileName);
    }

    private class DirectoryIterator<F extends FileDirectoryQueryableInformation>
    implements Iterator<F> {
        private final FileInformation.Decoder<F> decoder;
        private Iterator<F> currentIterator;
        private byte[] currentBuffer;
        private F next;
        private String searchPattern;

        DirectoryIterator(Class<F> informationClass, String searchPattern) {
            this.decoder = FileInformationFactory.getDecoder(informationClass);
            this.searchPattern = searchPattern;
            this.queryDirectory(true);
            this.next = this.prepareNext();
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public F next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            F fileInfo = this.next;
            this.next = this.prepareNext();
            return fileInfo;
        }

        private F prepareNext() {
            while (this.currentIterator != null) {
                if (this.currentIterator.hasNext()) {
                    return (F)((FileDirectoryQueryableInformation)this.currentIterator.next());
                }
                this.queryDirectory(false);
            }
            return null;
        }

        private void queryDirectory(boolean firstQuery) {
            DiskShare share = Directory.this.share;
            EnumSet<SMB2QueryDirectoryRequest.SMB2QueryDirectoryFlags> flags = firstQuery ? EnumSet.of(SMB2QueryDirectoryRequest.SMB2QueryDirectoryFlags.SMB2_RESTART_SCANS) : EnumSet.noneOf(SMB2QueryDirectoryRequest.SMB2QueryDirectoryFlags.class);
            FileInformationClass informationClass = this.decoder.getInformationClass();
            SMB2QueryDirectoryResponse qdResp = share.queryDirectory(Directory.this.fileId, flags, informationClass, this.searchPattern);
            long status = ((SMB2Header)qdResp.getHeader()).getStatusCode();
            byte[] buffer = qdResp.getOutputBuffer();
            if (status == NtStatus.STATUS_NO_MORE_FILES.getValue() || status == NtStatus.STATUS_NO_SUCH_FILE.getValue() || this.currentBuffer != null && Arrays.equals(this.currentBuffer, buffer)) {
                this.currentIterator = null;
                this.currentBuffer = null;
            } else {
                this.currentBuffer = buffer;
                this.currentIterator = FileInformationFactory.createFileInformationIterator(this.currentBuffer, this.decoder);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

