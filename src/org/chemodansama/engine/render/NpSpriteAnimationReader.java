package org.chemodansama.engine.render;

import static org.chemodansama.engine.utils.NpUtils.getAttributeAsInt;
import static org.chemodansama.engine.utils.NpUtils.readIntArray;

import java.util.Collection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class NpSpriteAnimationReader extends DefaultHandler {
    
    private Collection<NpSpriteAnimation> mAnimations;
    
    public NpSpriteAnimationReader(Collection<NpSpriteAnimation> animations) {
        mAnimations = animations;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        readAnimation(uri, localName, qName, attributes, mAnimations);
    }
    
    public static boolean readAnimation(String uri, String localName, 
            String qName, Attributes attributes, 
            Collection<NpSpriteAnimation> animations) {
        if ((localName == null) || (localName.equals(""))) {
            return false;
        }
        
        if (!localName.equalsIgnoreCase("animation")) {
            return true;
        }
        
        String name = attributes.getValue("alias");
        int fps = getAttributeAsInt(attributes, "fps");
        int verticalOrigin = getAttributeAsInt(attributes, "verticalOrigin");
        int[] sequence = readIntArray(attributes.getValue("sequence"));

        if ((fps != 0) && (sequence != null)) {
            animations.add(new NpSpriteAnimation(name, fps, verticalOrigin, 
                                                 sequence));
        }
        
        return true;
    }
}