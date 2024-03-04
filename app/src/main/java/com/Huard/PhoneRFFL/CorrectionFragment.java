package com.Huard.PhoneRFFL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class CorrectionFragment extends Fragment {
    private CorrectionViewModel correctionViewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_correction, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView lblCorr = view.findViewById(R.id.lblCorr);
        lblCorr.setOnClickListener(v -> onPressCorr());

        correctionViewModel = new ViewModelProvider(requireActivity()).get(CorrectionViewModel.class);
    }

    private void onPressCorr() {
        ShowSettingsFragment();
    }

    private void ShowSettingsFragment() {
        correctionViewModel.setShowCorrectionSettings(true);  // CorrectionSettingsFragment subscribes to this view model variable and will show itself if set to true
    }
}