package org.chemodansama.engine.render.imgui;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpVec2;
import org.chemodansama.engine.math.NpVec2i;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.utils.NpEndianness;

final public class NpFont {
    
    static private final int VERSION = 1;
    
    static public class NpFontCharStruct {
        
        final private char mCode;
        final private NpVec2i mSize;
        final private NpVec2i mPos;
        
        public NpFontCharStruct(DataInputStream in) throws IOException {
            mCode = NpEndianness.convertChar(in.readChar());
            mSize = new NpVec2i(NpEndianness.convertInt(in.readInt()),
                                NpEndianness.convertInt(in.readInt()));

            mPos = new NpVec2i(NpEndianness.convertInt(in.readInt()),
                               NpEndianness.convertInt(in.readInt()));
        }
        
        public char getCode() {
            return mCode;
        }
        
        public NpVec2i getSize() {
            return mSize;
        }
        
        public NpVec2i getPos() {
            return mPos;
        }
    }
    
    final private String mName;
    
    private NpTexture mTex = null;
    private TreeMap<Character, NpFontCharStruct> mChars = 
        new TreeMap<Character, NpFontCharStruct>();
    
    private int mColumnsCount = 0;
    private int mRowsCount = 0;
    private byte mFontSize = 0;
    
    private boolean loadChars(InputStream charsStream) {

        DataInputStream in = null;
        
        if (charsStream != null ) {
            in = new DataInputStream(charsStream);
        }

        if (in != null) {
            try {
                if (in.readByte() != VERSION) {
                    return false;
                }

                mFontSize = in.readByte();
                mColumnsCount = NpEndianness.convertInt(in.readInt());
                mRowsCount = NpEndianness.convertInt(in.readInt());
                
                int len = NpEndianness.convertInt(in.readInt());
                
                for (int i = 0; i < len; i++) {
                    NpFontCharStruct s = new NpFontCharStruct(in);
                    
                    mChars.put(s.getCode(), s); 
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
    
    public int getColumnsCount() {
        return mColumnsCount;
    }
    
    public int getRowsCount() {
        return mRowsCount;
    }
    
    public byte getSize() {
        return mFontSize;
    }
    
    public boolean hasName(String name) {
        return ((mName != null) && (mName.equals(name))) ? true : false;
    }
    
    public NpVec2 computeTextRect(float height, String s) {
        NpVec2 r = new NpVec2(0, 0);
        
        float ky = height / mFontSize;

        for (int i = 0; i < s.length(); i++) {
            
            NpVec2i cs = getSize(s.charAt(i));
            
            r.setX(r.getX() + cs.getX() * ky);
            r.setY(Math.max(r.getY(), ky * cs.getY()));
        }
        
        return r;
    }
    
    public NpTexture getTexture() {
        return mTex;
    }
    
    public NpFontCharStruct getChar(char c) {
        return mChars.get(c);
    }
    
    public NpVec2i getSize(char c) {
        NpFontCharStruct s = mChars.get(c);
        if (s != null) {
            return s.getSize();
        } else {
            return new NpVec2i(0, 0);
        }
    }
    
    public NpVec2i getPos(char c) {
        NpFontCharStruct s = mChars.get(c);
        if (s != null) {
            return s.getPos();
        } else {
            return new NpVec2i(0, 0);
        }
    }
}
