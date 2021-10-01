package com.musicslayer.cryptobuddy.util;

import android.app.Activity;

public class Reflect {
    // Assume no-arg constructor exists.
    public static <T> T constructSubclassInstanceFromName(String subclassName) {
        T object = null;

        try {
            Class<T> clazz = (Class<T>)Class.forName(subclassName);
            object = clazz.getConstructor().newInstance();
        }
        catch(Exception e) {
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
        catch(Exception e) {
            ExceptionLogger.processException(e);
        }

        return object;
    }
}
