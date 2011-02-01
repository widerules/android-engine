package org.chemodansama.engine.render.imgui;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.apache.http.util.EncodingUtils;
import org.chemodansama.engine.math.NpVec2;
import org.chemodansama.engine.math.NpVec2i;
import org.chemodansama.engine.math.NpVec4;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.render.NpTextureHeader;

import android.content.res.AssetManager;

/**
 * NpSkin - takes care of all rendering aspects of the gui. 
 *          Client code must call the prepare() method at start of the gui 
 *          render and call finish() method when its done. 
 *          Also, client code must use only NpSkin methods to work with 
 *          gapi state (no texture bindings or other state changes between 
 *          prepare() and finish() calls).
 */
public final class NpSkin implements NpGuiReturnConsts, NpAlignConsts {

    static private NpPolyBuffer mPolyBuffer;
    static private NpSkinScheme mScheme = null;
    static private NpTextureCache mTextureCache = null;
    
    static {
        mPolyBuffer = new NpPolyBuffer(16);
        mTextureCache = new NpTextureCache(mPolyBuffer);
    }
    
    static public NpVec2 computeTextRect(String fontName, float height, 
            byte[] text) {
        
        NpFont f = null;
        
        if ((f = mTextureCache.getActiveFont(fontName)) == null) {
            f = mScheme.getFonts().get(fontName);
        }
        
        if (f != null) {
            return f.computeTextRect(height, text);
        } else {
            // return zeroes 
            return new NpVec2();
        }
    }
    
    static public int doButton(GL10 gl, int id, String widgetLookName, 
            String fontName, byte[] caption, float fontHeight, 
            NpVec4 fontColor, NpRect rect) {
        
        int ret = 0;
        
        if (NpGuiState.regionHit(rect.getX(), rect.getY(), 
                                 rect.getW(), rect.getH())) {
            NpGuiState.mHotItem = id;
            
            if ((NpGuiState.mActiveItem == 0) && (NpGuiState.mMouseDown)) {
                NpGuiState.mActiveItem = id;
            }
        }
        
        ret |= GUI_RETURN_FLAG_NORMAL;
        
        if (NpGuiState.mHotItem == id) {
            ret |= GUI_RETURN_FLAG_HOT;
        } 
        
        if (NpGuiState.mActiveItem == id) {
            ret |= GUI_RETURN_FLAG_ACTIVE;
        }

        if ((!NpGuiState.mMouseDown) && (NpGuiState.mHotItem == id) 
                && (NpGuiState.mActiveItem == id)) {
            ret |= GUI_RETURN_FLAG_CLICKED;
        }
        
        NpWidgetState state;
        
        if ((ret & GUI_RETURN_FLAG_ACTIVE) > 0) {
            state = NpWidgetState.WS_PUSHED; 
        } else if ((ret & GUI_RETURN_FLAG_HOT) > 0) {
            state = NpWidgetState.WS_HOVER;
        } else {
            state = NpWidgetState.WS_NORMAL;
        }
        
        drawWidget(gl, state, widgetLookName, rect);
     
        if (mTextureCache.activateFont(gl, fontName)) {
            
            NpFont f = mTextureCache.getActiveFont();
            
            if (f == null) {
                return ret;
            }
            
            NpVec2 textRect = f.computeTextRect(fontHeight, caption);
            
            drawString(gl, caption, 
                       rect.getX() + (rect.getW() - textRect.getX()) * 0.5f, 
                       rect.getY() + (rect.getH() - textRect.getY()) * 0.5f, 
                       fontHeight, fontColor, 
                       rect.getW(), ALIGN_LEFT);
        }
        
        return ret;
    }
    
    static public int doLabel(GL10 gl, int id, float x, float y, 
            byte[] asciiText, String font, 
            NpVec4 fontColor, float fontHeight, byte align) {
        
        int ret = 0;
    
        if (!mTextureCache.activateFont(gl, font)) {
            return ret;
        }
        
        NpVec2 r = computeTextRect(font, fontHeight, asciiText);
        
        float offs = 0;
        
        if (align == ALIGN_CENTER) {
            offs = r.getX() * 0.5f;
        } else if (align == ALIGN_RIGHT) {
            offs = r.getX();
        };
        
        boolean hit = NpGuiState.regionHit(x - offs, y, 
                                           r.getX(), r.getY());
        
        if (hit) {
            if (NpGuiState.mHotItem != id) {
                ret |= GUI_RETURN_FLAG_MOUSE_MOVED_IN; 
                NpGuiState.mHotItem = id;
            }
            
            if ((NpGuiState.mActiveItem == 0) && NpGuiState.mMouseDown) {
                NpGuiState.mActiveItem = id;
            }
        } else if (NpGuiState.mHotItem == id) {
            NpGuiState.mHotItem = 0;
            ret |= GUI_RETURN_FLAG_MOUSE_MOVED_OUT;
        }
        
        ret |= GUI_RETURN_FLAG_NORMAL;
        
        if (NpGuiState.mHotItem == id) {
            ret |= GUI_RETURN_FLAG_HOT;
            if (NpGuiState.mActiveItem == id) {
                ret |= GUI_RETURN_FLAG_ACTIVE;
            }
        } else if (NpGuiState.mActiveItem == id) {
            ret |= GUI_RETURN_FLAG_ACTIVE;
        }

        drawString(gl, asciiText, x, y, fontHeight, fontColor, r.getX(), align);
        
        if (!NpGuiState.mMouseDown && (NpGuiState.mHotItem == id) 
                && (NpGuiState.mActiveItem == id)) {
            ret |= GUI_RETURN_FLAG_CLICKED;
        }
        
        return ret;
    }
    
    static public int doLabel(GL10 gl, int id, float x, float y, 
            String text, String font, 
            NpVec4 fontColor, float fontHeight, byte align) {
        
        return doLabel(gl, id, x, y, EncodingUtils.getAsciiBytes(text), font, 
                       fontColor, fontHeight, align);
    }
    
    static private void drawChar(GL10 gl, byte ansiChar, float x, 
            float fontSize, float rectSize, 
            float InvTexWidth, float InvTexHeight, 
            float charTH, float charTW) {
        
        float xTexCoord = (ansiChar % 16) * rectSize;
        
        float yTexCoord = (ansiChar / 16) * rectSize;
        
        drawRect(gl, x, 0.0f, fontSize, fontSize, 
                 xTexCoord * InvTexWidth, 
                 1.0f - (yTexCoord + rectSize) * InvTexHeight, 
                 charTW, charTH);
    }
    
    static void drawRect(GL10 gl, float x, float y, float w, float h, 
            float tx, float ty, float tw, float th) {
        mPolyBuffer.pushQuad(gl, x, y, w, h, tx, ty, tw, th);
    }
    
    static public void drawString(GL10 gl, byte[] s, float x, float y, 
            float fontSize, NpVec4 fontColor, float textWidth, byte align) {
        
        NpFont f = mTextureCache.getActiveFont();
        
        if (f == null) {
            return;
        }

        gl.glPushMatrix();
        
        // flush render on GL states change 
        mPolyBuffer.flushRender(gl);
        
        gl.glTranslatef(x, y, 0.0f);

        gl.glColor4f(fontColor.getX(), fontColor.getY(), fontColor.getZ(), 
                     fontColor.getW());
        
        if (align == ALIGN_CENTER) {
            gl.glTranslatef(-textWidth * 0.5f, 0.0f, 0.0f);
        } else if (align == ALIGN_RIGHT) {
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
                     InvTexWidth, InvTexHeight, charTW, charTH);
        }

        // flush render on matrix change 
        mPolyBuffer.flushRender(gl);

        gl.glPopMatrix();
    }
    
    static public void drawWidget(GL10 gl, NpWidgetState state, 
            String widgetName, NpRect rect) {
        
        if (mScheme == null) {
            return;
        }
        
        NpWidgetlook widget = mScheme.getWidget(widgetName);
        
        if (widget == null) {
            return;
        }
        
        NpWidgetStatelook stateLook = widget.getStateLook(state);
        
        if (stateLook == null) {
            return;
        }
        
        mPolyBuffer.flushRender(gl);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        
        for (NpWidgetArea area : stateLook.getAreas()) {
            
            NpWidgetImage image = stateLook.findImageByArea(area.getName());
            
            if (image == null) {
                continue;
            }
            
            NpSkinImageSet imageSet = mScheme.getImageSet(image.getImageset());
            
            if (imageSet == null) {
                continue;
            }
            
            NpSkinImageSet.NpSkinImage im = imageSet.getImage(image.getImage());
            
            if (im == null) {
                continue;
            }
            
            NpTexture texture = imageSet.getTexture();
            
            if (texture == null) {
                continue;
            }

            float x = area.getX().getValue(mScheme, stateLook, rect);
            float y = area.getY().getValue(mScheme, stateLook, rect);
            float w = area.getWidth().getValue(mScheme, stateLook, rect);
            float h = area.getHeight().getValue(mScheme, stateLook, rect);
            
            int tw = texture.getHeader().getWidth();
            int th = texture.getHeader().getHeight();
            
            mTextureCache.activateTexture(gl, texture);
            
            drawRect(gl, rect.getX() + x, rect.getY() + y, w, h, 
                     (float) im.getXPos() / tw, 
                     1.0f - (float) im.getYPos() / th, 
                     (float) im.getWidth() / tw, 
                     -(float) im.getHeight() / th);
        }
    }
    
    static void finish(GL10 gl) {
        mPolyBuffer.flushRender(gl);
    }
    
    static public boolean loadScheme(GL10 gl, AssetManager assets, 
            String schemeFileName) {
        
        mScheme = new NpSkinScheme(gl, assets, schemeFileName);
        
        mTextureCache.setFonts(gl, mScheme.getFonts());
        
        return true;
    }
    
    static void prepare(GL10 gl) {
        mTextureCache.reset(gl);
    }
    
    private NpSkin() {
        
    }
}

/**
 * NpTextureCache - caches active texture and font (since any font has exactly 
 *                  one texture).   
 */
final class NpTextureCache {
    private NpFont mActiveFont = null;
    private NpTexture mActiveTexture = null;
    
    private NpPolyBuffer mPolyBuffer = null;
    private HashMap<String, NpFont> mFontsMap = null;
    
    NpTextureCache(NpPolyBuffer polyBuffer) {
        mPolyBuffer = polyBuffer;
        
        if (mPolyBuffer == null) {
            throw new NullPointerException();
        }
    }
    
    /**
     * activateFont binds font with gl state
     * @param gl 
     * @param name alias of the font
     * @return returns true if specified font is bound to gapi, 
     *         otherwise returns false
     */
    public boolean activateFont(GL10 gl, String name) {
        
        if ((mActiveFont != null) && (mActiveFont.hasName(name))) {
            return true;
        } else {
            if (mFontsMap != null) {
                mActiveFont = mFontsMap.get(name);
            }
            
            if (mActiveFont != null) {
                doActivateTexture(gl, mActiveFont.getTexture());
                return true;
            }
        }
        
        return false;
    }
    
    public boolean activateTexture(GL10 gl, NpTexture texture) {

        if (doActivateTexture(gl, texture)) {
            mActiveFont = null;
        }
        
        return mActiveTexture.equalsToTexture(texture);
    }
    
    public void activateTextureNoRet(GL10 gl, NpTexture texture) {
        
        if (doActivateTexture(gl, texture)) {
            mActiveFont = null;
        }
    }
    
    /**
     * doActivateTexture binds texture with gl state
     * @param gl
     * @param texture
     * @return returns true if texture activating has been occurred, 
     *         otherwise returns false
     */
    private boolean doActivateTexture(GL10 gl, NpTexture texture) {

        if (texture == null) {
            return false;
        }
        
        if (!texture.equalsToTexture(mActiveTexture)) {
            
            // flush buffered quads on each texture change
            mPolyBuffer.flushRender(gl);
            
            texture.bindGL10(gl);
            mActiveTexture = texture;
            
            return true;
        }
        
        return false;
    }
    
    public NpFont getActiveFont() {
        return mActiveFont;
    }
    
    public NpFont getActiveFont(String name) {
        
        if ((mActiveFont != null) && (mActiveFont.hasName(name))) {
            return mActiveFont;
        } else {
            return null;
        }
    }
    
    public void reset(GL10 gl) {
        
        mPolyBuffer.flushRender(gl);
        
        mActiveFont = null;
        mActiveTexture = null;
    }
    
    public void setFonts(GL10 gl, HashMap<String, NpFont> fonts) {
        reset(gl);
        mFontsMap = fonts;
    }
}
