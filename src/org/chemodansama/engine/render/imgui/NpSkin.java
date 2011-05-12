package org.chemodansama.engine.render.imgui;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.NpHolder;
import org.chemodansama.engine.math.NpMath;
import org.chemodansama.engine.math.NpVec4;
import org.chemodansama.engine.render.NpTexture;
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

    private static NpPolyBuffer mPolyBuffer;
    private static NpSkinScheme mScheme = null;
    private static NpTextureCache mTextureCache = null;
    private static GL10 mGL = null;
    
    static {
        mPolyBuffer = new NpPolyBuffer(16);
        mTextureCache = new NpTextureCache(mPolyBuffer);
    }
    
    private static NpFont getFont(String name) {
        NpFont f = null;
        
        if ((f = mTextureCache.getActiveFont(name)) == null) {
            f = mScheme.getFonts().get(name);
        }
        
        return f;
    }
    
    public static float computeFontHeight(NpFontParams font) {
        NpFont f = getFont(font.name);
        return (f != null) ? f.computeTextHeight(font.height, "A") : 0;
    }
    
    public static NpRecti computeTextRect(String fontName, float height, 
            String text) {
        NpFont f = getFont(fontName);
        return (f != null) ? f.computeTextRect(height, text) : new NpRecti(); 
    }
    
    static private int getRectWidgetRetCode(int id, NpRecti rect) {
        
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
    
    static private void drawButtonText(int id, 
            String caption, NpFontParams font, NpRecti rect) {
        
        if ((caption == null) || (caption.length() == 0)) {
            return;
        }
        
        GL10 gl = mGL;
        
        if (!mTextureCache.activateFont(gl, font.name)) {
            return;
        }

        NpFont f = mTextureCache.getActiveFont();

        if (f == null) {
            return;
        }

        NpRecti textRect = f.computeTextRect(font.height, caption);

        int x = rect.getX() + (rect.getW() - textRect.getW()) / 2 
                - textRect.getX();
        
        int y = rect.getY() 
                + (int) ((rect.getH() + f.getXHeight(font.height)) * 0.5);
        
        drawString(caption, x, y, font.height, font.color);
    }
    
    static public int doButton(int id, String widgetLookName, 
            String caption, NpFontParams font, NpRecti rect, NpVec4 color) {
        drawWidget(getWidgetState(id), widgetLookName, rect, color);
        drawButtonText(id, caption, font, rect);
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doButton(int id, String widgetLookName, 
            String caption, NpFontParams font, NpRecti rect) {
        drawWidget(getWidgetState(id), widgetLookName, rect);
        drawButtonText(id, caption, font, rect);
        return getRectWidgetRetCode(id, rect);
    }
        
    // bunch of parameters :E
    static public int doLabel(int id, int x, int y, 
            String caption, NpFontParams font, int maxWidth, 
            NpHolder<Integer> outHeight) {

        
        return 0;
    }
    
    
    static public int doLabel(int id, int x, int y, 
            String caption, NpFontParams font, byte align) {
        
        GL10 gl = mGL;
        
        if (!mTextureCache.activateFont(gl, font.name)) {
            return 0;
        }
        
        NpRecti r = computeTextRect(font.name, font.height, caption);
        
        if (align == ALIGN_CENTER) {
            x -= r.getW() * 0.5f;
        } else if (align == ALIGN_RIGHT) {
            x -= r.getW();
        };
        
        x -= r.getX();
        
        drawString(caption, x, y, font.height, font.color);
        
        return getRectWidgetRetCode(id, new NpRecti(x, y, 
                                                    r.getX(), r.getY()));
    }
    
    static public int doRectWidget(int id, NpWidgetState state, 
            String widgetLookName, NpRecti rect) {
        
        drawWidget(state, widgetLookName, rect);
        
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doRectWidget(int id, String widgetLookName, 
            NpRecti rect) {
        
        drawWidget(getWidgetState(id), widgetLookName, rect);
        
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doVertSlider(int id, String widgetLookName,
            float x, float y, float h, NpHolder<Float> slidePos) {

        int r = GUI_RETURN_FLAG_NORMAL;
        
        String bgName = widgetLookName + "Bg";
        
        NpWidgetState state = getWidgetState(id);

        NpRecti rect = new NpRecti();
        
        if (!getWidgetRectDefW(state, bgName, x, y, h, rect)) {
            return r;
        }
        
        drawWidget(state, bgName, rect);
        
        r = getRectWidgetRetCode(id, rect);
        
        if ((r & GUI_RETURN_FLAG_ACTIVE) > 0) {
            slidePos.value = (float)(NpGuiState.getMouseY() - y) / h;
            slidePos.value = NpMath.clampf(slidePos.value, 0, 1);
        }
        
        float slideY = slidePos.value * rect.getH();
        
        getWidgetRectDefWH(state, widgetLookName, x, y + slideY, rect);
        
        rect.setH(32);
        
        if (rect.getY() + rect.getH() > y + h) {
            rect.setY(y + h - rect.getH());
        }
        
        drawWidget(state, widgetLookName, rect);
        
        return r;
    }

    static void drawRectWH(float x, float y, float w, float h, 
            float tx, float ty, float tw, float th) {
        mPolyBuffer.pushQuad(mGL, x, y, x + w, y + h, tx, ty, tx + tw, ty + th);
    }
    
    static void drawRect(float x1, float y1, float x2, float y2, 
            float tx1, float ty1, float tx2, float ty2) {
        mPolyBuffer.pushQuad(mGL, x1, y1, x2, y2, tx1, ty1, tx2, ty2);
    }    
    
    static public void drawString(String s, int x, int y, 
            float fontSize, NpVec4 fontColor) {
        
        GL10 gl = mGL; 
        
        NpFont f = mTextureCache.getActiveFont();
        
        if (f == null) {
            return;
        }

        gl.glPushMatrix();
        
        // flush render on GL states change 
        mPolyBuffer.flushRender(gl);
        
        gl.glTranslatef(x, y, 0);

        gl.glColor4f(fontColor.getX(), fontColor.getY(), fontColor.getZ(), 
                     fontColor.getW());
        
        float Ky = fontSize / f.getSize();

        float tx = 0;

        for (int i = 0; i < s.length(); i++) {
            
            NpFontCharStruct cs = f.getChar(s.charAt(i));
            
            if (cs == null) {
                continue;
            }
            
            NpImmutableRect verts = cs.getRenderRect();
            NpImmutableRect texCoords = cs.getTextureRect();
            
            drawRectWH(tx + Ky * verts.getX(),
                       Ky * verts.getY(), 
                       Ky * verts.getW(), 
                       Ky * verts.getH(), 
                       
                       texCoords.getX(),
                       texCoords.getY(), 
                       texCoords.getW(), 
                       texCoords.getH());
            
            tx += cs.getAdvance() * Ky;
        }
        
        // flush render on matrix change 
        mPolyBuffer.flushRender(gl);

        gl.glPopMatrix();
    }
    
    static private boolean getWidgetRectDefW(NpWidgetState state, 
            String widgetName, float x, float y, float h, NpRecti out) {
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
    
    static private boolean getWidgetRectDefWH(NpWidgetState state, 
            String widgetName, float x, float y, NpRecti out) {
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

    static private void drawWidget(NpWidgetState state, 
            String widgetName, NpRecti rect) {
        drawWidget(state, widgetName, rect, NpVec4.ONE);
    }
    
    static private void drawWidget(NpWidgetState state, 
            String widgetName, NpRecti rect, NpVec4 color) {
        
        GL10 gl = mGL;
        
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
        gl.glColor4f(color.getX(), color.getY(), color.getZ(), color.getW());
        
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
            
            float tw1 = (float) im.getWidth() / tw;
            float th1 = -(float) im.getHeight() / th;
            
            float th1m = im.getHeight();
            float tw1m = im.getWidth();
            
            switch (area.getWidthScale()) {
            case STRETCH:
                
                switch (area.getHeightScale()) {
                case STRETCH:
                    drawRectWH(x1, y1, w, h, tx1, ty1, tw1, th1);
                    break;
                    
                case REPEAT:
                    
                    int hn = (int) Math.floor(h / th1m);
                    
                    for (int i = 0; i < hn; i++) {
                        drawRectWH(x1, y1 + th1m * i, w, th1m, 
                                   tx1, ty1, tw1, th1);
                    }
                    
                    float ceilPart = h - th1m * hn;
                    
                    drawRectWH(x1, y1 + th1m * hn, w, ceilPart, 
                               tx1, ty1, tw1, ceilPart);
                    
                    break;

                default:
                    break;
                }
                
                break;
                
            case REPEAT:
                
                switch (area.getHeightScale()) {
                case STRETCH:
                    int wn = (int) Math.floor(w / tw1m);
                    
                    for (int i = 0; i < wn; i++) {
                        drawRectWH(x1 + tw1m * i, y1, tw1m, h, 
                                   tx1, ty1, tw1, th1);
                    }
                    
                    float ceilPart = w - tw1m * wn;
                    
                    drawRectWH(x1 + tw1m * wn, y1, ceilPart, h, 
                               tx1, ty1, ceilPart, th1);
                    break;
                    
                case REPEAT:
                    
                    int hn = (int) Math.floor(h / th1m);
                    
                    wn = (int) Math.floor(w / tw1m);
                    
                    float ceilW = w - tw1m * wn;
                    
                    for (int i = 0; i < hn; i++) {
                        
                        float y2 = y1 + th1m * i;

                        for (int j = 0; j < wn; j++) {
                            drawRectWH(x1 + j * tw1m, y2, tw1m, th1m, 
                                       tx1, ty1, tw1, th1);    
                        }
                        
                        drawRectWH(x1 + wn * tw1m, y2, ceilW, th1m, 
                                   tx1, ty1, ceilW, th1);
                    }
                    
                    float ceilH = h - th1m * hn;
                    float y2 = y1 + th1m * hn; 
                    
                    for (int j = 0; j < wn; j++) {
                        drawRectWH(x1 + j * tw1m, y2, w, ceilH, 
                                   tx1, ty1, tw1, ceilH);
                    }
                    
                    drawRectWH(x1 + wn * tw1m, y2, ceilW, ceilH, 
                               tx1, ty1, ceilW, ceilH);
                    
                    break;

                default:
                    break;
                }
                
                break;

            default:
                break;
            }
            
            
        }
    }
    
    static void finish() {
        mPolyBuffer.flushRender(mGL);
        mGL = null;
    }
    
    static public boolean loadScheme(GL10 gl, AssetManager assets, 
            String schemeFileName) {
        
        mScheme = new NpSkinScheme(gl, assets, schemeFileName);
        
        mTextureCache.setFonts(gl, mScheme.getFonts());
        
        return true;
    }
    
    static void prepare(GL10 gl) {
        mGL = gl;
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
