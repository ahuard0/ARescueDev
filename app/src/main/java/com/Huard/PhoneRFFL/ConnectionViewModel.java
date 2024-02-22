package com.Huard.PhoneRFFL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ConnectionViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>();
    private final MutableLiveData<String> connectionStatus = new MutableLiveData<>();
    private final MutableLiveData<String> terminalMsg = new MutableLiveData<>();

    public void setIsConnected(Boolean value) {
        isConnected.postValue(value);
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public void setConnectionStatusMsg(String value) {
        connectionStatus.postValue(value);
    }

    /** @noinspection unused*/
    public LiveData<String> getConnectionStatusMsg() {
        return connectionStatus;
    }

    public void setTerminalMsg(String value) {
        terminalMsg.postValue(value);
    }

    public LiveData<String> getTerminalMsg() {
        return terminalMsg;
    }
}
