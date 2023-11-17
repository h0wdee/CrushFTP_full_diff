/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.rpc;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.Share;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.client.rpc.RpcReader;
import com.visuality.nq.client.rpc.RpcWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SecurityDescriptor;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.UUID;
import java.util.Iterator;
import java.util.LinkedList;

public class Srvsvc
extends Dcerpc {
    private static final int OP_SHAREADD = 14;
    private static final int OP_SHAREENUM = 15;
    private static final int OP_SHAREGETINFO = 16;
    private static final int OP_SHARESETINFO = 17;
    private static final int OP_SHAREDEL = 18;
    private static final Dcerpc.Context context1 = new Dcerpc.Context();
    private static final Dcerpc.Context context2;
    private static final Dcerpc.RpcDescriptor theDescriptor;
    private static final int FLAG_HIDDEN = Integer.MIN_VALUE;
    private int numShares;
    private LinkedList shareList = new LinkedList();

    public Srvsvc(String server) throws NqException {
        super(server, theDescriptor);
    }

    public Srvsvc(String server, Credentials creds) throws NqException {
        super(server, theDescriptor, creds);
    }

    public Srvsvc(Credentials creds, Server server) throws NqException {
        super(server, theDescriptor, creds);
    }

    public Iterator shareEnum() throws NqException {
        TraceLog.get().enter(200);
        RpcWriter writer = this.startCall(15);
        Dcerpc.ObjectHandle serverObject = this.startObject();
        serverObject.writeStringRef(writer, "\\\\" + this.serverName.toUpperCase());
        serverObject.placeStrings(writer, true);
        writer.align();
        writer.writeInt4(1);
        writer.writeCardinal(1L);
        this.writeEmptyArray(writer);
        writer.writeInt4(-1);
        writer.writeCardinal(0L);
        RpcReader reader = this.performRpc(writer);
        long dummy = reader.readCardinal();
        dummy = reader.readCardinal();
        dummy = reader.readCardinal();
        this.numShares = reader.readInt4();
        dummy = reader.readCardinal();
        dummy = reader.readCardinal();
        this.shareList.clear();
        try {
            for (int i = 0; i < this.numShares; ++i) {
                dummy = reader.readCardinal();
                int type = reader.readInt4();
                Share.Info info = new Share.Info();
                info.isHidden = 0 != (type & Integer.MIN_VALUE);
                info.type = type & 0xFFFF;
                dummy = reader.readCardinal();
                this.shareList.add(info);
            }
        }
        catch (Exception e) {
            String errorMessage = "The format of the response can't be parsed: " + e.getClass().getName();
            TraceLog.get().error(errorMessage + "; Last line of stack trace: " + e.getStackTrace()[0]);
            TraceLog.get().exit(200);
            throw new NqException(errorMessage, -21);
        }
        Iterator iter = this.shareList.iterator();
        try {
            while (iter.hasNext()) {
                Share.Info info = (Share.Info)iter.next();
                info.name = reader.readReferencedString(true);
                info.comment = reader.readReferencedString(true);
            }
        }
        catch (Exception e) {
            String errorMessage = "The format of the response can't be parsed: " + e.getClass().getName();
            TraceLog.get().error(errorMessage + "; Last line of stack trace: " + e.getStackTrace()[0]);
            iter.remove();
            while (iter.hasNext()) {
                iter.next();
                iter.remove();
            }
        }
        TraceLog.get().exit(200);
        return this.shareList.iterator();
    }

    public void shareAdd(String shareName, String shareComment, String path, SecurityDescriptor sd) throws NqException {
        TraceLog.get().enter(200);
        RpcWriter writer = this.startCall(14);
        Dcerpc.ObjectHandle serverObject = this.startObject();
        serverObject.writeStringRef(writer, this.serverName);
        serverObject.placeStrings(writer, true);
        Dcerpc.ObjectHandle InfoObject = this.startObject();
        writer.align(0, 4);
        writer.writeInt4(502);
        writer.writeCardinal(502L);
        writer.writeCardinal(this.nextRefId());
        InfoObject.writeStringRef(writer, shareName);
        writer.writeInt4(0);
        InfoObject.writeStringRef(writer, shareComment);
        writer.writeInt4(0);
        writer.writeInt4(-1);
        writer.writeInt4(0);
        InfoObject.writeStringRef(writer, path);
        writer.writeCardinal(0L);
        int sdSizePos = writer.getOffset();
        writer.writeInt4(0);
        writer.writeCardinal(this.nextRefId());
        InfoObject.placeStrings(writer, true);
        writer.align();
        int sdSizePos1 = writer.getOffset();
        writer.writeCardinal(0L);
        int sdSize = SecurityDescriptor.EVERYONE_RO.write(writer);
        writer.writeInt4(sdSizePos, sdSize);
        writer.writeCardinal(sdSizePos1, sdSize);
        writer.writeCardinal(this.nextRefId());
        writer.writeInt4(0);
        RpcReader reader = this.performRpc(writer);
        reader.skip(8);
        this.checkResult(reader);
        TraceLog.get().exit(200);
    }

    public void shareDel(String shareName) throws NqException {
        TraceLog.get().enter(200);
        RpcWriter writer = this.startCall(18);
        Dcerpc.ObjectHandle handle = this.startObject();
        handle.writeStringRef(writer, this.serverName);
        handle.placeStrings(writer, true);
        writer.align(0, 4);
        writer.writeRpcString(shareName, true);
        writer.align(0, 4);
        writer.writeInt4(0);
        RpcReader reader = this.performRpc(writer);
        this.checkResult(reader);
        TraceLog.get().exit(200);
    }

    public Share.Info shareGetInfo(String shareName) throws NqException {
        TraceLog.get().enter("shareName = " + shareName, 200);
        RpcWriter writer = this.startCall(16);
        Dcerpc.ObjectHandle handle = this.startObject();
        handle.writeStringRef(writer, this.serverName);
        handle.placeStrings(writer, true);
        writer.align(0, 4);
        writer.writeRpcString(shareName, true);
        writer.align(0, 4);
        writer.writeInt4(502);
        RpcReader reader = this.performRpc(writer);
        Share.Info info = new Share.Info();
        long dummy = reader.readCardinal();
        long info2 = reader.readCardinal();
        if (0L == info2) {
            TraceLog.get().exit(200);
            throw new SmbException("retrive share info failed", -1073741790);
        }
        long nameRefId = reader.readCardinal();
        info.type = reader.readInt4();
        long commentRefId = reader.readCardinal();
        info.permissions = reader.readInt4();
        info.maxusers = reader.readInt4();
        reader.skip(4);
        long pathRefId = reader.readCardinal();
        long passRefId = reader.readCardinal();
        reader.skip(4);
        long aclRefId = reader.readCardinal();
        if (nameRefId != 0L) {
            info.name = reader.readReferencedString(true);
        }
        if (commentRefId != 0L) {
            info.comment = reader.readReferencedString(true);
        }
        if (pathRefId != 0L) {
            info.path = reader.readReferencedString(true);
        }
        if (passRefId != 0L) {
            reader.readReferencedString(true);
        }
        if (aclRefId != 0L) {
            dummy = reader.readCardinal();
            info.sd = new SecurityDescriptor(reader);
        }
        TraceLog.get().exit(200);
        return info;
    }

    public void shareSetInfo(String shareName, Share.Info info) throws NqException {
        TraceLog.get().enter(200);
        RpcWriter writer = this.startCall(17);
        Dcerpc.ObjectHandle handle = this.startObject();
        handle.writeStringRef(writer, this.serverName);
        handle.placeStrings(writer, true);
        writer.align(0, 4);
        writer.writeRpcString(shareName, true);
        writer.align(0, 4);
        Dcerpc.ObjectHandle infoHandle = this.startObject();
        writer.writeInt4(502);
        writer.writeCardinal(502L);
        writer.writeCardinal(this.nextRefId());
        infoHandle.writeStringRef(writer, info.name);
        writer.writeInt4(info.type);
        infoHandle.writeStringRef(writer, info.comment);
        writer.writeInt4(info.permissions);
        writer.writeInt4(info.maxusers);
        writer.writeInt4(0);
        infoHandle.writeStringRef(writer, info.path);
        writer.writeCardinal(0L);
        int sdSizePos = writer.getOffset();
        writer.writeInt4(0);
        if (info.sd == null) {
            writer.writeCardinal(0L);
        } else {
            writer.writeCardinal(this.nextRefId());
        }
        infoHandle.placeStrings(writer, true);
        int sdSizePos1 = writer.getOffset();
        writer.writeCardinal(0L);
        int sdSize = info.sd == null ? 0 : info.sd.write(writer);
        writer.writeInt4(sdSizePos, sdSize);
        writer.writeCardinal(sdSizePos1, sdSize);
        writer.writeCardinal(this.nextRefId());
        writer.writeInt4(0);
        RpcReader reader = this.performRpc(writer);
        reader.skip(4);
        this.checkResult(reader);
        TraceLog.get().exit(200);
    }

    static {
        Srvsvc.context1.syntaxGuid = new UUID(1261588424, 5744, 467, new byte[]{18, 120}, new byte[]{90, 71, -65, 110, -31, -120});
        Srvsvc.context1.versMajor = 3;
        Srvsvc.context1.versMinor = 0;
        Srvsvc.context1.transferOptions = new Dcerpc.TransferSyntax[]{new Dcerpc.Ndr32bit()};
        context2 = new Dcerpc.Context();
        Srvsvc.context2.syntaxGuid = new UUID(1261588424, 5744, 467, new byte[]{18, 120}, new byte[]{90, 71, -65, 110, -31, -120});
        Srvsvc.context2.versMajor = 3;
        Srvsvc.context2.versMinor = 0;
        Srvsvc.context2.transferOptions = new Dcerpc.TransferSyntax[]{new Dcerpc.Ndr64bit()};
        theDescriptor = new Dcerpc.RpcDescriptor();
        Srvsvc.theDescriptor.name = "srvsvc";
        Srvsvc.theDescriptor.contexts = new Dcerpc.Context[]{context1, context2};
    }

    private class EnumShares
    implements Iterator {
        private RpcReader reader;
        int count;

        private EnumShares(RpcReader reader, int count) {
            this.reader = reader;
            this.count = count;
        }

        public boolean hasNext() {
            return this.count > 0;
        }

        public Object next() {
            Share.Info info = new Share.Info();
            return info;
        }

        public void remove() {
        }
    }
}

