package org.chemodansama.engine.utils;

public class NpEndianness {
    public final static int convertInt(int value) {
        return value << 24   
            | value >> 8 & 0x0000FF00
            | value << 8 & 0x00FF0000
            | value >>> 24;
    }
    
    public final static char convertChar(char value) {
        return (char) (value << 8 | value >>> 8);
    }
}
