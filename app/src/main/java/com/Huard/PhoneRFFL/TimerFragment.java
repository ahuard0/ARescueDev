package com.Huard.PhoneRFFL;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class TimerFragment extends Fragment {

    private TimerManager timerManager;
    public TextView lblTimerStatus;
    public Button btnStartStop;
    public Button btnReset;
    private FrameLayout frameTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);  // Required: Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        frameTimer = view.findViewById(R.id.frameTimer);
        btnReset = view.findViewById(R.id.btnReset);
        btnStartStop = view.findViewById(R.id.btnStartStop);
        lblTimerStatus = view.findViewById(R.id.lblTimerStatus);

        btnReset.setOnClickListener(v -> onPressReset());
        btnStartStop.setOnClickListener(v -> onPressStartStop());

        TextView lblTimerStatus = view.findViewById(R.id.lblTimerStatus);
        lblTimerStatus.setOnClickListener(v -> onPressBtnGoToPlotter());

        SideViewModel sideViewModel = new ViewModelProvider(requireActivity()).get(SideViewModel.class);
        sideViewModel.getWidgetsSelected().observe(getViewLifecycleOwner(), this::onPressChkWidgets);

        timerManager = new TimerManager(this);
    }

    private void onPressChkWidgets(boolean isWidgetsSelected) {
        if (isWidgetsSelected)
            frameTimer.setVisibility(View.VISIBLE);
        else
            frameTimer.setVisibility(View.INVISIBLE);
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