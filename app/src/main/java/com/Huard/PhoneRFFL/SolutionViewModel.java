package com.Huard.PhoneRFFL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SolutionViewModel extends ViewModel {
    private final MutableLiveData<Double> centroidAOAAzi = new MutableLiveData<>();
    private final MutableLiveData<Double> centroidAOAEl = new MutableLiveData<>();
    private final MutableLiveData<Double> azimuthPointAOA = new MutableLiveData<>();
    private final MutableLiveData<Double> elevationPointAOA = new MutableLiveData<>();

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
