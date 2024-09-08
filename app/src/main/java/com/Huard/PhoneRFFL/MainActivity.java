package com.Huard.PhoneRFFL;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static final int screenWidth = 2400;
    static final int screenHeight = 1080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the result to the fragment for handling
        ImageFragment fragment = (ImageFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_image_container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initialize() {
        initializeWindow();
        initializeTimerFragment();
        initializeSideFragment();
        initializeImageFragment();
        initializeSolutionFragment();
        initializeConnectionFragment();
        initializeSimulationFragment();
        initializeFilterFragment();
        initializeTerminalFragment();
        initializeCorrFragment();
        initializeCorrSettingsFragment();
    }

    private void initializeWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_main);
    }

    private void initializeCorrSettingsFragment() {  // Show SimulationFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_corr_settings_container, new CorrectionSettingsFragment())
                .commit();
    }

    private void initializeCorrFragment() {  // Show SimulationFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_corr_container, new CorrectionFragment())
                .commit();
    }

    private void initializeFilterFragment() {  // Show SimulationFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_filter_container, new FilterFragment())
                .commit();
    }

    private void initializeSimulationFragment() {  // Show SimulationFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_simulation_container, new SimulationFragment())
                .commit();
    }

    private void initializeSolutionFragment() {  // Show SolutionFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_solution_container, new SolutionFragment())
                .commit();
    }

    private void initializeSideFragment() {  // Show SideFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_side_container, new SideFragment())
                .commit();
    }

    private void initializeImageFragment() {  // Show ImageFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_image_container, new ImageFragment())
                .commit();
    }

    private void initializeTimerFragment() {  // Show TimerFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_timer_container, new TimerFragment())
                .commit();
    }

    private void initializeConnectionFragment() {  // Show SolutionFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_connection_container, new ConnectionFragment())
                .commit();
    }

    private void initializeTerminalFragment() {  // Show SolutionFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_terminal_container, new TerminalFragment())
                .commit();
    }

}
