package com.Huard.PhoneRFFL;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class SolutionFragment extends Fragment {

    private boolean IsHapticsEnabled = false;
    private TextView lblAzimuth;
    private TextView lblElevation;
    private SolutionViewModel solutionViewModel;
    private ConnectionViewModel connectionViewModel;
    private static final double FOV_DEG = 77;
    public static final int CHANNEL_UP = 3;
    public static final int CHANNEL_DOWN = 4;
    public static final int CHANNEL_LEFT = 1;
    public static final int CHANNEL_RIGHT = 2;
    private static double CORR_dB_ELEVATION = 0;
    private static double CORR_dB_AZIMUTH = 0;
    private static boolean INVERT_AZIMUTH_AOA = false;
    private static boolean INVERT_ELEVATION_AOA = false;
    public static final int HAPTICS_CHANNEL_UP = 4;  // D4
    public static final int HAPTICS_CHANNEL_DOWN = 3;  // D3
    public static final int HAPTICS_CHANNEL_LEFT = 6;  // D6
    public static final int HAPTICS_CHANNEL_RIGHT = 5;  // D5

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_solution, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lblAzimuth = view.findViewById(R.id.lblAzimuth);
        lblElevation = view.findViewById(R.id.lblElevation);

        CorrectionViewModel correctionViewModel = new ViewModelProvider(requireActivity()).get(CorrectionViewModel.class);
        correctionViewModel.getInvertAxisAOAAzimuth().observe(getViewLifecycleOwner(), this::receiveInvertAxisAOAAzimuth);
        correctionViewModel.getInvertAxisAOAElevation().observe(getViewLifecycleOwner(), this::receiveInvertAxisAOAElevation);
        correctionViewModel.getCorrFactorDeltaDecibelsAzimuth().observe(getViewLifecycleOwner(), this::receiveCorrFactorDeltaDecibelsAzimuth);
        correctionViewModel.getCorrFactorDeltaDecibelsElevation().observe(getViewLifecycleOwner(), this::receiveCorrFactorDeltaDecibelsElevation);

        SideViewModel sideViewModel = new ViewModelProvider(requireActivity()).get(SideViewModel.class);
        sideViewModel.getCentroidAOASelected().observe(getViewLifecycleOwner(), this::receiveShowAOAMode);
        sideViewModel.getHapticsSelected().observe(getViewLifecycleOwner(), this::receiveHapticsMode);

        solutionViewModel = new ViewModelProvider(requireActivity()).get(SolutionViewModel.class);
        solutionViewModel.getCentroidAOAAzimuth().observe(getViewLifecycleOwner(), this::receiveCentroidAOAAzimuth);
        solutionViewModel.getCentroidAOAElevation().observe(getViewLifecycleOwner(), this::receiveCentroidAOAElevation);

        connectionViewModel = new ViewModelProvider(requireActivity()).get(ConnectionViewModel.class);

        ChannelViewModel channelViewModel = new ViewModelProvider(requireActivity()).get(ChannelViewModel.class);
        channelViewModel.getAzimuth().observe(getViewLifecycleOwner(), this::receiveAzimuthADC);
        channelViewModel.getElevation().observe(getViewLifecycleOwner(), this::receiveElevationADC);
    }

    private void receiveInvertAxisAOAAzimuth(Boolean value) {
        INVERT_AZIMUTH_AOA = value;
    }

    private void receiveInvertAxisAOAElevation(Boolean value) {
        INVERT_ELEVATION_AOA = value;
    }

    private void receiveCorrFactorDeltaDecibelsAzimuth(Double value) {
        CORR_dB_AZIMUTH = value;
    }

    private void receiveCorrFactorDeltaDecibelsElevation(Double value) {
        CORR_dB_ELEVATION = value;
    }

    private void receiveAzimuthADC(Queue<Pair<Short, Short>> pairQueue) {
        Queue<Double> solutionQueue = new LinkedList<>();
        for (Pair<Short, Short> values : pairQueue) {
            double Power_CHa = convert_ADC_to_dBm(values.first, CHANNEL_LEFT);
            double Power_CHb = convert_ADC_to_dBm(values.second, CHANNEL_RIGHT);
            double delta_dB = Power_CHa - Power_CHb;

            // Apply correction factor:  -0.96048 dB  (bottom channel #2 has this additional loss relative to the top channel #1)
            delta_dB -= CORR_dB_AZIMUTH;  // When equal power applied through splitter without correction: delta dB = +0.96 dB

            double AOA_deg = convert_delta_dB_to_AOA(delta_dB, INVERT_AZIMUTH_AOA);
            if (Math.abs(AOA_deg) <= FOV_DEG) {  // Limit AOA to within the Camera FOV
                Log.d("SolutionFragment", "AOA: " + AOA_deg + "deg Azi, Azimuth: (" + values.first + ", " + values.second + ")");
                solutionQueue.add(AOA_deg);
            }
        }
        solutionViewModel.setAzimuthPointsAOA(solutionQueue);
        sendHapticsAzimuth(solutionQueue);
    }

    private void receiveElevationADC(Queue<Pair<Short, Short>> pairQueue) {
        Queue<Double> solutionQueue = new LinkedList<>();
        for (Pair<Short, Short> values : pairQueue) {
            double Power_CHa = convert_ADC_to_dBm(values.first,  CHANNEL_UP);
            double Power_CHb = convert_ADC_to_dBm(values.second, CHANNEL_DOWN);
            double delta_dB = Power_CHa - Power_CHb;

            // Apply correction factor:  -2.21188 dB  (right channel #4 has this additional loss relative to the left channel #3)
            delta_dB -= CORR_dB_ELEVATION;  // When equal power applied through splitter without correction: delta dB = +2.21188 dB

            double AOA_deg = convert_delta_dB_to_AOA(delta_dB, INVERT_ELEVATION_AOA);
            if (Math.abs(AOA_deg) <= FOV_DEG) {  // Limit AOA to within the Camera FOV
                Log.d("SolutionFragment", "AOA: " + AOA_deg + "deg El, Elevation: (" + values.first + ", " + values.second + ")");
                solutionQueue.add(AOA_deg);
            }
        }
        solutionViewModel.setElevationPointsAOA(solutionQueue);
        sendHapticsElevation(solutionQueue);
    }

    private double convert_delta_dB_to_AOA(double delta_dB, boolean invert) {
        // dbm12diff = -0.1462 * AOAResult + 0.0000
        // dbm34diff = -0.1462 * AOAResult + 0.0000
        if (invert)
            return delta_dB/(0.1462);
        else
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

    public void setAOASolutionAzimuthText(double value) {
        @SuppressLint("DefaultLocale") String text = String.format("%.1f°<sup><small>Az</small></sup>", value);
        lblAzimuth.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
    }

    public void setAOASolutionElevationText(double value) {
        @SuppressLint("DefaultLocale") String text = String.format("%.1f°<sup><small>El</small></sup>", value);
        lblElevation.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
    }

    private void receiveHapticsMode(boolean isHapticsSelected) {
        IsHapticsEnabled = isHapticsSelected;

        if (!IsHapticsEnabled) {
            sendCommand(String.format(Locale.US, "$|_D%d_OFF\n", HAPTICS_CHANNEL_UP));
            sendCommand(String.format(Locale.US, "$|_D%d_OFF\n", HAPTICS_CHANNEL_DOWN));
            sendCommand(String.format(Locale.US, "$|_D%d_OFF\n", HAPTICS_CHANNEL_LEFT));
            sendCommand(String.format(Locale.US, "$|_D%d_OFF\n", HAPTICS_CHANNEL_RIGHT));
        }
    }

    private void sendHapticsAzimuth(Queue<Double> solutionQueue) {
        if (IsHapticsEnabled) {
            int positiveCount = 0;
            int negativeCount = 0;

            for (Double item : solutionQueue) {
                if (item > 0) {
                    positiveCount += 1;
                } else if (item < 0) {
                    negativeCount += 1;
                }
            }

            // Determine the Haptics Direction
            if (positiveCount > negativeCount) {  // Is Right
                sendCommand(String.format(Locale.US, "$|_D%d_OFF\n", HAPTICS_CHANNEL_LEFT));
                sendCommand(String.format(Locale.US, "$|_D%d_ON\n", HAPTICS_CHANNEL_RIGHT));
            } else if (negativeCount > positiveCount) {  // Is Left
                sendCommand(String.format(Locale.US, "$|_D%d_ON\n", HAPTICS_CHANNEL_LEFT));
                sendCommand(String.format(Locale.US, "$|_D%d_OFF\n", HAPTICS_CHANNEL_RIGHT));
            } else {  // Is Either Right or Left
                sendCommand(String.format(Locale.US, "$|_D%d_ON\n", HAPTICS_CHANNEL_LEFT));
                sendCommand(String.format(Locale.US, "$|_D%d_ON\n", HAPTICS_CHANNEL_RIGHT));
            }
        }
    }

    private void sendHapticsElevation(Queue<Double> solutionQueue) {
        if (IsHapticsEnabled) {
            int positiveCount = 0;
            int negativeCount = 0;

            for (Double item : solutionQueue) {
                if (item > 0) {
                    positiveCount += 1;
                } else if (item < 0) {
                    negativeCount += 1;
                }
            }

            // Determine the Haptics Direction
            if (positiveCount > negativeCount) {  // Is Up
                sendCommand(String.format(Locale.US, "$|_D%d_ON\n", HAPTICS_CHANNEL_UP));
                sendCommand(String.format(Locale.US, "$|_D%d_OFF\n", HAPTICS_CHANNEL_DOWN));
            } else if (negativeCount > positiveCount) {  // Is Down
                sendCommand(String.format(Locale.US, "$|_D%d_OFF\n", HAPTICS_CHANNEL_UP));
                sendCommand(String.format(Locale.US, "$|_D%d_ON\n", HAPTICS_CHANNEL_DOWN));
            } else {  // Is Either Up or Down
                sendCommand(String.format(Locale.US, "$|_D%d_ON\n", HAPTICS_CHANNEL_UP));
                sendCommand(String.format(Locale.US, "$|_D%d_ON\n", HAPTICS_CHANNEL_DOWN));
            }
        }
    }

    private void sendCommand(String command) {
        connectionViewModel.setCommand(command);
    }
}