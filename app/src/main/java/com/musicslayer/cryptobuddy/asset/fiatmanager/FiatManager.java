package com.musicslayer.cryptobuddy.asset.fiatmanager;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.fiat.Fiat;
import com.musicslayer.cryptobuddy.asset.fiat.UnknownFiat;
import com.musicslayer.cryptobuddy.persistence.FiatManagerList;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class FiatManager implements Serialization.SerializableToJSON, Serialization.Versionable {
    public static ArrayList<FiatManager> fiatManagers;
    public static HashMap<String, FiatManager> fiatManagers_map;
    public static HashMap<String, FiatManager> fiatManagers_fiat_type_map;
    public static ArrayList<String> fiatManagers_names;
    public static ArrayList<String> fiatManagers_fiat_types;

    public ArrayList<Fiat> hardcoded_fiats;
    public HashMap<String, Fiat> hardcoded_fiat_map;
    public ArrayList<String> hardcoded_fiat_names;
    public ArrayList<String> hardcoded_fiat_display_names;

    public ArrayList<Fiat> found_fiats;
    public HashMap<String, Fiat> found_fiat_map;
    public ArrayList<String> found_fiat_names;
    public ArrayList<String> found_fiat_display_names;

    public ArrayList<Fiat> custom_fiats;
    public HashMap<String, Fiat> custom_fiat_map;
    public ArrayList<String> custom_fiat_names;
    public ArrayList<String> custom_fiat_display_names;

    abstract public String getKey();
    abstract public String getName();
    abstract public String getFiatType();

    // Used to store persistent state
    abstract public String getSettingsKey();

    abstract public void initializeHardcodedFiats(Context context);

    // Most times we cannot do lookup, but subclasses can override if they can.
    public Fiat lookupFiat(String key, String name, String display_name, int scale) { return null; }

    public FiatManager() {
        this.hardcoded_fiats = new ArrayList<>();
        this.hardcoded_fiat_map = new HashMap<>();
        this.hardcoded_fiat_names = new ArrayList<>();
        this.hardcoded_fiat_display_names = new ArrayList<>();

        this.found_fiats = new ArrayList<>();
        this.found_fiat_map = new HashMap<>();
        this.found_fiat_names = new ArrayList<>();
        this.found_fiat_display_names = new ArrayList<>();

        this.custom_fiats = new ArrayList<>();
        this.custom_fiat_map = new HashMap<>();
        this.custom_fiat_names = new ArrayList<>();
        this.custom_fiat_display_names = new ArrayList<>();
    }

    public static void initialize(Context context) {
        fiatManagers = new ArrayList<>();
        fiatManagers_map = new HashMap<>();
        fiatManagers_fiat_type_map = new HashMap<>();
        fiatManagers_fiat_types = new ArrayList<>();

        fiatManagers_names = FileUtil.readFileIntoLines(context, R.raw.asset_fiatmanager);
        for(String fiatManagerName : fiatManagers_names) {
            FiatManager fiatManager = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.fiatmanager." + fiatManagerName);

            // Use the deserialized dummy object to fill in the fiats in this real one.
            FiatManager copyFiatManager = FiatManagerList.loadData(context, fiatManager.getSettingsKey());

            // If this is a new FiatManager that wasn't previously saved, then there are no fiats to add.
            if(copyFiatManager != null) {
                fiatManager.addHardcodedFiat(copyFiatManager.hardcoded_fiats);
                fiatManager.addFoundFiat(copyFiatManager.found_fiats);
                fiatManager.addCustomFiat(copyFiatManager.custom_fiats);
            }

            fiatManagers.add(fiatManager);
            fiatManagers_map.put(fiatManager.getKey(), fiatManager);
            fiatManagers_fiat_type_map.put(fiatManager.getFiatType(), fiatManager);
            fiatManagers_fiat_types.add(fiatManager.getFiatType());

            fiatManager.initializeHardcodedFiats(context);
        }
    }

    public static FiatManager getDefaultFiatManager() {
        return FiatManager.getFiatManagerFromKey("BaseFiatManager");
    }

    public static FiatManager getFiatManagerFromKey(String key) {
        FiatManager fiatManager = fiatManagers_map.get(key);
        if(fiatManager == null) {
            fiatManager = UnknownFiatManager.createUnknownFiatManager(key, "?");
        }

        return fiatManager;
    }

    public static FiatManager getFiatManagerFromFiatType(String fiatType) {
        FiatManager fiatManager = fiatManagers_fiat_type_map.get(fiatType);
        if(fiatManager == null) {
            fiatManager = UnknownFiatManager.createUnknownFiatManager("?", fiatType);
        }

        return fiatManager;
    }

    public Fiat getFiat(String key, String name, String display_name, int scale) {
        // Try to get the fiat from storage, then try to look it up, then try to create it from the input information.
        // If all of that fails, then return an UnknownFiat instance.
        Fiat fiat = getFiatWithPrecedence(key);

        if(fiat == null) {
            fiat = lookupFiat(key, name, display_name, scale);

            if(fiat == null || !fiat.isComplete()) {
                fiat = Fiat.buildFiat(key, name, display_name, scale, getFiatType());

                if(!fiat.isComplete()) {
                    fiat = UnknownFiat.createUnknownFiat(key, name, display_name, scale, getFiatType());
                }
            }

            addFoundFiat(fiat);
        }

        return fiat;
    }

    public Fiat getExistingFiat(String key, String name, String display_name, int scale, HashMap<String, String> additionalInfo) {
        // Only return a stored fiat. Do not build or lookup one.
        Fiat fiat = getFiatWithPrecedence(key);
        if(fiat == null) {
            fiat = UnknownFiat.createUnknownFiat(key, name, display_name, scale, getFiatType(), additionalInfo);
        }

        return fiat;
    }

    private Fiat getFiatWithPrecedence(String key) {
        // Maintain precedence - Hardcoded, then Found, then Custom Fiats.
        Fiat fiat = hardcoded_fiat_map.get(key);
        if(fiat == null) {
            fiat = found_fiat_map.get(key);
        }
        if(fiat == null) {
            fiat = custom_fiat_map.get(key);
        }

        return fiat;
    }

    public Fiat getHardcodedFiat(String key) {
        // Only allow the hardcoded fiat to be found.
        return hardcoded_fiat_map.get(key);
    }

    // Hardcoded fiats are not normally deleted.
    public void removeHardcodedFiat(Fiat fiat) {
        String key = fiat.getKey();
        if(hardcoded_fiat_map.get(key) != null) {
            hardcoded_fiats.remove(fiat);
            HashMapUtil.removeValueFromMap(hardcoded_fiat_map, key);
            hardcoded_fiat_names.remove(fiat.getName());
            hardcoded_fiat_display_names.remove(fiat.getDisplayName());
        }
    }

    public void removeFoundFiat(Fiat fiat) {
        String key = fiat.getKey();
        if(found_fiat_map.get(key) != null) {
            found_fiats.remove(fiat);
            HashMapUtil.removeValueFromMap(found_fiat_map, key);
            found_fiat_names.remove(fiat.getName());
            found_fiat_display_names.remove(fiat.getDisplayName());
        }
    }

    public void removeCustomFiat(Fiat fiat) {
        String key = fiat.getKey();
        if(custom_fiat_map.get(key) != null) {
            custom_fiats.remove(fiat);
            HashMapUtil.removeValueFromMap(custom_fiat_map, key);
            custom_fiat_names.remove(fiat.getName());
            custom_fiat_display_names.remove(fiat.getDisplayName());
        }
    }

    public void addHardcodedFiat(ArrayList<Fiat> fiatArrayList) {
        if(fiatArrayList == null) { return; }

        for(Fiat fiat : fiatArrayList) {
            addHardcodedFiat(fiat);
        }
    }

    public void addHardcodedFiat(Fiat fiat) {
        if(fiat == null || !fiat.isComplete()) { return; }

        String key = fiat.getKey();

        // Add or replace fiat.
        if(hardcoded_fiat_map.get(key) == null) {
            hardcoded_fiats.add(fiat);
            hardcoded_fiat_map.put(key, fiat);
            hardcoded_fiat_names.add(fiat.getName());
            hardcoded_fiat_display_names.add(fiat.getDisplayName());
        }
        else {
            hardcoded_fiat_map.put(key, fiat);

            int idx = hardcoded_fiats.indexOf(fiat);
            hardcoded_fiats.set(idx, fiat);
            hardcoded_fiat_names.set(idx, fiat.getName());
            hardcoded_fiat_display_names.set(idx, fiat.getDisplayName());
        }
    }

    public void addFoundFiat(ArrayList<Fiat> fiatArrayList) {
        if(fiatArrayList == null) { return; }

        for(Fiat fiat : fiatArrayList) {
            addFoundFiat(fiat);
        }
    }

    public void addFoundFiat(Fiat fiat) {
        if(fiat == null || !fiat.isComplete()) { return; }

        String key = fiat.getKey();

        // Add or replace fiat.
        if(found_fiat_map.get(key) == null) {
            found_fiats.add(fiat);
            found_fiat_map.put(key, fiat);
            found_fiat_names.add(fiat.getName());
            found_fiat_display_names.add(fiat.getDisplayName());
        }
        else {
            found_fiat_map.put(key, fiat);

            int idx = found_fiats.indexOf(fiat);
            found_fiats.set(idx, fiat);
            found_fiat_names.set(idx, fiat.getName());
            found_fiat_display_names.set(idx, fiat.getDisplayName());
        }
    }

    public void addCustomFiat(ArrayList<Fiat> fiatArrayList) {
        if(fiatArrayList == null) { return; }

        for(Fiat fiat : fiatArrayList) {
            addCustomFiat(fiat);
        }
    }

    public void addCustomFiat(Fiat fiat) {
        if(fiat == null || !fiat.isComplete()) { return; }

        String key = fiat.getKey();

        // Add or replace fiat.
        if(custom_fiat_map.get(key) == null) {
            custom_fiats.add(fiat);
            custom_fiat_map.put(key, fiat);
            custom_fiat_names.add(fiat.getName());
            custom_fiat_display_names.add(fiat.getDisplayName());
        }
        else {
            custom_fiat_map.put(key, fiat);

            int idx = custom_fiats.indexOf(fiat);
            custom_fiats.set(idx, fiat);
            custom_fiat_names.set(idx, fiat.getName());
            custom_fiat_display_names.set(idx, fiat.getDisplayName());
        }
    }

    // Hardcoded fiats are not normally deleted.
    public static void resetAllHardcodedFiats() {
        for(FiatManager fiatManager : fiatManagers) {
            fiatManager.resetHardcodedFiats();
        }
    }

    public void resetHardcodedFiats() {
        hardcoded_fiats = new ArrayList<>();
        hardcoded_fiat_map = new HashMap<>();
        hardcoded_fiat_names = new ArrayList<>();
        hardcoded_fiat_display_names = new ArrayList<>();
    }

    public static void resetAllFoundFiats() {
        for(FiatManager fiatManager : fiatManagers) {
            fiatManager.resetFoundFiats();
        }
    }

    public void resetFoundFiats() {
        found_fiats = new ArrayList<>();
        found_fiat_map = new HashMap<>();
        found_fiat_names = new ArrayList<>();
        found_fiat_display_names = new ArrayList<>();
    }

    public static void resetAllCustomFiats() {
        for(FiatManager fiatManager : fiatManagers) {
            fiatManager.resetCustomFiats();
        }
    }

    public void resetCustomFiats() {
        custom_fiats = new ArrayList<>();
        custom_fiat_map = new HashMap<>();
        custom_fiat_names = new ArrayList<>();
        custom_fiat_display_names = new ArrayList<>();
    }

    public static ArrayList<Fiat> getAllFiats() {
        ArrayList<Fiat> fiats = new ArrayList<>();

        for(FiatManager fiatManager : fiatManagers) {
            fiats.addAll(fiatManager.getFiats());
        }

        return fiats;
    }

    public ArrayList<Fiat> getFiats() {
        // Here we take into account precedence by favoring hardcoded fiats over found fiats over custom fiats.
        // We don't actually delete the shadowed fiats from the FiatManager, we merely don't add them to the list this method returns.
        ArrayList<Fiat> fiats = new ArrayList<>();

        // Found fiats -> remove hardcoded keys
        ArrayList<Fiat> copy_found_fiats = new ArrayList<>(found_fiats);
        copy_found_fiats.removeAll(hardcoded_fiats);

        // Custom fiats -> remove hardcoded/found keys
        ArrayList<Fiat> copy_custom_fiats = new ArrayList<>(custom_fiats);
        copy_custom_fiats.removeAll(hardcoded_fiats);
        copy_custom_fiats.removeAll(copy_found_fiats);

        fiats.addAll(hardcoded_fiats);
        fiats.addAll(copy_found_fiats);
        fiats.addAll(copy_custom_fiats);

        return fiats;
    }

    public static ArrayList<String> getAllFiatNames() {
        ArrayList<String> names = new ArrayList<>();

        for(FiatManager fiatManager : fiatManagers) {
            names.addAll(fiatManager.getFiatNames());
        }

        return names;
    }

    public ArrayList<String> getFiatNames() {
        ArrayList<String> names = new ArrayList<>();

        for(Fiat fiat : getFiats()) {
            names.add(fiat.getName());
        }

        return names;
    }

    public static ArrayList<String> getAllFiatDisplayNames() {
        ArrayList<String> displayNames = new ArrayList<>();

        for(FiatManager fiatManager : fiatManagers) {
            displayNames.addAll(fiatManager.getFiatDisplayNames());
        }

        return displayNames;
    }

    public ArrayList<String> getFiatDisplayNames() {
        ArrayList<String> displayNames = new ArrayList<>();

        for(Fiat fiat : getFiats()) {
            displayNames.add(fiat.getDisplayName());
        }

        return displayNames;
    }

    public String serializationVersion() { return "2"; }

    public String serializeToJSON() throws org.json.JSONException {
        // Just serialize the fiat array lists. FiatManagerList keeps track of which FiatManager had these.
        return new Serialization.JSONObjectWithNull()
            .put("key", Serialization.string_serialize(getKey()))
            .put("fiat_type", Serialization.string_serialize(getFiatType()))
            .put("hardcoded_fiats", new Serialization.JSONArrayWithNull(Serialization.fiat_serializeArrayList(hardcoded_fiats)))
            .put("found_fiats", new Serialization.JSONArrayWithNull(Serialization.fiat_serializeArrayList(found_fiats)))
            .put("custom_fiats", new Serialization.JSONArrayWithNull(Serialization.fiat_serializeArrayList(custom_fiats)))
            .toStringOrNull();
    }

    public static FiatManager deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String key = Serialization.string_deserialize(o.getString("key"));
        String fiat_type = Serialization.string_deserialize(o.getString("fiat_type"));
        ArrayList<Fiat> hardcoded_fiats = fiat_deserializeArrayList1(o.getJSONArrayString("hardcoded_fiats"));
        ArrayList<Fiat> found_fiats = fiat_deserializeArrayList1(o.getJSONArrayString("found_fiats"));
        ArrayList<Fiat> custom_fiats = fiat_deserializeArrayList1(o.getJSONArrayString("custom_fiats"));

        // This is a dummy object that only has to hold onto the fiat array lists.
        // We don't need to call the proper add* methods here.
        FiatManager fiatManager = UnknownFiatManager.createUnknownFiatManager(key, fiat_type);
        fiatManager.hardcoded_fiats = hardcoded_fiats;
        fiatManager.found_fiats = found_fiats;
        fiatManager.custom_fiats = custom_fiats;

        return fiatManager;
    }

    public static Fiat fiat_deserialize1(String s) {
        if(s == null) { return null; }

        try {
            Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
            String key = Serialization.string_deserialize(o.getString("key"));
            String name = Serialization.string_deserialize(o.getString("name"));
            String display_name = Serialization.string_deserialize(o.getString("display_name"));
            int scale = Serialization.int_deserialize(o.getString("scale"));
            String fiat_type = Serialization.string_deserialize(o.getString("fiat_type"));

            return Fiat.buildFiat(key, name, display_name, scale, fiat_type);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static ArrayList<Fiat> fiat_deserializeArrayList1(String s) {
        if(s == null) { return null; }

        try {
            ArrayList<Fiat> arrayList = new ArrayList<>();

            Serialization.JSONArrayWithNull a = new Serialization.JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                arrayList.add(fiat_deserialize1(o));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static FiatManager deserializeFromJSON2(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String key = Serialization.string_deserialize(o.getString("key"));
        String fiat_type = Serialization.string_deserialize(o.getString("fiat_type"));
        ArrayList<Fiat> hardcoded_fiats = Serialization.fiat_deserializeArrayList(o.getJSONArrayString("hardcoded_fiats"));
        ArrayList<Fiat> found_fiats = Serialization.fiat_deserializeArrayList(o.getJSONArrayString("found_fiats"));
        ArrayList<Fiat> custom_fiats = Serialization.fiat_deserializeArrayList(o.getJSONArrayString("custom_fiats"));

        // This is a dummy object that only has to hold onto the fiat array lists.
        // We don't need to call the proper add* methods here.
        FiatManager fiatManager = UnknownFiatManager.createUnknownFiatManager(key, fiat_type);
        fiatManager.hardcoded_fiats = hardcoded_fiats;
        fiatManager.found_fiats = found_fiats;
        fiatManager.custom_fiats = custom_fiats;

        return fiatManager;
    }
}
