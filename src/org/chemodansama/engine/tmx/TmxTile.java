package org.chemodansama.engine.tmx;

import java.util.TreeMap;

public class TmxTile {
    public final int id;
    
    private final TreeMap<String, String> mProperties;
    
    public TmxTile(int id) {
        this.id = id;
        mProperties = new TreeMap<String, String>();
    }

    void addProperty(String key, String value) {
        mProperties.put(key, value);
    }
    
    public String getProperty(String key) {
        return mProperties.get(key);
    }
}
