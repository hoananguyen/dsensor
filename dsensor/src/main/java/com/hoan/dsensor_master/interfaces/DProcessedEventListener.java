package com.hoan.dsensor_master.interfaces;

import com.hoan.dsensor_master.DSensorEvent;

/**
 * Call back for SensorManager.startDProcessedSensor
 * Created by Hoan on 2/28/2016.
 */
public interface DProcessedEventListener {
    void onProcessedValueChanged(DSensorEvent dSensorEvent);
}
