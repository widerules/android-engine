package org.chemodansama.engine.render;

import org.chemodansama.engine.math.NpMatrix4;
import org.chemodansama.engine.math.NpVec3;

public final class NpCamera {

    private NpMatrix4 mView = new NpMatrix4();
    private NpMatrix4 mProj = new NpMatrix4();
    private NpMatrix4 mViewProj = new NpMatrix4();
    private NpMatrix4 mInvViewProj = new NpMatrix4();

    private boolean mDirty = false;

    // View matrix parameters
    private float[] mPos = NpVec3.newInstance();
    private float[] mCenter = NpVec3.newInstance();
    private float[] mUp = NpVec3.newInstance();

    // Projection matrix parameters
    private float mFovy = 0;
    private float mNear = 0;
    private float mFar = 0;

    private float mAspect = 1;

    public NpCamera(float screenAspect) {
        super();

        mAspect = screenAspect;
    }

    private void computeMatrices() {
        computeView();
        computeProj();
        computeViewProj();

        mDirty = false;
    }

    private void computeProj() {
        mProj.setPerspective(mFovy, mAspect, mNear, mFar);
    }
    
    private void computeView() {
        mView.setLookAt(mPos, mCenter, mUp);
    }

    private void computeViewProj() {
        mProj.toMatrix(mViewProj);
        mViewProj.multiply(mView);
        
        mViewProj.computeInverse(mInvViewProj);
    }

    public void getInvViewProj(float[] invviewproj) {
        if (mDirty) {
            computeMatrices();
        }

        mInvViewProj.toArray(invviewproj);
    }

    public void getPos(float[] p) {
        System.arraycopy(mPos, 0, p, 0, 3);
    }

    public float[] getProj() {
        float[] result = NpMatrix4.constructMatrixArray();

        getProj(result);

        return result;
    }

    public void getProj(float[] proj) {
        if (mDirty) {
            computeMatrices();
        }

        mProj.toArray(proj);
    }

    public float[] getView() {
        float[] result = NpMatrix4.constructMatrixArray();

        getView(result);

        return result;
    }

    public void getView(float[] view) {
        if (mDirty) {
            computeMatrices();
        }

        mView.toArray(view);
    }

    public float[] getViewProj() {
        float[] result = NpMatrix4.constructMatrixArray();

        getViewProj(result);

        return result;
    }
    
    public void getViewProj(float[] viewproj) {
        if (mDirty) {
            computeMatrices();
        }

        mViewProj.toArray(viewproj);
    }

    public float[] getWorldViewMatrix(NpMatrix4 worldMat) {
        if (mDirty) {
            computeMatrices();
        }

        return mView.multiplyExternal(worldMat);
    }

    public void onScreenAspectChanged(float screenAspect) {
        mAspect = screenAspect;
    }

    public void setCenter(float[] c) {
        NpVec3.copy(c, mCenter);

        mDirty = true;
    }
    
    public void setFar(float far) {
        mFar = far;

        mDirty = true;
    }
    
    public void setFovy(float fovy) {
        mFovy = fovy;

        mDirty = true;
    }

    public void setNear(float near) {
        mNear = near;

        mDirty = true;
    }

    public void setPos(float[] p) {
        NpVec3.copy(p, mPos);

        mDirty = true;
    }

    public void setProjParams(float fovy, float near, float far) {
        setFovy(fovy);
        setNear(near);
        setFar(far);
    }

    public void setUp(float[] r) {
        NpVec3.copy(r, mUp);

        mDirty = true;
    }

    public void setView(float[] v) {
        mView.fromArray(v, 0);
        computeProj();
        computeViewProj();

        
        
        mDirty = false;
    }

    public void setViewParams(float[] pos, float[] center, float[] up) {
        setPos(pos);
        setCenter(center);
        setUp(up);
    }

}
