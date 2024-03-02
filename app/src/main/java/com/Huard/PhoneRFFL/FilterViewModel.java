package com.Huard.PhoneRFFL;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Queue;

public class FilterViewModel extends ViewModel {
    private final MutableLiveData<Queue<Pair<Short, Short>>> azimuth = new MutableLiveData<>();
    private final MutableLiveData<Queue<Pair<Short, Short>>> elevation = new MutableLiveData<>();

    public void setAzimuth(Queue<Pair<Short, Short>> value) {
        azimuth.postValue(value);
    }

    public LiveData<Queue<Pair<Short, Short>>> getAzimuth() {
        return azimuth;
    }

    public void setElevation(Queue<Pair<Short, Short>> value) {
        elevation.postValue(value);
    }

    public LiveData<Queue<Pair<Short, Short>>> getElevation() {
        return elevation;
    }
}
