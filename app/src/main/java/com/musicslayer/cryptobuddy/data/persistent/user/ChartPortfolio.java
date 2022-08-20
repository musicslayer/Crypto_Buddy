package com.musicslayer.cryptobuddy.data.persistent.user;

import android.content.SharedPreferences;

import com.musicslayer.cryptobuddy.data.bridge.DataBridge;
import com.musicslayer.cryptobuddy.util.HashMapUtil;
import com.musicslayer.cryptobuddy.util.SharedPreferencesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ChartPortfolio extends PersistentUserDataStore implements DataBridge.ExportableToJSON {
    public String getName() { return "ChartPortfolio"; }

    public boolean isVisible() { return true; }
    public String doExport() { return DataBridge.exportData(this, ChartPortfolio.class); }
    public void doImport(String s) { DataBridge.importData(this, s, ChartPortfolio.class); }

    // This default will cause an error when deserialized. We should never see this value used.
    public final static String DEFAULT = "null";

    public static ArrayList<String> settings_chart_portfolio_names = new ArrayList<>();

    public String getSharedPreferencesKey() {
        return "chart_portfolio_data";
    }

    public static boolean isSaved(String name) {
        return settings_chart_portfolio_names.contains(name);
    }

    public ChartPortfolioObj getFromName(String name) {
        if(!isSaved(name)) {
            return null;
        }

        int idx = settings_chart_portfolio_names.indexOf(name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        String serialString = sharedPreferences.getString("chart_portfolio" + idx, DEFAULT);
        return DataBridge.deserialize(serialString, ChartPortfolioObj.class);
    }

    public void addPortfolio(ChartPortfolioObj chartPortfolioObj) {
        // Add this portfolio to the end and save data.
        settings_chart_portfolio_names.add(chartPortfolioObj.name);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int size = settings_chart_portfolio_names.size();
        editor.putInt("chart_portfolio_size", size);
        editor.putString("chart_portfolio" + (size - 1), DataBridge.serialize(chartPortfolioObj, ChartPortfolioObj.class));
        editor.putString("chart_portfolio_names" + (size - 1), chartPortfolioObj.name);

        editor.apply();
    }

    public void removePortfolio(String chartPortfolioObjName) {
        // Remove this portfolio, and then shift others to condense.
        int idx = settings_chart_portfolio_names.indexOf(chartPortfolioObjName);
        settings_chart_portfolio_names.remove(idx);

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int size = sharedPreferences.getInt("chart_portfolio_size", 0);

        for(int i = idx; i < size - 1; i++) {
            String serialString = sharedPreferences.getString("chart_portfolio" + (i + 1), DEFAULT);
            String name = sharedPreferences.getString("chart_portfolio_names" + (i + 1), DEFAULT);

            editor.putString("chart_portfolio" + i, serialString);
            editor.putString("chart_portfolio_names" + i, name);
        }

        // Delete last element.
        editor.remove("chart_portfolio_names" + (size - 1));
        editor.remove("chart_portfolio" + (size - 1));

        // Update size.
        editor.putInt("chart_portfolio_size", size - 1);

        editor.apply();
    }

    public void renamePortfolio(String chartPortfolioObjOldName, String chartPortfolioObjNewName) {
        ChartPortfolioObj chartPortfolioObj = getFromName(chartPortfolioObjOldName);
        chartPortfolioObj.name = chartPortfolioObjNewName;

        int idx = settings_chart_portfolio_names.indexOf(chartPortfolioObjOldName);
        settings_chart_portfolio_names.set(idx, chartPortfolioObjNewName);

        updatePortfolio(chartPortfolioObj);
    }

    public void updatePortfolio(ChartPortfolioObj chartPortfolioObj) {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        int idx = settings_chart_portfolio_names.indexOf(chartPortfolioObj.name);
        editor.putString("chart_portfolio_names" + idx, chartPortfolioObj.name);
        editor.putString("chart_portfolio" + idx, DataBridge.serialize(chartPortfolioObj, ChartPortfolioObj.class));

        editor.apply();
    }

    public void saveAllData() {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();

        int size = settings_chart_portfolio_names.size();
        editor.putInt("chart_portfolio_size", size);

        for(int i = 0; i < size; i++) {
            String name = settings_chart_portfolio_names.get(i);
            editor.putString("chart_portfolio_names" + i, name);

            // Portfolios have to be loaded and then saved again.
            String serialString = sharedPreferences.getString("chart_portfolio" + i, DEFAULT);
            editor.putString("chart_portfolio" + i, DataBridge.cycleSerialization(serialString, ChartPortfolioObj.class));
        }

        editor.apply();
    }

    public void loadAllData() {
        // Only load portfolio names. Portfolios themselves are loaded when needed.
        settings_chart_portfolio_names = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        int size = sharedPreferences.getInt("chart_portfolio_size", 0);

        for(int i = 0; i < size; i++) {
            String name = sharedPreferences.getString("chart_portfolio_names" + i, DEFAULT);
            settings_chart_portfolio_names.add(name);
        }
    }

    public void resetAllData() {
        settings_chart_portfolio_names = new ArrayList<>();

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.apply();
    }

    @Override
    public void exportDataToJSON(DataBridge.Writer o) throws IOException {
        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());

        o.beginObject();
        o.serialize("!V!", "1", String.class);

        String sizeKey = "chart_portfolio_size";
        int size = sharedPreferences.getInt(sizeKey, 0);
        o.serialize(sizeKey, size, Integer.class);

        for(int i = 0; i < size; i++) {
            String nameKey = "chart_portfolio_names" + i;
            String serialNameString = sharedPreferences.getString(nameKey, DEFAULT);
            o.serialize(nameKey, serialNameString, String.class);

            String key = "chart_portfolio" + i;
            String serialString = sharedPreferences.getString(key, DEFAULT);
            o.serialize(key, serialString, String.class);
        }

        o.endObject();
    }

    @Override
    public void importDataFromJSON(DataBridge.Reader o) throws IOException {
        o.beginObject();

        String version = o.deserialize("!V!", String.class);
        if(!"1".equals(version)) {
            throw new IllegalStateException("version = " + version);
        }

        SharedPreferences sharedPreferences = SharedPreferencesUtil.getSharedPreferences(getSharedPreferencesKey());
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Create HashMap of all existing portfolios.
        HashMap<String, String> hashMap = new HashMap<>();
        for(int i = 0; i < settings_chart_portfolio_names.size(); i++) {
            String name = sharedPreferences.getString("chart_portfolio_names" + i, DEFAULT);
            String value = sharedPreferences.getString("chart_portfolio" + i, DEFAULT);
            HashMapUtil.putValueInMap(hashMap, name, value);
        }

        // Merge in the imported portfolios.
        int size = o.deserialize("chart_portfolio_size", Integer.class);
        for(int i = 0; i < size; i++) {
            String name = o.deserialize("chart_portfolio_names" + i, String.class);
            String value = o.deserialize("chart_portfolio" + i, String.class);
            HashMapUtil.putValueInMap(hashMap, name, DataBridge.cycleSerialization(value, ChartPortfolioObj.class));
        }

        // Erase portfolios, rewrite, and reload.
        editor.clear();

        ArrayList<String> keySet = new ArrayList<>(hashMap.keySet());

        editor.putInt("chart_portfolio_size", keySet.size());
        for(int i = 0; i < keySet.size(); i++) {
            String nameKey = "chart_portfolio_names" + i;
            String nameValue = keySet.get(i);
            editor.putString(nameKey, nameValue);

            String key = "chart_portfolio" + i;
            String value = HashMapUtil.getValueFromMap(hashMap, keySet.get(i));
            editor.putString(key, value);
        }

        editor.apply();

        o.endObject();

        // Reinitialize data.
        loadAllData();
    }
}
