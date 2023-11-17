/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.builders.layout;

import org.apache.log4j.Layout;
import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.xml.XmlConfiguration;
import org.w3c.dom.Element;

public interface LayoutBuilder {
    public Layout parseLayout(Element var1, XmlConfiguration var2);

    public Layout parseLayout(PropertiesConfiguration var1);
}

