package com.musicslayer.cryptobuddy.util;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.app.App;

import java.io.InputStream;
import java.io.OutputStream;

public class UriUtil {
    public static String readUri(Context context, Uri uri, String name) {
        InputStream o = null;

        try {
            DocumentFile documentFolder = DocumentFile.fromTreeUri(context, uri);
            DocumentFile documentFile = documentFolder.findFile(name);

            o = App.contentResolver.openInputStream(documentFile.getUri());
            String s = StreamUtil.readIntoString(o);
            StreamUtil.safeFlushAndClose(o);

            return s;
        }
        catch(Exception ignored) {
            StreamUtil.safeFlushAndClose(o);
            return null;
        }
    }

    public static boolean writeUri(Context context, Uri uri, String name, String s) {
        OutputStream o = null;

        try {
            DocumentFile documentFolder = DocumentFile.fromTreeUri(context, uri);

            // If the file exists, delete it before creating it again.
            DocumentFile oldDocumentFile = documentFolder.findFile(name);
            if(oldDocumentFile != null) {
                oldDocumentFile.delete();
            }

            DocumentFile documentFile = documentFolder.createFile("*/*", name);

            o = App.contentResolver.openOutputStream(documentFile.getUri());
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
