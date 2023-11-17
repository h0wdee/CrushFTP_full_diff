/*
 * Decompiled with CFR 0.152.
 */
package org.apache.log4j.or;

import java.util.Map;
import org.apache.log4j.or.ObjectRenderer;

public interface RendererSupport {
    public Map<Class<?>, ObjectRenderer> getRendererMap();
}

