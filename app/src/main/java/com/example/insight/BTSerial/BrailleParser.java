package com.example.insight.BTSerial;

import java.util.Arrays;

public class BrailleParser {
    private static final int ON = -1;

    public static int[] parse(char c) {
        int[] braille = new int[20];
        Arrays.fill(braille, 15);
        switch (c) {
            case 'a':
                braille[12] = ON;
                break;
            case 'b':
                braille[12] = ON;
                braille[14] = ON;
                break;
            case 'c':
                braille[12] = ON;
                braille[13] = ON;
                break;
            case 'd':
                braille[12] = ON;
                braille[13] = ON;
                braille[15] = ON;
                break;
            case 'e':
                braille[12] = ON;
                braille[15] = ON;
                break;
            case 'f':
                braille[12] = ON;
                braille[13] = ON;
                braille[14] = ON;
                break;
            case 'g':
                braille[12] = ON;
                braille[13] = ON;
                braille[15] = ON;
                braille[14] = ON;
                break;
            case 'h':
                braille[12] = ON;
                braille[14] = ON;
                braille[15] = ON;
                break;
            case 'i':
                braille[14] = ON;
                braille[13] = ON;
                break;
            case 'j':
                braille[14] = ON;
                braille[13] = ON;
                braille[15] = ON;
                break;
            case 'k':
                braille[12] = ON;
                braille[16] = ON;
                break;
            case 'l':
                braille[12] = ON;
                braille[16] = ON;
                braille[14] = ON;
                break;
            case 'm':
                braille[12] = ON;
                braille[16] = ON;
                braille[13] = ON;
                break;
            case 'n':
                braille[12] = ON;
                braille[16] = ON;
                braille[13] = ON;
                braille[15] = ON;
                break;
            case 'o':
                braille[12] = ON;
                braille[16] = ON;
                braille[15] = ON;
                break;
            case 'p':
                braille[12] = ON;
                braille[16] = ON;
                braille[14] = ON;
                braille[13] = ON;
                break;
            case 'q':
                braille[12] = ON;
                braille[16] = ON;
                braille[14] = ON;
                braille[15] = ON;
                braille[13] = ON;
                break;
            case 'r':
                braille[12] = ON;
                braille[16] = ON;
                braille[14] = ON;
                braille[15] = ON;
                break;
            case 's':
                braille[13] = ON;
                braille[16] = ON;
                braille[14] = ON;
                break;
            case 't':
                braille[15] = ON;
                braille[16] = ON;
                braille[14] = ON;
                braille[13] = ON;
                break;
            case 'u':
                braille[12] = ON;
                braille[16] = ON;
                braille[17] = ON;
                break;
            case 'v':
                braille[12] = ON;
                braille[16] = ON;
                braille[14] = ON;
                braille[17] = ON;
                break;
            case 'w':
                braille[15] = ON;
                braille[17] = ON;
                braille[14] = ON;
                braille[13] = ON;
                break;
            case 'x':
                braille[12] = ON;
                braille[16] = ON;
                braille[17] = ON;
                braille[13] = ON;
                break;
            case 'y':
                braille[12] = ON;
                braille[16] = ON;
                braille[17] = ON;
                braille[15] = ON;
                braille[13] = ON;
                break;
            case 'z':
                braille[12] = ON;
                braille[16] = ON;
                braille[17] = ON;
                braille[15] = ON;
                break;
            default:
                break;
        }
        return braille;
    }
}
