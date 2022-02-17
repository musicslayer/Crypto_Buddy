package com.musicslayer.cryptobuddy.util;

import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {
    public static void unzip(File file, UnzipListener L) {
        // Iterate through a zip file's contents. For each entry (file/folder) found, call the provided listener.
        FileInputStream fin = null;
        ZipInputStream zin = null;

        try {
            fin = new FileInputStream(file);
            zin = new ZipInputStream(fin);

            ZipEntry zipEntry;
            while((zipEntry = zin.getNextEntry()) != null){
                ProgressDialogFragment.checkForInterrupt();

                L.onUnzip(zipEntry, zin);
                zin.closeEntry();
            }

            StreamUtil.safeFlushAndClose(fin);
            StreamUtil.safeFlushAndClose(zin);
        }
        catch(Exception e) {
            StreamUtil.safeFlushAndClose(fin);
            StreamUtil.safeFlushAndClose(zin);
        }
    }

    abstract public static class UnzipListener {
        abstract public void onUnzip(ZipEntry zipEntry, ZipInputStream zin) throws IOException;
    }
}
