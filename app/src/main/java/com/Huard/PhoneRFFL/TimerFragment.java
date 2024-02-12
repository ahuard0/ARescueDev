package com.Huard.PhoneRFFL;

import android.content.Intent;
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

        btnReset = view.findViewById(R.id.btnReset);
        btnStartStop = view.findViewById(R.id.btnStartStop);
        lblTimerStatus = view.findViewById(R.id.lblTimerStatus);

        btnReset.setOnClickListener(v -> onPressReset());
        btnStartStop.setOnClickListener(v -> onPressStartStop());

        TextView lblTimerStatus = view.findViewById(R.id.lblTimerStatus);
        lblTimerStatus.setOnClickListener(v -> onPressBtnGoToPlotter());

        timerManager = new TimerManager(this);
    }

    private void onPressBtnGoToPlotter() {
        Intent intent = new Intent(requireContext(), AndroidSerialMainActivity.class);
        startActivity(intent);
        requireActivity().finish();  // finish the current activity
    }

    private void onPressReset() {
        timerManager.reset();
    }

    private void onPressStartStop() {
        timerManager.startStop();
    }
}