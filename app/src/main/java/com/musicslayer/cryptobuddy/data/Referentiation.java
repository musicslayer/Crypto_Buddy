package com.musicslayer.cryptobuddy.data;

import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

// The difference between Serialization and Referentiation is that an object serializes itself so it can be rebuilt completely,
// whereas an object creates references to lookup the object later from another place that has the information.

// Note: Referentiation has to be perfect, or we throw errors. There are no "default" or "fallback" values here.

// TODO Apply this to other classes.
// TODO - HashMap has two classes. What if I want to serialize on, and reference another?

public class Referentiation {
    // Keep this short because it will appear on every piece of stored data.
    public final static String REFERENTIATION_VERSION_MARKER = "!V!";

    // Any class implementing this can create and load references of itself with JSON.
    public interface ReferenceableToJSON {
        String referenceToJSON() throws org.json.JSONException;

        // Classes also need to implement static methods "dereferenceFromJSON" and "referentiationType".
    }

    // Any class implementing this supports versioning.
    // This is needed when saving data that may be loaded in a different release.
    public interface Versionable {
        // Classes also need to implement a static method "referentiationVersion".
    }

    // Methods for objects that implement "ReferenceableToJSON".
    public static <T> String reference(T obj, Class<T> clazzT) {
        if(obj == null) { return null; }
        ReferenceableToJSON wrappedObj = wrapObj(obj);
        Class<? extends ReferenceableToJSON> wrappedClass = wrapClass(clazzT);

        String s;
        try {
            s = wrappedObj.referenceToJSON();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        if(Versionable.class.isAssignableFrom(wrappedClass)) {
            // Add the version to every individual object that we serialize, or error if we cannot.
            try {
                String version = Referentiation.getCurrentVersion(wrappedClass);
                String type = Referentiation.getCurrentType(wrappedClass);

                if("!OBJECT!".equals(type)) {
                    DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
                    o.serialize(REFERENTIATION_VERSION_MARKER, version, String.class);
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

    public static <T> T dereference(String s, Class<T> clazzT) {
        if(s == null) { return null; }
        Class<? extends ReferenceableToJSON> wrappedClass = wrapClass(clazzT);

        String version = getVersion(s);

        try {
            return ReflectUtil.callStaticMethod(wrappedClass, "dereferenceFromJSON", s, version);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> String getCurrentVersion(Class<T> clazzT) {
        Class<? extends ReferenceableToJSON> wrappedClass = wrapClass(clazzT);
        if(Versionable.class.isAssignableFrom(wrappedClass)) {
            return ReflectUtil.callStaticMethod(wrappedClass, "referentiationVersion");
        }
        else {
            // If there is no version, just call it "version zero".
            return "0";
        }
    }

    public static String getVersion(String s) {
        try {
            DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
            return o.deserialize(REFERENTIATION_VERSION_MARKER, String.class);
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
        Class<? extends ReferenceableToJSON> wrappedClass = wrapClass(clazzT);
        return ReflectUtil.callStaticMethod(wrappedClass, "referentiationType", version);
    }

    // For classes that do not implement SerializableToJSON, we wrap them in classes that define the required methods.
    public static ReferenceableToJSON wrapObj(Object obj) {
        // Converts an arbitrary object into a ReferenceableToJSON subclass.
        // Note that obj will always be non-null.
        if(obj instanceof ReferenceableToJSON) {
            return (ReferenceableToJSON)obj;
        }
        else {
            // Anything else is unsupported.
            throw new IllegalStateException();
        }
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends ReferenceableToJSON> wrapClass(Class<?> clazz) {
        // Converts an arbitrary class into a ReferenceableToJSON class.
        if(ReferenceableToJSON.class.isAssignableFrom(clazz)) {
            return (Class<? extends ReferenceableToJSON>)clazz;
        }
        else {
            // Anything else is unsupported.
            throw new IllegalStateException();
        }
    }
}
