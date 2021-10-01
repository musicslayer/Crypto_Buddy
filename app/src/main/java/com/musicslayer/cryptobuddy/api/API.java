package com.musicslayer.cryptobuddy.api;

import java.io.Serializable;

abstract public class API implements Serializable {
    // For now, just use the name as the key.
    public String getKey() {
        return getName();
    }

    abstract public String getName();
    abstract public String getDisplayName();
}
