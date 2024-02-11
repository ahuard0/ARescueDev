package com.Huard.PhoneRFFL;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class SideFragment extends Fragment {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkHaptics;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkHeatmap;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    public Switch chkShowCentroid;
    public Button btnCentroid;
    private ImageFragment imageFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_side, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnCentroid = view.findViewById(R.id.btnCentroid);
        chkShowCentroid = view.findViewById(R.id.chkShowCentroid);
        chkHeatmap = view.findViewById(R.id.chkHeatmap);
        chkHaptics = view.findViewById(R.id.chkHaptics);

        btnCentroid.setOnClickListener(v -> onPressBtnCentroid());
        chkShowCentroid.setOnClickListener(v -> onPressChkShowCentroid());
        chkHeatmap.setOnClickListener(v -> onPressChkHeatmap());
        chkHaptics.setOnClickListener(v -> onPressChkHaptics());

        imageFragment = (ImageFragment) getParentFragmentManager().findFragmentById(R.id.imageView);
    }

    private void onPressBtnCentroid() {
        try {
            CharSequence text = "Az " + " " + (Double.toString(imageFragment.avgCentroid[0])).substring(0, 5)
                    + "\nEl " + (Double.toString(imageFragment.avgCentroid[1])).substring(0, 5);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(requireContext(), text, duration);
            toast.show();
        } catch (RuntimeException e) {
            CharSequence text = "Gathering data...";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(requireContext(), text, duration);
            toast.show();
        }
    }

    private void onPressChkShowCentroid() {
        // TODO: Implement very large azimuth overlay
    }

    private void onPressChkHeatmap() {
        if (imageFragment.imageView.getDrawable().getAlpha() == 0) {
            imageFragment.imageView.setImageAlpha(255);
        } else {
            imageFragment.imageView.setImageAlpha(0);
        }
    }

    private void onPressChkHaptics() {
        CharSequence text = "Haptic switch (TODO)";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(requireContext(), text, duration);
        toast.show();
    }
}