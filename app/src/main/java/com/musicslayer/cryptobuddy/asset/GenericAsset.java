package com.musicslayer.cryptobuddy.asset;

import java.util.HashMap;

// This asset could be any kind.

public class GenericAsset extends Asset {
    public String original_key;
    public String original_name;
    public String original_display_name;
    public int original_scale;
    public String original_asset_type;
    public HashMap<String, String> original_additional_info;

    public String key;
    public String name;
    public String display_name;
    public String combo_name;
    public int scale;
    public String asset_type;
    public HashMap<String, String> additional_info;

    public static GenericAsset createGenericAsset(String key, String name, String display_name, int scale, String asset_type) {
        HashMap<String, String> additional_info = new HashMap<>();
        return createGenericAsset(key, name, display_name, scale, asset_type, additional_info);
    }

    public static GenericAsset createGenericAsset(String key, String name, String display_name, int scale, String asset_type, HashMap<String, String> additional_info) {
        return new GenericAsset(key, name, display_name, scale, asset_type, additional_info);
    }

    private GenericAsset(String key, String name, String display_name, int scale, String asset_type, HashMap<String, String> additional_info) {
        this.original_key = key;
        this.original_name = name;
        this.original_display_name = display_name;
        this.original_scale = scale;
        this.original_asset_type = asset_type;
        this.original_additional_info = additional_info;

        // Modify everything to be non-null.
        if(key == null) {
            key = "?";
        }
        this.key = key;

        if(name == null) {
            name = "?";
        }
        this.name = name;

        if(display_name == null) {
            display_name = "?";
        }
        this.display_name = display_name;

        this.scale = scale;

        if(asset_type == null) {
            asset_type = "?";
        }
        this.asset_type = asset_type;

        if(additional_info == null) {
            additional_info = new HashMap<>();
        }
        this.additional_info = additional_info;

        // Further modify names for display purposes.
        modifyNames(this.name, this.display_name);
    }

    public void modifyNames(String name, String displayName) {
        // For now, don't add types.
        this.combo_name = displayName + " (" + name + ")";
    }

    public String getOriginalKey() { return original_key; }
    public String getOriginalName() { return original_name; }
    public String getOriginalDisplayName() { return original_display_name; }
    public int getOriginalScale() { return original_scale; }
    public String getOriginalAssetType() { return original_asset_type; }
    public HashMap<String, String> getOriginalAdditionalInfo() { return original_additional_info; }

    public String getKey() { return key; }
    public String getName() { return name; }
    public String getDisplayName() { return display_name; }
    public String getComboName() { return combo_name; }
    public int getScale() { return scale; }
    public String getAssetType() { return asset_type; }
    public HashMap<String, String> getAdditionalInfo() { return additional_info; }

    // This is a special type of asset that could be any one of the other 3.
    public String getAssetKind() { return "!GENERIC!"; }
}
