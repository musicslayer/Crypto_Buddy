package com.musicslayer.cryptobuddy.util;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;
import java.io.OutputStream;

public class UriUtil {
    public static String readFile(Context context, Uri uri, String fileName) {
        InputStream o = null;

        try {
            DocumentFile documentFolder = DocumentFile.fromTreeUri(context, uri);

            // If the file doesn't exists, return null.
            DocumentFile documentFile = documentFolder.findFile(fileName);
            if(documentFile == null) {
                return null;
            }

            o = context.getContentResolver().openInputStream(documentFile.getUri());
            String s = StreamUtil.readIntoString(o);
            StreamUtil.safeFlushAndClose(o);

            return s;
        }
        catch(Exception ignored) {
            StreamUtil.safeFlushAndClose(o);
            return null;
        }
    }

    public static boolean writeFile(Context context, Uri uri, String fileName, String s) {
        OutputStream o = null;

        try {
            DocumentFile documentFolder = DocumentFile.fromTreeUri(context, uri);

            // If the file exists, delete it before creating it.
            DocumentFile oldDocumentFile = documentFolder.findFile(fileName);
            if(oldDocumentFile != null) {
                oldDocumentFile.delete();
            }

            DocumentFile documentFile = documentFolder.createFile("*/*", fileName);

            o = context.getContentResolver().openOutputStream(documentFile.getUri());
            StreamUtil.writeFromString(o, s);
            StreamUtil.safeFlushAndClose(o);

            return true;
        }
        catch(Exception ignored) {
            StreamUtil.safeFlushAndClose(o);
            return false;
        }
    }
}
