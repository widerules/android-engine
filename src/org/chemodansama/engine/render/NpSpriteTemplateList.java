package org.chemodansama.engine.render;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.tmx.TmxTileset;
import org.chemodansama.engine.tmx.TmxTilesetParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.res.AssetManager;
import android.util.Xml;
import android.util.Xml.Encoding;

class NpSpriteTemplateListParser extends TmxTilesetParser {

    private final GL10 mGL;
    private final AssetManager mAssets;
    private final NpTexturePack mTexturePack;
    private final Collection<NpSpriteAnimation> mAnimations;
    private final Collection<NpSpriteTemplate> mSprites; 

    public NpSpriteTemplateListParser(GL10 gl, AssetManager assets, 
            NpTexturePack texturePack, ArrayList<TmxTileset> tilesets, 
            Collection<NpSpriteAnimation> animations,
            Collection<NpSpriteTemplate> sprites) {
        super(tilesets);
        
        if (gl == null) {
            throw new IllegalArgumentException("gl == null");
        }
        
        if (assets == null) {
            throw new IllegalArgumentException("assets == null");
        }
        
        if (texturePack == null) {
            throw new IllegalArgumentException("texturePack == null");
        }
        
        if (animations == null) {
            throw new IllegalArgumentException("animations == null");
        }
        
        if (sprites == null) {
            throw new IllegalArgumentException("sprites == null");
        }
        
        mGL = gl;
        mAssets = assets;
        mTexturePack = texturePack;
        mAnimations = animations;
        mSprites = sprites;
    }

    private boolean loadImage(String uri, String localName, String qName,
            Attributes attributes) {
        if (!localName.equalsIgnoreCase("image")) {
            return false;
        }
        
        String alias = attributes.getValue("alias");
        if ((alias == null) || (alias.equalsIgnoreCase(""))) {
            return true;
        }

        String texture = attributes.getValue("texture");
        if ((texture == null) || (texture.equalsIgnoreCase(""))) {
            return true;
        }

        boolean failed = false;

        try {
            failed = !mTexturePack.put(mGL, alias, texture, mAssets);
        } catch (IllegalArgumentException e) {
            failed = true;
        } catch (IOException e) {
            failed = true;
        }

        if (failed) {
            LogHelper.e("Can't instantiate texture '" + texture + "'");
            return true;
        }

        LogHelper.i("texture '" + texture + "' added as '" 
                    + alias + "'");
        return true;
    }
    
    private boolean loadAnimation(String uri, String localName, String qName,
            Attributes attributes) {
        return NpSpriteAnimationReader.readAnimation(uri, localName, qName, 
                                                     attributes, mAnimations);
    }
    
    private boolean loadSprite(String uri, String localName, String qName,
            Attributes attributes) {

        if (!localName.equalsIgnoreCase("sprite")) {
            return false;
        }
        
        String name = attributes.getValue("name");
        if ((name == null) || (name.equalsIgnoreCase(""))) {
            return false;
        }
        
        String tsName = attributes.getValue("tileset");
        if ((tsName == null) || (tsName.equals(""))) {
            return false;
        }
        TmxTileset tileset = null;
        for (TmxTileset ts : mTilesets) {
            if (tsName.equalsIgnoreCase(ts.name)) {
                tileset = ts;
                break;
            }
        }
        if (tileset == null) {
            return false;
        }
        
        NpTexture texture = mTexturePack.get(tileset.imageName);
        if (texture == null) {
            return false;
        }

        int gid = Integer.parseInt(attributes.getValue("gid"));
        if (!tileset.containsGid(gid)) {
            LogHelper.e("tileset \"" + tileset.name + "\" does not contain " 
                        + "gid = \"" + Integer.toString(gid) + "\"");
            return false;
        }
        
        mSprites.add(new NpSpriteTemplate(name, tileset, texture, gid));
        LogHelper.i("Sprite \"" + name + "\" added.");
        
        return true;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        
        super.startElement(uri, localName, qName, attributes);
        
        if (loadImage(uri, localName, qName, attributes)) {
            return;
        }
        
        if (loadAnimation(uri, localName, qName, attributes)) {
            return;
        }
        
        if (loadSprite(uri, localName, qName, attributes)) {
            return;
        }
    }
}

public class NpSpriteTemplateList {
    private final TreeMap<String, NpSpriteAnimationTemplate> mTemplates = 
            new TreeMap<String, NpSpriteAnimationTemplate>();
    
    private final ArrayList<TmxTileset> mTilesets = new ArrayList<TmxTileset>();
    private final NpTexturePack mTextures = new NpTexturePack();
    private final ArrayList<NpSpriteAnimation> mAnimations = 
            new ArrayList<NpSpriteAnimation>();
    
    private final ArrayList<NpSpriteTemplate> mSpriteTemplates = 
            new ArrayList<NpSpriteTemplate>();

    public NpSpriteTemplateList(GL10 gl, AssetManager assets, 
            String fileName) throws IOException {
        if (gl == null) {
            throw new IllegalArgumentException("gl == null");
        }
        
        if (assets == null) {
            throw new IllegalArgumentException("assets == null");
        }
        
        parseXml(gl, assets, fileName);
        constructTemplate();
    }
    
    private void constructTemplate() {
        mTemplates.clear();
        
        for (NpSpriteAnimation anim : mAnimations) {
            if (anim == null) {
                continue;
            }
            
            TmxTileset tileset = null;
            for (TmxTileset ts : mTilesets) {
                if ((ts == null) || (ts.name == null)) {
                    continue;
                }
                
                if (ts.name.equalsIgnoreCase(anim.tileset)) {
                    tileset = ts;
                    break;
                }
            }
            if (tileset == null) {
                continue;
            }
            
            NpTexture t = mTextures.get(tileset.imageName);
            if (t == null) {
                continue;
            }
            
            mTemplates.put(anim.name, new NpSpriteAnimationTemplate(tileset, t, anim));
        }
    }
    
    private void parseXml(GL10 gl, AssetManager assets, 
            String fileName) throws IOException {
        InputStream in = null;
        try {
            in = assets.open(fileName);
            Xml.parse(in, Encoding.US_ASCII, 
                      new NpSpriteTemplateListParser(gl, assets, mTextures, 
                                                     mTilesets, mAnimations, 
                                                     mSpriteTemplates));
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    public void refreshContextAssets(GL10 gl, AssetManager assets) {
        mTextures.refreshContextAssets(gl, assets);
    }
    
    public void releaseContextAssets(GL10 gl) {
        mTextures.release(gl);
    }
    
    public NpSpriteAnimationTemplate getAnimation(String name) {
        return mTemplates.get(name);
    }
    
    public NpSpriteTemplate getSprite(String name) {
        for (NpSpriteTemplate t : mSpriteTemplates) {
            if (t.name.equals(name)) {
                return t;
            }
        }
        return null;
    }
}