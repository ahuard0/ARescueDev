package com.Huard.PhoneRFFL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class SideFragment extends Fragment {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkHaptics;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkHeatmap;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkShowCentroid;
    private SideViewModel sideViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_side, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chkShowCentroid = view.findViewById(R.id.chkShowCentroid);
        chkHeatmap = view.findViewById(R.id.chkHeatmap);
        chkHaptics = view.findViewById(R.id.chkHaptics);

        chkShowCentroid.setOnClickListener(v -> onPressChkShowCentroid());
        chkHeatmap.setOnClickListener(v -> onPressChkHeatmap());
        chkHaptics.setOnClickListener(v -> onPressChkHaptics());

        sideViewModel = new ViewModelProvider(requireActivity()).get(SideViewModel.class);
    }

    private void onPressChkShowCentroid() {
        sideViewModel.setCentroidSelected(chkShowCentroid.isChecked());
    }

    private void onPressChkHeatmap() {
        sideViewModel.setHeatMapSelected(chkHeatmap.isChecked());
    }

    private void onPressChkHaptics() {
        sideViewModel.setIsHapticsSelected(chkHaptics.isChecked());
    }
}