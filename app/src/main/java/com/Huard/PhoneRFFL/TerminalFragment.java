package com.Huard.PhoneRFFL;

import android.os.Bundle;
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

public class TerminalFragment extends Fragment {
    private ChannelViewModel channelViewModel;
    private TextView lblTerminal;

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

        channelViewModel = new ViewModelProvider(requireActivity()).get(ChannelViewModel.class);
    }

    private void receiveTerminalMessage(String message) {
        Triplet<ArrayList<Integer>, TerminalManager.MeasurementType, Integer> data = TerminalManager.parseMessage(message);
        if (data == null) {
            setTerminalStatus("No Data");
        } else {
            setTerminalStatus(message);
            if (data.second == TerminalManager.MeasurementType.AZIMUTH)
                channelViewModel.setChannelDataAzimuth(new Pair<>(data.first.get(0), data.first.get(1)));
            else if (data.second == TerminalManager.MeasurementType.ELEVATION)
                channelViewModel.setChannelDataElevation(new Pair<>(data.first.get(0), data.first.get(1)));
        }
    }

    private void setTerminalStatus(String msg) {
        lblTerminal.setText(msg);
    }
}