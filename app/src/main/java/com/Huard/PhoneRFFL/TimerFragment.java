package com.Huard.PhoneRFFL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TimerFragment extends Fragment {

    private TimerManager timerManager;
    public TextView lblTimerStatus;
    public Button btnStartStop;
    public Button btnReset;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);  // Required: Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnReset = view.findViewById(R.id.resetButton);
        btnStartStop = view.findViewById(R.id.startStopButton);
        lblTimerStatus = view.findViewById(R.id.timerText);

        btnReset.setOnClickListener(v -> onPressReset());
        btnStartStop.setOnClickListener(v -> onPressStartStop());

        timerManager = new TimerManager(this);
    }

    private void onPressReset() {
        timerManager.reset();
    }

    private void onPressStartStop() {
        timerManager.startStop();
    }
}