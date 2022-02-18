package com.musicslayer.cryptobuddy.file;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.DocumentsContract;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import com.musicslayer.cryptobuddy.activity.BaseActivity;
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
        out.writeString(Serialization.uri_serialize(treeUri));
    }

    public static final Parcelable.Creator<UniversalFile> CREATOR = new Parcelable.Creator<UniversalFile>() {
        @SuppressLint("NewApi")
        @Override
        public UniversalFile createFromParcel(Parcel in) {
            File file = Serialization.file_deserialize(in.readString());
            Uri treeUri = Serialization.uri_deserialize(in.readString());

            if(file != null) {
                return fromFile(file);
            }
            else if(treeUri != null) {
                return fromTreeUri(treeUri);
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
    Uri treeUri;

    public static UniversalFile fromFile(File file) {
        UniversalFile obj = new UniversalFile();
        obj.file = file;
        obj.treeUri = null;
        return obj;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static UniversalFile fromTreeUri(Uri treeUri) {
        UniversalFile obj = new UniversalFile();
        obj.file = null;
        obj.treeUri = treeUri;
        return obj;
    }

    private UniversalFile() {
    }

    @SuppressLint("NewApi")
    public String getDisplayPath() {
        // For regular files, this is the full path to the file.
        // For uris, this is the document ID.
        if(file != null) {
            return file.getAbsolutePath();
        }
        else if(treeUri != null) {
            return DocumentsContract.getDocumentId(treeUri);
        }
        else {
            return null;
        }
    }

    public String getName() {
        if(file != null) {
            return file.getName();
        }
        else if(treeUri != null) {
            return treeUri.getLastPathSegment();
        }
        else {
            return null;
        }
    }

    public boolean isFile() {
        if(file != null) {
            return file.isFile();
        }
        else if(treeUri != null) {
            DocumentFile documentFile = DocumentFile.fromSingleUri(BaseActivity.activity, treeUri);
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
        else if(treeUri != null) {
            DocumentFile documentFolder = DocumentFile.fromTreeUri(BaseActivity.activity, treeUri);
            return documentFolder.findFile(name) != null && documentFolder.findFile(name).isFile();
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
        else if(treeUri != null) {
            DocumentFile documentFile = DocumentFile.fromSingleUri(BaseActivity.activity, treeUri);
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
        else if(treeUri != null) {
            DocumentFile documentFolder = DocumentFile.fromTreeUri(BaseActivity.activity, treeUri);
            return documentFolder.findFile(name) != null;
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
        else if(treeUri != null) {
            DocumentFile[] documentFiles = DocumentFile.fromTreeUri(BaseActivity.activity, treeUri).listFiles();
            if(documentFiles != null) {
                for(DocumentFile documentFile : documentFiles) {
                    if(documentFile.isFile()) {
                        fileNames.add(documentFile.getName());
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
        else if(treeUri != null) {
            DocumentFile[] documentFiles = DocumentFile.fromTreeUri(BaseActivity.activity, treeUri).listFiles();
            if(documentFiles != null) {
                for(DocumentFile documentFile : documentFiles) {
                    if(!documentFile.isFile()) {
                        folderNames.add(documentFile.getName());
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
        else if(treeUri != null) {
            try {
                return UriUtil.readUri(BaseActivity.activity, treeUri, name);
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
        else if(treeUri != null) {
            try {
                return UriUtil.writeUri(BaseActivity.activity, treeUri, name, s);
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
