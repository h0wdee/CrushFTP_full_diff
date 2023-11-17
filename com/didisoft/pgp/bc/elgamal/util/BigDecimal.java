/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc.elgamal.util;

import com.didisoft.pgp.bc.elgamal.util.BigInteger;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

public class BigDecimal
extends Number
implements Comparable {
    private BigInteger intVal;
    private int scale = 0;
    private static final long serialVersionUID = 6108874887143696463L;
    public static final int ROUND_UP = 0;
    public static final int ROUND_DOWN = 1;
    public static final int ROUND_CEILING = 2;
    public static final int ROUND_FLOOR = 3;
    public static final int ROUND_HALF_UP = 4;
    public static final int ROUND_HALF_DOWN = 5;
    public static final int ROUND_HALF_EVEN = 6;
    public static final int ROUND_UNNECESSARY = 7;

    public BigDecimal(String string) {
        int n;
        if (string.length() == 0) {
            throw new NumberFormatException();
        }
        if (string.charAt(0) == '+' && ((string = string.substring(1)).length() == 0 || string.charAt(0) == '-')) {
            throw new NumberFormatException();
        }
        int n2 = 0;
        int n3 = string.indexOf(101);
        if (n3 == -1) {
            n3 = string.indexOf(69);
        }
        if (n3 != -1) {
            String string2 = string.substring(n3 + 1);
            if (string2.length() == 0) {
                throw new NumberFormatException();
            }
            if (string2.charAt(0) == '+' && ((string2 = string2.substring(1)).length() == 0 || string2.charAt(0) == '-')) {
                throw new NumberFormatException();
            }
            n2 = Integer.parseInt(string2);
            if (n3 == 0) {
                throw new NumberFormatException();
            }
            string = string.substring(0, n3);
        }
        if ((n = string.indexOf(46)) == -1) {
            this.intVal = new BigInteger(string);
        } else if (n == string.length() - 1) {
            this.intVal = new BigInteger(string.substring(0, string.length() - 1));
        } else {
            if (string.charAt(n + 1) == '-') {
                throw new NumberFormatException();
            }
            char[] cArray = new char[string.length() - 1];
            string.getChars(0, n, cArray, 0);
            string.getChars(n + 1, string.length(), cArray, n);
            this.scale = string.length() - n - 1;
            this.intVal = new BigInteger(cArray);
        }
        long l = (long)this.scale - (long)n2;
        if (l > Integer.MAX_VALUE) {
            throw new NumberFormatException("Final scale out of range");
        }
        this.scale = (int)l;
        if (this.scale < 0) {
            this.intVal = BigDecimal.timesTenToThe(this.intVal, -this.scale);
            this.scale = 0;
        }
    }

    public BigDecimal(double d) {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            throw new NumberFormatException("Infinite or NaN");
        }
        long l = Double.doubleToLongBits(d);
        int n = l >> 63 == 0L ? 1 : -1;
        int n2 = (int)(l >> 52 & 0x7FFL);
        long l2 = n2 == 0 ? (l & 0xFFFFFFFFFFFFFL) << 1 : l & 0xFFFFFFFFFFFFFL | 0x10000000000000L;
        n2 -= 1075;
        if (l2 == 0L) {
            this.intVal = BigInteger.ZERO;
            return;
        }
        while ((l2 & 1L) == 0L) {
            l2 >>= 1;
            ++n2;
        }
        this.intVal = BigInteger.valueOf((long)n * l2);
        if (n2 < 0) {
            this.intVal = this.intVal.multiply(BigInteger.valueOf(5L).pow(-n2));
            this.scale = -n2;
        } else if (n2 > 0) {
            this.intVal = this.intVal.multiply(BigInteger.valueOf(2L).pow(n2));
        }
    }

    public BigDecimal(BigInteger bigInteger) {
        this.intVal = bigInteger;
    }

    public BigDecimal(BigInteger bigInteger, int n) {
        if (n < 0) {
            throw new NumberFormatException("Negative scale");
        }
        this.intVal = bigInteger;
        this.scale = n;
    }

    public static BigDecimal valueOf(long l, int n) {
        return new BigDecimal(BigInteger.valueOf(l), n);
    }

    public static BigDecimal valueOf(long l) {
        return BigDecimal.valueOf(l, 0);
    }

    public BigDecimal add(BigDecimal bigDecimal) {
        BigDecimal[] bigDecimalArray = new BigDecimal[]{this, bigDecimal};
        BigDecimal.matchScale(bigDecimalArray);
        return new BigDecimal(bigDecimalArray[0].intVal.add(bigDecimalArray[1].intVal), bigDecimalArray[0].scale);
    }

    public BigDecimal subtract(BigDecimal bigDecimal) {
        BigDecimal[] bigDecimalArray = new BigDecimal[]{this, bigDecimal};
        BigDecimal.matchScale(bigDecimalArray);
        return new BigDecimal(bigDecimalArray[0].intVal.subtract(bigDecimalArray[1].intVal), bigDecimalArray[0].scale);
    }

    public BigDecimal multiply(BigDecimal bigDecimal) {
        return new BigDecimal(this.intVal.multiply(bigDecimal.intVal), this.scale + bigDecimal.scale);
    }

    public BigDecimal divide(BigDecimal bigDecimal, int n, int n2) {
        int n3;
        BigDecimal bigDecimal2;
        BigDecimal bigDecimal3;
        if (n < 0) {
            throw new ArithmeticException("Negative scale");
        }
        if (n2 < 0 || n2 > 7) {
            throw new IllegalArgumentException("Invalid rounding mode");
        }
        if (n + bigDecimal.scale >= this.scale) {
            bigDecimal3 = this.setScale(n + bigDecimal.scale);
            bigDecimal2 = bigDecimal;
        } else {
            bigDecimal3 = this;
            bigDecimal2 = bigDecimal.setScale(this.scale - n);
        }
        BigInteger[] bigIntegerArray = bigDecimal3.intVal.divideAndRemainder(bigDecimal2.intVal);
        BigInteger bigInteger = bigIntegerArray[0];
        BigInteger bigInteger2 = bigIntegerArray[1];
        if (bigInteger2.signum() == 0) {
            return new BigDecimal(bigInteger, n);
        }
        if (n2 == 7) {
            throw new ArithmeticException("Rounding necessary");
        }
        int n4 = bigDecimal3.signum() * bigDecimal2.signum();
        boolean bl = n2 == 0 ? true : (n2 == 1 ? false : (n2 == 2 ? n4 > 0 : (n2 == 3 ? n4 < 0 : ((n3 = bigInteger2.abs().multiply(BigInteger.valueOf(2L)).compareTo(bigDecimal2.intVal.abs())) < 0 ? false : (n3 > 0 ? true : (n2 == 4 ? true : (n2 == 5 ? false : bigInteger.testBit(0))))))));
        return bl ? new BigDecimal(bigInteger.add(BigInteger.valueOf(n4)), n) : new BigDecimal(bigInteger, n);
    }

    public BigDecimal divide(BigDecimal bigDecimal, int n) {
        return this.divide(bigDecimal, this.scale, n);
    }

    public BigDecimal abs() {
        return this.signum() < 0 ? this.negate() : this;
    }

    public BigDecimal negate() {
        return new BigDecimal(this.intVal.negate(), this.scale);
    }

    public int signum() {
        return this.intVal.signum();
    }

    public int scale() {
        return this.scale;
    }

    public BigInteger unscaledValue() {
        return this.intVal;
    }

    public BigDecimal setScale(int n, int n2) {
        if (n < 0) {
            throw new ArithmeticException("Negative scale");
        }
        if (n2 < 0 || n2 > 7) {
            throw new IllegalArgumentException("Invalid rounding mode");
        }
        if (n == this.scale) {
            return this;
        }
        if (n > this.scale) {
            return new BigDecimal(BigDecimal.timesTenToThe(this.intVal, n - this.scale), n);
        }
        return this.divide(BigDecimal.valueOf(1L), n, n2);
    }

    public BigDecimal setScale(int n) {
        return this.setScale(n, 7);
    }

    public BigDecimal movePointLeft(int n) {
        return n >= 0 ? new BigDecimal(this.intVal, this.scale + n) : this.movePointRight(-n);
    }

    public BigDecimal movePointRight(int n) {
        return this.scale >= n ? new BigDecimal(this.intVal, this.scale - n) : new BigDecimal(BigDecimal.timesTenToThe(this.intVal, n - this.scale), 0);
    }

    public int compareTo(BigDecimal bigDecimal) {
        int n = this.signum() - bigDecimal.signum();
        if (n != 0) {
            return n > 0 ? 1 : -1;
        }
        BigDecimal[] bigDecimalArray = new BigDecimal[]{this, bigDecimal};
        BigDecimal.matchScale(bigDecimalArray);
        return bigDecimalArray[0].intVal.compareTo(bigDecimalArray[1].intVal);
    }

    public int compareTo(Object object) {
        return this.compareTo((BigDecimal)object);
    }

    public boolean equals(Object object) {
        if (!(object instanceof BigDecimal)) {
            return false;
        }
        BigDecimal bigDecimal = (BigDecimal)object;
        return this.scale == bigDecimal.scale && this.intVal.equals(bigDecimal.intVal);
    }

    public BigDecimal min(BigDecimal bigDecimal) {
        return this.compareTo(bigDecimal) < 0 ? this : bigDecimal;
    }

    public BigDecimal max(BigDecimal bigDecimal) {
        return this.compareTo(bigDecimal) > 0 ? this : bigDecimal;
    }

    public int hashCode() {
        return 31 * this.intVal.hashCode() + this.scale;
    }

    private String roundup(String string) {
        int n;
        int n2;
        char[] cArray = string.toCharArray();
        char c = cArray[n2 = (n = cArray.length) - 1];
        if (c == '9') {
            while (c == '9' && n2 > 0) {
                cArray[n2] = 48;
                c = cArray[--n2];
            }
            if (c == '9') {
                cArray[0] = 48;
                return "1" + String.valueOf(cArray);
            }
        }
        cArray[n2] = (char)(c + '\u0001');
        return String.valueOf(cArray);
    }

    public String toString() {
        if (this.scale == 0) {
            return this.intVal.toString();
        }
        return this.getValueString(this.signum(), this.intVal.abs().toString(), this.scale);
    }

    public BigInteger toBigInteger() {
        return this.scale == 0 ? this.intVal : this.intVal.divide(BigInteger.valueOf(10L).pow(this.scale));
    }

    public int intValue() {
        return this.toBigInteger().intValue();
    }

    public long longValue() {
        return this.toBigInteger().longValue();
    }

    public float floatValue() {
        return Float.valueOf(this.toString()).floatValue();
    }

    public double doubleValue() {
        return Double.valueOf(this.toString());
    }

    private String getValueString(int n, String string, int n2) {
        StringBuffer stringBuffer;
        int n3 = string.length() - n2;
        if (n3 == 0) {
            return (n < 0 ? "-0." : "0.") + string;
        }
        if (n3 > 0) {
            stringBuffer = new StringBuffer(string);
            stringBuffer.insert(n3, '.');
            if (n < 0) {
                stringBuffer.insert(0, '-');
            }
        } else {
            stringBuffer = new StringBuffer(3 - n3 + string.length());
            stringBuffer.append(n < 0 ? "-0." : "0.");
            for (int i = 0; i < -n3; ++i) {
                stringBuffer.append('0');
            }
            stringBuffer.append(string);
        }
        return stringBuffer.toString();
    }

    private static BigInteger timesTenToThe(BigInteger bigInteger, int n) {
        return bigInteger.multiply(BigInteger.valueOf(10L).pow(n));
    }

    private static void matchScale(BigDecimal[] bigDecimalArray) {
        if (bigDecimalArray[0].scale < bigDecimalArray[1].scale) {
            bigDecimalArray[0] = bigDecimalArray[0].setScale(bigDecimalArray[1].scale);
        } else if (bigDecimalArray[1].scale < bigDecimalArray[0].scale) {
            bigDecimalArray[1] = bigDecimalArray[1].setScale(bigDecimalArray[0].scale);
        }
    }

    private synchronized void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        if (this.scale < 0) {
            throw new StreamCorruptedException("BigDecimal: Negative scale");
        }
    }
}

