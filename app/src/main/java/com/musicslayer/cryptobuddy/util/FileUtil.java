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
    public static String readFile(File folder, String name) {
        String s;

        try {
            File file = new File(folder.getAbsolutePath() + File.separatorChar + name);
            s = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            s = null;
        }

        return s;
    }

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

            StreamUtil.safeFlushAndClose(file);
        }
        catch(IOException e) {
            ThrowableUtil.processThrowable(e);
            StreamUtil.safeFlushAndClose(file);
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

            StreamUtil.safeFlushAndClose(file);
        }
        catch(IOException e) {
            ThrowableUtil.processThrowable(e);
            StreamUtil.safeFlushAndClose(file);
            throw new IllegalStateException(e);
        }

        return stringArrayList;
    }

    public static boolean writeFile(File folder, String name, String s) {
        // Writes string to this file, and returns whether it was a success.
        try {
            File file = new File(folder.getAbsolutePath() + File.separatorChar + name);
            FileUtils.writeStringToFile(file, s, Charset.forName("UTF-8"));
            return true;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return false;
        }
    }

    public static File writeTempFile(String s) {
        // Returns a tempfile with the String written to it.
        File file;
        try {
            file = File.createTempFile("CryptoBuddy_TextFile_", ".txt", new File(App.cacheDir));
            FileUtils.writeStringToFile(file, s, Charset.forName("UTF-8"));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            file = null;
        }

        return file;
    }

    public static File downloadFile(String fileExtension, String urlString) {
        // Downloads the file at the url to a tempfile and then returns it.
        File file;
        try {
            file = File.createTempFile("CryptoBuddy_DownloadedFile_", fileExtension, new File(App.cacheDir));
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
