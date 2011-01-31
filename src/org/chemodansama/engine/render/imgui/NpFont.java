package org.chemodansama.engine.render.imgui;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpVec2;
import org.chemodansama.engine.math.NpVec2i;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.utils.NpEndianness;

public class NpFont {
    
    final private String mName;
    
    private NpTexture mTex = null;
    private NpVec2i[] mCharSize = new NpVec2i[256];
    
    private boolean loadChars(InputStream charsStream) {

        DataInputStream in = null;
        
        if (charsStream != null ) {
            in = new DataInputStream(charsStream);
        }

        if (in != null) {
            try {
                for (int i = 0; i < 256; i++) {
                    int x = NpEndianness.convertInt(in.readInt());
                    int y = NpEndianness.convertInt(in.readInt());
                    mCharSize[i] = new NpVec2i(x, y);
                }
            } catch (IOException e) {
                return false;
            }
        }
      
        return true;
    }
    
    public NpFont(GL10 gl, String name, InputStream texStream, 
            InputStream charsStream) {

        mName = name;
        
        loadChars(charsStream);
        
        mTex = new NpTexture();
        
        if (mTex.initFromStream(texStream)) {
            mTex.initGL10(gl);
        }
    }
    
    public String getName() {
        return mName;
    }
    
    public boolean hasName(String name) {
        return ((mName != null) && (mName.equals(name))) ? true : false;
    }
    
    public NpVec2 computeTextRect(float height, byte[] s) {
        NpVec2 r = new NpVec2(0, 0);
        
        float rectSize = mTex.getHeader().getWidth() / 16.0f;
        
        float ky = height / rectSize;
        
        if (s.length > 0) {
            r.setX((height - ky * mCharSize[s[0]].getX()) / 2.0f);
                
            for (byte b : s) {
                r.setX(r.getX() + ky * mCharSize[b].getX());
                r.setY(Math.max(r.getY(), ky * mCharSize[b].getY()));
            }
        }
        
        return r;
    }
    
    public NpTexture getTexture() {
        return mTex;
    }
    
    public final NpVec2i getSize(int i) {
        if ((i >= 0) && (i < 256)) {
            return mCharSize[i];
        } else
            return null;
    }
}
