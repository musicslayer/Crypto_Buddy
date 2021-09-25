package com.musicslayer.cryptobuddy.api;

import java.io.Serializable;

abstract public class API implements Serializable {
    abstract public String getName();
    abstract public String getDisplayName();
}
