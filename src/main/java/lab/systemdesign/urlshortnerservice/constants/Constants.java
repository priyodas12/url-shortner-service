package lab.systemdesign.urlshortnerservice.constants;

import java.math.BigInteger;

public class Constants {
    public static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final BigInteger BASE62_SPACE = BigInteger.valueOf(ALPHABET.length()).pow(7);
}
