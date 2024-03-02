package com.Huard.PhoneRFFL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoggingViewModel extends ViewModel {
    private final MutableLiveData<Boolean> logExportSelected = new MutableLiveData<>();
    public void setLogExportSelected(Boolean value) {
        logExportSelected.setValue(value);
    }
    public LiveData<Boolean> getLogExportSelected() {
        return logExportSelected;
    }
}
