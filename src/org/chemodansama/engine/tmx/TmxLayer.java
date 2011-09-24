package org.chemodansama.engine.tmx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.chemodansama.engine.LogHelper;
import org.chemodansama.engine.LogTag;
import org.chemodansama.engine.utils.Base64;

import android.util.Log;

abstract class LayerDataReader {

    protected final byte[] mCompressedData;
    protected final int mLevelWidth;
    protected final int mLevelHeight;
    
    public LayerDataReader(byte[] compressedData, int levelWidth, 
            int levelHeight) {
        mCompressedData = compressedData;
        mLevelWidth = levelWidth;
        mLevelHeight = levelHeight;
    }
    
    public abstract int[] getData();
}

class GZIPDataReader extends LayerDataReader {

    private final static int INT_SIZE_IN_BYTES = Integer.SIZE / 8;
    
    public GZIPDataReader(byte[] compressedData, int levelWidth, 
            int levelHeight) {
        super(compressedData, levelWidth, levelHeight);
    }

    @Override
    public int[] getData() {
        
        GZIPInputStream gis = null;
        int[] out = null;
        
        try {
            ByteArrayInputStream bais = 
                    new ByteArrayInputStream(mCompressedData);
            
            gis = new GZIPInputStream(bais);

            int intSize = mLevelHeight * mLevelWidth;
            int byteSize = intSize * INT_SIZE_IN_BYTES;

            byte[] rawByteData = new byte[byteSize];
            out = new int[intSize];

            int j = 0;

            while (true) {
                int r = gis.read(rawByteData, 0, byteSize);

                if (r == -1) {
                    break;
                }

                // merge bytes to integers
                for (int i = 0; i < r; i += 4) {
                    out[j++] = (rawByteData[i] & 0xFF) 
                            | ((rawByteData[i + 1] & 0xFF) << 8)
                            | ((rawByteData[i + 2] & 0xFF) << 16)
                            | ((rawByteData[i + 3] & 0xFF) << 24);
                }
            }
        } catch (Exception e) {
            out = null;
            LogHelper.e("IOException on GZIP reading.");
        } finally {
            try {
                if (gis != null) {
                    gis.close();
                }
            } catch (IOException e) {
                LogHelper.e("IOException on GZIP stream close().");
            }
        }
        return out;
    }
}

public class TmxLayer extends TmxEntity {
    public final String name;
    public final int width;
    public final int height;
    
    public final boolean isVisible;
    
    private int[] mData;
    
    public TmxLayer(String name, int width, int height, boolean isVisible) {
        super();
        
        this.height = height;
        this.width = width;
        this.name = name;
        
        this.isVisible = isVisible;
    }

    void setCompressedData(String data, TmxDataEncoding encoding, 
            TmxDataCompression compression) {
        
        byte[] decoded;
        
        switch (encoding) {
        case BASE64:
            try {
                decoded = Base64.decode(data, Base64.DONT_GUNZIP);
            } catch (IOException e) {
                LogHelper.e("IOException while base64 decoding.");
                return;
            }
            break;

        default:
            decoded = data.getBytes();
            break;
        }
        
        LayerDataReader dr;
        
        switch (compression) {
        case GZIP:
            dr = new GZIPDataReader(decoded, width, height); 
            break;

        default:
            Log.e(LogTag.TAG, 
                  "Unsupported compression: " + compression.toString());
            
            return;
        }
        
        mData = dr.getData();
    }
    
    public int getTileGid(int x, int y) {
        boolean coordsAreValid = (y >= 0) && (y < height) 
                                    && (x >= 0) && (x < width);
           
        return (coordsAreValid) ? mData[y * width + x] : 0;
    }
}
