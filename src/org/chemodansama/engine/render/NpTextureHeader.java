package org.chemodansama.engine.render;

import java.io.DataInputStream;
import java.io.IOException;

import org.chemodansama.engine.LogTag;

import android.util.Log;

final public class NpTextureHeader {
    private int bytesPerPel = 0;

    // GL_TEXTURE_1D, GL_TEXTURE_2D, GL_TEXTURE_3D 
    private int target = 0;
    // GL_INTENSITY, GL_ALPHA, GL_RGB, etc }
    private int internalFormat = 0;

    private int width = 0;
    private int height = 0;

    // GL_RGBA, GL_RGB, etc 
    private int format = 0;
    // GL_FLOAT, GL_UNSIGNED_BYTE, etc 
    private int type = 0;

    // Zero for SettingsChoise or GL_NEAREST, GL_LINEAR, etc 
    private int minFilter = 0;
    // Zero for SettingsChoise or GL_NEAREST, GL_LINEAR, etc 
    private int magFilter = 0;
    // stands for GL_TEXTURE_MAX_LEVEL 
    private int mipsCount = 0;
    
    public int getBytesPerPel() {
        return bytesPerPel;
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
    
    public int getTarget() {
        return target;
    }
    
    public int getType() {
        return type;
    }
    
    public int getWidth() {
        return width;
    }
    
    public boolean loadFromStream(DataInputStream in) {
        boolean r = false;

        if (in != null) {
            try {
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

                r = true;
                
            } catch (IOException e) {
                Log.e(LogTag.TAG, "IOException while reading texHeader", e);
                r = false;
            }
        } 
        return r;
    }
}