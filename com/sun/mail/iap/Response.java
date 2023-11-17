/*
 * Decompiled with CFR 0.152.
 */
package com.sun.mail.iap;

import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.util.ASCIIUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Response {
    protected int index;
    protected int pindex;
    protected int size;
    protected byte[] buffer = null;
    protected int type = 0;
    protected String tag = null;
    protected Exception ex;
    private static final int increment = 100;
    public static final int TAG_MASK = 3;
    public static final int CONTINUATION = 1;
    public static final int TAGGED = 2;
    public static final int UNTAGGED = 3;
    public static final int TYPE_MASK = 28;
    public static final int OK = 4;
    public static final int NO = 8;
    public static final int BAD = 12;
    public static final int BYE = 16;
    public static final int SYNTHETIC = 32;
    private static String ATOM_CHAR_DELIM = " (){%*\"\\]";
    private static String ASTRING_CHAR_DELIM = " (){%*\"\\";

    public Response(String s) {
        this.buffer = ASCIIUtility.getBytes(s);
        this.size = this.buffer.length;
        this.parse();
    }

    public Response(Protocol p) throws IOException, ProtocolException {
        ByteArray ba = p.getResponseBuffer();
        ByteArray response = p.getInputStream().readResponse(ba);
        this.buffer = response.getBytes();
        this.size = response.getCount() - 2;
        this.parse();
    }

    public Response(Response r) {
        this.index = r.index;
        this.size = r.size;
        this.buffer = r.buffer;
        this.type = r.type;
        this.tag = r.tag;
    }

    public static Response byeResponse(Exception ex) {
        String err = "* BYE JavaMail Exception: " + ex.toString();
        err = err.replace('\r', ' ').replace('\n', ' ');
        Response r = new Response(err);
        r.type |= 0x20;
        r.ex = ex;
        return r;
    }

    private void parse() {
        this.index = 0;
        if (this.size == 0) {
            return;
        }
        if (this.buffer[this.index] == 43) {
            this.type |= 1;
            ++this.index;
            return;
        }
        if (this.buffer[this.index] == 42) {
            this.type |= 3;
            ++this.index;
        } else {
            this.type |= 2;
            this.tag = this.readAtom();
            if (this.tag == null) {
                this.tag = "";
            }
        }
        int mark = this.index;
        String s = this.readAtom();
        if (s == null) {
            s = "";
        }
        if (s.equalsIgnoreCase("OK")) {
            this.type |= 4;
        } else if (s.equalsIgnoreCase("NO")) {
            this.type |= 8;
        } else if (s.equalsIgnoreCase("BAD")) {
            this.type |= 0xC;
        } else if (s.equalsIgnoreCase("BYE")) {
            this.type |= 0x10;
        } else {
            this.index = mark;
        }
        this.pindex = this.index;
    }

    public void skipSpaces() {
        while (this.index < this.size && this.buffer[this.index] == 32) {
            ++this.index;
        }
    }

    public void skipToken() {
        while (this.index < this.size && this.buffer[this.index] != 32) {
            ++this.index;
        }
    }

    public void skip(int count) {
        this.index += count;
    }

    public byte peekByte() {
        if (this.index < this.size) {
            return this.buffer[this.index];
        }
        return 0;
    }

    public byte readByte() {
        if (this.index < this.size) {
            return this.buffer[this.index++];
        }
        return 0;
    }

    public String readAtom() {
        return this.readDelimString(ATOM_CHAR_DELIM);
    }

    private String readDelimString(String delim) {
        byte b;
        this.skipSpaces();
        if (this.index >= this.size) {
            return null;
        }
        int start = this.index;
        while (this.index < this.size && (b = this.buffer[this.index]) > 32 && delim.indexOf((char)b) < 0 && b >= 32 && b != 127) {
            ++this.index;
        }
        return ASCIIUtility.toString(this.buffer, start, this.index);
    }

    public String readString(char delim) {
        this.skipSpaces();
        if (this.index >= this.size) {
            return null;
        }
        int start = this.index;
        while (this.index < this.size && this.buffer[this.index] != delim) {
            ++this.index;
        }
        return ASCIIUtility.toString(this.buffer, start, this.index);
    }

    public String[] readStringList() {
        return this.readStringList(false);
    }

    public String[] readAtomStringList() {
        return this.readStringList(true);
    }

    private String[] readStringList(boolean atom) {
        this.skipSpaces();
        if (this.buffer[this.index] != 40) {
            return null;
        }
        ++this.index;
        ArrayList<String> result = new ArrayList<String>();
        this.skipSpaces();
        if (this.peekByte() != 41) {
            do {
                result.add(atom ? this.readAtomString() : this.readString());
            } while (this.index < this.size && this.buffer[this.index++] != 41);
        } else {
            ++this.index;
        }
        return result.toArray(new String[result.size()]);
    }

    public int readNumber() {
        this.skipSpaces();
        int start = this.index;
        while (this.index < this.size && Character.isDigit((char)this.buffer[this.index])) {
            ++this.index;
        }
        if (this.index > start) {
            try {
                return ASCIIUtility.parseInt(this.buffer, start, this.index);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return -1;
    }

    public long readLong() {
        this.skipSpaces();
        int start = this.index;
        while (this.index < this.size && Character.isDigit((char)this.buffer[this.index])) {
            ++this.index;
        }
        if (this.index > start) {
            try {
                return ASCIIUtility.parseLong(this.buffer, start, this.index);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return -1L;
    }

    public String readString() {
        return (String)this.parseString(false, true);
    }

    public ByteArrayInputStream readBytes() {
        ByteArray ba = this.readByteArray();
        if (ba != null) {
            return ba.toByteArrayInputStream();
        }
        return null;
    }

    public ByteArray readByteArray() {
        if (this.isContinuation()) {
            this.skipSpaces();
            return new ByteArray(this.buffer, this.index, this.size - this.index);
        }
        return (ByteArray)this.parseString(false, false);
    }

    public String readAtomString() {
        return (String)this.parseString(true, true);
    }

    private Object parseString(boolean parseAtoms, boolean returnString) {
        this.skipSpaces();
        byte b = this.buffer[this.index];
        if (b == 34) {
            ++this.index;
            int start = this.index;
            int copyto = this.index;
            while (this.index < this.size && (b = this.buffer[this.index]) != 34) {
                if (b == 92) {
                    ++this.index;
                }
                if (this.index != copyto) {
                    this.buffer[copyto] = this.buffer[this.index];
                }
                ++copyto;
                ++this.index;
            }
            if (this.index >= this.size) {
                return null;
            }
            ++this.index;
            if (returnString) {
                return ASCIIUtility.toString(this.buffer, start, copyto);
            }
            return new ByteArray(this.buffer, start, copyto - start);
        }
        if (b == 123) {
            int start = ++this.index;
            while (this.buffer[this.index] != 125) {
                ++this.index;
            }
            int count = 0;
            try {
                count = ASCIIUtility.parseInt(this.buffer, start, this.index);
            }
            catch (NumberFormatException nex) {
                return null;
            }
            start = this.index + 3;
            this.index = start + count;
            if (returnString) {
                return ASCIIUtility.toString(this.buffer, start, start + count);
            }
            return new ByteArray(this.buffer, start, count);
        }
        if (parseAtoms) {
            int start = this.index;
            String s = this.readDelimString(ASTRING_CHAR_DELIM);
            if (returnString) {
                return s;
            }
            return new ByteArray(this.buffer, start, this.index);
        }
        if (b == 78 || b == 110) {
            this.index += 3;
            return null;
        }
        return null;
    }

    public int getType() {
        return this.type;
    }

    public boolean isContinuation() {
        return (this.type & 3) == 1;
    }

    public boolean isTagged() {
        return (this.type & 3) == 2;
    }

    public boolean isUnTagged() {
        return (this.type & 3) == 3;
    }

    public boolean isOK() {
        return (this.type & 0x1C) == 4;
    }

    public boolean isNO() {
        return (this.type & 0x1C) == 8;
    }

    public boolean isBAD() {
        return (this.type & 0x1C) == 12;
    }

    public boolean isBYE() {
        return (this.type & 0x1C) == 16;
    }

    public boolean isSynthetic() {
        return (this.type & 0x20) == 32;
    }

    public String getTag() {
        return this.tag;
    }

    public String getRest() {
        this.skipSpaces();
        return ASCIIUtility.toString(this.buffer, this.index, this.size);
    }

    public Exception getException() {
        return this.ex;
    }

    public void reset() {
        this.index = this.pindex;
    }

    public String toString() {
        return ASCIIUtility.toString(this.buffer, 0, this.size);
    }
}

