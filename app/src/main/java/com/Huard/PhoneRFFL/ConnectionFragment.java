package com.Huard.PhoneRFFL;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.LinkedList;
import java.util.Queue;

public class ConnectionFragment extends Fragment {
    private ConnectionManager connectionManager;
    private ConnectionViewModel connectionViewModel;
    private ImageView connectionStatusIndicator;
    private final Queue<String> messageQueue = new LinkedList<>();
    private static final int BATCH_SIZE = 20;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connection, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        connectionStatusIndicator = view.findViewById(R.id.connection_status_indicator);
        setConnectionStatusIndicator(false);  // initialize status indicator

        connectionViewModel = new ViewModelProvider(requireActivity()).get(ConnectionViewModel.class);

        connectionManager = new ConnectionManager(requireActivity(), statusTerminalHandler, statusConnectionHandler);
        connectionManager.connect();
    }

    private final Handler statusConnectionHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String message = (String) msg.obj;
            Log.d("ConnectionFragment", "handleMessage Message: " + msg);
            connectionViewModel.setConnectionStatusMsg(message);
            if (message != null && message.equals("Connected")) {
                setConnectionStatusIndicator(true);
                connectionViewModel.setIsConnected(true);
            } else {
                setConnectionStatusIndicator(false);
                connectionViewModel.setIsConnected(false);
            }
        }
    };

    private final Handler statusTerminalHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String message = (String) msg.obj;
            //Log.d("ConnectionFragment", "Message: " + message);
            addToMessageQueue(message);  // Add the received message to the message queue
        }
    };

    private synchronized void addToMessageQueue(String message) {
        messageQueue.offer(message); // Add message to the end of the queue

        // Process the message queue if it reaches the batch size
        if (messageQueue.size() >= BATCH_SIZE) {
            processMessageQueue();
        }
    }

    private synchronized void processMessageQueue() {
        connectionViewModel.setTerminalMsg(new LinkedList<>(messageQueue));  // Pass the message queue to the ViewModel
        messageQueue.clear();
    }

    private void setConnectionStatusIndicator(boolean isConnected) {
        if (isConnected) {
            connectionStatusIndicator.setBackgroundResource(R.drawable.connection_status_circle_green);
        } else {
            connectionStatusIndicator.setBackgroundResource(R.drawable.connection_status_circle_red);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        connectionManager.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        connectionManager.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        connectionManager.disconnect();
    }
}