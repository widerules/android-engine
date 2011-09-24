package org.chemodansama.engine.tmx.render;

import java.util.Collection;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpBox;
import org.chemodansama.engine.render.NpPolyBuffer;
import org.chemodansama.engine.tmx.TmxMap;

class TmxKdNode {
    
    private final static int MAX_EXTENT = 100;
    private final static float MAX_OBJECT_SIZE = 0.25f;
    private final static int BINS_COUNT = 10;
    private final static int LOW_LIMIT = 3;
    private final static int EMPTY_COST = 90000;
    
    private static boolean mustBeLocalObject(TmxRenderObject o, 
            float boxWidth, float boxHeight) {
        return (o.extentsX() * 2 > MAX_OBJECT_SIZE * boxWidth) 
                && (o.extentsY() * 2 > MAX_OBJECT_SIZE * boxHeight);
    }
    private TmxRenderObject[] mObjects;
    
    private final TmxKdNode[] mChilds = new TmxKdNode[2];
    private final NpBox mBox;
    
    public TmxKdNode(TmxRenderObject[] objects, int[] indices) {
        
        float boxLeft   = Float.MAX_VALUE;
        float boxRight  = Float.MIN_VALUE;
        
        float boxTop    = Float.MIN_VALUE;
        float boxBottom = Float.MAX_VALUE;
        
        for (int i : indices) {
            TmxRenderObject o = objects[i];
            
            if (o == null) {
                continue;
            }
            
            float left = o.left();
            float right = o.right();
            float top = o.top();
            float bottom = o.bottom();
            
            if (left < boxLeft) {
                boxLeft = left;
            }
            
            if (right > boxRight) {
                boxRight = right;
            }
            
            if (top > boxTop) {
                boxTop = top;
            }
            
            if (bottom < boxBottom) {
                boxBottom = bottom;
            }
        }
        
        mBox = new NpBox((boxLeft + boxRight) / 2, (boxTop + boxBottom) / 2, 
                         (boxRight - boxLeft) / 2, (boxTop - boxBottom) / 2);
        
        setupChilds(objects, indices, boxLeft, boxRight, boxBottom, boxTop);
    }
    
    private int countBottom(float border, TmxRenderObject[] objects, 
            int[] indices) {
        int r = 0;
        for (int i : indices) {
            TmxRenderObject o = objects[i];
            
            if (o == null) {
                continue;
            }
            
            if (o.centerY() < border) {
                r++;
            }
        }
        return r;
    }
    
    private int countLeft(float border, TmxRenderObject[] objects, 
            int[] indices) {
        int r = 0;
        for (int i : indices) {
            TmxRenderObject o = objects[i];
            
            if (o == null) {
                continue;
            }
            
            if (o.centerX() < border) {
                r++;
            }
        }
        return r;
    }
    
    public void debugRender(GL10 gl, NpPolyBuffer pb, int level) {
        float left = mBox.center[0] - mBox.extents[0] + level * 3;
        float right = mBox.center[0] + mBox.extents[0] - level * 3;
        float bottom = mBox.center[1] - mBox.extents[1] + level * 3;
        float top = mBox.center[1] + mBox.extents[1] - level * 3;
        
        pb.pushQuadWH(gl, left,  bottom, 1, top - bottom, 0, 0, 0, 0);
        pb.pushQuadWH(gl, right, bottom, 1, top - bottom, 0, 0, 0, 0);
        pb.pushQuadWH(gl, left, bottom, right - left, 1, 0, 0, 0, 0);
        pb.pushQuadWH(gl, left, top,    right - left, 1, 0, 0, 0, 0);

        for (TmxKdNode child : mChilds) {
            if (child == null) {
                continue;
            }
            
            child.debugRender(gl, pb, level + 1);
        }
    }
    
    public void getVisibleObjects(NpBox cameraBounds, 
            Collection<TmxRenderObject> objects) {
        for (TmxRenderObject o : mObjects) {
            objects.add(o);
        }

        for (TmxKdNode child : mChilds) {
            if (child == null) {
                continue;
            }
            
            if (child.mBox.overlaps(cameraBounds)) {
                child.getVisibleObjects(cameraBounds, objects);
            }
        }
    }
    
    private void setupChilds(TmxRenderObject[] objects, int[] indices,  
            float boxLeft, float boxRight, float boxBottom, float boxTop) {
        
        float boxWidth = boxRight - boxLeft;
        float boxHeight = boxTop - boxBottom;
        
        if (((boxWidth < MAX_EXTENT) && (boxHeight < MAX_EXTENT)) 
                || (indices.length <= LOW_LIMIT)) {
            mObjects = new TmxRenderObject[indices.length];

            int cnt = 0;
            for (int i : indices) {
                TmxRenderObject o = objects[i];
                
                if (o == null) {
                    continue;
                }
                
                mObjects[cnt] = o;
                cnt++;
            }
            return;
        } 

        int localCnt = 0;
        int childsCnt = 0;
        
        for (int i : indices) {
            TmxRenderObject o = objects[i];
            
            if (o == null) {
                continue;
            }

            if (mustBeLocalObject(o, boxWidth, boxHeight)) {
                localCnt++;
            } else {
                childsCnt++;
            }
        }
        
        mObjects = new TmxRenderObject[localCnt];
        int[] childIndices = new int[childsCnt];
        
        localCnt = 0;
        childsCnt = 0;
        
        for (int i : indices) {
            TmxRenderObject o = objects[i];
            
            if (o == null) {
                continue;
            }
            
            if (mustBeLocalObject(o, boxWidth, boxHeight)) {
                mObjects[localCnt] = o;
                localCnt++;
            } else {
                childIndices[childsCnt] = i;
                childsCnt++;
            }
        }    
        
        if (boxWidth > boxHeight) {
            splitLeftRight(objects, boxLeft, boxRight, boxHeight, childIndices);
        } else {
            splitTopBottom(objects, boxBottom, boxTop, boxWidth, childIndices);
        }
    }
    
    private void splitLeftRight(TmxRenderObject[] objects, 
            float boxLeft, float boxRight, float boxHeight, int[] indices) {

        int n = indices.length;
        
        if (n == 0) {
            return;
        }
        
        float boxWidth = boxRight - boxLeft;
        
        float d = boxWidth / BINS_COUNT;
        
        float border = boxLeft;
        
        int bestLeft = 0;
        float bestPart = Float.MAX_VALUE;
        float bestBorder = (boxLeft + boxRight) / 2;
        
        while (border < boxRight) {
            int left = countLeft(border, objects, indices);

            float part = boxHeight * (left * (border - boxLeft) + 
                    (n - left) * (boxRight - border)) + EMPTY_COST;
            
            if (part < bestPart) {
                bestBorder = border;
                bestPart = part;
                bestLeft = left;
            }
            
            border += d;
        }
        
        int[] leftIndices = null;
        if (bestLeft > 0) {
            leftIndices = new int[bestLeft];
        }
        int leftCount = 0;
        
        int[] rightIndices = null;
        if (bestLeft < n) {
            rightIndices = new int[n - bestLeft];
        }
        int rightCount = 0;
        
        for (int i : indices) {
            TmxRenderObject o = objects[i];
            
            if (o == null) {
                continue;
            }
            
            if (o.centerX() < bestBorder) {
                leftIndices[leftCount] = i;
                leftCount++;
            } else {
                rightIndices[rightCount] = i;
                rightCount++;
            }
        }

        if (leftIndices != null) {
            mChilds[0] = new TmxKdNode(objects, leftIndices);
        }
        
        if (rightIndices != null) {
            mChilds[1] = new TmxKdNode(objects, rightIndices);
        }
    }
    
    private void splitTopBottom(TmxRenderObject[] objects, 
            float boxBottom, float boxTop, float boxWidth, int[] indices) {

        int n = indices.length;
        
        float boxHeight = boxTop - boxBottom;
        
        float d = boxHeight / BINS_COUNT;
        
        float border = boxBottom;
        
        int bestBottom = 0;
        float bestPart = Float.MAX_VALUE;
        float bestBorder = (boxBottom + boxTop) / 2;
        
        while (border < boxTop) {
            int bottom = countBottom(border, objects, indices);

            float part = boxWidth * (bottom * (border - boxBottom) + 
                    (n - bottom) * (boxTop - border)) + EMPTY_COST;
            
            if (part < bestPart) {
                bestBorder = border;
                bestPart = part;
                bestBottom = bottom;
            }
            
            border += d;
        }
        
        int[] leftIndices = null;
        if (bestBottom > 0) {
            leftIndices = new int[bestBottom];
        }
        int l = 0;
        
        int[] rightIndices = null;
        if (bestBottom < n) {
            rightIndices = new int[n - bestBottom];
        }
        int r = 0;
        for (int i : indices) {
            TmxRenderObject o = objects[i];
            
            if (o == null) {
                continue;
            }
            
            if (o.centerY() < bestBorder) {
                leftIndices[l] = i;
                l++;
            } else {
                rightIndices[r] = i;
                r++;
            }
        }
        
        if (leftIndices != null) {
            mChilds[0] = new TmxKdNode(objects, leftIndices);
        }
        if (rightIndices != null) {
            mChilds[1] = new TmxKdNode(objects, rightIndices);
        }
    }
}

class TmxKdTree {
    
    private final TmxKdNode mRoot;
    
    public TmxKdTree(TmxRenderObject[] objects, TmxMap map) {
        
        if (objects == null) {
            mRoot = null;
            return;
        }
        
        int n = objects.length;
        
        int[] indices = new int[n];
        
        for (int i = 0; i < n; i++) {
            indices[i] = i;  
        };
        
        mRoot = new TmxKdNode(objects, indices);
    }
    
    public void getVisibleObjects(NpBox cameraBounds, 
            Collection<TmxRenderObject> objects) {
        
        if (mRoot == null) {
            return;
        }
            
        mRoot.getVisibleObjects(cameraBounds, objects);
    }
    
    public void debugRender(GL10 gl, NpPolyBuffer pb) {
        if (mRoot == null) {
            return;
        }
        
        mRoot.debugRender(gl, pb, 0);
    }
}
