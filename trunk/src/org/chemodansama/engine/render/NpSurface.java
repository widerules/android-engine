package org.chemodansama.engine.render;


import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class NpSurface {

    private class GLSurfaceViewWrapper extends GLSurfaceView {

        private NpSurfaceRenderer mRenderer;

        private NpTouchEventListener touchEventListener = null;

        public GLSurfaceViewWrapper(Context context) {
            super(context);

            mRenderer = new NpSurfaceRenderer();
            setRenderer(mRenderer);
        }

        @Override
        public boolean onTouchEvent(final MotionEvent event) {
            if (touchEventListener != null) {
                return touchEventListener.onTouchEvent(event);
            } else {
                return false;
            }
        }
    }

    private GLSurfaceViewWrapper mSurfaceWrapper = null;

    public NpSurface(Context context) {
        super();

        mSurfaceWrapper = new GLSurfaceViewWrapper(context);
    }

    public void applyActivityContentView(Activity activity) {
        if (activity != null) {
            activity.setContentView(mSurfaceWrapper);
        }
    }

    public void onResume() {
        mSurfaceWrapper.onResume();
    }

    public void onPause() {
        mSurfaceWrapper.onPause();
    }

    public void setTouchEventListener(NpTouchEventListener listener) {
        mSurfaceWrapper.touchEventListener = listener;
    }

    public void addRenderable(final NpRenderable r) {
        mSurfaceWrapper.queueEvent(new Runnable() {

            @Override
            public void run() {
                mSurfaceWrapper.mRenderer.AddRenderable(r);
            }
        });
    }
}
