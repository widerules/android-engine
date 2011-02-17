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
    private NpVec3 mPos = new NpVec3();
    private NpVec3 mCenter = new NpVec3();
    private NpVec3 mUp = new NpVec3();

    // Projection matrix parameters
    private float mFovy = 0;
    private float mNear = 0;
    private float mFar = 0;

    private float mAspect = 1;

    private void computeView() {
        mView.setLookAt(mPos, mCenter, mUp);
    }

    private void computeProj() {
        mProj.setPerspective(mFovy, mAspect, mNear, mFar);
    }

    private void computeViewProj() {
        mProj.toMatrix(mViewProj);
        mViewProj.multiply(mView);
        
        mViewProj.computeInverse(mInvViewProj);
    }
    
    private void computeMatrices() {
        computeView();
        computeProj();
        computeViewProj();

        mDirty = false;
    }

    public void onScreenAspectChanged(float screenAspect) {
        mAspect = screenAspect;
    }

    public NpCamera(float screenAspect) {
        super();

        mAspect = screenAspect;
    }

    public void getView(float[] view) {
        if (mDirty) {
            computeMatrices();
        }

        mView.toArray(view);
    }

    public float[] getView() {
        float[] result = NpMatrix4.constructMatrixArray();

        getView(result);

        return result;
    }

    public void getProj(float[] proj) {
        if (mDirty) {
            computeMatrices();
        }

        mProj.toArray(proj);
    }

    public float[] getProj() {
        float[] result = NpMatrix4.constructMatrixArray();

        getProj(result);

        return result;
    }

    public void getViewProj(float[] viewproj) {
        if (mDirty) {
            computeMatrices();
        }

        mViewProj.toArray(viewproj);
    }

    public float[] getViewProj() {
        float[] result = NpMatrix4.constructMatrixArray();

        getViewProj(result);

        return result;
    }

    public void setPos(NpVec3 p) {
        mPos.setValues(p);

        mDirty = true;
    }

    public void setUp(NpVec3 r) {
        mUp.setValues(r);

        mDirty = true;
    }

    public void setCenter(NpVec3 c) {
        mCenter.setValues(c);

        mDirty = true;
    }

    public void setViewParams(NpVec3 pos, NpVec3 center, NpVec3 up) {
        setPos(pos);
        setCenter(center);
        setUp(up);
    }

    public void setFovy(float fovy) {
        mFovy = fovy;

        mDirty = true;
    }

    public void setNear(float near) {
        mNear = near;

        mDirty = true;
    }

    public void setFar(float far) {
        mFar = far;

        mDirty = true;
    }

    public void setProjParams(float fovy, float near, float far) {
        setFovy(fovy);
        setNear(near);
        setFar(far);
    }

    public float[] getWorldViewMatrix(NpMatrix4 worldMat) {
        if (mDirty) {
            computeMatrices();
        }

        return mView.multiplyExternal(worldMat);
    }

}
