/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.boris.winrun4j.Closure;
import org.boris.winrun4j.FFI;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeHelper;
import org.boris.winrun4j.PInvoke;

public class NativeBinder {
    private static Map<Method, NativeBinder> methods = new HashMap<Method, NativeBinder>();
    private static Map<Class, PInvoke.NativeStruct> ansiStructs = new ConcurrentHashMap<Class, PInvoke.NativeStruct>();
    private static Map<Class, PInvoke.NativeStruct> wideStructs = new ConcurrentHashMap<Class, PInvoke.NativeStruct>();
    private static boolean is64 = Native.IS_64;
    private static long invokeId = Native.getMethodId(NativeBinder.class, "invoke", "(JJ)V", false);
    private long function;
    private FFI.CIF callbackCif;
    private FFI.CIF functionCif;
    private int[] argTypes;
    private Class[] params;
    private int returnType;
    private int returnSize;
    private boolean wideChar;
    private int argSize;
    private long avalue;
    private long pvalue;
    private long objectId;
    private long methodId;
    private long handle;
    private long callback;
    static final int ARG_INT = 1;
    static final int ARG_BOOL = 2;
    static final int ARG_STRING_BUILDER = 3;
    static final int ARG_UINT_PTR = 4;
    static final int ARG_INT_PTR = 5;
    static final int ARG_STRING = 6;
    static final int ARG_CALLBACK = 7;
    static final int ARG_STRUCT_PTR = 8;
    static final int ARG_LONG = 9;
    static final int ARG_BYTE_ARRAY_BUILDER = 10;
    static final int ARG_SHORT = 11;
    static final int ARG_VOID = 12;
    static final int ARG_BYTE = 13;
    static final int ARG_RAW_CLOSURE = 14;
    static final int ARG_STRING_PTR = 15;
    static final int ARG_BYTE_ARRAY = 16;

    public static void bind(Class clazz) {
        NativeBinder.bind(clazz, null);
    }

    public static void bind(Class clazz, String library) {
        Method[] cms;
        for (Method m : cms = clazz.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (!Modifier.isStatic(mod) || !Modifier.isNative(mod)) continue;
            NativeBinder nb = methods.get(m);
            if (nb != null) {
                nb.destroy();
            }
            NativeBinder.register(clazz, m, library);
        }
    }

    private static void register(Class clazz, Method m, String library) {
        int i;
        long fun;
        PInvoke.DllImport di = m.getAnnotation(PInvoke.DllImport.class);
        if (di == null) {
            return;
        }
        String lib = di.value();
        if (lib == null || lib.length() == 0) {
            lib = di.lib();
        }
        if (lib == null || lib.length() == 0) {
            lib = library;
        }
        long lp = 0L;
        if (!di.internal()) {
            lp = Native.loadLibrary(lib);
        }
        if (lp == 0L && !di.internal()) {
            return;
        }
        String fn = di.entryPoint();
        if (fn == null || fn.length() == 0) {
            fn = m.getName();
        }
        if ((fun = Native.getProcAddress(lp, fn)) == 0L) {
            fn = fn + (di.wideChar() ? "W" : "A");
            fun = Native.getProcAddress(lp, fn);
        }
        if (fun == 0L) {
            return;
        }
        NativeBinder nb = new NativeBinder();
        nb.params = m.getParameterTypes();
        Class<?>[] params = nb.params;
        Class<?> returnType = m.getReturnType();
        nb.function = fun;
        int[] types = new int[params.length + 2];
        types[1] = 14;
        types[0] = 14;
        for (i = 0; i < params.length; ++i) {
            int t = 14;
            if (!is64 && Long.TYPE.equals(params[i])) {
                t = 12;
            }
            types[i + 2] = t;
        }
        nb.callbackCif = FFI.CIF.prepare(is64 ? 1 : 2, types);
        nb.functionCif = FFI.CIF.prepare(is64 ? 1 : 2, params.length);
        nb.argTypes = new int[params.length];
        for (i = 0; i < params.length; ++i) {
            nb.argTypes[i] = NativeBinder.getArgType(params[i], m.getName());
        }
        nb.returnType = NativeBinder.getArgType(returnType, m.getName());
        if (nb.returnType == 6) {
            PInvoke.MarshalAs ma = m.getAnnotation(PInvoke.MarshalAs.class);
            if (ma == null) {
                throw new RuntimeException("Return type of string must have MarshalAs size: " + m.getName());
            }
            nb.returnSize = ma.sizeConst();
        }
        nb.wideChar = di.wideChar();
        if (params.length > 0) {
            nb.argSize = params.length * NativeHelper.PTR_SIZE;
            nb.avalue = Native.malloc(nb.argSize);
            nb.pvalue = Native.malloc(nb.argSize);
            ByteBuffer pb = NativeHelper.getBuffer(nb.pvalue, nb.argSize);
            for (int i2 = 0; i2 < nb.argTypes.length; ++i2) {
                if (is64) {
                    pb.putLong(nb.avalue + (long)(i2 * NativeHelper.PTR_SIZE));
                    continue;
                }
                pb.putInt((int)(nb.avalue + (long)(i2 * NativeHelper.PTR_SIZE)));
            }
        }
        nb.objectId = Native.newGlobalRef(nb);
        nb.methodId = invokeId;
        nb.handle = FFI.prepareClosure(nb.callbackCif.get(), nb.objectId, nb.methodId);
        nb.callback = NativeHelper.getPointer(nb.handle);
        if (!Native.bind(clazz, m.getName(), NativeBinder.generateSig(m), nb.callback)) {
            nb.destroy();
        } else {
            methods.put(m, nb);
        }
    }

    public static String generateSig(Method m) {
        Class<?>[] c;
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Class<?> cl : c = m.getParameterTypes()) {
            sb.append(NativeBinder.generateSig(cl));
        }
        sb.append(")");
        sb.append(NativeBinder.generateSig(m.getReturnType()));
        return sb.toString();
    }

    public static String generateSig(Class c) {
        if (Boolean.TYPE.equals(c)) {
            return "Z";
        }
        if (Byte.TYPE.equals(c)) {
            return "B";
        }
        if (Character.TYPE.equals(c)) {
            return "C";
        }
        if (Short.TYPE.equals(c)) {
            return "S";
        }
        if (Integer.TYPE.equals(c)) {
            return "I";
        }
        if (Long.TYPE.equals(c)) {
            return "J";
        }
        if (Float.TYPE.equals(c)) {
            return "F";
        }
        if (Double.TYPE.equals(c)) {
            return "D";
        }
        if (Void.TYPE.equals(c)) {
            return "V";
        }
        if (c.isArray()) {
            return "[" + NativeBinder.generateSig(c.getComponentType());
        }
        return "L" + c.getName().replace('.', '/') + ";";
    }

    static int getArgType(Class clazz, String source) {
        if (Integer.TYPE.equals(clazz)) {
            return 1;
        }
        if (Boolean.TYPE.equals(clazz)) {
            return 2;
        }
        if (Byte.TYPE.equals(clazz)) {
            return 13;
        }
        if (Short.TYPE.equals(clazz)) {
            return 11;
        }
        if (Long.TYPE.equals(clazz)) {
            return 9;
        }
        if (Void.TYPE.equals(clazz)) {
            return 12;
        }
        if (PInvoke.IntPtr.class.equals((Object)clazz)) {
            return 5;
        }
        if (PInvoke.UIntPtr.class.equals((Object)clazz)) {
            return 4;
        }
        if (StringBuilder.class.equals((Object)clazz)) {
            return 3;
        }
        if (String.class.equals((Object)clazz)) {
            return 6;
        }
        if (PInvoke.Struct.class.isAssignableFrom(clazz)) {
            return 8;
        }
        if (PInvoke.Callback.class.isAssignableFrom(clazz)) {
            return 7;
        }
        if (Closure.class.isAssignableFrom(clazz)) {
            return 14;
        }
        if (PInvoke.ByteArrayBuilder.class.isAssignableFrom(clazz)) {
            return 10;
        }
        if (byte[].class.isAssignableFrom(clazz)) {
            return 16;
        }
        throw new RuntimeException("Unrecognized native argument type: " + clazz + " from " + source);
    }

    static PInvoke.NativeStruct getStruct(Class clazz, boolean wideChar) {
        PInvoke.NativeStruct ns = wideChar ? wideStructs.get(clazz) : ansiStructs.get(clazz);
        if (ns == null) {
            ns = PInvoke.NativeStruct.fromClass(clazz, wideChar);
            if (wideChar) {
                wideStructs.put(clazz, ns);
            } else {
                ansiStructs.put(clazz, ns);
            }
        }
        return ns;
    }

    public void invoke(long resp, long args) throws Exception {
        long ptr;
        Object[] jargs = new Object[this.argSize];
        if (this.argTypes.length > 0) {
            long offset = 2 * NativeHelper.PTR_SIZE;
            ByteBuffer ib = NativeHelper.getBuffer(args + offset, this.argSize);
            ByteBuffer pb = NativeHelper.getBuffer(this.pvalue, this.argSize);
            ByteBuffer vb = NativeHelper.getBuffer(this.avalue, this.argSize);
            for (int i = 0; i < this.argTypes.length; ++i) {
                long argValue = 0L;
                long pointer = this.avalue + (long)(i * NativeHelper.PTR_SIZE);
                long inp = is64 ? ib.getLong() : (long)ib.getInt();
                long inv = inp == 0L ? 0L : NativeHelper.getPointer(inp);
                switch (this.argTypes[i]) {
                    case 1: 
                    case 2: 
                    case 11: {
                        argValue = inv;
                        jargs[i] = new Integer((int)inv);
                        break;
                    }
                    case 9: {
                        argValue = inv;
                        break;
                    }
                    case 4: 
                    case 5: {
                        if (inv == 0L) break;
                        jargs[i] = Native.getObject(inv);
                        int value = (int)((PInvoke.IntPtr)jargs[i]).value;
                        argValue = Native.malloc(4);
                        NativeHelper.setInt(argValue, value);
                        if (i <= 0 || value <= 0) break;
                        long sptr = 0L;
                        if (this.argTypes[i - 1] == 3) {
                            int ssize = value;
                            if (this.wideChar) {
                                ssize *= 2;
                            }
                            sptr = Native.malloc(ssize);
                        } else if (this.argTypes[i - 1] == 10) {
                            sptr = Native.malloc(value);
                        }
                        if (sptr == 0L) break;
                        NativeHelper.setInt(this.avalue + (long)((i - 1) * NativeHelper.PTR_SIZE), (int)sptr);
                        break;
                    }
                    case 3: 
                    case 10: {
                        if (inv == 0L) break;
                        jargs[i] = Native.getObject(inv);
                        break;
                    }
                    case 6: {
                        if (inv == 0L) break;
                        jargs[i] = Native.getObject(inv);
                        argValue = NativeHelper.toNativeString(jargs[i], this.wideChar);
                        break;
                    }
                    case 7: {
                        if (inv == 0L) break;
                        Object o = Native.getObject(inv);
                        Closure c = Closure.build(this.params[i], o, this.wideChar);
                        if (c == null) {
                            throw new RuntimeException("Could not create callback for parameter " + (i + 1));
                        }
                        argValue = c.getPointer();
                        jargs[i] = c;
                        break;
                    }
                    case 14: {
                        if (inv == 0L) break;
                        jargs[i] = Native.getObject(inv);
                        argValue = ((Closure)jargs[i]).getPointer();
                        break;
                    }
                    case 8: {
                        if (inv == 0L) break;
                        Object o = Native.getObject(inv);
                        PInvoke.NativeStruct ns = NativeBinder.getStruct(this.params[i], this.wideChar);
                        argValue = ns.toNative(o);
                        jargs[i] = o;
                        break;
                    }
                    case 16: {
                        if (inv == 0L) break;
                        byte[] b = (byte[])Native.getObject(inv);
                        jargs[i] = b;
                        argValue = NativeHelper.toNative(b, 0, b.length);
                    }
                }
                if (is64) {
                    vb.putLong(argValue);
                    pb.putLong(pointer);
                    continue;
                }
                vb.putInt((int)(argValue & 0xFFFFFFFFFFFFFFFFL));
                pb.putInt((int)(pointer & 0xFFFFFFFFFFFFFFFFL));
            }
        }
        FFI.call(this.functionCif.get(), this.function, resp, this.pvalue);
        if (resp != 0L && this.returnType == 6 && (ptr = NativeHelper.getPointer(resp)) != 0L) {
            String s = NativeHelper.getString(ptr, this.returnSize, this.wideChar);
            long sptr = Native.getObjectId(s);
            NativeHelper.setPointer(resp, sptr);
        }
        if (this.argTypes.length > 0) {
            ByteBuffer vb = NativeHelper.getBuffer(this.avalue, this.argSize);
            long prevPointer = 0L;
            for (int i = 0; i < this.argTypes.length; ++i) {
                long argValue = is64 ? vb.getLong() : (long)vb.getInt();
                switch (this.argTypes[i]) {
                    case 1: 
                    case 2: 
                    case 11: {
                        break;
                    }
                    case 6: {
                        NativeHelper.free(argValue);
                        break;
                    }
                    case 4: 
                    case 5: {
                        long value;
                        if (argValue == 0L) break;
                        ((PInvoke.IntPtr)jargs[i]).value = value = NativeHelper.getPointer(argValue);
                        if (i > 0) {
                            if (this.argTypes[i - 1] == 3) {
                                if (value > 0L && jargs[i - 1] != null) {
                                    int ssize = (int)value;
                                    if (this.wideChar) {
                                        ssize *= 2;
                                    }
                                    String s = NativeHelper.getString(prevPointer, ssize, this.wideChar);
                                    ((StringBuilder)jargs[i - 1]).setLength(0);
                                    ((StringBuilder)jargs[i - 1]).append(s);
                                }
                                NativeHelper.free(prevPointer);
                            } else if (this.argTypes[i - 1] == 10) {
                                if (value > 0L && jargs[i - 1] != null) {
                                    ByteBuffer bb = NativeHelper.getBuffer(prevPointer, (int)value);
                                    byte[] buffer = new byte[(int)value];
                                    bb.get(buffer);
                                    ((PInvoke.ByteArrayBuilder)jargs[i - 1]).set(buffer);
                                }
                                NativeHelper.free(prevPointer);
                            }
                        }
                        NativeHelper.free(argValue);
                        break;
                    }
                    case 3: {
                        break;
                    }
                    case 7: {
                        if (jargs[i] == null) break;
                        ((Closure)jargs[i]).destroy();
                        break;
                    }
                    case 16: {
                        if (jargs[i] == null) break;
                        NativeHelper.free(argValue);
                        break;
                    }
                    case 14: {
                        break;
                    }
                    case 8: {
                        if (jargs[i] != null) {
                            PInvoke.NativeStruct ns = null;
                            ns = this.wideChar ? wideStructs.get(this.params[i]) : ansiStructs.get(this.params[i]);
                            ns.fromNative(argValue, jargs[i]);
                        }
                        NativeHelper.free(argValue);
                    }
                }
                prevPointer = argValue;
            }
        }
    }

    public synchronized void destroy() {
        if (this.callbackCif != null) {
            this.callbackCif.destroy();
            this.callbackCif = null;
        }
        if (this.functionCif != null) {
            this.functionCif.destroy();
            this.functionCif = null;
        }
        if (this.handle != 0L) {
            FFI.freeClosure(this.handle);
            this.handle = 0L;
            this.callback = 0L;
        }
        if (this.pvalue != 0L) {
            Native.free(this.pvalue);
            this.pvalue = 0L;
        }
        if (this.avalue != 0L) {
            Native.free(this.avalue);
            this.avalue = 0L;
        }
        if (this.objectId != 0L) {
            Native.deleteGlobalRef(this.objectId);
            this.objectId = 0L;
        }
    }
}

