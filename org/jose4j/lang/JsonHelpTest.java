/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.hamcrest.CoreMatchers
 *  org.hamcrest.Matcher
 *  org.junit.Assert
 *  org.junit.Test
 */
package org.jose4j.lang;

import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.jose4j.json.JsonUtil;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.JsonHelp;
import org.junit.Assert;
import org.junit.Test;

public class JsonHelpTest {
    @Test
    public void longs() throws JoseException {
        Map<String, Object> map = JsonUtil.parseJson("{\"number\":1024}");
        Long nope = JsonHelp.getLong(map, "nope");
        Assert.assertThat((Object)nope, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.nullValue()));
        Long number = JsonHelp.getLong(map, "number");
        Assert.assertThat((Object)number, (Matcher)CoreMatchers.is((Matcher)CoreMatchers.equalTo((Object)1024L)));
    }
}

