package com.Huard.PhoneRFFL;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Queue;

public class ConnectionViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isConnected = new MutableLiveData<>();
    private final MutableLiveData<String> connectionStatus = new MutableLiveData<>();
    private final MutableLiveData<Queue<String>> terminalMsg = new MutableLiveData<>();

    public void setTerminalMsg(Queue<String> value) {
        terminalMsg.postValue(value);
    }

    public LiveData<Queue<String>> getTerminalMsg() {
        return terminalMsg;
    }

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

}
