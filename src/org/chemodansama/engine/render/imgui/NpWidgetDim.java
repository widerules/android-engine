package org.chemodansama.engine.render.imgui;

import java.util.ArrayList;

import org.chemodansama.engine.math.NpRect;

final class NpWidgetDim {
    final private NpWidgetDimType mType;
    final private float mValue;
    final private NpWidgetDimSource mSource;
    final private String mArea;
    
    private final ArrayList<NpWidgetDimOp> mOp = new ArrayList<NpWidgetDimOp>();
    
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
    
    NpWidgetDimOp addOp(NpWidgetDimOp op) {
        if (op == null) {
            return null;
        }
        
        mOp.add(op);
        return op;
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
    
    private float getBaseScaleValue(NpSkinScheme skinScheme,
            NpWidgetStatelook stateLook, 
            NpRect instanceRect) {

        switch (mSource) {
        case HEIGHT:
            return instanceRect.h * mValue;

        case WIDTH:
            return instanceRect.w * mValue;

        default:
            return 0;
        }
    }
    
    private float getBaseScaleValue(NpSkinScheme skinScheme,
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
            return image.getHeight() * mValue;

        case WIDTH:
            return image.getWidth() * mValue;

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
    
    private static float computeSubValue(NpWidgetDimOp op,
            NpSkinScheme skinScheme, NpWidgetStatelook stateLook,
            NpRect instanceRect) {
        if (op == null || op.getDim() == null) {
            return 0;
        }

        return op.getDim().getValue(skinScheme, stateLook, instanceRect);
    }

    private static float computeSubValue(NpWidgetDimOp op,
            NpSkinScheme skinScheme, NpWidgetStatelook stateLook,
            String areaName) {
        if (op == null || op.getDim() == null) {
            return 0;
        }

        return op.getDim().getValue(skinScheme, stateLook, areaName);
    }
    
    private static float execOp(NpWidgetDimOp op, float baseValue, float subValue) {
        float r = baseValue;
        
        if (op != null) {
            
            switch (op.getOpType()) {
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

        float r = getBaseValue(skinScheme, stateLook, instanceRect);

        for (NpWidgetDimOp op : mOp) {
            r = execOp(op, r,
                       computeSubValue(op, skinScheme, stateLook, 
                                       instanceRect));
        }

        return r;
    }

    /**
     * getValue - get value, based on area name, which is supposed to be equal
     * to its image name
     */
    public float getValue(NpSkinScheme skinScheme, NpWidgetStatelook stateLook,
            String areaName) {
        float r = getBaseValue(skinScheme, stateLook, areaName);

        for (NpWidgetDimOp op : mOp) {
            r = execOp(op, r,
                       computeSubValue(op, skinScheme, stateLook, areaName));
        }

        return r;
    }
}
