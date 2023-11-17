/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.builders.filter;

import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.xml.XmlConfiguration;
import org.w3c.dom.Element;

public interface FilterBuilder {
    public Filter parseFilter(Element var1, XmlConfiguration var2);

    public Filter parseFilter(PropertiesConfiguration var1);
}

