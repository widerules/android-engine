package org.chemodansama.engine.render.imgui;

import javax.microedition.khronos.opengles.GL10;

public final class NpGuiState {
    
    static private int mMouseX = 0;
    static private int mMouseY = 0;
    static private int mHotItem = 0;
    static private int mHotItemTemp = 0;
    static int mActiveItem = 0;
    static private boolean mMouseDown = false;
    
    static public void finish(GL10 gl) {

        // Introduce one-render-frame lag here to get a correct hot id.
        // Assume widgets rendering goes in z-order, 
        // so the correct hot id is the last one assigned to mHotItemTemp
        // between prepare() and finish() calls.
        mHotItem = mHotItemTemp;
        mHotItemTemp = 0;

        NpSkin.finish(gl);
        
        finishRender(gl);
    }
    
    static private void finishRender(GL10 gl) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPopMatrix();
        
        gl.glDisable(GL10.GL_ALPHA_TEST);
        gl.glDisable(GL10.GL_BLEND);
        
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
    
    static boolean getMouseDown() {
        return mMouseDown;
    }
    
    static int getMouseX() {
        return mMouseX;
    }
    
    static int getMouseY() {
        return mMouseY;
    }
    
    static void setHotItem(int id) {
        mHotItemTemp = id;
    }
    
    static int getHotItem() {
        return mHotItem;
    }
    
    static public void onMouseDown() {
        mMouseDown = true;
    }
    
    static public void onMouseMove(int x, int y) {
        mMouseX = x;
        mMouseY = y;
    }
    
    static public void onMouseUp() {
        mMouseDown = false;
    }
    
    static public void prepare(GL10 gl, int orthoX, int orthoY) {
        prepareRender(gl, orthoX, orthoY);
        
        NpWidgetIdGen.reset();
        
        NpSkin.prepare(gl);
    }
    
    static private void prepareRender(GL10 gl, int orthoX, int orthoY) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrthof(0.0f, orthoX, orthoY, 0.0f, -1.0f, 1.0f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL10.GL_GREATER, 0.0f);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        
        gl.glCullFace(GL10.GL_FRONT);
    }
    
    static public boolean regionHit(float x, float y, float w, float h) {
        return (mMouseX > x) && (mMouseX < x + w) 
            && (mMouseY > y) && (mMouseY < y + h);
    }
    
    private NpGuiState() {
        super();
        
    }
}
