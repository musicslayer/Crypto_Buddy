package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.ZipUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

// Token Registry:
// https://github.com/cardano-foundation/cardano-token-registry/

public class ADATokenManager extends TokenManager {
    public String getKey() { return "ADATokenManager"; }
    public String getName() { return "ADATokenManager"; }
    public String getBlockchainID() { return "cardano"; }
    public String getTokenType() { return "ADA"; }
    public String getSettingsKey() { return "ada"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        ProgressDialogFragment.updateProgressSubtitle("Downloading " + getTokenType() + " Tokens...");

        try {
            File file = FileUtil.downloadFile("https://api.github.com/repos/cardano-foundation/cardano-token-registry/zipball");
            HashMap<String, String> zipMap = ZipUtil.unzip(file, "mappings");

            // Each file contains the info for one token.
            ArrayList<String> tokenInfoArray = new ArrayList<>(zipMap.values());

            StringBuilder json = new StringBuilder("[");
            for(int i = 0; i < tokenInfoArray.size(); i++) {
                json.append(tokenInfoArray.get(i));

                if(i < tokenInfoArray.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            return json.toString();
        }
        catch(Exception ignored) {
            return null;
        }
    }

    public boolean parse(String tokenJSON) {
        try {
            JSONArray jsonArray = new JSONArray(tokenJSON);
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject tokenInfo = jsonArray.getJSONObject(i);

                String display_name = tokenInfo.getJSONObject("name").getString("value");

                String name;
                if(tokenInfo.has("ticker")) {
                    name = tokenInfo.getJSONObject("ticker").getString("value");
                }
                else {
                    name = display_name;
                }

                int scale;
                if(tokenInfo.has("decimals")) {
                    scale = tokenInfo.getJSONObject("decimals").getInt("value");
                }
                else {
                    scale = 0;
                }

                String id = tokenInfo.getString("subject");
                String blockchain_id = "cardano";
                String token_type = "ADA";
                String key = id;

                Token token = new Token(key, name, display_name, scale, id, blockchain_id, token_type);
                addDownloadedToken(token);
            }

            return true;
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
            return false;
        }
    }
}