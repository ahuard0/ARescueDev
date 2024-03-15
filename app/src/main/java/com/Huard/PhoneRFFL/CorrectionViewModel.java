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
    public final MutableLiveData<Boolean> enableParallaxCorrection = new MutableLiveData<>(false);
    public final MutableLiveData<Double> estimatedDistanceToEmitter = new MutableLiveData<>(1.0);
    public final MutableLiveData<Double> cameraOffsetX = new MutableLiveData<>(0.0);
    public final MutableLiveData<Double> cameraOffsetY = new MutableLiveData<>(0.0);


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
    
    public void setEnableParallaxCorrection(Boolean value) { enableParallaxCorrection.setValue(value); }

    public LiveData<Boolean> getEnableParallaxCorrection() {
        return enableParallaxCorrection;
    }

    public void setEstimatedDistanceToEmitter(Double value) { estimatedDistanceToEmitter.setValue(value); }

    public LiveData<Double> getEstimatedDistanceToEmitter() { return estimatedDistanceToEmitter; }

    public void setCameraOffsetX(Double value) { cameraOffsetX.setValue(value); }

    public LiveData<Double> getCameraOffsetX() { return cameraOffsetX; }

    public void setCameraOffsetY(Double value) { cameraOffsetY.setValue(value); }

    public LiveData<Double> getCameraOffsetY() { return cameraOffsetY; }
}
