/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.pattern.ConverterKeys
 *  org.apache.logging.log4j.core.pattern.LogEventPatternConverter
 *  org.apache.logging.log4j.util.TriConsumer
 */
package org.apache.log4j.pattern;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.TriConsumer;

@Plugin(name="Log4j1MdcPatternConverter", category="Converter")
@ConverterKeys(value={"properties"})
public final class Log4j1MdcPatternConverter
extends LogEventPatternConverter {
    private final String key;
    private static TriConsumer<String, Object, StringBuilder> APPEND_EACH = (key, value, toAppendTo) -> toAppendTo.append('{').append((String)key).append(',').append(value).append('}');

    private Log4j1MdcPatternConverter(String[] options) {
        super(options != null && options.length > 0 ? "Log4j1MDC{" + options[0] + '}' : "Log4j1MDC", "property");
        this.key = options != null && options.length > 0 ? options[0] : null;
    }

    public static Log4j1MdcPatternConverter newInstance(String[] options) {
        return new Log4j1MdcPatternConverter(options);
    }

    public void format(LogEvent event, StringBuilder toAppendTo) {
        if (this.key == null) {
            toAppendTo.append('{');
            event.getContextData().forEach(APPEND_EACH, (Object)toAppendTo);
            toAppendTo.append('}');
        } else {
            Object val = event.getContextData().getValue(this.key);
            if (val != null) {
                toAppendTo.append(val);
            }
        }
    }
}

