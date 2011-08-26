package org.chemodansama.engine.tmx;

enum TmxDataCompression {
    NONE, GZIP, ZLIB;
    
    static final TmxDataCompression getFromString(String s) {
        if (s == null) {
            return NONE;
        }
        
        if (s.equalsIgnoreCase("gzip")) {
            return GZIP;
        }
        
        if (s.equalsIgnoreCase("zlib")) {
            return ZLIB;
        }
        
        return NONE;
    }
}
