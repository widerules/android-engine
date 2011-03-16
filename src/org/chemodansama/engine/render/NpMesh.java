package org.chemodansama.engine.render;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.chemodansama.engine.LogTag;
import org.chemodansama.engine.math.NpVec3;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

class NpMeshParser extends DefaultHandler {
    
    private short[] mFaces = null;
    
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
            mFaces = new short[i * 3];
            
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
        return (mFaces != null) ? mFaces.length : 0;
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
    private FloatBuffer mCoordsBuffer = null;
    private FloatBuffer mNormalsBuffer = null;
    private FloatBuffer mTexCoordsBuffer = null;
    private ShortBuffer mFacesBuffer = null;
    private int mFacesCount = 0;
    
    private float[] mCoords = null; 
    private float[] mNormals = null;
    
    private float[] mTangent = null;
    private float[] mBitangent = null;
    
    public NpMesh(InputStream in, boolean needNormalMapping) {
        
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
        
        mFacesCount      = parser.getFacesCount();
        
        mCoordsBuffer    = makeFloatBuffer(parser.getCoords());
        mNormalsBuffer   = makeFloatBuffer(parser.getNormals());
        mTexCoordsBuffer = makeFloatBuffer(parser.getTexCoords());
        mFacesBuffer     = makeShortBuffer(parser.getFaces());
        
        if (needNormalMapping) {
            mCoords  = parser.getCoords();
            mNormals = parser.getNormals();
            computeTangetSpace(parser.getTexCoords(), parser.getFaces());
        }
    }
    
    public int getCoordsLen() {
        return (mCoords != null) ? mCoords.length : 0;
    }
    public boolean computeTangentSpaceLight(float[] objectLightPos, 
            float[] tangentSpaceLight) {
        
        if ((mCoords == null) || (mTangent == null) || (mBitangent == null) 
                || (mNormals == null) || (tangentSpaceLight == null)) {
            Log.e(LogTag.TAG, 
                  "can't computeTangentSpaceLight: some data is null");
            
            return false;
        }
        
        int vertsLen = mCoords.length;
        
        if ((mTangent.length != vertsLen) || (mBitangent.length != vertsLen) 
                || (mNormals.length != vertsLen) 
                || (tangentSpaceLight.length != vertsLen)) {
            Log.e(LogTag.TAG, 
                  "can't computeTangentSpaceLight: arrays length arent equal");
            
            return false;
        }

        float[] lightVector = new float[3];
        
        int i = 0;
        while (i < vertsLen) {
            NpVec3.sub(objectLightPos, 0, mCoords, i, lightVector, 0);
            
            tangentSpaceLight[i]     = NpVec3.dot(mTangent, i, 
                                                  lightVector, 0);
            
            tangentSpaceLight[i + 1] = NpVec3.dot(mBitangent, i, 
                                                  lightVector, 0);
            
            tangentSpaceLight[i + 2] = NpVec3.dot(mNormals, i, 
                                                  lightVector, 0);
            i += 3;
        }
        
        return true;
    }
    
    private void computeTangetSpace(float[] texCoords, short[] faces) {
        
        if ((mCoords == null) || (mCoords.length % 3 != 0)) {
            Log.w(LogTag.TAG, "mCoords invalid");
            return;
        }
        
        if ((mNormals == null) || (mNormals.length != mCoords.length)) {
            Log.w(LogTag.TAG, "mNormals invalid");
            return;
        }
        
        if ((faces == null) || (faces.length % 3 != 0)) {
            Log.w(LogTag.TAG, "faces invalid");
            return;
        }
        
        if ((texCoords == null) || (texCoords.length % 2 != 0)) {
            Log.w(LogTag.TAG, "texCoords invalid");
            return;
        }
        
        int vertsLen = mCoords.length;
        
        mTangent = new float[vertsLen];
        mBitangent = new float[vertsLen];
        
        int facesLen = faces.length;
        
        int i = 0;
        while (i < facesLen) {
            int i1 = faces[i++];
            int i2 = faces[i++];
            int i3 = faces[i++];
            
            float v1x = mCoords[i1 * 3];
            float v1y = mCoords[i1 * 3 + 1];
            float v1z = mCoords[i1 * 3 + 2];
            
            float v2x = mCoords[i2 * 3];
            float v2y = mCoords[i2 * 3 + 1];
            float v2z = mCoords[i2 * 3 + 2];
            
            float v3x = mCoords[i3 * 3];
            float v3y = mCoords[i3 * 3 + 1];
            float v3z = mCoords[i3 * 3 + 2];
            
            float w1x = texCoords[i1 * 2];
            float w1y = texCoords[i1 * 2 + 1];
            
            float w2x = texCoords[i2 * 2];
            float w2y = texCoords[i2 * 2 + 1];
            
            float w3x = texCoords[i3 * 2];
            float w3y = texCoords[i3 * 2 + 1];
            
            float x1 = v2x - v1x;
            float x2 = v3x - v1x;
            
            float y1 = v2y - v1y;
            float y2 = v3y - v1y;
            
            float z1 = v2z - v1z;
            float z2 = v3z - v1z;

            float s1 = w2x - w1x;
            float t1 = w2y - w1y;
            
            float s2 = w3x - w1x;
            float t2 = w3y - w1y;
            
            float r = 1 / (s1 * t2 - s2 * t1);
            
            float sx = (t2 * x1 - t1 * x2) * r;
            float sy = (t2 * y1 - t1 * y2) * r;
            float sz = (t2 * z1 - t1 * z2) * r;
            
            float tx = (s1 * x2 - s2 * x1) * r;
            float ty = (s1 * y2 - s2 * y1) * r;
            float tz = (s1 * z2 - s2 * z1) * r;
            
            int ii = i1 * 3;
            mTangent  [ii]   += sx;
            mBitangent[ii++] += tx;
            
            mTangent  [ii]   += sy;
            mBitangent[ii++] += ty;
            
            mTangent  [ii]   += sz;
            mBitangent[ii]   += tz;
            
            ii = i2 * 3;
            mTangent  [ii]   += sx;
            mBitangent[ii++] += tx;
            
            mTangent  [ii]   += sy;
            mBitangent[ii++] += ty;
            
            mTangent  [ii]   += sz;
            mBitangent[ii]   += tz;
            
            ii = i3 * 3;
            mTangent  [ii]   += sx;
            mBitangent[ii++] += tx;
            
            mTangent  [ii]   += sy;
            mBitangent[ii++] += ty;
            
            mTangent  [ii]   += sz;
            mBitangent[ii]   += tz;            
        }
        
        float[] n  = NpVec3.newInstance();
        float[] t  = NpVec3.newInstance();
        float[] ns = NpVec3.newInstance();
        float[] ts = NpVec3.newInstance();
        
        float[] b  = NpVec3.newInstance();
        float[] bs = NpVec3.newInstance();
        
        float[] cr = NpVec3.newInstance();
        
        i = 0;
        while (i < vertsLen) {
            System.arraycopy(mNormals, i, n, 0, 3);
            System.arraycopy(mTangent, i, t, 0, 3);
            
            NpVec3.mul(n, NpVec3.dot(n, t), ns);
            NpVec3.sub(t, ns, ts);
            NpVec3.normalize(ts);
            
            System.arraycopy(ts, 0, mTangent, i, 3);

            NpVec3.cross(ts, n, b);
            NpVec3.cross(t, n, cr);
            
            System.arraycopy(mBitangent, i, bs, 0, 3);
            
            if (NpVec3.dot(cr, bs) < 0) {
                NpVec3.mul(b, -1, b);
            }

            System.arraycopy(b, 0, mBitangent, i, 3);

            i += 3;
        }
    }
    
    static private FloatBuffer makeFloatBuffer(float[] arr) {  

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
  
    static private ShortBuffer makeShortBuffer(short[] arr) {
        
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
    
    public void setupArraysPointers(GL10 gl, boolean shouldBindTexCoords,
            boolean shouldBindNormals) {
        if ((mTexCoordsBuffer == null) || (mNormalsBuffer == null) 
                || (mCoordsBuffer == null)) {
            Log.w(LogTag.TAG, 
                  "Mesh data incorrect! Can't setupArraysPointers.");
            
            return;
        }
        
        if (shouldBindTexCoords) {
            setupTexCoordPointer(gl);
        }
        
        if (shouldBindNormals) {
            gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormalsBuffer);
        }
        
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mCoordsBuffer);
    }
    
    public void setupTexCoordPointer(GL10 gl) {
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordsBuffer);
    }
    
    public void setupNormalPointer(GL10 gl) {
        gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormalsBuffer);
    }
    
    /** draw - render the mesh (assume all required gl-states 
     *         are enabled externally)
     *         
     * @param gl GL10 instance
     */
    public void draw(GL10 gl, boolean setupPointers) {

        if (setupPointers) {
            setupArraysPointers(gl, true, true);
        }
        
        if (mFacesBuffer == null) {
            Log.w(LogTag.TAG, "Mesh data incorrect! Can't draw.");
            return;
        }

        gl.glDrawElements(GL10.GL_TRIANGLES, mFacesCount, 
                          GL10.GL_UNSIGNED_SHORT, mFacesBuffer);
    }  
}
