package org.chemodansama.engine;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

class NpSoundStruct {
    final int soundId;
    int status = 1;
    
    public NpSoundStruct(int soundId) {
        this.soundId = soundId;
    }
    
    boolean isLoaded() {
        return status == 0;
    }
}

class SoundPoolMap {
    private final HashMap<Integer, NpSoundStruct> mMapByResourceId = 
            new HashMap<Integer, NpSoundStruct>();
    private final HashMap<Integer, NpSoundStruct> mMapBySoundId = 
            new HashMap<Integer, NpSoundStruct>();
    
    public NpSoundStruct getByResourceId(int resourceId) {
        return mMapByResourceId.get(resourceId);
    }
    
    public NpSoundStruct getBySoundId(int soundId) {
        return mMapBySoundId.get(soundId);
    }
    
    public void add(int resourceId, int soundId) {
        NpSoundStruct ss = new NpSoundStruct(soundId);
        mMapByResourceId.put(resourceId, ss);
        mMapBySoundId.put(soundId, ss);
    }
}

public class NpSoundMan implements OnLoadCompleteListener {
    
    private SoundPool mSoundPool;
    private final SoundPoolMap mSoundPoolMap = new SoundPoolMap();
    private AudioManager mAudioManager;
    private Activity mActivity;
    
    private boolean mSoundEnabled;
    public static final String SOUND_SETTINGS = "sound.dat"; 
    
    private static NpSoundMan mInstance = null;
    
    synchronized public static NpSoundMan getInstance(Activity activity) {
        if (mInstance == null) {
            mInstance = new NpSoundMan(activity);
        }
        
        return mInstance;
    }
    
    private NpSoundMan(Activity activity) {
        mActivity = activity;
        mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mSoundPool.setOnLoadCompleteListener(this);
        
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        mSoundEnabled = loadSetting(activity, SOUND_SETTINGS);
    }
    
    private static boolean loadSetting(Context context, String fileName) {
        try {
            FileInputStream in = context.openFileInput(fileName);

            InputStreamReader sr = new InputStreamReader(in);
            
            int enabled = sr.read();
            
            sr.close();
            
            return (enabled == 1) ? true : false;
            
        } catch (Exception e) {
            return true;
        }
    }
    
    private void writeSettings(Context context, String fileName) {
        try {
            FileOutputStream os = context.openFileOutput(fileName, 
                                                         Context.MODE_PRIVATE);
            
            OutputStreamWriter sw = new OutputStreamWriter(os);
            
            sw.write(mSoundEnabled ? 1 : 0);
            sw.flush();
            sw.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveSettings() {
        writeSettings(mActivity, SOUND_SETTINGS);
    }
    
    public boolean getSoundEnabled() {
        return mSoundEnabled;
    }
    
    public void revertSound() {
        mSoundEnabled ^= true;
    }
    
    public void addSound(int resourceId) {
        
        NpSoundStruct ss = mSoundPoolMap.getByResourceId(resourceId);
        if (ss != null) {
            return;
        }
        
        try {
            int soundId = mSoundPool.load(mActivity, resourceId, 1);
            mSoundPoolMap.add(resourceId, soundId);
        } catch (Exception e) {
            Log.e(LogTag.TAG, e.getMessage());
        }
    }
    
    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
        NpSoundStruct ss = mSoundPoolMap.getBySoundId(sampleId);
        if (ss == null) {
            return;
        }
        
        ss.status = status;
        if (!ss.isLoaded()) {
            LogHelper.e("Sample " + sampleId + " failed to load with status = " 
                        + status);
        }
    }
    
    private void playSound(int resourceId, final int looped, 
            final double volume) {
        
        if (!mSoundEnabled) {
            return;
        }
        
        NpSoundStruct ss = mSoundPoolMap.getByResourceId(resourceId);
        if (ss == null){
            return;
        }
        
        if (!ss.isLoaded()) {
            LogHelper.i("sample is not loaded");
            return;
        }
        
        final int soundId = ss.soundId;
        
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AudioManager am = mAudioManager;
                
                float streamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                streamVolume /= am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                
                streamVolume *= volume;
                
                mSoundPool.play(soundId, streamVolume, streamVolume, 1, looped, 
                                1f);        
            }
        });
    }
    
    public void playSound(int index) {
        playSound(index, 0, 1);
    }
    
    public void playSound(int index, double volume) {
        playSound(index, 0, volume);
    }
    
    public void playLoopedSound(int index) {
        playSound(index, 1, 1);
    }
    
    public void playLoopedSound(int index, double volume) {
        playSound(index, 1, volume);
    }
}
