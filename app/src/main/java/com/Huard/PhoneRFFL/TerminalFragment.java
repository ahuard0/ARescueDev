package com.Huard.PhoneRFFL;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TerminalFragment extends Fragment {
    private FilterViewModel filterViewModel;
    private TextView lblTerminal;
    private final Queue<Pair<Short, Short>> dataQueueAzimuth = new LinkedList<>();
    private final Queue<Pair<Short, Short>> dataQueueElevation = new LinkedList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_terminal, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lblTerminal = view.findViewById(R.id.lblTerminal);

        ConnectionViewModel connectionViewModel = new ViewModelProvider(requireActivity()).get(ConnectionViewModel.class);
        connectionViewModel.getTerminalMsg().observe(getViewLifecycleOwner(), this::receiveTerminalMessage);

        filterViewModel = new ViewModelProvider(requireActivity()).get(FilterViewModel.class);
    }

    private void receiveTerminalMessage(Queue<String> messageQueue) {
        if (messageQueue.isEmpty())
            return;

        dataQueueAzimuth.clear();
        dataQueueElevation.clear();

        for (String message : messageQueue) {
            Log.d("TerminalFragment", "Message Received: " + message);
            Triplet<ArrayList<Short>, TerminalManager.MeasurementType, Integer> data = TerminalManager.parseMessage(message);

            if (data == null) {
                setTerminalStatus("No Data");
            } else {
                setTerminalStatus("");
                if (data.second == TerminalManager.MeasurementType.AZIMUTH)
                    dataQueueAzimuth.offer(new Pair<>(data.first.get(0), data.first.get(1)));  // data.first : [chA,chB] azimuth
                else if (data.second == TerminalManager.MeasurementType.ELEVATION)
                    dataQueueElevation.offer(new Pair<>(data.first.get(0), data.first.get(1)));  // data.first : [chA,chB] elevation
            }
        }

        if (!dataQueueAzimuth.isEmpty())
            filterViewModel.setAzimuth(new LinkedList<>(dataQueueAzimuth));

        if (!dataQueueElevation.isEmpty())
            filterViewModel.setElevation(new LinkedList<>(dataQueueElevation));
    }

    private void setTerminalStatus(String msg) {
        if (msg == null)
            return;
        if (lblTerminal == null)
            return;

        lblTerminal.setText(msg);
    }
}