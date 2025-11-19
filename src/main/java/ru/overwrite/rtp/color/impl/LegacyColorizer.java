package ru.overwrite.rtp.color.impl;

import org.jetbrains.annotations.Nullable;
import ru.overwrite.rtp.color.Colorizer;

public class LegacyColorizer implements Colorizer {

    private static final char COLOR_CHAR = '§';
    private static final char ALT_COLOR_CHAR = '&';

    @Override
    public String colorize(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final char[] chars = message.toCharArray();
        final int length = chars.length;

        final StringBuilder builder = new StringBuilder(length + 32);
        char[] hex = null;

        int start = 0, end;
        loop:
        for (int i = 0; i < length - 1; ) {
            final char ch = chars[i];
            if (ch == ALT_COLOR_CHAR) {
                final char nextChar = chars[++i];
                if (nextChar == '#') {
                    if (i + 6 >= length) break;
                    if (hex == null) {
                        hex = new char[14];
                        hex[0] = COLOR_CHAR;
                        hex[1] = 'x';
                    }
                    end = i - 1;
                    for (int j = 0, hexI = 1; j < 6; j++) {
                        final char hexChar = chars[++i];
                        if (!isHexCharacter(hexChar)) {
                            continue loop;
                        }
                        hex[++hexI] = COLOR_CHAR;
                        hex[++hexI] = hexChar;
                    }
                    builder.append(chars, start, end - start).append(hex);
                    start = i + 1;
                } else {
                    if (isColorCharacter(nextChar)) {
                        chars[i - 1] = COLOR_CHAR;
                        chars[i] |= 0x20;
                    }
                }
            }
            ++i;
        }

        builder.append(chars, start, length - start);
        return builder.toString();
    }

    public static boolean isHexCharacter(final char ch) {
        return switch (ch) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                 'a', 'A', 'b', 'B', 'c', 'C', 'd', 'D', 'e', 'E', 'f', 'F' -> true;
            default -> false;
        };
    }

    private boolean isColorCharacter(char c) {
        return switch (c) {
            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                 'a', 'b', 'c', 'd', 'e', 'f',
                 'A', 'B', 'C', 'D', 'E', 'F',
                 'r', 'R', 'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O', 'x', 'X' -> true;
            default -> false;
        };
    }
}