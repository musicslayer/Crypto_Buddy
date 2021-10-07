package com.musicslayer.cryptobuddy.asset.tokenmanager;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.crypto.token.UnknownToken;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.TokenManagerList;
import com.musicslayer.cryptobuddy.serialize.Serialization;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;
import com.musicslayer.cryptobuddy.util.ReflectUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class TokenManager implements Serialization.SerializableToJSON {
    public static ArrayList<TokenManager> tokenManagers;
    public static HashMap<String, TokenManager> tokenManagers_map;
    public static HashMap<String, TokenManager> tokenManagers_token_type_map;
    public static ArrayList<String> tokenManagers_names;
    public static ArrayList<String> tokenManagers_blockchain_ids;
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
    abstract public String getBlockchainID();
    abstract public String getTokenType();

    // Used to store persistent state
    abstract public String getSettingsKey();

    // Most times we cannot do these, but subclasses can override these if they can do any of them.
    public Token lookupToken(String baseURL, String id) { return null; }

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

    public static void initialize(Context context) {
        tokenManagers = new ArrayList<>();
        tokenManagers_map = new HashMap<>();
        tokenManagers_token_type_map = new HashMap<>();
        tokenManagers_blockchain_ids = new ArrayList<>();
        tokenManagers_token_types = new ArrayList<>();

        TokenManagerList.initializeRawArray();

        tokenManagers_names = FileUtil.readFileIntoLines(context, R.raw.asset_tokenmanager);
        for(String tokenManagerName : tokenManagers_names) {
            TokenManager tokenManager = ReflectUtil.constructClassInstanceFromName("com.musicslayer.cryptobuddy.asset.tokenmanager." + tokenManagerName);

            if(Purchases.isUnlockTokensPurchased) {
                // Use the deserialized dummy object to fill in the tokens in this real one.
                TokenManager copyTokenManager = TokenManagerList.loadData(context, tokenManager.getSettingsKey());

                // If this is a new TokenManager that wasn't previously saved, then there are no tokens to add.
                if(copyTokenManager != null) {
                    tokenManager.addDownloadedToken(copyTokenManager.downloaded_tokens);
                    tokenManager.addFoundToken(copyTokenManager.found_tokens);
                    tokenManager.addCustomToken(copyTokenManager.custom_tokens);
                }
            }

            tokenManagers.add(tokenManager);
            tokenManagers_map.put(tokenManager.getKey(), tokenManager);
            tokenManagers_token_type_map.put(tokenManager.getTokenType(), tokenManager);
            tokenManagers_blockchain_ids.add(tokenManager.getBlockchainID());
            tokenManagers_token_types.add(tokenManager.getTokenType());
        }
    }

    public static TokenManager getTokenManagerFromKey(String key) {
        TokenManager tokenManager = tokenManagers_map.get(key);
        if(tokenManager == null || !Purchases.isUnlockTokensPurchased) {
            tokenManager = UnknownTokenManager.createUnknownTokenManager(key, "?");
        }

        return tokenManager;
    }

    public static TokenManager getTokenManagerFromTokenType(String tokenType) {
        TokenManager tokenManager = tokenManagers_token_type_map.get(tokenType);
        if(tokenManager == null || !Purchases.isUnlockTokensPurchased) {
            tokenManager = UnknownTokenManager.createUnknownTokenManager("?", tokenType);
        }

        return tokenManager;
    }

    // Try to get token, but if we can't then create a new instance.
    public Token getOrCreateToken(String key, String name, String display_name, int scale, String id) {
        Token token = getTokenWithPrecedence(key);

        if(token == null) {
            token = new Token(key, name, display_name, scale, id, getBlockchainID(), getTokenType());
            addFoundToken(token);
        }

        return token;
    }

    public Token getOrLookupToken(String baseURL, String key, String name, String display_name, int scale, String id) {
        Token token = getTokenWithPrecedence(key);

        if(token == null) {
            token = lookupToken(baseURL, id);
        }

        if(token == null) {
            token = UnknownToken.createUnknownToken(key, name, display_name, scale, id, getBlockchainID(), getTokenType());
        }
        else {
            addFoundToken(token);
        }

        return token;
    }

    // Try to get token, but if we can't then return an UnknownToken instance.
    public Token getToken(String key, String name, String display_name, int scale, String id) {
        Token token = getTokenWithPrecedence(key);

        if(token == null) {
            token = UnknownToken.createUnknownToken(key, name, display_name, scale, id, getBlockchainID(), getTokenType());
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

    public void addDownloadedToken(ArrayList<Token> tokenArrayList) {
        if(tokenArrayList == null) { return; }

        for(Token token : tokenArrayList) {
            addDownloadedToken(token);
        }
    }

    public void addDownloadedToken(Token token) {
        if(token == null) { return; }

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
        if(token == null) { return; }

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
        if(token == null) { return; }

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

    public void resetDownloadedTokens() {
        downloaded_tokens = new ArrayList<>();
        downloaded_token_map = new HashMap<>();
        downloaded_token_names = new ArrayList<>();
        downloaded_token_display_names = new ArrayList<>();
    }

    public void resetFoundTokens() {
        found_tokens = new ArrayList<>();
        found_token_map = new HashMap<>();
        found_token_names = new ArrayList<>();
        found_token_display_names = new ArrayList<>();
    }

    public void resetCustomTokens() {
        custom_tokens = new ArrayList<>();
        custom_token_map = new HashMap<>();
        custom_token_names = new ArrayList<>();
        custom_token_display_names = new ArrayList<>();
    }

    public static void resetAllDownloadedTokens() {
        for(TokenManager tokenManager : tokenManagers) {
            tokenManager.resetDownloadedTokens();
        }
    }

    public static void resetAllFoundTokens() {
        for(TokenManager tokenManager : tokenManagers) {
            tokenManager.resetFoundTokens();
        }
    }

    public static void resetAllCustomTokens() {
        for(TokenManager tokenManager : tokenManagers) {
            tokenManager.resetCustomTokens();
        }
    }

    public ArrayList<Token> getTokens() {
        ArrayList<Token> tokens = new ArrayList<>();

        tokens.addAll(downloaded_tokens);
        tokens.addAll(found_tokens);
        tokens.addAll(custom_tokens);

        return tokens;
    }

    public ArrayList<String> getTokenNames() {
        ArrayList<String> names = new ArrayList<>();

        names.addAll(downloaded_token_names);
        names.addAll(found_token_names);
        names.addAll(custom_token_names);

        return names;
    }

    public ArrayList<String> getTokenDisplayNames() {
        ArrayList<String> displayNames = new ArrayList<>();

        displayNames.addAll(downloaded_token_display_names);
        displayNames.addAll(found_token_display_names);
        displayNames.addAll(custom_token_display_names);

        return displayNames;
    }

    public static ArrayList<Token> getAllTokens() {
        ArrayList<Token> tokens = new ArrayList<>();

        for(TokenManager tokenManager : tokenManagers) {
            tokens.addAll(tokenManager.downloaded_tokens);
            tokens.addAll(tokenManager.found_tokens);
            tokens.addAll(tokenManager.custom_tokens);
        }

        return tokens;
    }

    public static ArrayList<String> getAllTokenNames() {
        ArrayList<String> names = new ArrayList<>();

        for(TokenManager tokenManager : tokenManagers) {
            names.addAll(tokenManager.downloaded_token_names);
            names.addAll(tokenManager.found_token_names);
            names.addAll(tokenManager.custom_token_names);
        }

        return names;
    }

    public static ArrayList<String> getAllTokenDisplayNames() {
        ArrayList<String> displayNames = new ArrayList<>();

        for(TokenManager tokenManager : tokenManagers) {
            displayNames.addAll(tokenManager.downloaded_token_display_names);
            displayNames.addAll(tokenManager.found_token_display_names);
            displayNames.addAll(tokenManager.custom_token_display_names);
        }

        return displayNames;
    }

    public String getFixedJSON() {
        return RESTUtil.get("https://raw.githubusercontent.com/musicslayer/token_hub/main/token_info/" + getSettingsKey());
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


                Token token = new Token(key, name, display_name, scale, id, getBlockchainID(), getTokenType());
                addDownloadedToken(token);
            }

            return true;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return false;
        }
    }

    public String serializationVersion() { return "1"; }

    public String serializeToJSON() throws org.json.JSONException {
        // Just serialize the token array lists. TokenManagerList keeps track of which TokenManager had these.
        return new Serialization.JSONObjectWithNull()
            .put("key", Serialization.string_serialize(getKey()))
            .put("token_type", Serialization.string_serialize(getTokenType()))
            .put("downloaded_tokens", new Serialization.JSONArrayWithNull(Serialization.token_serializeArrayList(downloaded_tokens)))
            .put("found_tokens", new Serialization.JSONArrayWithNull(Serialization.token_serializeArrayList(found_tokens)))
            .put("custom_tokens", new Serialization.JSONArrayWithNull(Serialization.token_serializeArrayList(custom_tokens)))
            .toStringOrNull();
    }

    public static TokenManager deserializeFromJSON1(String s) throws org.json.JSONException {
        Serialization.JSONObjectWithNull o = new Serialization.JSONObjectWithNull(s);
        String key = Serialization.string_deserialize(o.getString("key"));
        String token_type = Serialization.string_deserialize(o.getString("token_type"));
        ArrayList<Token> downloaded_tokens = Serialization.token_deserializeArrayList(o.getJSONArrayString("downloaded_tokens"));
        ArrayList<Token> found_tokens = Serialization.token_deserializeArrayList(o.getJSONArrayString("found_tokens"));
        ArrayList<Token> custom_tokens = Serialization.token_deserializeArrayList(o.getJSONArrayString("custom_tokens"));

        // This is a dummy object that only has to hold onto the token array lists.
        // We don't need to call the proper add* methods here.
        TokenManager tokenManager = UnknownTokenManager.createUnknownTokenManager(key, token_type);
        tokenManager.downloaded_tokens = downloaded_tokens;
        tokenManager.found_tokens = found_tokens;
        tokenManager.custom_tokens = custom_tokens;

        return tokenManager;
    }
}
