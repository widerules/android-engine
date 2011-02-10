package org.chemodansama.engine.render.imgui;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.math.NpVec2;
import org.chemodansama.engine.math.NpVec4;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.render.NpTextureHeader;
import org.chemodansama.engine.render.imgui.NpFont.NpFontCharStruct;

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
            String text) {
        
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
    
    static private int getRectWidgetRetCode(int id, NpRect rect) {
        
        boolean r = rect.overlapsPoint(NpGuiState.getMouseX(), 
                                       NpGuiState.getMouseY());
        
        int ret = 0;
        
        if (NpGuiState.getMouseDown()) {
            
            if (r) {
               
                if ((NpGuiState.getHotItem() == id) 
                        && (NpGuiState.mActiveItem == 0)) {
                
                    NpGuiState.mActiveItem = id;
                
                }
                
                NpGuiState.setHotItem(id);
            } else {
                if (NpGuiState.getHotItem() == id) {
                    NpGuiState.setHotItem(0);
                    
                    ret |= GUI_RETURN_FLAG_MOUSE_MOVED_OUT;
                }
            }
        } else {
            
            if (NpGuiState.mActiveItem == id) {

                NpGuiState.mActiveItem = 0;
                
                if (NpGuiState.getHotItem() == id) {
                    NpGuiState.setHotItem(0);
                    ret |= GUI_RETURN_FLAG_CLICKED;
                }
            }
        }
        
        ret |= GUI_RETURN_FLAG_NORMAL;

        if (NpGuiState.getHotItem() == id) {
            ret |= GUI_RETURN_FLAG_HOT;
        } 

        if (NpGuiState.mActiveItem == id) {
            ret |= GUI_RETURN_FLAG_ACTIVE;
        }
        
        return ret;
    }
    
    static private void drawButtonImage(GL10 gl, int id, String widgetLookName, 
            NpRect rect) {
        
        NpWidgetState state;
        
        if (NpGuiState.mActiveItem == id) {
            state = NpWidgetState.WS_PUSHED; 
        } else if (NpGuiState.getHotItem() == id) {
            state = NpWidgetState.WS_HOVER;
        } else {
            state = NpWidgetState.WS_NORMAL;
        }
        
        drawWidget(gl, state, widgetLookName, rect);
    }
    
    static private void drawButtonText(GL10 gl, int id, 
            String fontName, String caption, float fontHeight, 
            NpVec4 fontColor, NpRect rect) {
        
        if (!mTextureCache.activateFont(gl, fontName)) {
            return;
        }

        NpFont f = mTextureCache.getActiveFont();

        if (f == null) {
            return;
        }

        NpVec2 textRect = f.computeTextRect(fontHeight, caption);

        drawString(gl, caption, 
                   rect.getX() + (rect.getW() - textRect.getX()) * 0.5f, 
                   rect.getY() + (rect.getH() - textRect.getY()) * 0.5f, 
                   fontHeight, fontColor, 
                   rect.getW(), ALIGN_LEFT);
    }
    
    static private int getButtonRetCode(int id, NpRect rect) {

        return getRectWidgetRetCode(id, rect);
        
    }
    
    static public int doButton(GL10 gl, int id, String widgetLookName, 
            String fontName, String caption, float fontHeight, 
            NpVec4 fontColor, NpRect rect) {
        
        drawButtonImage(gl, id, widgetLookName, rect);
     
        drawButtonText(gl, id, fontName, caption, fontHeight, fontColor, rect);

        return getButtonRetCode(id, rect);
    }
    
    static public int doLabel(GL10 gl, int id, float x, float y, 
            String asciiText, String font, 
            NpVec4 fontColor, float fontHeight, byte align) {
        
        if (!mTextureCache.activateFont(gl, font)) {
            return 0;
        }
        
        NpVec2 r = computeTextRect(font, fontHeight, asciiText);
        
        drawString(gl, asciiText, x, y, fontHeight, fontColor, r.getX(), align);

        float offs;
        
        if (align == ALIGN_CENTER) {
            offs = r.getX() * 0.5f;
        } else if (align == ALIGN_RIGHT) {
            offs = r.getX();
        } else {
            offs = 0;
        }
        
        return getRectWidgetRetCode(id, new NpRect(x - offs, y, 
                                                   r.getX(), r.getY()));
    }
    
    static void drawRect(GL10 gl, float x, float y, float w, float h, 
            float tx, float ty, float tw, float th) {
        mPolyBuffer.pushQuad(gl, x, y, w, h, tx, ty, tw, th);
    }
    
    static public void drawString(GL10 gl, String s, float x, float y, 
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

        int rectSizeX = fontTexHeader.getWidth() / f.getColumnsCount();
        int rectSizeY = fontTexHeader.getHeight() / f.getRowsCount();

        float Ky = fontSize / f.getSize();

        float InvTexWidth = 1.0f / fontTexHeader.getWidth();
        float InvTexHeight = 1.0f / fontTexHeader.getHeight();

        float tx = 0.0f;

        float charTW = rectSizeX * InvTexWidth;
        float charTH = rectSizeY * InvTexHeight;

        for (int i = 0; i < s.length(); i++) {
            
            NpFontCharStruct cs = f.getChar(s.charAt(i));
            
            if (cs == null) {
                continue;
            }
            
            float xTexCoord = cs.getPos().getX() * rectSizeX;
            float yTexCoord = cs.getPos().getY() * rectSizeY;
            
            float charW = cs.getSize().getX() * Ky;
            float charH = cs.getSize().getY() * Ky;

            drawRect(gl, tx - Ky * (rectSizeX - cs.getSize().getX()) * 0.5f,
                     0.0f, rectSizeX * Ky, charH,
                     xTexCoord * InvTexWidth, 
                     1.0f - (yTexCoord + rectSizeY) * InvTexHeight, 
                     charTW, charTH);
        
            mPolyBuffer.flushRender(gl);
            
            tx += charW;
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
