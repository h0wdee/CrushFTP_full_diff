/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.msfscc.fileinformation;

import com.hierynomus.msdtyp.FileTime;
import com.hierynomus.msdtyp.MsDataTypes;
import com.hierynomus.msfscc.FileInformationClass;
import com.hierynomus.msfscc.fileinformation.FileAccessInformation;
import com.hierynomus.msfscc.fileinformation.FileAlignmentInformation;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileAllocationInformation;
import com.hierynomus.msfscc.fileinformation.FileBasicInformation;
import com.hierynomus.msfscc.fileinformation.FileBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileDirectoryQueryableInformation;
import com.hierynomus.msfscc.fileinformation.FileDispositionInformation;
import com.hierynomus.msfscc.fileinformation.FileEaInformation;
import com.hierynomus.msfscc.fileinformation.FileEndOfFileInformation;
import com.hierynomus.msfscc.fileinformation.FileFullDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileIdFullDirectoryInformation;
import com.hierynomus.msfscc.fileinformation.FileInformation;
import com.hierynomus.msfscc.fileinformation.FileInternalInformation;
import com.hierynomus.msfscc.fileinformation.FileLinkInformation;
import com.hierynomus.msfscc.fileinformation.FileModeInformation;
import com.hierynomus.msfscc.fileinformation.FileNamesInformation;
import com.hierynomus.msfscc.fileinformation.FilePositionInformation;
import com.hierynomus.msfscc.fileinformation.FileRenameInformation;
import com.hierynomus.msfscc.fileinformation.FileStandardInformation;
import com.hierynomus.msfscc.fileinformation.FileStreamInformation;
import com.hierynomus.msfscc.fileinformation.FileStreamInformationItem;
import com.hierynomus.protocol.commons.Charsets;
import com.hierynomus.protocol.commons.buffer.Buffer;
import com.hierynomus.protocol.commons.buffer.Endian;
import com.hierynomus.smbj.common.SMBRuntimeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class FileInformationFactory {
    private static final Map<Class, FileInformation.Encoder> encoders = new HashMap<Class, FileInformation.Encoder>();
    private static final Map<Class, FileInformation.Decoder> decoders = new HashMap<Class, FileInformation.Decoder>();

    private FileInformationFactory() {
    }

    public static <F extends FileInformation> FileInformation.Encoder<F> getEncoder(F fileInformation) {
        return FileInformationFactory.getEncoder(fileInformation.getClass());
    }

    public static <F extends FileInformation> FileInformation.Encoder<F> getEncoder(Class<F> fileInformationClass) {
        FileInformation.Encoder encoder = encoders.get(fileInformationClass);
        if (encoder == null) {
            throw new IllegalArgumentException("FileInformationClass not supported - " + fileInformationClass);
        }
        return encoder;
    }

    public static <F extends FileInformation> FileInformation.Decoder<F> getDecoder(Class<F> fileInformationClass) {
        FileInformation.Decoder decoder = decoders.get(fileInformationClass);
        if (decoder == null) {
            throw new IllegalArgumentException("FileInformationClass not supported - " + fileInformationClass);
        }
        return decoder;
    }

    public static <F extends FileDirectoryQueryableInformation> List<F> parseFileInformationList(byte[] data, FileInformation.Decoder<F> decoder) {
        ArrayList<F> _fileInfoList = new ArrayList<F>();
        Iterator<F> iterator = FileInformationFactory.createFileInformationIterator(data, decoder);
        while (iterator.hasNext()) {
            _fileInfoList.add(iterator.next());
        }
        return _fileInfoList;
    }

    public static <F extends FileDirectoryQueryableInformation> Iterator<F> createFileInformationIterator(byte[] data, FileInformation.Decoder<F> decoder) {
        return new FileInfoIterator<F>(data, decoder, 0);
    }

    public static FileAllInformation parseFileAllInformation(Buffer<?> buffer) throws Buffer.BufferException {
        FileBasicInformation basicInformation = FileInformationFactory.parseFileBasicInformation(buffer);
        FileStandardInformation standardInformation = FileInformationFactory.parseFileStandardInformation(buffer);
        FileInternalInformation internalInformation = FileInformationFactory.parseFileInternalInformation(buffer);
        FileEaInformation eaInformation = FileInformationFactory.parseFileEaInformation(buffer);
        FileAccessInformation accessInformation = FileInformationFactory.parseFileAccessInformation(buffer);
        FilePositionInformation positionInformation = FileInformationFactory.parseFilePositionInformation(buffer);
        FileModeInformation modeInformation = FileInformationFactory.parseFileModeInformation(buffer);
        FileAlignmentInformation alignmentInformation = FileInformationFactory.parseFileAlignmentInformation(buffer);
        String nameInformation = FileInformationFactory.parseFileNameInformation(buffer);
        return new FileAllInformation(basicInformation, standardInformation, internalInformation, eaInformation, accessInformation, positionInformation, modeInformation, alignmentInformation, nameInformation);
    }

    private static String parseFileNameInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long fileNameLen = buffer.readUInt32();
        return buffer.readString(Charsets.UTF_16LE, (int)fileNameLen / 2);
    }

    private static FileBasicInformation parseFileBasicInformation(Buffer<?> buffer) throws Buffer.BufferException {
        FileTime creationTime = MsDataTypes.readFileTime(buffer);
        FileTime lastAccessTime = MsDataTypes.readFileTime(buffer);
        FileTime lastWriteTime = MsDataTypes.readFileTime(buffer);
        FileTime changeTime = MsDataTypes.readFileTime(buffer);
        long fileAttributes = buffer.readUInt32();
        buffer.skip(4);
        return new FileBasicInformation(creationTime, lastAccessTime, lastWriteTime, changeTime, fileAttributes);
    }

    private static FileStandardInformation parseFileStandardInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long allocationSize = buffer.readLong();
        long endOfFile = buffer.readUInt64();
        long numberOfLinks = buffer.readUInt32();
        boolean deletePending = buffer.readBoolean();
        boolean directory = buffer.readBoolean();
        buffer.skip(2);
        return new FileStandardInformation(allocationSize, endOfFile, numberOfLinks, deletePending, directory);
    }

    private static FileInternalInformation parseFileInternalInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long indexNumber = buffer.readLong();
        return new FileInternalInformation(indexNumber);
    }

    private static FileEaInformation parseFileEaInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long eaSize = buffer.readUInt32();
        return new FileEaInformation(eaSize);
    }

    private static FileStreamInformation parseFileStreamInformation(Buffer<?> buffer) throws Buffer.BufferException {
        ArrayList<FileStreamInformationItem> streamList = new ArrayList<FileStreamInformationItem>();
        long currEntry = 0L;
        long nextEntry = 0L;
        do {
            buffer.rpos((int)(currEntry += nextEntry));
            nextEntry = buffer.readUInt32();
            long nameLen = buffer.readUInt32();
            long size = buffer.readLong();
            long allocSize = buffer.readLong();
            String name = buffer.readString(Charsets.UTF_16LE, (int)nameLen / 2);
            streamList.add(new FileStreamInformationItem(size, allocSize, name));
        } while (nextEntry != 0L);
        return new FileStreamInformation(streamList);
    }

    private static FileAccessInformation parseFileAccessInformation(Buffer<?> buffer) throws Buffer.BufferException {
        int accessFlags = (int)buffer.readUInt32();
        return new FileAccessInformation(accessFlags);
    }

    private static FilePositionInformation parseFilePositionInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long currentByteOffset = buffer.readLong();
        return new FilePositionInformation(currentByteOffset);
    }

    private static FileModeInformation parseFileModeInformation(Buffer<?> buffer) throws Buffer.BufferException {
        int mode = (int)buffer.readUInt32();
        return new FileModeInformation(mode);
    }

    private static FileAlignmentInformation parseFileAlignmentInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long alignmentReq = buffer.readUInt32();
        return new FileAlignmentInformation(alignmentReq);
    }

    public static FileBothDirectoryInformation parseFileBothDirectoryInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long nextOffset = buffer.readUInt32();
        long fileIndex = buffer.readUInt32();
        FileTime creationTime = MsDataTypes.readFileTime(buffer);
        FileTime lastAccessTime = MsDataTypes.readFileTime(buffer);
        FileTime lastWriteTime = MsDataTypes.readFileTime(buffer);
        FileTime changeTime = MsDataTypes.readFileTime(buffer);
        long endOfFile = buffer.readUInt64();
        long allocationSize = buffer.readUInt64();
        long fileAttributes = buffer.readUInt32();
        long fileNameLen = buffer.readUInt32();
        long eaSize = buffer.readUInt32();
        byte shortNameLen = buffer.readByte();
        buffer.readByte();
        byte[] shortNameBytes = buffer.readRawBytes(24);
        String shortName = new String(shortNameBytes, 0, (int)shortNameLen, Charsets.UTF_16LE);
        String fileName = buffer.readString(Charsets.UTF_16LE, (int)fileNameLen / 2);
        FileBothDirectoryInformation fi = new FileBothDirectoryInformation(nextOffset, fileIndex, fileName, creationTime, lastAccessTime, lastWriteTime, changeTime, endOfFile, allocationSize, fileAttributes, eaSize, shortName);
        return fi;
    }

    public static FileDirectoryInformation parseFileDirectoryInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long nextOffset = buffer.readUInt32();
        long fileIndex = buffer.readUInt32();
        FileTime creationTime = MsDataTypes.readFileTime(buffer);
        FileTime lastAccessTime = MsDataTypes.readFileTime(buffer);
        FileTime lastWriteTime = MsDataTypes.readFileTime(buffer);
        FileTime changeTime = MsDataTypes.readFileTime(buffer);
        long endOfFile = buffer.readUInt64();
        long allocationSize = buffer.readUInt64();
        long fileAttributes = buffer.readUInt32();
        String fileName = FileInformationFactory.parseFileNameInformation(buffer);
        FileDirectoryInformation fi = new FileDirectoryInformation(nextOffset, fileIndex, fileName, creationTime, lastAccessTime, lastWriteTime, changeTime, endOfFile, allocationSize, fileAttributes);
        return fi;
    }

    public static FileFullDirectoryInformation parseFileFullDirectoryInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long nextOffset = buffer.readUInt32();
        long fileIndex = buffer.readUInt32();
        FileTime creationTime = MsDataTypes.readFileTime(buffer);
        FileTime lastAccessTime = MsDataTypes.readFileTime(buffer);
        FileTime lastWriteTime = MsDataTypes.readFileTime(buffer);
        FileTime changeTime = MsDataTypes.readFileTime(buffer);
        long endOfFile = buffer.readUInt64();
        long allocationSize = buffer.readUInt64();
        long fileAttributes = buffer.readUInt32();
        long fileNameLen = buffer.readUInt32();
        long eaSize = buffer.readUInt32();
        String fileName = buffer.readString(Charsets.UTF_16LE, (int)fileNameLen / 2);
        FileFullDirectoryInformation fi = new FileFullDirectoryInformation(nextOffset, fileIndex, fileName, creationTime, lastAccessTime, lastWriteTime, changeTime, endOfFile, allocationSize, fileAttributes, eaSize);
        return fi;
    }

    public static FileIdBothDirectoryInformation parseFileIdBothDirectoryInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long nextOffset = buffer.readUInt32();
        long fileIndex = buffer.readUInt32();
        FileTime creationTime = MsDataTypes.readFileTime(buffer);
        FileTime lastAccessTime = MsDataTypes.readFileTime(buffer);
        FileTime lastWriteTime = MsDataTypes.readFileTime(buffer);
        FileTime changeTime = MsDataTypes.readFileTime(buffer);
        long endOfFile = buffer.readUInt64();
        long allocationSize = buffer.readUInt64();
        long fileAttributes = buffer.readUInt32();
        long fileNameLen = buffer.readUInt32();
        long eaSize = buffer.readUInt32();
        byte shortNameLen = buffer.readByte();
        buffer.readByte();
        byte[] shortNameBytes = buffer.readRawBytes(24);
        String shortName = new String(shortNameBytes, 0, (int)shortNameLen, Charsets.UTF_16LE);
        buffer.readUInt16();
        byte[] fileId = buffer.readRawBytes(8);
        String fileName = buffer.readString(Charsets.UTF_16LE, (int)fileNameLen / 2);
        FileIdBothDirectoryInformation fi = new FileIdBothDirectoryInformation(nextOffset, fileIndex, fileName, creationTime, lastAccessTime, lastWriteTime, changeTime, endOfFile, allocationSize, fileAttributes, eaSize, shortName, fileId);
        return fi;
    }

    public static FileIdFullDirectoryInformation parseFileIdFullDirectoryInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long nextOffset = buffer.readUInt32();
        long fileIndex = buffer.readUInt32();
        FileTime creationTime = MsDataTypes.readFileTime(buffer);
        FileTime lastAccessTime = MsDataTypes.readFileTime(buffer);
        FileTime lastWriteTime = MsDataTypes.readFileTime(buffer);
        FileTime changeTime = MsDataTypes.readFileTime(buffer);
        long endOfFile = buffer.readUInt64();
        long allocationSize = buffer.readUInt64();
        long fileAttributes = buffer.readUInt32();
        long fileNameLen = buffer.readUInt32();
        long eaSize = buffer.readUInt32();
        buffer.skip(4);
        byte[] fileId = buffer.readRawBytes(8);
        String fileName = buffer.readString(Charsets.UTF_16LE, (int)fileNameLen / 2);
        FileIdFullDirectoryInformation fi = new FileIdFullDirectoryInformation(nextOffset, fileIndex, fileName, creationTime, lastAccessTime, lastWriteTime, changeTime, endOfFile, allocationSize, fileAttributes, eaSize, fileId);
        return fi;
    }

    public static FileNamesInformation parseFileNamesInformation(Buffer<?> buffer) throws Buffer.BufferException {
        long nextOffset = buffer.readUInt32();
        long fileIndex = buffer.readUInt32();
        long fileNameLen = buffer.readUInt32();
        String fileName = buffer.readString(Charsets.UTF_16LE, (int)fileNameLen / 2);
        return new FileNamesInformation(nextOffset, fileIndex, fileName);
    }

    public static void writeFileRenameInformation(FileRenameInformation information, Buffer<?> buffer) {
        buffer.putByte((byte)(information.isReplaceIfExists() ? 1 : 0));
        buffer.putRawBytes(new byte[]{0, 0, 0, 0, 0, 0, 0});
        buffer.putUInt64(information.getRootDirectory());
        buffer.putUInt32((long)information.getFileNameLength() * 2L);
        buffer.putRawBytes(information.getFileName().getBytes(Charsets.UTF_16LE));
    }

    static {
        decoders.put(FileAccessInformation.class, new FileInformation.Decoder<FileAccessInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileAccessInformation;
            }

            @Override
            public FileAccessInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileAccessInformation(inputBuffer);
            }
        });
        decoders.put(FileAlignmentInformation.class, new FileInformation.Decoder<FileAlignmentInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileAlignmentInformation;
            }

            @Override
            public FileAlignmentInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileAlignmentInformation(inputBuffer);
            }
        });
        decoders.put(FileAllInformation.class, new FileInformation.Decoder<FileAllInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileAllInformation;
            }

            @Override
            public FileAllInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileAllInformation(inputBuffer);
            }
        });
        FileInformation.Codec<FileAllocationInformation> allocationCodec = new FileInformation.Codec<FileAllocationInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileAllocationInformation;
            }

            @Override
            public FileAllocationInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                long allocationSize = inputBuffer.readLong();
                return new FileAllocationInformation(allocationSize);
            }

            @Override
            public void write(FileAllocationInformation info, Buffer outputBuffer) {
                outputBuffer.putLong(info.getAllocationSize());
            }
        };
        decoders.put(FileAllocationInformation.class, allocationCodec);
        encoders.put(FileAllocationInformation.class, allocationCodec);
        FileInformation.Codec<FileBasicInformation> basicCodec = new FileInformation.Codec<FileBasicInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileBasicInformation;
            }

            @Override
            public FileBasicInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileBasicInformation(inputBuffer);
            }

            @Override
            public void write(FileBasicInformation info, Buffer outputBuffer) {
                MsDataTypes.putFileTime(info.getCreationTime(), outputBuffer);
                MsDataTypes.putFileTime(info.getLastAccessTime(), outputBuffer);
                MsDataTypes.putFileTime(info.getLastWriteTime(), outputBuffer);
                MsDataTypes.putFileTime(info.getChangeTime(), outputBuffer);
                outputBuffer.putUInt32(info.getFileAttributes());
                outputBuffer.putUInt32(0L);
            }
        };
        decoders.put(FileBasicInformation.class, basicCodec);
        encoders.put(FileBasicInformation.class, basicCodec);
        FileInformation.Encoder<FileDispositionInformation> dispositionCodec = new FileInformation.Encoder<FileDispositionInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileDispositionInformation;
            }

            @Override
            public void write(FileDispositionInformation info, Buffer outputBuffer) {
                outputBuffer.putBoolean(info.isDeleteOnClose());
            }
        };
        encoders.put(FileDispositionInformation.class, dispositionCodec);
        decoders.put(FileEaInformation.class, new FileInformation.Decoder<FileEaInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileEaInformation;
            }

            @Override
            public FileEaInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileEaInformation(inputBuffer);
            }
        });
        decoders.put(FileStreamInformation.class, new FileInformation.Decoder<FileStreamInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileStreamInformation;
            }

            @Override
            public FileStreamInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileStreamInformation(inputBuffer);
            }
        });
        FileInformation.Encoder<FileEndOfFileInformation> endOfFileCodec = new FileInformation.Encoder<FileEndOfFileInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileEndOfFileInformation;
            }

            @Override
            public void write(FileEndOfFileInformation info, Buffer outputBuffer) {
                outputBuffer.putLong(info.getEndOfFile());
            }
        };
        encoders.put(FileEndOfFileInformation.class, endOfFileCodec);
        decoders.put(FileInternalInformation.class, new FileInformation.Decoder<FileInternalInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileInternalInformation;
            }

            @Override
            public FileInternalInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileInternalInformation(inputBuffer);
            }
        });
        FileInformation.Codec<FileModeInformation> modeCodec = new FileInformation.Codec<FileModeInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileModeInformation;
            }

            @Override
            public FileModeInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileModeInformation(inputBuffer);
            }

            @Override
            public void write(FileModeInformation info, Buffer outputBuffer) {
                outputBuffer.putUInt32((long)info.getMode() & 0xFFFFFFFFL);
            }
        };
        decoders.put(FileModeInformation.class, modeCodec);
        encoders.put(FileModeInformation.class, modeCodec);
        decoders.put(FilePositionInformation.class, new FileInformation.Decoder<FilePositionInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FilePositionInformation;
            }

            @Override
            public FilePositionInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFilePositionInformation(inputBuffer);
            }
        });
        decoders.put(FileStandardInformation.class, new FileInformation.Decoder<FileStandardInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileStandardInformation;
            }

            @Override
            public FileStandardInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileStandardInformation(inputBuffer);
            }
        });
        decoders.put(FileBothDirectoryInformation.class, new FileInformation.Decoder<FileBothDirectoryInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileBothDirectoryInformation;
            }

            @Override
            public FileBothDirectoryInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileBothDirectoryInformation(inputBuffer);
            }
        });
        decoders.put(FileDirectoryInformation.class, new FileInformation.Decoder<FileDirectoryInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileDirectoryInformation;
            }

            @Override
            public FileDirectoryInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileDirectoryInformation(inputBuffer);
            }
        });
        decoders.put(FileFullDirectoryInformation.class, new FileInformation.Decoder<FileFullDirectoryInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileFullDirectoryInformation;
            }

            @Override
            public FileFullDirectoryInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileFullDirectoryInformation(inputBuffer);
            }
        });
        decoders.put(FileIdBothDirectoryInformation.class, new FileInformation.Decoder<FileIdBothDirectoryInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileIdBothDirectoryInformation;
            }

            @Override
            public FileIdBothDirectoryInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileIdBothDirectoryInformation(inputBuffer);
            }
        });
        decoders.put(FileIdFullDirectoryInformation.class, new FileInformation.Decoder<FileIdFullDirectoryInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileIdFullDirectoryInformation;
            }

            @Override
            public FileIdFullDirectoryInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileIdFullDirectoryInformation(inputBuffer);
            }
        });
        decoders.put(FileNamesInformation.class, new FileInformation.Decoder<FileNamesInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileNamesInformation;
            }

            @Override
            public FileNamesInformation read(Buffer inputBuffer) throws Buffer.BufferException {
                return FileInformationFactory.parseFileNamesInformation(inputBuffer);
            }
        });
        FileInformation.Encoder<FileRenameInformation> renameCodec = new FileInformation.Encoder<FileRenameInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileRenameInformation;
            }

            @Override
            public void write(FileRenameInformation info, Buffer outputBuffer) {
                FileInformationFactory.writeFileRenameInformation(info, outputBuffer);
            }
        };
        encoders.put(FileRenameInformation.class, renameCodec);
        FileInformation.Encoder<FileLinkInformation> linkCodec = new FileInformation.Encoder<FileLinkInformation>(){

            @Override
            public FileInformationClass getInformationClass() {
                return FileInformationClass.FileLinkInformation;
            }

            @Override
            public void write(FileLinkInformation info, Buffer outputBuffer) {
                FileInformationFactory.writeFileRenameInformation(info, outputBuffer);
            }
        };
        encoders.put(FileLinkInformation.class, linkCodec);
    }

    private static class FileInfoIterator<F extends FileDirectoryQueryableInformation>
    implements Iterator<F> {
        private final Buffer.PlainBuffer buffer;
        private final FileInformation.Decoder<F> decoder;
        private int offsetStart;
        private F next;

        FileInfoIterator(byte[] data, FileInformation.Decoder<F> decoder, int offsetStart) {
            this.buffer = new Buffer.PlainBuffer(data, Endian.LE);
            this.decoder = decoder;
            this.offsetStart = offsetStart;
            this.next = this.prepareNext();
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public F next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            F fileInfo = this.next;
            this.next = this.prepareNext();
            return fileInfo;
        }

        private F prepareNext() {
            try {
                FileDirectoryQueryableInformation nxt = null;
                while (nxt == null && this.offsetStart != -1) {
                    this.buffer.rpos(this.offsetStart);
                    FileDirectoryQueryableInformation fileInfo = (FileDirectoryQueryableInformation)this.decoder.read(this.buffer);
                    int nextOffset = (int)fileInfo.getNextOffset();
                    this.offsetStart = nextOffset == 0 ? -1 : (this.offsetStart += nextOffset);
                    nxt = fileInfo;
                }
                return (F)nxt;
            }
            catch (Buffer.BufferException e) {
                throw new SMBRuntimeException(e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

