package com.Huard.PhoneRFFL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SideViewModel extends ViewModel {
    private final MutableLiveData<Boolean> centroidSelected = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hapticsSelected = new MutableLiveData<>();
    private final MutableLiveData<Boolean> heatMapSelected = new MutableLiveData<>();

    public void setCentroidSelected(Boolean value) {
        centroidSelected.setValue(value);
    }

    public LiveData<Boolean> getCentroidSelected() {
        return centroidSelected;
    }
    public void setIsHapticsSelected(Boolean value) {
        hapticsSelected.setValue(value);
    }

    public LiveData<Boolean> getHapticsSelected() {
        return hapticsSelected;
    }
    public void setHeatMapSelected(Boolean value) {
        heatMapSelected.setValue(value);
    }

    public LiveData<Boolean> getHeatMapSelected() {
        return heatMapSelected;
    }
}
