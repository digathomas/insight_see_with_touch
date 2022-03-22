package com.example.insight.BTSerial;

import java.util.Arrays;

public class BrailleParser {
    private static final int ON = 15;

    public static int[] parse(char c) {
        int[] braille = new int[20];
        Arrays.fill(braille, 0);
        switch (c) {
            case 'a':
                braille[14] = ON;
                break;
            case 'b':
                braille[14] = ON;
                braille[16] = ON;
                break;
            case 'c':
                braille[14] = ON;
                braille[15] = ON;
                break;
            case 'd':
                braille[14] = ON;
                braille[15] = ON;
                braille[17] = ON;
                break;
            case 'e':
                braille[14] = ON;
                braille[17] = ON;
                break;
            case 'f':
                braille[14] = ON;
                braille[15] = ON;
                braille[16] = ON;
                break;
            case 'g':
                braille[14] = ON;
                braille[15] = ON;
                braille[17] = ON;
                braille[16] = ON;
                break;
            case 'h':
                braille[14] = ON;
                braille[16] = ON;
                braille[17] = ON;
                break;
            case 'i':
                braille[16] = ON;
                braille[15] = ON;
                break;
            case 'j':
                braille[16] = ON;
                braille[15] = ON;
                braille[17] = ON;
                break;
            case 'k':
                braille[14] = ON;
                braille[18] = ON;
                break;
            case 'l':
                braille[14] = ON;
                braille[18] = ON;
                braille[16] = ON;
                break;
            case 'm':
                braille[14] = ON;
                braille[18] = ON;
                braille[15] = ON;
                break;
            case 'n':
                braille[14] = ON;
                braille[18] = ON;
                braille[15] = ON;
                braille[17] = ON;
                break;
            case 'o':
                braille[14] = ON;
                braille[18] = ON;
                braille[17] = ON;
                break;
            case 'p':
                braille[14] = ON;
                braille[18] = ON;
                braille[16] = ON;
                braille[15] = ON;
                break;
            case 'q':
                braille[14] = ON;
                braille[18] = ON;
                braille[16] = ON;
                braille[17] = ON;
                braille[15] = ON;
                break;
            case 'r':
                braille[14] = ON;
                braille[18] = ON;
                braille[16] = ON;
                braille[17] = ON;
                break;
            case 's':
                braille[15] = ON;
                braille[18] = ON;
                braille[16] = ON;
                break;
            case 't':
                braille[17] = ON;
                braille[18] = ON;
                braille[16] = ON;
                braille[15] = ON;
                break;
            case 'u':
                braille[14] = ON;
                braille[18] = ON;
                braille[19] = ON;
                break;
            case 'v':
                braille[14] = ON;
                braille[18] = ON;
                braille[16] = ON;
                braille[19] = ON;
                break;
            case 'w':
                braille[17] = ON;
                braille[19] = ON;
                braille[16] = ON;
                braille[15] = ON;
                break;
            case 'x':
                braille[14] = ON;
                braille[18] = ON;
                braille[19] = ON;
                braille[15] = ON;
                break;
            case 'y':
                braille[14] = ON;
                braille[18] = ON;
                braille[19] = ON;
                braille[17] = ON;
                braille[15] = ON;
                break;
            case 'z':
                braille[14] = ON;
                braille[18] = ON;
                braille[19] = ON;
                braille[17] = ON;
                break;
            default:
                break;
        }
        return braille;
    }
}
