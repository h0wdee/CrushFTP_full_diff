/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.config.plugins.PluginAttribute
 *  org.apache.logging.log4j.core.config.plugins.PluginFactory
 *  org.apache.logging.log4j.core.layout.AbstractStringLayout
 *  org.apache.logging.log4j.core.layout.ByteBufferDestination
 *  org.apache.logging.log4j.core.util.Transform
 *  org.apache.logging.log4j.util.ReadOnlyStringMap
 *  org.apache.logging.log4j.util.Strings
 */
package org.apache.log4j.layout;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.util.Transform;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="Log4j1XmlLayout", category="Core", elementType="layout", printObject=true)
public final class Log4j1XmlLayout
extends AbstractStringLayout {
    private final boolean locationInfo;
    private final boolean properties;

    @PluginFactory
    public static Log4j1XmlLayout createLayout(@PluginAttribute(value="locationInfo") boolean locationInfo, @PluginAttribute(value="properties") boolean properties) {
        return new Log4j1XmlLayout(locationInfo, properties);
    }

    private Log4j1XmlLayout(boolean locationInfo, boolean properties) {
        super(StandardCharsets.UTF_8);
        this.locationInfo = locationInfo;
        this.properties = properties;
    }

    public boolean isLocationInfo() {
        return this.locationInfo;
    }

    public boolean isProperties() {
        return this.properties;
    }

    public void encode(LogEvent event, ByteBufferDestination destination) {
        StringBuilder text = Log4j1XmlLayout.getStringBuilder();
        this.formatTo(event, text);
        this.getStringBuilderEncoder().encode((Object)text, destination);
    }

    public String toSerializable(LogEvent event) {
        StringBuilder text = Log4j1XmlLayout.getStringBuilder();
        this.formatTo(event, text);
        return text.toString();
    }

    private void formatTo(LogEvent event, StringBuilder buf) {
        ReadOnlyStringMap contextMap;
        StackTraceElement source;
        Throwable thrown;
        buf.append("<log4j:event logger=\"");
        buf.append(Transform.escapeHtmlTags((String)event.getLoggerName()));
        buf.append("\" timestamp=\"");
        buf.append(event.getTimeMillis());
        buf.append("\" level=\"");
        buf.append(Transform.escapeHtmlTags((String)String.valueOf(event.getLevel())));
        buf.append("\" thread=\"");
        buf.append(Transform.escapeHtmlTags((String)event.getThreadName()));
        buf.append("\">\r\n");
        buf.append("<log4j:message><![CDATA[");
        Transform.appendEscapingCData((StringBuilder)buf, (String)event.getMessage().getFormattedMessage());
        buf.append("]]></log4j:message>\r\n");
        List ndc = event.getContextStack().asList();
        if (!ndc.isEmpty()) {
            buf.append("<log4j:NDC><![CDATA[");
            Transform.appendEscapingCData((StringBuilder)buf, (String)Strings.join((Iterable)ndc, (char)' '));
            buf.append("]]></log4j:NDC>\r\n");
        }
        if ((thrown = event.getThrown()) != null) {
            buf.append("<log4j:throwable><![CDATA[");
            StringWriter w = new StringWriter();
            thrown.printStackTrace(new PrintWriter(w));
            Transform.appendEscapingCData((StringBuilder)buf, (String)w.toString());
            buf.append("]]></log4j:throwable>\r\n");
        }
        if (this.locationInfo && (source = event.getSource()) != null) {
            buf.append("<log4j:locationInfo class=\"");
            buf.append(Transform.escapeHtmlTags((String)source.getClassName()));
            buf.append("\" method=\"");
            buf.append(Transform.escapeHtmlTags((String)source.getMethodName()));
            buf.append("\" file=\"");
            buf.append(Transform.escapeHtmlTags((String)source.getFileName()));
            buf.append("\" line=\"");
            buf.append(source.getLineNumber());
            buf.append("\"/>\r\n");
        }
        if (this.properties && !(contextMap = event.getContextData()).isEmpty()) {
            buf.append("<log4j:properties>\r\n");
            contextMap.forEach((key, val) -> {
                if (val != null) {
                    buf.append("<log4j:data name=\"");
                    buf.append(Transform.escapeHtmlTags((String)key));
                    buf.append("\" value=\"");
                    buf.append(Transform.escapeHtmlTags((String)Objects.toString(val, null)));
                    buf.append("\"/>\r\n");
                }
            });
            buf.append("</log4j:properties>\r\n");
        }
        buf.append("</log4j:event>\r\n\r\n");
    }
}

