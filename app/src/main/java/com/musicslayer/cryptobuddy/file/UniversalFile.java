package com.musicslayer.cryptobuddy.file;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.DocumentsContract;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.UriUtil;

import java.io.File;
import java.util.ArrayList;

// Can be used to represent items that may be direct file paths or URI paths.
// Note that, similar to java.io.File, a UniversalFile may represent a folder or a file.

// For now, this only deals with a small subset of possible URIs.
public class UniversalFile implements Parcelable {
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(Serialization.file_serialize(file));
        out.writeString(Serialization.documentfile_serialize(documentFile));
    }

    public static final Parcelable.Creator<UniversalFile> CREATOR = new Parcelable.Creator<UniversalFile>() {
        @SuppressLint("NewApi")
        @Override
        public UniversalFile createFromParcel(Parcel in) {
            File file = Serialization.file_deserialize(in.readString());
            DocumentFile documentFile = Serialization.documentfile_deserialize(in.readString());

            if(file != null) {
                return fromFile(file);
            }
            else if(documentFile != null) {
                return fromDocumentFile(documentFile);
            }
            else {
                return null;
            }
        }

        @Override
        public UniversalFile[] newArray(int size) {
            return new UniversalFile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    // Only one field should be non-null.
    File file;
    DocumentFile documentFile;

    public static UniversalFile fromFile(File file) {
        UniversalFile obj = new UniversalFile();
        obj.file = file;
        obj.documentFile = null;
        return obj;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static UniversalFile fromDocumentFile(DocumentFile documentFile) {
        UniversalFile obj = new UniversalFile();
        obj.file = null;
        obj.documentFile = documentFile;
        return obj;
    }

    private UniversalFile() {
    }

    @SuppressLint("NewApi")
    public String getDisplayPath() {
        // For regular files, this is the full path to the file.
        // For document files, this is the document ID.
        if(file != null) {
            return file.getAbsolutePath();
        }
        else if(documentFile != null) {
            return DocumentsContract.getDocumentId(documentFile.getUri());
        }
        else {
            return null;
        }
    }

    public boolean isFile() {
        if(file != null) {
            return file.isFile();
        }
        else if(documentFile != null) {
            return documentFile.isFile();
        }
        else {
            // If we don't recognize it, just say it isn't a file.
            return false;
        }
    }

    public boolean containsFile(String name) {
        // Returns whether this contains the input as a file.
        // It is assumed this object represents a directory.
        if(file != null) {
            return new File(file.getAbsolutePath() + File.separatorChar + name).isFile();
        }
        else if(documentFile != null) {
            DocumentFile child = documentFile.findFile(name);
            return child != null && child.isFile();
        }
        else {
            // If we don't recognize it, just say it doesn't exist.
            return false;
        }
    }

    public boolean exists() {
        // Returns whether this file exists.
        if(file != null) {
            return file.exists();
        }
        else if(documentFile != null) {
            return documentFile.exists();
        }
        else {
            // If we don't recognize it, just say it doesn't exist.
            return false;
        }
    }

    public boolean contains(String name) {
        // Returns whether this contains the input as a file.
        // It is assumed this object represents a directory.
        if(file != null) {
            return new File(file.getAbsolutePath() + File.separatorChar + name).exists();
        }
        else if(documentFile != null) {
            return documentFile.findFile(name) != null;
        }
        else {
            // If we don't recognize it, just say it doesn't exist.
            return false;
        }
    }

    public ArrayList<String> getFileNames() {
        ArrayList<String> fileNames = new ArrayList<>();

        if(file != null) {
            File[] files = file.listFiles();
            if(files != null) {
                for(File file : files) {
                    if(file.isFile()) {
                        fileNames.add(file.getName());
                    }
                }
            }
        }
        else if(documentFile != null) {
            DocumentFile[] documentFiles = documentFile.listFiles();
            if(documentFiles != null) {
                for(DocumentFile df : documentFiles) {
                    if(df.isFile()) {
                        fileNames.add(df.getName());
                    }
                }
            }
        }
        else {
            fileNames = null;
        }

        return fileNames;
    }

    public ArrayList<String> getFolderNames() {
        ArrayList<String> folderNames = new ArrayList<>();

        if(file != null) {
            File[] files = file.listFiles();
            if(files != null) {
                for(File file : files) {
                    if(!file.isFile()) {
                        folderNames.add(file.getName());
                    }
                }
            }
        }
        else if(documentFile != null) {
            DocumentFile[] documentFiles = documentFile.listFiles();
            if(documentFiles != null) {
                for(DocumentFile df : documentFiles) {
                    if(!df.isFile()) {
                        folderNames.add(df.getName());
                    }
                }
            }
        }
        else {
            folderNames = null;
        }

        return folderNames;
    }

    public String readFile(String name) {
        // Reads the content of the input file in this folder and returns it as a string.
        if(file != null) {
            try {
                return FileUtil.readFile(file, name);
            }
            catch(Exception ignored) {
                return null;
            }
        }
        else if(documentFile != null) {
            try {
                return UriUtil.readUri(documentFile, name);
            }
            catch(Exception ignored) {
                return null;
            }
        }
        else {
            // Nothing read, so return null.
            return null;
        }
    }

    public boolean writeFile(String name, String s) {
        // Writes string to the input file in this folder and returns whether it was a success.
        if(file != null) {
            try {
                return FileUtil.writeFile(file, name, s);
            }
            catch(Exception ignored) {
                return false;
            }
        }
        else if(documentFile != null) {
            try {
                return UriUtil.writeUri(documentFile, name, s);
            }
            catch(Exception ignored) {
                return false;
            }
        }
        else {
            // Nothing written, so return false.
            return false;
        }
    }
}
