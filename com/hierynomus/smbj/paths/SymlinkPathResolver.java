/*
 * Decompiled with CFR 0.152.
 */
package com.hierynomus.smbj.paths;

import com.hierynomus.mserref.NtStatus;
import com.hierynomus.mssmb2.SMB2Error;
import com.hierynomus.mssmb2.SMB2Functions;
import com.hierynomus.mssmb2.SMB2Header;
import com.hierynomus.mssmb2.SMB2Packet;
import com.hierynomus.protocol.commons.Charsets;
import com.hierynomus.smbj.common.SmbPath;
import com.hierynomus.smbj.paths.PathResolveException;
import com.hierynomus.smbj.paths.PathResolver;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.StatusHandler;
import com.hierynomus.utils.Strings;
import java.util.List;

public class SymlinkPathResolver
implements PathResolver {
    private PathResolver wrapped;
    private StatusHandler statusHandler;

    public SymlinkPathResolver(final PathResolver wrapped) {
        this.wrapped = wrapped;
        this.statusHandler = new StatusHandler(){

            @Override
            public boolean isSuccess(long statusCode) {
                return statusCode == NtStatus.STATUS_STOPPED_ON_SYMLINK.getValue() || wrapped.statusHandler().isSuccess(statusCode);
            }
        };
    }

    @Override
    public SmbPath resolve(Session session, SMB2Packet responsePacket, SmbPath smbPath) throws PathResolveException {
        if (((SMB2Header)responsePacket.getHeader()).getStatusCode() == NtStatus.STATUS_STOPPED_ON_SYMLINK.getValue()) {
            SMB2Error.SymbolicLinkError symlinkData = SymlinkPathResolver.getSymlinkErrorData(responsePacket.getError());
            if (symlinkData == null) {
                throw new PathResolveException(((SMB2Header)responsePacket.getHeader()).getStatusCode(), "Create failed for " + smbPath + ": missing symlink data");
            }
            String target = this.resolveSymlinkTarget(smbPath.getPath(), symlinkData);
            return new SmbPath(smbPath.getHostname(), smbPath.getShareName(), target);
        }
        return this.wrapped.resolve(session, responsePacket, smbPath);
    }

    @Override
    public SmbPath resolve(Session session, SmbPath smbPath) throws PathResolveException {
        return this.wrapped.resolve(session, smbPath);
    }

    @Override
    public StatusHandler statusHandler() {
        return this.statusHandler;
    }

    private static SMB2Error.SymbolicLinkError getSymlinkErrorData(SMB2Error error) {
        if (error != null) {
            List<SMB2Error.SMB2ErrorData> errorData = error.getErrorData();
            for (SMB2Error.SMB2ErrorData errorDatum : errorData) {
                if (!(errorDatum instanceof SMB2Error.SymbolicLinkError)) continue;
                return (SMB2Error.SymbolicLinkError)errorDatum;
            }
        }
        return null;
    }

    private String resolveSymlinkTarget(String originalFileName, SMB2Error.SymbolicLinkError symlinkData) {
        String target;
        int unparsedPathLength = symlinkData.getUnparsedPathLength();
        String unparsedPath = this.getSymlinkUnparsedPath(originalFileName, unparsedPathLength);
        String substituteName = symlinkData.getSubstituteName();
        if (symlinkData.isAbsolute()) {
            target = substituteName + unparsedPath;
        } else {
            String parsedPath = this.getSymlinkParsedPath(originalFileName, unparsedPathLength);
            StringBuilder b = new StringBuilder();
            int startIndex = parsedPath.lastIndexOf("\\");
            if (startIndex != -1) {
                b.append(parsedPath, 0, startIndex);
                b.append('\\');
            }
            b.append(substituteName);
            b.append(unparsedPath);
            target = b.toString();
        }
        return this.normalizePath(target);
    }

    private String getSymlinkParsedPath(String fileName, int unparsedPathLength) {
        byte[] fileNameBytes = SMB2Functions.unicode(fileName);
        return new String(fileNameBytes, 0, fileNameBytes.length - unparsedPathLength, Charsets.UTF_16LE);
    }

    private String getSymlinkUnparsedPath(String fileName, int unparsedPathLength) {
        byte[] fileNameBytes = SMB2Functions.unicode(fileName);
        return new String(fileNameBytes, fileNameBytes.length - unparsedPathLength, unparsedPathLength, Charsets.UTF_16LE);
    }

    private String normalizePath(String path) {
        List<String> parts = Strings.split(path, '\\');
        int i = 0;
        while (i < parts.size()) {
            String s = parts.get(i);
            if (".".equals(s)) {
                parts.remove(i);
                continue;
            }
            if ("..".equals(s)) {
                if (i > 0) {
                    parts.remove(i--);
                }
                parts.remove(i);
                continue;
            }
            ++i;
        }
        return Strings.join(parts, '\\');
    }
}

