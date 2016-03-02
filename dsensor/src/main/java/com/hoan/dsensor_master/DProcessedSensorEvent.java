package com.hoan.dsensor_master;

/**
 * Processed sensors' values
 * Created by Hoan on 2/19/2016.
 */
public class DProcessedSensorEvent {
    public final DSensorEvent accelerometerInDeviceBasis;
    public final DSensorEvent accelerometerInWorldBasis;
    public final DSensorEvent gravityInDeviceBasis;
    public final DSensorEvent gravityInWorldBasis;
    public final DSensorEvent magneticFieldInDeviceBasis;
    public final DSensorEvent magneticFieldInWorldBasis;
    public final DSensorEvent linearAccelerationInDeviceBasis;
    public final DSensorEvent linearAccelerationInWorldBasis;
    public final DSensorEvent gyroscope;
    public final DSensorEvent rotationVector;
    public final DSensorEvent depreciatedOrientation;
    public final DSensorEvent inclination;
    public final DSensorEvent deviceRotation;
    public final DSensorEvent pitch;
    public final DSensorEvent roll;
    public final DSensorEvent xAxisDirection;
    public final DSensorEvent minusXAxisDirection;
    public final DSensorEvent yAxisDirection;
    public final DSensorEvent minusYAxisDirection;
    public final DSensorEvent zAxisDirection;
    public final DSensorEvent minusZAxisDirection;

    public static class DProcessedSensorEventBuilder {
        private DSensorEvent mAccelerometerInDeviceBasis;
        private DSensorEvent mAccelerometerInWorldBasis;
        private DSensorEvent mGravityInDeviceBasis;
        private DSensorEvent mGravityInWorldBasis;
        private DSensorEvent mMagneticFieldInDeviceBasis;
        private DSensorEvent mMagneticFieldInWorldBasis;
        private DSensorEvent mLinearAccelerationInDeviceBasis;
        private DSensorEvent mLinearAccelerationInWorldBasis;
        private DSensorEvent mGyroscope;
        private DSensorEvent mRotationVector;
        private DSensorEvent mDepreciatedOrientation;
        private DSensorEvent mInclination;
        private DSensorEvent mDeviceRotation;
        private DSensorEvent mPitch;
        private DSensorEvent mRoll;
        private DSensorEvent mXAxisDirection;
        private DSensorEvent mMinusXAxisDirection;
        private DSensorEvent mYAxisDirection;
        private DSensorEvent mMinusYAxisDirection;
        private DSensorEvent mZAxisDirection;
        private DSensorEvent mMinusZAxisDirection;

        public DProcessedSensorEventBuilder() {

        }

        public DProcessedSensorEventBuilder setAccelerometerInDeviceBasis(DSensorEvent accelerometerInDeviceBasis) {
            mAccelerometerInDeviceBasis = accelerometerInDeviceBasis;
            return this;
        }

        public DProcessedSensorEventBuilder setAccelerometerInWorldBasis(DSensorEvent accelerometerInWorldBasis) {
            mAccelerometerInWorldBasis = accelerometerInWorldBasis;
            return this;
        }

        public DProcessedSensorEventBuilder setGravityInDeviceBasis(DSensorEvent gravityInDeviceBasis) {
            mGravityInDeviceBasis = gravityInDeviceBasis;
            return this;
        }

        public DProcessedSensorEventBuilder setGravityInWorldBasis(DSensorEvent gravityInWorldBasis) {
            mGravityInWorldBasis = gravityInWorldBasis;
            return this;
        }

        public DProcessedSensorEventBuilder setMagneticFieldInDeviceBasis(DSensorEvent magneticFieldInDeviceBasis) {
            mMagneticFieldInDeviceBasis = magneticFieldInDeviceBasis;
            return this;
        }

        public DProcessedSensorEventBuilder setMagneticFieldInWorldBasis(DSensorEvent magneticFieldInWorldBasis) {
            mMagneticFieldInWorldBasis = magneticFieldInWorldBasis;
            return this;
        }

        public DProcessedSensorEventBuilder setLinearAccelerationInDeviceBasis(DSensorEvent linearAccelerationInDeviceBasis) {
            mLinearAccelerationInDeviceBasis = linearAccelerationInDeviceBasis;
            return this;
        }

        public DProcessedSensorEventBuilder setLinearAccelerationInWorldBasis(DSensorEvent linearAccelerationInWorldBasis) {
            mLinearAccelerationInWorldBasis = linearAccelerationInWorldBasis;
            return this;
        }

        public DProcessedSensorEventBuilder setGyroscope(DSensorEvent gyroscope) {
            mGyroscope = gyroscope;
            return this;
        }

        public DProcessedSensorEventBuilder setRotationVector(DSensorEvent rotationVector) {
            mRotationVector = rotationVector;
            return this;
        }

        public DProcessedSensorEventBuilder setDepreciatedOrientation(DSensorEvent depreciatedOrientation) {
            mDepreciatedOrientation = depreciatedOrientation;
            return this;
        }

        public DProcessedSensorEventBuilder setInclination(DSensorEvent inclination) {
            mInclination = inclination;
            return this;
        }

        public DProcessedSensorEventBuilder setDeviceRotation(DSensorEvent deviceRotation) {
            mDeviceRotation = deviceRotation;
            return this;
        }

        public DProcessedSensorEventBuilder setPitch(DSensorEvent pitch) {
            mPitch = pitch;
            return this;
        }

        public DProcessedSensorEventBuilder setRoll(DSensorEvent roll) {
            mRoll = roll;
            return this;
        }

        public DProcessedSensorEventBuilder setXAxisDirection(DSensorEvent xAxisDirection) {
            this.mXAxisDirection = xAxisDirection;
            return this;
        }

        public DProcessedSensorEventBuilder setMinusXAxisDirection(DSensorEvent minusXAxisDirection) {
            this.mMinusXAxisDirection = minusXAxisDirection;
            return this;
        }

        public DProcessedSensorEventBuilder setYAxisDirection(DSensorEvent yAxisDirection) {
            mYAxisDirection = yAxisDirection;
            return this;
        }

        public DProcessedSensorEventBuilder setMinusYAxisDirection(DSensorEvent minusYAxisDirection) {
            this.mMinusYAxisDirection = minusYAxisDirection;
            return this;
        }

        public DProcessedSensorEventBuilder setZAxisDirection(DSensorEvent zAxisDirection) {
            mZAxisDirection = zAxisDirection;
            return this;
        }

        public DProcessedSensorEventBuilder setMinusZAxisDirection(DSensorEvent minusZAxisDirection) {
            mMinusZAxisDirection = minusZAxisDirection;
            return this;
        }

        public DProcessedSensorEvent build() {
           return new DProcessedSensorEvent(this);
        }
    }

    private DProcessedSensorEvent(DProcessedSensorEventBuilder builder) {
        this.accelerometerInDeviceBasis = builder.mAccelerometerInDeviceBasis;
        this.accelerometerInWorldBasis = builder.mAccelerometerInWorldBasis;
        this.gravityInDeviceBasis = builder.mGravityInDeviceBasis;
        this.gravityInWorldBasis = builder.mGravityInWorldBasis;
        this.magneticFieldInDeviceBasis = builder.mMagneticFieldInDeviceBasis;
        this.magneticFieldInWorldBasis = builder.mMagneticFieldInWorldBasis;
        this.linearAccelerationInDeviceBasis = builder.mLinearAccelerationInDeviceBasis;
        this.linearAccelerationInWorldBasis = builder.mLinearAccelerationInWorldBasis;
        this.gyroscope = builder.mGyroscope;
        this.rotationVector = builder.mRotationVector;
        this.depreciatedOrientation = builder.mDepreciatedOrientation;
        this.inclination = builder.mInclination;
        this.deviceRotation = builder.mDeviceRotation;
        this.pitch = builder.mPitch;
        this.roll = builder.mRoll;
        this.xAxisDirection = builder.mXAxisDirection;
        this.minusXAxisDirection = builder.mMinusXAxisDirection;
        this.yAxisDirection = builder.mYAxisDirection;
        this.minusYAxisDirection = builder.mMinusYAxisDirection;
        this.zAxisDirection = builder.mZAxisDirection;
        this.minusZAxisDirection = builder.mMinusZAxisDirection;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (this.inclination != null) {
            sb.append(this.inclination.toString());
        }

        if (this.pitch != null) {
            sb.append(this.pitch.toString());
        }

        if (this.roll != null) {
            sb.append(this.roll.toString());
        }

        if (this.deviceRotation != null) {
            sb.append(this.deviceRotation.toString());
        }

        if (this.xAxisDirection != null) {
            sb.append(this.xAxisDirection.toString());
        }

        if (this.minusXAxisDirection != null) {
            sb.append(this.minusXAxisDirection.toString());
        }

        if (this.yAxisDirection != null) {
            sb.append(this.yAxisDirection.toString());
        }

        if (this.minusYAxisDirection != null) {
            sb.append(this.minusYAxisDirection.toString());
        }

        if (this.zAxisDirection != null) {
            sb.append(this.zAxisDirection.toString());
        }

        if (this.minusZAxisDirection != null) {
            sb.append(this.minusZAxisDirection.toString());
        }

        if (this.depreciatedOrientation != null) {
            sb.append(this.depreciatedOrientation.toString());
        }

        if (this.accelerometerInDeviceBasis != null) {
            sb.append(this.accelerometerInDeviceBasis.toString());
        }

        if (this.accelerometerInWorldBasis != null) {
            sb.append(this.accelerometerInWorldBasis.toString());
        }

        if (this.gravityInDeviceBasis != null) {
            sb.append(this.gravityInDeviceBasis.toString());
        }

        if (this.gravityInWorldBasis != null) {
            sb.append(this.gravityInWorldBasis.toString());
        }

        if (this.magneticFieldInDeviceBasis != null) {
            sb.append(this.magneticFieldInDeviceBasis.toString());
        }

        if (this.magneticFieldInWorldBasis != null) {
            sb.append(this.magneticFieldInWorldBasis.toString());
        }

        if (this.linearAccelerationInDeviceBasis != null) {
            sb.append(this.linearAccelerationInDeviceBasis.toString());
        }

        if (this.linearAccelerationInWorldBasis != null) {
            sb.append(this.linearAccelerationInWorldBasis.toString());
        }

        if (this.gyroscope != null) {
            sb.append(this.gyroscope.toString());
        }

        if (this.rotationVector != null) {
            sb.append(this.rotationVector.toString());
        }

        return sb.toString();
    }
}
