package org.chemodansama.engine.utils;

public class NpEndianness {
    public final static char convertChar(char value) {
        return (char) (value << 8 | value >>> 8);
    }
}
