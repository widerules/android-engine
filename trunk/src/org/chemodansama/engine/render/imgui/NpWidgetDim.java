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
    public NpWidgetDim(String area, NpWidgetDimSource source) {
      mType = NpWidgetDimType.IMAGE;
      mArea = area;
      mSource = source;
      
      mValue = 0;
    }
    
    void setOperator(NpWidgetDimOp op) {
        mOp = op;
    }
    
    private int getBaseImageValue(NpSkinScheme skinScheme, 
            NpWidgetStatelook stateLook) {

        NpWidgetImage im = stateLook.findImageByArea(mArea);
        
        if (im == null) {
            return 0;
        }

        NpSkinImageSet is = skinScheme.getImageSet(im.getImageset());

        if (is == null) {
            return 0;
        }
        
        NpSkinImageSet.NpSkinImage image = is.getImage(im.getImage());
        
        if (image == null) {
            return 0;
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
    
    private int getBaseScaleValue(NpSkinScheme skinScheme,
            NpWidgetStatelook stateLook, 
            NpRecti instanceRect) {

        switch (mSource) {
        case HEIGHT:
            return instanceRect.getH();

        case WIDTH:
            return instanceRect.getW();

        default:
            return 0;
        }
    }
    
    private int getBaseScaleValue(NpSkinScheme skinScheme,
            NpWidgetStatelook stateLook, 
            String areaName) {
        NpWidgetImage im = stateLook.findImageByArea(areaName);
        
        if (im == null) {
            return 0;
        }
        
        NpSkinImageSet is = skinScheme.getImageSet(im.getImageset());

        if (is == null) {
            return 0;
        }            
        
        NpSkinImageSet.NpSkinImage image = is.getImage(im.getImage());
        
        if (image == null) {
            return 0;
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
    
    private float getBaseValue(NpSkinScheme skinScheme, 
            NpWidgetStatelook stateLook, NpRecti instanceRect) {

        switch (mType) {
        case ABSOLUTE:
            return mValue;
            
        case IMAGE:
            return getBaseImageValue(skinScheme, stateLook);
            
        case SCALE:
            return getBaseScaleValue(skinScheme, stateLook, instanceRect);

        default:
            return 0.0f;
        }
    }
    
    private float getBaseValue(NpSkinScheme skinScheme, 
            NpWidgetStatelook stateLook, String areaName) {

        switch (mType) {
        case ABSOLUTE:
            return mValue;
            
        case IMAGE:
            return getBaseImageValue(skinScheme, stateLook);
            
        case SCALE:
            return getBaseScaleValue(skinScheme, stateLook, areaName);

        default:
            return 0.0f;
        }
    }
    
    private float computeSubValue(NpSkinScheme skinScheme,
            NpWidgetStatelook stateLook, NpRecti instanceRect) {
        if (mOp == null) {
            return 0;
        }
        
        return mOp.getDim().getValue(skinScheme, stateLook, 
                                     instanceRect);
    }
    
    private float computeSubValue(NpSkinScheme skinScheme,
            NpWidgetStatelook stateLook, String areaName) {
        if (mOp == null) {
            return 0;
        }
        
        return mOp.getDim().getValue(skinScheme, stateLook, 
                                     areaName);
    }
    
    private float execOp(float baseValue, float subValue) {
        float r = baseValue;
        
        if (mOp != null) {
            
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
    
    private float applyOp(float baseValue, NpSkinScheme skinScheme,
            NpWidgetStatelook stateLook, NpRecti instanceRect) {
        return execOp(baseValue, 
                      computeSubValue(skinScheme, stateLook, instanceRect));
    }
    
    private float applyOp(float baseValue, NpSkinScheme skinScheme,
            NpWidgetStatelook stateLook, String areaName) {
        return execOp(baseValue, 
                      computeSubValue(skinScheme, stateLook, areaName));
    }
    
    
    public float getValue(NpSkinScheme skinScheme, NpWidgetStatelook stateLook,
            NpRecti instanceRect) {
        
        // base value for this dimension, with no applied operation yet
        float r = getBaseValue(skinScheme, stateLook, instanceRect);

        // applying specified operation for this dim
        r = applyOp(r, skinScheme, stateLook, instanceRect);
        
        return r;
    }
    
    /** getValue - get value, based on area name, 
     *             which is supposed to be equal to its image name 
     */
    public float getValue(NpSkinScheme skinScheme, NpWidgetStatelook stateLook,
            String areaName) {
        
        // base value for this dimension, with no applied operation yet
        float r = getBaseValue(skinScheme, stateLook, areaName);

        // applying specified operation for this dim
        r = applyOp(r, skinScheme, stateLook, areaName);
        
        return r;
    }
}
