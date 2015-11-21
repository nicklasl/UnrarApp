package nu.nldv.unroar.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Hasher {
    private static Md5Hasher instance;
    private final MessageDigest messageDigest;

    private Md5Hasher() {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Md5Hasher getInstance() {
        if (instance == null) {
            instance = new Md5Hasher();
        }
        return instance;
    }

    public String hash(String s) {
        messageDigest.reset();
        messageDigest.update(s.getBytes());
        byte[] digest = messageDigest.digest();
        BigInteger bigInt = new BigInteger(1,digest);
        return bigInt.toString(16);
    }
}
