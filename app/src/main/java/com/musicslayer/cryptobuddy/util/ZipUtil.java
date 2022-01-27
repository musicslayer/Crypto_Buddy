package com.musicslayer.cryptobuddy.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

// TODO Make sure folder is last folder before the file.

public class ZipUtil {
    public static HashMap<String, String> unzip(File file, String folder) {
        // Return a map of file names to their content, for files within the folder given.
        HashMap<String, String> contents = new HashMap<>();

        FileInputStream fin = null;
        ZipInputStream zin = null;

        try {
            fin = new FileInputStream(file);
            zin = new ZipInputStream(fin);

            ZipEntry entry;
            while((entry = zin.getNextEntry()) != null){
                if(!entry.isDirectory() && entry.getName().contains(folder + "/")) {
                    // This works whether or not "/" is in the file name.
                    String fullFileName = entry.getName();
                    int pos = fullFileName.lastIndexOf("/");
                    String fileName = fullFileName.substring(pos+1);

                    String fileContents = StreamUtil.readIntoString(zin);
                    contents.put(fileName, fileContents);
                }
                zin.closeEntry();
            }

            StreamUtil.safeClose(fin);
            StreamUtil.safeClose(zin);
        }
        catch(Exception e) {
            StreamUtil.safeClose(fin);
            StreamUtil.safeClose(zin);
            contents = null;
        }

        return contents;
    }
}
