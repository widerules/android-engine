package org.chemodansama.engine.math;

final public class NpMath {
    public static final float ZERO = 0.000001f;
    
    public static float clampf(float x, float min, float max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
    }
    
    public static boolean calcLinePlaneIntersection(float[] plane0, 
            float[] planeDir, float[] line0, float[] lineDir, float[] out) {
        
        float d = NpVec3.dot(planeDir, lineDir);
        
        if (Math.abs(d) > ZERO) {
            NpVec3.sub(plane0, line0, out);
            float n = NpVec3.dot(planeDir, out);
            
            NpVec3.mul(lineDir, n / d, out);
            NpVec3.add(out, line0, out);
            
            return true;
        }
        
        return false;
    }
    
    public static boolean calcLineConvexIntersection(float[] line0, 
            float[] lineDir, float[] convex, float[] out) {

        int convexVertsCnt = convex.length / 3;
        
        if (convexVertsCnt < 3) {
            return false;
        }
        
        float[] convexNorm = new float[3];
        
        float[] u = new float[3];
        float[] v = new float[3];

        NpVec3.setValues(u, 0, convex, 0);
        NpVec3.setValues(v, 0, convex, 0);
        
        NpVec3.sub(convex, 3, convex, 0, u, 0);
        NpVec3.sub(convex, 6, convex, 0, v, 0);
        
        NpVec3.cross(u, v, convexNorm);
        
        if (!calcLinePlaneIntersection(convex, convexNorm, 
                                       line0, lineDir, out)) {
            return false;
        }
        
        float[] t = new float[3];
        float[] tc = new float[3];
        
        boolean ok = true;
        
        for (int i = 0; i < convexVertsCnt; i++) {
            int j = (i + 1) % convexVertsCnt;
            
            NpVec3.sub(convex, j * 3, convex, i * 3, u, 0);
            NpVec3.sub(out, 0, convex, i * 3, t, 0);
            
            NpVec3.cross(u, t, tc);
            
            if (NpVec3.dot(tc, convexNorm) < -NpMath.ZERO) {
                ok = false;
                break;
            }
        }
        return ok;
    }

    public static float calcRayPointDist(float[] rayPos, float[] rayDirNorm, 
            float[] p) {
        float[] s = new float[3];
        NpVec3.sub(p, rayPos, s);
        
        float d = NpVec3.dot(s, rayDirNorm);
        
        float[] pp = new float[3];
        NpVec3.mul(rayDirNorm, d, pp);
        
        NpVec3.add(pp, rayPos, pp);
        
        return NpVec3.dist(pp, p);
    }
    
    public static boolean calcLineSphereIntersection(float[] rayPos, 
            float[] rayDirNorm, float[] spherePos, float sphereRad,
            float[] out1, float[] out2) {
        float[] c = new float[3];
        
        NpVec3.sub(spherePos, rayPos, c);
        
        double lc = NpVec3.dot(rayDirNorm, c);
        double cc = NpVec3.dot(c, c);
        
        double d = lc * lc - cc + sphereRad * sphereRad;
        
        if (d >= 0) {
            d = Math.sqrt(d);
            
            double d1 = lc + d;
            double d2 = lc - d;
            
            NpVec3.mul(rayDirNorm, (float) d1, out1);
            NpVec3.mul(rayDirNorm, (float) d2, out2);

            NpVec3.add(out1, rayPos, out1);
            NpVec3.add(out2, rayPos, out2);
            
            return true;
        } else {
            return false;
        }
    }
    
    public static float calcRayPointDistExt(final float[] ray0, float[] ray1, 
            float[] p) {
        
        float[] rayDir = new float[3];
        
        NpVec3.sub(ray1, ray0, rayDir);
        NpVec3.normalize(rayDir);
        
        return calcRayPointDist(ray0, rayDir, p);
    }
}
