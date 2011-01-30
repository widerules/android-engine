package org.chemodansama.engine.render.imgui;

public class NpWidgetImage {
    final private String mArea;
    final private String mImageset;
    final private String mImage;
    
    NpWidgetImage(String area, String imageset, String image) {
        mArea = area;
        mImage = image;
        mImageset = imageset;
    }
    
    public String getArea() {
        return mArea;
    }
    
    public String getImageset() {
        return mImageset;
    }
    
    public String getImage() {
        return mImage;
    }
}
