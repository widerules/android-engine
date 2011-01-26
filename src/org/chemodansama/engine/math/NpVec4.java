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

    public NpVec4(final float aX, final float aY, final float aZ, 
            final float aW) {
        super();

        setValues(aX, aY, aZ, aW);
    }

    public NpVec4(final float[] a) {
        super();

        if (a.length == 4) {
            setValues(a[0], a[1], a[2], a[3]);
        } else {
            setValues(0, 0, 0, 1);
        }
    }

    public NpVec4(final NpVec4 v) {
        super();

        setValues(v);
    }

    public final boolean clip() {
        
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
    
    public final float[] getArray() {
        return mValues;
    }

    public final float getW() {
        return mValues[3];
    }

    public final float getX() {
        return mValues[0];
    }
    
    public final float getY() {
        return mValues[1];
    }
    
    public final float getZ() {
        return mValues[2];
    }

    public final void setValues(final float aX, final float aY, final float aZ, 
            final float aW) {
        mValues[0] = aX;
        mValues[1] = aY;
        mValues[2] = aZ;
        mValues[3] = aZ;
    }

    public final void setValues(NpVec4 v) {
        setValues(v.getX(), v.getY(), v.getZ(), v.getW());
    }
}
