package org.chemodansama.engine.tmx;

import java.util.ArrayList;

public class TmxObjectGroup extends TmxEntity {

    public final String name;
    public final int width;
    public final int heigth;
    public final boolean visible;
    
    private final ArrayList<TmxObject> mObjects = new ArrayList<TmxObject>();
    
    public TmxObjectGroup(String name, int width, int height, boolean visible) {
        super();
        this.name = name;
        this.width = width;
        this.heigth = height;
        this.visible = visible;
    }
    
    void addObject(TmxObject object) {
        mObjects.add(object);
    }
    
    public Iterable<TmxObject> getObjects() {
        return mObjects;
    }
    
    public int getObjectsCount() {
        return mObjects.size();
    }
}
