package org.chemodansama.engine;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;


public class NpSoundMan {
    private SoundPool mSoundPool;
    private HashMap<Integer, Integer> mSoundPoolMap;
    private AudioManager mAudioManager;
    private Activity mActivity;
    
    private boolean soundEnabled = true;
    
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
        mSoundPoolMap = new HashMap<Integer, Integer>();
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
    }
    
    public boolean getSoundEnabled() {
        return soundEnabled;
    }
    
    public void revertSound() {
        soundEnabled ^= true;
    }
    
    public void addSound(int SoundID) {
        if (!mSoundPoolMap.containsKey(SoundID)) {
            try {
                int r = mSoundPool.load(mActivity, SoundID, 1);
                mSoundPoolMap.put(SoundID, r);
            } catch (Exception e) {
                Log.e(LogTag.TAG, e.getMessage());
            }
        }
    }
    
    private void playSound(int index, final int looped, final double volume) {
        
        if (!soundEnabled) {
            return;
        }
        
        final Integer soundID = mSoundPoolMap.get(index);
        
        if (soundID == null) {
            return;
        }
        
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AudioManager am = mAudioManager;
                
                float streamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                streamVolume /= am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                
                streamVolume *= volume;
                
                mSoundPool.play(soundID, streamVolume, streamVolume, 1, looped, 
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
