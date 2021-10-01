package com.musicslayer.cryptobuddy.asset.tokenmanager;

import android.content.Context;

import com.musicslayer.cryptobuddy.R;
import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.asset.crypto.token.UnknownToken;
import com.musicslayer.cryptobuddy.persistence.Purchases;
import com.musicslayer.cryptobuddy.persistence.TokenList;
import com.musicslayer.cryptobuddy.util.ExceptionLogger;
import com.musicslayer.cryptobuddy.util.File;
import com.musicslayer.cryptobuddy.util.REST;
import com.musicslayer.cryptobuddy.util.Reflect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class TokenManager {
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
    public void parse(String tokenJSON) {}

    public static void initialize(Context context) {
        tokenManagers = new ArrayList<>();
        tokenManagers_map = new HashMap<>();
        tokenManagers_token_type_map = new HashMap<>();
        tokenManagers_blockchain_ids = new ArrayList<>();
        tokenManagers_token_types = new ArrayList<>();

        tokenManagers_names = File.readFileIntoLines(context, R.raw.asset_tokenmanager);
        for(String tokenManagerName : tokenManagers_names) {
            TokenManager tokenManager = Reflect.constructSubclassInstanceFromName("com.musicslayer.cryptobuddy.asset.tokenmanager." + tokenManagerName);
            tokenManagers.add(tokenManager);
            tokenManagers_map.put(tokenManager.getKey(), tokenManager);
            tokenManagers_token_type_map.put(tokenManager.getTokenType(), tokenManager);
            tokenManagers_blockchain_ids.add(tokenManager.getBlockchainID());
            tokenManagers_token_types.add(tokenManager.getTokenType());

            tokenManager.downloaded_tokens = new ArrayList<>();
            tokenManager.downloaded_token_map = new HashMap<>();
            tokenManager.downloaded_token_names = new ArrayList<>();
            tokenManager.downloaded_token_display_names = new ArrayList<>();

            tokenManager.found_tokens = new ArrayList<>();
            tokenManager.found_token_map = new HashMap<>();
            tokenManager.found_token_names = new ArrayList<>();
            tokenManager.found_token_display_names = new ArrayList<>();

            tokenManager.custom_tokens = new ArrayList<>();
            tokenManager.custom_token_map = new HashMap<>();
            tokenManager.custom_token_names = new ArrayList<>();
            tokenManager.custom_token_display_names = new ArrayList<>();
        }

        if(Purchases.isUnlockTokensPurchased) {
            // Load data stored in the settings.
            loadAll(context, "downloaded");
            loadAll(context, "found");
            loadAll(context, "custom");
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
            downloaded_tokens.remove(idx);
            downloaded_tokens.add(idx, token);

            downloaded_token_names.remove(idx);
            downloaded_token_names.add(idx, token.getName());

            downloaded_token_display_names.remove(idx);
            downloaded_token_display_names.add(idx, token.getDisplayName());
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
            found_tokens.remove(idx);
            found_tokens.add(idx, token);

            found_token_names.remove(idx);
            found_token_names.add(idx, token.getName());

            found_token_display_names.remove(idx);
            found_token_display_names.add(idx, token.getDisplayName());
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
            custom_tokens.remove(idx);
            custom_tokens.add(idx, token);

            custom_token_names.remove(idx);
            custom_token_names.add(idx, token.getName());

            custom_token_display_names.remove(idx);
            custom_token_display_names.add(idx, token.getDisplayName());
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

    public String serializeToJSON(String source) {
        StringBuilder s = new StringBuilder("[");

        if("downloaded".equals(source)) {
            for(int i = 0; i < downloaded_tokens.size(); i++) {
                Token token = downloaded_tokens.get(i);

                s.append(token.serializeToJSON());

                if(i < downloaded_tokens.size() - 1) {
                    s.append(",");
                }
            }
        }
        else if("found".equals(source)) {
            for(int i = 0; i < found_tokens.size(); i++) {
                Token token = found_tokens.get(i);

                s.append(token.serializeToJSON());

                if(i < found_tokens.size() - 1) {
                    s.append(",");
                }
            }
        }
        else if("custom".equals(source)) {
            for(int i = 0; i < custom_tokens.size(); i++) {
                Token token = custom_tokens.get(i);

                s.append(token.serializeToJSON());

                if(i < custom_tokens.size() - 1) {
                    s.append(",");
                }
            }
        }

        s.append("]");

        return s.toString();
    }

    // Don't actually create a new instance, just fill in tokens in this existing instance.
    public void deserializeFromJSON(String s, String source) {
        try {
            JSONArray json = new JSONArray(s);

            if("downloaded".equals(source)) {
                for(int i = 0; i < json.length(); i++) {
                    JSONObject tokenJSON = json.getJSONObject(i);
                    addDownloadedToken(Token.deserializeFromJSON(tokenJSON));
                }
            }
            else if("found".equals(source)) {
                for(int i = 0; i < json.length(); i++) {
                    JSONObject tokenJSON = json.getJSONObject(i);
                    addFoundToken(Token.deserializeFromJSON(tokenJSON));
                }
            }
            else if("custom".equals(source)) {
                for(int i = 0; i < json.length(); i++) {
                    JSONObject tokenJSON = json.getJSONObject(i);
                    addCustomToken(Token.deserializeFromJSON(tokenJSON));
                }
            }
        }
        catch(Exception ignored) {
            // If there is any problem at all, just wipe everything clean.
            resetDownloadedTokens();
            resetFoundTokens();
            resetCustomTokens();
        }
    }

    public static void saveAll(Context context, String source) {
        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            tokenManager.save(context, source);
        }
    }

    public void save(Context context, String source) {
        TokenList.set(context, getSettingsKey(), source, serializeToJSON(source));
    }

    public static void loadAll(Context context, String source) {
        for(TokenManager tokenManager : TokenManager.tokenManagers) {
            tokenManager.load(context, source);
        }
    }

    public void load(Context context, String source) {
        String s = TokenList.get(context, getSettingsKey(), source);
        deserializeFromJSON(s, source);
    }

    public String getFixedJSON() {
        return REST.get("https://raw.githubusercontent.com/musicslayer/token_hub/main/token_info/" + getSettingsKey());
    }

    public void parseFixed(String tokenJSON) {
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
        }
        catch(Exception e) {
            ExceptionLogger.processException(e);
        }
    }
}
