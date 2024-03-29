package org.chemodansama.engine.math;

import java.util.ArrayList;

final public class NpMath {
    public static final float ZERO = 1.0e-06f;
    
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
    
    public static float calcRayPointDistExt(final float[] ray0, float[] ray1, 
            float[] p) {
        
        float[] rayDir = new float[3];
        
        NpVec3.sub(ray1, ray0, rayDir);
        NpVec3.normalize(rayDir);
        
        return calcRayPointDist(ray0, rayDir, p);
    }

    public static double clampd(double x, double min, double max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
    }
    
    public static float clampf(float x, float min, float max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
    }
    
    /**
     * @param points source points. Can not be {@code null}. 
     *               Can not contain {@code null}.
     * @param out where to store resulting convex hull. Can not be {@code null}. 
     * @throws IllegalArgumentException if any parameter is {@code null}. 
     */
    public static void constructConvexhull(ArrayList<NpVec2> points, 
            ArrayList<NpVec2> out) {
        if (points == null) {
            throw new IllegalArgumentException("points == null");
        }
        
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        }
        
        out.clear();
        
        int next = getFirstConvexhullPoint(points);
        if (next < 0) {
            return;
        }
        
        int first = next;

        do {
            out.add(points.get(next));
            next = getNextConvexhullPoint(points, next);
        } while ((first != next) && (next != -1));
    }

    /**
     * @param points source points. Can not be {@code null}. 
     *               Can not contain {@code null}.
     * @param out where to store resulting convex hull. Can not be {@code null}. 
     * @throws IllegalArgumentException if any parameter is {@code null}. 
     */
    public static void constructConvexhullIndices(ArrayList<NpVec2> points, 
            ArrayList<Integer> out) {
        if (points == null) {
            throw new IllegalArgumentException("points == null");
        }
        
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        }
        
        out.clear();
        
        int next = getFirstConvexhullPoint(points);
        if (next < 0) {
            return;
        }

        do {
            out.add(next);
            next = getNextConvexhullPoint(points, next);
        } while ((out.get(0) != next) && (next != -1));
    }
    
    /**
     * @param points source for convex hull. Can not be {@code null}. 
     *               Can not contain {@code null}.
     * @return index from 0 to {@code points.size() - 1}, such that 
     *                  element from {@code points} at returned index belongs 
     *                  to convex hull, or {@code -1} if empty.
     *                  
     * @throws IllegalArgumentException if {@code points} is {@code null}.                   
     */
    private static int getFirstConvexhullPoint(ArrayList<NpVec2> points) {

        if (points == null) {
            throw new IllegalArgumentException();
        }
        
        int ret = -1;
        float x = 0;
        
        int size = points.size();
        
        // return point with maximum x-axis coordinate value.
        for (int i = 0; i < size; i++) {
            float[] coords = points.get(i).coords;
            if ((coords[0] > x) || (ret < 0)) {
                x = coords[0];
                ret = i;
            }
        }
        
        return ret;
    }
    
    /**
     * @param points source for convex hull. Can not contain {@code null}.
     * @param last last point added to convex hull. 
     *              Must be within {@code 0} to {@code points.size() - 1} range.
     * @return next point from {@code points} which belongs to convex hull, 
     *          or {@code -1} if empty.
     * @throws IllegalArgumentException if {@code points} is {@code null}.  
     */
    private static int getNextConvexhullPoint(ArrayList<NpVec2> points, 
            int last) {
        
        if (points == null) {
            throw new IllegalArgumentException("points == null");
        }
        
        int size = points.size();
        
        if (size == 0) {
            return -1;
        }
        
        float[] lastCoords = points.get(last).coords;
        
        int ret = -1;
        float ux = 0;
        float uy = 0;
        
        for (int i = 0; i < size; i++) {
            
            float[] coords = points.get(i).coords;

            float vx = coords[0] - lastCoords[0];
            float vy = coords[1] - lastCoords[1];
         
            if (((ret < 0) || (ux * vy - uy * vx < 0)) 
                    && (vx * vx + vy * vy > ZERO)) {
                ret = i;
                ux = vx;
                uy = vy;
            }
        }
        
        return ret;
    }
    
    public static double lerp(double s, double t, double p) {
        return s * p + t * (1 - p);
    }
    
    public static boolean pointInConvex2d(final ArrayList<NpVec2> convex, 
            float x, float y) {
        if (convex == null) {
            return false;
        }
        
        int csize = convex.size();
        
        if (csize < 3) {
            return false;
        }
        
        NpVec2 a = convex.get(0);
        NpVec2 b = convex.get(1);
        
        float ax = a.coords[0];
        float ay = a.coords[1];
        
        float v0x = b.coords[0] - ax;
        float v0y = b.coords[1] - ay;
        
        float v2x = x - ax;
        float v2y = y - ay;
        
        for (int i = 2; i < csize; i++) {
            NpVec2 c = convex.get(i);
            float v1x = c.coords[0] - ax;
            float v1y = c.coords[1] - ay;

            float dot00 = v0x * v0x + v0y * v0y;
            float dot01 = v0x * v1x + v0y * v1y;
            float dot02 = v0x * v2x + v0y * v2y;
            float dot11 = v1x * v1x + v1y * v1y;
            float dot12 = v1x * v2x + v1y * v2y;

            // Compute barycentric coordinates
            float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
            float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
            float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

            // Check if point is in triangle
            if ((u >= 0) && (v >= 0) && (u + v < 1)) {
                return true;
            }
            
            v0x = v1x;
            v0y = v1y;
        }
        
        return false;
    }
}
