package org.chemodansama.engine.render.imgui;

import javax.microedition.khronos.opengles.GL10;

public final class NpGuiState {

    private static GL10 mGL = null;

    static private int mMouseX = 0;
    static private int mMouseY = 0;
    static private int mHotItem = 0;
    static private int mHotItemTemp = 0;
    static private int mActiveItem = 0;
    static private boolean mMouseDown = false;

    static public void finish() {

        // Introduce one-render-frame lag here to get a correct hot id.
        // Assume widgets rendering goes in z-order,
        // so the correct hot id is the last one assigned to mHotItemTemp
        // between prepare() and finish() calls.
        mHotItem = mHotItemTemp;
        mHotItemTemp = 0;
        
        if (!mMouseDown) {
            setActive(0);
            setHot(0);
        }

        NpSkin.finish();

        finishRender();

        mGL = null;
    }

    static private void finishRender() {

        GL10 gl = mGL;

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glDisable(GL10.GL_ALPHA_TEST);
        gl.glDisable(GL10.GL_BLEND);

        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

    static int getMouseX() {
        return mMouseX;
    }

    static int getMouseY() {
        return mMouseY;
    }

    static boolean isActive(int id) {
        return mActiveItem == id;
    }

    public static boolean isAnyActive() {
        return mActiveItem != 0;
    }

    static boolean isHot(int id) {
        return mHotItem == id;
    }

    static boolean isMouseDown() {
        return mMouseDown;
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

        mGL = gl;

        prepareRender(gl, orthoX, orthoY);

        NpWidgetIdGen.reset();

        NpSkin.prepare(gl);
    }

    static private void prepareRender(GL10 gl, int orthoX, int orthoY) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrthof(0, orthoX, orthoY, 0, -1, 1);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glEnable(GL10.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL10.GL_GREATER, 0);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glCullFace(GL10.GL_FRONT);
    }

    static public boolean regionHit(float x, float y, float w, float h) {
        return (mMouseX > x) && (mMouseX < x + w) && (mMouseY > y)
                && (mMouseY < y + h);
    }

    static void setActive(int id) {
        mActiveItem = id;
    }

    static void setHot(int id) {
        mHotItemTemp = id;
    }

    static int doWidgetLogic(int id, boolean over) {

        int ret = 0;

        if (isMouseDown()) {
            if (over) {
                if (isHot(id) && !isAnyActive()) {
                    setActive(id);
                    ret |= NpGuiReturnConsts.GUI_RETURN_FLAG_MOUSE_MOVED_IN;
                }
                setHot(id);
            } else {
                if (isHot(id)) {
                    setHot(0);
                    ret |= NpGuiReturnConsts.GUI_RETURN_FLAG_MOUSE_MOVED_OUT;
                }
            }
        } else {
            if (isActive(id) && isHot(id)) {
                ret |= NpGuiReturnConsts.GUI_RETURN_FLAG_CLICKED;
            }
        }

        ret |= NpGuiReturnConsts.GUI_RETURN_FLAG_NORMAL;

        if (isHot(id)) {
            ret |= NpGuiReturnConsts.GUI_RETURN_FLAG_HOT;
        }

        if (isActive(id)) {
            ret |= NpGuiReturnConsts.GUI_RETURN_FLAG_ACTIVE;
        }

        return ret;
    }

    private NpGuiState() {
        super();
    }
}
