package org.chemodansama.engine.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogTag;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

class NpMeshParser extends DefaultHandler {
    
    private short[] mFaces = null;
    private int mFacesCount = 0;
    
    private float[] mCoords = null; 
    private float[] mNormals = null; 
    private float[] mTexCoords = null;
    
    private int mFaceIndex = 0;
    private int mVertexIndex = 0;
    private int mNormalIndex = 0;
    private int mTexCoordIndex = 0;

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);
    }
    
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        
        mFaces = null;
        mFacesCount = 0;
        
        mCoords = null; 
        mNormals = null; 
        mTexCoords = null;
        
        mFaceIndex = 0;
        mVertexIndex = 0;
        mNormalIndex = 0;
        mTexCoordIndex = 0;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        
        if (localName.equals("faces")) {
            
            int i = Integer.parseInt(attributes.getValue("count"));  
            mFacesCount = i * 3;   
            mFaces = new short[mFacesCount];
            
        } else if (localName.equals("geometry")) {
            
            int i = Integer.parseInt(attributes.getValue("vertexcount"));
            
            mCoords = new float[i * 3];  
            mNormals = new float[i * 3];  
            mTexCoords = new float[i * 2]; 
            
        } else if (localName.equals("face")) {  
            
            short v1 = (short) Short.parseShort(attributes.getValue("v1"));
            short v2 = (short) Short.parseShort(attributes.getValue("v2"));
            short v3 = (short) Short.parseShort(attributes.getValue("v3"));
            
            mFaces[mFaceIndex++] = v1;  
            mFaces[mFaceIndex++] = v2;  
            mFaces[mFaceIndex++] = v3;
            
        } else if (localName.equals("position")) {
            
            float x = Float.parseFloat(attributes.getValue("x"));
            float y = Float.parseFloat(attributes.getValue("y"));
            float z = Float.parseFloat(attributes.getValue("z"));
            
            mCoords[mVertexIndex++] = x;
            mCoords[mVertexIndex++] = y; 
            mCoords[mVertexIndex++] = z; 
            
        } else if (localName.equals("normal")) {

            float x = Float.parseFloat(attributes.getValue("x"));
            float y = Float.parseFloat(attributes.getValue("y"));
            float z = Float.parseFloat(attributes.getValue("z"));
            
            mNormals[mNormalIndex++] = x;
            mNormals[mNormalIndex++] = y; 
            mNormals[mNormalIndex++] = z; 

        } else if (localName.equals("texcoord")) {
            
            float u = Float.parseFloat(attributes.getValue("u"));
            float v = Float.parseFloat(attributes.getValue("v"));
            
            mTexCoords[mTexCoordIndex++] = u;
            mTexCoords[mTexCoordIndex++] = 1.0f - v; 
        }
    }

    public short[] getFaces() {
        return mFaces;
    }

    public int getFacesCount() {
        return mFacesCount;
    }

    public float[] getCoords() {
        return mCoords;
    }

    public float[] getNormals() {
        return mNormals;
    }

    public float[] getTexCoords() {
        return mTexCoords;
    }
}

public class NpMesh {
    
    private FloatBuffer mCoords = null;
    private FloatBuffer mNormals = null;
    private FloatBuffer mTexCoords = null;
    private ShortBuffer mFaces = null;
    private int mFacesCount = 0;

    public NpMesh(InputStream in) {
        
        super();
        
        if (in == null) {
            Log.w(LogTag.TAG, "InputStream is null in NpMesh()");
            return;
        }
        
        NpMeshParser parser = new NpMeshParser(); 

        try {
            Xml.parse(in, Encoding.US_ASCII, parser);
        } catch (IOException e) {
            Log.w(LogTag.TAG, "IOException while Xml.parse()", e);
            return;
        } catch (SAXException e) {
            Log.w(LogTag.TAG, "SAXException while Xml.parse()", e);
            return;
        }
        
        mFacesCount = parser.getFacesCount();
        mCoords = makeFloatBuffer(parser.getCoords());
        mNormals = makeFloatBuffer(parser.getNormals());
        mTexCoords = makeFloatBuffer(parser.getTexCoords());
        mFaces = makeShortBuffer(parser.getFaces());
    }
    
    private FloatBuffer makeFloatBuffer(float[] arr) {  

        if (arr == null) {
            Log.w(LogTag.TAG, "can't make float buffer: arr is null!");
            return null;
        }
        
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);  
        bb.order(ByteOrder.nativeOrder());  
        FloatBuffer fb = bb.asFloatBuffer();  
        fb.put(arr);  
        fb.position(0);  
        return fb;  
    }  
  
    private ShortBuffer makeShortBuffer(short[] arr) {
        
        if (arr == null) {
            Log.w(LogTag.TAG, "can't make short buffer: arr is null!");
            return null;
        }
        
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);  
        bb.order(ByteOrder.nativeOrder());  
        ShortBuffer ib = bb.asShortBuffer();  
        ib.put(arr);  
        ib.position(0);  
        return ib;  
    }
    
    /** draw - render the mesh (assume all required gl-states 
     *         are enabled externally)
     *         
     * @param gl GL10 instance
     */
    public void draw(GL10 gl) {
        
        if ((mTexCoords == null) || (mNormals == null) || (mCoords == null) 
                || (mFaces == null)) {
            Log.w(LogTag.TAG, "Mesh data incorrect! Can't draw.");
            return;
        }
        
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoords);
        gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormals);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mCoords);
        
        gl.glDrawElements(GL10.GL_TRIANGLES, mFacesCount, 
                          GL10.GL_UNSIGNED_SHORT, mFaces);
    }  
}
