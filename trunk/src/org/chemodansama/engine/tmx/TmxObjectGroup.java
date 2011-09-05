package org.chemodansama.engine.tmx;

import java.util.ArrayList;
import java.util.TreeMap;

public class TmxObjectGroup {

    public final String name;
    public final int width;
    public final int heigth;
    
    private final ArrayList<TmxObject> mObjects = new ArrayList<TmxObject>();
    private final TreeMap<String, String> mProperties = 
            new TreeMap<String, String>();
    
    public TmxObjectGroup(String name, int width, int height) {
        this.name = name;
        this.width = width;
        this.heigth = height;
    }
    
    void addObject(TmxObject object) {
        mObjects.add(object);
    }
    
    void addProperty(String name, String value) {
        mProperties.put(name, value);
    }
    
    public String getProperty(String name) {
        return mProperties.get(name);
    }
    
    public Iterable<TmxObject> getObjects() {
        return mObjects;
    }
    
    public int getObjectsCount() {
        return mObjects.size();
    }
}
