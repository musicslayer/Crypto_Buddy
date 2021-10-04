package com.musicslayer.cryptobuddy.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import shadow.org.apache.commons.io.FileUtils;

public class File {
    public static String readFile(Context context, int id) {
        return readFile(new BufferedReader(new InputStreamReader(context.getResources().openRawResource(id))));
    }

    public static String readFile(BufferedReader file) {
        StringBuilder stringBuilder = new StringBuilder();

        try
        {
            String string;
            while((string = file.readLine()) != null) {
                stringBuilder.append(string).append("\n");
            }

            file.close();
        }
        catch(IOException e) {
            ThrowableLogger.processThrowable(e);
        }

        return stringBuilder.toString();
    }

    public static ArrayList<String> readFileIntoLines(Context context, int id) {
        return readFileIntoLines(new BufferedReader(new InputStreamReader(context.getResources().openRawResource(id))));
    }

    public static ArrayList<String> readFileIntoLines(BufferedReader file) {
        ArrayList<String> stringArrayList = new ArrayList<>();

        try
        {
            String string;
            while((string = file.readLine()) != null) {
                stringArrayList.add(string);
            }

            file.close();
        }
        catch(IOException e) {
            ThrowableLogger.processThrowable(e);
        }

        return stringArrayList;
    }

    public static java.io.File writeFile(Context context, String s) {
        // Returns a tempfile with the String written to it.
        java.io.File file;
        try {
            file = java.io.File.createTempFile("CrashLog", null, context.getCacheDir());
            FileUtils.writeStringToFile(file, s, Charset.forName("UTF-8"));
        }
        catch(Exception e) {
            ThrowableLogger.processThrowable(e);
            file = null;
        }

        return file;
    }
}
