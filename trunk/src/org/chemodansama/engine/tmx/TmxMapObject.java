package org.chemodansama.engine.tmx;

public class TmxMapObject extends TmxEntity {
    public final String name;
    public final int gid;
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    public final String type;
    
    public TmxMapObject(String name, int gid, int x, int y, int width, 
            int height, String type) {
        this.name = name;
        this.gid = gid;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }
}
