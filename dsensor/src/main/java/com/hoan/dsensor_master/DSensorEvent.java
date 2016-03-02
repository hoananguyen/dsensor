package com.hoan.dsensor_master;


/**
 *
 * Created by Hoan on 2/8/2016.
 */
public class DSensorEvent {
    public final int sensorType;
    public int accuracy;
    public long timestamp;
    public final float[] values;

    public DSensorEvent(int sensorType, int valuesLength) {
        this.sensorType = sensorType;
        values = new float[valuesLength];
    }

    public DSensorEvent(int sensorType, int accuracy, long timestamp, float value) {
        this.sensorType = sensorType;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
        this.values = new float[]{value};
    }

    public DSensorEvent(int sensorType, int accuracy, long timestamp, float[] values) {
        this.sensorType = sensorType;
        this.accuracy = accuracy;
        this.timestamp = timestamp;
        this.values = new float[values.length];
        System.arraycopy(values, 0, this.values, 0, values.length);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append("Sensor type = ");
        sb.append(sensorType);
        sb.append("\naccuracy = ");
        sb.append(this.accuracy);
        sb.append("\ntimestamp = ");
        sb.append(this.timestamp);
        sb.append("\nvalues = [");
        sb.append(values[0]);
        for (int i = 1; i < values.length; i++) {
            sb.append(", ");
            sb.append(values[i]);
        }
        sb.append("]\n");
        return sb.toString();
    }
}
