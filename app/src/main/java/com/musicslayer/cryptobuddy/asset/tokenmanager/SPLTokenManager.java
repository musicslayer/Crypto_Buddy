package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.RESTUtil;

import org.json.JSONArray;
import org.json.JSONObject;

// Token Program ID: TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA

public class SPLTokenManager extends TokenManager {
    public String getKey() { return "SPLTokenManager"; }
    public String getName() { return "SPLTokenManager"; }
    public String getBlockchainID() { return "solana"; }
    public String getTokenType() { return "SOL - SPL"; }
    public String getSettingsKey() { return "sol_spl"; }

    public boolean canGetJSON() { return true; }

    public String getJSON() {
        return RESTUtil.get("https://raw.githubusercontent.com/solana-labs/token-list/main/src/tokens/solana.tokenlist.json");
    }

    public void parse(String tokenJSON) {
        if("{}".equals(tokenJSON)) { return; }

        try {
            JSONObject jsonData = new JSONObject(tokenJSON);
            JSONArray jsonArray = jsonData.getJSONArray("tokens");
            for(int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);

                String name = json.getString("symbol");
                String display_name = json.getString("name");
                int scale = json.getInt("decimals");
                String id = json.getString("address");
                String blockchain_id = "solana";
                String token_type = "SOL - SPL";
                String key = id;

                Token token = new Token(key, name, display_name, scale, id, blockchain_id, token_type);
                addDownloadedToken(token);
            }
        }
        catch(Exception e) {
            ThrowableUtil.processThrowable(e);
        }
    }
}