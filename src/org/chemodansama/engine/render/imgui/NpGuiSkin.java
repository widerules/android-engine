package org.chemodansama.engine.render.imgui;

import java.io.InputStream;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpVec2;
import org.chemodansama.engine.math.NpVec2i;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.render.NpTextureHeader;

class NpFontWithName {
    
    private NpFont mFont = null;
    private String mName = null;
    
    NpFontWithName() {
    }
    
    void setValues(String s, NpFont f) {
        mFont = f;
        mName = s;
    }
    
    String getName() {
        return mName;
    }
    
    NpFont getFont(String name) {
        if ((mName != null) && (mName.equals(name))) {
            return mFont;
        } else {
            return null;
        }
    }
    
    NpFont getFont() {
        return mFont;
    }
    
    boolean containsFont(String name) {
        return ((mName != null) && (mName.equals(name)));
    }
    
    boolean valid() {
        return (mFont != null) && (mName != null);
    }
    
    void reset() {
        mFont = null;
        mName = null;
    }
}

public class NpGuiSkin {
    
    static private NpGuiPolyBuffer mPolyBuffer = new NpGuiPolyBuffer();
    
    static private HashMap<String, NpFont> mFontsMap = new HashMap<String, NpFont>();
    static private NpFontWithName mActiveFont = new NpFontWithName();
    
    static private NpTexture mSkinTex = null;
    
    static public byte ALIGN_LEFT   = 0;
    static public byte ALIGN_CENTER = 1;
    static public byte ALIGN_RIGHT  = 2;
    
    static public boolean activateFont(GL10 gl, String name) {
        
        if (mActiveFont.containsFont(name)) {
            return true;
        } else {
            NpFont f = mFontsMap.get(name);
            
            if (f != null) {
                f.getTexture().bindGL10(gl);
                
                mActiveFont.setValues(name, f);
                
                return true;
            }
        }
        
        return false;
    }
    
    static public void addFont(String name, GL10 gl, InputStream texStream, 
            InputStream charsStream) {
        
        if (!mFontsMap.containsKey(name)) {
            NpFont f = new NpFont(gl, texStream, charsStream);
            
            mFontsMap.put(name, f);
        }
    }
    
    static public void bindTex(GL10 gl) {
        if (mSkinTex != null) {
            mSkinTex.bindGL10(gl);
        }
    }
    
    static public NpVec2 computeTextRect(String fontName, float height, byte[] s) {
        
        NpFont f = null;
        
        if ((f = mActiveFont.getFont(fontName)) == null) {
            f = mFontsMap.get(fontName);
        }
        
        if (f != null) {
            return f.computeTextRect(height, s);
        } else {
            // return zeroes 
            return new NpVec2();
        }
    }
    
    static private void drawChar(GL10 gl, byte ansiChar, float x, 
            float fontSize, float rectSize, 
            float InvTexWidth, float InvTexHeight, 
            float charTH, float charTW) {
        
        float xTexCoord = (ansiChar % 16) * rectSize;
        
        float yTexCoord = (ansiChar / 16) * rectSize;
        
        mPolyBuffer.pushQuad(gl, x, 0.0f, fontSize, fontSize, 
                             xTexCoord * InvTexWidth, 
                             1.0f - (yTexCoord + rectSize) * InvTexHeight, 
                             charTW, charTH);

    }
    
    static void drawRect(GL10 gl, int x, int y, int w, int h, 
            int tx, int ty, int tw, int th) {
    }
    
    static public void drawString(GL10 gl, byte[] s, float x, float y, 
            float fontSize, float textWidth, byte align) {
        
        NpFont f = mActiveFont.getFont();
        
        if (f != null) {

            gl.glPushMatrix();

            gl.glTranslatef(x, y, 0.0f);


            if (align == ALIGN_CENTER) {
                gl.glTranslatef(-textWidth * 0.5f, 0.0f, 0.0f);
            } else
                if (align == ALIGN_RIGHT) {
                    gl.glTranslatef(-textWidth, 0.0f, 0.0f);
                }

            NpTextureHeader fontTexHeader = f.getTexture().getHeader(); 

            int rectSize = fontTexHeader.getWidth() / 16;

            float Ky = fontSize / rectSize;

            NpVec2i CSPrev = f.getSize(s[0]);

            float InvTexWidth = 1.0f / fontTexHeader.getWidth();
            float InvTexHeight = 1.0f / fontTexHeader.getHeight();

            float tx = 0.0f;

            float charTW = rectSize * InvTexWidth;
            float charTH = rectSize * InvTexHeight;

            if (s.length > 0) {
                for (int i = 0; i < s.length - 1; i++) {

                    drawChar(gl, s[i], tx, fontSize, rectSize, 
                             InvTexWidth, InvTexHeight, 
                             charTW, charTH);

                    NpVec2i CSNext = f.getSize(s[i + 1]);

                    tx += Ky * (CSNext.getX() + CSPrev.getX()) * 0.5f;
                    CSPrev = CSNext;
                }

                drawChar(gl, s[s.length - 1], tx, fontSize, rectSize, 
                         InvTexWidth, InvTexHeight, 
                         charTW, charTH);
            }

            mPolyBuffer.flushRender(gl);

            gl.glPopMatrix();
        }
    }
    
    static void prepare(GL10 gl) {
        mActiveFont.reset();
    }
    
    static void finish(GL10 gl) {
    }
    
    static public void setSkinTex(NpTexture texture) {
        mSkinTex = texture;
    }
    
    private NpGuiSkin() {
        
    }
}
