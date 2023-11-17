/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  junit.framework.TestCase
 */
package org.jose4j.jwx;

import junit.framework.TestCase;
import org.jose4j.jwx.CompactSerializer;
import org.jose4j.lang.JoseException;

public class CompactSerializerTest
extends TestCase {
    public void testDeserialize1() throws JoseException {
        String cs = "one.two.three";
        String[] parts = CompactSerializer.deserialize(cs);
        int i = 0;
        CompactSerializerTest.assertEquals((String)"one", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"two", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"three", (String)parts[i++]);
        CompactSerializerTest.assertEquals((int)i, (int)parts.length);
    }

    public void testDeserialize2() throws JoseException {
        String cs = "one.two.three.four";
        String[] parts = CompactSerializer.deserialize(cs);
        int i = 0;
        CompactSerializerTest.assertEquals((String)"one", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"two", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"three", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"four", (String)parts[i++]);
        CompactSerializerTest.assertEquals((int)i, (int)parts.length);
    }

    public void testDeserialize3() throws JoseException {
        String cs = "one.two.";
        String[] parts = CompactSerializer.deserialize(cs);
        int i = 0;
        CompactSerializerTest.assertEquals((String)"one", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"two", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"", (String)parts[i++]);
        CompactSerializerTest.assertEquals((int)i, (int)parts.length);
    }

    public void testDeserialize4() throws JoseException {
        String cs = "one.two.three.";
        String[] parts = CompactSerializer.deserialize(cs);
        int i = 0;
        CompactSerializerTest.assertEquals((String)"one", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"two", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"three", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"", (String)parts[i++]);
        CompactSerializerTest.assertEquals((int)i, (int)parts.length);
    }

    public void testDeserialize5() throws JoseException {
        String cs = "one..three.four.five";
        String[] parts = CompactSerializer.deserialize(cs);
        int i = 0;
        CompactSerializerTest.assertEquals((String)"one", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"three", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"four", (String)parts[i++]);
        CompactSerializerTest.assertEquals((String)"five", (String)parts[i++]);
        CompactSerializerTest.assertEquals((int)i, (int)parts.length);
    }

    public void testSerialize1() throws JoseException {
        String cs = CompactSerializer.serialize("one", "two", "three");
        CompactSerializerTest.assertEquals((String)"one.two.three", (String)cs);
    }

    public void testSerialize2() throws JoseException {
        String cs = CompactSerializer.serialize("one", "two", "three", "four");
        CompactSerializerTest.assertEquals((String)"one.two.three.four", (String)cs);
    }

    public void testSerialize3() throws JoseException {
        String cs = CompactSerializer.serialize("one", "two", "three", null);
        CompactSerializerTest.assertEquals((String)"one.two.three.", (String)cs);
    }

    public void testSerialize4() throws JoseException {
        String cs = CompactSerializer.serialize("one", "two", "three", "");
        CompactSerializerTest.assertEquals((String)"one.two.three.", (String)cs);
    }

    public void testSerialize5() throws JoseException {
        String cs = CompactSerializer.serialize("one", null, "three", "four", "five");
        CompactSerializerTest.assertEquals((String)"one..three.four.five", (String)cs);
    }

    public void testSerialize6() throws JoseException {
        String cs = CompactSerializer.serialize("one", "", "three", "four", "five");
        CompactSerializerTest.assertEquals((String)"one..three.four.five", (String)cs);
    }
}

