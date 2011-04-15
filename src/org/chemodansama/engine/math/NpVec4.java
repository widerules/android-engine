package org.chemodansama.engine.math;

final public class NpVec4 {
    
    private float[] mValues = new float[4];
    
    static public NpVec4 iOrt4 = new NpVec4(1, 0, 0, 1);
    static public NpVec4 jOrt4 = new NpVec4(0, 1, 0, 1);
    static public NpVec4 kOrt4 = new NpVec4(0, 0, 1, 1);
    static public NpVec4 ONE = new NpVec4(1, 1, 1, 1);

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
        } else if (a.length == 3) {
            setValues(a[0], a[1], a[2], 1);
        } else {
            setValues(0, 0, 0, 1);
        }
    }

    public NpVec4(NpVec4 v) {
        super();

        setValues(v);
    }

    public static float squareLen3(float[] v) {
        return v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
    }
    
    public static boolean clip(float[] v) {
        
        if (Math.abs(v[3]) > NpMath.ZERO) {
        
            final float invW = 1 / v[3];

            v[0] *= invW;
            v[1] *= invW;
            v[2] *= invW;
            v[3] = 1;
            
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
    
    public static void sub(float[] a, float[] b, float[] r) {
        r[0] = a[0] - b[0];
        r[1] = a[1] - b[1];
        r[2] = a[2] - b[2];
        r[3] = a[3] - b[3];
    }
    
    public static void sub(float[] a, float[] b) {
        sub(a, b, a);
    }
}
