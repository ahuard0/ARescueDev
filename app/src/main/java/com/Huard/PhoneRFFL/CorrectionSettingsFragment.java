package com.Huard.PhoneRFFL;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

public class CorrectionSettingsFragment extends Fragment {
    private  CorrectionStorage correctionStorage;
    private CorrectionViewModel correctionViewModel;
    private LinearLayout pnlCorrectionSettings;
    private EditText editTxtOffsetAzimuth;
    private EditText editTxtOffsetElevation;
    private CheckBox chkInvertAzimuth;
    private CheckBox chkInvertElevation;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_correction_settings, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnBack = view.findViewById(R.id.corr_factors_btnBack);
        btnBack.setOnClickListener(v -> onPressBack());

        Button btnSave = view.findViewById(R.id.corr_factors_btnSave);
        btnSave.setOnClickListener(v -> onPressSave());

        pnlCorrectionSettings = view.findViewById(R.id.pnlCorrectionSettings);
        editTxtOffsetAzimuth = view.findViewById(R.id.corr_factors_txtOffsetAzimuth);
        editTxtOffsetElevation = view.findViewById(R.id.corr_factors_txtOffsetElevation);
        chkInvertAzimuth = view.findViewById(R.id.corr_factors_chkInvertAzimuth);
        chkInvertElevation = view.findViewById(R.id.corr_factors_chkInvertElevation);

        correctionViewModel = new ViewModelProvider(requireActivity()).get(CorrectionViewModel.class);

        correctionViewModel.getShowCorrectionSettings().observe(getViewLifecycleOwner(), this::ShowCorrectionSettings);

        correctionStorage = new CorrectionStorage(requireContext());

        LoadSettingsFromStorage();
    }

    @SuppressLint("SetTextI18n")
    private void ShowCorrectionSettings(Boolean show) {
        if (show)
            pnlCorrectionSettings.setVisibility(View.VISIBLE);
        else
            pnlCorrectionSettings.setVisibility(View.INVISIBLE);

        // populate settings: invert axes
        boolean invertAzimuth = Boolean.TRUE.equals(correctionViewModel.invertAxisAOAAzimuth.getValue());
        boolean invertElevation = Boolean.TRUE.equals(correctionViewModel.invertAxisAOAElevation.getValue());
        chkInvertAzimuth.setChecked(invertAzimuth);
        chkInvertElevation.setChecked(invertElevation);

        // populate settings: correction factors
        Double corrAzimuth = correctionViewModel.corrFactorDeltaDecibelsAzimuth.getValue();
        Double corrElevation = correctionViewModel.corrFactorDeltaDecibelsElevation.getValue();

        if (corrAzimuth != null)
            editTxtOffsetAzimuth.setText(corrAzimuth.toString());
        if (corrElevation != null)
            editTxtOffsetElevation.setText(corrElevation.toString());
    }

    private void onPressBack() {
        GoBack();
    }

    private void onPressSave() {
        SaveSettings();
        SaveSettingsToStorage();
        GoBack();
    }

    private void LoadSettingsFromStorage() {
        CorrectionSettings settings = correctionStorage.Load();
        if (settings != null) {
            chkInvertAzimuth.setChecked(settings.invertAzimuth);
            chkInvertAzimuth.setChecked(settings.invertElevation);
            editTxtOffsetAzimuth.setText(String.format(Locale.getDefault(), "%.4f\n", settings.corrAzimuth));
            editTxtOffsetElevation.setText(String.format(Locale.getDefault(), "%.4f\n", settings.corrElevation));

            correctionViewModel.setInvertAxisAOAAzimuth(settings.invertAzimuth);
            correctionViewModel.setInvertAxisAOAElevation(settings.invertElevation);
            correctionViewModel.setCorrFactorDeltaDecibelsAzimuth(settings.corrAzimuth);
            correctionViewModel.setCorrFactorDeltaDecibelsElevation(settings.corrElevation);
        }
    }

    private void SaveSettings() {
        boolean invertAzimuth = chkInvertAzimuth.isChecked();
        boolean invertElevation = chkInvertElevation.isChecked();
        correctionViewModel.setInvertAxisAOAAzimuth(invertAzimuth);
        correctionViewModel.setInvertAxisAOAElevation(invertElevation);

        String offsetAzimuthStr = String.valueOf(editTxtOffsetAzimuth.getText());
        String offsetElevationStr = String.valueOf(editTxtOffsetElevation.getText());

        double offsetAzimuth;
        double offsetElevation;
        if (!offsetAzimuthStr.isEmpty()) {
            offsetAzimuth = Double.parseDouble(offsetAzimuthStr);
            correctionViewModel.setCorrFactorDeltaDecibelsAzimuth(offsetAzimuth);
        }

        if (!offsetElevationStr.isEmpty()) {
            offsetElevation = Double.parseDouble(offsetElevationStr);
            correctionViewModel.setCorrFactorDeltaDecibelsElevation(offsetElevation);
        }
    }

    private void SaveSettingsToStorage() {
        boolean invertAzimuth = chkInvertAzimuth.isChecked();
        boolean invertElevation = chkInvertElevation.isChecked();

        String offsetAzimuthStr = String.valueOf(editTxtOffsetAzimuth.getText());
        String offsetElevationStr = String.valueOf(editTxtOffsetElevation.getText());

        if (!offsetAzimuthStr.isEmpty() && !offsetElevationStr.isEmpty()) {
            double offsetAzimuth = Double.parseDouble(offsetAzimuthStr);
            double offsetElevation = Double.parseDouble(offsetElevationStr);

            CorrectionSettings settings = new CorrectionSettings();
            settings.corrAzimuth = offsetAzimuth;
            settings.corrElevation = offsetElevation;
            settings.invertAzimuth = invertAzimuth;
            settings.invertElevation = invertElevation;
            correctionStorage.Save(settings);
        }
    }

    private void GoBack() {
        correctionViewModel.setShowCorrectionSettings(false);  // set correction view state
        ShowCorrectionSettings(false);  // hide panel
    }
}