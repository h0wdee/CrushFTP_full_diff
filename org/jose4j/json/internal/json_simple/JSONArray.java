/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.json.internal.json_simple;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.jose4j.json.internal.json_simple.JSONAware;
import org.jose4j.json.internal.json_simple.JSONStreamAware;
import org.jose4j.json.internal.json_simple.JSONValue;

public class JSONArray
extends ArrayList
implements JSONAware,
JSONStreamAware {
    private static final long serialVersionUID = 3957988303675231981L;

    public JSONArray() {
    }

    public JSONArray(Collection c) {
        super(c);
    }

    public static void writeJSONString(Collection collection, Writer out) throws IOException {
        if (collection == null) {
            out.write("null");
            return;
        }
        boolean first = true;
        Iterator iter = collection.iterator();
        out.write(91);
        while (iter.hasNext()) {
            if (first) {
                first = false;
            } else {
                out.write(44);
            }
            Object value = iter.next();
            if (value == null) {
                out.write("null");
                continue;
            }
            JSONValue.writeJSONString(value, out);
        }
        out.write(93);
    }

    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONArray.writeJSONString(this, out);
    }

    public static String toJSONString(Collection collection) {
        StringWriter writer = new StringWriter();
        try {
            JSONArray.writeJSONString(collection, (Writer)writer);
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJSONString(byte[] array, Writer out) throws IOException {
        if (array == null) {
            out.write("null");
        } else if (array.length == 0) {
            out.write("[]");
        } else {
            out.write("[");
            out.write(String.valueOf(array[0]));
            int i = 1;
            while (i < array.length) {
                out.write(",");
                out.write(String.valueOf(array[i]));
                ++i;
            }
            out.write("]");
        }
    }

    public static String toJSONString(byte[] array) {
        StringWriter writer = new StringWriter();
        try {
            JSONArray.writeJSONString(array, (Writer)writer);
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJSONString(short[] array, Writer out) throws IOException {
        if (array == null) {
            out.write("null");
        } else if (array.length == 0) {
            out.write("[]");
        } else {
            out.write("[");
            out.write(String.valueOf(array[0]));
            int i = 1;
            while (i < array.length) {
                out.write(",");
                out.write(String.valueOf(array[i]));
                ++i;
            }
            out.write("]");
        }
    }

    public static String toJSONString(short[] array) {
        StringWriter writer = new StringWriter();
        try {
            JSONArray.writeJSONString(array, (Writer)writer);
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJSONString(int[] array, Writer out) throws IOException {
        if (array == null) {
            out.write("null");
        } else if (array.length == 0) {
            out.write("[]");
        } else {
            out.write("[");
            out.write(String.valueOf(array[0]));
            int i = 1;
            while (i < array.length) {
                out.write(",");
                out.write(String.valueOf(array[i]));
                ++i;
            }
            out.write("]");
        }
    }

    public static String toJSONString(int[] array) {
        StringWriter writer = new StringWriter();
        try {
            JSONArray.writeJSONString(array, (Writer)writer);
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJSONString(long[] array, Writer out) throws IOException {
        if (array == null) {
            out.write("null");
        } else if (array.length == 0) {
            out.write("[]");
        } else {
            out.write("[");
            out.write(String.valueOf(array[0]));
            int i = 1;
            while (i < array.length) {
                out.write(",");
                out.write(String.valueOf(array[i]));
                ++i;
            }
            out.write("]");
        }
    }

    public static String toJSONString(long[] array) {
        StringWriter writer = new StringWriter();
        try {
            JSONArray.writeJSONString(array, (Writer)writer);
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJSONString(float[] array, Writer out) throws IOException {
        if (array == null) {
            out.write("null");
        } else if (array.length == 0) {
            out.write("[]");
        } else {
            out.write("[");
            out.write(String.valueOf(array[0]));
            int i = 1;
            while (i < array.length) {
                out.write(",");
                out.write(String.valueOf(array[i]));
                ++i;
            }
            out.write("]");
        }
    }

    public static String toJSONString(float[] array) {
        StringWriter writer = new StringWriter();
        try {
            JSONArray.writeJSONString(array, (Writer)writer);
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJSONString(double[] array, Writer out) throws IOException {
        if (array == null) {
            out.write("null");
        } else if (array.length == 0) {
            out.write("[]");
        } else {
            out.write("[");
            out.write(String.valueOf(array[0]));
            int i = 1;
            while (i < array.length) {
                out.write(",");
                out.write(String.valueOf(array[i]));
                ++i;
            }
            out.write("]");
        }
    }

    public static String toJSONString(double[] array) {
        StringWriter writer = new StringWriter();
        try {
            JSONArray.writeJSONString(array, (Writer)writer);
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJSONString(boolean[] array, Writer out) throws IOException {
        if (array == null) {
            out.write("null");
        } else if (array.length == 0) {
            out.write("[]");
        } else {
            out.write("[");
            out.write(String.valueOf(array[0]));
            int i = 1;
            while (i < array.length) {
                out.write(",");
                out.write(String.valueOf(array[i]));
                ++i;
            }
            out.write("]");
        }
    }

    public static String toJSONString(boolean[] array) {
        StringWriter writer = new StringWriter();
        try {
            JSONArray.writeJSONString(array, (Writer)writer);
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJSONString(char[] array, Writer out) throws IOException {
        if (array == null) {
            out.write("null");
        } else if (array.length == 0) {
            out.write("[]");
        } else {
            out.write("[\"");
            out.write(JSONValue.escape(String.valueOf(array[0])));
            int i = 1;
            while (i < array.length) {
                out.write("\",\"");
                out.write(JSONValue.escape(String.valueOf(array[i])));
                ++i;
            }
            out.write("\"]");
        }
    }

    public static String toJSONString(char[] array) {
        StringWriter writer = new StringWriter();
        try {
            JSONArray.writeJSONString(array, (Writer)writer);
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeJSONString(Object[] array, Writer out) throws IOException {
        if (array == null) {
            out.write("null");
        } else if (array.length == 0) {
            out.write("[]");
        } else {
            out.write("[");
            JSONValue.writeJSONString(array[0], out);
            int i = 1;
            while (i < array.length) {
                out.write(",");
                JSONValue.writeJSONString(array[i], out);
                ++i;
            }
            out.write("]");
        }
    }

    public static String toJSONString(Object[] array) {
        StringWriter writer = new StringWriter();
        try {
            JSONArray.writeJSONString(array, (Writer)writer);
            return writer.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toJSONString() {
        return JSONArray.toJSONString(this);
    }

    @Override
    public String toString() {
        return this.toJSONString();
    }
}

