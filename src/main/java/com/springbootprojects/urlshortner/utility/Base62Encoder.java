package com.springbootprojects.urlshortner.utility;

public class Base62Encoder {
    static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String base62Encoder(Long key) {

        StringBuilder shortUrl = new StringBuilder();

        if(key == 0){
            return String.valueOf(ALPHABET.charAt(0));
        }

        while(Long.compareUnsigned(key,0) > 0){
            int remainder = (int) Long.remainderUnsigned(key, 62);
            shortUrl.append(ALPHABET.charAt(remainder));
            key = Long.divideUnsigned(key, 62);
        }

        return shortUrl.reverse().toString();
    }
}
