package org.chemodansama.engine.render.imgui;

import java.util.ArrayList;
import java.util.Iterator;

final class NpWidgetStatelook {
    
    final private ArrayList<NpWidgetArea> mAreas = 
        new ArrayList<NpWidgetArea>();
    
    final private ArrayList<NpWidgetImage> mImages = 
        new ArrayList<NpWidgetImage>();
    
    NpWidgetStatelook(ArrayList<NpWidgetArea> areas,
            ArrayList<NpWidgetImage> images) {
        mAreas.addAll(areas);
        mImages.addAll(images);
    }
    
    /**
     * Finds widget image by the area name via linear lookup. 
     * @param area specifies name of the area to find
     * @return Returns widget image if such area was found.
     * Returns null if not.
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
