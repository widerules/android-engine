package org.chemodansama.engine.math;

public final class NpVec3 {
    
    private float[] mValues = new float[3];
    
    static public NpVec3 iOrt3 = new NpVec3(1, 0, 0);
    static public NpVec3 jOrt3 = new NpVec3(0, 1, 0);
    static public NpVec3 kOrt3 = new NpVec3(0, 0, 1);

    public NpVec3() {
        super();

        setValues(0, 0, 0);
    }

    public NpVec3(final float[] a) {
        super();

        if (a.length == 3) {
            setValues(a[0], a[1], a[2]);
        } else {
            setValues(0, 0, 0);
        }
    }

    public NpVec3(final float aX, final float aY, final float aZ) {
        super();

        setValues(aX, aY, aZ);
    }

    public NpVec3(final NpVec3 v) {
        super();

        setValues(v);
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
    
    public float[] getArray() {
        return mValues;
    }

    public void setValues(final float aX, final float aY, final float aZ) {
        mValues[0] = aX;
        mValues[1] = aY;
        mValues[2] = aZ;
    }

    public void setValues(NpVec3 v) {
        setValues(v.getX(), v.getY(), v.getZ());
    }
    
    public NpVec3 sub(final NpVec3 v) {
        return new NpVec3(getX() - v.getX(), getY() - v.getY(), 
                          getZ() - v.getZ());
    }
    
    public void normalize() {
        float invLen = 1.0f / (float) Math.sqrt(getX() * getX() 
                                                + getY() * getY() 
                                                + getZ() * getZ());
        mValues[0] *= invLen;
        mValues[1] *= invLen;
        mValues[2] *= invLen;
    }
    
    public NpVec3 cross(final NpVec3 v) {
        return new NpVec3(getY() * v.getZ() - v.getY() * getZ(), 
                          getZ() * v.getX() - v.getZ() * getX(), 
                          getX() * v.getY() - v.getX() * getY());
    }
}
