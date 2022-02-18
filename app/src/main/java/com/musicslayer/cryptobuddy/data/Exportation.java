package com.musicslayer.cryptobuddy.data;

import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

// The difference between Serialization and Exportation is that an object serializes itself,
// whereas an object exports the data it has stored within.

// Note: Exportation has to be perfect, or we throw errors. There are no "default" or "fallback" values here.

public class Exportation {
    // Keep this short because it will appear on every piece of stored data.
    public final static String EXPORTATION_VERSION_MARKER = "!V!";

    // Any class implementing this can be exported and imported with JSON.
    public interface ExportableToJSON {
        // "exportDataToJSON" is always assumed to be the latest version for that class.
        String exportDataToJSON() throws org.json.JSONException;

        // Classes also need to implement a static method "importDataFromJSON".
    }

    // Any class implementing this supports versioning.
    // This is needed when saving data that may be loaded in a different release.
    public interface Versionable {
        // Can be different for every individual class that implements this interface.
        String exportationVersion();

        // Classes also need to implement a static method "importDataFromJSON" + VERSION for each version they support.
    }

    // Methods for objects that implement "ExportableToJSON".
    public static <T extends ExportableToJSON> String exportData(T obj) {
        if(obj == null) { return null; }

        String s;
        try {
            s = obj.exportDataToJSON();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        if(obj instanceof Versionable) {
            // Add the version to every individual object that we serialize, or error if we cannot.
            try {
                JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
                o.put(EXPORTATION_VERSION_MARKER, ((Versionable)obj).exportationVersion());
                s = o.toStringOrNull();
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }

        return s;
    }

    public static <T extends ExportableToJSON> void importData(String s, Class<T> clazzT) {
        if(s == null) { return; }

        // First try to get the version number. If none is present, then the data was not versioned.
        String version;
        try {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
            version = o.getString(EXPORTATION_VERSION_MARKER);
        }
        catch(Exception e) {
            version = null;
        }

        // Call the appropriate deserialization method for the version number.
        try {
            if(version == null) {
                ReflectUtil.callStaticMethod(clazzT, "importDataFromJSON", s);
            }
            else {
                ReflectUtil.callStaticMethod(clazzT, "importDataFromJSON" + version, s);
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }
}
