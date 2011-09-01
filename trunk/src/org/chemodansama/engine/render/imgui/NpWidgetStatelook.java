package org.chemodansama.engine.render.imgui;

import java.util.ArrayList;
import java.util.Iterator;

import org.chemodansama.engine.math.NpRect;

final class NpWidgetStatelook {
    
    private final ArrayList<NpWidgetArea> mAreas = 
        new ArrayList<NpWidgetArea>();
    
    private final ArrayList<NpWidgetImage> mImages = 
        new ArrayList<NpWidgetImage>();
    
    private boolean mDefaultsComputed = false;
    
    private float mWidthDef = 0;
    private float mHeightDef = 0;

    private final NpWidgetRect mClientRect;
    
    NpWidgetStatelook(ArrayList<NpWidgetArea> areas,
            ArrayList<NpWidgetImage> images, NpWidgetRect clientRect) {
        mAreas.addAll(areas);
        mImages.addAll(images);
        mClientRect = clientRect;
    }
    
    public NpRect computeClientRect(NpSkinScheme scheme, NpRect instanceRect,
            boolean invertX, boolean invertY) {
        
        if (mClientRect == null) {
            return new NpRect(instanceRect);
        }

        NpRect result = new NpRect();
        
        float x = mClientRect.getX().getValue(scheme, this, instanceRect);
        float y = mClientRect.getY().getValue(scheme, this, instanceRect);
        float w = mClientRect.getWidth().getValue(scheme, this, instanceRect);
        float h = mClientRect.getHeight().getValue(scheme, this, instanceRect);
        
        if (invertX) {
            x = instanceRect.w - x - w;
        } 
        if (invertY) {
            y = instanceRect.h - y - h;
        }

        result.set(instanceRect.x + x, instanceRect.y + y, w, h);
        return result;
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

    public NpWidgetRect getClientRect() {
        return mClientRect;
    }
}
