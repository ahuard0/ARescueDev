package com.Huard.PhoneRFFL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CorrectionViewModel extends ViewModel {
    public final MutableLiveData<Boolean> showCorrectionSettings = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> invertAxisAOAAzimuth = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> invertAxisAOAElevation = new MutableLiveData<>(false);
    public final MutableLiveData<Double> corrFactorDeltaDecibelsAzimuth = new MutableLiveData<>(0.0);
    public final MutableLiveData<Double> corrFactorDeltaDecibelsElevation = new MutableLiveData<>(0.0);


    public void setShowCorrectionSettings(Boolean value) {
        showCorrectionSettings.setValue(value);
    }

    public LiveData<Boolean> getShowCorrectionSettings() {
        return showCorrectionSettings;
    }

    public void setInvertAxisAOAAzimuth(Boolean value) {
        invertAxisAOAAzimuth.setValue(value);
    }

    public LiveData<Boolean> getInvertAxisAOAAzimuth() {
        return invertAxisAOAAzimuth;
    }

    public void setInvertAxisAOAElevation(Boolean value) {
        invertAxisAOAElevation.setValue(value);
    }

    public LiveData<Boolean> getInvertAxisAOAElevation() {
        return invertAxisAOAElevation;
    }

    public void setCorrFactorDeltaDecibelsAzimuth(Double value) { corrFactorDeltaDecibelsAzimuth.setValue(value); }

    public LiveData<Double> getCorrFactorDeltaDecibelsAzimuth() { return corrFactorDeltaDecibelsAzimuth; }

    public void setCorrFactorDeltaDecibelsElevation(Double value) { corrFactorDeltaDecibelsElevation.setValue(value); }

    public LiveData<Double> getCorrFactorDeltaDecibelsElevation() { return corrFactorDeltaDecibelsElevation; }
}
