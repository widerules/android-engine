package org.chemodansama.engine.render;

public enum NpPvrPixelTag {
    MGLPT_ARGB_4444(0x00), MGLPT_ARGB_1555(0x01), MGLPT_RGB_565(0x02), 
    MGLPT_RGB_555(0x03), MGLPT_RGB_888(0x04), MGLPT_ARGB_8888(0x05), 
    MGLPT_ARGB_8332(0x06), MGLPT_I_8(0x07), MGLPT_AI_88(0x08), 
    MGLPT_1_BPP(0x09), MGLPT_VY1UY0(0x0A), MGLPT_Y1VY0U(0x0B), 
    MGLPT_PVRTC2(0x0C), MGLPT_PVRTC4(0x0D), MGLPT_PVRTC2_2(0x0E), 
    MGLPT_PVRTC2_4(0x0F),

    OGL_RGBA_4444(0x10), OGL_RGBA_5551(0x11), OGL_RGBA_8888(0x12), 
    OGL_RGB_565(0x13), OGL_RGB_555(0x14), OGL_RGB_888(0x15), OGL_I_8(0x16), 
    OGL_AI_88(0x17), OGL_PVRTC2(0x18), OGL_PVRTC4(0x19), OGL_BGRA_8888(0x1A), 

    D3D_DXT1(0x20), D3D_DXT2(0x21), D3D_DXT3(0x22), D3D_DXT4(0x23), 
    D3D_DXT5(0x24),

    D3D_RGB_332(0x25), D3D_AI_44(0x26), D3D_LVU_655(0x27), D3D_XLVU_8888(0x28), 
    D3D_QWVU_8888(0x29),

    // 10 bits per channel
    D3D_ABGR_2101010(0x2A), D3D_ARGB_2101010(0x2B), D3D_AWVU_2101010(0x2C),

    // 16 bits per channel
    D3D_GR_1616(0x2D), D3D_VU_1616(0x2E), D3D_ABGR_16161616(0x2F),

    // HDR formats
    D3D_R16F(0x30), D3D_GR_1616F(0x31), D3D_ABGR_16161616F(0x32),

    // 32 bits per channel
    D3D_R32F(0x33), D3D_GR_3232F(0x34), D3D_ABGR_32323232F(0x35),

    // Ericsson
    ETC_RGB_4BPP(0x36), ETC_RGBA_EXPLICIT(0x37), ETC_RGBA_INTERPOLATED(0x38); 
    
    public final int pixelTag;
    
    private NpPvrPixelTag(int pixelTag) {
        this.pixelTag = pixelTag;
    }
}
