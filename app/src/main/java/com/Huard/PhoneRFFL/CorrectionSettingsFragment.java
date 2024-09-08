package com.Huard.PhoneRFFL;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class CorrectionSettingsFragment extends Fragment implements ControlClientListener, DataClientListener {
    private CorrectionStorage correctionStorage;
    private CorrectionViewModel correctionViewModel;
    private LinearLayout pnlCorrectionSettings;
    private EditText editTxtOffsetAzimuth;
    private EditText editTxtOffsetElevation;
    private CheckBox chkInvertAzimuth;
    private CheckBox chkInvertElevation;
    private CheckBox chkEnableParallaxCorrection;
    private EditText txtEstimatedDistanceToEmitter;
    private EditText txtCameraOffsetX;
    private EditText txtCameraOffsetY;
    private EditText txtFreq;
    private EditText txtIPAddress;
    private TextView txtStatusDAQ;

    private static String IP_ADDRESS_DAQ;

    private final String TAG = "CorrectionSettingsFragment";

    private boolean isInitialized = false;
    private static float SAMPLE_BANDWIDTH_MHz = 2.4f; // MHz
    private static float[][] iqSamples;

    private static ArrayList<Double> MAX_POWER_dBm;  // FFT Max Power listed per channel

    private static ArrayList<ArrayList<Double>> FFT_FREQ_MHz;   // FFT output listed per channel
    private static ArrayList<ArrayList<Double>> FFT_POWER_dBm;  // FFT output listed per channel

    private static DataClient dataClient;
    private static ControlClient controlClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_correction_settings, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MAX_POWER_dBm = new ArrayList<>();
        MAX_POWER_dBm.add(0d);
        MAX_POWER_dBm.add(0d);
        MAX_POWER_dBm.add(0d);
        MAX_POWER_dBm.add(0d);
        MAX_POWER_dBm.add(0d);

        FFT_FREQ_MHz = new ArrayList<>();
        FFT_FREQ_MHz.add(new ArrayList<>());
        FFT_FREQ_MHz.add(new ArrayList<>());
        FFT_FREQ_MHz.add(new ArrayList<>());
        FFT_FREQ_MHz.add(new ArrayList<>());
        FFT_FREQ_MHz.add(new ArrayList<>());

        FFT_POWER_dBm = new ArrayList<>();
        FFT_POWER_dBm.add(new ArrayList<>());
        FFT_POWER_dBm.add(new ArrayList<>());
        FFT_POWER_dBm.add(new ArrayList<>());
        FFT_POWER_dBm.add(new ArrayList<>());
        FFT_POWER_dBm.add(new ArrayList<>());

        Button btnBack = view.findViewById(R.id.corr_factors_btnBack);
        btnBack.setOnClickListener(v -> onPressBack());

        Button btnSave = view.findViewById(R.id.corr_factors_btnSave);
        btnSave.setOnClickListener(v -> onPressSave());

        Button btnConfigDAQ = view.findViewById(R.id.corr_settings_btnConfigDAQ);
        btnConfigDAQ.setOnClickListener(v -> onPressConfigDAQ());

        Button btnConnectDAQ = view.findViewById(R.id.corr_settings_btnConnectDAQ);
        btnConnectDAQ.setOnClickListener(v -> onPressConnectDAQ());

        txtStatusDAQ = view.findViewById(R.id.corr_settings_txtStatusDAQ);
        txtIPAddress = view.findViewById(R.id.corr_settings_txtIPAddress);
        txtFreq = view.findViewById(R.id.corr_settings_txtFreq);
        pnlCorrectionSettings = view.findViewById(R.id.pnlCorrectionSettings);
        editTxtOffsetAzimuth = view.findViewById(R.id.corr_factors_txtOffsetAzimuth);
        editTxtOffsetElevation = view.findViewById(R.id.corr_factors_txtOffsetElevation);
        chkInvertAzimuth = view.findViewById(R.id.corr_factors_chkInvertAzimuth);
        chkInvertElevation = view.findViewById(R.id.corr_factors_chkInvertElevation);

        chkEnableParallaxCorrection = view.findViewById(R.id.corr_factors_chkEnableParallax);
        txtEstimatedDistanceToEmitter = view.findViewById(R.id.corr_factors_txtEstimatedDistance);
        txtCameraOffsetX = view.findViewById(R.id.corr_factors_txtCameraOffsetX);
        txtCameraOffsetY = view.findViewById(R.id.corr_factors_txtCameraOffsetY);

        correctionViewModel = new ViewModelProvider(requireActivity()).get(CorrectionViewModel.class);
        correctionViewModel.getShowCorrectionSettings().observe(getViewLifecycleOwner(), this::ShowCorrectionSettings);

        correctionStorage = new CorrectionStorage(requireContext());

        LoadSettingsFromStorage();

        isInitialized = true;
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

        // populate settings: parallax
        boolean enableParallax = Boolean.TRUE.equals(correctionViewModel.enableParallaxCorrection.getValue());
        chkEnableParallaxCorrection.setChecked(enableParallax);

        Double estDistance = correctionViewModel.estimatedDistanceToEmitter.getValue();
        Double offsetX = correctionViewModel.cameraOffsetX.getValue();
        Double offsetY = correctionViewModel.cameraOffsetY.getValue();

        if (estDistance != null)
            txtEstimatedDistanceToEmitter.setText(estDistance.toString());
        if (offsetX != null)
            txtCameraOffsetX.setText(offsetX.toString());
        if (offsetY != null)
            txtCameraOffsetY.setText(offsetY.toString());
    }

    private void onPressBack() {
        GoBack();
    }

    private void onPressSave() {
        SaveSettings();
        SaveSettingsToStorage();
        GoBack();
    }

    private void onPressConfigDAQ() {
        if (isInitialized) {
            IP_ADDRESS_DAQ = txtIPAddress.getText().toString().trim();
            if (!IP_ADDRESS_DAQ.isEmpty()) {
                if (controlClient != null)
                    controlClient.disconnect();
                controlClient = new ControlClient(this, IP_ADDRESS_DAQ, 5001);
                controlClient.connect();

                float freq_MHz;
                try {
                    freq_MHz = Float.parseFloat(txtFreq.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid frequency input: " + txtFreq.getText().toString());
                    return;
                }
                controlClient.sendGain(new int[]{496, 496, 496, 496, 496});
                controlClient.sendFrequency(freq_MHz);
                controlClient.sendSquelchThreshold(0.5f);
                controlClient.sendInit();
                controlClient.sendExit();
                controlClient.disconnect();
            }
        }
    }

    private void onPressConnectDAQ() {
        if (isInitialized) {
            IP_ADDRESS_DAQ = txtIPAddress.getText().toString().trim();
            if (!IP_ADDRESS_DAQ.isEmpty()) {
                if (dataClient != null)
                    dataClient.disconnect();
                dataClient = new DataClient(this, IP_ADDRESS_DAQ, 5000);
                dataClient.connect();
            }
        }
    }

    private void LoadSettingsFromStorage() {
        CorrectionSettings settings = correctionStorage.Load();
        if (settings != null) {
            chkInvertAzimuth.setChecked(settings.invertAzimuth);
            chkInvertAzimuth.setChecked(settings.invertElevation);
            editTxtOffsetAzimuth.setText(String.format(Locale.getDefault(), "%.4f\n", settings.corrAzimuth));
            editTxtOffsetElevation.setText(String.format(Locale.getDefault(), "%.4f\n", settings.corrElevation));
            chkEnableParallaxCorrection.setChecked(settings.enableParallax);
            txtEstimatedDistanceToEmitter.setText(String.format(Locale.getDefault(), "%.4f\n", settings.distanceToEmitter));
            txtCameraOffsetX.setText(String.format(Locale.getDefault(), "%.4f\n", settings.cameraOffsetX));
            txtCameraOffsetY.setText(String.format(Locale.getDefault(), "%.4f\n", settings.cameraOffsetY));

            txtFreq.setText(String.format(Locale.getDefault(), "%4.1f\n", settings.frequency_mhz));
            txtIPAddress.setText(settings.ip_address_daq);

            correctionViewModel.setInvertAxisAOAAzimuth(settings.invertAzimuth);
            correctionViewModel.setInvertAxisAOAElevation(settings.invertElevation);
            correctionViewModel.setCorrFactorDeltaDecibelsAzimuth(settings.corrAzimuth);
            correctionViewModel.setCorrFactorDeltaDecibelsElevation(settings.corrElevation);
            correctionViewModel.setEnableParallaxCorrection(settings.enableParallax);
            correctionViewModel.setEstimatedDistanceToEmitter(settings.distanceToEmitter);
            correctionViewModel.setCameraOffsetX(settings.cameraOffsetX);
            correctionViewModel.setCameraOffsetY(settings.cameraOffsetY);
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

        boolean enableParallax = chkEnableParallaxCorrection.isChecked();
        correctionViewModel.setEnableParallaxCorrection(enableParallax);

        String distanceToEmitterStr = String.valueOf(txtEstimatedDistanceToEmitter.getText());
        String offsetXStr= String.valueOf(txtCameraOffsetX.getText());
        String offsetYStr = String.valueOf(txtCameraOffsetY.getText());

        double distanceToEmitter;
        double offsetX;
        double offsetY;
        if (!distanceToEmitterStr.isEmpty() && !offsetXStr.isEmpty() && !offsetYStr.isEmpty()) {
            distanceToEmitter = Double.parseDouble(distanceToEmitterStr);
            offsetX = Double.parseDouble(offsetXStr);
            offsetY = Double.parseDouble(offsetYStr);
            correctionViewModel.setEstimatedDistanceToEmitter(distanceToEmitter);
            correctionViewModel.setCameraOffsetX(offsetX);
            correctionViewModel.setCameraOffsetY(offsetY);
        }
    }

    private void SaveSettingsToStorage() {
        boolean invertAzimuth = chkInvertAzimuth.isChecked();
        boolean invertElevation = chkInvertElevation.isChecked();
        boolean enableParallax = chkEnableParallaxCorrection.isChecked();

        String offsetAzimuthStr = String.valueOf(editTxtOffsetAzimuth.getText());
        String offsetElevationStr = String.valueOf(editTxtOffsetElevation.getText());
        String distanceToEmitterStr = String.valueOf(txtEstimatedDistanceToEmitter.getText());
        String offsetXStr= String.valueOf(txtCameraOffsetX.getText());
        String offsetYStr = String.valueOf(txtCameraOffsetY.getText());

        String ip_address_daq = String.valueOf(txtIPAddress.getText());
        String frequency_mhz_str = String.valueOf(txtFreq.getText());

        if (!ip_address_daq.isEmpty() && !frequency_mhz_str.isEmpty() && !offsetAzimuthStr.isEmpty() && !offsetElevationStr.isEmpty() && !distanceToEmitterStr.isEmpty() && !offsetXStr.isEmpty() && !offsetYStr.isEmpty()) {
            double offsetAzimuth = Double.parseDouble(offsetAzimuthStr);
            double offsetElevation = Double.parseDouble(offsetElevationStr);
            double distanceToEmitter = Double.parseDouble(distanceToEmitterStr);
            double offsetX = Double.parseDouble(offsetXStr);
            double offsetY = Double.parseDouble(offsetYStr);
            double frequency_mhz = Double.parseDouble(frequency_mhz_str);

            CorrectionSettings settings = new CorrectionSettings();
            settings.corrAzimuth = offsetAzimuth;
            settings.corrElevation = offsetElevation;
            settings.invertAzimuth = invertAzimuth;
            settings.invertElevation = invertElevation;
            settings.enableParallax = enableParallax;
            settings.distanceToEmitter = distanceToEmitter;
            settings.cameraOffsetX = offsetX;
            settings.cameraOffsetY = offsetY;
            settings.ip_address_daq = ip_address_daq;
            settings.frequency_mhz = frequency_mhz;
            correctionStorage.Save(settings);
        }
    }

    private void GoBack() {
        correctionViewModel.setShowCorrectionSettings(false);  // set correction view state
        ShowCorrectionSettings(false);  // hide panel
    }



    public void notifyControlClient(String message) {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> {
                Log.i(TAG, "Control message received: " + message);
                setStatusText(message);
            });
    }

    public void notifyDataClient(float[][] data, HeaderIQ header) {
        if (getActivity() != null)
            getActivity().runOnUiThread(() -> processData(data, header));
    }

    private void setStatusText(String message) {
        if (pnlCorrectionSettings.getVisibility() == View.VISIBLE)
            txtStatusDAQ.setText(message);
    }

    private void processData(float[][] data, @NonNull HeaderIQ header) {
        iqSamples = data;
        int SAMPLE_SIZE = (int) header.getCpiLength();
        SAMPLE_BANDWIDTH_MHz = (float)header.getSamplingFreq()/1E6f;
        Log.i(TAG, "I/Q Data received: Size " + SAMPLE_SIZE);

        computeFFT();
        computeMaxPower();

        int ID = header.getIndexCPI();

        StringBuilder msg = new StringBuilder();
        int size = MAX_POWER_dBm.size();
        for (int i = 0; i < size; i++) {
            Double v = MAX_POWER_dBm.get(i);
            msg.append(String.format(Locale.US, "%2.1f", v));
            if (i < size - 1) {
                msg.append(", ");
            } else {
                msg.append(" dBm");
            }
        }

        String message = String.format(Locale.US, "Max Power (Index %d): %s", ID, msg);

        Log.i(TAG, message);

        setStatusText(msg.toString());

        // TODO: Send channel peak powers up to the next higher module
    }

    public static double[][] convertInterleavedToSeparate(float[] iqSamples) {
        int sampleSize = iqSamples.length / 2;
        double[][] dataRI = new double[2][sampleSize];

        for (int i = 0; i < sampleSize; i++) {
            dataRI[0][i] = iqSamples[2 * i];       // Real part
            dataRI[1][i] = iqSamples[2 * i + 1];   // Imaginary part
        }

        return dataRI;
    }

    @NonNull
    private double[][] computeFFT(@NonNull float[] iq_samples) {  // Verified Good
        double[][] dataRI = convertInterleavedToSeparate(iq_samples);

        FastFourierTransformer.transformInPlace(dataRI, DftNormalization.STANDARD, TransformType.FORWARD);
        return dataRI;
    }

    private void computeFFT() {

        for (int j=0; j<5; j++) {
            double[][] fftResult = computeFFT(iqSamples[j]);
            double[][] shiftedFftResult = fftShift(fftResult); // [real,imag][n_samples]

            FFT_FREQ_MHz.get(j).clear();  // clear containers used for calculating max power in a later step
            FFT_POWER_dBm.get(j).clear();

            int num_samples = shiftedFftResult[0].length;
            double frequencyStep = SAMPLE_BANDWIDTH_MHz / num_samples;
            for (int i = 0; i < num_samples; i++) {
                double fs_MHz = i * frequencyStep - SAMPLE_BANDWIDTH_MHz / 2;
                double V_mV_real = shiftedFftResult[0][i];
                double V_mV_imag = shiftedFftResult[1][i];
                double V_mV = (V_mV_real * V_mV_real + V_mV_imag * V_mV_imag)/num_samples; // magnitude
                double psd_uW = V_mV / 50;
                double P_dBm = (10*Math.log10(psd_uW)) - 30;
                FFT_FREQ_MHz.get(j).add(fs_MHz);
                FFT_POWER_dBm.get(j).add(P_dBm);
            }
        }
    }

    public static double[][] fftShift(double[][] data) {
        int n = data.length;  // Number of rows (real/imag)
        int m = data[0].length;  // Number of columns (length of FFT)
        int halfSize = m / 2;  // Midpoint of the FFT data
        double[][] shiftedData = new double[n][m];  // Array to hold shifted data

        for (int i = 0; i < n; i++) {
            // Shift the second half to the first half
            System.arraycopy(data[i], halfSize, shiftedData[i], 0, m - halfSize);
            // Shift the first half to the second half
            System.arraycopy(data[i], 0, shiftedData[i], m - halfSize, halfSize);
        }
        return shiftedData;
    }

    private void computeMaxPower() {
        for (int j = 0; j < 5; j++) {
            List<Double> powerList = FFT_POWER_dBm.get(j); // Get the list of power values for the j-th channel
            Double maxPower = Double.NEGATIVE_INFINITY; // Start with the lowest possible value

            // Iterate through the power list to find the maximum value
            for (Double power : powerList) {
                if (power > maxPower) {
                    maxPower = power;
                }
            }

            // Store the maximum power value in MAX_POWER_dBm
            MAX_POWER_dBm.set(j, maxPower);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dataClient != null) {
            dataClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isInitialized) {
            if (dataClient != null) {
                dataClient.disconnect();
            }
            if (controlClient != null) {
                controlClient.disconnect();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isInitialized) {
            if (dataClient != null) {
                dataClient.disconnect();
            }
            if (controlClient != null) {
                controlClient.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isInitialized) {
            if (dataClient != null) {
                dataClient.disconnect();
            }
            if (controlClient != null) {
                controlClient.disconnect();
            }
        }
    }
}