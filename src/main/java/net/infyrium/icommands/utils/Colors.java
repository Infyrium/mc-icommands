package net.infyrium.icommands.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets texts use legacy color codes next to MiniMessage tags by turning the codes into tags.
 */
public final class Colors {

    private static final String COLOR_CODES = "0123456789abcdef";

    private static final String[] COLOR_NAMES = {
            "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", "white"
    };

    private static final String DECORATION_CODES = "klmno";

    private static final String[] DECORATION_NAMES = {"obfuscated", "bold", "strikethrough", "underlined", "italic"};

    private static final String HEX_DIGITS = "0123456789abcdefABCDEF";

    private Colors() {
    }

    /**
     * Replaces color codes that start with an ampersand or a section sign with MiniMessage tags.
     * Supports colors, styles, reset and hex colors in both the #rrggbb and the x-repeated form.
     * As in the legacy format, a color code turns off the styles set by the codes before it.
     */
    public static String toMiniMessage(String input) {
        if (input.indexOf('&') < 0 && input.indexOf('§') < 0) return input;

        StringBuilder result = new StringBuilder();
        List<String> decorations = new ArrayList<>();

        int i = 0;
        while (i < input.length()) {
            char current = input.charAt(i);
            if ((current != '&' && current != '§') || i + 1 >= input.length()) {
                result.append(current);
                i++;
                continue;
            }

            char code = Character.toLowerCase(input.charAt(i + 1));
            String hex = readHex(input, i + 1, current);

            if (hex != null) {
                closeDecorations(result, decorations);
                result.append("<#").append(hex).append('>');
                i += code == '#' ? 8 : 14;
            } else if (COLOR_CODES.indexOf(code) >= 0) {
                closeDecorations(result, decorations);
                result.append('<').append(COLOR_NAMES[COLOR_CODES.indexOf(code)]).append('>');
                i += 2;
            } else if (DECORATION_CODES.indexOf(code) >= 0) {
                String name = DECORATION_NAMES[DECORATION_CODES.indexOf(code)];
                result.append('<').append(name).append('>');
                decorations.add(name);
                i += 2;
            } else if (code == 'r') {
                decorations.clear();
                result.append("<reset>");
                i += 2;
            } else {
                // Not a color code, e.g. a plain ampersand in the text
                result.append(current);
                i++;
            }
        }
        return result.toString();
    }

    // Returns "rrggbb" if a hex color starts at the position after the code character, otherwise null
    private static String readHex(String input, int position, char codeChar) {
        char first = Character.toLowerCase(input.charAt(position));

        if (first == '#') {
            if (position + 6 >= input.length()) return null;

            String hex = input.substring(position + 1, position + 7);
            return isHex(hex) ? hex : null;
        }

        if (first == 'x') {
            // x followed by six pairs of the code character and a hex digit
            if (position + 12 >= input.length()) return null;

            StringBuilder hex = new StringBuilder();
            for (int j = 0; j < 6; j++) {
                if (input.charAt(position + 1 + j * 2) != codeChar) return null;
                hex.append(input.charAt(position + 2 + j * 2));
            }
            return isHex(hex.toString()) ? hex.toString() : null;
        }

        return null;
    }

    private static boolean isHex(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (HEX_DIGITS.indexOf(text.charAt(i)) < 0) return false;
        }
        return true;
    }

    private static void closeDecorations(StringBuilder result, List<String> decorations) {
        for (int i = decorations.size() - 1; i >= 0; i--) {
            result.append("</").append(decorations.get(i)).append('>');
        }
        decorations.clear();
    }
}
