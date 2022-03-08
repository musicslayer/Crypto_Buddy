package com.musicslayer.cryptobuddy.data.bridge;

import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import org.json.JSONException;

import java.util.ArrayList;

public class LegacyReferentiation {
    // Keep this short because it will appear on every piece of stored data.
    public final static String REFERENTIATION_VERSION_MARKER = "!V!";

    // Any class implementing this can be serialized and deserialized with JSON.
    public interface ReferenceableToJSON {
        String legacy_referenceToJSON() throws JSONException;

        // Classes also need to implement static methods "legacy_dereferenceFromJSON" and "legacy_referentiationType".
    }

    // Any class implementing this supports versioning.
    // This is needed when saving data that may be loaded in a different release.
    public interface Versionable {
        // Classes also need to implement a static method "legacy_referentiationVersion".
    }

    public static <T> String reference(T obj, Class<T> clazzT) {
        if(obj == null) { return null; }
        ReferenceableToJSON wrappedObj = wrapObj(obj);
        Class<? extends ReferenceableToJSON> wrappedClass = wrapClass(clazzT);

        String s;
        try {
            s = wrappedObj.legacy_referenceToJSON();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }

        if(Versionable.class.isAssignableFrom(wrappedClass)) {
            // Add the version to every individual object that we reference, or error if we cannot.
            try {
                String version = LegacyReferentiation.getCurrentVersion(wrappedClass);
                String type = LegacyReferentiation.getCurrentType(wrappedClass);

                if("!OBJECT!".equals(type)) {
                    LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
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
            return ReflectUtil.callStaticMethod(wrappedClass, "legacy_dereferenceFromJSON", s, version);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> String getCurrentVersion(Class<T> clazzT) {
        Class<? extends ReferenceableToJSON> wrappedClass = wrapClass(clazzT);
        if(Versionable.class.isAssignableFrom(wrappedClass)) {
            return ReflectUtil.callStaticMethod(wrappedClass, "legacy_referentiationVersion");
        }
        else {
            // If there is no version, just call it "version zero".
            return "0";
        }
    }

    public static String getVersion(String s) {
        try {
            LegacyDataBridge.JSONObjectDataBridge o = new LegacyDataBridge.JSONObjectDataBridge(s);
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
        return ReflectUtil.callStaticMethod(wrappedClass, "legacy_referentiationType", version);
    }

    public static <T> String referenceArrayList(ArrayList<T> arrayList, Class<T> clazzT) {
        if(arrayList == null) { return null; }

        try {
            LegacyDataBridge.JSONArrayDataBridge a = new LegacyDataBridge.JSONArrayDataBridge();
            for(T t : arrayList) {
                a.reference(t, clazzT);
            }

            return a.toStringOrNull();
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static <T> ArrayList<T> dereferenceArrayList(String s, Class<T> clazzT) {
        if(s == null) { return null; }

        try {
            ArrayList<T> arrayList = new ArrayList<>();

            LegacyDataBridge.JSONArrayDataBridge a = new LegacyDataBridge.JSONArrayDataBridge(s);
            for(int i = 0; i < a.length(); i++) {
                arrayList.add(a.dereference(i, clazzT));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

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
