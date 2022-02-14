package com.musicslayer.cryptobuddy.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.musicslayer.cryptobuddy.app.App;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

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

    public static File writeTempFile(Context context, String s) {
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

    public static String getDocumentsFolder() {
        if(Build.VERSION.SDK_INT < 29) {
            return "Documents";
        }
        else {
            return Environment.DIRECTORY_DOCUMENTS;
        }
    }

    public static String getFullExternalFolderPath(Context context, String subfolder) {
        // Used only to show the folder for display purposes. Should not be used with APIs to read/write files.
        return Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separatorChar + getDocumentsFolder() +
            File.separatorChar + context.getPackageName() +
            File.separatorChar + subfolder +
            File.separatorChar;
    }

    public static String getExternalFolderPath(Context context, String subfolder) {
        String externalFolderBase = getDocumentsFolder() + File.separatorChar + context.getPackageName() + File.separatorChar + subfolder + File.separatorChar;

        // New APIs only need relative path, but old APIs need entire path.
        if(Build.VERSION.SDK_INT < 29) {
            externalFolderBase = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + externalFolderBase;
        }

        return externalFolderBase;
    }

    public static String readExternalFile(Context context, String subfolder, String name) {
        // Reads data from an external file into a String.
        String s;
        if(Build.VERSION.SDK_INT < 29) {
            // Older APIs can write to the file like normal.
            try {
                File externalFile = new File(getExternalFolderPath(context, subfolder) + name);
                s = FileUtils.readFileToString(externalFile, Charset.forName("UTF-8"));
            }
            catch(Exception e) { // Catch everything!
                ThrowableUtil.processThrowable(e);
                s = null;
            }
        }
        else {
            // Newer APIs must use the MediaStore to read from the file.
            try {
                // TODO Find more precise way to do this?
                File externalFile = null;

                ArrayList<File> fileArrayList = getExternalFiles(context, subfolder);
                for(File file : fileArrayList) {
                    if(name.equals(file.getName())) {
                        externalFile = file;
                        break;
                    }
                }

                s = FileUtils.readFileToString(externalFile, Charset.forName("UTF-8"));
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                s = null;
            }
        }

        return s;
    }

    public static File writeExternalFile(Context context, String subfolder, String name, String s) {
        // Returns an external file with the String written to it.
        File externalFile;

        if(Build.VERSION.SDK_INT < 29) {
            // Older APIs can write to the file like normal.
            try {
                externalFile = new File(getExternalFolderPath(context, subfolder) + name);
                FileUtils.writeStringToFile(externalFile, s, Charset.forName("UTF-8"));
            }
            catch(Exception e) { // Catch everything!
                ThrowableUtil.processThrowable(e);
                externalFile = null;
            }
        }
        else {
            // Newer APIs must use the MediaStore to write to the file.
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "*/*");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, getExternalFolderPath(context, subfolder));

                Uri fileUri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), contentValues);

                // Do this so we can write to the file later.
                context.getContentResolver().openOutputStream(fileUri).close();

                // Get the absolute file path (there should only be one item found here).
                String externalFilePath;
                Cursor cursor = context.getContentResolver().query(fileUri, null, null, null, null);
                if(cursor == null) {
                    externalFilePath = fileUri.getPath();
                }
                else {
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    externalFilePath = cursor.getString(idx);
                    cursor.close();
                }

                externalFile = new File(externalFilePath);
                FileUtils.writeStringToFile(externalFile, s, Charset.forName("UTF-8"));
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                externalFile = null;
            }
        }

        return externalFile;
    }

    public static ArrayList<File> getExternalFiles(Context context, String subfolder) {
        updateExternalFiles(context, subfolder);

        ArrayList<File> fileArrayList;

        if(Build.VERSION.SDK_INT < 29) {
            try {
                File externalFolder = new File(getExternalFolderPath(context, subfolder));

                File[] externalFiles = externalFolder.listFiles();
                if(externalFiles == null) {
                    fileArrayList = new ArrayList<>();
                }
                else {
                    fileArrayList = new ArrayList<>(Arrays.asList(externalFiles));
                }
            }
            catch(Exception ignored) {
                fileArrayList = null;
            }
        }
        else {
            try {
                fileArrayList = new ArrayList<>();

                Cursor cursor = context.getContentResolver().query(
                    MediaStore.Files.getContentUri("external"),
                    null,
                    MediaStore.MediaColumns.RELATIVE_PATH + "=?",
                    new String[]{getExternalFolderPath(context, subfolder)}, null);
                if(cursor == null) {
                    // Something went wrong.
                    fileArrayList = null;
                }
                else {
                    while(cursor.moveToNext()) {
                        int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                        fileArrayList.add(new File(cursor.getString(idx)));
                    }
                    cursor.close();
                }
            }
            catch(Exception ignored) {
                fileArrayList = null;
            }
        }

        return fileArrayList;
    }

    public static void updateExternalFiles(Context context, String subfolder) {
        // Refresh the MediaStore in case files were altered by external means.
        // This is only needed on newer APIs.
        if(Build.VERSION.SDK_INT < 29) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(new File(getFullExternalFolderPath(context, subfolder))));
        context.sendBroadcast(intent);
    }

    public static File downloadFile(String urlString) {
        // Downloads the file at the url to a tempfile and then returns it.
        File file;
        try {
            // TODO Pass in name.
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
