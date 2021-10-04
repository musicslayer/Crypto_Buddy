package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unchecked")
public class Reflect {
    public static <T, U> U callStaticMethodOrError(Class<T> clazz, String staticMethodName, Object... args) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
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

    public static <T> T constructCrashDialogInstance(Class<T> clazz, Activity activity, Exception exception) {
        // Hardcode the Exception arguments to deal with subclasses.
        T object = null;

        try {
            object = clazz.getConstructor(Activity.class, Exception.class).newInstance(activity, exception);
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

    public static <T> void callResetAllData(Class<T> clazz, Context context) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        // Return type is void, and the only argument is the Context object.
        Method m = clazz.getMethod("resetAllData", Context.class);
        m.invoke(null, context);
    }
}
