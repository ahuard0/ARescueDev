package com.Huard.PhoneRFFL;

import android.util.Log;
import android.util.Pair;

import java.util.LinkedList;
import java.util.Queue;

public class FilterManager {
    private static final int FILTER_BUFFER_SIZE = 5000;
    private static final double FILTER_SIGMA_THRESHOLD = 3;
    private final short[][] azimuthBuffer = new short[FILTER_BUFFER_SIZE][2];
    private final short[][] elevationBuffer = new short[FILTER_BUFFER_SIZE][2];
    private int azimuthBufferIndex = 0;
    private int elevationBufferIndex = 0;
    private int azimuthBufferCount = 0;
    private int elevationBufferCount = 0;
    private byte tempBufferCountAzimuth = 0;
    private byte tempBufferCountElevation = 0;

    public void Reset() {
        azimuthBufferIndex = 0;
        elevationBufferIndex = 0;
        azimuthBufferCount = 0;
        elevationBufferCount = 0;
        tempBufferCountAzimuth = 0;
        tempBufferCountElevation = 0;
    }

    Queue<Pair<Short, Short>> filterAzimuth(Queue<Pair<Short, Short>> pairQueue) {
        if (pairQueue.isEmpty())
            return pairQueue;

        if (tempBufferCountAzimuth++<30)  // don't save the first 30 samples
            return pairQueue;

        // Update buffer with new values
        for (Pair<Short, Short> values : pairQueue) {  // rolling buffer
            azimuthBuffer[azimuthBufferIndex][0] = values.first; // CH3
            azimuthBuffer[azimuthBufferIndex][1] = values.second; // CH4
            azimuthBufferIndex = (azimuthBufferIndex + 1) % FILTER_BUFFER_SIZE; // Move to the next position
            azimuthBufferCount++;
        }

        int bufferSize = Math.min(FILTER_BUFFER_SIZE, azimuthBufferCount);  // limit processing to used portion of the buffer

        // Calculate temporary mean
        double[] tempMean = new double[2];
        for (int i = 0; i < 2; i++) { // Iterate over CH3 and CH4
            double sum = 0;
            for (int j = 0; j < bufferSize; j++) {
                sum += azimuthBuffer[j][i];
            }
            tempMean[i] = sum / bufferSize;
        }

        // Set temporary threshold for noise calculation at 3 times the temporary mean
        double[] tempThreshold = new double[2];
        for (int i = 0; i < 2; i++) {
            tempThreshold[i] = FILTER_SIGMA_THRESHOLD * tempMean[i];
        }

        // Calculate the mean and standard deviation of the noise using temporary threshold
        double[] noiseMean = new double[2];
        double[] noiseStdDev = new double[2];
        for (int i = 0; i < 2; i++) { // Iterate over CH3 and CH4
            double noiseSum = 0;
            double noiseSumSqDiff = 0;
            int noiseSampleCount = 0;
            for (int j = 0; j < bufferSize; j++) {
                double value = azimuthBuffer[j][i];
                if (value <= tempThreshold[i]) {
                    noiseSum += value;
                    noiseSumSqDiff += Math.pow(value - tempMean[i], 2);
                    noiseSampleCount++;
                }
            }

            noiseMean[i] = noiseSum / noiseSampleCount;
            noiseStdDev[i] = Math.sqrt(noiseSumSqDiff / noiseSampleCount);
        }

        // Calculate the new threshold based on the mean of noise plus 3 standard deviations
        double[] noiseThreshold = new double[2];
        for (int i = 0; i < 2; i++) {
            noiseThreshold[i] = noiseMean[i] + FILTER_SIGMA_THRESHOLD * noiseStdDev[i];
        }

        // Separate the signal from the noise
        Queue<Pair<Short, Short>> filteredQueue = new LinkedList<>();
        for (Pair<Short, Short> pair : pairQueue) {
            if (pair.first < 150 && pair.second < 150) {
                continue;
            }
            if (pair.first >= noiseThreshold[0] || pair.second >= noiseThreshold[1]) {
                filteredQueue.add(pair);
            }
        }

        // Log and Return
        for (Pair<Short, Short> values : filteredQueue) {
            Log.d("FilterAzimuth", "CH3: " + values.first + ", CH4: " +values.second);
        }
        return filteredQueue;
    }

    Queue<Pair<Short, Short>> filterElevation(Queue<Pair<Short, Short>> pairQueue) {
        if (pairQueue.isEmpty())
            return pairQueue;

        if (tempBufferCountElevation++<30)  // don't save the first 30 samples
            return pairQueue;

        // Update buffer with new values
        for (Pair<Short, Short> values : pairQueue) {  // rolling buffer
            elevationBuffer[elevationBufferIndex][0] = values.first; // CH3
            elevationBuffer[elevationBufferIndex][1] = values.second; // CH4
            elevationBufferIndex = (elevationBufferIndex + 1) % FILTER_BUFFER_SIZE; // Move to the next position
            elevationBufferCount++;
        }

        int bufferSize = Math.min(FILTER_BUFFER_SIZE, elevationBufferCount);  // limit processing to used portion of the buffer

        // Calculate temporary mean
        double[] tempMean = new double[2];
        for (int i = 0; i < 2; i++) { // Iterate over CH3 and CH4
            double sum = 0;
            for (int j = 0; j < bufferSize; j++) {
                sum += elevationBuffer[j][i];
            }
            tempMean[i] = sum / bufferSize;
        }

        // Set temporary threshold for noise calculation at 3 times the temporary mean
        double[] tempThreshold = new double[2];
        for (int i = 0; i < 2; i++) {
            tempThreshold[i] = FILTER_SIGMA_THRESHOLD * tempMean[i];
        }

        // Calculate the mean and standard deviation of the noise using temporary threshold
        double[] noiseMean = new double[2];
        double[] noiseStdDev = new double[2];
        for (int i = 0; i < 2; i++) { // Iterate over CH3 and CH4
            double noiseSum = 0;
            double noiseSumSqDiff = 0;
            int noiseSampleCount = 0;
            for (int j = 0; j < bufferSize; j++) {
                double value = elevationBuffer[j][i];
                if (value <= tempThreshold[i]) {
                    noiseSum += value;
                    noiseSumSqDiff += Math.pow(value - tempMean[i], 2);
                    noiseSampleCount++;
                }
            }

            noiseMean[i] = noiseSum / noiseSampleCount;
            noiseStdDev[i] = Math.sqrt(noiseSumSqDiff / noiseSampleCount);
        }

        // Calculate the new threshold based on the mean of noise plus 2 standard deviations
        double[] noiseThreshold = new double[2];
        for (int i = 0; i < 2; i++) {
            noiseThreshold[i] = noiseMean[i] + FILTER_SIGMA_THRESHOLD * noiseStdDev[i];
        }

        // Separate the signal from the noise
        Queue<Pair<Short, Short>> filteredQueue = new LinkedList<>();
        for (Pair<Short, Short> pair : pairQueue) {
            if (pair.first < 150 && pair.second < 150) {
                continue;
            }
            if (pair.first >= noiseThreshold[0] || pair.second >= noiseThreshold[1]) {
                filteredQueue.add(pair);
            }
        }

        // Log and Return
        for (Pair<Short, Short> values : filteredQueue) {
            Log.d("FilterElevation", "CH1: " + values.first + ", CH2: " +values.second);
        }
        return filteredQueue;
    }

}
