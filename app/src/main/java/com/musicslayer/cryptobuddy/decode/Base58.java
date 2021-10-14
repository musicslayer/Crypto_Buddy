package com.musicslayer.cryptobuddy.decode;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Base58 {
    final private static String charList = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final int[] indexList = new int[128];
    static {
        Arrays.fill(indexList, -1);

        char[] charList_c = charList.toCharArray();

        for (int i = 0; i < charList_c.length; i++) {
            indexList[charList_c[i]] = i;
        }
    }

    public static boolean isAddress(String address) {
        for(char t : address.toCharArray()) {
            int p = indexList[t];
            if (p == -1) { return false; }
        }

        return true;
    }

    public static boolean hasByteLength(String address, int byteLength) {
        byte[] bytes = toBytes(address);
        return bytes != null && bytes.length == byteLength;
    }

    public static int getAddressNetworkID(String address) {
        byte[] decoded = toBytes(address);
        if (decoded == null){return -1;}

        // Array may be smaller than 25 bytes, so let's copy it in a 25 byte array so we get the extra zero digits.
        byte[] decoded25 = new byte[25];
        System.arraycopy(decoded, 0, decoded25, decoded25.length - decoded.length, decoded.length);

        // Each crypto has a different expected network ID in the first byte.
        return decoded25[0] & 0xff;
    }

    public static boolean hasValidChecksum(String address) {
        // Many classic Base58 coins have the same address requirements.
        // The address must fit into 25 bytes, and have the right checksum for the last 4 bytes.
        byte[] decoded = toBytes(address);
        if(decoded == null || decoded.length > 25) {
            return false;
        }

        // Array may be smaller than 25 bytes, so let's copy it in a 25 byte array so we get the extra zero digits.
        byte[] decoded25 = new byte[25];
        System.arraycopy(decoded, 0, decoded25, decoded25.length - decoded.length, decoded.length);

        byte[] hash1 = sha256(Arrays.copyOfRange(decoded25, 0, 21));
        byte[] hash2 = sha256(hash1);

        return Arrays.equals(Arrays.copyOfRange(hash2, 0, 4), Arrays.copyOfRange(decoded25, 21, 25));
    }

    private static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] toBytes(String address) {
        // Convert address from Base58 to Base10 integer, and then return the byte array of that integer.
        BigInteger I = BigInteger.ZERO;
        for (char t : address.toCharArray()) {
            int p = indexList[t];
            if (p == -1) { return null; }

            I = I.multiply(BigInteger.valueOf(58)).add(BigInteger.valueOf(p));
        }

        // The number will always be positive, but we still need an extra element to hold the sign bit
        // because BigInteger may prepend an extra zero in some cases. We then remove this before returning.
        byte[] bytesI = I.toByteArray();
        byte[] result = new byte[getTrueByteArraySize(I) + 1];

        System.arraycopy(bytesI, 0, result, result.length - bytesI.length, bytesI.length);
        return Arrays.copyOfRange(result, 1, result.length); // Remove potential sign array.
    }

    private static int getTrueByteArraySize(BigInteger I) {
        // BigInteger may prepend an extra sign byte, even though we are only dealing with positive numbers.
        // This method tells us the "true" number of bytes we would need without this potential extra bit.
        int i = 1; // Start with 1 because even a value of 0 would need 1 byte of info.
        BigInteger B = new BigInteger("256");

        while(I.compareTo(B.pow(i)) >= 0) {
            i++;
        }

        return i;
    }
}
