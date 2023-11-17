/*
 * Decompiled with CFR 0.152.
 */
package com.didisoft.pgp.bc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {
    public static Object callPrivateStaticMethod(Class clazz, String string, Object[] objectArray) {
        Class[] classArray;
        if (objectArray.length == 0) {
            classArray = new Class[]{};
        } else {
            classArray = new Class[objectArray.length];
            for (int i = 0; i < objectArray.length; ++i) {
                classArray[i] = objectArray[i].getClass();
            }
        }
        Object object = null;
        try {
            Method method = clazz.getDeclaredMethod(string, classArray);
            method.setAccessible(true);
            object = method.invoke(null, objectArray);
        }
        catch (InvocationTargetException invocationTargetException) {
        }
        catch (IllegalAccessException illegalAccessException) {
        }
        catch (NoSuchMethodException noSuchMethodException) {
            throw new Error("No such method: " + string);
        }
        return object;
    }

    public static Object callPrivateMethod(Class clazz, String string, Object object, Object[] objectArray) {
        Class[] classArray;
        if (objectArray.length == 0) {
            classArray = new Class[]{};
        } else {
            classArray = new Class[objectArray.length];
            for (int i = 0; i < objectArray.length; ++i) {
                classArray[i] = objectArray[i].getClass();
            }
        }
        Object object2 = null;
        try {
            Method method = clazz.getDeclaredMethod(string, classArray);
            method.setAccessible(true);
            object2 = method.invoke(object, objectArray);
        }
        catch (InvocationTargetException invocationTargetException) {
        }
        catch (IllegalAccessException illegalAccessException) {
        }
        catch (NoSuchMethodException noSuchMethodException) {
            throw new Error("No such method: " + string);
        }
        return object2;
    }

    public static Object callPrivateConstrtuctor(Class clazz, Object[] objectArray, Class[] classArray) {
        Object var3_3 = null;
        try {
            Constructor constructor = clazz.getDeclaredConstructor(classArray);
            constructor.setAccessible(true);
            var3_3 = constructor.newInstance(objectArray);
        }
        catch (InvocationTargetException invocationTargetException) {
        }
        catch (IllegalAccessException illegalAccessException) {
        }
        catch (InstantiationException instantiationException) {
        }
        catch (NoSuchMethodException noSuchMethodException) {
            throw new Error("No such method: " + clazz.getName());
        }
        return var3_3;
    }

    public static void setPrivateFieldvalue(Object object, String string, Object object2) {
        try {
            Class<?> clazz = object.getClass();
            Field field = clazz.getDeclaredField(string);
            field.setAccessible(true);
            field.set(object, object2);
        }
        catch (NoSuchFieldException noSuchFieldException) {
            noSuchFieldException.printStackTrace();
        }
        catch (IllegalArgumentException illegalArgumentException) {
            illegalArgumentException.printStackTrace();
        }
        catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        }
    }

    public static Object getPrivateFieldvalue(Object object, String string) {
        try {
            Class<?> clazz = object.getClass();
            Field field = clazz.getDeclaredField(string);
            field.setAccessible(true);
            return field.get(object);
        }
        catch (NoSuchFieldException noSuchFieldException) {
            noSuchFieldException.printStackTrace();
        }
        catch (IllegalArgumentException illegalArgumentException) {
            illegalArgumentException.printStackTrace();
        }
        catch (IllegalAccessException illegalAccessException) {
            illegalAccessException.printStackTrace();
        }
        return null;
    }
}

