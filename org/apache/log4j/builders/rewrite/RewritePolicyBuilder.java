/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.builders.rewrite;

import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.rewrite.RewritePolicy;
import org.apache.log4j.xml.XmlConfiguration;
import org.w3c.dom.Element;

public interface RewritePolicyBuilder {
    public RewritePolicy parseRewritePolicy(Element var1, XmlConfiguration var2);

    public RewritePolicy parseRewritePolicy(PropertiesConfiguration var1);
}

