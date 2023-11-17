/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.boris.winrun4j.Closure;
import org.boris.winrun4j.Native;
import org.boris.winrun4j.NativeBinder;
import org.boris.winrun4j.NativeHelper;

public class PInvoke {
    private static boolean is64 = Native.IS_64;

    public static void bind(Class clazz) {
        NativeBinder.bind(clazz);
    }

    public static void bind(Class clazz, String library) {
        NativeBinder.bind(clazz, library);
    }

    public static int sizeOf(Class struct, boolean wideChar) {
        NativeStruct ns = NativeStruct.fromClass(struct, wideChar);
        return ns == null ? 0 : ns.sizeOf();
    }

    public static class NativeStruct {
        private boolean wideChar;
        private Field[] fields;
        private int[] fieldTypes;
        private int[] fieldSizes;
        private Map<Field, NativeStruct> childStructs = new HashMap<Field, NativeStruct>();
        private int size;

        public NativeStruct(boolean wideChar) {
            this.wideChar = wideChar;
        }

        public static NativeStruct fromClass(Class struct, boolean wideChar) {
            if (struct == null) {
                return null;
            }
            if (!Struct.class.isAssignableFrom(struct)) {
                throw new RuntimeException("Invalid class used as struct: " + struct.getSimpleName());
            }
            NativeStruct ns = new NativeStruct(wideChar);
            ns.parse(struct);
            return ns;
        }

        public void parse(Class struct) {
            int i;
            Field[] fields = struct.getFields();
            ArrayList<Field> fieldList = new ArrayList<Field>();
            ArrayList<Integer> fieldTypes = new ArrayList<Integer>();
            ArrayList<Integer> fieldSizes = new ArrayList<Integer>();
            int size = 0;
            for (i = 0; i < fields.length; ++i) {
                MarshalAs ma;
                Field f = fields[i];
                if (Modifier.isStatic(f.getModifiers()) || !Modifier.isPublic(f.getModifiers())) continue;
                int ft = NativeBinder.getArgType(f.getType(), struct.getSimpleName());
                if (ft == 6 && (ma = f.getAnnotation(MarshalAs.class)) == null) {
                    ft = 15;
                }
                fieldList.add(f);
                fieldTypes.add(ft);
                if (ft == 8) {
                    this.childStructs.put(f, NativeStruct.fromClass(f.getType(), this.wideChar));
                }
                int sz = this.sizeOf(ft, f);
                fieldSizes.add(sz);
                size += sz;
            }
            this.fields = fieldList.toArray(new Field[0]);
            this.fieldTypes = new int[fields.length];
            this.fieldSizes = new int[fields.length];
            for (i = 0; i < this.fields.length; ++i) {
                this.fieldTypes[i] = (Integer)fieldTypes.get(i);
                this.fieldSizes[i] = (Integer)fieldSizes.get(i);
            }
            this.size = size;
        }

        private int sizeOf(int fieldType, Field field) {
            int nativeSize = Native.IS_64 ? 8 : 4;
            int size = 0;
            switch (fieldType) {
                case 2: 
                case 13: {
                    ++size;
                    break;
                }
                case 11: {
                    size += 2;
                    break;
                }
                case 1: {
                    size += 4;
                    break;
                }
                case 4: 
                case 5: 
                case 9: 
                case 14: 
                case 15: {
                    size += nativeSize;
                    break;
                }
                case 6: {
                    MarshalAs ma = field.getAnnotation(MarshalAs.class);
                    if (ma == null) {
                        throw new RuntimeException("Invalid string arg type: " + field.getName());
                    }
                    size += ma.sizeConst();
                    if (!this.wideChar) break;
                    size <<= 1;
                    break;
                }
                case 3: {
                    throw new RuntimeException("StringBuilder not supported in structs - " + field.getName());
                }
                case 8: {
                    size += this.childStructs.get(field).sizeOf();
                    break;
                }
                default: {
                    throw new RuntimeException("Unsupported struct type: " + field.getName());
                }
            }
            return size;
        }

        public int sizeOf() {
            return this.size;
        }

        public long toNative(Object obj) throws IllegalArgumentException, IllegalAccessException {
            long ptr = Native.malloc(this.size);
            this.toNative(ptr, obj);
            return ptr;
        }

        public void toNative(long ptr, Object obj) throws IllegalArgumentException, IllegalAccessException {
            ByteBuffer bb = NativeHelper.getBuffer(ptr, this.size);
            for (int i = 0; i < this.fieldTypes.length; ++i) {
                this.toNative(obj, this.fieldTypes[i], this.fieldSizes[i], this.fields[i], bb);
            }
        }

        private void toNative(Object obj, int fieldType, int fieldSize, Field field, ByteBuffer bb) throws IllegalArgumentException, IllegalAccessException {
            switch (fieldType) {
                case 2: {
                    boolean b = field.getBoolean(obj);
                    bb.put(b ? (byte)1 : 0);
                    break;
                }
                case 13: {
                    bb.put(field.getByte(obj));
                    break;
                }
                case 1: {
                    bb.putInt(field.getInt(obj));
                    break;
                }
                case 11: {
                    bb.putShort(field.getShort(obj));
                    break;
                }
                case 14: {
                    Closure cl = (Closure)field.get(obj);
                    if (is64) {
                        bb.putLong(cl.getPointer());
                        break;
                    }
                    bb.putInt((int)(cl.getPointer() & 0xFFFFFFL));
                    break;
                }
                case 9: {
                    long l = field.getLong(obj);
                    if (is64) {
                        bb.putLong(l);
                        break;
                    }
                    bb.putInt((int)(l & 0xFFFFFFL));
                    break;
                }
                case 6: {
                    String s = (String)field.get(obj);
                    int bytesWritten = 0;
                    if (s != null) {
                        int i;
                        if (this.wideChar) {
                            char[] c = s.toCharArray();
                            for (i = 0; i < c.length && bytesWritten < fieldSize; bytesWritten += 2, ++i) {
                                bb.putChar(c[i]);
                            }
                        } else {
                            byte[] bs = s.getBytes();
                            for (i = 0; i < bs.length && bytesWritten < fieldSize; ++bytesWritten, ++i) {
                                bb.put(bs[i]);
                            }
                        }
                    }
                    for (int i = bytesWritten; i < fieldSize; ++i) {
                        bb.put((byte)0);
                    }
                    break;
                }
            }
        }

        public void fromNative(long ptr, Object obj) throws IllegalArgumentException, IllegalAccessException {
            ByteBuffer bb = NativeHelper.getBuffer(ptr, this.size);
            this.fromNative(bb, obj);
        }

        private void fromNative(ByteBuffer bb, Object obj) throws IllegalArgumentException, IllegalAccessException {
            for (int i = 0; i < this.fieldTypes.length; ++i) {
                this.fromNative(bb, this.fieldTypes[i], this.fieldSizes[i], this.fields[i], obj);
            }
        }

        private void fromNative(ByteBuffer bb, int fieldType, int fieldSize, Field field, Object obj) throws IllegalArgumentException, IllegalAccessException {
            switch (fieldType) {
                case 2: {
                    field.set(obj, bb.get() != 0);
                    break;
                }
                case 13: {
                    field.set(obj, bb.get());
                    break;
                }
                case 1: {
                    field.set(obj, bb.getInt());
                    break;
                }
                case 9: {
                    field.set(obj, is64 ? bb.getLong() : (long)bb.getInt());
                    break;
                }
                case 11: {
                    field.set(obj, bb.getShort());
                    break;
                }
                case 6: {
                    byte[] b = new byte[fieldSize];
                    bb.get(b);
                    field.set(obj, NativeHelper.getString(b, this.wideChar));
                }
            }
        }
    }

    public static interface Union {
    }

    public static interface Struct {
    }

    public static interface Callback {
    }

    public static class ByteArrayBuilder {
        private byte[] array;

        public void set(byte[] array) {
            this.array = array;
        }

        public byte[] toArray() {
            return this.array;
        }
    }

    public static class UIntPtr
    extends IntPtr {
        public UIntPtr() {
        }

        public UIntPtr(long value) {
            this.value = value;
        }
    }

    public static class IntPtr {
        public long value;

        public IntPtr() {
        }

        public IntPtr(long value) {
            this.value = value;
        }

        public int intValue() {
            return (int)this.value;
        }

        public String toString() {
            return Long.toString(this.value);
        }
    }

    @Retention(value=RetentionPolicy.RUNTIME)
    public static @interface MarshalAs {
        public int sizeConst() default 0;

        public boolean isPointer() default false;
    }

    @Retention(value=RetentionPolicy.RUNTIME)
    public static @interface Delegate {
    }

    @Retention(value=RetentionPolicy.RUNTIME)
    public static @interface Out {
    }

    @Retention(value=RetentionPolicy.RUNTIME)
    public static @interface DllImport {
        public String value() default "";

        public String lib() default "";

        public String entryPoint() default "";

        public boolean wideChar() default true;

        public boolean setLastError() default false;

        public boolean internal() default false;
    }
}

