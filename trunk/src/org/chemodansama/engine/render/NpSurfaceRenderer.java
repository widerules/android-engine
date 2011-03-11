package org.chemodansama.engine.render;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpVec3;

import android.opengl.GLSurfaceView;

class NpSurfaceRenderer implements GLSurfaceView.Renderer {

    private NpRenderQueue mRenderQueue = new NpRenderQueue();
    
    private ArrayList<NpRenderable> mRenderables = new ArrayList<NpRenderable>();
    private IntBuffer mTextures;

    private long mCurrentTime = 0;
    
    private NpCamera mCamera = new NpCamera(1.0f);

    public NpSurfaceRenderer() {
        super();

        int textures[] = { 0, 0 };

        mTextures = IntBuffer.allocate(2);
        mTextures.put(textures);
        mTextures.position(0);
    }
    
    public void AddRenderable(NpRenderable r) {
        mRenderables.add(r);
    }
    
    @Override
    synchronized public void onDrawFrame(GL10 gl) {
       
        
        
        update(gl);
        
        render(gl);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

        float aspect = (float) width / height;

        mCamera.onScreenAspectChanged(aspect);
        mCamera.setProjParams(45.0f, 0.125f, 32.0f);
        mCamera.setViewParams(NpVec3.newInstance(0, 0, 5), 
                              NpVec3.newInstance(0, 0, 0),
                              NpVec3.newInstance(0, 1, 0));

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadMatrixf(mCamera.getProj(), 0);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadMatrixf(mCamera.getView(), 0);
        
        mCurrentTime = System.currentTimeMillis();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glDisable(GL10.GL_DITHER);

        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);

        gl.glClearColor(0, 0, 0, 0);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);

        gl.glGenTextures(1, mTextures);

        ByteBuffer pixels = ByteBuffer.allocate(1);
        byte[] p = { 127 };

        pixels.put(p);
        pixels.position(0);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextures.get(0));
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                           GL10.GL_NEAREST);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                           GL10.GL_NEAREST);
        gl.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGBA, 1, 1, 0,
                        GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, pixels);
    }

    private void render(GL10 gl) {
        synchronized (mCamera) {
            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            mRenderQueue.clear();
            
            for (NpRenderable r : mRenderables) {
                r.collectRenderOps(mCamera, mRenderQueue);
            }
            
            mRenderQueue.sortRenderOps();
            
            mRenderQueue.executeRenderOps(gl);
        }
    }

    private void update(GL10 gl) {
        long t = System.currentTimeMillis();

        int deltaTime = (int) (t - mCurrentTime);

        mCurrentTime = t;

        if (deltaTime > 0) {
            update(deltaTime);
        }
    }

    synchronized private void update(final int deltaTime) {
        for (NpRenderable r : mRenderables) {
            r.update(deltaTime, mCamera, null);
        }
    }

}
