/*
 * Decompiled with CFR 0.152.
 */
package org.jose4j.json.internal.json_simple.parser;

import java.util.List;
import java.util.Map;

public interface ContainerFactory {
    public Map createObjectContainer();

    public List createArrayContainer();
}

