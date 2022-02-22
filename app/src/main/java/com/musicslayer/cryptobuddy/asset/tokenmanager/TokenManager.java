package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.api.address.CryptoAddress;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.crypto.token.UnknownToken;
import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.data.bridge.StreamDataBridge;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.data.persistent.app.Purchases;
import com.musicslayer.cryptobuddy.json.JSONWithNull;
import com.musicslayer.cryptobuddy.data.bridge.Serialization;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

abstract public class TokenManager implements Serialization.SerializableToJSON, Serialization.Versionable {
    public static ArrayList<TokenManager> tokenManagers;
    public static HashMap<String, TokenManager> tokenManagers_map;
    public static HashMap<String, TokenManager> tokenManagers_token_type_map;
    public static ArrayList<String> tokenManagers_names;
    public static ArrayList<String> tokenManagers_token_types;

    public ArrayList<Token> downloaded_tokens;
    public HashMap<String, Token> downloaded_token_map;
    public ArrayList<String> downloaded_token_names;
    public ArrayList<String> downloaded_token_display_names;

    public ArrayList<Token> found_tokens;
    public HashMap<String, Token> found_token_map;
    public ArrayList<String> found_token_names;
    public ArrayList<String> found_token_display_names;

    public ArrayList<Token> custom_tokens;
    public HashMap<String, Token> custom_token_map;
    public ArrayList<String> custom_token_names;
    public ArrayList<String> custom_token_display_names;

    abstract public String getKey();
    abstract public String getName();
    abstract public String getCoinGeckoBlockchainID();
    abstract public String getTokenType();

    // Used to store persistent state
    abstract public String getSettingsKey();

    // Most times we cannot do lookup, but subclasses can override if they can.
    public Token lookupToken(CryptoAddress cryptoAddress, String key, String name, String display_name, int scale, String id) { return null; }

    public boolean canGetJSON() { return false; }
    public String getJSON() { return null; }
    public boolean parse(String tokenJSON) { return true; }

    public TokenManager() {
        this.downloaded_tokens = new ArrayList<>();
        this.downloaded_token_map = new HashMap<>();
        this.downloaded_token_names = new ArrayList<>();
        this.downloaded_token_display_names = new ArrayList<>();

        this.found_tokens = new ArrayList<>();
        this.found_token_map = new HashMap<>();
        this.found_token_names = new ArrayList<>();
        this.found_token_display_names = new ArrayList<>();

        this.custom_tokens = new ArrayList<>();
        this.custom_token_map = new HashMap<>();
        this.custom_token_names = new ArrayList<>();
        this.custom_token_display_names = new ArrayList<>();
    }

    public static void initialize() {
        tokenManagers = new ArrayList<>();
        tokenManagers_map = new HashMap<>();
        tokenManagers_token_type_map = new HashMap<>();
        tokenManagers_token_types = new ArrayList<>();

        tokenManagers_names = FileUtil.readFileIntoLines(R.raw.asset_tokenmanager);
        for(String tokenManagerName : tokenManagers_names) {
            TokenManager tokenManager = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.tokenmanager." + tokenManagerName);

            tokenManagers.add(tokenManager);
            tokenManagers_map.put(tokenManager.getKey(), tokenManager);
            tokenManagers_token_type_map.put(tokenManager.getTokenType(), tokenManager);
            tokenManagers_token_types.add(tokenManager.getTokenType());
        }
    }

    public static TokenManager getTokenManagerFromKey(String key) {
        TokenManager tokenManager = tokenManagers_map.get(key);
        if(tokenManager == null || !Purchases.isUnlockTokensPurchased()) {
            tokenManager = UnknownTokenManager.createUnknownTokenManager(key, "?");
        }

        return tokenManager;
    }

    public static TokenManager getTokenManagerFromTokenType(String tokenType) {
        TokenManager tokenManager = tokenManagers_token_type_map.get(tokenType);
        if(tokenManager == null || !Purchases.isUnlockTokensPurchased()) {
            tokenManager = UnknownTokenManager.createUnknownTokenManager("?", tokenType);
        }

        return tokenManager;
    }

    public Token getToken(CryptoAddress cryptoAddress, String key, String name, String display_name, int scale, String id) {
        // Try to get the token from storage, then try to look it up, then try to create it from the input information.
        // If all of that fails, then return an UnknownToken instance.
        Token token = getTokenWithPrecedence(key);

        if(token == null) {
            token = lookupToken(cryptoAddress, key, name, display_name, scale, id);

            if(token == null || !token.isComplete()) {
                token = Token.buildToken(key, name, display_name, scale, getTokenType(), id, getCoinGeckoBlockchainID());

                if(!token.isComplete()) {
                    token = UnknownToken.createUnknownToken(key, name, display_name, scale, getTokenType(), id, getCoinGeckoBlockchainID());
                }
            }

            addFoundToken(token);
        }

        return token;
    }

    public Token getExistingToken(String key, String name, String display_name, int scale, HashMap<String, String> additionalInfo) {
        // Only return a stored token. Do not build or lookup one.
        Token token = getTokenWithPrecedence(key);
        if(token == null) {
            token = UnknownToken.createUnknownToken(key, name, display_name, scale, getTokenType(), additionalInfo);
        }

        return token;
    }

    private Token getTokenWithPrecedence(String key) {
        // Maintain precedence - Downloaded, then Found, then Custom Tokens.
        Token token = downloaded_token_map.get(key);
        if(token == null) {
            token = found_token_map.get(key);
        }
        if(token == null) {
            token = custom_token_map.get(key);
        }

        return token;
    }

    public void removeDownloadedToken(Token token) {
        String key = token.getKey();
        if(downloaded_token_map.get(key) != null) {
            downloaded_tokens.remove(token);
            HashMapUtil.removeValueFromMap(downloaded_token_map, key);
            downloaded_token_names.remove(token.getName());
            downloaded_token_display_names.remove(token.getDisplayName());
        }
    }

    public void removeFoundToken(Token token) {
        String key = token.getKey();
        if(found_token_map.get(key) != null) {
            found_tokens.remove(token);
            HashMapUtil.removeValueFromMap(found_token_map, key);
            found_token_names.remove(token.getName());
            found_token_display_names.remove(token.getDisplayName());
        }
    }

    public void removeCustomToken(Token token) {
        String key = token.getKey();
        if(custom_token_map.get(key) != null) {
            custom_tokens.remove(token);
            HashMapUtil.removeValueFromMap(custom_token_map, key);
            custom_token_names.remove(token.getName());
            custom_token_display_names.remove(token.getDisplayName());
        }
    }

    public void addDownloadedToken(ArrayList<Token> tokenArrayList) {
        if(tokenArrayList == null) { return; }

        for(Token token : tokenArrayList) {
            addDownloadedToken(token);
        }
    }

    public void addDownloadedToken(Token token) {
        if(token == null || !token.isComplete()) { return; }

        String key = token.getKey();

        // Add or replace token.
        if(downloaded_token_map.get(key) == null) {
            downloaded_tokens.add(token);
            downloaded_token_map.put(key, token);
            downloaded_token_names.add(token.getName());
            downloaded_token_display_names.add(token.getDisplayName());
        }
        else {
            downloaded_token_map.put(key, token);

            int idx = downloaded_tokens.indexOf(token);
            downloaded_tokens.set(idx, token);
            downloaded_token_names.set(idx, token.getName());
            downloaded_token_display_names.set(idx, token.getDisplayName());
        }
    }

    public void addFoundToken(ArrayList<Token> tokenArrayList) {
        if(tokenArrayList == null) { return; }

        for(Token token : tokenArrayList) {
            addFoundToken(token);
        }
    }

    public void addFoundToken(Token token) {
        if(token == null || !token.isComplete()) { return; }

        String key = token.getKey();

        // Add or replace token.
        if(found_token_map.get(key) == null) {
            found_tokens.add(token);
            found_token_map.put(key, token);
            found_token_names.add(token.getName());
            found_token_display_names.add(token.getDisplayName());
        }
        else {
            found_token_map.put(key, token);

            int idx = found_tokens.indexOf(token);
            found_tokens.set(idx, token);
            found_token_names.set(idx, token.getName());
            found_token_display_names.set(idx, token.getDisplayName());
        }
    }

    public void addCustomToken(ArrayList<Token> tokenArrayList) {
        if(tokenArrayList == null) { return; }

        for(Token token : tokenArrayList) {
            addCustomToken(token);
        }
    }

    public void addCustomToken(Token token) {
        if(token == null || !token.isComplete()) { return; }

        String key = token.getKey();

        // Add or replace token.
        if(custom_token_map.get(key) == null) {
            custom_tokens.add(token);
            custom_token_map.put(key, token);
            custom_token_names.add(token.getName());
            custom_token_display_names.add(token.getDisplayName());
        }
        else {
            custom_token_map.put(key, token);

            int idx = custom_tokens.indexOf(token);
            custom_tokens.set(idx, token);
            custom_token_names.set(idx, token.getName());
            custom_token_display_names.set(idx, token.getDisplayName());
        }
    }

    public static void resetAllTokens() {
        resetAllDownloadedTokens();
        resetAllFoundTokens();
        resetAllCustomTokens();
    }

    public static void resetAllDownloadedTokens() {
        for(TokenManager tokenManager : tokenManagers) {
            tokenManager.resetDownloadedTokens();
        }
    }

    public void resetDownloadedTokens() {
        downloaded_tokens = new ArrayList<>();
        downloaded_token_map = new HashMap<>();
        downloaded_token_names = new ArrayList<>();
        downloaded_token_display_names = new ArrayList<>();
    }

    public static void resetAllFoundTokens() {
        for(TokenManager tokenManager : tokenManagers) {
            tokenManager.resetFoundTokens();
        }
    }

    public void resetFoundTokens() {
        found_tokens = new ArrayList<>();
        found_token_map = new HashMap<>();
        found_token_names = new ArrayList<>();
        found_token_display_names = new ArrayList<>();
    }

    public static void resetAllCustomTokens() {
        for(TokenManager tokenManager : tokenManagers) {
            tokenManager.resetCustomTokens();
        }
    }

    public void resetCustomTokens() {
        custom_tokens = new ArrayList<>();
        custom_token_map = new HashMap<>();
        custom_token_names = new ArrayList<>();
        custom_token_display_names = new ArrayList<>();
    }

    public static ArrayList<Token> getAllTokens() {
        ArrayList<Token> tokens = new ArrayList<>();

        for(TokenManager tokenManager : tokenManagers) {
            tokens.addAll(tokenManager.getTokens());
        }

        return tokens;
    }

    public ArrayList<Token> getTokens() {
        // Here we take into account precedence by favoring downloaded tokens over found tokens over custom tokens.
        // We don't actually delete the shadowed tokens from the TokenManager, we merely don't add them to the list this method returns.
        ArrayList<Token> tokens = new ArrayList<>();

        // Found tokens -> remove downloaded keys
        ArrayList<Token> copy_found_tokens = new ArrayList<>(found_tokens);
        copy_found_tokens.removeAll(downloaded_tokens);

        // Custom tokens -> remove downloaded/found keys
        ArrayList<Token> copy_custom_tokens = new ArrayList<>(custom_tokens);
        copy_custom_tokens.removeAll(downloaded_tokens);
        copy_custom_tokens.removeAll(copy_found_tokens);

        tokens.addAll(downloaded_tokens);
        tokens.addAll(copy_found_tokens);
        tokens.addAll(copy_custom_tokens);

        return tokens;
    }

    public static ArrayList<String> getAllTokenNames() {
        ArrayList<String> names = new ArrayList<>();

        for(TokenManager tokenManager : tokenManagers) {
            names.addAll(tokenManager.getTokenNames());
        }

        return names;
    }

    public ArrayList<String> getTokenNames() {
        ArrayList<String> names = new ArrayList<>();

        for(Token token : getTokens()) {
            names.add(token.getName());
        }

        return names;
    }

    public static ArrayList<String> getAllTokenDisplayNames() {
        ArrayList<String> displayNames = new ArrayList<>();

        for(TokenManager tokenManager : tokenManagers) {
            displayNames.addAll(tokenManager.getTokenDisplayNames());
        }

        return displayNames;
    }

    public ArrayList<String> getTokenDisplayNames() {
        ArrayList<String> displayNames = new ArrayList<>();

        for(Token token : getTokens()) {
            displayNames.add(token.getDisplayName());
        }

        return displayNames;
    }

    public String getFixedJSON() {
        ProgressDialogFragment.updateProgressTitle("Downloading " + getTokenType() + " Tokens...");
        return WebUtil.get("https://raw.githubusercontent.com/musicslayer/token_hub/main/token_info/" + getSettingsKey());
    }

    public boolean parseFixed(String tokenJSON) {
        try {
            JSONObject jsonObject = new JSONObject(tokenJSON);
            JSONArray jsonArray = jsonObject.getJSONArray("tokens");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String key = json.getString("key");
                String name = json.getString("name");
                String display_name = json.getString("display_name");
                int scale = json.getInt("scale");
                String id = json.getString("id");

                Token token = Token.buildToken(key, name, display_name, scale, getTokenType(), id, getCoinGeckoBlockchainID());
                addDownloadedToken(token);
            }

            return true;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return false;
        }
    }

    public static String serializationVersion() {
        return "2";
    }

    public static String serializationType(String version) {
        return "!OBJECT!";
    }

    @Override
    public String serializeToJSON() throws org.json.JSONException {
        String s = new DataBridge.JSONObjectDataBridge()
            .serialize("key", getKey(), String.class)
            .serialize("token_type", getTokenType(), String.class)
            .serializeArrayList("downloaded_tokens", downloaded_tokens, Token.class)
            .serializeArrayList("found_tokens", found_tokens, Token.class)
            .serializeArrayList("custom_tokens", custom_tokens, Token.class)
            .toStringOrNull();

        return s;
    }

    public void serializeToJSONX(StreamDataBridge.JSONStreamDataBridge o) throws IOException {
        o.beginObject()
                .serialize("!V!", "2", String.class)
                .serialize("key", getKey(), String.class)
                .serialize("token_type", getTokenType(), String.class)
                .serializeArrayList("downloaded_tokens", downloaded_tokens, Token.class)
                .serializeArrayList("found_tokens", found_tokens, Token.class)
                .serializeArrayList("custom_tokens", custom_tokens, Token.class)
                .endObject();
    }

    public static TokenManager deserializeFromJSONX(StreamDataBridge.JSONStreamDataBridge o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        //String version = "2";
        TokenManager tokenManager;

        if("2".equals(version)) {
            String key = o.deserialize("key", String.class);
            String token_type = o.deserialize("token_type", String.class);
            ArrayList<Token> downloaded_tokens = o.deserializeArrayList("downloaded_tokens", Token.class);
            ArrayList<Token> found_tokens = o.deserializeArrayList("found_tokens", Token.class);
            ArrayList<Token> custom_tokens = o.deserializeArrayList("custom_tokens", Token.class);
            //o.finish();

            //o.deserialize("!V!", String.class);

            o.endObject();

            // This is a dummy object that only has to hold onto the token array lists.
            // We don't need to call the proper add* methods here.
            tokenManager = UnknownTokenManager.createUnknownTokenManager(key, token_type);
            tokenManager.downloaded_tokens = downloaded_tokens;
            tokenManager.found_tokens = found_tokens;
            tokenManager.custom_tokens = custom_tokens;
        }
        else {
            throw new IllegalStateException();
        }

        return tokenManager;
    }

    public static TokenManager deserializeFromJSON(String s, String version) throws org.json.JSONException {
        TokenManager tokenManager;

        if("2".equals(version)) {
            DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
            String key = o.deserialize("key", String.class);
            String token_type = o.deserialize("token_type", String.class);
            ArrayList<Token> downloaded_tokens = o.deserializeArrayList("downloaded_tokens", Token.class);
            ArrayList<Token> found_tokens = o.deserializeArrayList("found_tokens", Token.class);
            ArrayList<Token> custom_tokens = o.deserializeArrayList("custom_tokens", Token.class);

            // This is a dummy object that only has to hold onto the token array lists.
            // We don't need to call the proper add* methods here.
            tokenManager = UnknownTokenManager.createUnknownTokenManager(key, token_type);
            tokenManager.downloaded_tokens = downloaded_tokens;
            tokenManager.found_tokens = found_tokens;
            tokenManager.custom_tokens = custom_tokens;
        }
        else if("1".equals(version)) {
            DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
            String key = o.deserialize("key", String.class);
            String token_type = o.deserialize("token_type", String.class);

            // We have to manually deserialize this legacy data.
            ArrayList<Token> downloaded_tokens = legacy_token_deserializeArrayList(o.getJSONArrayString("downloaded_tokens"));
            ArrayList<Token> found_tokens = legacy_token_deserializeArrayList(o.getJSONArrayString("found_tokens"));
            ArrayList<Token> custom_tokens = legacy_token_deserializeArrayList(o.getJSONArrayString("custom_tokens"));

            // This is a dummy object that only has to hold onto the token array lists.
            // We don't need to call the proper add* methods here.
            tokenManager = UnknownTokenManager.createUnknownTokenManager(key, token_type);
            tokenManager.downloaded_tokens = downloaded_tokens;
            tokenManager.found_tokens = found_tokens;
            tokenManager.custom_tokens = custom_tokens;

        }
        else {
            throw new IllegalStateException();
        }

        return tokenManager;
    }

    public static Token legacy_token_deserialize(String s) {
        if(s == null) { return null; }

        try {
            DataBridge.JSONObjectDataBridge o = new DataBridge.JSONObjectDataBridge(s);
            String key = o.deserialize("key", String.class);
            String name = o.deserialize("name", String.class);
            String display_name = o.deserialize("display_name", String.class);
            int scale = o.deserialize("scale", Integer.class);
            String id = o.deserialize("id", String.class);
            String blockchain_id = o.deserialize("blockchain_id", String.class);
            String token_type = o.deserialize("token_type", String.class);

            return Token.buildToken(key, name, display_name, scale, token_type, id, blockchain_id);
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }

    public static ArrayList<Token> legacy_token_deserializeArrayList(String s) {
        if(s == null) { return null; }

        try {
            ArrayList<Token> arrayList = new ArrayList<>();

            JSONWithNull.JSONArrayWithNull a = new JSONWithNull.JSONArrayWithNull(s);
            for(int i = 0; i < a.length(); i++) {
                String o = a.getString(i);
                arrayList.add(legacy_token_deserialize(o));
            }

            return arrayList;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            throw new IllegalStateException(e);
        }
    }
}
