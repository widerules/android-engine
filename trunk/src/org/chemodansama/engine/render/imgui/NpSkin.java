package org.chemodansama.engine.render.imgui;

import static org.chemodansama.engine.render.imgui.NpGuiReturnConsts.clicked;

import java.util.Collection;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.NpHolder;
import org.chemodansama.engine.math.NpMath;
import org.chemodansama.engine.math.NpRect;
import org.chemodansama.engine.math.NpVec4;
import org.chemodansama.engine.render.NpPolyBuffer;
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
public final class NpSkin implements NpAlignConsts {

    private final static NpVec4 WHITE_COLOR = new NpVec4(1, 1, 1, 1);
    
    private static NpPolyBuffer mPolyBuffer;
    private static NpSkinScheme mScheme = null;
    private static NpTextureCache mTextureCache = null;
    private static GL10 mGL = null;
    
    private static boolean mScaling = false;
    private static float mHScale = 1;
    private static float mVScale = 1;
    
    static {
        mPolyBuffer = new NpPolyBuffer(64);
        mTextureCache = new NpTextureCache(mPolyBuffer);
    }
    
    public static float computeFontHeight(NpFontParams font) {
        NpFont f = getFont(font.name);
        return (f != null) ? f.computeTextHeight(font.height, "A") : 0;
    }
    
    public static NpRect computeTextRect(String fontName, float height, 
            String text) {
        NpFont f = getFont(fontName);
        return (f != null) ? f.computeTextRect(height, text) : new NpRect(); 
    }
    
    public static void disableScaling() {
        mScaling = false;
        mHScale = 1;
        mVScale = 1;
    }
    
    static public int doButton(boolean condition, String widgetLookName, 
            String caption, NpFontParams font, NpRect rect) {
        return (condition) ? doButton(NpWidgetIdGen.nextId(), widgetLookName, 
                                      caption, font, rect) 
                           : doDummy();
    }
    
    static public int doButton(int id, String widgetLookName, 
            String caption, NpFontParams font, NpRect rect) {
        return doButton(id, widgetLookName, caption, font, rect, WHITE_COLOR);
    }
    
    static public int doButton(String widgetLookName, 
            String caption, NpFontParams font, NpRect rect) {
        return doButton(NpWidgetIdGen.nextId(), widgetLookName, caption, font, 
                        rect);
    }

    static public int doButton(String widgetLookName, 
            String caption, NpFontParams font, NpRect rect, NpVec4 color) {
        return doButton(NpWidgetIdGen.nextId(), widgetLookName, caption, font, 
                        rect, color);
    }
    
    static public int doButton(boolean condition, String widgetLookName, 
            String caption, NpFontParams font, NpRect rect, NpVec4 color) {
        return (condition) ? doButton(NpWidgetIdGen.nextId(), widgetLookName,
                                      caption, font, rect, color) 
                           : doDummy();
    }
    
    static public int doButton(int id, String widgetLookName, 
            String caption, NpFontParams font, NpRect rect, NpVec4 color) {
        NpWidgetStatelook sl = drawWidget(getWidgetState(id), widgetLookName, 
                                          rect, color, false, false);
        
        NpRect clientRect;
        if (sl == null) {
            clientRect = rect;
        } else {
            clientRect = sl.computeClientRect(mScheme, rect, false, false);
        }
        
        drawButtonText(id, caption, font, clientRect);
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doButtonEx(boolean condition, String widgetLookName, 
            String caption, NpFontParams font, NpRect rect, 
            boolean invertX, boolean invertY) {
        return (condition) ? doButtonEx(widgetLookName, caption, font, 
                                        rect, invertX, invertY) 
                           : doDummy();
    }
    
    static public int doButtonEx(int id, String widgetLookName, 
            String caption, NpFontParams font, NpRect rect, 
            boolean invertX, boolean invertY) {
        NpWidgetStatelook sl = drawWidget(getWidgetState(id), widgetLookName, 
                                          rect, NpVec4.ONE, invertX, invertY);
        
        NpRect clientRect;
        if (sl == null) {
            clientRect = rect;
        } else {
            clientRect = sl.computeClientRect(mScheme, rect, invertX, invertY);
        }
        
        drawButtonText(id, caption, font, clientRect);
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doButtonEx(String widgetLookName, 
            String caption, NpFontParams font, NpRect rect, 
            boolean invertX, boolean invertY) {
        return doButtonEx(NpWidgetIdGen.nextId(), widgetLookName, caption, font, 
                          rect, invertX, invertY);
    }
    
    static public int doDummy() {
        NpWidgetIdGen.nextId();
        return NpGuiReturnConsts.GUI_RETURN_FLAG_NORMAL;
    }
    
    static public int doLabel(boolean condition, int x, int y, 
            String caption, NpFontParams font) {
        return (condition) ? doLabel(NpWidgetIdGen.nextId(), x, y, caption, 
                                     font, ALIGN_LEFT) 
                           : doDummy();
    }

    static public int doLabel(boolean condition, int x, int y, 
            String caption, NpFontParams font, byte align) {
        return (condition) ? doLabel(x, y, caption, font, align) 
                           : doDummy();
    }

    static public int doLabel(int id, int x, int y, 
            String caption, NpFontParams font) {
        return doLabel(id, x, y, caption, font, ALIGN_LEFT);
    }
    
    static public int doLabel(int id, int x, int y, 
            String caption, NpFontParams font, byte align) {
        
        GL10 gl = mGL;
        
        if (!mTextureCache.activateFont(gl, font.name)) {
            return 0;
        }
        
        NpRect r = computeTextRect(font.name, font.height, caption);
        
        if (align == ALIGN_CENTER) {
            x -= r.w / 2;
        } else if (align == ALIGN_RIGHT) {
            x -= r.w;
        };
        x -= r.x;
        
        drawString(caption, x, y, font.height, font.color);
        
        return getRectWidgetRetCode(id, new NpRect(x, y, r.x, r.y));
    }
    
    static public int doLabel(int x, int y, 
            String caption, NpFontParams font) {
        return doLabel(NpWidgetIdGen.nextId(), x, y, caption, font, ALIGN_LEFT);
    }
    
    static public int doLabel(int x, int y, 
            String caption, NpFontParams font, byte align) {
        return doLabel(NpWidgetIdGen.nextId(), x, y, caption, font, align);
    }

    static public void doRect(NpRect r, NpVec4 color) {
        if (mGL == null) {
            LogHelper.e("mGL == null");
            return;
        }
        
        mTextureCache.activateTextureNoRet(mGL, null);
        
        if (color == null) {
            color = WHITE_COLOR;
        }
        
        mGL.glColor4f(color.coords[0], color.coords[1], color.coords[2], 
                      color.coords[3]);
        drawRectWH(r.x, r.y, r.w, r.h, 0, 0, 0, 0);
    }
    
    static public int doRectWidget(boolean condition, NpWidgetState state, 
            String widgetLookName, NpRect rect) {
        return (condition) ? doRectWidget(state, widgetLookName, rect) 
                           : doDummy();
    }
    
    static public int doRectWidget(int id, NpWidgetState state, 
            String widgetLookName, NpRect rect) {
        drawWidget(state, widgetLookName, rect);
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doRectWidget(NpWidgetState state, 
            String widgetLookName, NpRect rect) {
        drawWidget(state, widgetLookName, rect);
        return getRectWidgetRetCode(NpWidgetIdGen.nextId(), rect);
    }

    static public int doRectWidget(int id, String widgetLookName, NpRect rect) {
        drawWidget(getWidgetState(id), widgetLookName, rect);
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doRectWidget(String widgetLookName, NpRect rect) {
        int id = NpWidgetIdGen.nextId();
        drawWidget(getWidgetState(id), widgetLookName, rect);
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doRectWidgetEx(int id, NpWidgetState state, 
            String widgetLookName, NpRect rect, 
            boolean invertX, boolean invertY, NpVec4 color) {
        drawWidget(state, widgetLookName, rect, color, invertX, invertY);
        return getRectWidgetRetCode(id, rect);
    }

    static public int doRectWidgetEx(NpWidgetState state, 
            String widgetLookName, NpRect rect, 
            boolean invertX, boolean invertY, NpVec4 color) {
        int id = NpWidgetIdGen.nextId();
        drawWidget(state, widgetLookName, rect, color, invertX, invertY);
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doRectWidgetEx(int id, String widgetLookName, NpRect rect,
            boolean invertX, boolean invertY, NpVec4 color) {
        drawWidget(getWidgetState(id), widgetLookName, rect, color, 
                   invertX, invertY);
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doRectWidgetEx(String widgetLookName, NpRect rect,
            boolean invertX, boolean invertY, NpVec4 color) {
        int id = NpWidgetIdGen.nextId();
        drawWidget(getWidgetState(id), widgetLookName, rect, color, 
                   invertX, invertY);
        return getRectWidgetRetCode(id, rect);
    }
    
    static public int doVertSlider(int id, String widgetLookName,
            float x, float y, float h, NpHolder<Float> slidePos) {

        int r = NpGuiReturnConsts.GUI_RETURN_FLAG_NORMAL;
        
        String bgName = widgetLookName + "Bg";
        
        NpWidgetState state = getWidgetState(id);

        NpRect rect = new NpRect();
        
        if (!getWidgetRectDefW(state, bgName, x, y, h, rect)) {
            return r;
        }
        
        drawWidget(state, bgName, rect);
        
        if (clicked(getRectWidgetRetCode(id, rect))) {
            slidePos.value = (float)(NpGuiState.getMouseY() - y) / h;
            slidePos.value = NpMath.clampf(slidePos.value, 0, 1);
        }
        
        float slideY = slidePos.value * rect.h;
        
        getWidgetRectDefWH(state, widgetLookName, x, y + slideY, rect);
        
        rect.h = 32;
        
        if (rect.y + rect.h > y + h) {
            rect.y = (int) (y + h - rect.h);
        }
        
        drawWidget(state, widgetLookName, rect);
        
        return r;
    }
    
    static private void drawButtonText(int id,
            String caption, NpFontParams font, NpRect rect) {
        
        if ((caption == null) || (caption.length() == 0)) {
            return;
        }
        
        if (font == null) {
            return;
        }
        
        if (rect == null) {
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
        
        NpRect textRect = f.computeTextRect(font.height, caption);

        int x = rect.x + (rect.w - textRect.w) / 2 - textRect.x;
        int y = rect.y + (int) ((rect.h + f.getXHeight(font.height)) * 0.5);
        
        drawString(caption, x, y, font.height, font.color);
    }
    
    static void drawRect(float x1, float y1, float x2, float y2, 
            float tx1, float ty1, float tx2, float ty2) {
        if (mScaling) {
            mPolyBuffer.pushQuad(mGL, x1 * mHScale, y1 * mVScale, 
                                 x2 * mHScale, y2 * mVScale, 
                                 tx1, ty1, tx2, ty2);
        } else {
            mPolyBuffer.pushQuad(mGL, x1, y1, x2, y2, tx1, ty1, tx2, ty2);
        }
    }
    
    static void drawRectWH(float x, float y, float w, float h, 
            float tx, float ty, float tw, float th) {
        if (mScaling) {
            mPolyBuffer.pushQuad(mGL, x * mHScale, y * mVScale, 
                                 (x + w) * mHScale, (y + h) * mVScale, 
                                 tx, ty, tx + tw, ty + th);
        } else {
            mPolyBuffer.pushQuad(mGL, x, y, x + w, y + h, 
                                 tx, ty, tx + tw, ty + th);
        }
    }
        
    static private void drawString(String s, int x, int y, 
            float fontSize, NpVec4 fontColor) {
        
        GL10 gl = mGL; 
        
        NpFont f = mTextureCache.getActiveFont();
        
        if (f == null) {
            return;
        }

        gl.glPushMatrix();
        
        // flush render on GL states change 
        mPolyBuffer.flushRender(gl);
        
        if (mScaling) {
            x *= mHScale;
            y *= mVScale;
        }
        
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
            
            NpTextureRect verts = cs.getRenderRect();
            NpTextureRect texCoords = cs.getTextureRect();
            
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
    
    
    static private NpWidgetStatelook drawWidget(NpWidgetState state, 
            String widgetName, NpRect rect) {
        return drawWidget(state, widgetName, rect, NpVec4.ONE, false, false);
    }
    
    static private NpWidgetStatelook drawWidget(NpWidgetState state, 
            String widgetName, NpRect rect, NpVec4 color, 
            boolean invertX, boolean invertY) {
        
        GL10 gl = mGL;
        
        if (mScheme == null) {
            return null;
        }
        
        NpWidgetlook widget = mScheme.getWidget(widgetName);
        if (widget == null) {
            return null;
        }
        
        NpWidgetStatelook stateLook = widget.getStateLook(state);
        if (stateLook == null) {
            return null;
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

            float relX = area.getX().getValue(mScheme, stateLook, rect);
            float relY = area.getY().getValue(mScheme, stateLook, rect);
            float w = area.getWidth().getValue(mScheme, stateLook, rect);
            float h = area.getHeight().getValue(mScheme, stateLook, rect);
            
            int tw = texture.getWidth();
            int th = texture.getHeight();
            
            if (!mTextureCache.activateTexture(gl, texture)) {
                continue;
            }
            
            float absX = rect.x + relX;
            float absY = rect.y + relY;
            
            float imageX = (float) im.getXPos() / tw;
            float imageY = 1.0f - (float) im.getYPos() / th;
            
            float imageW = (float) im.getWidth() / tw;
            float imageH = -(float) im.getHeight() / th;
            
            if (invertX) {
                imageX = imageX + imageW;
                imageW = -imageW;
            }
            
            if (invertY) {
                imageY = imageY + imageH;
                imageH = -imageH;
            }
            
            float imageWidthInPels = im.getWidth();
            float imageHeightInPels = im.getHeight();
            
            switch (area.getWidthScale()) {
            case STRETCH:
                switch (area.getHeightScale()) {
                case STRETCH:
                    drawRectWH(absX, absY, w, h, 
                               imageX, imageY, imageW, imageH);
                    break;
                    
                case REPEAT:
                    tileImageVertical(absX, absY, w, h, imageHeightInPels, 
                                      imageX, imageY, imageW, imageH);
                    break;
                }
                break;
                
            case REPEAT:
                switch (area.getHeightScale()) {
                case STRETCH:
                    tileImageHorizontal(absX, absY, w, h, imageWidthInPels, 
                                        imageX, imageY, imageW, imageH);
                    break;
                    
                case REPEAT:
                    tileImagePlane(absX, absY, w, h, 
                                   imageWidthInPels, imageHeightInPels, 
                                   imageX, imageY, imageW, imageH);
                    break;
                }
                break;
            }
        }
        
        return stateLook;
    }
    
    public static void enableScaling(float hscale, float vscale) {
        mScaling = true;
        mHScale = hscale;
        mVScale = vscale;
    }
    
    static void finish() {
        mPolyBuffer.flushRender(mGL);
        mGL = null;
    }

    private static NpFont getFont(String name) {
        NpFont f = null;
        
        if ((f = mTextureCache.getActiveFont(name)) == null) {
            f = mScheme.getFonts().get(name);
        }
        
        return f;
    }
    
    public static float getFontAscender(NpFontParams font) {
        NpFont f = getFont(font.name);
        return (f != null) ? f.getAscender() * font.height / f.getSize() : 0;
    }    
    
    public static float getFontXHeight(NpFontParams font) {
        NpFont f = getFont(font.name);
        return (f != null) ? f.getXHeight(font.height) : 0;
    }
    
    static public boolean getImageRect(String widgetName, 
            NpWidgetState state, String area, NpRect out) {
        
        NpWidgetlook widget = mScheme.getWidget(widgetName);
        
        if (widget == null) {
            return false;
        }
        
        NpWidgetStatelook stateLook = widget.getStateLook(state);
        
        if (stateLook == null) {
            return false;
        }
        
        NpWidgetImage image = stateLook.findImageByArea(area);
        
        if (image == null) {
            return false;
        }
        
        NpSkinImageSet imageSet = mScheme.getImageSet(image.getImageset());
        
        if (imageSet == null) {
            return false;
        }
        
        NpSkinImageSet.NpSkinImage im = imageSet.getImage(image.getImage());
        
        if (im == null) {
            return false;
        }
        
        out.set(im.getXPos(), im.getYPos(), im.getWidth(), im.getHeight());

        return true;
    }
    
    static private int getRectWidgetRetCode(int id, NpRect rect) {
        
        boolean over;
        
        if (mScaling) {
            over = rect.overlapsPoint((int) (NpGuiState.getMouseX() / mHScale), 
                                   (int) (NpGuiState.getMouseY() / mVScale));
        } else {
            over = rect.overlapsPoint(NpGuiState.getMouseX(), 
                                   NpGuiState.getMouseY());    
        }
        
        return NpGuiState.doWidgetLogic(id, over);
    }

    static private boolean getWidgetRectDefW(NpWidgetState state, 
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
    
    static private boolean getWidgetRectDefWH(NpWidgetState state, 
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
    
    static public NpWidgetState getWidgetState(int id) {
        if (NpGuiState.isActive(id)) {
            return NpWidgetState.WS_PUSHED; 
        } else if (NpGuiState.isHot(id)) {
            return NpWidgetState.WS_HOVER;
        } else {
            return NpWidgetState.WS_NORMAL;
        }
    }
    
    static public boolean loadScheme(GL10 gl, AssetManager assets, 
            String schemeFileName) {
        
        if (mScheme != null) {
            mScheme.reloadAssets(gl, assets);
        } else { 
            mScheme = new NpSkinScheme(gl, assets, schemeFileName);
        }
        
        mTextureCache.setFonts(gl, mScheme.getFonts());
        
        return true;
    }
    
    static void prepare(GL10 gl) {
        mGL = gl;
        mTextureCache.reset(gl);
    }
    
    static private void tileImageHorizontal(float absX, float absY, 
            float w, float h, float imageWidthInPels, 
            float imageX, float imageY, float imageW, float imageH) {
        int wn = (int) Math.floor(w / imageWidthInPels);
        
        for (int i = 0; i < wn; i++) {
            drawRectWH(absX + imageWidthInPels * i, absY, imageWidthInPels, h, 
                       imageX, imageY, imageW, imageH);
        }
        
        float ceilPart = w - imageWidthInPels * wn;
        
        float texCeil = ceilPart / imageWidthInPels * imageW;
        
        drawRectWH(absX + imageWidthInPels * wn, absY, ceilPart, h, 
                   imageX, imageY, texCeil, imageH);
    }
    
    static private void tileImagePlane(float absX, float absY, 
            float w, float h, float imageWidthInPels, float imageHeightInPels,  
            float imageX, float imageY, float imageW, float imageH) {
        
        int hn = (int) Math.floor(h / imageHeightInPels);
        int wn = (int) Math.floor(w / imageWidthInPels);
        
        float ceilW = w - imageWidthInPels * wn;
        float texCeilW = ceilW / imageWidthInPels * imageW;
        
        for (int i = 0; i < hn; i++) {
            float y = absY + imageHeightInPels * i;

            for (int j = 0; j < wn; j++) {
                drawRectWH(absX + j * imageWidthInPels, y, 
                           imageWidthInPels, imageHeightInPels, 
                           imageX, imageY, imageW, imageH);    
            }
            
            drawRectWH(absX + wn * imageWidthInPels, y, 
                       ceilW, imageHeightInPels, 
                       imageX, imageY, texCeilW, imageH);
        }
        
        float y = absY + imageHeightInPels * hn; 
        
        float ceilH = h - imageHeightInPels * hn;
        float texCeilH = ceilH / imageHeightInPels * imageH;
        
        for (int j = 0; j < wn; j++) {
            drawRectWH(absX + j * imageWidthInPels, y, imageWidthInPels, ceilH, 
                       imageX, imageY, imageW, texCeilH);
        }
        
        drawRectWH(absX + wn * imageWidthInPels, y, ceilW, ceilH, 
                   imageX, imageY, texCeilW, texCeilH);
    }
    
    static private void tileImageVertical(float absX, float absY, 
            float w, float h, float imageHeightInPels, 
            float imageX, float imageY, float imageW, float imageH) {
        
        int hn = (int) Math.floor(h / imageHeightInPels);
        
        for (int i = 0; i < hn; i++) {
            drawRectWH(absX, absY + imageHeightInPels * i, w, imageHeightInPels, 
                       imageX, imageY, imageW, imageH);
        }
        
        float ceilPart = h - imageHeightInPels * hn;
        float texCeil = ceilPart / imageHeightInPels * imageH;
        
        drawRectWH(absX, absY + imageHeightInPels * hn, w, ceilPart, 
                   imageX, imageY, imageW, texCeil);
    }
    
    public static String truncate(NpFontParams font, int maxWidth, 
            String s) {
        if (font == null) {
            throw new IllegalArgumentException("font == null");
        }
        
        NpFont f = getFont(font.name);
        if (f == null) {
            return null;
        }
        
        return new WordWrapper(f, font.height, maxWidth, s).truncate();
    }
    
    public static void wordWrap(NpFontParams font, float maxWidth, 
            String text, Collection<String> dest) {
        
        if (font == null) {
            throw new IllegalArgumentException("font == null");
        }
        
        if (text == null) {
            throw new IllegalArgumentException("text == null");
        }

        NpFont f = getFont(font.name);
        if (f == null) {
            return;
        }
        
        new WordWrapper(f, font.height, maxWidth, text).wrap(' ', dest);
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
        doActivateTexture(gl, texture);
        mActiveFont = null;
        return (mActiveTexture != null) ? 
                mActiveTexture.equalsToTexture(texture) : true;
    }
    
    public void activateTextureNoRet(GL10 gl, NpTexture texture) {
        doActivateTexture(gl, texture);
        mActiveFont = null;
    }
    
    /**
     * doActivateTexture binds texture with gl state
     * @param gl
     * @param texture
     * @return returns true if texture activating has occurred, 
     *         otherwise returns false
     */
    private void doActivateTexture(GL10 gl, NpTexture texture) {
        // flush buffered quads on each texture change
        mPolyBuffer.flushRender(gl);
        if (texture == null) {
            NpTexture.unbind(gl);
        } else if (!texture.equalsToTexture(mActiveTexture)) {
            texture.bindGL10(gl);
        }
        mActiveTexture = texture;
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

/**
 * Utility class for strings truncation purposes: 
 * able to truncate string to a given width, or split string into a collection 
 * of string, each one of which fits given width.
 */
class WordWrapper {
    
    private final String s;
    private final NpFont font;
    private final float height;
    private final float maxWidth;

    /**
     * @param font font to render. 
     * @param height font's height.
     * @param maxWidth maximum allowed width. Must be greater than zero.
     * @param s source string to deal with.
     * @throws IllegalArgumentException if {@code font == null} 
     *                                  or {@code s == null}
     *                                  or {@code maxWidth <= 0}.
     */
    public WordWrapper(NpFont font, float height, float maxWidth, String s) {
        if (s == null) {
            throw new IllegalArgumentException("s == null");
        }
        
        if (font == null) {
            throw new IllegalArgumentException("font == null");
        }
        
        if (maxWidth <= 0) {
            throw new IllegalArgumentException("maxWidth <= 0");
        }
        
        this.font = font;
        this.height = height;
        this.s = s;
        this.maxWidth = maxWidth;
    }

    /**
     * @return String truncated to given width.
     */
    public String truncate() {
        int t = truncate(0, s.length() - 1);
        if (t < 0) {
            return null;
        }
        
        return s.substring(0, t + 1);
    }
    
    /**
     * Computes index within range {@code [from, to]}. 
     * 
     * @param from range left-bound.  
     * @param to range right-bound.
     * @return {@code -1}, if no substring within given range fits given width.
     *         <br>or {@code index} within {@code [from, to]}, 
     *         such that {@code s.substring(from, index) < maxWidth}.  
     */
    private int truncate(int from, int to) {
        
        int len = to - from + 1;
        if (len <= 0) {
            return -1;
        }
        
        float w = font.computeTextWidth(height, s, from, 1);
        if (w > maxWidth) {
            return -1;
        }
        
        w = font.computeTextWidth(height, s, from, len);
        if (w <= maxWidth) {
            return to;
        }
        
        // binary search goes here.
        int left = from;
        while (to - left > 1) {
            int m = (left + to) / 2;
            w = font.computeTextWidth(height, s, from, m - from + 1);
            if (w <= maxWidth) {
                left = m;
            } else {
                to = m;
            }
        }
        
        if (left == to) {
            return left;
        } else {
            w = font.computeTextWidth(height, s, from, to - from + 1);
            return (w <= maxWidth) ? to : left;
        }
    }
    
    /**
     * Splits given string by given maxWidth into a collection of strings.
     *  
     * @param separator preferable character to split on.
     * @param dest destination collection of String's, 
     *             where the result will be stored.
     * @throws IllegalArgumentException if dest is {@code null}.
     */
    public void wrap(char separator, Collection<String> dest) {
        
        if (dest == null) {
            throw new IllegalArgumentException("out == null");
        }
        
        int left = 0;
        int end = s.length() - 1;
        
        while (true) {
            int t = truncate(left, end);
            if (t < left) {
                break;
            }

            int separatorPos = t;

            // look for a first separator from the end.
            if (t != end) {
                for (int i = t; i >= left; i--) {
                    if (s.charAt(i) == separator) {
                        separatorPos = i;
                        break;
                    }
                }
            }
            
            // if not empty string, add it.
            if ((separatorPos > left) || (s.charAt(left) != separator)) {
                dest.add(s.substring(left, separatorPos + 1));
            }
            
            if (separatorPos == end) {
                break;
            }
            
            left = separatorPos + 1;
        }
    }
}
