package org.chemodansama.engine.tmx;

enum TmxDataEncoding {
    NONE, BASE64;
    
    static final TmxDataEncoding getFromString(String s) {
        if (s == null) {
            return NONE;
        }
        
        if (s.equalsIgnoreCase("base64")) {
            return BASE64;
        }
        
        return NONE;
    }
}
