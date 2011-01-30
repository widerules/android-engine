package org.chemodansama.engine.render.imgui;

import org.chemodansama.engine.render.imgui.NpSkinImageSet.NpSkinImage;

final class NpWidgetDim {
    final private NpWidgetDimType mType;
    final private float mValue;
    final private NpWidgetDimSource mSource;
    final private String mImageSet;
    final private String mImage;
    
    private NpWidgetDimOp mOp = null;
    
    /**
     * Creates Dim with absolute DimType
     * 
     * @param value absolute value of the dim
     */
    public NpWidgetDim(float value) {
        mType = NpWidgetDimType.ABSOLUTE;
        mValue = value;
        
        mSource = NpWidgetDimSource.WIDTH;
        mImageSet = "";
        mImage = "";
    }

    /**
     * Creates Dim with scale DimType
     * 
     * @param value scale value
     * @param source scaling source
     */
    public NpWidgetDim(float value, NpWidgetDimSource source) {
        mType = NpWidgetDimType.SCALE;
        mSource = source;
        mValue = value;
        
        mImageSet = "";
        mImage = "";
    }
    
    /**
     * Creates Dim with image scale DimType
     * 
     * @param imageSet image holder name
     * @param image specifies Image where from Dim should get source
     * @param source specifies the source of the Dim (width, height, etc.)
     */
    public NpWidgetDim(String imageSet, String image, 
            NpWidgetDimSource source) {
      mType = NpWidgetDimType.IMAGE;
      mImage = image;
      mImageSet = imageSet;
      mSource = source;
      
      mValue = 0.0f;
    }
    
    void setOperator(NpWidgetDimOp op) {
        mOp = op;
    }
    
    private float getBaseImageValue(NpSkinScheme skinScheme) {

        NpSkinImageSet i = skinScheme.getImageSet(mImageSet);
        
        if (i != null) {
            NpSkinImage im = i.getImage(mImage);
            
            if (im != null) {
                switch (mSource) {
                case HEIGHT:
                    return im.getHeight();
                case WIDTH:
                    return im.getWidth();
                default:
                    return 0;
                }
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
    
    private float getBaseScaleValue(NpSkinScheme skinScheme, 
            NpRect instanceRect) {

        switch (mSource) {
        case HEIGHT:
            return instanceRect.getH();
            
        case WIDTH:
            return instanceRect.getW();

        default:
            return 0;
        }
    }
    
    private float getBaseValue(NpSkinScheme skinScheme, NpRect instanceRect) {

        switch (mType) {
        case ABSOLUTE:
            return mValue;
            
        case IMAGE:
            return getBaseImageValue(skinScheme);
            
        case SCALE:
            return getBaseScaleValue(skinScheme, instanceRect);

        default:
            return 0.0f;
        }
    }
    
    private float applyOp(float baseValue, NpSkinScheme skinScheme, 
            NpRect instanceRect) {
        
        float r = baseValue;
        
        if (mOp != null) {
            
            float subValue = mOp.getDim().getValue(skinScheme, instanceRect);
            
            switch (mOp.getOpType()) {
            case ADD:
                r += subValue;
                break;

            case DIVIDE:
                r /= subValue;
                break;
                
            case MULTIPLY:
                r *= subValue;
                break;
                
            case SUBTRACT:
                r -= subValue;
                break;

            default:
                break;
            }
        }
        
        return r;
    }
    
    public float getValue(NpSkinScheme skinScheme, NpRect instanceRect) {
        
        // base value for this dimension, with no applied operation yet
        float r = getBaseValue(skinScheme, instanceRect);

        // applying specified operation for this dim
        r = applyOp(r, skinScheme, instanceRect);
        
        return r;
    }
}
