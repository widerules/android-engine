package org.chemodansama.engine.render;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

public class NpPvrTextureData extends NpTextureData {

    public NpPvrTextureData(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected NpTextureHeader loadData(InputStream in, 
            ArrayList<ByteBuffer> mips) throws IOException {
        
        if (in == null) {
            throw new IllegalArgumentException("in is null");
        }
        
        NpTextureHeader header = 
                new NpTextureHeader(new NpPvrHeader(new DataInputStream(in)), 
                                    GL10.GL_NEAREST, GL10.GL_NEAREST);
        
        byte[] mipData = new byte[header.getMipSize(0)];
        
        for (int i = 0; i < header.getMipsCount(); i++) {
            int mipSize = header.getMipSize(i);
            in.read(mipData, 0, mipSize);
            
            ByteBuffer b = ByteBuffer.allocateDirect(mipSize);
            b.put(mipData, 0, mipSize);
            b.position(0);
            mips.add(b);
        }
        
        return header;
    }

}
