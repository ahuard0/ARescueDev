package com.Huard.PhoneRFFL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Queue;

public class SolutionViewModel extends ViewModel {
    private final MutableLiveData<Double> centroidAOAAzi = new MutableLiveData<>();
    private final MutableLiveData<Double> centroidAOAEl = new MutableLiveData<>();
    private final MutableLiveData<Queue<Double>> azimuthPointsAOA = new MutableLiveData<>();
    private final MutableLiveData<Queue<Double>> elevationPointsAOA = new MutableLiveData<>();

    public void setCentroidAOAAzimuth(Double value) {
        centroidAOAAzi.postValue(value);
    }

    public LiveData<Double> getCentroidAOAAzimuth() {
        return centroidAOAAzi;
    }
    public void setCentroidAOAElevation(Double value) {
        centroidAOAEl.postValue(value);
    }

    public LiveData<Double> getCentroidAOAElevation() {
        return centroidAOAEl;
    }

    public void setAzimuthPointsAOA(Queue<Double> values) {
        azimuthPointsAOA.postValue(values);
    }

    public LiveData<Queue<Double>> getAzimuthPointsAOA() {
        return azimuthPointsAOA;
    }

    public void setElevationPointsAOA(Queue<Double> values) {
        elevationPointsAOA.postValue(values);
    }

    public LiveData<Queue<Double>> getElevationPointsAOA() {
        return elevationPointsAOA;
    }
}
