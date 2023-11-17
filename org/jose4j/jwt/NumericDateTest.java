/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.jwt;

import org.jose4j.jwt.NumericDate;
import org.junit.Assert;
import org.junit.Test;

public class NumericDateTest {
    @Test
    public void testComps1() {
        NumericDate one = NumericDate.fromSeconds(1350647028L);
        NumericDate two = NumericDate.fromSeconds(1350647029L);
        Assert.assertTrue((boolean)one.isBefore(two));
        Assert.assertFalse((boolean)two.isBefore(one));
        Assert.assertFalse((boolean)one.equals(two));
        Assert.assertFalse((boolean)one.isAfter(two));
        Assert.assertFalse((boolean)one.isOnOrAfter(two));
        Assert.assertTrue((boolean)two.isAfter(one));
        Assert.assertTrue((boolean)two.isOnOrAfter(one));
    }

    @Test
    public void testComps2() {
        NumericDate one = NumericDate.fromSeconds(1350647028L);
        NumericDate two = NumericDate.fromSeconds(1350647028L);
        Assert.assertFalse((boolean)one.isBefore(two));
        Assert.assertFalse((boolean)two.isBefore(one));
        Assert.assertTrue((boolean)one.isOnOrAfter(two));
        Assert.assertTrue((boolean)two.isOnOrAfter(one));
        Assert.assertFalse((boolean)one.isAfter(two));
        Assert.assertFalse((boolean)two.isAfter(one));
        Assert.assertTrue((boolean)one.equals(two));
    }

    @Test
    public void testEquals() {
        NumericDate date1;
        NumericDate date2 = date1 = NumericDate.now();
        Assert.assertTrue((boolean)date1.equals(date2));
        Assert.assertTrue((boolean)date2.equals(date1));
        date2 = NumericDate.fromSeconds(date1.getValue());
        Assert.assertTrue((boolean)date1.equals(date2));
        Assert.assertTrue((boolean)date2.equals(date1));
        date2.addSeconds(100L);
        Assert.assertFalse((boolean)date1.equals(date2));
        Assert.assertFalse((boolean)date2.equals(date1));
        date1.addSeconds(100L);
        Assert.assertTrue((boolean)date1.equals(date2));
        Assert.assertTrue((boolean)date2.equals(date1));
    }

    @Test
    public void testAddSecs() {
        NumericDate date = NumericDate.fromMilliseconds(0L);
        int seconds = 100;
        date.addSeconds(seconds);
        Assert.assertEquals((long)100L, (long)date.getValue());
        date = NumericDate.fromMilliseconds(0L);
        long secondsLong = 100L;
        date.addSeconds(secondsLong);
        Assert.assertEquals((long)100L, (long)date.getValue());
        date = NumericDate.fromMilliseconds(0L);
        int secondsInt = 100;
        date.addSeconds(secondsInt);
        Assert.assertEquals((long)100L, (long)date.getValue());
        date = NumericDate.fromMilliseconds(0L);
        date.addSeconds(100L);
        Assert.assertEquals((long)100L, (long)date.getValue());
    }
}

