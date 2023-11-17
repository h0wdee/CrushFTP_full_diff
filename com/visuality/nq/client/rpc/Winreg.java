/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.rpc;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.client.rpc.Dcerpc;
import com.visuality.nq.client.rpc.RpcReader;
import com.visuality.nq.client.rpc.RpcWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.UUID;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Vector;

public class Winreg
extends Dcerpc {
    public static final int ACCESS_CREATELINK = 32;
    public static final int ACCESS_NOTIFY = 16;
    public static final int ACCESS_ENUMERATE = 8;
    public static final int ACCESS_CREATESUBKEY = 4;
    public static final int ACCESS_SETVALUE = 2;
    public static final int ACCESS_QUERYVALUE = 1;
    public static final int ACCESS_READ = 25;
    public static final int ACCESS_WRITE = 27;
    public static final int ACCESS_FULL = 63;
    private static final int OP_OPENHKLM = 2;
    private static final int OP_CLOSEKEY = 5;
    private static final int OP_ENUMKEY = 9;
    private static final int OP_ENUmMVALUE = 10;
    private static final int OP_OPENKEY = 15;
    private static final int OP_QUERYVALUE = 17;
    private static final int OP_GETVERSION = 26;
    private Vector handles = new Vector();
    private static final Dcerpc.Context context1 = new Dcerpc.Context();
    private static final Dcerpc.Context context2;
    private static final Dcerpc.RpcDescriptor theDescriptor;

    protected void finalize() throws Throwable {
        Enumeration en = this.handles.elements();
        while (en.hasMoreElements()) {
            try {
                this.closeKey((Dcerpc.Handle)en.nextElement());
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

    public Winreg(String server) throws NqException {
        super(server, theDescriptor);
    }

    public Winreg(String server, Credentials creds) throws NqException {
        super(server, theDescriptor, creds);
    }

    public Dcerpc.Handle openLocalMachine() throws NqException {
        RpcWriter writer = this.startCall(2);
        writer.writeCardinal(this.nextRefId());
        writer.writeInt2(0);
        writer.align(0, 4);
        writer.writeInt4(0x2000000);
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
    public void closeKey(Dcerpc.Handle handle) throws NqException {
        TraceLog.get().enter("hashCode = " + this.hashCode(), 200);
        Winreg winreg = this;
        synchronized (winreg) {
            try {
                if (!this.connected) {
                    TraceLog.get().exit("The transport is already been closed", 200);
                    return;
                }
                if (null == handle || !this.handles.contains(handle)) {
                    TraceLog.get().exit("The handle is null or not in the handles list", 200);
                    return;
                }
                RpcWriter writer = this.startCall(5);
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

    public int getVersion(Dcerpc.Handle handle) throws NqException {
        RpcWriter writer = this.startCall(26);
        this.writeHandle(writer, handle);
        RpcReader reader = this.performRpc(writer);
        int version = reader.readInt4();
        this.checkResult(reader);
        return version;
    }

    public Dcerpc.Handle openKey(Dcerpc.Handle handle, String key, int access) throws NqException {
        RpcWriter writer = this.startCall(15);
        this.writeHandle(writer, handle);
        int length = key.length() + 1;
        writer.align();
        writer.writeInt2(length * 2);
        writer.writeInt2(length * 2);
        writer.align();
        writer.writeInt4(this.nextRefId());
        writer.writeRpcString(key, true);
        writer.align(0, 4);
        writer.writeInt4(0);
        writer.writeInt4(access);
        RpcReader reader = this.performRpc(writer);
        Dcerpc.Handle keyHandle = this.readHandle(reader);
        this.checkResult(reader);
        this.handles.add(keyHandle);
        return keyHandle;
    }

    public Object queryValue(Dcerpc.Handle handle, String valName) throws NqException {
        RpcWriter writer = this.startCall(17);
        this.writeHandle(writer, handle);
        writer.align();
        int length = valName.length() + 1;
        writer.writeInt2(length * 2);
        writer.writeInt2(length * 2);
        Dcerpc.ObjectHandle valueNameHandle = this.startObject();
        valueNameHandle.writeStringRef(writer, valName);
        valueNameHandle.placeStrings(writer, true);
        writer.align(0, 4);
        writer.writeCardinal(this.nextRefId());
        writer.writeInt4(0);
        int dataSize = this.getServer().maxTrans - 1000;
        writer.writeCardinal(this.nextRefId());
        writer.writeCardinal(dataSize);
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        writer.writeCardinal(this.nextRefId());
        writer.writeCardinal(dataSize);
        writer.writeCardinal(this.nextRefId());
        writer.writeCardinal(0L);
        RpcReader reader = this.performRpc(writer);
        reader.readCardinal();
        return this.parseValue(reader, valName);
    }

    public String enumKey(Dcerpc.Handle handle, int index) throws NqException {
        RpcWriter writer = this.startCall(9);
        this.writeHandle(writer, handle);
        writer.align(0, 4);
        writer.writeInt4(index);
        writer.writeInt2(0);
        writer.writeInt2(2048);
        writer.writeCardinal(this.nextRefId());
        writer.writeCardinal(1024L);
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        writer.writeCardinal(this.nextRefId());
        writer.writeInt2(0);
        writer.writeInt2(2048);
        writer.writeCardinal(0L);
        writer.writeCardinal(1024L);
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        RpcReader reader = this.performRpc(writer);
        short len = reader.readInt2();
        reader.skip(2);
        reader.readCardinal();
        String keyName = null;
        if (len > 0) {
            keyName = reader.readReferencedString(true);
        } else {
            reader.readCardinal();
            reader.readCardinal();
            reader.readCardinal();
        }
        reader.align();
        reader.readCardinal();
        reader.readInt2();
        reader.readInt2();
        reader.align();
        reader.readCardinal();
        reader.readCardinal();
        reader.readInt8();
        this.checkResult(reader, 259);
        return keyName;
    }

    public String enumValue(Dcerpc.Handle handle, int index) throws NqException {
        RpcWriter writer = this.startCall(10);
        this.writeHandle(writer, handle);
        writer.align(0, 4);
        writer.writeInt4(index);
        writer.writeInt2(0);
        writer.writeInt2(2048);
        writer.writeCardinal(this.nextRefId());
        writer.writeCardinal(1024L);
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        writer.writeCardinal(this.nextRefId());
        writer.writeInt2(0);
        writer.writeInt2(2048);
        writer.writeCardinal(0L);
        writer.writeCardinal(this.nextRefId());
        writer.writeCardinal(0L);
        writer.writeCardinal(0L);
        RpcReader reader = this.performRpc(writer);
        short len = reader.readInt2();
        reader.skip(2);
        reader.align();
        reader.readCardinal();
        String valName = null;
        if (0 == len) {
            reader.readCardinal();
            reader.readCardinal();
            reader.readCardinal();
        } else {
            valName = reader.readReferencedString(true);
        }
        reader.readCardinal();
        reader.readInt4();
        reader.align();
        reader.readCardinal();
        reader.readInt4();
        reader.align();
        reader.readCardinal();
        this.checkResult(reader, 259);
        return valName;
    }

    private Object parseValue(RpcReader reader, String valName) throws NqException {
        int type = (int)reader.readCardinal();
        if (0 == type) {
            throw new NqException("Value '" + valName + "' not available", -23);
        }
        long data = reader.readCardinal();
        if (0L == data) {
            reader.readCardinal();
            reader.readCardinal();
            reader.readCardinal();
            reader.readCardinal();
            return null;
        }
        int dataLen = (int)reader.readCardinal();
        reader.readCardinal();
        reader.readCardinal();
        byte[] value = new byte[dataLen];
        reader.readBytes(value, dataLen);
        reader.align();
        reader.readCardinal();
        reader.readInt4();
        reader.readCardinal();
        reader.readInt4();
        this.checkResult(reader);
        try {
            switch (type) {
                case 1: 
                case 2: {
                    return new String(value, 0, value.length - 2, "UTF-16LE");
                }
                case 3: {
                    return value;
                }
                case 4: {
                    return value[0] + value[1] >> 8 + value[2] >> 16 + value[3] >> 24;
                }
                case 5: {
                    return value[3] + value[2] >> 8 + value[1] >> 16 + value[0] >> 24;
                }
                case 7: {
                    Vector<String> list = new Vector<String>();
                    int start = 0;
                    int end = 0;
                    for (int i = 0; i < value.length - 1; ++i) {
                        if (value[i] != 0 || value[i + 1] != 0) continue;
                        end = i;
                        if (start == end) break;
                        list.add(new String(value, start, end - start, "UTF-16LE"));
                        start = i + 2;
                        ++i;
                    }
                    return list.toArray();
                }
                case 11: {
                    return (long)value[0] + (long)value[1] >> (int)(8L + (long)value[2]) >> (int)(16L + (long)value[3]) >> (int)(24L + (long)value[4]) >> (int)(32L + (long)value[5]) >> (int)(40L + (long)value[6]) >> (int)(48L + (long)value[7]) >> 56;
                }
            }
            throw new NqException("Value '" + valName + "' has unexpected type - " + type, -23);
        }
        catch (UnsupportedEncodingException e) {
            TraceLog.get().error("UTF-16LE is not supported by the platform", 5, 0);
            return null;
        }
    }

    static {
        Winreg.context1.syntaxGuid = new UUID(864866305, 8772, 12785, new byte[]{-86, -86}, new byte[]{-112, 0, 56, 0, 16, 3});
        Winreg.context1.versMajor = 1;
        Winreg.context1.versMinor = 0;
        Winreg.context1.transferOptions = new Dcerpc.TransferSyntax[]{new Dcerpc.Ndr32bit()};
        context2 = new Dcerpc.Context();
        Winreg.context2.syntaxGuid = new UUID(864866305, 8772, 12785, new byte[]{-86, -86}, new byte[]{-112, 0, 56, 0, 16, 3});
        Winreg.context2.versMajor = 1;
        Winreg.context2.versMinor = 0;
        Winreg.context2.transferOptions = new Dcerpc.TransferSyntax[]{new Dcerpc.Ndr64bit()};
        theDescriptor = new Dcerpc.RpcDescriptor();
        Winreg.theDescriptor.name = "winreg";
        Winreg.theDescriptor.contexts = new Dcerpc.Context[]{context1, context2};
    }
}

