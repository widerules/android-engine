package org.chemodansama.engine.render.imgui;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.NpHolder;
import org.chemodansama.engine.math.NpMath;
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
    
    static public NpWidgetState getWidgetState(int id) {
        if (NpGuiState.mActiveItem == id) {
            return NpWidgetState.WS_PUSHED; 
        } else if (NpGuiState.getHotItem() == id) {
            return NpWidgetState.WS_HOVER;
        } else {
            return NpWidgetState.WS_NORMAL;
        }
    }
    
    static private void drawButtonImage(GL10 gl, int id, String widgetLookName, 
            NpRect rect) {
        drawWidget(gl, getWidgetState(id), widgetLookName, rect);
    }
    
    static private void drawButtonText(GL10 gl, int id, 
            String caption, String font, float height, 
            NpVec4 color, NpRect rect) {
        
        if (!mTextureCache.activateFont(gl, font)) {
            return;
        }

        NpFont f = mTextureCache.getActiveFont();

        if (f == null) {
            return;
        }

        NpVec2 textRect = f.computeTextRect(height, caption);

        drawString(gl, caption, 
                   rect.getX() + (rect.getW() - textRect.getX()) * 0.5f, 
                   rect.getY() + (rect.getH() - textRect.getY()) * 0.5f, 
                   height, color, 
                   rect.getW(), ALIGN_LEFT);
    }
    
    static private int getButtonRetCode(int id, NpRect rect) {

        return getRectWidgetRetCode(id, rect);
        
    }
    
    static public int doButton(GL10 gl, int id, String widgetLookName, 
            String caption, String font, float fontHeight, 
            NpVec4 fontColor, NpRect rect) {
        
        drawButtonImage(gl, id, widgetLookName, rect);
     
        drawButtonText(gl, id, caption, font, fontHeight, fontColor, rect);

        return getButtonRetCode(id, rect);
    }
    
    static public int doLabel(GL10 gl, int id, float x, float y, 
            String caption, String font, 
            float fontHeight, NpVec4 fontColor, byte align) {
        
        if (!mTextureCache.activateFont(gl, font)) {
            return 0;
        }
        
        NpVec2 r = computeTextRect(font, fontHeight, caption);
        
        drawString(gl, caption, x, y, fontHeight, fontColor, r.getX(), align);

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
    
    static public int doRectWidget(GL10 gl, int id, NpWidgetState state, 
            String widgetLookName, NpRect rect) {
        
        drawWidget(gl, state, widgetLookName, rect);
        
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doRectWidget(GL10 gl, int id, String widgetLookName, 
            NpRect rect) {
        
        drawWidget(gl, getWidgetState(id), widgetLookName, rect);
        
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doVertSlider(GL10 gl, int id, String widgetLookName,
            float x, float y, float h, NpHolder<Float> slidePos) {

        int r = GUI_RETURN_FLAG_NORMAL;
        
        String bgName = widgetLookName + "Bg";
        
        NpWidgetState state = getWidgetState(id);

        NpRect rect = new NpRect();
        
        if (!getWidgetRectDefW(gl, state, bgName, x, y, h, rect)) {
            return r;
        }
        
        drawWidget(gl, state, bgName, rect);
        
        r = getRectWidgetRetCode(id, rect);
        
        if ((r & GUI_RETURN_FLAG_ACTIVE) > 0) {
            slidePos.value = (float)(NpGuiState.getMouseY() - y) / h;
            slidePos.value = NpMath.clampf(slidePos.value, 0, 1);
        }
        
        float slideY = slidePos.value * rect.getH();
        
        getWidgetRectDefWH(gl, state, widgetLookName, x, y + slideY, rect);
        
        rect.setH(32);
        
        if (rect.getY() + rect.getH() > y + h) {
            rect.setY(y + h - rect.getH());
        }
        
        drawWidget(gl, state, widgetLookName, rect);
        
        return r;
    }

    static void drawRectWH(GL10 gl, float x, float y, float w, float h, 
            float tx, float ty, float tw, float th) {
        mPolyBuffer.pushQuad(gl, x, y, x + w, y + h, tx, ty, tx + tw, ty + th);
    }
    
    static void drawRect(GL10 gl, float x1, float y1, float x2, float y2, 
            float tx1, float ty1, float tx2, float ty2) {
        mPolyBuffer.pushQuad(gl, x1, y1, x2, y2, tx1, ty1, tx2, ty2);
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
            
            float tcX = cs.getPosX() * rectSizeX;
            float tcY = cs.getPosY() * rectSizeY;
            
            float charW = cs.getSizeX() * Ky;
            float charH = cs.getSizeY() * Ky;

            float x1 = tx - Ky * (rectSizeX - cs.getSizeX()) * 0.5f;
            
            float tx1 = tcX * InvTexWidth;
            float ty1 = 1.0f - tcY * InvTexHeight;
            
            drawRect(gl, x1, 0, x1 + rectSizeX * Ky, charH, 
                     tx1, ty1, tx1 + charTW, ty1 - charTH);
            
            tx += charW;
        }
        
        // flush render on matrix change 
        mPolyBuffer.flushRender(gl);

        gl.glPopMatrix();
    }
    
    static private boolean getWidgetRectDefW(GL10 gl, NpWidgetState state, 
            String widgetName, float x, float y, float h, NpRect out) {
        NpWidgetlook look = mScheme.getWidget(widgetName);
        
        if (look == null) {
            return false;
        }        
        
        NpWidgetStatelook stateLook = look.getStateLook(NpWidgetState.WS_NORMAL);
        
        if (stateLook == null) {
            return false;
        }

        out.set(x, y, stateLook.getDefaultWidth(mScheme, stateLook), h);
        
        return true;
    }
    
    static private boolean getWidgetRectDefWH(GL10 gl, NpWidgetState state, 
            String widgetName, float x, float y, NpRect out) {
        NpWidgetlook look = mScheme.getWidget(widgetName);
        
        if (look == null) {
            return false;
        }        
        
        NpWidgetStatelook stateLook = look.getStateLook(NpWidgetState.WS_NORMAL);
        
        if (stateLook == null) {
            return false;
        }

        out.set(x, y, 
                stateLook.getDefaultWidth(mScheme, stateLook), 
                stateLook.getDefaultHeight(mScheme, stateLook));
        
        return true;
    }
    
    static private void drawWidget(GL10 gl, NpWidgetState state, 
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
        gl.glColor4f(1, 1, 1, 1);
        
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
            
            if (!mTextureCache.activateTexture(gl, texture)) {
                continue;
            }
            
            float x1 = rect.getX() + x;
            float y1 = rect.getY() + y;
            
            float tx1 = (float) im.getXPos() / tw;
            float ty1 = 1.0f - (float) im.getYPos() / th;
            
            drawRect(gl, x1, y1, x1 + w, y1 + h, 
                     tx1, ty1, 
                     tx1 + (float) im.getWidth() / tw, 
                     ty1 - (float) im.getHeight() / th);
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
    
    /**
     * @param gl
     * @param texture
     * @return returns true if specified texture is bound to gapi,
     *         otherwise returns false
     */
    public boolean activateTexture(GL10 gl, NpTexture texture) {

        if (doActivateTexture(gl, texture)) {
            mActiveFont = null;
        }

        return (mActiveTexture != null) ? 
                mActiveTexture.equalsToTexture(texture) : false;
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
     * @return returns true if texture activating has occurred, 
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
