package org.chemodansama.engine.tmx;

import java.util.TreeMap;

public class TmxEntity {

    protected final TreeMap<String, String> mProperties;

    public TmxEntity() {
        super();
        mProperties = new TreeMap<String, String>();
    }

    void addProperty(String name, String value) {
        mProperties.put(name, value);
    }

    public String getProperty(String name) {
        return mProperties.get(name);
    }
    
    public int getPropertyAsInt(String name, int defaultValue) {
        String s = getProperty(name);
        return (s != null) ? Integer.parseInt(s) : defaultValue;
    }
}