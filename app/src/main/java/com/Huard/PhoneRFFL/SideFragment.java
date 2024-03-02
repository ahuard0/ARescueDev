package com.Huard.PhoneRFFL;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class SideFragment extends Fragment {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkFilter;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkHaptics;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkEllipse;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkShowCentroidAOA;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkShowWidgets;
    private SideViewModel sideViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_side, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chkShowWidgets = view.findViewById(R.id.chkShowWidgets);
        chkShowCentroidAOA = view.findViewById(R.id.chkShowAOA);
        chkEllipse = view.findViewById(R.id.chkEllipse);
        chkHaptics = view.findViewById(R.id.chkHaptics);
        chkFilter = view.findViewById(R.id.chkFilter);

        chkShowWidgets.setOnClickListener(v -> onPressChkShowWidgets());
        chkShowCentroidAOA.setOnClickListener(v -> onPressChkShowCentroidAOA());
        chkEllipse.setOnClickListener(v -> onPressChkEllipse());
        chkHaptics.setOnClickListener(v -> onPressChkHaptics());
        chkFilter.setOnClickListener(v -> onPressChkFilter());

        sideViewModel = new ViewModelProvider(requireActivity()).get(SideViewModel.class);
    }

    private void onPressChkFilter() {
        sideViewModel.setFilterSelected(chkFilter.isChecked());
    }

    private void onPressChkShowWidgets() {
        sideViewModel.setWidgetsSelected(chkShowWidgets.isChecked());
    }

    private void onPressChkShowCentroidAOA() {
        sideViewModel.setCentroidAOASelected(chkShowCentroidAOA.isChecked());
    }

    private void onPressChkEllipse() {
        sideViewModel.setEllipseSelected(chkEllipse.isChecked());
    }

    private void onPressChkHaptics() {
        sideViewModel.setIsHapticsSelected(chkHaptics.isChecked());
    }
}