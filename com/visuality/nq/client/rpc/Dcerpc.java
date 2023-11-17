/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client.rpc;

import com.visuality.nq.auth.Credentials;
import com.visuality.nq.auth.PasswordCredentials;
import com.visuality.nq.client.File;
import com.visuality.nq.client.Server;
import com.visuality.nq.client.rpc.RpcReader;
import com.visuality.nq.client.rpc.RpcWriter;
import com.visuality.nq.client.rpc.SmbTransport;
import com.visuality.nq.client.rpc.TcpTransport;
import com.visuality.nq.common.Buffer;
import com.visuality.nq.common.BufferReader;
import com.visuality.nq.common.BufferWriter;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.SmbException;
import com.visuality.nq.common.SmbSerializable;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.common.UUID;
import com.visuality.nq.common.Utility;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public abstract class Dcerpc
extends File {
    private static boolean useSmb = true;
    private static boolean useTcp = false;
    private int startOffset;
    protected Credentials credentials;
    protected Transport transport;
    protected boolean connected = false;
    protected int maxXmit = 16384;
    protected int maxRecv = 16384;
    protected String serverName;
    protected RpcDescriptor rpcDescr;
    protected Server server;
    protected static final int HEADER_LENGTH = 16;
    protected static final int PACKET_BIND = 11;
    protected static final int PACKET_BIND_ACK = 12;
    protected static final int PACKET_CALL_REQ = 0;
    protected static final int PACKET_CALL_RES = 2;
    protected int callId = 2;
    protected static final int TRANSPORT_SMB = 1;
    protected static final int TRANSPORT_TCP = 2;
    protected int contextIdx;
    protected TransferSyntax transferSyntax;
    private static final int PERFORM_CALL_LOOP_COUNTER = 3;
    private static final int PERFORM_CALL_LOOP_TIMER = 100;
    private int refId = 1;
    private static final int XMIT_OFFSET = 100;
    private static final int RECV_OFFSET = 100;
    private static final int MAX_XMIT_FRAG = 16384;
    private static final int MAX_RECV_FRAG = 16384;

    protected static void enableTransport(int transport, boolean enable) throws NqException {
        switch (transport) {
            case 1: {
                useSmb = enable;
                break;
            }
            default: {
                throw new NqException("Illegal transport for RPC", -20);
            }
        }
    }

    public Dcerpc(String server, RpcDescriptor descr) throws NqException {
        TraceLog.get().enter("hashCode = " + this.hashCode(), 200);
        this.serverName = server;
        this.rpcDescr = descr;
        this.connect(new PasswordCredentials());
        TraceLog.get().exit(200);
    }

    public Dcerpc(String server, RpcDescriptor descr, Credentials creds) throws NqException {
        TraceLog.get().enter("hashCode = " + this.hashCode(), 200);
        this.serverName = server;
        this.rpcDescr = descr;
        this.connect(creds);
        TraceLog.get().exit(200);
    }

    protected Dcerpc(Server server, RpcDescriptor descr, Credentials creds) throws NqException {
        this.server = server;
        this.serverName = server.getName();
        this.rpcDescr = descr;
        this.connect(creds);
    }

    private void serialize(RpcWriter writer, char firstLetter, Class c, Object src, ObjectHandle curHandle) throws NqException {
        boolean isCardinal;
        if (curHandle == null && c.isPrimitive()) {
            throw new NqException("Top level PDU is a primitive value");
        }
        boolean isPointer = firstLetter == '_';
        boolean bl = isCardinal = firstLetter == '$';
        if (isPointer) {
            writer.align(this.startOffset, 4);
            if (null != src) {
                writer.writeCardinal(this.nextRefId());
                curHandle.pushObject(src);
            } else {
                writer.writeCardinal(0L);
            }
            return;
        }
        if (isCardinal) {
            writer.writeCardinal((Long)src);
        } else if (c.equals(Long.TYPE)) {
            writer.align(this.startOffset, 8);
            writer.writeInt8((Long)src);
        } else if (c.equals(Integer.TYPE)) {
            writer.align(this.startOffset, 4);
            writer.writeInt4((Integer)src);
        } else if (c.equals(Short.TYPE)) {
            writer.writeInt2(((Short)src).shortValue());
        } else if (c.equals(String.class)) {
            writer.align(this.startOffset, 4);
            writer.writeRpcString((String)src, false);
        } else if (c.equals(NtString.class)) {
            writer.align(0, 4);
            writer.writeRpcString(((NtString)src).getString(), true);
        } else if (c.isArray()) {
            writer.align(this.startOffset, 4);
            int len = Array.getLength(src);
            writer.writeInt4(len);
            if (0 == len) {
                writer.writeInt4(0);
            } else {
                for (int n = 0; n < len; ++n) {
                    this.serialize(writer, '$', c.getComponentType(), Array.get(src, n), curHandle);
                }
            }
        } else if (c.equals(Vector.class)) {
            writer.align(this.startOffset, 4);
            Vector v = (Vector)src;
            int len = v.size();
            writer.writeInt4(len);
            for (int n = 0; n < len; ++n) {
                Object element = v.get(n);
                this.serialize(writer, '$', null == element ? Object.class : element.getClass(), element, curHandle);
            }
        } else if (SmbSerializable.class.isAssignableFrom(c)) {
            SmbSerializable blobSrc = (SmbSerializable)src;
            int sizeOffset = writer.getOffset();
            writer.writeCardinal(0L);
            int size = blobSrc.write(writer);
            int tempOffset = writer.getOffset();
            writer.writeCardinal(sizeOffset, size);
        } else if (!c.isPrimitive()) {
            writer.align(this.startOffset, 4);
            ObjectHandle nestedHandle = new ObjectHandle();
            Field[] fields = c.getDeclaredFields();
            for (int i = 0; i < fields.length; ++i) {
                if (!Modifier.isPublic(fields[i].getModifiers())) continue;
                try {
                    this.serialize(writer, fields[i].getName().charAt(0), fields[i].getType(), fields[i].get(src), nestedHandle);
                    continue;
                }
                catch (Exception e) {
                    throw new NqException("Illegal PDU object: " + e.getMessage());
                }
            }
            nestedHandle.popObjects(writer);
        } else {
            throw new NqException("Unsupported field type: " + src.getClass().getName());
        }
    }

    private Object deSerialize(RpcReader reader, Object masterArray, int idx, String fieldName, Class fieldClass, ObjectHandle curHandle) throws NqException, IllegalArgumentException, IllegalAccessException, InstantiationException, NoSuchMethodException, SecurityException, InvocationTargetException, NoSuchFieldException {
        if (fieldName.startsWith("_")) {
            curHandle.readObjectRef(reader, fieldName, masterArray, idx);
            return null;
        }
        if (fieldName.startsWith("$")) {
            return reader.readCardinal();
        }
        if (fieldClass.equals(Long.TYPE)) {
            return reader.readInt8();
        }
        if (fieldClass.equals(Integer.TYPE)) {
            return reader.readInt4();
        }
        if (fieldClass.equals(Short.TYPE)) {
            return reader.readInt2();
        }
        if (fieldClass.equals(String.class)) {
            return reader.readReferencedString(false);
        }
        if (fieldClass.equals(NtString.class)) {
            return new NtString(reader.readReferencedString(true));
        }
        if (fieldClass.isArray()) {
            int len = reader.readInt4();
            Class<?> elementClass = fieldClass.getComponentType();
            Object array = Array.newInstance(elementClass, len);
            for (int i = 0; i < len; ++i) {
                Array.set(array, i, this.deSerialize(reader, array, i, fieldName, elementClass, curHandle));
            }
            return array;
        }
        if (SmbSerializable.class.isAssignableFrom(fieldClass)) {
            SmbSerializable res = (SmbSerializable)fieldClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            long size = reader.readCardinal();
            int offset = reader.getOffset();
            res.read(reader);
            reader.setOffset(offset + (int)size);
            return res;
        }
        if (!fieldClass.isPrimitive()) {
            Object res = fieldClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
            ObjectHandle nestedHandle = masterArray != null ? curHandle : new ObjectHandle();
            Field[] fields = fieldClass.getFields();
            for (int i = 0; i < fields.length; ++i) {
                Object val;
                if (!Modifier.isPublic(fields[i].getModifiers()) || null == (val = this.deSerialize(reader, masterArray, idx, fields[i].getName(), fields[i].getType(), nestedHandle))) continue;
                fields[i].set(res, val);
            }
            if (null == masterArray) {
                nestedHandle.readReferencedObjects(reader, res);
            }
            return res;
        }
        throw new NqException("Unsupported field type: " + fieldClass.getName());
    }

    public void performCall(int opnum, Pdu inPdu, Pdu outPdu) throws NqException {
        RpcWriter writer = this.startCall(opnum);
        this.startOffset = writer.getOffset();
        this.serialize(writer, ' ', inPdu.getClass(), inPdu, null);
        RpcReader reader = this.performCall(writer);
        ObjectHandle handle = new ObjectHandle();
        Field[] fields = outPdu.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; ++i) {
            if (!Modifier.isPublic(fields[i].getModifiers())) continue;
            try {
                Object val = this.deSerialize(reader, null, 0, fields[i].getName(), fields[i].getType(), handle);
                if (null == val) continue;
                fields[i].set(outPdu, val);
                continue;
            }
            catch (Exception e) {
                throw new NqException("Unable to de-serialize RPC response PDU: " + e.getMessage());
            }
        }
    }

    public RpcWriter startCall(int opnum) throws NqException {
        RpcWriter writer = new RpcWriter(this);
        writer.writeInt4(80);
        writer.writeInt2(this.contextIdx);
        writer.writeInt2(opnum);
        return writer;
    }

    public RpcReader performCall(BufferWriter writer) throws NqException {
        ((RpcWriter)writer).sendLast();
        RpcReader reader = null;
        int counter = 0;
        while (counter++ < 3) {
            try {
                reader = new RpcReader(this);
                break;
            }
            catch (NqException e) {
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException ie) {
                    // empty catch block
                }
                if (counter != 3) continue;
                throw new SmbException("RPC Error: " + e.getErrCode(), e.getErrCode());
            }
        }
        if (reader.packetType != 2) {
            int reason = reader.readInt4();
            throw new SmbException("RPC fault: " + reason, reason);
        }
        return reader;
    }

    private void connect(Credentials creds) throws NqException {
        this.credentials = creds;
        try {
            if (useTcp) {
                this.transport = new TcpTransport(this, creds);
            }
        }
        catch (NqException e) {
            // empty catch block
        }
        if (useSmb) {
            try {
                this.transport = new SmbTransport(this, creds);
            }
            catch (NqException e) {
                throw (NqException)Utility.throwableInitCauseException(new NqException("failed to connect " + this.rpcDescr.name, e.getErrCode()), e);
            }
        }
        if (null == this.transport) {
            throw new NqException("No available transport", -22);
        }
        this.connected = true;
        int bindSize = 12 + this.rpcDescr.contexts.length * 24;
        for (int i = 0; i < this.rpcDescr.contexts.length; ++i) {
            bindSize += this.rpcDescr.contexts[i].transferOptions.length * 20;
        }
        RpcWriter writer = new RpcWriter(this, bindSize + 2);
        writer.writeHeader(11, true, true, bindSize);
        Server server = this.getServer();
        this.maxXmit = this.maxXmit < server.maxTrans - 100 ? this.maxXmit : server.maxTrans - 100;
        this.maxRecv = this.maxRecv < server.maxTrans - 100 ? this.maxRecv : server.maxTrans - 100;
        writer.writeInt2(this.maxXmit);
        writer.writeInt2(this.maxRecv);
        writer.writeInt4(0);
        writer.writeByte((byte)this.rpcDescr.contexts.length);
        writer.align(0, 4);
        for (int i = 0; i < this.rpcDescr.contexts.length; ++i) {
            Context context = this.rpcDescr.contexts[i];
            writer.writeInt2(i);
            writer.writeByte((byte)context.transferOptions.length);
            writer.align(0, 2);
            context.syntaxGuid.write(writer);
            writer.writeInt2(context.versMajor);
            writer.writeInt2(context.versMinor);
            for (int j = 0; j < context.transferOptions.length; ++j) {
                TransferSyntax ts = context.transferOptions[j];
                ts.syntaxGuid.write(writer);
                writer.writeInt4(ts.vers);
            }
        }
        writer.sendLast();
        RpcReader reader = new RpcReader(this, bindSize + 100);
        if (reader.packetType != 12) {
            throw new SmbException(-1073741790);
        }
        this.maxXmit = 0xFFFF & reader.readInt2();
        this.maxRecv = 0xFFFF & reader.readInt2();
        reader.skip(4);
        short addrLen = reader.readInt2();
        reader.skip(addrLen);
        reader.align(0, 4);
        int numRes = reader.readByte();
        reader.align(0, 4);
        for (int i = 0; i < numRes; ++i) {
            short res = reader.readInt2();
            if (res == 0) {
                Context context = this.rpcDescr.contexts[i];
                for (int k = 0; k < context.transferOptions.length; ++k) {
                    reader.skip(2);
                    UUID receivedId = new UUID();
                    receivedId.read(reader);
                    int vers = reader.readInt4();
                    TransferSyntax nextSyntax = context.transferOptions[k];
                    if (!receivedId.equals(nextSyntax.syntaxGuid) || nextSyntax.vers != vers) continue;
                    this.contextIdx = i;
                    this.transferSyntax = context.transferOptions[k];
                    return;
                }
                continue;
            }
            reader.skip(22);
        }
        throw new NqException("Bind acknowledged but does not match syntax", -22);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean close() {
        TraceLog.get().enter("hashCode = " + this.hashCode(), 200);
        Dcerpc dcerpc = this;
        synchronized (dcerpc) {
            if (!this.connected) {
                TraceLog.get().exit("The transport is already been closed", 200);
                return false;
            }
            try {
                super.close();
            }
            catch (NqException nqException) {
                // empty catch block
            }
            if (null != this.transport) {
                this.transport.close();
                this.transport = null;
            }
            this.connected = false;
        }
        TraceLog.get().exit(200);
        return false;
    }

    protected void finalize() throws Throwable {
        TraceLog.get().enter("hashCode = " + this.hashCode(), 2000);
        this.close();
        TraceLog.get().exit(2000);
    }

    public ObjectHandle startObject() {
        return new ObjectHandle();
    }

    public void writeEmptyArray(RpcWriter writer) throws NqException {
        writer.writeCardinal(this.refId++);
        writer.writeInt4(0);
        writer.writeCardinal(0L);
    }

    public int nextRefId() {
        return this.refId++;
    }

    public void checkResult(RpcReader reader) throws NqException {
        int res = reader.readInt4();
        if (-2147483622 == res) {
            return;
        }
        if (0 != res) {
            throw new SmbException("RPC call failed", res);
        }
    }

    protected RpcReader performRpc(RpcWriter writer) throws NqException {
        RpcReader reader = null;
        try {
            reader = this.performCall(writer);
        }
        catch (SmbException e) {
            TraceLog.get().error("e = ", e, 10, e.getErrCode());
            TraceLog.get().caught(e, 10);
            throw e;
        }
        return reader;
    }

    public boolean checkResult(RpcReader reader, int expectedError) throws NqException {
        int res = reader.readInt4();
        if (0 != res && res != expectedError) {
            throw new SmbException("RPC call failed", res);
        }
        return res == 0;
    }

    public void writeHandle(BufferWriter writer, Handle handle) {
        writer.writeBytes(handle.data);
    }

    public Handle readHandle(BufferReader reader) throws NqException {
        Handle handle = new Handle();
        reader.readBytes(handle.data, handle.data.length);
        return handle;
    }

    public class Handle {
        protected byte[] data = new byte[20];

        public byte[] getData() {
            return this.data;
        }
    }

    public class ObjectHandle {
        private LinkedList pendingStrings = new LinkedList();
        private LinkedList referencedObjects = new LinkedList();

        public void writeStringRef(RpcWriter writer, String str) throws NqException {
            if (null == str) {
                writer.writeCardinal(0L);
            } else {
                writer.writeCardinal(Dcerpc.this.refId++);
                this.pendingStrings.add(str);
            }
        }

        public void placeStrings(RpcWriter writer, boolean nullTerm) throws NqException {
            Iterator iter = this.pendingStrings.iterator();
            while (iter.hasNext()) {
                writer.align(0, 4);
                String str = (String)iter.next();
                writer.writeRpcString(str, nullTerm);
            }
        }

        protected boolean readObjectRef(RpcReader reader, String fieldName, Object masterArray, int idx) throws NqException {
            long refId = reader.readCardinal();
            if (0L == refId) {
                return false;
            }
            this.referencedObjects.add(new ObjectRef(fieldName, masterArray, idx));
            return true;
        }

        protected void readReferencedObjects(RpcReader reader, Object dst) throws NqException, IllegalArgumentException, IllegalAccessException, InstantiationException, NoSuchMethodException, SecurityException, InvocationTargetException, NoSuchFieldException {
            Iterator iter = this.referencedObjects.iterator();
            int i = 0;
            while (iter.hasNext()) {
                ObjectRef ref = (ObjectRef)this.referencedObjects.remove();
                String fieldName = ref.fieldName;
                Object masterArray = ref.masterArray;
                Class<?> masterClass = null == masterArray ? dst.getClass() : masterArray.getClass().getComponentType();
                Field field = masterClass.getField(fieldName);
                Class<?> fieldClass = field.getType();
                int idx = ref.idx;
                Object val = Dcerpc.this.deSerialize(reader, null, idx, "XXX", fieldClass, this);
                if (null != masterArray) {
                    Object component = Array.get(masterArray, idx);
                    if (component == null) {
                        component = masterClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                        Array.set(masterArray, idx, component);
                    }
                    field.set(component, val);
                } else {
                    field.set(dst, val);
                }
                ++i;
            }
        }

        protected void pushObject(Object obj) {
            this.referencedObjects.add(obj);
        }

        protected void popObjects(RpcWriter writer) throws NqException {
            Iterator iter = this.referencedObjects.iterator();
            while (iter.hasNext()) {
                writer.align(0, 4);
                Object obj = iter.next();
                Dcerpc.this.serialize(writer, ' ', obj.getClass(), obj, this);
            }
        }

        private class ObjectRef {
            String fieldName;
            Object masterArray;
            int idx;

            protected ObjectRef(String fieldName, Object masterArray, int idx) {
                this.fieldName = fieldName;
                this.masterArray = masterArray;
                this.idx = idx;
            }
        }
    }

    protected static abstract class Transport {
        Transport() throws NqException {
        }

        abstract void write(Buffer var1) throws NqException;

        abstract int read(Buffer var1) throws NqException;

        abstract void close();
    }

    public static class RpcDescriptor {
        public String name;
        public Context[] contexts;
    }

    public static class Context {
        public UUID syntaxGuid;
        public int versMajor;
        public int versMinor;
        public TransferSyntax[] transferOptions;
    }

    public class NtString {
        private String str;

        public NtString(String str) {
            this.str = str;
        }

        public String toString() {
            return this.str;
        }

        public String getString() {
            return this.str;
        }

        public void putString(String str) {
            this.str = str;
        }
    }

    public static interface Pdu
    extends Serializable {
    }

    public static class Ndr64bit
    extends TransferSyntax {
        public Ndr64bit() {
            this.vers = 1;
            this.align = 8;
            this.syntaxGuid = new UUID(1903232307, 48826, 18743, new byte[]{-125, 25}, new byte[]{-75, -37, -17, -100, -52, 54});
        }

        public long readCardinal(RpcReader reader) throws NqException {
            reader.align(0, this.align);
            return reader.readInt8();
        }

        public void writeCardinal(RpcWriter writer, long value) throws NqException {
            writer.align(0, this.align);
            writer.writeInt8((int)value);
        }
    }

    public static class Ndr32bit
    extends TransferSyntax {
        public Ndr32bit() {
            this.vers = 2;
            this.align = 4;
            this.syntaxGuid = new UUID(-1970774780, 7403, 4553, new byte[]{-97, -24}, new byte[]{8, 0, 43, 16, 72, 96});
        }

        public long readCardinal(RpcReader reader) throws NqException {
            return reader.readInt4();
        }

        public void writeCardinal(RpcWriter writer, long value) throws NqException {
            writer.writeInt4((int)value);
        }
    }

    public static abstract class TransferSyntax {
        protected UUID syntaxGuid;
        protected int vers;
        protected int align = 0;

        public abstract long readCardinal(RpcReader var1) throws NqException;

        public abstract void writeCardinal(RpcWriter var1, long var2) throws NqException;
    }
}

