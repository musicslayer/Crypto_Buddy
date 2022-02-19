package com.musicslayer.cryptobuddy.data;

import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

// The difference between Serialization and Exportation is that an object serializes itself,
// whereas an object exports the data it has stored within.

// Note: Exportation has to be perfect, or we throw errors. There are no "default" or "fallback" values here.

// TODO Apply this to other classes.
// TODO Persistence Classes need exportationType.

public class Exportation {
    // Keep this short because it will appear on every piece of stored data.
    public final static String EXPORTATION_VERSION_MARKER = "!V!";

    // Any class implementing this can be exported and imported with JSON.
    public interface ExportableToJSON {
        // Classes also need to implement static methods "exportDataToJSON", "importDataFromJSON", and "exportationType".
    }

    // Any class implementing this supports versioning.
    // This is needed when saving data that may be loaded in a different release.
    public interface Versionable {
        // Classes also need to implement a static method "exportationVersion"
    }

    // Exporting is a static process, so we do not need an object.
    public static <T> String exportData(Class<T> clazzT) {
        Class<? extends ExportableToJSON> wrappedClass = wrapClass(clazzT);

        String s;
        try {
            s = ReflectUtil.callStaticMethod(wrappedClass, "exportDataToJSON");
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        if(Versionable.class.isAssignableFrom(wrappedClass)) {
            // Add the version to every individual object that we serialize, or error if we cannot.
            // Currently, only type !OBJECT! supports a version marker.
            try {
                String version = Exportation.getCurrentVersion(wrappedClass);
                String type = Exportation.getCurrentType(wrappedClass);

                if("!OBJECT!".equals(type)) {
                    JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
                    o.put(EXPORTATION_VERSION_MARKER, version, String.class);
                    s = o.toStringOrNull();
                }
            }
            catch(Exception e) {
                ThrowableUtil.processThrowable(e);
                throw new IllegalStateException(e);
            }
        }

        return s;
    }

    // Importing does not return anything. The class itself must store the data appropriately.
    public static <T> void importData(String s, Class<T> clazzT) {
        if(s == null) { return; }
        Class<? extends ExportableToJSON> wrappedClass = wrapClass(clazzT);

        String version = getVersion(s);

        try {
            ReflectUtil.callStaticMethod(wrappedClass, "importDataFromJSON", s, version);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> String getCurrentVersion(Class<T> clazz) {
        Class<? extends ExportableToJSON> wrappedClass = wrapClass(clazz);
        if(Versionable.class.isAssignableFrom(wrappedClass)) {
            return ReflectUtil.callStaticMethod(wrappedClass, "exportationVersion");
        }
        else {
            // If there is no version, just call it "version zero".
            return "0";
        }
    }

    public static String getVersion(String s) {
        try {
            JSONWithNull.JSONObjectWithNull o = new JSONWithNull.JSONObjectWithNull(s);
            return o.get(EXPORTATION_VERSION_MARKER, String.class);
        }
        catch(Exception ignored) {
            // If there is no version, just call it "version zero".
            return "0";
        }
    }

    public static <T> String getCurrentType(Class<T> clazz) {
        return getTypeForVersion(getCurrentVersion(clazz), clazz);
    }

    public static <T> String getTypeForVersion(String version, Class<T> clazz) {
        Class<? extends ExportableToJSON> wrappedClass = wrapClass(clazz);
        return ReflectUtil.callStaticMethod(wrappedClass, "exportationType", version);
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends ExportableToJSON> wrapClass(Class<?> clazz) {
        // Converts an arbitrary class into a ExportableToJSON class.
        if(ExportableToJSON.class.isAssignableFrom(clazz)) {
            return (Class<? extends ExportableToJSON>)clazz;
        }
        else {
            // Anything else is unsupported.
            throw new IllegalStateException();
        }
    }
}
