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
    public static String readFile(String folderName, String fileName) {
        String s;

        try {
            File file = new File(folderName + fileName);
            s = FileUtils.readFileToString(file, Charset.forName("UTF-8"));
        }
        catch(Exception e) { // Catch everything!
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

    public static File writeFile(String folderName, String fileName, String s) {
        // Returns a file with the String written to it.
        File file;

        try {
            file = new File(folderName + fileName);
            FileUtils.writeStringToFile(file, s, Charset.forName("UTF-8"));
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            file = null;
        }

        return file;
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

    public static ArrayList<File> getFiles(String folderName) {
        ArrayList<File> fileArrayList = new ArrayList<>();

        try {
            File folder = new File(folderName);

            File[] files = folder.listFiles();
            if(files != null) {
                for(File file : files) {
                    if(file.isFile()) {
                        fileArrayList.add(file);
                    }
                }
            }
        }
        catch(Exception ignored) {
            fileArrayList = null;
        }

        return fileArrayList;
    }

    public static ArrayList<File> getFolders(String folderName) {
        ArrayList<File> fileArrayList = new ArrayList<>();

        try {
            File folder = new File(folderName);

            File[] files = folder.listFiles();
            if(files != null) {
                for(File file : files) {
                    if(!file.isFile()) {
                        fileArrayList.add(file);
                    }
                }
            }
        }
        catch(Exception ignored) {
            fileArrayList = null;
        }

        return fileArrayList;
    }

    public static boolean exists(String folderName, String fileName) {
        // This takes into account the case sensitivity of the file system.
        return new File(folderName + fileName).exists();
    }

    public static boolean isFile(String folderName, String fileName) {
        // This takes into account the case sensitivity of the file system.
        return new File(folderName + fileName).isFile();
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
