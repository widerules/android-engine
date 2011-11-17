package org.chemodansama.engine.math;

public final class NpVec3 {
    
    public static final float[] ORIGIN = {0, 0, 0};
    public static final float[] iOrt3 = {1, 0, 0};
    public static final float[] jOrt3 = {0, 1, 0};
    public static final float[] kOrt3 = {0, 0, 1};
    
    /** Addition operation as follows: s + t = r. 
     *  All parameters lengths must be equal to 3 or greater.
     * @param s first addition term  
     * @param t second addition term   
     * @param r addition result  
     * 
     */
    public static void add(float[] s, float[] t, float[] r) {
        r[0] = s[0] + t[0];
        r[1] = s[1] + t[1]; 
        r[2] = s[2] + t[2];
    }

    public static void copy(float[] src, float[] dst) {
        System.arraycopy(src, 0, dst, 0, 3);
    }
    
    public static void cross(float[] v1, float[] v2, float[] r) {
        r[0] = v1[1] * v2[2] - v2[1] * v1[2];
        r[1] = v1[2] * v2[0] - v2[2] * v1[0];
        r[2] = v1[0] * v2[1] - v2[0] * v1[1];
    }
    
    public static float squareDist(float[] a, float[] b) {
        return (a[0] - b[0]) * (a[0] - b[0]) + (a[1] - b[1]) * (a[1] - b[1]) 
                + (a[2] - b[2]) * (a[2] - b[2]);
    }
    
    public static float dist(float[] a, float[] b) {
        return (float) Math.sqrt(squareDist(a, b));
    }
    
    public static float dot(float[] v1, float[] v2) {
        return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
    }

    public static float dot(float[] v1, int v1Pos, float[] v2, int v2Pos) {
        return v1[v1Pos] * v2[v2Pos] + v1[v1Pos + 1] * v2[v2Pos + 1] 
               + v1[v1Pos + 2] * v2[v2Pos + 2];
    }
    
    public static float len(float [] v) {
        return (float)Math.sqrt(squareLen(v));
    }

    public static void mul(float[] v, float k, float[] r) {
        r[0] = v[0] * k;
        r[1] = v[1] * k;
        r[2] = v[2] * k;
    }

    public static float[] newInstance() {
        return new float[] {0, 0, 0};
    }

    public static float[] newInstance(float x, float y, float z) {
        float [] r = newInstance();
        setValues(r, x, y, z);
        return r;
    }
    
    public static float[] newInstance(float[] v) {
        float [] r = newInstance();
        copy(v, r);
        return r;
    }
    
    public static void normalize(float[] v) {
        float len = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        float invLen = 1.0f / len;
        v[0] *= invLen;
        v[1] *= invLen;
        v[2] *= invLen;
    }
    
    public static void normalize(float[] v, int offset) {
        float len = (float) Math.sqrt(v[offset] * v[offset] 
                                      + v[offset + 1] * v[offset + 1] 
                                      + v[offset + 2] * v[offset + 2]);
        float invLen = 1.0f / len;
        v[offset]     *= invLen;
        v[offset + 1] *= invLen;
        v[offset + 2] *= invLen;
    }
    
    public static void packTo01(float[] v) {
        v[0] += 1.0f;
        v[0] *= 0.5f;
        
        v[1] += 1.0f;
        v[1] *= 0.5f;
        
        v[2] += 1.0f;
        v[2] *= 0.5f;
    }
    
    public static void setValues(float[] v, 
            final float aX, final float aY, final float aZ) {
        v[0] = aX;
        v[1] = aY;
        v[2] = aZ;
    }
    
    public static void setValues(float[] dst, int dstPos, 
            float[] src, int srcPos) {
        dst[dstPos]     = src[srcPos];
        dst[dstPos + 1] = src[srcPos + 1];
        dst[dstPos + 2] = src[srcPos + 2];
    }
    
    public static float squareLen(float [] v) {
        return (v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }
    
    public static void sub(float[] s, float[] t, float[] r) {
        r[0] = s[0] - t[0];
        r[1] = s[1] - t[1]; 
        r[2] = s[2] - t[2];
    }
    
    public static void sub(float[] v1, int v1Offset, float[] v2, int v2Offset, 
            float[] r, int rOffset) {
        for (int i = 0; i < 3; i++) {
            r[rOffset++] = v1[v1Offset++] - v2[v2Offset++];
        }
    }
}
