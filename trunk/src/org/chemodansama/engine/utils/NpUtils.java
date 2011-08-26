package org.chemodansama.engine.utils;

import java.io.IOException;
import java.io.InputStream;

import org.chemodansama.engine.LogTag;

import android.content.res.AssetManager;
import android.util.Log;

public class NpUtils {
    /** openStreamNoCatch - simply wraps opening stream, 
     *                      so no catch clause needed
     * @param fileName
     * @return returns InputStream object if no exception occurred,
     *                 null otherwise.
     */
    public static InputStream openStreamNoCatch(AssetManager assets, 
            String fileName) {
        
        InputStream in = null;
        try {
            in = assets.open(fileName);
        } catch (IOException e) {
            Log.e(LogTag.TAG, "IOException while open: " + fileName, e);
            return null;
        }
        return in;
    }
    
    public static int byteToUnsignedInt(byte b) {
        return b & 0xFF;
    }
}
