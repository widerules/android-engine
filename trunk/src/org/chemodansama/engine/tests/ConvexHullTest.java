package org.chemodansama.engine.tests;

import java.util.ArrayList;
import java.util.Random;

import org.chemodansama.engine.math.NpMath;
import org.chemodansama.engine.math.NpVec2;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConvexHullTest {

    final ArrayList<NpVec2> mPoints = new ArrayList<NpVec2>();
    final ArrayList<NpVec2> mConvex = new ArrayList<NpVec2>();
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAllSame() {
        mPoints.clear();
        mPoints.add(new NpVec2(10, 10));
        mPoints.add(new NpVec2(10, 10));
        mPoints.add(new NpVec2(10, 10));
        mPoints.add(new NpVec2(10, 10));
        mPoints.add(new NpVec2(10, 10));
        mPoints.add(new NpVec2(10, 10));
        
        NpMath.constructConvexhull(mPoints, mConvex);
        
        for (NpVec2 p : mConvex) {
            System.out.println(p.getX() + " " + p.getY());
        }
    }
    
    @Test
    public void testRandom() {
        mPoints.clear();
        
        Random r = new Random(System.currentTimeMillis());
        
        for (int i = 0; i < 100; i++) {
            mPoints.add(new NpVec2(r.nextInt(), r.nextInt()));
        }
        
        NpMath.constructConvexhull(mPoints, mConvex);
        
        for (NpVec2 p : mConvex) {
            System.out.println(p.getX() + " " + p.getY());
        }
    }
    
    @Test
    public void testCustom() {
        mPoints.clear();
        mPoints.add(new NpVec2(10, 10));
        mPoints.add(new NpVec2(10, 10));
        mPoints.add(new NpVec2(20, 10));
        
        NpMath.constructConvexhull(mPoints, mConvex);
        
        for (NpVec2 p : mConvex) {
            System.out.println(p.getX() + " " + p.getY());
        }
    }

}
