package com.hoan.dsensor_master.utils;


import com.hoan.dsensor_master.DSensorEvent;

import java.util.LinkedList;

/**
 * Math utility class
 * Created by Hoan on 1/31/2016.
 */
public class DMath {
    private DMath() {

    }

    public static float calculateNorm(float[] vector) {
        float norm = 0;
        for (float coordinate : vector) {
            norm += (float) Math.pow(coordinate, 2);
        }
        return (float) Math.sqrt(norm);
    }

    public static void addAngle(float angle, float[] currentSum) {
        currentSum[0] += Math.sin(angle);
        currentSum[1] += Math.cos(angle);
    }

    public static void removeAngle(float angle, float[] currentSum) {
        currentSum[0] -= Math.sin(angle);
        currentSum[1] -= Math.cos(angle);
    }

    public static float averageAngle(float[] currentSum, int totalTerms) {
        return (float) Math.atan2(currentSum[0] / totalTerms, currentSum[1] / totalTerms);
    }

    public static float[] scaleVector(float[] vector, float scaleFactor) {
        //Logger.d(DMath.class.getSimpleName(), "scaleVector(" + vector[0] + ", " + vector[1] + ", " + vector[2] + ", " + scaleFactor + ")");
        float[] result = new float[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = scaleFactor * vector[i];
        }
        return result;
    }

    public static float[] productOfSquareMatrixAndVector(float[] matrix, float[] vector) {
        //Logger.d(DMath.class.getSimpleName(), "productOfSquareMatrixAndVector: vector = ("
        //        + vector[0] + ", " + vector[1] + ", " + vector[2] + ")");
        if (matrix == null || vector == null || matrix.length != Math.pow(vector.length, 2)) {
            return null;
        }
        int numberOfColumns = vector.length;
        float[] result = new float[numberOfColumns];
        int k = 0;
        for (int i = 0; i < numberOfColumns; i++) {
            result[i] = 0;
            for (int j = 0; j < numberOfColumns; j++) {
                result[i] += matrix[k++] * vector[j];
            }
        }
        return result;
    }
}
