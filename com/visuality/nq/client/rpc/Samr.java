/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.rpc;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.client.SidCache;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.client.rpc.RpcReader;
import com.visuality.nq.client.rpc.RpcWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.Sid;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.UUID;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public class Samr
extends Dcerpc {
    private static final int OP_CLOSEHANDLE = 1;
    private static final int OP_LOOKUPDOMAIN = 5;
    private static final int OP_ENUMDOMAINSINSAMSERVER = 6;
    private static final int OP_OPENDOMAIN = 7;
    private static final int OP_ENUMALIASESINDOMAIN = 15;
    private static final int OP_LOOKUPNAMES = 17;
    private static final int OP_LOOKUPIDS = 18;
    private static final int OP_OPENALIAS = 27;
    private static final int OP_GETMEMBERSINALIAS = 33;
    private static final int OP_CONNECT2 = 57;
    private static final int OP_CONNECT4 = 62;
    private static final int OP_CONNECT5 = 64;
    private Vector handles = new Vector();
    private static final Dcerpc.Context context1 = new Dcerpc.Context();
    private static final Dcerpc.Context context2;
    private static final Dcerpc.RpcDescriptor theDescriptor;
    private static final int DESIRED_ACCESS = 49;

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

    public Samr(String server) throws NqException {
        super(server, theDescriptor);
    }

    public Samr(String server, Credentials creds) throws NqException {
        super(server, theDescriptor, creds);
    }

    public Dcerpc.Handle openPolicy() throws NqException {
        Dcerpc.Handle handle = null;
        try {
            handle = this.connect5(49);
        }
        catch (Exception e) {
            try {
                handle = this.connect4(49);
            }
            catch (Exception e1) {
                handle = this.connect2(49);
            }
        }
        return handle;
    }

    private Dcerpc.Handle connect2(int desiredAccess) throws NqException {
        RpcWriter writer = this.startCall(57);
        Dcerpc.ObjectHandle objectHandle = this.startObject();
        objectHandle.writeStringRef(writer, this.serverName);
        objectHandle.placeStrings(writer, true);
        writer.align(0, 4);
        writer.writeInt4(desiredAccess);
        RpcReader reader = this.performRpc(writer);
        Dcerpc.Handle handle = this.readHandle(reader);
        this.checkResult(reader);
        this.handles.add(handle);
        return handle;
    }

    private Dcerpc.Handle connect4(int desiredAccess) throws NqException {
        RpcWriter writer = this.startCall(62);
        Dcerpc.ObjectHandle objectHandle = this.startObject();
        objectHandle.writeStringRef(writer, this.serverName);
        objectHandle.placeStrings(writer, true);
        writer.align(0, 4);
        writer.writeInt4(0);
        writer.writeInt4(desiredAccess);
        RpcReader reader = this.performRpc(writer);
        Dcerpc.Handle handle = this.readHandle(reader);
        this.checkResult(reader);
        this.handles.add(handle);
        return handle;
    }

    private Dcerpc.Handle connect5(int desiredAccess) throws NqException {
        RpcWriter writer = this.startCall(64);
        Dcerpc.ObjectHandle objectHandle = this.startObject();
        objectHandle.writeStringRef(writer, this.serverName);
        objectHandle.placeStrings(writer, true);
        writer.align(0, 4);
        writer.writeInt4(desiredAccess);
        writer.writeInt4(1);
        writer.writeInt4(1);
        writer.writeInt4(3);
        writer.writeInt4(0);
        RpcReader reader = this.performRpc(writer);
        reader.skip(4);
        reader.skip(4);
        reader.skip(4);
        reader.skip(4);
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
        Samr samr = this;
        synchronized (samr) {
            try {
                if (!this.connected) {
                    TraceLog.get().exit("The transport is already been closed", 200);
                    return;
                }
                if (null == handle || !this.handles.contains(handle)) {
                    TraceLog.get().exit("The handle is null or not in the handles list", 200);
                    return;
                }
                RpcWriter writer = this.startCall(1);
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

    public Dcerpc.Handle openDomain(Dcerpc.Handle serverHandle, String domainName) throws NqException {
        Sid domainSid = this.lookupDomain(serverHandle, domainName);
        return this.openDomain(serverHandle, domainSid);
    }

    private Sid lookupDomain(Dcerpc.Handle serverHandle, String domainName) throws NqException {
        Sid sid = SidCache.findSidByDomainName(this.serverName, domainName);
        if (null != sid) {
            return sid;
        }
        RpcWriter writer = this.startCall(5);
        this.writeHandle(writer, serverHandle);
        writer.align();
        Dcerpc.ObjectHandle objectHandle = this.startObject();
        writer.writeInt2(domainName.length() * 2);
        writer.writeInt2(domainName.length() * 2 + 2);
        objectHandle.writeStringRef(writer, domainName);
        objectHandle.placeStrings(writer, false);
        RpcReader reader = this.performRpc(writer);
        Sid domainSid = null;
        if (0L != reader.readCardinal()) {
            long dummy = reader.readCardinal();
            domainSid = new Sid();
            domainSid.read(reader);
        }
        this.checkResult(reader);
        SidCache.addSidByDomainName(this.serverName, domainName, domainSid);
        return domainSid;
    }

    private Dcerpc.Handle openDomain(Dcerpc.Handle serverHandle, Sid domainSid) throws NqException {
        RpcWriter writer = this.startCall(7);
        this.writeHandle(writer, serverHandle);
        writer.writeInt4(901);
        writer.writeCardinal(domainSid.subs.length);
        domainSid.write(writer);
        RpcReader reader = this.performRpc(writer);
        Dcerpc.Handle handle = this.readHandle(reader);
        this.checkResult(reader);
        this.handles.add(handle);
        return handle;
    }

    public Iterator getMemberSidsInAlias(Dcerpc.Handle serverHandle, String alias) throws NqException {
        Dcerpc.Handle localDomainHandle = this.openDomain(serverHandle, Sid.localGroup);
        int aliasId = this.lookupName(localDomainHandle, alias);
        Dcerpc.Handle aliasHandle = this.openAlias(localDomainHandle, aliasId);
        RpcWriter writer = this.startCall(33);
        this.writeHandle(writer, aliasHandle);
        RpcReader reader = this.performRpc(writer);
        Vector<Sid> members = new Vector<Sid>();
        long numSids = reader.readCardinal();
        if (0L != reader.readCardinal()) {
            long dummy = reader.readCardinal();
            int i = 0;
            while ((long)i < numSids) {
                dummy = reader.readCardinal();
                ++i;
            }
            while (numSids-- > 0L) {
                dummy = reader.readCardinal();
                Sid sid = new Sid();
                sid.read(reader);
                members.add(sid);
            }
        }
        this.checkResult(reader);
        this.close(localDomainHandle);
        this.close(aliasHandle);
        return members.iterator();
    }

    public Iterator getMemberNamesInAlias(Dcerpc.Handle serverHandle, Dcerpc.Handle domainHandle, String alias) throws NqException {
        Iterator sids = this.getMemberSidsInAlias(serverHandle, alias);
        LinkedList<String> members = new LinkedList<String>();
        while (sids.hasNext()) {
            Sid sid = (Sid)sids.next();
            if (!sid.hasRid()) continue;
            int rid = sid.getRid();
            try {
                String memberName = this.lookupRid(domainHandle, rid);
                if (null == memberName) continue;
                members.add(memberName);
            }
            catch (NqException e) {}
        }
        return members.iterator();
    }

    public Iterator getMemberNamesInAlias(Dcerpc.Handle serverHandle, String alias) throws NqException {
        Dcerpc.Handle local = this.openDomain(serverHandle, Sid.localGroup);
        Iterator it = this.getMemberNamesInAlias(serverHandle, local, alias);
        this.close(local);
        return it;
    }

    private String lookupRid(Dcerpc.Handle domainHandle, int rid) throws NqException {
        RpcWriter writer = this.startCall(18);
        this.writeHandle(writer, domainHandle);
        writer.writeInt4(1);
        writer.align();
        writer.writeInt4(1000);
        writer.align();
        writer.writeInt4(0);
        writer.align();
        writer.writeInt4(1);
        writer.align();
        writer.writeInt4(rid);
        RpcReader reader = this.performRpc(writer);
        long numNames = reader.readCardinal();
        if (numNames != 1L) {
            throw new NqException("Unxpected number of results in LookupRids");
        }
        long dummy = reader.readCardinal();
        dummy = reader.readCardinal();
        reader.skip(4);
        reader.align();
        String name = null;
        if (0L != reader.readCardinal()) {
            name = reader.readReferencedString(false);
        }
        reader.align();
        dummy = reader.readCardinal();
        dummy = reader.readCardinal();
        dummy = reader.readCardinal();
        reader.skip(4);
        this.checkResult(reader);
        return name;
    }

    public int lookupName(Dcerpc.Handle domainHandle, String alias) throws NqException {
        RpcWriter writer = this.startCall(17);
        this.writeHandle(writer, domainHandle);
        writer.writeInt4(1);
        writer.align();
        writer.writeInt4(1000);
        writer.align();
        writer.writeInt4(0);
        writer.align();
        writer.writeInt4(1);
        writer.align();
        writer.writeInt2(alias.length() * 2);
        writer.writeInt2(alias.length() * 2 + 2);
        Dcerpc.ObjectHandle objectHandle = this.startObject();
        objectHandle.writeStringRef(writer, alias);
        objectHandle.placeStrings(writer, false);
        RpcReader reader = this.performRpc(writer);
        long numRids = reader.readCardinal();
        if (numRids != 1L) {
            throw new NqException("Unexpected LookupNames response");
        }
        long dummy = reader.readCardinal();
        dummy = reader.readCardinal();
        int rid = reader.readInt4();
        reader.skip(16);
        this.checkResult(reader);
        return rid;
    }

    private Dcerpc.Handle openAlias(Dcerpc.Handle domainHandle, int aliasId) throws NqException {
        RpcWriter writer = this.startCall(27);
        this.writeHandle(writer, domainHandle);
        writer.writeInt4(131084);
        writer.writeInt4(aliasId);
        RpcReader reader = this.performRpc(writer);
        Dcerpc.Handle handle = this.readHandle(reader);
        this.checkResult(reader);
        this.handles.add(handle);
        return handle;
    }

    public Iterator enumerateAliasesInDomain(Dcerpc.Handle serverHandle, Dcerpc.Handle domainHandle) throws NqException {
        LinkedList<Alias> aliases = new LinkedList<Alias>();
        RpcWriter writer = this.startCall(15);
        this.writeHandle(writer, domainHandle);
        writer.writeInt4(0);
        writer.writeInt4(-1);
        RpcReader reader = this.performRpc(writer);
        reader.skip(4);
        reader.align();
        if (0L != reader.readCardinal()) {
            long numEntries = reader.readCardinal();
            long dummy = reader.readCardinal();
            dummy = reader.readCardinal();
            while (numEntries-- > 0L) {
                Alias alias = new Alias();
                alias.id = reader.readCardinal();
                reader.skip(4);
                reader.align();
                dummy = reader.readCardinal();
                aliases.add(alias);
            }
            for (Alias alias : aliases) {
                alias.name = reader.readReferencedString(false);
            }
        }
        reader.align(0, 4);
        reader.skip(4);
        this.checkResult(reader);
        return aliases.iterator();
    }

    public Iterator enumerateAliasesInDomain(Dcerpc.Handle serverHandle) throws NqException {
        Dcerpc.Handle local = this.openDomain(serverHandle, Sid.localGroup);
        Iterator it = this.enumerateAliasesInDomain(serverHandle, local);
        this.close(local);
        return it;
    }

    public Iterator enumerateDomainaInServer(Dcerpc.Handle serverHandle) throws NqException {
        RpcWriter writer = this.startCall(6);
        this.writeHandle(writer, serverHandle);
        writer.writeInt4(0);
        writer.writeInt4(-1);
        RpcReader reader = this.performRpc(writer);
        reader.skip(4);
        Vector<String> domains = new Vector<String>();
        if (0L != reader.readCardinal()) {
            long numEntries = reader.readCardinal();
            long dummy = reader.readCardinal();
            dummy = reader.readCardinal();
            int i = 0;
            while ((long)i < numEntries) {
                dummy = reader.readCardinal();
                reader.skip(4);
                dummy = reader.readCardinal();
                ++i;
            }
            while (numEntries-- > 0L) {
                domains.add(reader.readReferencedString(false));
            }
        }
        reader.align(0, 4);
        reader.skip(4);
        this.checkResult(reader);
        return domains.iterator();
    }

    static {
        Samr.context1.syntaxGuid = new UUID(305420152, 4660, 43981, new byte[]{-17, 0}, new byte[]{1, 35, 69, 103, -119, -84});
        Samr.context1.versMajor = 1;
        Samr.context1.versMinor = 0;
        Samr.context1.transferOptions = new Dcerpc.TransferSyntax[]{new Dcerpc.Ndr32bit()};
        context2 = new Dcerpc.Context();
        Samr.context2.syntaxGuid = new UUID(305420152, 4660, 43981, new byte[]{-17, 0}, new byte[]{1, 35, 69, 103, -119, -84});
        Samr.context2.versMajor = 1;
        Samr.context2.versMinor = 0;
        Samr.context2.transferOptions = new Dcerpc.TransferSyntax[]{new Dcerpc.Ndr64bit()};
        theDescriptor = new Dcerpc.RpcDescriptor();
        Samr.theDescriptor.name = "lsarpc";
        Samr.theDescriptor.contexts = new Dcerpc.Context[]{context1, context2};
    }

    public class Alias {
        public String name;
        public long id;
    }
}

