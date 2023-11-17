/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.builders.appender;

import java.util.Properties;
import org.apache.log4j.Appender;
import org.apache.log4j.config.PropertiesConfiguration;
import org.apache.log4j.xml.XmlConfiguration;
import org.w3c.dom.Element;

public interface AppenderBuilder {
    public Appender parseAppender(Element var1, XmlConfiguration var2);

    public Appender parseAppender(String var1, String var2, String var3, String var4, Properties var5, PropertiesConfiguration var6);
}

