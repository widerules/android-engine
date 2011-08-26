package org.chemodansama.engine.tmx;

import java.util.TreeMap;

public class TmxTile {
    private final int mId;
    
    private final TreeMap<String, String> mParams;
    
    public TmxTile(int id) {
        mId = id;
        mParams = new TreeMap<String, String>();
    }

    public int getId() {
        return mId;
    }
    
    void addParam(String key, String value) {
        mParams.put(key, value);
    }
    
    public String getParam(String key) {
        return mParams.get(key);
    }
}
