package com.hoan.dsensor_master;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.hoan.dsensor_master.interfaces.DSensorEventListener;
import com.hoan.dsensor_master.utils.DMath;
import com.hoan.dsensor_master.utils.Logger;

import java.util.LinkedList;

/**
 * Class to process onSensorChanged
 * Created by Hoan on 1/31/2016.
 */
public class DSensorEventProcessor implements SensorEventListener {

    public static  final int DEFAULT_HISTORY_SIZE = 10;

    private static final float TWENTY_FIVE_DEGREE_IN_RADIAN = 0.436332313f;
    private static final float ONE_FIFTY_FIVE_DEGREE_IN_RADIAN = 2.7052603f;

    /**
     *  Low pass filter constant.
     *  Use to filter linear acceleration from accelerometer values.
     */
    private static final float ALPHA = .1f;
    private static final float ONE_MINUS_ALPHA = 1 - ALPHA;

    private final float[] mRotationMatrix = new float[9];
    private DSensorEvent mAccelerometer;
    private DSensorEvent mGravity;
    private DSensorEvent mMagneticField;
    // need this member to calculate TYPE_WORLD_LINEAR_ACCELERATION
    // when device has TYPE_LINEAR_ACCELERATION
    private DSensorEvent mLinearAcceleration;
    private float mInclination;

    /**
     * List to keep history of sensor in world coordinate for averaging.
     */
    private WorldHistory mAccelerometerInWorldBasisHistories;
    private WorldHistory mGravityInWorldBasisHistories;
    private WorldHistory mLinearAccelerationInWorldBasisHistories;

    /**
     *  List to keep history directions of compass for averaging.
     */
    private DirectionHistory mXAxisDirectionHistories;
    private DirectionHistory mMinusXAxisDirectionHistories;
    private DirectionHistory mYAxisDirectionHistories;
    private DirectionHistory mMinusYAxisDirectionHistories;
    private DirectionHistory mZAxisDirectionHistories;
    private DirectionHistory mMinusZAxisDirectionHistories;

    /**
     *  The type of sensor to listen to. See DSensor.
     */
    private final int mDSensorTypes;

    /**
     * For DSensor types that required Rotation Matrix
     * i.e. direction or world coordinate
     */
    private final boolean mProcessDataWithRotationMatrix;

    /**
     * For sensor type that does not require Rotation Matrix'
     * but gravity for calculation
     */
    private final boolean mProcessData;

    private final boolean mCalculateInclination;

    /**
     * Flag to indicate device has TYPE_ROTATION_VECTOR. If value is HAS_TYPE_ROTATION_VECTOR then
     * all calculation is done when onSensorChanged is of TYPE_ROTATION_VECTOR.
     */
    private final int mHasTypeRotationVector;

    /**
     * Flag to indicate device has TYPE_GRAVITY. If needed and the value is
     * DSensorManager.TYPE_GRAVITY_NOT_AVAILABLE then use low filter on accelerometer value.
     */
    private final int mHasTypeGravity;

    /**
     * Flag to indicate device has TYPE_LINEAR_ACCELERATION. If needed and the value is
     * DSensorManager.TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE then calculate from
     * accelerometer and gravity.
     */
    private final int mHasTypeLinearAcceleration;

    private final DSensorEventListener mDSensorEventListener;
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());

    /**
     * Constructor.
     * @param dSensorTypes Bitwise OR of DSensor types.
     * @param historyMaxLength Maximum list length to keep history.
     * @param hasTypeRotationVector one of the flag HAS_TYPE_ROTATION_VECTOR,
     *                              TYPE_ROTATION_VECTOR_NOT_NEEDED or
     *                              TYPE_ROTATION_VECTOR_NOT_AVAILABLE.
     * @param hasTypeGravity one of the flag HAS_TYPE_GRAVITY,
     *                       TYPE_GRAVITY_NOT_NEEDED or
     *                       TYPE_GRAVITY_NOT_AVAILABLE.
     * @param hasTypeLinearAcceleration one of the flag HAS_TYPE_LINEAR_ACCELERATION,
     *                                  TYPE_LINEAR_ACCELERATION_NOT_NEEDED or
     *                                  TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE.
     * @param dSensorEventListener Callback for processed values
     */
    public DSensorEventProcessor(int dSensorTypes, int historyMaxLength, int hasTypeRotationVector, int hasTypeGravity,
                                 int hasTypeLinearAcceleration, DSensorEventListener dSensorEventListener) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "constructor(" + dSensorTypes + ", " + historyMaxLength
                + ", " + hasTypeRotationVector + ", " + hasTypeGravity + ", " + hasTypeLinearAcceleration + ")");
        mDSensorTypes = dSensorTypes;
        mDSensorEventListener = dSensorEventListener;
        mHasTypeRotationVector = hasTypeRotationVector;
        mHasTypeGravity = hasTypeGravity;
        mHasTypeLinearAcceleration = hasTypeLinearAcceleration;
        boolean hasDirectionMember = initDirectionHistoryMembers(historyMaxLength);
        mProcessDataWithRotationMatrix = initWorldHistoryMembers(historyMaxLength) || hasDirectionMember;
        mCalculateInclination = (mDSensorTypes & DSensor.TYPE_INCLINATION) != 0
                || (mDSensorTypes & DSensor.TYPE_DEVICE_ROTATION) != 0 || hasDirectionMember;
        mProcessData = mCalculateInclination || (mDSensorTypes & DSensor.TYPE_PITCH) != 0
                || (mDSensorTypes & DSensor.TYPE_ROLL) != 0;
        initDSensorEventMembers();
    }

    private void initDSensorEventMembers() {
        if (mProcessDataWithRotationMatrix) {
            mMagneticField = new DSensorEvent(DSensor.TYPE_DEVICE_MAGNETIC_FIELD, 3);
            mGravity = new DSensorEvent(DSensor.TYPE_DEVICE_GRAVITY, 3);
        } else if ((mDSensorTypes & DSensor.TYPE_WORLD_GRAVITY) != 0){
            mGravity = new DSensorEvent(DSensor.TYPE_DEVICE_GRAVITY, 3);
        } else if ((mDSensorTypes & DSensor.TYPE_WORLD_MAGNETIC_FIELD) != 0){
            mMagneticField = new DSensorEvent(DSensor.TYPE_DEVICE_MAGNETIC_FIELD, 3);
        }

        if (mHasTypeGravity == DSensorManager.TYPE_GRAVITY_NOT_AVAILABLE
                || (mDSensorTypes & DSensor.TYPE_WORLD_ACCELEROMETER) != 0) {
            mAccelerometer = new DSensorEvent(DSensor.TYPE_DEVICE_ACCELEROMETER, 3);
        }

        if (mHasTypeLinearAcceleration == DSensorManager.TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE
                && (mDSensorTypes & DSensor.TYPE_WORLD_LINEAR_ACCELERATION) != 0) {
            mLinearAcceleration = new DSensorEvent(DSensor.TYPE_DEVICE_LINEAR_ACCELERATION, 3);
        }
    }

    private boolean initDirectionHistoryMembers(int historyMaxLength) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "initDirectionHistoryMembers(" + historyMaxLength + ")");
        if ((mDSensorTypes & DSensor.TYPE_X_AXIS_DIRECTION) != 0) {
            mXAxisDirectionHistories = new DirectionHistory(historyMaxLength);
        }

        if ((mDSensorTypes & DSensor.TYPE_MINUS_X_AXIS_DIRECTION) != 0) {
            mMinusXAxisDirectionHistories = new DirectionHistory(historyMaxLength);
        }

        if ((mDSensorTypes & DSensor.TYPE_Y_AXIS_DIRECTION) != 0) {
            mYAxisDirectionHistories = new DirectionHistory(historyMaxLength);
        }

        if ((mDSensorTypes & DSensor.TYPE_MINUS_Y_AXIS_DIRECTION) != 0) {
            mMinusYAxisDirectionHistories = new DirectionHistory(historyMaxLength);
        }

        if ((mDSensorTypes & DSensor.TYPE_Z_AXIS_DIRECTION) != 0) {
            mZAxisDirectionHistories = new DirectionHistory(historyMaxLength);
        }

        if ((mDSensorTypes & DSensor.TYPE_MINUS_Z_AXIS_DIRECTION) != 0) {
            mMinusZAxisDirectionHistories = new DirectionHistory(historyMaxLength);
        }

        return mXAxisDirectionHistories != null || mYAxisDirectionHistories != null
                || mZAxisDirectionHistories != null || mMinusXAxisDirectionHistories != null
                || mMinusYAxisDirectionHistories != null || mMinusZAxisDirectionHistories != null;
    }

    private boolean initWorldHistoryMembers(int historyMaxLength) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "initWorldHistoryMembers(" + historyMaxLength + ")");
        if ((mDSensorTypes & DSensor.TYPE_WORLD_ACCELEROMETER) != 0) {
            mAccelerometerInWorldBasisHistories = new WorldHistory(historyMaxLength, 3);
        }

        if ((mDSensorTypes & DSensor.TYPE_WORLD_GRAVITY) != 0) {
            mGravityInWorldBasisHistories = new WorldHistory(historyMaxLength, 3);
        }

        if ((mDSensorTypes & DSensor.TYPE_WORLD_LINEAR_ACCELERATION) != 0) {
            mLinearAccelerationInWorldBasisHistories = new WorldHistory(historyMaxLength, 3);
        }

        return ((mDSensorTypes & DSensor.TYPE_WORLD_MAGNETIC_FIELD) != 0)
                || mLinearAccelerationInWorldBasisHistories != null
                || mGravityInWorldBasisHistories != null
                || mAccelerometerInWorldBasisHistories != null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "onSensorChanged");
        final DProcessedSensorEvent.DProcessedSensorEventBuilder builder
                = new DProcessedSensorEvent.DProcessedSensorEventBuilder();
        final int changedSensorTypes;
        int sensorType = event.sensor.getType();
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            changedSensorTypes = onAccelerometerChanged(event, builder);
        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {
            changedSensorTypes = onGyroscopeChanged(event, builder);
        } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
            changedSensorTypes = onMagneticFieldChanged(event, builder);
        } else if (sensorType == Sensor.TYPE_ORIENTATION) {
                changedSensorTypes = onOrientationChanged(event, builder);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            if (sensorType == Sensor.TYPE_GRAVITY) {
                changedSensorTypes = onGravityChanged(event, builder);
            } else if (sensorType == Sensor.TYPE_LINEAR_ACCELERATION) {
                changedSensorTypes = onLinearAccelerationChanged(event, builder);
            } else if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
                changedSensorTypes = onRotationVectorChanged(event, builder);
            } else {
                changedSensorTypes = 0;
            }
        } else {
            changedSensorTypes = 0;
        }

        if (changedSensorTypes != 0) {
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDSensorEventListener.onDSensorChanged(changedSensorTypes, builder.build());
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "onAccuracyChanged(" + sensor.getName() + ", " + accuracy + ")");
    }

    private int onAccelerometerChanged(SensorEvent event, DProcessedSensorEvent.DProcessedSensorEventBuilder builder) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "onAccelerometerChanged");
        int changedSensorTypes = 0;
        if (mAccelerometer != null) {
            mAccelerometer.accuracy = event.accuracy;
            mAccelerometer.timestamp = event.timestamp;
            System.arraycopy(event.values, 0, mAccelerometer.values, 0, event.values.length);
        }
        if ((mDSensorTypes & DSensor.TYPE_DEVICE_ACCELEROMETER) != 0) {
            builder.setAccelerometerInDeviceBasis(new DSensorEvent(DSensor.TYPE_DEVICE_ACCELEROMETER,
                    event.accuracy, event.timestamp, event.values));
            changedSensorTypes = DSensor.TYPE_DEVICE_ACCELEROMETER;
        }
        if (mHasTypeGravity == DSensorManager.TYPE_GRAVITY_NOT_AVAILABLE) {
            changedSensorTypes |= calculateGravity(builder);
            if ((mDSensorTypes & DSensor.TYPE_DEVICE_LINEAR_ACCELERATION) != 0
                    || (mDSensorTypes & DSensor.TYPE_WORLD_LINEAR_ACCELERATION) != 0) {
                changedSensorTypes |= calculateLinearAcceleration(builder);
            }
            if (mMagneticField != null) {
                if (mProcessDataWithRotationMatrix && mHasTypeRotationVector == DSensorManager.TYPE_ROTATION_VECTOR_NOT_AVAILABLE) {
                    if (SensorManager.getRotationMatrix(mRotationMatrix, null, mGravity.values, mMagneticField.values)) {
                        changedSensorTypes |= processSensorDataWithRotationMatrix(builder);
                    }
                } else if (mProcessData) {
                    changedSensorTypes |= processSensorData(builder);
                }
            }
        }
        return changedSensorTypes;
    }

    private int onGravityChanged(SensorEvent event, DProcessedSensorEvent.DProcessedSensorEventBuilder builder) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "onGravityChanged");
        int changedSensorTypes = 0;
        if (mGravity != null) {
            mGravity.accuracy = event.accuracy;
            mGravity.timestamp = event.timestamp;
            System.arraycopy(event.values, 0, mGravity.values, 0, event.values.length);
        }
        if ((mDSensorTypes & DSensor.TYPE_DEVICE_GRAVITY) != 0) {
            builder.setGravityInDeviceBasis(new DSensorEvent(DSensor.TYPE_DEVICE_GRAVITY, event.accuracy,
                    event.timestamp, event.values));
            changedSensorTypes |= DSensor.TYPE_DEVICE_GRAVITY;
        }
        if (mMagneticField != null) {
            if (mProcessDataWithRotationMatrix && mHasTypeRotationVector == DSensorManager.TYPE_ROTATION_VECTOR_NOT_AVAILABLE) {
                if (SensorManager.getRotationMatrix(mRotationMatrix, null, mGravity.values, mMagneticField.values)) {
                    changedSensorTypes |= processSensorDataWithRotationMatrix(builder);
                }
            } else if (mProcessData) {
                changedSensorTypes |= processSensorData(builder);
            }
        }

        return changedSensorTypes;
    }

    private int onMagneticFieldChanged(SensorEvent event, DProcessedSensorEvent.DProcessedSensorEventBuilder builder) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "onMagneticFieldChanged");
        int changedSensorTypes = 0;
        if (mMagneticField != null) {
            mMagneticField.accuracy = event.accuracy;
            mMagneticField.timestamp = event.timestamp;
            System.arraycopy(event.values, 0, mMagneticField.values, 0, event.values.length);
        }
        if ((mDSensorTypes & DSensor.TYPE_DEVICE_MAGNETIC_FIELD) != 0) {
            builder.setMagneticFieldInDeviceBasis(new DSensorEvent(DSensor.TYPE_DEVICE_MAGNETIC_FIELD,
                    event.accuracy, event.timestamp, event.values));
            changedSensorTypes |= DSensor.TYPE_DEVICE_MAGNETIC_FIELD;
        }
        if (mGravity != null) {
            if (mProcessDataWithRotationMatrix && mHasTypeRotationVector == DSensorManager.TYPE_ROTATION_VECTOR_NOT_AVAILABLE) {
                if (SensorManager.getRotationMatrix(mRotationMatrix, null, mGravity.values, mMagneticField.values)) {
                    changedSensorTypes |= processSensorDataWithRotationMatrix(builder);
                }
            } else if (mProcessData) {
                changedSensorTypes |= processSensorData(builder);
            }
        }

        return changedSensorTypes;
    }

    private int onLinearAccelerationChanged(SensorEvent event, DProcessedSensorEvent.DProcessedSensorEventBuilder builder) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "onLinearAccelerationChanged");
        int changedSensorTypes = 0;
        if ((mDSensorTypes & DSensor.TYPE_DEVICE_LINEAR_ACCELERATION) != 0) {
            builder.setLinearAccelerationInDeviceBasis(new DSensorEvent(DSensor.TYPE_DEVICE_LINEAR_ACCELERATION,
                    event.accuracy, event.timestamp, event.values));
            changedSensorTypes |= DSensor.TYPE_DEVICE_LINEAR_ACCELERATION;
        }
        if ((mDSensorTypes & DSensor.TYPE_WORLD_LINEAR_ACCELERATION) != 0) {
            if (mRotationMatrix != null) {
                mLinearAccelerationInWorldBasisHistories.add(DSensor.TYPE_DEVICE_LINEAR_ACCELERATION,
                        event.accuracy, event.timestamp, event.values, mRotationMatrix);
                builder.setLinearAccelerationInWorldBasis(mLinearAccelerationInWorldBasisHistories
                        .getAverageSensorEvent(DSensor.TYPE_DEVICE_LINEAR_ACCELERATION));
                changedSensorTypes |= DSensor.TYPE_WORLD_LINEAR_ACCELERATION;
            }
        }
        return changedSensorTypes;
    }

    private int onOrientationChanged(SensorEvent event, DProcessedSensorEvent.DProcessedSensorEventBuilder builder) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "onOrientationChanged  angle = " + Math.round(event.values[0]));
        builder.setDepreciatedOrientation(new DSensorEvent(DSensor.TYPE_DEPRECIATED_ORIENTATION,
                event.accuracy, event.timestamp, event.values));
        return DSensor.TYPE_DEPRECIATED_ORIENTATION;
    }

    private int onGyroscopeChanged(SensorEvent event, DProcessedSensorEvent.DProcessedSensorEventBuilder builder) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "onGyroscopeChanged");
        int changedSensorTypes = 0;
        float[] gyroscope = new float[event.values.length];
        System.arraycopy(event.values, 0, gyroscope, 0, event.values.length);
        if ((mDSensorTypes & DSensor.TYPE_GYROSCOPE) != 0) {
            builder.setGyroscope(new DSensorEvent(DSensor.TYPE_GYROSCOPE, event.accuracy,
                    event.timestamp, gyroscope));
            changedSensorTypes |= DSensor.TYPE_GYROSCOPE;
        }
        return changedSensorTypes;
    }

    @TargetApi(9)
    private int onRotationVectorChanged(SensorEvent event, DProcessedSensorEvent.DProcessedSensorEventBuilder builder) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "onRotationVectorChanged");
        int changedSensorTypes = 0;
        float[] rotationVector = new float[event.values.length];
        System.arraycopy(event.values, 0, rotationVector, 0, event.values.length);
        if ((mDSensorTypes & DSensor.TYPE_ROTATION_VECTOR) != 0) {
            builder.setRotationVector(new DSensorEvent(DSensor.TYPE_ROTATION_VECTOR, event.accuracy,
                    event.timestamp, rotationVector));
            changedSensorTypes |= DSensor.TYPE_ROTATION_VECTOR;
        }
        SensorManager.getRotationMatrixFromVector(mRotationMatrix, rotationVector);
        if (mProcessDataWithRotationMatrix) {
            changedSensorTypes |= processSensorDataWithRotationMatrix(builder);
        }
        return changedSensorTypes;
    }

    private int processSensorData(DProcessedSensorEvent.DProcessedSensorEventBuilder builder) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "processSensorData");
        int changedSensorTypes = 0;
        float gravityNorm = DMath.calculateNorm(mGravity.values);
        if ((mDSensorTypes & DSensor.TYPE_DEVICE_ROTATION) != 0) {
            mInclination = (float) Math.acos(mGravity.values[2] / gravityNorm);
            if ((mDSensorTypes & DSensor.TYPE_INCLINATION) != 0) {
                builder.setInclination(new DSensorEvent(DSensor.TYPE_INCLINATION,
                        mGravity.accuracy, mGravity.timestamp, mInclination));
                changedSensorTypes |= DSensor.TYPE_INCLINATION;
            }
            float deviceRotation;
            if (mInclination < TWENTY_FIVE_DEGREE_IN_RADIAN
                    || mInclination > ONE_FIFTY_FIVE_DEGREE_IN_RADIAN) {
                deviceRotation = Float.NaN;
            } else {
                deviceRotation = (float) Math.atan2(mGravity.values[0] / gravityNorm,
                        mGravity.values[1] / gravityNorm);
            }
            builder.setDeviceRotation(new DSensorEvent(DSensor.TYPE_DEVICE_ROTATION,
                    mGravity.accuracy, mGravity.timestamp, deviceRotation));
            changedSensorTypes |= DSensor.TYPE_DEVICE_ROTATION;
        } else if ((mDSensorTypes & DSensor.TYPE_INCLINATION) != 0) {
            mInclination = (float) Math.acos(mGravity.values[2] / gravityNorm);
            builder.setInclination(new DSensorEvent(DSensor.TYPE_INCLINATION,
                    mGravity.accuracy, mGravity.timestamp, mInclination));
            changedSensorTypes |= DSensor.TYPE_INCLINATION;
        }
        if ((mDSensorTypes & DSensor.TYPE_PITCH) != 0) {
            builder.setPitch(new DSensorEvent(DSensor.TYPE_PITCH, mGravity.accuracy,
                    mGravity.timestamp, (float)Math.asin(-mGravity.values[1] / gravityNorm)));
            changedSensorTypes |= DSensor.TYPE_PITCH;
        }
        if ((mDSensorTypes & DSensor.TYPE_ROLL) != 0) {
            builder.setRoll(new DSensorEvent(DSensor.TYPE_ROLL, mGravity.accuracy, mGravity.timestamp,
                    (float)Math.atan2(-mGravity.values[0] / gravityNorm, mGravity.values[2] / gravityNorm)));
            changedSensorTypes |= DSensor.TYPE_ROLL;
        }
        return changedSensorTypes;
    }

    private int processSensorDataWithRotationMatrix(DProcessedSensorEvent.DProcessedSensorEventBuilder builder) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "processSensorDataWithRotationMatrix()");
        int changedSensorTypes = 0;

        if ((mDSensorTypes & DSensor.TYPE_ROLL) != 0) {
            builder.setRoll(new DSensorEvent(DSensor.TYPE_ROLL, mGravity.accuracy, mGravity.timestamp,
                    (float)Math.atan2(-mRotationMatrix[6], mRotationMatrix[8])));
            changedSensorTypes |= DSensor.TYPE_ROLL;
        }

        if ((mDSensorTypes & DSensor.TYPE_PITCH) != 0) {
            builder.setPitch(new DSensorEvent(DSensor.TYPE_PITCH, mGravity.accuracy,
                    mGravity.timestamp, (float) Math.asin(-mRotationMatrix[7])));
            changedSensorTypes |= DSensor.TYPE_PITCH;
        }

        if ((mDSensorTypes & DSensor.TYPE_WORLD_ACCELEROMETER) != 0) {
            mAccelerometerInWorldBasisHistories.add(mAccelerometer, mRotationMatrix);
            builder.setAccelerometerInWorldBasis(mAccelerometerInWorldBasisHistories
                    .getAverageSensorEvent(DSensor.TYPE_WORLD_ACCELEROMETER));
            changedSensorTypes |= DSensor.TYPE_WORLD_ACCELEROMETER;
        }

        if ((mDSensorTypes & DSensor.TYPE_WORLD_GRAVITY) != 0) {
            mGravityInWorldBasisHistories.add(mGravity, mRotationMatrix);
            builder.setGravityInWorldBasis(mGravityInWorldBasisHistories
                    .getAverageSensorEvent(DSensor.TYPE_WORLD_GRAVITY));
            changedSensorTypes |= DSensor.TYPE_WORLD_GRAVITY;
        }

        if ((mDSensorTypes & DSensor.TYPE_WORLD_MAGNETIC_FIELD) != 0) {
            builder.setMagneticFieldInWorldBasis(new DSensorEvent(DSensor.TYPE_WORLD_MAGNETIC_FIELD,
                    mMagneticField.accuracy, mMagneticField.timestamp,
                    DMath.productOfSquareMatrixAndVector(mRotationMatrix, mMagneticField.values)));
            changedSensorTypes |= DSensor.TYPE_WORLD_MAGNETIC_FIELD;
        }

        if (mHasTypeLinearAcceleration == DSensorManager.TYPE_LINEAR_ACCELERATION_NOT_AVAILABLE) {
            mLinearAccelerationInWorldBasisHistories.add(mLinearAcceleration, mRotationMatrix);
            builder.setLinearAccelerationInWorldBasis(mLinearAccelerationInWorldBasisHistories
                    .getAverageSensorEvent(DSensor.TYPE_WORLD_MAGNETIC_FIELD));
            changedSensorTypes |= DSensor.TYPE_WORLD_LINEAR_ACCELERATION;
        }

        if (mCalculateInclination) {
            mInclination = (float) Math.acos(mRotationMatrix[8]);
            if ((mDSensorTypes & DSensor.TYPE_INCLINATION) != 0) {
                builder.setInclination(new DSensorEvent(DSensor.TYPE_INCLINATION,
                        mGravity.accuracy, mGravity.timestamp, mInclination));
                changedSensorTypes |= DSensor.TYPE_INCLINATION;
            }

            // Due to noise and numerical limitation, only compass
            // field is calculable when inclination < 25 or inclination > 155
            if (mInclination < TWENTY_FIVE_DEGREE_IN_RADIAN
                    || mInclination > ONE_FIFTY_FIVE_DEGREE_IN_RADIAN) {
                if ((mDSensorTypes & DSensor.TYPE_Z_AXIS_DIRECTION) != 0) {
                    mZAxisDirectionHistories.clearHistories();
                    builder.setZAxisDirection(new DSensorEvent(DSensor.TYPE_Z_AXIS_DIRECTION, 0, 0, Float.NaN));
                    changedSensorTypes |= DSensor.TYPE_Z_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_MINUS_Z_AXIS_DIRECTION) != 0) {
                    mMinusZAxisDirectionHistories.clearHistories();
                    builder.setMinusZAxisDirection(new DSensorEvent(DSensor.TYPE_MINUS_Z_AXIS_DIRECTION, 0, 0, Float.NaN));
                    changedSensorTypes |= DSensor.TYPE_MINUS_Z_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_Y_AXIS_DIRECTION) != 0) {
                    mYAxisDirectionHistories.add(new DSensorEvent(DSensor.TYPE_Y_AXIS_DIRECTION,
                            mGravity.accuracy, mGravity.timestamp > mMagneticField.timestamp ? mGravity.timestamp
                            : mMagneticField.timestamp, (float) Math.atan2(mRotationMatrix[1], mRotationMatrix[4])));
                    builder.setYAxisDirection(mYAxisDirectionHistories
                            .getAverageSensorEvent(DSensor.TYPE_Y_AXIS_DIRECTION));
                    changedSensorTypes |= DSensor.TYPE_Y_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_MINUS_Y_AXIS_DIRECTION) != 0) {
                    mMinusYAxisDirectionHistories.add(new DSensorEvent(DSensor.TYPE_MINUS_Y_AXIS_DIRECTION,
                            mGravity.accuracy, mGravity.timestamp > mMagneticField.timestamp ? mGravity.timestamp
                            : mMagneticField.timestamp, (float) Math.atan2(-mRotationMatrix[1], -mRotationMatrix[4])));
                    builder.setMinusYAxisDirection(mMinusYAxisDirectionHistories
                            .getAverageSensorEvent(DSensor.TYPE_MINUS_Y_AXIS_DIRECTION));
                    changedSensorTypes |= DSensor.TYPE_MINUS_Y_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_X_AXIS_DIRECTION) != 0) {
                    mXAxisDirectionHistories.add(new DSensorEvent(DSensor.TYPE_X_AXIS_DIRECTION,
                            mGravity.accuracy, mGravity.timestamp > mMagneticField.timestamp ? mGravity.timestamp
                            : mMagneticField.timestamp, (float) Math.atan2(mRotationMatrix[0], mRotationMatrix[3])));
                    builder.setXAxisDirection(mXAxisDirectionHistories
                            .getAverageSensorEvent(DSensor.TYPE_X_AXIS_DIRECTION));
                    changedSensorTypes |= DSensor.TYPE_X_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_MINUS_X_AXIS_DIRECTION) != 0) {
                    mMinusXAxisDirectionHistories.add(new DSensorEvent(DSensor.TYPE_MINUS_X_AXIS_DIRECTION,
                            mGravity.accuracy, mGravity.timestamp > mMagneticField.timestamp ? mGravity.timestamp
                            : mMagneticField.timestamp, (float) Math.atan2(-mRotationMatrix[0], -mRotationMatrix[3])));
                    builder.setMinusXAxisDirection(mMinusXAxisDirectionHistories
                            .getAverageSensorEvent(DSensor.TYPE_MINUS_X_AXIS_DIRECTION));
                    changedSensorTypes |= DSensor.TYPE_MINUS_X_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_DEVICE_ROTATION) != 0) {
                    builder.setDeviceRotation(new DSensorEvent(DSensor.TYPE_DEVICE_ROTATION,
                            mGravity.accuracy, mGravity.timestamp, Float.NaN));
                    changedSensorTypes |= DSensor.TYPE_DEVICE_ROTATION;
                }
            } else {
                if ((mDSensorTypes & DSensor.TYPE_Y_AXIS_DIRECTION) != 0) {
                    mYAxisDirectionHistories.clearHistories();
                    builder.setYAxisDirection(new DSensorEvent(DSensor.TYPE_Y_AXIS_DIRECTION, 0, 0, Float.NaN));
                    changedSensorTypes |= DSensor.TYPE_Y_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_MINUS_Y_AXIS_DIRECTION) != 0) {
                    mMinusYAxisDirectionHistories.clearHistories();
                    builder.setMinusYAxisDirection(new DSensorEvent(DSensor.TYPE_MINUS_Y_AXIS_DIRECTION, 0, 0, Float.NaN));
                    changedSensorTypes |= DSensor.TYPE_MINUS_Y_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_X_AXIS_DIRECTION) != 0) {
                    mXAxisDirectionHistories.clearHistories();
                    builder.setXAxisDirection(new DSensorEvent(DSensor.TYPE_X_AXIS_DIRECTION, 0, 0, Float.NaN));
                    changedSensorTypes |= DSensor.TYPE_X_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_MINUS_X_AXIS_DIRECTION) != 0) {
                    mMinusXAxisDirectionHistories.clearHistories();
                    builder.setMinusXAxisDirection(new DSensorEvent(DSensor.TYPE_MINUS_X_AXIS_DIRECTION, 0, 0, Float.NaN));
                    changedSensorTypes |= DSensor.TYPE_MINUS_X_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_Z_AXIS_DIRECTION) != 0) {
                    mZAxisDirectionHistories.add(new DSensorEvent(DSensor.TYPE_Z_AXIS_DIRECTION,
                            mGravity.accuracy, mGravity.timestamp > mMagneticField.timestamp ? mGravity.timestamp
                            : mMagneticField.timestamp, (float) Math.atan2(mRotationMatrix[2], mRotationMatrix[5])));
                    builder.setZAxisDirection(mZAxisDirectionHistories
                            .getAverageSensorEvent(DSensor.TYPE_Z_AXIS_DIRECTION));
                    changedSensorTypes |= DSensor.TYPE_Z_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_MINUS_Z_AXIS_DIRECTION) != 0) {
                    mMinusZAxisDirectionHistories.add(new DSensorEvent(DSensor.TYPE_MINUS_Z_AXIS_DIRECTION,
                            mGravity.accuracy, mGravity.timestamp > mMagneticField.timestamp ? mGravity.timestamp
                            : mMagneticField.timestamp, (float) Math.atan2(-mRotationMatrix[2], -mRotationMatrix[5])));
                    builder.setMinusZAxisDirection(mMinusZAxisDirectionHistories
                            .getAverageSensorEvent(DSensor.TYPE_MINUS_Z_AXIS_DIRECTION));
                    changedSensorTypes |= DSensor.TYPE_MINUS_Z_AXIS_DIRECTION;
                }

                if ((mDSensorTypes & DSensor.TYPE_DEVICE_ROTATION) != 0) {
                    builder.setDeviceRotation(new DSensorEvent(DSensor.TYPE_DEVICE_ROTATION,
                            mGravity.accuracy, mGravity.timestamp,
                            (float) Math.atan2(mRotationMatrix[6], mRotationMatrix[7])));
                    changedSensorTypes |= DSensor.TYPE_DEVICE_ROTATION;
                }
            }
        }
        return changedSensorTypes;
    }

    private int calculateGravity(DProcessedSensorEvent.DProcessedSensorEventBuilder builder)
    {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "calculateGravity");
        if (mGravity != null) {
            if (mGravity.timestamp == 0) {
                System.arraycopy(mAccelerometer.values, 0, mGravity.values, 0, 3);
            } else {
                for (int i = 0; i < 3; i++) {
                    mGravity.values[i] = ALPHA * mAccelerometer.values[i] + ONE_MINUS_ALPHA * mGravity.values[i];
                }
            }
        }

        if ((mDSensorTypes & DSensor.TYPE_DEVICE_GRAVITY) != 0) {
            builder.setGravityInDeviceBasis(new DSensorEvent(DSensor.TYPE_DEVICE_GRAVITY,
                    mAccelerometer.accuracy, mAccelerometer.timestamp, mGravity.values));
            return DSensor.TYPE_DEVICE_GRAVITY;
        }
        return 0;
    }

    private int calculateLinearAcceleration(DProcessedSensorEvent.DProcessedSensorEventBuilder builder) {
        Logger.d(DSensorEventProcessor.class.getSimpleName(), "calculateLinearAcceleration");
        float[] linearAcceleration = new float[3];
        for (int i = 0; i < 3; i++) {
            linearAcceleration[i] = mAccelerometer.values[i] - mGravity.values[i];
        }
        if (mLinearAccelerationInWorldBasisHistories != null) {
            mLinearAcceleration.accuracy = mAccelerometer.accuracy;
            mLinearAcceleration.timestamp = mAccelerometer.timestamp;
            System.arraycopy(linearAcceleration, 0, mLinearAcceleration.values, 0, 3);
        }
        if ((mDSensorTypes & DSensor.TYPE_DEVICE_LINEAR_ACCELERATION) != 0) {
            builder.setLinearAccelerationInDeviceBasis(new DSensorEvent(DSensor.TYPE_DEVICE_LINEAR_ACCELERATION,
                    mAccelerometer.accuracy, mAccelerometer.timestamp, linearAcceleration));
            return DSensor.TYPE_DEVICE_LINEAR_ACCELERATION;
        }
        return 0;
    }

    private static class DirectionHistory {
        static int mHistoryMaxSize;
        final LinkedList<DSensorEvent> mHistories = new LinkedList<>();
        final float[] mHistoriesSum = new float[]{0.0f, 0.0f};
        long mHistoryTimeStampSum;

        public DirectionHistory(int historyMaxSize) {
            mHistoryMaxSize = historyMaxSize;
        }

        public void clearHistories() {
            Logger.d(DirectionHistory.class.getSimpleName(), "clearHistories");
                    mHistories.clear();
            mHistoriesSum[0] = 0.0f;
            mHistoriesSum[1] = 0.0f;
        }

        public void add(DSensorEvent item) {
            Logger.d(DirectionHistory.class.getSimpleName(), "add size = " + mHistories.size()
                    + " angle = " + Math.round(Math.toDegrees(item.values[0])));
            mHistoryTimeStampSum += item.timestamp;
            if (mHistories.size() == mHistoryMaxSize) {
                DSensorEvent firstTerm = mHistories.removeFirst();
                mHistoryTimeStampSum -= firstTerm.timestamp;
                DMath.removeAngle(firstTerm.values[0], mHistoriesSum);
            }
            mHistories.addLast(item);
            DMath.addAngle(item.values[0], mHistoriesSum);
        }

        public DSensorEvent getAverageSensorEvent(int sensorType) {
            return new DSensorEvent(sensorType, mHistories.getFirst().accuracy,
                    ((long) (1.0f / mHistories.size()) * mHistoryTimeStampSum),
                    mHistories.isEmpty() ? Float.NaN : DMath.averageAngle(mHistoriesSum, mHistories.size()));
        }
    }

    private static class WorldHistory {
        static int mHistoryMaxSize;
        final LinkedList<DSensorEvent> mHistories = new LinkedList<>();
        final float[] mHistoriesValuesSum;
        long mHistoryTimeStampSum;

        public WorldHistory(int historyMaxSize, int historyValuesSumLength) {
            mHistoryMaxSize = historyMaxSize;
            mHistoriesValuesSum = new float[historyValuesSumLength];
        }

        public void add(DSensorEvent itemInDeviceBasis, float[] rotationMatrix) {
            Logger.d(WorldHistory.class.getSimpleName(), "add");
            mHistoryTimeStampSum += itemInDeviceBasis.timestamp;
            DSensorEvent firstTerm = null;
            if (mHistories.size() == mHistoryMaxSize) {
                firstTerm = mHistories.removeFirst();
                mHistoryTimeStampSum -= firstTerm.timestamp;
            }
            float[] itemInWorldBasisValues = DMath.productOfSquareMatrixAndVector(rotationMatrix, itemInDeviceBasis.values);
            for (int i = 0; i < mHistoriesValuesSum.length; i++) {
                mHistoriesValuesSum[i] += itemInWorldBasisValues[i];
                if (firstTerm != null) {
                    mHistoriesValuesSum[i] -= firstTerm.values[i];
                }
            }
            mHistories.addLast(new DSensorEvent(itemInDeviceBasis.sensorType, itemInDeviceBasis.accuracy,
                    itemInDeviceBasis.timestamp, itemInWorldBasisValues));
        }

        public void add(int sensorType, int accuracy, long timestamp, float[] valuesInDeviceBasis, float[] rotationMatrix) {
            Logger.d(WorldHistory.class.getSimpleName(), "add");
            mHistoryTimeStampSum += timestamp;
            DSensorEvent firstTerm = null;
            if (mHistories.size() == mHistoryMaxSize) {
                firstTerm = mHistories.removeFirst();
                mHistoryTimeStampSum -= firstTerm.timestamp;
            }
            float[] itemInWorldBasisValues = DMath.productOfSquareMatrixAndVector(rotationMatrix, valuesInDeviceBasis);
            for (int i = 0; i < mHistoriesValuesSum.length; i++) {
                mHistoriesValuesSum[i] += itemInWorldBasisValues[i];
                if (firstTerm != null) {
                    mHistoriesValuesSum[i] -= firstTerm.values[i];
                }
            }
            mHistories.addLast(new DSensorEvent(sensorType, accuracy, timestamp, itemInWorldBasisValues));
        }

        public DSensorEvent getAverageSensorEvent(int sensorType) {
            Logger.d(WorldHistory.class.getSimpleName(), "getAverageSensorEvent");
            return new DSensorEvent(sensorType, mHistories.getFirst().accuracy,
                    ((long) (1.0f / mHistories.size()) * mHistoryTimeStampSum),
                    DMath.scaleVector(mHistoriesValuesSum, (1.0f / mHistories.size())));
        }
    }
}
