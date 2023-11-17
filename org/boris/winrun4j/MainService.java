/*
 * Decompiled with CFR 0.152.
 */
package org.boris.winrun4j;

import java.lang.reflect.Method;
import org.boris.winrun4j.INI;
import org.boris.winrun4j.Service;
import org.boris.winrun4j.ServiceException;

public class MainService
implements Service {
    private String serviceClass = INI.getProperty("MainService:class");

    public int serviceMain(String[] args) throws ServiceException {
        try {
            Class<?> c = Class.forName(this.serviceClass);
            Method m = c.getMethod("main", String[].class);
            m.invoke(null, new Object[]{args});
        }
        catch (Exception e) {
            throw new ServiceException(e);
        }
        return 0;
    }

    public int serviceRequest(int control) throws ServiceException {
        switch (control) {
            case 1: 
            case 5: {
                System.exit(0);
                break;
            }
        }
        return 0;
    }
}

