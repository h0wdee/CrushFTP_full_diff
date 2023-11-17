/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.util.Strings
 */
package org.apache.log4j.spi;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.apache.logging.log4j.util.Strings;

public class ThrowableInformation
implements Serializable {
    static final long serialVersionUID = -4748765566864322735L;
    private transient Throwable throwable;
    private Method toStringList;

    public ThrowableInformation(Throwable throwable) {
        this.throwable = throwable;
        Method method = null;
        try {
            Class<?> throwables = Class.forName("org.apache.logging.log4j.core.util.Throwables");
            method = throwables.getMethod("toStringList", Throwable.class);
        }
        catch (ClassNotFoundException | NoSuchMethodException reflectiveOperationException) {
            // empty catch block
        }
        this.toStringList = method;
    }

    public Throwable getThrowable() {
        return this.throwable;
    }

    public synchronized String[] getThrowableStrRep() {
        if (this.toStringList != null && this.throwable != null) {
            try {
                List elements = (List)this.toStringList.invoke(null, this.throwable);
                if (elements != null) {
                    return elements.toArray(Strings.EMPTY_ARRAY);
                }
            }
            catch (IllegalAccessException | InvocationTargetException reflectiveOperationException) {
                // empty catch block
            }
        }
        return null;
    }
}

