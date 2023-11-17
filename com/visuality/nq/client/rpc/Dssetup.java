/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.rpc;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.client.rpc.Domain;
import com.visuality.nq.client.rpc.RpcReader;
import com.visuality.nq.client.rpc.RpcWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.UUID;

public class Dssetup
extends Dcerpc {
    private static final Dcerpc.Context context1 = new Dcerpc.Context();
    private static final Dcerpc.Context context2;
    private static final Dcerpc.RpcDescriptor theDescriptor;

    public Dssetup(String server) throws NqException {
        super(server, theDescriptor);
    }

    public Dssetup(String server, Credentials creds) throws NqException {
        super(server, theDescriptor, creds);
    }

    public Domain roleGetPrimaryDomainInformation() throws NqException {
        RpcWriter writer = this.startCall(0);
        writer.writeInt2(1);
        writer.align();
        RpcReader reader = this.performRpc(writer);
        Domain result = new Domain();
        long dummy = reader.readCardinal();
        dummy = reader.readCardinal();
        result.role = reader.readInt2();
        reader.align(0, 4);
        result.flags = reader.readInt4();
        long netbiosId = reader.readCardinal();
        long dnsNameId = reader.readCardinal();
        long forestId = reader.readCardinal();
        result.guid = new UUID();
        result.guid.read(reader);
        result.name = 0L != netbiosId ? reader.readReferencedString(true) : "";
        result.nameDns = 0L != dnsNameId ? reader.readReferencedString(true) : "";
        result.forest = 0L != forestId ? reader.readReferencedString(true) : "";
        this.checkResult(reader);
        return result;
    }

    static {
        Dssetup.context1.syntaxGuid = new UUID(957950058, 45324, 4560, new byte[]{-101, -88}, new byte[]{0, -64, 79, -39, 46, -11});
        Dssetup.context1.versMajor = 0;
        Dssetup.context1.versMinor = 0;
        Dssetup.context1.transferOptions = new Dcerpc.TransferSyntax[]{new Dcerpc.Ndr32bit()};
        context2 = new Dcerpc.Context();
        Dssetup.context2.syntaxGuid = new UUID(957950058, 45324, 4560, new byte[]{-101, -88}, new byte[]{0, -64, 79, -39, 46, -11});
        Dssetup.context2.versMajor = 0;
        Dssetup.context2.versMinor = 0;
        Dssetup.context2.transferOptions = new Dcerpc.TransferSyntax[]{new Dcerpc.Ndr64bit()};
        theDescriptor = new Dcerpc.RpcDescriptor();
        Dssetup.theDescriptor.name = "lsarpc";
        Dssetup.theDescriptor.contexts = new Dcerpc.Context[]{context1};
    }
}

