/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.logging.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger;

public final class Log4jFactory
extends LogFactory {
    private Hashtable attributes = new Hashtable();
    private Hashtable instances = new Hashtable();

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public String[] getAttributeNames() {
        Vector<String> names = new Vector<String>();
        Enumeration keys = this.attributes.keys();
        while (keys.hasMoreElements()) {
            names.addElement((String)keys.nextElement());
        }
        String[] results = new String[names.size()];
        int i = 0;
        while (i < results.length) {
            results[i] = (String)names.elementAt(i);
            ++i;
        }
        return results;
    }

    public Log getInstance(Class clazz) throws LogConfigurationException {
        Log instance = (Log)this.instances.get(clazz);
        if (instance != null) {
            return instance;
        }
        instance = new Log4JLogger(Logger.getLogger(clazz));
        this.instances.put(clazz, instance);
        return instance;
    }

    public Log getInstance(String name) throws LogConfigurationException {
        Log instance = (Log)this.instances.get(name);
        if (instance != null) {
            return instance;
        }
        instance = new Log4JLogger(Logger.getLogger(name));
        this.instances.put(name, instance);
        return instance;
    }

    public void release() {
        this.instances.clear();
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
        if (value == null) {
            this.attributes.remove(name);
        } else {
            this.attributes.put(name, value);
        }
    }
}

