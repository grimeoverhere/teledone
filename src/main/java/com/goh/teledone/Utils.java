package com.goh.teledone;

import java.util.Set;

public class Utils {

    private Utils() {}
    private static final String escapingCharacter = "\\";
    private static final Set<String> charactersToEscape =
            Set.of("_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", "-", "=", "|", "{", "}", ".", "!");


    public static String escapeForTelegramMarkdownV2(String text) {
        return charactersToEscape.stream()
                .reduce(text, (partialString, element) -> partialString.replace(element, escapingCharacter + element));
    }

}
