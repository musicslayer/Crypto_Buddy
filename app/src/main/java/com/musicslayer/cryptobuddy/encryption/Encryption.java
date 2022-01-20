package com.musicslayer.cryptobuddy.encryption;

import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    public static byte[] encrypt(String s, String key) {
        if(s == null) { return null; }

        try {
            SecretKeySpec skeyspec = new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")),"Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, skeyspec);
            return cipher.doFinal(s.getBytes(Charset.forName("UTF-8")));
        }
        catch(Exception ignored) {
            return null;
        }
    }

    public static String decrypt(byte[] e, String key) {
        if(e == null) { return null; }

        try {
            SecretKeySpec skeyspec = new SecretKeySpec(key.getBytes(Charset.forName("UTF-8")),"Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, skeyspec);
            return new String(cipher.doFinal(e), Charset.forName("UTF-8"));
        }
        catch(Exception ignored) {
            return null;
        }
    }
}