package com.hoan.dsensor_master.interfaces;

import com.hoan.dsensor_master.DProcessedSensorEvent;

/**
 * Call back for SensorManager.startDSensor
 * Created by Hoan on 2/28/2016.
 */
public interface DSensorEventListener {
    void onDSensorChanged(int changedDSensorTypes, DProcessedSensorEvent processedSensorEvent);
}
