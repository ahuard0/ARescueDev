package com.Huard.PhoneRFFL;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class SolutionFragment extends Fragment {

    public TextView lblAzimuth;
    public TextView lblElevation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_solution, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lblAzimuth = view.findViewById(R.id.lblAzimuth);
        lblElevation = view.findViewById(R.id.lblElevation);

        SideViewModel sideViewModel = new ViewModelProvider(requireActivity()).get(SideViewModel.class);
        sideViewModel.getCentroidSelected().observe(getViewLifecycleOwner(), this::updateCentroidMode);
        sideViewModel.getHapticsSelected().observe(getViewLifecycleOwner(), this::updateHapticsMode);

        SolutionViewModel solutionViewModel = new ViewModelProvider(requireActivity()).get(SolutionViewModel.class);
        solutionViewModel.getAzimuth().observe(getViewLifecycleOwner(), this::UpdateCentroidAzimuth);
        solutionViewModel.getElevation().observe(getViewLifecycleOwner(), this::UpdateCentroidElevation);

        ConnectionViewModel connectionViewModel = new ViewModelProvider(requireActivity()).get(ConnectionViewModel.class);
        connectionViewModel.getIsConnected().observe(getViewLifecycleOwner(), this::receiveIsConnected);
        connectionViewModel.getConnectionStatusMsg().observe(getViewLifecycleOwner(), this::receiveConnectionStatusMsg);
        connectionViewModel.getTerminalMsg().observe(getViewLifecycleOwner(), this::receiveConnectionTerminalMsg);
    }

    private void receiveIsConnected(boolean isConnected) {
        // TODO: Implement this
    }

    private void receiveConnectionStatusMsg(String message) {
        // TODO: Implement this
    }

    private void receiveConnectionTerminalMsg(String message) {
        // TODO: Implement this
    }

    private void UpdateCentroidAzimuth(double value) {
        setAzimuthText(value);
    }

    private void UpdateCentroidElevation(double value) {
        setElevationText(value);
    }

    private void updateCentroidMode(boolean isCentroidSelected) {
        if (isCentroidSelected) {
            lblAzimuth.setVisibility(View.VISIBLE);
            lblElevation.setVisibility(View.VISIBLE);
        } else {
            lblAzimuth.setVisibility(View.INVISIBLE);
            lblElevation.setVisibility(View.INVISIBLE);
        }
    }

    private void updateHapticsMode(boolean isHapticsSelected) {
        // TODO: Implement Haptics Cues
    }

    public void setAzimuthText(double value) {
        @SuppressLint("DefaultLocale") String text = String.format("%.1f°<sup><small>Az</small></sup>", value);
        lblAzimuth.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
    }

    public void setElevationText(double value) {
        @SuppressLint("DefaultLocale") String text = String.format("%.1f°<sup><small>El</small></sup>", value);
        lblElevation.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
    }

}