package com.Huard.PhoneRFFL;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class SolutionFragment extends Fragment {

    private TextView lblAzimuth;
    private TextView lblElevation;
    private SolutionViewModel solutionViewModel;

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
        sideViewModel.getCentroidAOASelected().observe(getViewLifecycleOwner(), this::receiveShowAOAMode);
        sideViewModel.getHapticsSelected().observe(getViewLifecycleOwner(), this::receiveHapticsMode);

        solutionViewModel = new ViewModelProvider(requireActivity()).get(SolutionViewModel.class);
        solutionViewModel.getCentroidAOAAzimuth().observe(getViewLifecycleOwner(), this::receiveCentroidAOAAzimuth);
        solutionViewModel.getCentroidAOAElevation().observe(getViewLifecycleOwner(), this::receiveCentroidAOAElevation);

        ChannelViewModel channelViewModel = new ViewModelProvider(requireActivity()).get(ChannelViewModel.class);
        channelViewModel.getChannelDataAzimuth().observe(getViewLifecycleOwner(), this::receiveAzimuthADC);
        channelViewModel.getChannelDataElevation().observe(getViewLifecycleOwner(), this::receiveElevationADC);
    }

    private void receiveAzimuthADC(Pair<Integer, Integer> values) {
        double Power_CH3 = convert_ADC_to_dBm(values.first,  3);
        double Power_CH4 = convert_ADC_to_dBm(values.second, 4);
        double delta_dB = Power_CH3 - Power_CH4;
        double AOA_deg = convert_delta_dB_to_AOA(delta_dB);
        solutionViewModel.setAzimuthPointAOA(AOA_deg);
    }

    private void receiveElevationADC(Pair<Integer, Integer> values) {
        double Power_CH1 = convert_ADC_to_dBm(values.first,  1);
        double Power_CH2 = convert_ADC_to_dBm(values.second, 2);
        double delta_dB = Power_CH1 - Power_CH2;
        double AOA_deg = convert_delta_dB_to_AOA(delta_dB);
        solutionViewModel.setElevationPointAOA(AOA_deg);
    }

    private double convert_delta_dB_to_AOA(double delta_dB) {
        // dbm12diff = -0.1462 * AOAResult + 0.0000
        // dbm34diff = -0.1462 * AOAResult + 0.0000
        return delta_dB/(-0.1462);
    }

    private double convert_ADC_to_dBm(int ADC_value, int channel) {
        double dBm;

        switch (channel) {
            case 1:
                dBm = (ADC_value - 1155.3) / 16.468; // CH1:  ADC1 = 16.468 * dbm1 + 1155.3
                break;
            case 2:
                dBm = (ADC_value - 1145.5) / 16.461; // CH2:  ADC2 = 16.461 * dbm1 + 1145.5
                break;
            case 3:
                dBm = (ADC_value - 1131.4) / 16.272; // CH3:  ADC3 = 16.272 * dbm3 + 1131.4
                break;
            case 4:
                dBm = (ADC_value - 1121.0) / 16.014; // CH4:  ADC4 = 16.014 * dbm4 + 1121
                break;
            default:
                return Double.NaN;
        }

        return dBm;
    }

    private void receiveCentroidAOAAzimuth(double value) {
        setAOASolutionAzimuthText(value);
    }

    private void receiveCentroidAOAElevation(double value) {
        setAOASolutionElevationText(value);
    }

    private void receiveShowAOAMode(boolean isCentroidSelected) {
        if (isCentroidSelected) {
            lblAzimuth.setVisibility(View.VISIBLE);
            lblElevation.setVisibility(View.VISIBLE);
        } else {
            lblAzimuth.setVisibility(View.INVISIBLE);
            lblElevation.setVisibility(View.INVISIBLE);
        }
    }

    /** @noinspection EmptyMethod, unused */
    private void receiveHapticsMode(boolean isHapticsSelected) {
        // TODO: Implement Haptics Cues
    }

    public void setAOASolutionAzimuthText(double value) {
        @SuppressLint("DefaultLocale") String text = String.format("%.1f°<sup><small>Az</small></sup>", value);
        lblAzimuth.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
    }

    public void setAOASolutionElevationText(double value) {
        @SuppressLint("DefaultLocale") String text = String.format("%.1f°<sup><small>El</small></sup>", value);
        lblElevation.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
    }

}