package org.chemodansama.engine.render.imgui;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogTag;
import org.chemodansama.engine.math.NpVec2i;
import org.chemodansama.engine.render.NpTexture;
import org.chemodansama.engine.render.NpTextureHeader;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.res.AssetManager;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

public final class NpFont {
    
    static public final class NpFontCharStruct {
        
        private final char mCode;
        private final NpVec2i mOffset;
        private final NpImmutableRect mRect;
        private final int mAdvance;
        
        private final NpImmutableRect mRenderRect;
        private final NpImmutableRect mTextureRect;
        
        public NpFontCharStruct(Attributes attributes, int textureHeightInPels, 
                int textureWidthInPels) {
            String code = attributes.getValue("id");
            
            if (code.length() == 1) {
                mCode = code.charAt(0);
            } else if (code.length() > 1) {
                if (code.equalsIgnoreCase("&quot;")) {
                    mCode = '"';
                } else if (code.equalsIgnoreCase("&amp;")) {
                    mCode = '&';
                } else if (code.equalsIgnoreCase("&lt;")) {
                    mCode = '<';
                } else if (code.equalsIgnoreCase("&gt;")) {
                    mCode = '>';
                } else {
                    Log.w(LogTag.TAG, "Unknown char code: " + code);
                    mCode = ' ';
                }
            } else {
                Log.w(LogTag.TAG, "Unknown char code: " + code);
                mCode = ' ';
            }
            
            String offsets = attributes.getValue("offset").trim();
            String[] s = offsets.split(" ");
            
            mOffset = new NpVec2i(Integer.parseInt(s[0]), 
                                  -Integer.parseInt(s[1]));
            
            String rects = attributes.getValue("rect").trim();
            s = rects.split(" ");
            
            int x = Integer.parseInt(s[0]); 
            int y = Integer.parseInt(s[1]); 
            int w = Integer.parseInt(s[2]); 
            int h = Integer.parseInt(s[3]); 
            
            mRect = new NpImmutableRect(x, textureHeightInPels - y, w, h);
            
            mAdvance = Integer.parseInt(attributes.getValue("advance"));
            
            
            mRenderRect = new NpImmutableRect(mOffset.getX(), mOffset.getY(), 
                                              mRect.getW(), mRect.getH());
            
            float InvTexWidth = 1.0f / textureWidthInPels;
            float InvTexHeight = 1.0f / textureHeightInPels;
            
            mTextureRect = new NpImmutableRect(mRect.getX() * InvTexWidth, 
                                               mRect.getY() * InvTexHeight, 
                                               mRect.getW() * InvTexWidth, 
                                               -mRect.getH() * InvTexHeight);
        }
        
        public int getAdvance() {
            return mAdvance;
        }
        
        public char getCode() {
            return mCode;
        }
        
        /**
         * @return character rectangle, assuming cursor is placed 
         *         on the base line and X coordinate is zero. 
         */
        protected NpImmutableRect getRenderRect() {
            return mRenderRect;
        }

        protected NpImmutableRect getTextureRect() {
            return mTextureRect;
        }
    }
    
    private final class NpGhlFontReader extends DefaultHandler {
        
        private final GL10 mGL;

        public NpGhlFontReader(GL10 gl) {
            mGL = gl;
        }
        
        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            
            
            if (localName.equalsIgnoreCase("description")) {
                mSize = Integer.parseInt(attributes.getValue("size"));
                mFamily = attributes.getValue("family");
            } else if (localName.equalsIgnoreCase("metrics")) {
                mAscender = Integer.parseInt(attributes.getValue("ascender"));
                mHeight = Integer.parseInt(attributes.getValue("height"));
                mXHeight = Integer.parseInt(attributes.getValue("xheight"));
                mDescender = Integer.parseInt(attributes.getValue("descender"));
            } else if (localName.equalsIgnoreCase("texture")) {
                String texFileName = attributes.getValue("file");
                
                try {
                    InputStream is = mAssets.open(texFileName);
                    
                    if (is != null) {
                        mTex = new NpTexture(mGL, is, true);
                    }
                } catch (Exception e) {
                    Log.e(LogTag.TAG, 
                          "Exc while reading font texture: " + texFileName);
                }
            } else if (localName.equalsIgnoreCase("char")) {
                
                NpTextureHeader h = mTex.getHeader();
                
                NpFontCharStruct s = new NpFontCharStruct(attributes, 
                                                          h.getHeight(),
                                                          h.getWidth());
                mChars.put(s.getCode(), s); 
            }
        }
    }
    
    final private String mName;
    
    private String mFamily = "";
    private int mSize = 0;
    private String mStyle = "";
    
    private int mAscender = 0;
    private int mHeight = 0;
    private int mDescender = 0;
    private int mXHeight = 0;

    private NpTexture mTex = null;
    private TreeMap<Character, NpFontCharStruct> mChars = 
        new TreeMap<Character, NpFontCharStruct>();
    
    private final AssetManager mAssets;
    
    public NpFont(GL10 gl, String name, AssetManager assets, String fileName) {

        mName = name;
        
        mAssets = assets;
        
        try {
            Xml.parse(assets.open(fileName), Encoding.US_ASCII, 
                      new NpGhlFontReader(gl));
        } catch (IOException e) {
            return;
        } catch (SAXException e) {
            return;
        }
    }
    
    public float computeTextHeight(float height, String s) {
        float r = 0;
        
        float ky = height / mSize;
        
        for (int i = 0; i < s.length(); i++) {
            
            NpFontCharStruct c = getChar(s.charAt(i));
            
            if (c == null) {
                continue;
            }
            
            r = Math.max(r, ky * c.getRenderRect().getH());
        }
        
        return r;
    }
    
    public NpRecti computeTextRect(float height, String s) {
        NpRecti r = new NpRecti(0, 0, 0, 0);
        
        float w = 0;
        float h = 0;
        float x = 0;
        float y = 0;
        
        float ky = height / mSize;

        if ((s == null) || (s.length() == 0)) {
            return r;
        }
        
        if (s.length() == 1) {
            NpFontCharStruct c = getChar(s.charAt(0));
            
            if (c != null) {
                r.set(ky * c.getRenderRect().getX(), 
                      ky * c.getRenderRect().getY(), 
                      ky * c.getRenderRect().getW(), 
                      ky * c.getRenderRect().getH());
            }
            
            return r;
        } else if (s.length() > 1) {
            NpFontCharStruct c = getChar(s.charAt(0));
            
            if (c != null) {
                x = ky * c.getRenderRect().getX();
                y = ky * c.getRenderRect().getY();
                w = ky * (c.getAdvance() - c.getRenderRect().getX());
                h = ky * c.getRenderRect().getH();
            }
        }
        
        for (int i = 1; i < s.length() - 1; i++) {
            
            NpFontCharStruct c = getChar(s.charAt(i));
            
            if (c == null) {
                continue;
            }
            
            w += ky * c.getAdvance();
            h = Math.max(h, ky * c.getRenderRect().getH());
            y = Math.min(y, ky * c.getRenderRect().getY());
        }
        
        NpFontCharStruct c = getChar(s.charAt(s.length() - 1));
        
        if (c != null) {
            w += ky * (c.getRenderRect().getX() + c.getRenderRect().getW());
            h = Math.max(h, ky * c.getRenderRect().getH());
            y = Math.min(y, ky * c.getRenderRect().getY());
        }
 
        r.set(x, y, w, h);
        
        return r;
    }
    
    public int getAscender() {
        return mAscender;
    }
    
    public NpFontCharStruct getChar(char c) {
        return mChars.get(c);
    }
    
    public int getDescender() {
        return mDescender;
    }
    
    public String getFamily() {
        return mFamily;
    }
    
    public int getHeight() {
        return mHeight;
    }

    public int getSize() {
        return mSize;
    }

    public String getStyle() {
        return mStyle;
    }

    public NpTexture getTexture() {
        return mTex;
    }

    public float getXHeight(float height) {
        return mXHeight;
    }

    public boolean hasName(String name) {
        return ((mName != null) && (mName.equals(name))) ? true : false;
    }
}
