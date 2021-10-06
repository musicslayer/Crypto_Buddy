package com.musicslayer.cryptobuddy.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

// For BTC, DOGE, LTC, etc... other similar coins.
public class DecodeUtil {
    final private static String hexadecimalCharList = "0123456789ABCDEFabcdef";
    final private static String base58CharList = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    final private static String base32CharList = "234567ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // Algorand
    final private static String bech32CharList = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";
    private static final int[] generator = {0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3};

    private static final int[] INDEXES = new int[128];
    static {
        Arrays.fill(INDEXES, -1);

        char[] base58CharList_c = base58CharList.toCharArray();

        for (int i = 0; i < base58CharList_c.length; i++) {
            INDEXES[base58CharList_c[i]] = i;
        }
    }

    private static final byte[] encodingTable =
            {
                    (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', (byte)'6', (byte)'7',
                    (byte)'8', (byte)'9', (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f'
            };
    private static final byte[] decodingTable = new byte[128];
    static {
        Arrays.fill(decodingTable, (byte) 0xff);

        for (int i = 0; i < encodingTable.length; i++)
        {
            decodingTable[encodingTable[i]] = (byte)i;
        }

        decodingTable['A'] = decodingTable['a'];
        decodingTable['B'] = decodingTable['b'];
        decodingTable['C'] = decodingTable['c'];
        decodingTable['D'] = decodingTable['d'];
        decodingTable['E'] = decodingTable['e'];
        decodingTable['F'] = decodingTable['f'];
    }

    public static int getAddressNetworkID(String address) {
        byte[] decoded = decodeBase58To25Bytes(address);
        if (decoded == null){return -1;}

        // Each crypto has a different expected network ID in the first byte.
        return decoded[0] & 0xff;
    }

    public static boolean isBase32(String address) {
        for (char t : address.toCharArray()) {
            int p = base32CharList.indexOf(t);
            if (p == -1){return false;}
        }

        return true;
    }

    public static boolean isBase58(String address) {
        for (char t : address.toCharArray()) {
            int p = base58CharList.indexOf(t);
            if (p == -1){return false;}
        }

        return true;
    }

    public static boolean hasValidBase58Checksum(String address) {
        byte[] decoded = decodeBase58To25Bytes(address);
        if (decoded == null){return false;}

        byte[] hash1 = sha256(Arrays.copyOfRange(decoded, 0, 21));
        byte[] hash2 = sha256(hash1);

        return Arrays.equals(Arrays.copyOfRange(hash2, 0, 4), Arrays.copyOfRange(decoded, 21, 25));
    }

    private static byte[] decodeBase58To25Bytes(String address) {
        BigInteger num = BigInteger.ZERO;
        for (char t : address.toCharArray()) {
            int p = base58CharList.indexOf(t);
            if (p == -1){return null;}
            num = num.multiply(BigInteger.valueOf(58)).add(BigInteger.valueOf(p));
        }

        byte[] result = new byte[26]; // Use extra element to hold potential sign array.
        byte[] numBytes = num.toByteArray();

        System.arraycopy(numBytes, 0, result, result.length - numBytes.length, numBytes.length);
        return Arrays.copyOfRange(result, 1, result.length); // Remove potential sign array.
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

    public static boolean hasValidBase32Checksum(String address) {
        String lowercased = address.toLowerCase();
        String uppercased = address.toUpperCase();
        if (lowercased.equals(address) && uppercased.equals(address)) {
            return false;
        }
        address = uppercased;
        int pos = 0;
        String hrp = address.substring(0, pos);
        for (int i = 0; i < hrp.length(); i++) {
            char c = hrp.charAt(i);
            if (c < 33 || c > 126) { // Boundaries of "normal" ascii characters.
                return false;
            }
        }
        byte[] data = new byte[address.length() - pos - 1];
        for (int p = pos + 1, i = 0; p < address.length(); p++, i++) {
            int d = base32CharList.indexOf(address.charAt(p));
            if (d == -1) {
                return false;
            }
            data[i] = (byte) d;
        }
        //return verifyChecksum(hrp, data); // TODO figure this out.
        return true;
    }

    public static boolean hasValidBech32Checksum(String address) {
        // I don't think we should enforce length here...
        //if (address.length() > 90) {
        //    return false;
        //}

        String lowercased = address.toLowerCase();
        String uppercased = address.toUpperCase();
        if (lowercased.equals(address) && uppercased.equals(address)) {
            return false;
        }
        address = lowercased;
        int pos = address.lastIndexOf('1'); // Human readable part always ends at last "1", the rest should obey the checksum
        if (pos < 1 || pos + 7 > address.length()) {
            return false;
        }
        String hrp = address.substring(0, pos);
        for (int i = 0; i < hrp.length(); i++) {
            char c = hrp.charAt(i);
            if (c < 33 || c > 126) { // Boundaries of "normal" ascii characters.
                return false;
            }
        }
        byte[] data = new byte[address.length() - pos - 1];
        for (int p = pos + 1, i = 0; p < address.length(); p++, i++) {
            int d = bech32CharList.indexOf(address.charAt(p));
            if (d == -1) {
                return false;
            }
            data[i] = (byte) d;
        }
        return verifyChecksum(hrp, data);
    }

    private static boolean verifyChecksum(String hrp, byte[] data) {
        byte[] ehrp = hrpExpand(hrp);
        byte[] values = new byte[ehrp.length + data.length];
        System.arraycopy(ehrp, 0, values, 0, ehrp.length);
        System.arraycopy(data, 0, values, ehrp.length, data.length);
        return polymod(values) == 1;
    }

    private static int polymod(byte[] values) {
        int chk = 1;
        for (byte value : values) {
            int v = value & 0xff;
            int top = chk >>> 25;
            chk = (chk & 0x1ffffff) << 5 ^ v;
            for (int j = 0; j < 5; j++) {
                if (((top >> j) & 1) == 1) {
                    chk ^= generator[j];
                }
            }
        }
        return chk;
    }

    private static byte[] hrpExpand(String hrp) {
        byte[] ret = new byte[hrp.length() * 2 + 1];
        for (int i = 0; i < hrp.length(); i++) {
            char c = hrp.charAt(i);
            ret[i] = (byte) (c >> 5);
        }
        for (int i = 0; i < hrp.length(); i++) {
            char c = hrp.charAt(i);
            ret[i + hrp.length() + 1] = (byte) (c & 31);
        }
        return ret;
    }

    public static boolean isValidBlockchainAddress(String address) {
        // All ETH addresses start with 0x and then have 40 hexadecimal characters.
        char[] address_char = address.toCharArray();
        if(address_char.length != 42 || address_char[0] != '0' || address_char[1] != 'x') {
            return false;
        }

        for (int i = 2; i < address_char.length; i++) {
            int p = hexadecimalCharList.indexOf(address_char[i]);
            if (p == -1) {
                return false;
            }
        }

        return true;
    }

    public static String tronHex(String base58) {
        byte[] decoded = decode58(base58);
        return toHexString(decoded);
    }

    private static byte[] decode58(String input) {
        byte[] decodeCheck = base58_decode(input);
        if (decodeCheck.length <= 4) {
            return null;
        }
        byte[] decodeData = new byte[decodeCheck.length - 4];
        System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
        byte[] hash0 = sha256(decodeData);
        byte[] hash1 = sha256(hash0);
        if (hash1[0] == decodeCheck[decodeData.length] &&
                hash1[1] == decodeCheck[decodeData.length + 1] &&
                hash1[2] == decodeCheck[decodeData.length + 2] &&
                hash1[3] == decodeCheck[decodeData.length + 3]) {
            return decodeData;
        }
        return null;
    }

    public static byte[] base58_decode(String input) throws IllegalArgumentException {
        if (input.length() == 0) {
            return new byte[0];
        }
        byte[] input58 = new byte[input.length()];
        // Transform the String to a base58 byte sequence
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);

            int digit58 = -1;
            if (c >= 0 && c < 128) {
                digit58 = INDEXES[c];
            }
            if (digit58 < 0) {
                throw new IllegalArgumentException("Illegal character " + c + " at " + i);
            }

            input58[i] = (byte) digit58;
        }
        // Count leading zeroes
        int zeroCount = 0;
        while (zeroCount < input58.length && input58[zeroCount] == 0) {
            ++zeroCount;
        }
        // The encoding
        byte[] temp = new byte[input.length()];
        int j = temp.length;

        int startAt = zeroCount;
        while (startAt < input58.length) {
            byte mod = divmod256(input58, startAt);
            if (input58[startAt] == 0) {
                ++startAt;
            }

            temp[--j] = mod;
        }
        // Do no add extra leading zeroes, move j to first non null byte.
        while (j < temp.length && temp[j] == 0) {
            ++j;
        }

        return copyOfRange(temp, j - zeroCount, temp.length);
    }

    private static boolean ignore(char c) {
        return c == '\n' || c =='\r' || c == '\t' || c == ' ';
    }

    private static byte divmod256(byte[] number58, int startAt) {
        int remainder = 0;
        for (int i = startAt; i < number58.length; i++) {
            int digit58 = (int) number58[i] & 0xFF;
            int temp = remainder * 58 + digit58;

            number58[i] = (byte) (temp / 256);

            remainder = temp % 256;
        }

        return (byte) remainder;
    }

    private static byte[] copyOfRange(byte[] source, int from, int to) {
        byte[] range = new byte[to - from];
        System.arraycopy(source, from, range, 0, range.length);

        return range;
    }

    public static String toHexString(
            byte[] data)
    {
        return toHexString(data, 0, data.length);
    }

    public static String toHexString(
            byte[] data,
            int    off,
            int    length)
    {
        byte[] encoded = encode(data, off, length);
        return new String(asCharArray(encoded));
    }

    public static byte[] encode(
            byte[]    data,
            int       off,
            int       length)
    {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        try
        {
            encoder_encode(data, off, length, bOut);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("exception encoding Hex string: " + e.getMessage(), e);
        }

        return bOut.toByteArray();
    }

    public static int encoder_encode(byte[] inBuf, int inOff, int inLen, byte[] outBuf, int outOff) throws IOException
    {
        int inPos = inOff;
        int inEnd = inOff + inLen;
        int outPos = outOff;

        while (inPos < inEnd)
        {
            int b = inBuf[inPos++] & 0xFF;

            outBuf[outPos++] = encodingTable[b >>> 4];
            outBuf[outPos++] = encodingTable[b & 0xF];
        }

        return outPos - outOff;
    }

    public static int encoder_encode(byte[] buf, int off, int len, OutputStream out)
            throws IOException
    {
        byte[] tmp = new byte[72];
        while (len > 0)
        {
            int inLen = Math.min(36, len);
            int outLen = encoder_encode(buf, off, inLen, tmp, 0);
            out.write(tmp, 0, outLen);
            off += inLen;
            len -= inLen;
        }
        return len * 2;
    }

    public static char[] asCharArray(byte[] bytes)
    {
        char[] chars = new char[bytes.length];

        for (int i = 0; i != chars.length; i++)
        {
            chars[i] = (char)(bytes[i] & 0xff);
        }

        return chars;
    }
}
