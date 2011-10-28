package org.chemodansama.engine.render;

import java.io.DataInputStream;
import java.io.IOException;

import javax.microedition.khronos.opengles.GL10;

final public class NpTextureHeader implements NpPvrTc {

    private final int bytesPerPel;
    // GL_TEXTURE_1D, GL_TEXTURE_2D, GL_TEXTURE_3D 
    private final int target;
    // GL_INTENSITY, GL_ALPHA, GL_RGB, etc }
    private final int internalFormat;

    private final int width;
    private final int height;

    // GL_RGBA, GL_RGB, etc 
    private final int format;
    // GL_FLOAT, GL_UNSIGNED_BYTE, etc 
    private final int type;

    // Zero for SettingsChoise or GL_NEAREST, GL_LINEAR, etc 
    private final int minFilter;
    // Zero for SettingsChoise or GL_NEAREST, GL_LINEAR, etc 
    private final int magFilter;
    // stands for GL_TEXTURE_MAX_LEVEL 
    private final int mipsCount;
    
    public NpTextureHeader(DataInputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in == null");
        }
        
        bytesPerPel    = Integer.reverseBytes(in.readInt()); 
        target         = Integer.reverseBytes(in.readInt());
        internalFormat = Integer.reverseBytes(in.readInt());
        width          = Integer.reverseBytes(in.readInt());
        height         = Integer.reverseBytes(in.readInt());
        format         = Integer.reverseBytes(in.readInt());
        type           = Integer.reverseBytes(in.readInt());
        minFilter      = Integer.reverseBytes(in.readInt());
        magFilter      = Integer.reverseBytes(in.readInt());
        mipsCount      = Integer.reverseBytes(in.readInt());
    }
    
    /**
     * @param pvrHeader
     * @throws IllegalArgumentException if {@code pvrHeader} is {@code null}.  
     * @throws RuntimeException if unsupported pixel format is specified.
     */
    public NpTextureHeader(NpPvrHeader pvrHeader, int minFilter, 
            int magFilter) {
        if (pvrHeader == null) {
            throw new IllegalArgumentException("pvrHeader == null");
        }
        
        width  = pvrHeader.width;
        height = pvrHeader.height;
        
        target         = GL10.GL_TEXTURE_2D;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        mipsCount      = pvrHeader.mipMapCount + 1;        
        
        int pixelTagInt = pvrHeader.pixelFormatFlags & PVRTEX_PIXELTYPE;
        
        NpPvrPixelTag pixelTag = null;
        for (NpPvrPixelTag tag : NpPvrPixelTag.values()) {
            if (tag.pixelTag == pixelTagInt) {
                pixelTag = tag;
                break;
            }
        }
        
        if (pixelTag == null) {
            throw new RuntimeException(Integer.toHexString(pixelTagInt) 
                                       + " is not supported pixel format.");
        }
        
        switch (pixelTag) {
        case OGL_RGBA_4444:
            type = GL10.GL_UNSIGNED_SHORT_4_4_4_4;
            format = GL10.GL_RGBA;
            internalFormat = format;
            bytesPerPel = 2;
            break;

        case OGL_RGBA_5551:
            type = GL10.GL_UNSIGNED_SHORT_5_5_5_1;
            format = GL10.GL_RGBA;
            internalFormat = format;
            bytesPerPel = 2;
            break;

        case OGL_RGBA_8888:
            type = GL10.GL_UNSIGNED_BYTE;
            format = GL10.GL_RGBA;
            internalFormat = format;
            bytesPerPel = 4;
            break;

        /* New OGL Specific Formats Added */
        case OGL_RGB_565:
            type = GL10.GL_UNSIGNED_SHORT_5_6_5;
            format = GL10.GL_RGB;
            internalFormat = format;
            bytesPerPel = 2;
            break;

        case OGL_RGB_555:
            throw new RuntimeException("OGL_RGB_555 is not supported.");

        case OGL_RGB_888:
            type = GL10.GL_UNSIGNED_BYTE;
            format = GL10.GL_RGB;
            internalFormat = format;
            bytesPerPel = 3;
            break;

        case OGL_I_8:
            type = GL10.GL_UNSIGNED_BYTE;
            format = GL10.GL_LUMINANCE;
            internalFormat = format;
            bytesPerPel = 1;
            break;

        case OGL_AI_88:
            type = GL10.GL_UNSIGNED_BYTE;
            format = GL10.GL_LUMINANCE_ALPHA;
            internalFormat = format;
            bytesPerPel = 2;
            break;

        case OGL_PVRTC2:
            type = GL10.GL_UNSIGNED_BYTE;
            internalFormat = pvrHeader.alphaBitMask == 0 
                   ? GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG 
                   : GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG ; // PVRTC2
            format = pvrHeader.alphaBitMask == 0 ? GL10.GL_RGB : GL10.GL_RGBA;
            bytesPerPel = 0;
            break;

        case OGL_PVRTC4:
            type = GL10.GL_UNSIGNED_BYTE;
            internalFormat = pvrHeader.alphaBitMask == 0    
                   ? GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG 
                   : GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG ; // PVRTC4
            format = pvrHeader.alphaBitMask == 0 ? GL10.GL_RGB : GL10.GL_RGBA;
            bytesPerPel = 0;
            break;

        default:                                            // NOT SUPPORTED
            throw new RuntimeException(Integer.toHexString(pixelTag.pixelTag) 
                                       + " is not supported pixel format.");
        }
    }
    
    public int getFormat() {
        return format;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getInternalFormat() {
        return internalFormat;
    }
    
    public int getMagFilter() {
        return magFilter;
    }
    
    public int getMinFilter() {
        return minFilter;
    }
    
    public int getMipsCount() {
        return mipsCount;
    }
    
    public int getMipSize(int i) {
        if ((i < 0) || (i > mipsCount)) {
            return 0;
        }
        
        if (!isCompressedFormat()) {
            return (width >> i) * (height >> i) * bytesPerPel;
        }
        
        switch (internalFormat) {
        case GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG:
        case GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG:
            return (Math.max(width, 16) * Math.max(height, 8) * 2 + 7) / 8;
            
        case GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG:
        case GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG:
            return (Math.max(width, 8) * Math.max(height, 8) * 4 + 7) / 8;
            
        default:
            break;
        }
        
        return 0;
    }
    
    public int getTarget() {
        return target;
    }
    
    public int getType() {
        return type;
    }
    
    public int getWidth() {
        return width;
    }
    
    public boolean isCompressedFormat() {
        return (internalFormat == GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG)
                || (internalFormat == GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG)
                || (internalFormat == GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG)
                || (internalFormat == GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG);
    }
}