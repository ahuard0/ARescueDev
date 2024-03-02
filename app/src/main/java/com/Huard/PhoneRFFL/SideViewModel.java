package com.Huard.PhoneRFFL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SideViewModel extends ViewModel {
    private final MutableLiveData<Boolean> widgetsSelected = new MutableLiveData<>();
    private final MutableLiveData<Boolean> centroidAOASelected = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hapticsSelected = new MutableLiveData<>();
    private final MutableLiveData<Boolean> ellipseSelected = new MutableLiveData<>();
    private final MutableLiveData<Boolean> filterSelected = new MutableLiveData<>();


    public void setFilterSelected(Boolean value) {
        filterSelected.setValue(value);
    }

    public LiveData<Boolean> getFilterSelected() {
        return filterSelected;
    }

    public void setWidgetsSelected(Boolean value) {
        widgetsSelected.setValue(value);
    }

    public LiveData<Boolean> getWidgetsSelected() {
        return widgetsSelected;
    }

    public void setCentroidAOASelected(Boolean value) {
        centroidAOASelected.setValue(value);
    }

    public LiveData<Boolean> getCentroidAOASelected() {
        return centroidAOASelected;
    }
    public void setIsHapticsSelected(Boolean value) {
        hapticsSelected.setValue(value);
    }

    public LiveData<Boolean> getHapticsSelected() {
        return hapticsSelected;
    }
    public void setEllipseSelected(Boolean value) {
        ellipseSelected.setValue(value);
    }

    public LiveData<Boolean> getEllipseSelected() {
        return ellipseSelected;
    }
}
