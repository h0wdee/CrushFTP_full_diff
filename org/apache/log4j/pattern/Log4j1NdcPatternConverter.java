/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.core.LogEvent
 *  org.apache.logging.log4j.core.config.plugins.Plugin
 *  org.apache.logging.log4j.core.pattern.ConverterKeys
 *  org.apache.logging.log4j.core.pattern.LogEventPatternConverter
 *  org.apache.logging.log4j.util.Strings
 */
package org.apache.log4j.pattern;

import java.util.List;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="Log4j1NdcPatternConverter", category="Converter")
@ConverterKeys(value={"ndc"})
public final class Log4j1NdcPatternConverter
extends LogEventPatternConverter {
    private static final Log4j1NdcPatternConverter INSTANCE = new Log4j1NdcPatternConverter();

    private Log4j1NdcPatternConverter() {
        super("Log4j1NDC", "ndc");
    }

    public static Log4j1NdcPatternConverter newInstance(String[] options) {
        return INSTANCE;
    }

    public void format(LogEvent event, StringBuilder toAppendTo) {
        List ndc = event.getContextStack().asList();
        toAppendTo.append(Strings.join((Iterable)ndc, (char)' '));
    }
}

