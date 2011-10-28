package org.chemodansama.engine.render;

import java.io.DataInputStream;
import java.io.IOException;

public class NpPvrHeader {
    public final int headerSize; /* size of the structure */
    public final int height; /* height of surface to be created */
    public final int width; /* width of input surface */
    public final int mipMapCount; /* number of MIP-map levels requested */
    public final int pixelFormatFlags; /* pixel format flags */
    public final int dataSize; /* Size of the compress data */
    public final int bitCount; /* number of bits per pixel */
    public final int rBitMask; /* mask for red bit */
    public final int gBitMask; /* mask for green bits */
    public final int bBitMask; /* mask for blue bits */
    public final int alphaBitMask; /* mask for alpha channel */
    public final int pvr; /* should be 'P' 'V' 'R' '!' */
    public final int numSurfs; /* number of slices for volume textures or skyboxes */
    
    public NpPvrHeader(DataInputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("in == null");
        }
        
        headerSize       = Integer.reverseBytes(in.readInt());
        height           = Integer.reverseBytes(in.readInt()); 
        width            = Integer.reverseBytes(in.readInt()); 
        mipMapCount      = Integer.reverseBytes(in.readInt()); 
        pixelFormatFlags = Integer.reverseBytes(in.readInt()); 
        dataSize         = Integer.reverseBytes(in.readInt()); 
        bitCount         = Integer.reverseBytes(in.readInt()); 
        rBitMask         = Integer.reverseBytes(in.readInt()); 
        gBitMask         = Integer.reverseBytes(in.readInt()); 
        bBitMask         = Integer.reverseBytes(in.readInt()); 
        alphaBitMask     = Integer.reverseBytes(in.readInt()); 
        pvr              = Integer.reverseBytes(in.readInt()); 
        numSurfs         = Integer.reverseBytes(in.readInt()); 
    }
}
