package com.hoan.dsensor_master;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import android.view.WindowManager;

import com.hoan.dsensor_master.interfaces.DProcessedEventListener;
import com.hoan.dsensor_master.interfaces.DSensorEventListener;
import com.hoan.dsensor_master.utils.Logger;

/**
 * DSensorManager let you access direction sensors (accelerometer, magnetic field, gravity, gyroscope etc...)
 * Call startDSensor to receive sensors values. Make sure that you call stopDSensor when sensors are not needed anymore.
 * Created by Hoan on 1/30/2016.
 */
public class DSensorManager {

    public static final int TYPE_GRAVITY_NOT_NEEDED = 0;
    public static final int HAS_TYPE_GRAVITY = 1;

    public static final int TYPE_LINEAR_ACCELERATION_NOT_NEEDED = 0;
    public static final int HAS_TYPE_LINEAR_ACCELERATION = 1;

    public static final int TYPE_ROTATION_VECTOR_NOT_NEEDED = 0;
    public static final int HAS_TYPE_ROTATION_VECTOR = 1;

    public static final int REGISTERED_TYPES_AVAILABLE = 0;
    public static final int TYPE_ACCELEROMETER_NOT_AVAILABLE = 2;
    public static final int TYPE_MAGNETIC_FIELD_NOT_AVAILABLE = 4;
    public static final int TYPE_GRAVITY_NOT_AVAILABLE = 8;
    public static final int TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE = 16;
    public static final int TYPE_GYROSCOPE_NOT_AVAILABLE = 32;
    public static final int TYPE_ROTATION_VECTOR_NOT_AVAILABLE = 64;
    public static final int TYPE_ORIENTATION_NOT_AVAILABLE = 128;

    public static final int ERROR_UNSUPPORTED_TYPE = -1;

    private static SensorManager mSensorManager;
    private static DSensorEventProcessor mDSensorEventProcessor;
    private static HandlerThread mSensorThread;

    private DSensorManager(Context context) {
        Logger.d(DSensorManager.class.getSimpleName(), "constructor");
        mSensorThread = new HandlerThread("sensor_thread");
        mSensorThread.start();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * Call this to receive result for one of the sensor type in DProcessedSensor. The sensor rate
     * is default to SensorManager.SENSOR_DELAY_NORMAL and history size for averaging is
     * DSensorEventProcessor.DEFAULT_HISTORY_SIZE
     * @param context
     * @param dProcessedSensorType One of the DProcessedSensor types. No bitwise OR
     * @param dProcessedEventListener callback to receive result
     * @return REGISTERED_TYPES_AVAILABLE for supported DProcessedSensor type and device has all
     *         sensors needed for processing. Otherwise ERROR_UNSUPPORTED_TYPE for unsupported
     *         DProcessedSensor type or the one of the not available for needed sensor.
     * */
    public static int startDProcessedSensor(Context context, int dProcessedSensorType,
                                            DProcessedEventListener dProcessedEventListener) {
        Logger.d(DSensorManager.class.getSimpleName(), "startDSensor(" + dProcessedSensorType + ")");
        return startDProcessedSensor(context, dProcessedSensorType, SensorManager.SENSOR_DELAY_NORMAL,
                DSensorEventProcessor.DEFAULT_HISTORY_SIZE, dProcessedEventListener);
    }

    /**
     * Call this to receive result for one of the sensor type in DProcessedSensor. The history size
     * for averaging is DSensorEventProcessor.DEFAULT_HISTORY_SIZE
     * @param context
     * @param dProcessedSensorType One of the DProcessedSensor types. No bitwise OR
     * @param dProcessedEventListener callback to receive result
     * @return REGISTERED_TYPES_AVAILABLE for supported DProcessedSensor type and device has all
     *         sensors needed for processing. Otherwise ERROR_UNSUPPORTED_TYPE for unsupported
     *         DProcessedSensor type or the one of the not available for needed sensor.
     * */
    public static int startDProcessedSensor(Context context, int dProcessedSensorType, int sensorRate,
                                            DProcessedEventListener dProcessedEventListener) {
        Logger.d(DSensorManager.class.getSimpleName(), "startDSensor(" + dProcessedSensorType + ")");
        return startDProcessedSensor(context, dProcessedSensorType, sensorRate,
                DSensorEventProcessor.DEFAULT_HISTORY_SIZE, dProcessedEventListener);
    }

    /**
     * Call this to receive result for one of the sensor type in DProcessedSensor.
     * @param context
     * @param dProcessedSensorType One of the DProcessedSensor types. No bitwise OR
     * @param dProcessedEventListener callback to receive result
     * @return REGISTERED_TYPES_AVAILABLE for supported DProcessedSensor type and device has all
     *         sensors needed for processing. Otherwise ERROR_UNSUPPORTED_TYPE for unsupported
     *         DProcessedSensor type or the one of the not available for needed sensor.
     * */
    public static int startDProcessedSensor(Context context, int dProcessedSensorType, int sensorRate,
                                            int historyMaxLength, DProcessedEventListener dProcessedEventListener) {
        Logger.d(DSensorManager.class.getSimpleName(), "startDSensor(" + dProcessedSensorType + ")");
        switch (dProcessedSensorType) {
            case DProcessedSensor.TYPE_3D_COMPASS:
                return onTypeCompassRegistered(context, sensorRate, historyMaxLength, dProcessedEventListener);

            case DProcessedSensor.TYPE_COMPASS_FLAT_ONLY:
                return onTypeCompassFlatOnlyRegistered(context, sensorRate, historyMaxLength, dProcessedEventListener);

            case DProcessedSensor.TYPE_3D_COMPASS_AND_DEPRECIATED_ORIENTATION:
                return onType3DCompassAndOrientationRegister(context, sensorRate, historyMaxLength, dProcessedEventListener);

            case DProcessedSensor.TYPE_COMPASS_FLAT_ONLY_AND_DEPRECIATED_ORIENTATION:
                return onTypeCompassFlatOnlyAndOrientationRegister(context, sensorRate, historyMaxLength, dProcessedEventListener);

            default:
                return ERROR_UNSUPPORTED_TYPE;
        }
    }

    /**
     *  Start DSensor processing, processed results are in the DProcessedSensorEvent parameter of
     *  onDSensorChanged method of the DSensorEventListener callback. History size for average the
     *  directions DSensors and values of sensors in World coordinate system is set to default value
     *  of DSensorEventProcessor.DEFAULT_HISTORY_SIZE. Sensor rate is default to
     *  SensorManager.SENSOR_DELAY_NORMAL
     * @param context
     * @param dSensorTypes Bitwise OR of DSensor types
     * @param dSensorEventListener callback
     * @return REGISTERED_TYPES_AVAILABLE if device has all sensors or can be
     *          calculated from other sensors in sensorTypes. Otherwise bitwise
     *          or of all unavailable sensors.
     *          For example TYPE_MAGNETIC_FIELD_NOT_AVAILABLE | TYPE_GYROSCOPE_NOT_AVAILABLE
     */
    public static int startDSensor(Context context, int dSensorTypes,
                                   DSensorEventListener dSensorEventListener) {
        Logger.d(DSensorManager.class.getSimpleName(), "startDSensor(" + dSensorTypes + ")");
        return startDSensor(context, dSensorTypes, SensorManager.SENSOR_DELAY_NORMAL,
                DSensorEventProcessor.DEFAULT_HISTORY_SIZE, dSensorEventListener);
    }

    /**
     *  Start DSensor processing, processed results are in the DProcessedSensorEvent parameter of
     *  onDSensorChanged method of the DSensorEventListener callback. History size for average the
     *  directions DSensors and values of sensors in World coordinate system is set to default value
     *  of DSensorEventProcessor.DEFAULT_HISTORY_SIZE.
     * @param context
     * @param dSensorTypes Bitwise OR of DSensor types
     * @param sensorRate Sensor rate using android SensorManager rate constants,
     *                  i.e. SensorManager.SENSOR_DELAY_FASTEST.
     * @param dSensorEventListener callback
     * @return REGISTERED_TYPES_AVAILABLE if device has all sensors or can be
     *          calculated from other sensors in sensorTypes. Otherwise bitwise
     *          or of all unavailable sensors.
     *          For example TYPE_MAGNETIC_FIELD_NOT_AVAILABLE | TYPE_GYROSCOPE_NOT_AVAILABLE
     */
    public static int startDSensor(Context context, int dSensorTypes, int sensorRate,
                                   DSensorEventListener dSensorEventListener) {
        Logger.d(DSensorManager.class.getSimpleName(), "startDSensor(" + dSensorTypes + ", " + sensorRate + ")");
        return startDSensor(context, dSensorTypes, sensorRate, DSensorEventProcessor.DEFAULT_HISTORY_SIZE, dSensorEventListener);
    }

    /**
     *  Start DSensor processing, processed results are in the DProcessedSensorEvent parameter of
     *  onDSensorChanged method of the DSensorEventListener callback.
     * @param context
     * @param dSensorTypes Bitwise OR of DSensor types
     * @param sensorRate Sensor rate using android SensorManager rate constants,
     *                  i.e. SensorManager.SENSOR_DELAY_FASTEST.
     * @param historyMaxLength max history size for averaging.
     * @param dSensorEventListener callback
     * @return REGISTERED_TYPES_AVAILABLE if device has all sensors or can be
     *          calculated from other sensors in sensorTypes. Otherwise bitwise
     *          or of all unavailable sensors.
     *          For example TYPE_MAGNETIC_FIELD_NOT_AVAILABLE | TYPE_GYROSCOPE_NOT_AVAILABLE
     */
    public static int startDSensor(Context context, int dSensorTypes, int sensorRate, int historyMaxLength,
                                   DSensorEventListener dSensorEventListener) {
        Logger.d(DSensorManager.class.getSimpleName(), "startDSensor(" + dSensorTypes + ", " + sensorRate + "' " + historyMaxLength + ")");

        if (mSensorManager != null) {
            stopDSensor();
        }
        new DSensorManager(context);
        boolean worldTypesRegistered = worldTypesRegistered(dSensorTypes);
        boolean directionTypesRegistered = directionTypesRegistered(dSensorTypes);
        int hasRotationVector = hasTypeRotationVector(dSensorTypes, worldTypesRegistered, directionTypesRegistered);
        int hasLinearAcceleration = hasTypeLinearAcceleration(dSensorTypes);
        int hasGravity = hasTypeGravity(dSensorTypes, hasLinearAcceleration, hasRotationVector,
                worldTypesRegistered, directionTypesRegistered);
        mDSensorEventProcessor = new DSensorEventProcessor(dSensorTypes, historyMaxLength,
                hasRotationVector, hasGravity, hasLinearAcceleration, dSensorEventListener);
        return registerListener(dSensorTypes, sensorRate, hasLinearAcceleration, hasRotationVector,
                hasGravity, worldTypesRegistered, directionTypesRegistered);
    }

    /**
     * Check if TYPE_WORLD_* is registered
     * @param dSensorTypes Bitwise OR of DSensor types
     * @return true if dSensorTypes is of TYPE_WORLD_*
     */
    private static boolean worldTypesRegistered(int dSensorTypes) {
        return (dSensorTypes & DSensor.TYPE_WORLD_ACCELEROMETER) == 0
                && (dSensorTypes & DSensor.TYPE_WORLD_GRAVITY) == 0
                && (dSensorTypes & DSensor.TYPE_WORLD_MAGNETIC_FIELD) == 0
                && (dSensorTypes & DSensor.TYPE_WORLD_LINEAR_ACCELERATION) == 0;
    }

    /**
     * Check if TYPE_*_DIRECTION is registered
     * @param dSensorTypes Bitwise OR of DSensor types
     * @return true if dSensorTypes is of TYPE_*_DIRECTION
     */
    private static boolean directionTypesRegistered(int dSensorTypes) {
        return (dSensorTypes & DSensor.TYPE_Z_AXIS_DIRECTION) == 0
                && (dSensorTypes & DSensor.TYPE_MINUS_Z_AXIS_DIRECTION) == 0
                && (dSensorTypes & DSensor.TYPE_X_AXIS_DIRECTION) == 0
                && (dSensorTypes & DSensor.TYPE_MINUS_X_AXIS_DIRECTION) == 0
                && (dSensorTypes & DSensor.TYPE_Y_AXIS_DIRECTION) == 0
                && (dSensorTypes & DSensor.TYPE_MINUS_Y_AXIS_DIRECTION) == 0;
    }

    /**
     * Check if device has TYPE_ROTATION_VECTOR if registered of required for calculation.
     * @param sensorTypes Bitwise OR of DSensor types.
     * @param worldTypesRegistered Is TYPE_WORLD_* registered.
     * @param directionTypesRegistered Is TYPE_*_DIRECTION registered.
     * @return TYPE_ROTATION_VECTOR_NOT_NEEDED if TYPE_ROTATION_VECTOR is not registered or not
     *         needed for calculation. Else HAS_TYPE_ROTATION_VECTOR if device has TYPE_ROTATION_VECTOR
     *         or TYPE_ROTATION_VECTOR_NOT_AVAILABLE if device does not have TYPE_ROTATION_VECTOR.
     */
    private static int hasTypeRotationVector(int sensorTypes, boolean worldTypesRegistered,
                                             boolean directionTypesRegistered) {
        Logger.d(DSensorManager.class.getSimpleName(), "hasTypeRotationVector(" + sensorTypes + ")");
        if ((sensorTypes & DSensor.TYPE_ROTATION_VECTOR) == 0
                && !worldTypesRegistered && !directionTypesRegistered) {
            return TYPE_ROTATION_VECTOR_NOT_NEEDED;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO
                && mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            return HAS_TYPE_ROTATION_VECTOR;
        }
        return TYPE_ROTATION_VECTOR_NOT_AVAILABLE;
    }

    /**
     * Check if the device has TYPE_GRAVITY if registered of required for calculation.
     * @param sensorTypes Bitwise OR of DSensor types.
     * @param hasLinearAcceleration one of the flag HAS_TYPE_LINEAR_ACCELERATION,
     *                              TYPE_LINEAR_ACCELERATION_NOT_NEEDED or
     *                              TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE.
     * @param hasRotationVector one of the flag HAS_TYPE_ROTATION_VECTOR,
     *                          TYPE_ROTATION_VECTOR_NOT_NEEDED or
     *                          TYPE_ROTATION_VECTOR_NOT_AVAILABLE.
     * @param worldTypesRegistered Is TYPE_WORLD_* registered.
     * @param directionTypesRegistered Is TYPE_*_DIRECTION registered.
     * @return TYPE_GRAVITY_NOT_NEEDED if TYPE_GRAVITY is not registered or not needed for calculation.
     *         Else HAS_TYPE_GRAVITY or TYPE_GRAVITY_NOT_AVAILABLE.
     */
    private static int hasTypeGravity(int sensorTypes, int hasLinearAcceleration, int hasRotationVector,
                                      boolean worldTypesRegistered, boolean directionTypesRegistered) {
        Logger.d(DSensorManager.class.getSimpleName(), "hasTypeGravity(" + sensorTypes + ")");
        if ((sensorTypes & DSensor.TYPE_DEVICE_GRAVITY) == 0
                && (hasLinearAcceleration == TYPE_LINEAR_ACCELERATION_NOT_NEEDED
                    || hasLinearAcceleration == HAS_TYPE_LINEAR_ACCELERATION)
                && (hasRotationVector == HAS_TYPE_ROTATION_VECTOR
                    || (!worldTypesRegistered && !directionTypesRegistered
                        && (sensorTypes & DSensor.TYPE_INCLINATION) == 0
                        && (sensorTypes & DSensor.TYPE_DEVICE_ROTATION) == 0
                        && (sensorTypes & DSensor.TYPE_PITCH) == 0
                        && (sensorTypes & DSensor.TYPE_ROLL) == 0))) {
                return TYPE_GRAVITY_NOT_NEEDED;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO
                && mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            return HAS_TYPE_GRAVITY;
        }

        return TYPE_GRAVITY_NOT_AVAILABLE;
    }

    /**
     * Check if the device has TYPE_LINEAR_ACCELERATION if registered of required for calculation.
     * @param sensorTypes Bitwise OR of DSensor types.
     * @return TYPE_LINEAR_ACCELERATION_NOT_NEEDED if TYPE_LINEAR_ACCELERATION is not registered
     *         or not needed for calculation. Else HAS_TYPE_LINEAR_ACCELERATION or
     *         TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE.
     */
    private static int hasTypeLinearAcceleration(int sensorTypes) {
        Logger.d(DSensorManager.class.getSimpleName(), "hasTypeLinearAcceleration(" + sensorTypes + ")");
        if ((sensorTypes & DSensor.TYPE_DEVICE_LINEAR_ACCELERATION) == 0
                && (sensorTypes & DSensor.TYPE_WORLD_LINEAR_ACCELERATION) == 0) {
            return TYPE_LINEAR_ACCELERATION_NOT_NEEDED;
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO
                && mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            return HAS_TYPE_LINEAR_ACCELERATION;
        }

        return TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE;
    }

    /**
     * Register sensors
     * @param sensorType One of SDK Sensor type.
     * @param sensorRate One of SensorManager.SENSOR_DELAY_*
     * @param errorValue Error to return if sensorType not available.
     * @return REGISTERED_TYPES_AVAILABLE if device has the sensor, else errorValue.
     */
    private static int registerListener(int sensorType, int sensorRate, int errorValue) {
        Logger.d(DSensorManager.class.getSimpleName(), "registerListener(" + sensorType + ", " + sensorRate + ", " + errorValue + ")");
        Sensor sensor = mSensorManager.getDefaultSensor(sensorType);
        if (sensor == null) {
            return errorValue;
        }

        mSensorManager.registerListener(mDSensorEventProcessor, mSensorManager.getDefaultSensor(sensorType),
                sensorRate, new Handler(mSensorThread.getLooper()));
        return REGISTERED_TYPES_AVAILABLE;
    }


    /**
     * Register all sensors in dSensorTypes or sensors required for calculation.
     * @param dSensorTypes Bitwise OR of DSensor types.
     * @param sensorRate One of SensorManager.SENSOR_DELAY_*
     * @param hasLinearAcceleration one of the flag HAS_TYPE_LINEAR_ACCELERATION,
     *                              TYPE_LINEAR_ACCELERATION_NOT_NEEDED or
     *                              TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE.
     * @param hasRotationVector one of the flag HAS_TYPE_ROTATION_VECTOR,
     *                          TYPE_ROTATION_VECTOR_NOT_NEEDED or
     *                          TYPE_ROTATION_VECTOR_NOT_AVAILABLE.
     * @param hasGravity one of the flag HAS_TYPE_GRAVITY,
     *                   TYPE_GRAVITY_NOT_NEEDED or
     *                   TYPE_GRAVITY_NOT_AVAILABLE.
     * @param worldTypesRegistered Is TYPE_WORLD_* registered.
     * @param directionTypesRegistered Is TYPE_*_DIRECTION registered.
     * @return REGISTERED_TYPES_AVAILABLE if device has all needed sensors else bitwise or of
     *         unavailable sensors.
     */
    private static int registerListener(int dSensorTypes, int sensorRate, int hasLinearAcceleration,
                                        int hasRotationVector, int hasGravity, boolean worldTypesRegistered,
                                        boolean directionTypesRegistered) {
        Logger.d(DSensorManager.class.getSimpleName(), "registerListener(" + dSensorTypes + ", "
                + sensorRate + ", " + hasGravity + ", " + hasLinearAcceleration + ")");
        int result = REGISTERED_TYPES_AVAILABLE;
        if (worldTypesRegistered || directionTypesRegistered) {
            if (hasRotationVector == HAS_TYPE_ROTATION_VECTOR) {
                result |= registerListener(Sensor.TYPE_ROTATION_VECTOR, sensorRate, TYPE_ROTATION_VECTOR_NOT_AVAILABLE);
                if ((dSensorTypes & DSensor.TYPE_DEVICE_MAGNETIC_FIELD) != 0
                        || (dSensorTypes & DSensor.TYPE_WORLD_MAGNETIC_FIELD) == 0) {
                    result |= registerListener(Sensor.TYPE_MAGNETIC_FIELD, sensorRate, TYPE_MAGNETIC_FIELD_NOT_AVAILABLE);
                }

                if (hasLinearAcceleration == TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE
                        || (dSensorTypes & DSensor.TYPE_DEVICE_ACCELEROMETER) != 0
                        || (dSensorTypes & DSensor.TYPE_WORLD_ACCELEROMETER) == 0) {
                    result |= registerListener(Sensor.TYPE_ACCELEROMETER, sensorRate, TYPE_ACCELEROMETER_NOT_AVAILABLE);
                }
            } else {
                result |= registerListener(Sensor.TYPE_MAGNETIC_FIELD, sensorRate, TYPE_MAGNETIC_FIELD_NOT_AVAILABLE);
                if (hasGravity == HAS_TYPE_GRAVITY) {
                    result |= registerListener(Sensor.TYPE_GRAVITY, sensorRate, TYPE_GRAVITY_NOT_AVAILABLE);
                    if (hasLinearAcceleration == TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE
                            || (dSensorTypes & DSensor.TYPE_DEVICE_ACCELEROMETER) != 0
                            || (dSensorTypes & DSensor.TYPE_WORLD_ACCELEROMETER) == 0) {
                        result |= registerListener(Sensor.TYPE_ACCELEROMETER, sensorRate, TYPE_ACCELEROMETER_NOT_AVAILABLE);
                    }
                } else {
                    result |= registerListener(Sensor.TYPE_ACCELEROMETER, sensorRate, TYPE_ACCELEROMETER_NOT_AVAILABLE);
                }
            }
        } else {
            if (hasGravity == HAS_TYPE_GRAVITY) {
                result |= registerListener(Sensor.TYPE_GRAVITY, sensorRate, TYPE_GRAVITY_NOT_AVAILABLE);
                if ((dSensorTypes & DSensor.TYPE_DEVICE_ACCELEROMETER) != 0
                        || (dSensorTypes & DSensor.TYPE_WORLD_ACCELEROMETER) == 0) {
                    result |= registerListener(Sensor.TYPE_ACCELEROMETER, sensorRate, TYPE_ACCELEROMETER_NOT_AVAILABLE);
                }
            } else if (hasGravity == TYPE_GRAVITY_NOT_AVAILABLE
                    || hasLinearAcceleration == TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE
                    || (dSensorTypes & DSensor.TYPE_DEVICE_ACCELEROMETER) != 0
                    || (dSensorTypes & DSensor.TYPE_WORLD_ACCELEROMETER) == 0) {
                result |= registerListener(Sensor.TYPE_ACCELEROMETER, sensorRate, TYPE_ACCELEROMETER_NOT_AVAILABLE);
            }

            if ((dSensorTypes & DSensor.TYPE_DEVICE_MAGNETIC_FIELD) != 0) {
                result |= registerListener(Sensor.TYPE_MAGNETIC_FIELD, sensorRate, TYPE_MAGNETIC_FIELD_NOT_AVAILABLE);
            }

            if ((dSensorTypes & DSensor.TYPE_ROTATION_VECTOR) != 0) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                    result |= registerListener(Sensor.TYPE_ROTATION_VECTOR, sensorRate, TYPE_ROTATION_VECTOR_NOT_AVAILABLE);
                } else {
                    result |= TYPE_ROTATION_VECTOR_NOT_AVAILABLE;
                }
            }
        }

        if (hasLinearAcceleration == HAS_TYPE_LINEAR_ACCELERATION) {
            result |= registerListener(Sensor.TYPE_LINEAR_ACCELERATION, sensorRate, TYPE_ACCELEROMETER_NOT_AVAILABLE);
        }

        if ((dSensorTypes & DSensor.TYPE_GYROSCOPE) != 0) {
            result |= registerListener(Sensor.TYPE_GYROSCOPE, sensorRate, TYPE_GYROSCOPE_NOT_AVAILABLE);
        }

        if ((dSensorTypes & DSensor.TYPE_DEPRECIATED_ORIENTATION) != 0) {
            //noinspection deprecation
            result |= registerListener(Sensor.TYPE_ORIENTATION, sensorRate,
                    TYPE_ORIENTATION_NOT_AVAILABLE);
        }

        return result;
    }

    private static int onTypeCompassRegistered(Context context, int sensorRate, int historyMaxLength,
                                               final DProcessedEventListener dProcessedEventListener) {
        final int dSensorDirectionTypes = getCompassDirectionType(context)  | DSensor.TYPE_MINUS_Z_AXIS_DIRECTION;
        Logger.d(DSensorManager.class.getSimpleName(), "onTypeCompassRegistered dSensorDirectionTypes = " + dSensorDirectionTypes);
        int flag = DSensorManager.startDSensor(context, dSensorDirectionTypes, sensorRate, historyMaxLength,
                new DSensorEventListener() {
                    @Override
                    public void onDSensorChanged(int changedDSensorTypes, DProcessedSensorEvent processedSensorEvent) {
                        DSensorEvent result;
                        if (Float.isNaN(processedSensorEvent.minusZAxisDirection.values[0])) {
                            if ((changedDSensorTypes & DSensor.TYPE_Y_AXIS_DIRECTION) == 0) {
                                if ((changedDSensorTypes & DSensor.TYPE_MINUS_Y_AXIS_DIRECTION) == 0) {
                                    if ((changedDSensorTypes & DSensor.TYPE_X_AXIS_DIRECTION) == 0) {
                                        result = processedSensorEvent.minusXAxisDirection;
                                    } else {
                                        result = processedSensorEvent.xAxisDirection;
                                    }
                                } else {
                                    result = processedSensorEvent.minusYAxisDirection;
                                }
                            } else {
                                result = processedSensorEvent.yAxisDirection;
                            }
                        } else {
                            result = processedSensorEvent.minusZAxisDirection;
                        }
                        dProcessedEventListener.onProcessedValueChanged(
                                new DSensorEvent(DProcessedSensor.TYPE_3D_COMPASS, result.accuracy,
                                        result.timestamp, result.values));
                    }
                });
        if ((flag & TYPE_MAGNETIC_FIELD_NOT_AVAILABLE) != 0
            || ((flag & TYPE_GRAVITY_NOT_AVAILABLE) != 0 && (flag & TYPE_ACCELEROMETER_NOT_AVAILABLE) != 0)) {
            stopDSensor();
        }
        return flag;
    }

    private static int onTypeCompassFlatOnlyRegistered(Context context, int sensorRate, int historyMaxLength,
                                                       final DProcessedEventListener dProcessedEventListener) {
        final int dSensorDirectionTypes = getCompassDirectionType(context);
        Logger.d(DSensorManager.class.getSimpleName(), "onTypeCompassRegistered dSensorDirectionTypes = " + dSensorDirectionTypes);
        int flag = DSensorManager.startDSensor(context, dSensorDirectionTypes, sensorRate, historyMaxLength,
                new DSensorEventListener() {
                    @Override
                    public void onDSensorChanged(int changedDSensorTypes, DProcessedSensorEvent processedSensorEvent) {
                        DSensorEvent result;
                        if ((changedDSensorTypes & DSensor.TYPE_Y_AXIS_DIRECTION) == 0) {
                            if ((changedDSensorTypes & DSensor.TYPE_MINUS_Y_AXIS_DIRECTION) == 0) {
                                if ((changedDSensorTypes & DSensor.TYPE_X_AXIS_DIRECTION) == 0) {
                                    result = processedSensorEvent.minusXAxisDirection;
                                } else {
                                    result = processedSensorEvent.xAxisDirection;
                                }
                            } else {
                                result = processedSensorEvent.minusYAxisDirection;
                            }
                        } else {
                            result = processedSensorEvent.yAxisDirection;
                        }
                        dProcessedEventListener.onProcessedValueChanged(
                                new DSensorEvent(DProcessedSensor.TYPE_COMPASS_FLAT_ONLY, result.accuracy,
                                        result.timestamp, result.values));
                    }
                });
        if ((flag & TYPE_MAGNETIC_FIELD_NOT_AVAILABLE) != 0
                || ((flag & TYPE_GRAVITY_NOT_AVAILABLE) != 0 && (flag & TYPE_ACCELEROMETER_NOT_AVAILABLE) != 0)) {
            stopDSensor();
        }
        return flag;
    }

    private static int onTypeCompassFlatOnlyAndOrientationRegister(Context context, int sensorRate, int historyMaxLength,
                                                                   final DProcessedEventListener dProcessedEventListener) {
        final int dSensorDirectionTypes = getCompassDirectionType(context) | DSensor.TYPE_DEPRECIATED_ORIENTATION;
        Logger.d(DSensorManager.class.getSimpleName(), "onTypeCompassFlatOnlyAndOrientationRegister dSensorDirectionTypes = " + dSensorDirectionTypes);
        int flag = DSensorManager.startDSensor(context, dSensorDirectionTypes, sensorRate, historyMaxLength,
                new DSensorEventListener() {
                    @Override
                    public void onDSensorChanged(int changedDSensorTypes, DProcessedSensorEvent processedSensorEvent) {
                        DSensorEvent result;
                        if ((changedDSensorTypes & DSensor.TYPE_DEPRECIATED_ORIENTATION) == 0) {
                            if ((changedDSensorTypes & DSensor.TYPE_Y_AXIS_DIRECTION) == 0) {
                                if ((changedDSensorTypes & DSensor.TYPE_MINUS_Y_AXIS_DIRECTION) == 0) {
                                    if ((changedDSensorTypes & DSensor.TYPE_X_AXIS_DIRECTION) == 0) {
                                        result = processedSensorEvent.minusXAxisDirection;
                                    } else {
                                        result = processedSensorEvent.xAxisDirection;
                                    }
                                } else {
                                    result = processedSensorEvent.minusYAxisDirection;
                                }
                            } else {
                                result = processedSensorEvent.yAxisDirection;
                            }
                        } else {
                            result = processedSensorEvent.depreciatedOrientation;
                        }
                        dProcessedEventListener.onProcessedValueChanged(
                                new DSensorEvent((changedDSensorTypes & DSensor.TYPE_DEPRECIATED_ORIENTATION) == 0
                                        ? DProcessedSensor.TYPE_3D_COMPASS : DSensor.TYPE_DEPRECIATED_ORIENTATION,
                                        result.accuracy, result.timestamp, result.values));
                    }
                });
        if ((flag & TYPE_MAGNETIC_FIELD_NOT_AVAILABLE) != 0
                || ((flag & TYPE_GRAVITY_NOT_AVAILABLE) != 0 && (flag & TYPE_ACCELEROMETER_NOT_AVAILABLE) != 0)) {
            stopDSensor();
        }
        return flag;
    }

    private static int onType3DCompassAndOrientationRegister(Context context, int sensorRate, int historyMaxLength,
                                                             final DProcessedEventListener dProcessedEventListener) {
        final int dSensorDirectionTypes = getCompassDirectionType(context)
                | DSensor.TYPE_MINUS_Z_AXIS_DIRECTION | DSensor.TYPE_DEPRECIATED_ORIENTATION;
        int flag = DSensorManager.startDSensor(context, dSensorDirectionTypes, sensorRate, historyMaxLength,
                new DSensorEventListener() {
                    @Override
                    public void onDSensorChanged(int changedDSensorTypes, DProcessedSensorEvent processedSensorEvent) {
                        DSensorEvent result;
                        if ((changedDSensorTypes & DSensor.TYPE_DEPRECIATED_ORIENTATION) == 0) {
                            if (Float.isNaN(processedSensorEvent.minusZAxisDirection.values[0])) {
                                if ((changedDSensorTypes & DSensor.TYPE_Y_AXIS_DIRECTION) == 0) {
                                    if ((changedDSensorTypes & DSensor.TYPE_MINUS_Y_AXIS_DIRECTION) == 0) {
                                        if ((changedDSensorTypes & DSensor.TYPE_X_AXIS_DIRECTION) == 0) {
                                            result = processedSensorEvent.minusXAxisDirection;
                                        } else {
                                            result = processedSensorEvent.xAxisDirection;
                                        }
                                    } else {
                                        result = processedSensorEvent.minusYAxisDirection;
                                    }
                                } else {
                                    result = processedSensorEvent.yAxisDirection;
                                }
                            } else {
                                result = processedSensorEvent.minusZAxisDirection;
                            }
                        } else {
                            result = processedSensorEvent.depreciatedOrientation;
                        }
                        dProcessedEventListener.onProcessedValueChanged(
                                new DSensorEvent((changedDSensorTypes & DSensor.TYPE_DEPRECIATED_ORIENTATION) == 0
                                        ? DProcessedSensor.TYPE_3D_COMPASS : DSensor.TYPE_DEPRECIATED_ORIENTATION,
                                        result.accuracy, result.timestamp, result.values));
                    }
                });
        if ((flag & TYPE_MAGNETIC_FIELD_NOT_AVAILABLE) != 0
                || ((flag & TYPE_GRAVITY_NOT_AVAILABLE) != 0 && (flag & TYPE_ACCELEROMETER_NOT_AVAILABLE) != 0)) {
            stopDSensor();
        }
        return flag;
    }

    private static int getCompassDirectionType(Context context) {
        int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        if (rotation == Surface.ROTATION_90) {
            return DSensor.TYPE_X_AXIS_DIRECTION;
        }
        if (rotation == Surface.ROTATION_180) {
            return DSensor.TYPE_MINUS_Y_AXIS_DIRECTION;
        }
        if (rotation == Surface.ROTATION_270) {
            return DSensor.TYPE_MINUS_X_AXIS_DIRECTION;
        }
        return DSensor.TYPE_Y_AXIS_DIRECTION;
    }

    /**
     *  Unregister all listeners.
     */
    public static void stopDSensor() {
        Logger.d(DSensorManager.class.getSimpleName(), "stopDSensor");
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mDSensorEventProcessor);
            mSensorThread.quit();
            mSensorManager = null;
        }
    }


}
