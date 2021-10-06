package com.musicslayer.cryptobuddy.util;

import android.app.Activity;
import android.content.Context;

import java.lang.reflect.Method;

// Many reflection calls need unsavory casts, so just allow them all.
@SuppressWarnings("unchecked")

public class ReflectUtil {
    public static <T, U> U callStaticMethod(Class<T> clazz, String staticMethodName, Object... args) {
        try {
            Object[] argArray = new Object[args.length];
            Class<?>[] argClassArray = new Class<?>[args.length];

            for(int i = 0; i < args.length; i++) {
                argArray[i] = args[i];
                argClassArray[i] = args[i].getClass();
            }

            Method m = clazz.getMethod(staticMethodName, argClassArray);
            return (U)m.invoke(null, argArray);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> T constructClassInstanceFromName(String className) {
        // Assume no-arg constructor exists.
        try {
            Class<T> clazz = (Class<T>)Class.forName(className);
            return clazz.getConstructor().newInstance();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> T constructDialogInstance(Class<T> clazz, Activity activity, Object... args) {
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

            return clazz.getConstructor(argClassArray).newInstance(argArray);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> void callResetAllData(Class<T> clazz, Context context) {
        // Return type is void, and the only argument is the Context object.
        try {
            Method m = clazz.getMethod("resetAllData", Context.class);
            m.invoke(null, context);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }
}
