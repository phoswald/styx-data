package styx.data.impl;

import styx.data.Text;

class FormatUtils {

    static boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }

    static boolean isIdentifier(Text value) {
        int charCount = value.charCount();
        for(int index = 0; index < charCount; index++) {
            char character = value.charAt(index);
            if(index == 0 && !isIdentifierStartChar(character)) {
                return false;
            } else if(!isIdentifierChar(character)) {
                return false;
            }
        }
        return charCount > 0;
    }

    static boolean isIdentifierStartChar(char character) {
        return (character >= 'A' && character <= 'Z') || (character >= 'a' && character <= 'z') || (character == '_');
    }

    static boolean isIdentifierChar(char character) {
        return isIdentifierStartChar(character) || (character >= '0' && character <= '9');
    }

    static boolean isHexChar(char character) {
        return (character >= '0' && character <= '9') || (character >= 'A' && character <= 'F');
    }

    static int getHexDigit(char character) {
        if(character <= '9') {
            return character - '0';
        } else {
            return character - 'A' + 10;
        }
    }

    static char getHexChar(int digit) {
        if(digit < 10) {
            return (char) ('0' + digit);
        } else {
            return (char) ('A' + digit - 10);
        }
    }
}
