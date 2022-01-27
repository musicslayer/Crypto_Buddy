package com.musicslayer.cryptobuddy.util;

import android.content.Context;

import com.musicslayer.cryptobuddy.app.App;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class FileUtil {
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

            StreamUtil.safeClose(file);
        }
        catch(IOException e) {
            ThrowableUtil.processThrowable(e);
            StreamUtil.safeClose(file);
            throw new IllegalStateException(e);
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

            StreamUtil.safeClose(file);
        }
        catch(IOException e) {
            ThrowableUtil.processThrowable(e);
            StreamUtil.safeClose(file);
            throw new IllegalStateException(e);
        }

        return stringArrayList;
    }

    public static File writeFile(Context context, String s) {
        // Returns a tempfile with the String written to it.
        File file;
        try {
            file = File.createTempFile("CryptoBuddy_TextFile_", ".txt", new File(App.cacheDir));
            FileUtils.writeStringToFile(file, s, Charset.forName("UTF-8"));
        }
        catch(Exception e) { // Catch everything!
            ThrowableUtil.processThrowable(e);

            // This class may be used by CrashReporterDialog, so just return null instead of throwing something.
            file = null;
        }

        return file;
    }

    public static File downloadFile(String urlString) {
        // Downloads the file at the url to a tempfile and then returns it.
        File file;
        try {
            file = File.createTempFile("CryptoBuddy_ZipFile_", ".zip", new File(App.cacheDir));
            boolean result = WebUtil.download(urlString, file);

            // If anything went wrong, we don't want the file.
            if(!result) {
                file = null;
            }
        }
        catch(Exception ignored) {
            file = null;
        }

        return file;
    }
}
