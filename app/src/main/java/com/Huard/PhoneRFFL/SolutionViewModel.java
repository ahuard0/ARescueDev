package com.Huard.PhoneRFFL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SolutionViewModel extends ViewModel {
    private final MutableLiveData<Double> azi = new MutableLiveData<>();
    private final MutableLiveData<Double> el = new MutableLiveData<>();

    public void setAzimuth(Double value) {
        azi.postValue(value);
    }

    public LiveData<Double> getAzimuth() {
        return azi;
    }
    public void setElevation(Double value) {
        el.postValue(value);
    }

    public LiveData<Double> getElevation() {
        return el;
    }
}
