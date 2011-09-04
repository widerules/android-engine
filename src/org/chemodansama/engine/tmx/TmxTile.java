package org.chemodansama.engine.tmx;

import java.util.TreeMap;

public class TmxTile {
    public final int id;
    
    private final TreeMap<String, String> mParams;
    
    public TmxTile(int id) {
        this.id = id;
        mParams = new TreeMap<String, String>();
    }

    void addParam(String key, String value) {
        mParams.put(key, value);
    }
    
    public String getParam(String key) {
        return mParams.get(key);
    }
}
