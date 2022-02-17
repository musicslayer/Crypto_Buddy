package com.musicslayer.cryptobuddy.util;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class UriUtil {
    public static String readFile(Context context, Uri uri, String fileName) {
        try {
            DocumentFile documentFolder = DocumentFile.fromTreeUri(context, uri);

            // If the file doesn't exists, return null.
            DocumentFile documentFile = documentFolder.findFile(fileName);
            if(documentFile == null) {
                return null;
            }

            InputStream o = context.getContentResolver().openInputStream(documentFile.getUri());
            return StreamUtil.readIntoString(o);
        }
        catch(Exception ignored) {
            return null;
        }
    }

    public static boolean writeFile(Context context, Uri uri, String fileName, String s) {
        try {
            DocumentFile documentFolder = DocumentFile.fromTreeUri(context, uri);

            // If the file exists, delete it before creating it.
            DocumentFile oldDocumentFile = documentFolder.findFile(fileName);
            if(oldDocumentFile != null) {
                oldDocumentFile.delete();
            }

            DocumentFile documentFile = documentFolder.createFile("*/*", fileName);

            byte[] out = s.getBytes(Charset.forName("UTF-8"));
            OutputStream o = context.getContentResolver().openOutputStream(documentFile.getUri());
            o.write(out);
            o.close();
            return true;
        }
        catch(Exception ignored) {
            return false;
        }
    }
}
