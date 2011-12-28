package org.chemodansama.engine.render.imgui;

class NpWidgetDimOp {
    private NpWidgetDimOpType mOpType;
    private NpWidgetDim mDim;
    
    public NpWidgetDimOp(NpWidgetDimOpType type) {
        mOpType = type;
        mDim = null;
    }
    
    NpWidgetDimOpType getOpType() {
        return mOpType;
    }
    
    NpWidgetDim setDim(NpWidgetDim dim) {
        mDim = dim;
        return dim;
    }
    
    NpWidgetDim getDim() {
        return mDim;
    }
}
