package org.chemodansama.engine.tmx;

public class TmxMapObject {
    public final int gid;
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    
    public TmxMapObject(int gid, int x, int y, int width, int height) {
        this.gid = gid;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
