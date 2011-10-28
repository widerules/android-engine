package org.chemodansama.engine.tmx;

public class TmxObject {
    public final int gid;
    public final int x;
    public final int y;
    public final int width;
    public final int height;
    
    public TmxObject(int gid, int x, int y, int width, int height) {
        this.gid = gid;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
