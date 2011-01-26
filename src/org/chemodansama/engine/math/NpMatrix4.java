package org.chemodansama.engine.math;

import android.opengl.Matrix;

final public class NpMatrix4 {

    private float[] mValues = null;

    private int mOffset = 0;

    static private final int MATRIX_ARRAY_SIZE = 16;

    static public float[] constructMatrixArray() {
        return new float[MATRIX_ARRAY_SIZE];
    }

    static private float[] getPerspectiveMatrix(float fovy, float aspect,
            float zNear, float zFar) {
        float[] m = constructMatrixArray();

        float f = (float) Math.atan(1 / Math.tan(fovy / 2));

        m[0] = f / aspect;
        m[1] = 0;
        m[2] = 0;
        m[3] = 0;

        m[4] = 0;
        m[5] = f;
        m[6] = 0;
        m[7] = 0;

        m[8] = 0;
        m[9] = 0;
        m[10] = (zFar + zNear) / (zNear - zFar);
        m[11] = -1;

        m[12] = 0;
        m[13] = 0;
        m[14] = (2 * zFar * zNear) / (zNear - zFar);
        m[15] = 0;

        return m;
    }

    public NpMatrix4() {
        super();

        mValues = constructMatrixArray();
        mOffset = 0;

        loadIdentity();
    }
    
    public NpMatrix4(final NpMatrix4 m) {
        this();

        if (m != null) {
            System.arraycopy(m.mValues, m.mOffset, mValues, mOffset, 
                             MATRIX_ARRAY_SIZE);
        }
    }

    public boolean computeInverse(float[] result) {
        
        if ((result == null) || (result.length < MATRIX_ARRAY_SIZE)) {
            return false;
        }
        
        float[] m = mValues;
        float[] a = constructMatrixArray();
        
        a[0] =     m[5] * (m[10] * m[15] - m[11] * m[14]) - m[9] * (m[6] * m[15] 
                    - m[7] * m[14]) + m[13] * (m[6] * m[11] - m[7] * m[10]);
        
        a[1] = -  (m[4] * (m[10] * m[15] - m[11] * m[14]) - m[8] * (m[6] * m[15] 
                    - m[7] * m[14]) + m[12] * (m[6] * m[11] - m[7] * m[10]));
        
        a[2] =     m[4] * (m[9]  * m[15] - m[11] * m[13]) - m[8] * (m[5] * m[15] 
                    - m[7] * m[13]) + m[12] * (m[5] * m[11] - m[7] * m[9]);
        
        a[3] = -  (m[4] * (m[9]  * m[14] - m[10] * m[13]) - m[8] * (m[5] * m[14] 
                    - m[6] * m[13]) + m[12] * (m[5] * m[10] - m[6] * m[9]));
        
        a[4] = -  (m[1] * (m[10] * m[15] - m[11] * m[14]) - m[9] * (m[2] * m[15] 
                    - m[3] * m[14]) + m[13] * (m[2] * m[11] - m[3] * m[10]));
        
        a[5] =     m[0] * (m[10] * m[15] - m[11] * m[14]) - m[8] * (m[2] * m[15] 
                    - m[3] * m[14]) + m[12] * (m[2] * m[11] - m[3] * m[10]);
        
        a[6] = -  (m[0] * (m[9]  * m[15] - m[11] * m[13]) - m[8] * (m[1] * m[15] 
                    - m[3] * m[13]) + m[12] * (m[1] * m[11] - m[3] * m[9]));
        
        a[7] =     m[0] * (m[9]  * m[14] - m[10] * m[13]) - m[8] * (m[1] * m[14] 
                    - m[2] * m[13]) + m[12] * (m[1] * m[10] - m[2] * m[9]);
        
        a[8] =     m[1] * (m[6]  * m[15] - m[7]  * m[14]) - m[5] * (m[2] * m[15] 
                    - m[3] * m[14]) + m[13] * (m[2] * m[7]  - m[3] * m[6]);
        
        a[9] = -  (m[0] * (m[6]  * m[15] - m[7]  * m[14]) - m[4] * (m[2] * m[15] 
                    - m[3] * m[14]) + m[12] * (m[2] * m[7]  - m[3] * m[6]));
        
        a[10] =    m[0] * (m[5]  * m[15] - m[7]  * m[13]) - m[4] * (m[1] * m[15] 
                    - m[3] * m[13]) + m[12] * (m[1] * m[7]  - m[3] * m[5]);
        
        a[11] = - (m[0] * (m[5]  * m[14] - m[6]  * m[13]) - m[4] * (m[1] * m[14] 
                    - m[2] * m[13]) + m[12] * (m[1] * m[6]  - m[2] * m[5]));
        
        a[12] = - (m[1] * (m[6]  * m[11] - m[7]  * m[10]) - m[5] * (m[2] * m[11] 
                    - m[3] * m[10]) + m[9]  * (m[2] * m[7]  - m[3] * m[6]));
        
        a[13] =    m[0] * (m[6]  * m[11] - m[7]  * m[10]) - m[4] * (m[2] * m[11] 
                    - m[3] * m[10]) + m[8]  * (m[2] * m[7]  - m[3] * m[6]);
        
        a[14] = - (m[0] * (m[5]  * m[11] - m[7]  * m[9])  - m[4] * (m[1] * m[11] 
                    - m[3]  * m[9])  + m[8] * (m[1] * m[7]  - m[3] * m[5]));
        
        a[15] =    m[0] * (m[5]  * m[10] - m[6]  * m[9])  - m[4] * (m[1] * m[10] 
                    - m[2]  * m[9])  + m[8] * (m[1] * m[6]  - m[2] * m[5]);

        float detInv = (m[0] * a[0] + m[1] * a[1] + m[2] * a[2] + m[3] * a[3]);
        
        if (Math.abs(detInv) > NpMath.ZERO) {
            detInv = 1 / detInv;

            result[0]  = a[0]  * detInv;
            result[1]  = a[4]  * detInv;
            result[2]  = a[8]  * detInv;
            result[3]  = a[12] * detInv;
            result[4]  = a[1]  * detInv;
            result[5]  = a[5]  * detInv;
            result[6]  = a[9]  * detInv;
            result[7]  = a[13] * detInv;
            result[8]  = a[2]  * detInv;
            result[9]  = a[6]  * detInv;
            result[10] = a[10] * detInv;
            result[11] = a[14] * detInv;
            result[12] = a[3]  * detInv;
            result[13] = a[7]  * detInv;
            result[14] = a[11] * detInv;
            result[15] = a[15] * detInv;

            return true;
        } else {
            return false;
        }
    }
    
    public boolean computeInverse(NpMatrix4 result) {
        return computeInverse(result.getArray());
    }

    public boolean fromArray(float[] array, int offset) {
        if (array.length == MATRIX_ARRAY_SIZE) {
            System.arraycopy(array, offset, mValues, mOffset, MATRIX_ARRAY_SIZE);
            return true;
        } else {
            return false;
        }
    }

    public float[] getArray() {
        return mValues;
    }

    public int getOffset() {
        return mOffset;
    }

    public void loadIdentity() {
        Matrix.setIdentityM(mValues, mOffset);
    }

    public void multiply(NpMatrix4 m) {
        float[] t = constructMatrixArray();
        System.arraycopy(mValues, mOffset, t, 0, MATRIX_ARRAY_SIZE);

        Matrix.multiplyMM(mValues, mOffset, t, 0, m.mValues, m.mOffset);
    }

    public float[] multiplyExternal(NpMatrix4 m) {
        float[] t = constructMatrixArray();

        Matrix.multiplyMM(t, 0, mValues, mOffset, m.mValues, m.mOffset);

        return t;
    }
    
    public float[] multiplyVec(NpVec4 v) {
        float[] r = new float[4];

        Matrix.multiplyMV(r, 0, mValues, mOffset, v.getArray(), 0);
        
        return r;
    }

    public void ortho(float left, float right, float bottom, float top,
            float near, float far) {
        Matrix.orthoM(mValues, mOffset, left, right, bottom, top, near, far);
    }

    public void perspective(float fovy, float aspect, float zNear, float zFar) {

        float[] m = getPerspectiveMatrix(fovy, aspect, zNear, zFar);

        float[] t = constructMatrixArray();
        System.arraycopy(mValues, mOffset, t, 0, MATRIX_ARRAY_SIZE);

        // TODO: check multiplication order correctness
        Matrix.multiplyMM(mValues, mOffset, m, 0, t, 0);
    }

    public void rotate(float a, float x, float y, float z) {
        Matrix.rotateM(mValues, mOffset, a, x, y, z);
    }

    public void rotate(float a, NpVec3 v) {
        rotate(a, v.getX(), v.getY(), v.getZ());
    }

    public void scale(float kx, float ky, float kz) {
        Matrix.scaleM(mValues, mOffset, kx, ky, kz);
    }

    public void scale(NpVec3 k) {
        scale(k.getX(), k.getY(), k.getZ());
    }

    // set, not multiply!
    public void setLookAt(final NpVec3 eye, final NpVec3 center, 
            final NpVec3 up) {
        
        NpVec3 f = center.sub(eye);
        f.normalize();
        
        NpVec3 uUp = new NpVec3(up); 
        uUp.normalize();
        
        NpVec3 s = f.cross(uUp);
        
        NpVec3 u = s.cross(f);

        float[] m = new float[16];

        
        m[0] = s.getX();
        m[1] = u.getX();
        m[2] = -f.getX();
        m[3] = 0;
        
        m[4] = s.getY();
        m[5] = u.getY();
        m[6] = -f.getY();
        m[7] = 0;
        
        m[8] = s.getZ();
        m[9] = u.getZ();
        m[10] = -f.getZ();
        m[11] = 0;
        
        m[12] = 0;
        m[13] = 0;
        m[14] = 0;
        m[15] = 1;
        
        float[] t = constructMatrixArray();
        
        Matrix.multiplyMM(t, 0, mValues, mOffset, m, 0);
        Matrix.translateM(t, 0, -eye.getX(), -eye.getY(), -eye.getZ());
        
        System.arraycopy(t, 0, mValues, mOffset, MATRIX_ARRAY_SIZE);
        
        /*
        Matrix.setLookAtM(mValues, mOffset, 
                          eye.getX(), eye.getY(), eye.getZ(),
                          center.getX(), center.getY(), center.getZ(),
                          up.getX(), up.getY(), up.getZ());
                          */
        
    }

    public void setPerspective(final float fovy, final float aspect, 
            final float zNear, final float zFar) {
        
        float[] m = getPerspectiveMatrix(fovy, aspect, zNear, zFar);

        System.arraycopy(m, 0, mValues, mOffset, MATRIX_ARRAY_SIZE);
    }

    public boolean toArray(float[] array) {
        if ((array != null) && (array.length >= MATRIX_ARRAY_SIZE)) {
            System.arraycopy(mValues, mOffset, array, 0, MATRIX_ARRAY_SIZE);
            return true;
        } else {
            return false;
        }
    }

    public boolean toMatrix(NpMatrix4 m) {
        if (m != null) {
            System.arraycopy(mValues, mOffset, m.mValues, m.mOffset,
                             MATRIX_ARRAY_SIZE);
            return true;
        } else {
            return false;
        }
    }

    public void translate(float x, float y, float z) {
        Matrix.translateM(mValues, mOffset, x, y, z);
    }

    public void translate(NpVec3 v) {
        translate(v.getX(), v.getY(), v.getZ());
    }
    
    @Override
    protected Object clone() throws CloneNotSupportedException {

        NpMatrix4 m = new NpMatrix4();
        toMatrix(m);
        
        return m;
    }
}
