package com.musicslayer.cryptobuddy.asset.tokenmanager;

import com.musicslayer.cryptobuddy.asset.crypto.token.Token;
import com.musicslayer.cryptobuddy.dialog.ProgressDialogFragment;
import com.musicslayer.cryptobuddy.util.FileUtil;
import com.musicslayer.cryptobuddy.util.StreamUtil;
import com.musicslayer.cryptobuddy.util.ThrowableUtil;
import com.musicslayer.cryptobuddy.util.WebUtil;
import com.musicslayer.cryptobuddy.util.ZipUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
            // Do this just to get the total number of tokens we expect.
            String fileArray = WebUtil.get("https://api.github.com/repos/cardano-foundation/cardano-token-registry/contents/mappings");
            JSONArray fileArrayJSON = new JSONArray(fileArray);

            final int[] progress_current = new int[1];
            int progress_total = fileArrayJSON.length();

            File file = FileUtil.downloadFile("https://api.github.com/repos/cardano-foundation/cardano-token-registry/zipball");

            StringBuilder json = new StringBuilder("[");

            ZipUtil.unzip(file, new ZipUtil.UnzipListener() {
                @Override
                public void onUnzip(ZipEntry zipEntry, ZipInputStream zin) throws IOException {
                    // If we have already cancelled, throw exception to stop unzip process.
                    if(ProgressDialogFragment.isCancelled()) {
                        throw new IOException();
                    }

                    // All the tokens are in the mappings folder.
                    if(!zipEntry.isDirectory() && zipEntry.getName().contains("/mappings/")) {
                        progress_current[0]++;
                        ProgressDialogFragment.reportProgress(progress_current[0], progress_total, getTokenType() + " Tokens Processed");

                        String fileContents = StreamUtil.readIntoString(zin);

                        // The logo takes up too much memory so we remove it here.
                        JSONObject tokenJSON;
                        try {
                            tokenJSON = new JSONObject(fileContents);
                            tokenJSON.remove("logo");
                        }
                        catch(Exception e) {
                            throw new IOException(e);
                        }

                        json.append(tokenJSON.toString());
                        json.append(",");
                    }
                }
            });

            // We are done with the zip file and it is rather large, so just delete it now.
            // Don't check the return value - if we do not succeed then the file will be deleted next time the app starts anyway.
            file.delete();

            // Get rid of last "," to avoid potential for a last "null" element being seen.
            json.deleteCharAt(json.length() - 1);
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