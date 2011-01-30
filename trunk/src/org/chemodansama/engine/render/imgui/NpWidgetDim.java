package org.chemodansama.engine.render.imgui;

final class NpWidgetDim {
    final private NpWidgetDimType mType;
    final private float mValue;
    final private NpWidgetDimSource mSource;
    final private String mArea;
    
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
        mArea = "";
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
        
        mArea = "";
    }
    
    /**
     * Creates Dim with image scale DimType
     */
    public NpWidgetDim(String area, 
            NpWidgetDimSource source) {
      mType = NpWidgetDimType.IMAGE;
      mArea = area;
      mSource = source;
      
      mValue = 0.0f;
    }
    
    void setOperator(NpWidgetDimOp op) {
        mOp = op;
    }
    
    private float getBaseImageValue(NpSkinScheme skinScheme, 
            NpWidgetStatelook stateLook) {

        NpWidgetImage im = null;
        
        for (NpWidgetImage i : stateLook.getImages()) {
            if (i.getArea().equals(mArea)) {
                im = i;
                break;
            }
        }

        NpSkinImageSet is = skinScheme.getImageSet(im.getImageset());

        if (is == null) {
            return 0.0f;
        }
        
        NpSkinImageSet.NpSkinImage image = is.getImage(im.getImage());
        
        if (image == null) {
            return 0.0f;
        }
        
        switch (mSource) {
        case HEIGHT:
            return image.getHeight();
        case WIDTH:
            return image.getWidth();
        default:
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
    
    private float getBaseValue(NpSkinScheme skinScheme, 
            NpWidgetStatelook stateLook, NpRect instanceRect) {

        switch (mType) {
        case ABSOLUTE:
            return mValue;
            
        case IMAGE:
            return getBaseImageValue(skinScheme, stateLook);
            
        case SCALE:
            return getBaseScaleValue(skinScheme, instanceRect);

        default:
            return 0.0f;
        }
    }
    
    private float applyOp(float baseValue, NpSkinScheme skinScheme,
            NpWidgetStatelook stateLook, NpRect instanceRect) {
        
        float r = baseValue;
        
        if (mOp != null) {
            
            float subValue = mOp.getDim().getValue(skinScheme, stateLook, 
                                                   instanceRect);
            
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
    
    public float getValue(NpSkinScheme skinScheme, NpWidgetStatelook stateLook,
            NpRect instanceRect) {
        
        // base value for this dimension, with no applied operation yet
        float r = getBaseValue(skinScheme, stateLook, instanceRect);

        // applying specified operation for this dim
        r = applyOp(r, skinScheme, stateLook, instanceRect);
        
        return r;
    }
}
