package com.musicslayer.cryptobuddy.state;

import com.musicslayer.cryptobuddy.asset.Asset;

import java.util.ArrayList;

// Currently used in a small number of cases, but may become more widely used later.
public class SearchStateObj {
    public ArrayList<Asset> assetArrayList = new ArrayList<>();
    public ArrayList<String> options_symbols = new ArrayList<>();
    public ArrayList<String> options_names = new ArrayList<>();
}
