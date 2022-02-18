package com.musicslayer.cryptobuddy.util;

import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.app.App;

import java.io.InputStream;
import java.io.OutputStream;

public class UriUtil {
    public static String readUri(DocumentFile documentFile, String name) {
        InputStream o = null;

        try {
            DocumentFile child = documentFile.findFile(name);

            o = App.contentResolver.openInputStream(child.getUri());
            String s = StreamUtil.readIntoString(o);
            StreamUtil.safeFlushAndClose(o);

            return s;
        }
        catch(Exception ignored) {
            StreamUtil.safeFlushAndClose(o);
            return null;
        }
    }

    public static boolean writeUri(DocumentFile documentFile, String name, String s) {
        OutputStream o = null;

        try {
            // If the file exists, delete it before creating it again.
            DocumentFile oldChild = documentFile.findFile(name);
            if(oldChild != null) {
                oldChild.delete();
            }

            DocumentFile child = documentFile.createFile("*/*", name);

            o = App.contentResolver.openOutputStream(child.getUri());
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
