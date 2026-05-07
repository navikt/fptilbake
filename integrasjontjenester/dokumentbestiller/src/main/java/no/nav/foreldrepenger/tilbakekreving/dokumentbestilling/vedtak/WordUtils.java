package no.nav.foreldrepenger.tilbakekreving.dokumentbestilling.vedtak;

import java.util.HashSet;
import java.util.Set;

public class WordUtils {

    public static String capitalizeFully(String str, char... delimiters) {
        if (str == null || str.isEmpty()) {
            return str;
        } else {
            str = str.toLowerCase();
            return capitalize(str, delimiters);
        }
    }

    private static String capitalize(String str, char... delimiters) {
        Set<Integer> delimiterSet = generateDelimiterSet(delimiters);
        int strLen = str.length();
        int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        boolean capitalizeNext = true;
        int index = 0;

        while(index < strLen) {
            int codePoint = str.codePointAt(index);
            if (delimiterSet.contains(codePoint)) {
                capitalizeNext = true;
                newCodePoints[outOffset++] = codePoint;
                index += Character.charCount(codePoint);
            } else if (capitalizeNext) {
                int titleCaseCodePoint = Character.toTitleCase(codePoint);
                newCodePoints[outOffset++] = titleCaseCodePoint;
                index += Character.charCount(titleCaseCodePoint);
                capitalizeNext = false;
            } else {
                newCodePoints[outOffset++] = codePoint;
                index += Character.charCount(codePoint);
            }
        }

        return new String(newCodePoints, 0, outOffset);
    }

    private static Set<Integer> generateDelimiterSet(char[] delimiters) {
        Set<Integer> delimiterHashSet = new HashSet<>();
        if (delimiters != null && delimiters.length != 0) {
            for(int index = 0; index < delimiters.length; ++index) {
                delimiterHashSet.add(Character.codePointAt(delimiters, index));
            }

            return delimiterHashSet;
        } else {
            if (delimiters == null) {
                delimiterHashSet.add(Character.codePointAt(new char[]{' '}, 0));
            }

            return delimiterHashSet;
        }
    }

}
