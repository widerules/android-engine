package org.chemodansama.engine.math;

public final class NpVec4 {
    
    private float[] mValues = new float[4];
    
    static public NpVec4 iOrt4 = new NpVec4(1, 0, 0, 1);
    static public NpVec4 jOrt4 = new NpVec4(0, 1, 0, 1);
    static public NpVec4 kOrt4 = new NpVec4(0, 0, 1, 1);

    public NpVec4() {
        super();

        setValues(0, 0, 0, 1);
    }

    public NpVec4(float aX, float aY, float aZ, float aW) {
        super();

        setValues(aX, aY, aZ, aW);
    }

    public NpVec4(float[] a) {
        super();

        if (a.length == 4) {
            setValues(a[0], a[1], a[2], a[3]);
        } else {
            setValues(0, 0, 0, 1);
        }
    }

    public NpVec4(NpVec4 v) {
        super();

        setValues(v);
    }

    public boolean clip() {
        
        if (Math.abs(mValues[3]) > NpMath.ZERO) {
        
            final float invW = 1 / mValues[3];

            mValues[0] *= invW;
            mValues[1] *= invW;
            mValues[2] *= invW;
            mValues[3] = 1;
            
            return true;
        } else {
            return false;
        }
    }
    
    public float[] getArray() {
        return mValues;
    }

    public float getW() {
        return mValues[3];
    }

    public float getX() {
        return mValues[0];
    }
    
    public float getY() {
        return mValues[1];
    }
    
    public float getZ() {
        return mValues[2];
    }

    public void setValues(float aX, float aY, float aZ, float aW) {
        mValues[0] = aX;
        mValues[1] = aY;
        mValues[2] = aZ;
        mValues[3] = aW;
    }

    public void setValues(NpVec4 v) {
        setValues(v.getX(), v.getY(), v.getZ(), v.getW());
    }
}
