package org.chemodansama.engine.hardware;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerManager {

    private int mInterval = 1000;

    private final List<AccelerometerListener> mListeners;

    private final Sensor mSensor;

    /**
     * The listener that listen to events from the accelerometer listener
     */
    private SensorEventListener mSensorListener = new SensorEventListener() {
        
        private float force = 0;
        private long lastShake = 0;
        private long lastUpdate = 0;
        private float lastX = 0;

        private float lastY = 0;
        private float lastZ = 0;
        private long now = 0;
        private long timeDiff = 0;
        private float x = 0;
        private float y = 0;
        private float z = 0;

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            // use the event timestamp as reference
            // so the manager precision won't depends
            // on the AccelerometerListener implementation
            // processing time
            now = event.timestamp;

            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            // trigger change event
            for (AccelerometerListener listener : mListeners) {
                listener.onAccelerationChanged(x, y, z);
            }
            
            // if not interesting in shake events
            // just remove the whole if then else bloc
            if (lastUpdate == 0) {
                lastUpdate = now;
                lastShake = now;
                lastX = x;
                lastY = y;
                lastZ = z;
            } 

            timeDiff = now - lastUpdate;

            if (timeDiff <= 0) {
                return;
            }
            
            force = Math.abs(x + y + z - lastX - lastY - lastZ) / timeDiff;
            if (force > mThreshold) {
                if (now - lastShake >= mInterval) {
                    // trigger shake event
                    for (AccelerometerListener listener : mListeners) {
                        listener.onShake(force);
                    }
                }
                lastShake = now;
            }
            lastX = x;
            lastY = y;
            lastZ = z;
            lastUpdate = now;
        }
    };

    private final SensorManager mSensorManager;

    /** Accuracy configuration */
    private float mThreshold = 0.2f;

    public AccelerometerManager(Context ctx) {
        
        mListeners = new ArrayList<AccelerometerListener>();
        
        if (ctx == null) {
            mSensorManager = null;
            mSensor = null;
            return;
        }
        
        mSensorManager = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
        
        if (mSensorManager == null) {
            mSensor = null;
            return;
        }
        
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        
        mSensor = (sensors.size() > 0) ? sensors.get(0) : null;
    }

    /**
     * Configure the listener for shaking
     * 
     * @param threshold
     *            minimum acceleration variation for considering shaking
     * @param interval
     *            minimum interval between to shake events
     */
    public void configure(int threshold, int interval) {
        mInterval = interval;
        mThreshold = threshold;
    }
    
    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public boolean isSupported() {
        return mSensor != null;
    }

    /**
     * Registers a listener and start listening
     * 
     * @param listener callback for accelerometer events
     */
    public void startListening(AccelerometerListener listener) {

        if (!isSupported()) {
            return;
        }
        
        if (mListeners.contains(listener)) {
            return;
        }
        
        if (mSensorManager.registerListener(mSensorListener,
                                            mSensor,
                                            SensorManager.SENSOR_DELAY_GAME)) {
            mListeners.add(listener);
        }
    }

    /**
     * Configures threshold and interval And registers a listener and start
     * listening
     * 
     * @param listener
     *            callback for accelerometer events
     * @param threshold
     *            minimum acceleration variation for considering shaking
     * @param interval
     *            minimum interval between to shake events
     */
    public void startListening(AccelerometerListener listener, 
            int threshold, int interval) {
        configure(threshold, interval);
        startListening(listener);
    }

    /**
     * Unregisters listeners
     */
    public void stopListening(AccelerometerListener listener) {
        try {
            if (isSupported()) {
                mListeners.remove(listener);

                if (mListeners.size() == 0) {
                    mSensorManager.unregisterListener(mSensorListener);
                }
            }
        } catch (Exception e) {
        }
    }

    public void stopAll() {
        mListeners.clear();
        mSensorManager.unregisterListener(mSensorListener);
    }
}