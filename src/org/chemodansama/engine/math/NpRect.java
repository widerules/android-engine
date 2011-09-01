package org.chemodansama.engine.math;

public class NpRect {
    
    public int x;
    public int y;
    public int w;
    public int h;

    public NpRect() {
        this.x = 0;
        this.y = 0;
        this.w = 0;
        this.h = 0;
    }
    
    public NpRect(NpRect r) {
        this.x = r.x;
        this.y = r.y;
        this.w = r.w;
        this.h = r.h;
    }

    public NpRect(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public boolean overlapsPoint(int x, int y) {
        return (x > this.x) && (x < this.x + this.w) 
                && (y > this.y) && (y < this.y + this.h);
    }
    
    public void set(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
    
    public void set(float x, float y, float w, float h) {
        this.x = (int) x;
        this.y = (int) y;
        this.w = (int) w;
        this.h = (int) h;
    }
    
    public int right() {
        return x + w;
    }
    
    public int top() {
        return y + h;
    }
}
