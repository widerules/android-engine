package org.chemodansama.engine.render;

public interface NpPvrTc {
    
    static int GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG = 0x8C00;
    static int GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG = 0x8C01;
    static int GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG = 0x8C02;
    static int GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG = 0x8C03;
    
    int PVRTEX_MIPMAP      = (1 << 8); // has mip map levels
    int PVRTEX_TWIDDLE     = (1 << 9); // is twiddled
    int PVRTEX_BUMPMAP     = (1 << 10); // has normals encoded for a bump map
    int PVRTEX_TILING      = (1 << 11); // is bordered for tiled pvr
    int PVRTEX_CUBEMAP     = (1 << 12); // is a cubemap/skybox
    int PVRTEX_FALSEMIPCOL = (1 << 13); //
    int PVRTEX_VOLUME      = (1 << 14);
    int PVRTEX_PIXELTYPE   = 0xff; /* pixel type is always in the last 16bits 
                                      of the flags */
    
    int PVRTEX_IDENTIFIER = 0x21525650; /* the pvr identifier is the 
                                           characters 'P','V','R' */
    
    int PVRTEX_V1_HEADER_SIZE = 44; /* old header size was 44 for 
                                       identification  purposes */

    int PVRTC2_MIN_TEXWIDTH = 16;
    int PVRTC2_MIN_TEXHEIGHT = 8;
    int PVRTC4_MIN_TEXWIDTH = 8;
    int PVRTC4_MIN_TEXHEIGHT = 8;
    int ETC_MIN_TEXWIDTH = 4;
    int ETC_MIN_TEXHEIGHT = 4;
}
