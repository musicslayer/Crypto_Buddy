package com.musicslayer.cryptobuddy.util;

import android.app.Activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class Reflect {
    public static <T, U> U callStaticMethodOrError(Class<T> clazz, String staticMethodName, Object... args) throws Exception {
        // Do not catch errors.
        Object[] argArray = new Object[args.length];
        Class<?>[] argClassArray = new Class<?>[args.length];

        for(int i = 0; i < args.length; i++) {
            argArray[i] = args[i];
            argClassArray[i] = args[i].getClass();
        }

        Method m = clazz.getMethod(staticMethodName, argClassArray);
        return (U)m.invoke(null, argArray);
    }

    public static <T> T constructClassInstanceFromName(String className) {
        T object = null;

        try {
            // Assume no-arg constructor exists.
            Class<T> clazz = (Class<T>)Class.forName(className);
            object = clazz.getConstructor().newInstance();
        }
        catch(ClassNotFoundException e) {
            ExceptionLogger.processException(e);
        }
        catch(IllegalAccessException e) {
            ExceptionLogger.processException(e);
        }
        catch(InstantiationException e) {
            ExceptionLogger.processException(e);
        }
        catch(InvocationTargetException e) {
            ExceptionLogger.processException(e);
        }
        catch(NoSuchMethodException e) {
            ExceptionLogger.processException(e);
        }

        return object;
    }

    public static <T> T constructDialogInstance(Class<T> clazz, Activity activity, Object... args) {
        T object = null;

        try {
            Object[] argArray = new Object[args.length + 1];
            Class<?>[] argClassArray = new Class<?>[args.length + 1];

            // All our Dialog classes have the first argument class as "Activity".
            argArray[0] = activity;
            argClassArray[0] = Activity.class;

            for(int i = 0; i < args.length; i++) {
                argArray[i + 1] = args[i];
                argClassArray[i + 1] = args[i].getClass();
            }

            object = clazz.getConstructor(argClassArray).newInstance(argArray);
        }
        catch(IllegalAccessException e) {
            ExceptionLogger.processException(e);
        }
        catch(InstantiationException e) {
            ExceptionLogger.processException(e);
        }
        catch(InvocationTargetException e) {
            ExceptionLogger.processException(e);
        }
        catch(NoSuchMethodException e) {
            ExceptionLogger.processException(e);
        }

        return object;
    }
}
