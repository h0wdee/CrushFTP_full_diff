/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.rpc;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.client.SidCache;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.client.rpc.Domain;
import com.visuality.nq.client.rpc.RpcReader;
import com.visuality.nq.client.rpc.RpcWriter;
import com.visuality.nq.common.HexBuilder;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Sid;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.UUID;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public class Lsar
extends Dcerpc {
    private static final int OP_CLOSE = 0;
    private static final int OP_QUERYINFORMATIONPOLICY = 7;
    private static final int OP_OPENPOLICY2 = 44;
    private static final int OP_ENUMERATETRUSTEDDOMAINSEX = 50;
    private static final int OP_LOOKUPSIDS2 = 57;
    private static final int OP_LOOKUPNAMES3 = 68;
    private static final int SID_NAME_USE_NONE = 0;
    private static final int SID_NAME_USER = 1;
    private static final int SID_NAME_DOM_GRP = 2;
    private static final int SID_NAME_DOMAIN = 3;
    private static final int SID_NAME_ALIAS = 4;
    private static final int SID_NAME_WKN_GRP = 5;
    private static final int SID_NAME_DELETED = 6;
    private static final int SID_NAME_INVALID = 7;
    private static final int SID_NAME_UNKNOWN = 8;
    private Vector handles = new Vector();
    private static final Dcerpc.Context context1 = new Dcerpc.Context();
    private static final Dcerpc.Context context2;
    private static final Dcerpc.RpcDescriptor theDescriptor;
    public static final int LSA_POLICY_VIEW_LOCAL_INFORMATION = 1;
    public static final int LSA_POLICY_VIEW_AUDIT_INFORMATION = 2;
    public static final int LSA_POLICY_GET_PRIVATE_INFORMATION = 4;
    public static final int LSA_POLICY_TRUST_ADMIN = 8;
    public static final int LSA_POLICY_CREATE_ACCOUNT = 16;
    public static final int LSA_POLICY_CREATE_SECRET = 32;
    public static final int LSA_POLICY_CREATE_PRIVILEGE = 64;
    public static final int LSA_POLICY_SET_DEFAULT_QUOTA_LIMITS = 128;
    public static final int LSA_POLICY_AUDIT_REQUIREMENTS = 256;
    public static final int LSA_POLICY_AUDIT_LOG_ADMIN = 512;
    public static final int LSA_POLICY_SERVER_ADMIN = 1024;
    public static final int ACCESS_LOOKUP_NAMES = 2048;
    public static final int LSA_POLICY_NOTIFICATION = 4096;

    protected void finalize() throws Throwable {
        Enumeration en = this.handles.elements();
        while (en.hasMoreElements()) {
            try {
                this.close((Dcerpc.Handle)en.nextElement());
            }
            catch (NqException e) {
                TraceLog.get().error("NqException = ", e, 10, e.getErrCode());
            }
            catch (Exception e) {
                TraceLog.get().error("Exception = ", e, 10, 0);
            }
        }
        this.handles.clear();
        this.close();
    }

    public Lsar(String server) throws NqException {
        super(server, theDescriptor);
    }

    public Lsar(String server, Credentials creds) throws NqException {
        super(server, theDescriptor, creds);
    }

    public Dcerpc.Handle openPolicy(int desiredAccess) throws NqException {
        RpcWriter writer = this.startCall(44);
        Dcerpc.ObjectHandle objectHandle = this.startObject();
        objectHandle.writeStringRef(writer, this.serverName);
        objectHandle.placeStrings(writer, true);
        writer.align(0, 4);
        int lengthOff = writer.getOffset();
        writer.writeCardinal(0L);
        int objectAttribStart = writer.getOffset();
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        writer.writeCardinal(lengthOff, writer.getOffset() - objectAttribStart);
        writer.align();
        writer.writeInt4(desiredAccess);
        writer.align();
        RpcReader reader = this.performRpc(writer);
        Dcerpc.Handle handle = this.readHandle(reader);
        this.checkResult(reader);
        this.handles.add(handle);
        return handle;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void close(Dcerpc.Handle handle) throws NqException {
        TraceLog.get().enter("hashCode = " + this.hashCode(), 200);
        Lsar lsar = this;
        synchronized (lsar) {
            try {
                if (!this.connected) {
                    TraceLog.get().exit("The transport is already been closed", 200);
                    return;
                }
                if (null == handle || !this.handles.contains(handle)) {
                    TraceLog.get().exit("The handle is null or not in the handles list", 200);
                    return;
                }
                RpcWriter writer = this.startCall(0);
                this.writeHandle(writer, handle);
                RpcReader reader = this.performRpc(writer);
                reader.skip(20);
                this.checkResult(reader);
            }
            catch (NqException e) {
                TraceLog.get().caught(e);
                TraceLog.get().exit(200);
                throw e;
            }
            finally {
                if (null != handle) {
                    this.handles.remove(handle);
                }
            }
        }
        TraceLog.get().exit(200);
    }

    public Sid lookupName(Dcerpc.Handle handle, String name) throws NqException {
        Sid sid = SidCache.findSidByName(this.serverName, name);
        if (null != sid) {
            return sid;
        }
        Sid result = null;
        byte[] readerSrc = null;
        RpcReader reader = null;
        try {
            long dummy;
            RpcWriter writer = this.startCall(68);
            this.writeHandle(writer, handle);
            writer.writeInt4(1);
            Dcerpc.ObjectHandle namesHandle = this.startObject();
            writer.writeCardinal(1L);
            writer.writeInt2(name.length() * 2);
            writer.writeInt2(name.length() * 2 + 2);
            namesHandle.writeStringRef(writer, name);
            namesHandle.placeStrings(writer, false);
            writer.align(0, 4);
            writer.writeCardinal(0L);
            writer.writeCardinal(0L);
            writer.writeInt2(1);
            writer.align(0, 4);
            writer.writeInt4(0);
            writer.writeInt4(0);
            writer.writeInt4(2);
            reader = this.performCall(writer);
            readerSrc = (byte[])reader.getSrc().clone();
            boolean hasSid = false;
            long domainListRefId = reader.readCardinal();
            if (0L != domainListRefId) {
                long numDomains = reader.readCardinal();
                Sid domainSid = new Sid();
                hasSid = true;
                if (numDomains > 0L) {
                    if (0L < reader.readCardinal()) {
                        dummy = reader.readCardinal();
                        dummy = reader.readCardinal();
                        do {
                            reader.readInt2();
                            reader.skip(2);
                            dummy = reader.readCardinal();
                            dummy = reader.readCardinal();
                            reader.readReferencedString(true);
                            reader.align(0, 4);
                            dummy = reader.readCardinal();
                            domainSid.read(reader);
                        } while (--numDomains > 0L);
                    }
                } else {
                    domainListRefId = reader.readCardinal();
                    dummy = reader.readCardinal();
                    if (0L != domainListRefId) {
                        dummy = reader.readCardinal();
                    }
                }
            } else {
                dummy = reader.readCardinal();
                dummy = reader.readCardinal();
            }
            result = new Sid();
            if (hasSid) {
                long numSids = reader.readCardinal();
                dummy = reader.readCardinal();
                dummy = reader.readCardinal();
                dummy = reader.readCardinal();
                do {
                    long sidRef;
                    if (0L != (sidRef = reader.readCardinal())) {
                        dummy = reader.readInt4();
                        dummy = reader.readInt4();
                        dummy = reader.readCardinal();
                        result.read(reader);
                        continue;
                    }
                    dummy = reader.readInt4();
                    dummy = reader.readInt4();
                } while (--numSids > 0L);
            }
            dummy = reader.readInt4();
        }
        catch (NqException e) {
            if (null == reader) {
                TraceLog.get().error("No data received from source");
                throw new NqException("No data returned from lookupName: " + e.getMessage() + ", error code = " + e.getErrCode(), 10);
            }
            StringBuilder sb = HexBuilder.toHex(readerSrc);
            TraceLog.get().error("Lsar Response Dump = ", sb);
        }
        this.checkResult(reader);
        SidCache.addSidByName(this.serverName, name, result);
        return result;
    }

    public String lookupSid(Dcerpc.Handle handle, Sid sid) throws NqException {
        return this.lookupSid(handle, sid, false);
    }

    public String lookupSid(Dcerpc.Handle handle, Sid sid, boolean withDomainName) throws NqException {
        String domainName = "";
        RpcWriter writer = this.startCall(57);
        this.writeHandle(writer, handle);
        writer.writeCardinal(1L);
        writer.writeCardinal(this.nextRefId());
        writer.writeCardinal(1L);
        writer.writeCardinal(this.nextRefId());
        writer.writeCardinal(sid.subs.length);
        sid.write(writer);
        writer.align(0, 4);
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        writer.writeInt2(1);
        writer.align(0, 4);
        writer.writeInt4(0);
        writer.writeInt4(0);
        writer.writeInt4(2);
        String result = null;
        RpcReader reader = this.performRpc(writer);
        long dummy = 0L;
        long domainListRefId = reader.readCardinal();
        if (0L != domainListRefId) {
            long numDomains = reader.readCardinal();
            Sid domainSid = new Sid();
            if (numDomains > 0L) {
                dummy = reader.readCardinal();
                dummy = reader.readCardinal();
                dummy = reader.readCardinal();
                do {
                    reader.skip(2);
                    reader.skip(2);
                    long stringRedIf = reader.readCardinal();
                    dummy = reader.readCardinal();
                    if (0L != stringRedIf) {
                        domainName = reader.readReferencedString(false);
                    }
                    reader.align(0, 4);
                    dummy = reader.readCardinal();
                    domainSid.read(reader);
                } while (--numDomains > 0L);
            } else {
                domainListRefId = reader.readCardinal();
                dummy = reader.readCardinal();
                if (0L != domainListRefId) {
                    dummy = reader.readCardinal();
                }
            }
        }
        long numNames = reader.readCardinal();
        long nameRefId = reader.readCardinal();
        long sidType = 0L;
        if (0L < nameRefId) {
            dummy = reader.readCardinal();
            sidType = reader.readCardinal();
            long nameReferenceId = 0L;
            int i = 0;
            while ((long)i < numNames) {
                reader.skip(2);
                reader.skip(2);
                nameReferenceId = reader.readCardinal();
                reader.skip(4);
                reader.skip(4);
                ++i;
            }
            if (1L < numNames || 0L != nameReferenceId) {
                i = 0;
                while ((long)i < numNames) {
                    result = reader.readReferencedString(false);
                    ++i;
                }
            }
            reader.align(0, 4);
        }
        reader.skip(4);
        this.checkResult(reader);
        if (3L == sidType) {
            return domainName;
        }
        if (5L == sidType) {
            return result;
        }
        if (domainName.equals("BUILTIN")) {
            if (8L == sidType) {
                return sid.toString();
            }
            return result;
        }
        if (withDomainName && null != domainName && domainName.length() > 0) {
            result = domainName + "\\" + result;
        }
        return result;
    }

    public Iterator enumTrustedDomains(Dcerpc.Handle handle) throws NqException {
        RpcWriter writer = this.startCall(50);
        this.writeHandle(writer, handle);
        writer.writeInt4(0);
        writer.writeInt4(65535);
        RpcReader reader = this.performRpc(writer);
        LinkedList<Domain> domains = new LinkedList<Domain>();
        LinkedList<Long> sidPtrs = new LinkedList<Long>();
        reader.skip(4);
        long numDomains = reader.readCardinal();
        long dummy = reader.readCardinal();
        if (numDomains > 0L) {
            dummy = reader.readCardinal();
            int i = 0;
            while ((long)i < numDomains) {
                reader.skip(2);
                reader.skip(2);
                reader.align();
                dummy = reader.readCardinal();
                reader.skip(2);
                reader.skip(2);
                reader.align();
                dummy = reader.readCardinal();
                long sidRefId = reader.readCardinal();
                sidPtrs.add(sidRefId);
                reader.skip(4);
                reader.skip(4);
                reader.skip(4);
                reader.align();
                ++i;
            }
            i = 0;
            while ((long)i < numDomains) {
                Domain info = new Domain();
                info.name = reader.readReferencedString(false);
                info.nameDns = reader.readReferencedString(false);
                reader.align();
                long sidRefId = (Long)sidPtrs.remove();
                if (0L == sidRefId) {
                    info.sid = null;
                } else {
                    info.sid = new Sid();
                    dummy = reader.readCardinal();
                    info.sid.read(reader);
                }
                domains.add(info);
                ++i;
            }
        }
        this.checkResult(reader);
        return domains.iterator();
    }

    public Domain queryInformationDomain(Dcerpc.Handle handle) throws NqException {
        RpcWriter writer = this.startCall(7);
        this.writeHandle(writer, handle);
        writer.writeInt2(5);
        writer.align();
        RpcReader reader = this.performRpc(writer);
        long refInfoId = reader.readCardinal();
        if (0L == refInfoId) {
            this.checkResult(reader);
        }
        reader.skip(2);
        reader.align();
        reader.skip(2);
        reader.skip(2);
        long refNameId = reader.readCardinal();
        long refSidId = reader.readCardinal();
        Domain info = new Domain();
        info.name = reader.readReferencedString(false);
        reader.align(0, 4);
        long countSid = reader.readCardinal();
        info.sid = new Sid();
        info.sid.read(reader);
        this.checkResult(reader);
        return info;
    }

    static {
        Lsar.context1.syntaxGuid = new UUID(305420152, 4660, 43981, new byte[]{-17, 0}, new byte[]{1, 35, 69, 103, -119, -85});
        Lsar.context1.versMajor = 0;
        Lsar.context1.versMinor = 0;
        Lsar.context1.transferOptions = new Dcerpc.TransferSyntax[]{new Dcerpc.Ndr32bit()};
        context2 = new Dcerpc.Context();
        Lsar.context2.syntaxGuid = new UUID(305420152, 4660, 43981, new byte[]{-17, 0}, new byte[]{1, 35, 69, 103, -119, -85});
        Lsar.context2.versMajor = 0;
        Lsar.context2.versMinor = 0;
        Lsar.context2.transferOptions = new Dcerpc.TransferSyntax[]{new Dcerpc.Ndr64bit()};
        theDescriptor = new Dcerpc.RpcDescriptor();
        Lsar.theDescriptor.name = "lsarpc";
        Lsar.theDescriptor.contexts = new Dcerpc.Context[]{context1, context2};
    }
}

