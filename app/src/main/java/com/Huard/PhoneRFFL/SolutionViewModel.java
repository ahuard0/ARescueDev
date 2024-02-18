package com.Huard.PhoneRFFL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SolutionViewModel extends ViewModel {
    private final MutableLiveData<Double> centroidAzi = new MutableLiveData<>();
    private final MutableLiveData<Double> centroidEl = new MutableLiveData<>();
    private final MutableLiveData<Double> centroidX = new MutableLiveData<>();
    private final MutableLiveData<Double> centroidY = new MutableLiveData<>();
    private final MutableLiveData<Double> azimuthPointAOA = new MutableLiveData<>();
    private final MutableLiveData<Double> elevationPointAOA = new MutableLiveData<>();

    public void setCentroidX(Double value) {
        centroidX.postValue(value);
    }

    public LiveData<Double> getCentroidX() {
        return centroidX;
    }
    public void setCentroidY(Double value) {
        centroidY.postValue(value);
    }

    public LiveData<Double> getCentroidY() {
        return centroidY;
    }

    public void setCentroidAzimuth(Double value) {
        centroidAzi.postValue(value);
    }

    public LiveData<Double> getCentroidAzimuth() {
        return centroidAzi;
    }
    public void setCentroidElevation(Double value) {
        centroidEl.postValue(value);
    }

    public LiveData<Double> getCentroidElevation() {
        return centroidEl;
    }

    public void setAzimuthPointAOA(Double value) {
        azimuthPointAOA.postValue(value);
    }

    public LiveData<Double> getAzimuthPointAOA() {
        return azimuthPointAOA;
    }

    public void setElevationPointAOA(Double value) {
        elevationPointAOA.postValue(value);
    }

    public LiveData<Double> getElevationPointAOA() {
        return elevationPointAOA;
    }
}
