package com.musicslayer.cryptobuddy.data;

import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

// The difference between Serialization and Exportation is that an object serializes itself,
// whereas an object exports the data it has stored within.

// Note: Exportation has to be perfect, or we throw errors. There are no "default" or "fallback" values here.

// TODO Apply this to other classes.

public class Exportation {
    // Keep this short because it will appear on every piece of stored data.
    public final static String EXPORTATION_VERSION_MARKER = "!V!";

    // Any class implementing this can be exported and imported with JSON.
    public interface ExportableToJSON {
        String exportDataToJSON() throws org.json.JSONException;
        void importDataFromJSON(String s, String version) throws org.json.JSONException;

        // Classes also need to implement a static method "exportationType".
    }

    // Any class implementing this supports versioning.
    // This is needed when saving data that may be loaded in a different release.
    public interface Versionable {
        // Classes also need to implement a static method "exportationVersion".
    }

    public static <T> String exportData(T obj, Class<T> clazzT) {
        if(obj == null) { return null; }
        ExportableToJSON wrappedObj = wrapObj(obj);
        Class<? extends ExportableToJSON> wrappedClass = wrapClass(clazzT);

        String s;
        try {
            s = wrappedObj.exportDataToJSON();
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
                    DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
                    o.serialize(EXPORTATION_VERSION_MARKER, version, String.class);
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

    // Importing does not return anything. The object passed in must store the data appropriately.
    public static <T> void importData(T obj, String s, Class<T> clazzT) {
        if(s == null) { return; }
        ExportableToJSON wrappedObj = wrapObj(obj);
        Class<? extends ExportableToJSON> wrappedClass = wrapClass(clazzT);

        String version = getVersion(s);

        try {
            wrappedObj.importDataFromJSON(s, version);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> String getCurrentVersion(Class<T> clazzT) {
        Class<? extends ExportableToJSON> wrappedClass = wrapClass(clazzT);
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
            DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
            return o.deserialize(EXPORTATION_VERSION_MARKER, String.class);
        }
        catch(Exception ignored) {
            // If there is no version, just call it "version zero".
            return "0";
        }
    }

    public static <T> String getCurrentType(Class<T> clazzT) {
        return getTypeForVersion(getCurrentVersion(clazzT), clazzT);
    }

    public static <T> String getTypeForVersion(String version, Class<T> clazzT) {
        Class<? extends ExportableToJSON> wrappedClass = wrapClass(clazzT);
        return ReflectUtil.callStaticMethod(wrappedClass, "exportationType", version);
    }

    // For classes that do not implement ExportableToJSON, we wrap them in classes that define the required methods.
    public static ExportableToJSON wrapObj(Object obj) {
        // Converts an arbitrary object into a ExportableToJSON subclass.
        // Note that obj will always be non-null.
        if(obj instanceof ExportableToJSON) {
            return (ExportableToJSON)obj;
        }
        else {
            // Anything else is unsupported.
            throw new IllegalStateException();
        }
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
