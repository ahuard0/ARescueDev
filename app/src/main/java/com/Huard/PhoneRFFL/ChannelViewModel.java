package com.Huard.PhoneRFFL;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ChannelViewModel extends ViewModel {
    private final MutableLiveData<Pair<Integer, Integer>> channelDataAzimuth = new MutableLiveData<>();
    private final MutableLiveData<Pair<Integer, Integer>> channelDataElevation = new MutableLiveData<>();

    public void setChannelDataAzimuth(Pair<Integer, Integer> value) {
        channelDataAzimuth.postValue(value);
    }

    public LiveData<Pair<Integer, Integer>> getChannelDataAzimuth() {
        return channelDataAzimuth;
    }
    public void setChannelDataElevation(Pair<Integer, Integer> value) {
        channelDataElevation.postValue(value);
    }

    public LiveData<Pair<Integer, Integer>> getChannelDataElevation() {
        return channelDataElevation;
    }
}
