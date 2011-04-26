package org.chemodansama.engine.render.imgui;

import java.util.ArrayList;
import java.util.Iterator;

final class NpWidgetStatelook {
    
    final private ArrayList<NpWidgetArea> mAreas = 
        new ArrayList<NpWidgetArea>();
    
    final private ArrayList<NpWidgetImage> mImages = 
        new ArrayList<NpWidgetImage>();
    
    private boolean mDefaultsComputed = false;
    
    private float mWidthDef = 0;
    private float mHeightDef = 0;
    
    NpWidgetStatelook(ArrayList<NpWidgetArea> areas,
            ArrayList<NpWidgetImage> images) {
        mAreas.addAll(areas);
        mImages.addAll(images);
    }
    
    private void computeDefaults(NpSkinScheme scheme, 
            NpWidgetStatelook stateLook) {
        
        mWidthDef = 0;
        mHeightDef = 0;
        
        for (NpWidgetArea a : mAreas) {
            if (a == null) {
                continue;
            }
            
            float w = a.getX().getValue(scheme, stateLook, a.getName())
                    + a.getWidth().getValue(scheme, stateLook, a.getName());
            
            float h = a.getY().getValue(scheme, stateLook, a.getName())
                    + a.getHeight().getValue(scheme, stateLook, a.getName());
            
            mWidthDef = Math.max(mWidthDef, w);
            mHeightDef = Math.max(mHeightDef, h);
        }
        
        mDefaultsComputed = true;
    }
    
    float getDefaultWidth(NpSkinScheme scheme, 
            NpWidgetStatelook stateLook) {
        if (!mDefaultsComputed) {
            computeDefaults(scheme, stateLook);
        }
        
        return mWidthDef;
    }
    
    float getDefaultHeight(NpSkinScheme scheme, 
            NpWidgetStatelook stateLook) {
        if (!mDefaultsComputed) {
            computeDefaults(scheme, stateLook);
        }
        
        return mHeightDef;
    }
    
    /**
     * Finds widget image by the area name via linear lookup. 
     * @param area specifies name of the area to find
     * @return Returns widget image if such area was found.
     *         Returns null if not.
     */ 
    NpWidgetImage findImageByArea(String area) {
        
        for (NpWidgetImage i : mImages) {
            if (i.getArea().equals(area)) {
                return i;
            }
        }
        
        return null;
    }
    
    Iterable<NpWidgetArea> getAreas() {
        return new Iterable<NpWidgetArea>() {
            @Override
            public Iterator<NpWidgetArea> iterator() {
                return mAreas.iterator();
            }
        };
    }
    
    Iterable<NpWidgetImage> getImages() {
        return new Iterable<NpWidgetImage>() {
            @Override
            public Iterator<NpWidgetImage> iterator() {
                return mImages.iterator();
            }
        };
    }
}
