package com.Huard.PhoneRFFL;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    static final int screenWidth = 2400;
    static final int screenHeight = 1080;
    static final int range = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
    }

    private void initialize() {
        initializeWindow();
        initializeTimerFragment();
        initializeSideFragment();
        initializeImageFragment();
        initializeSolutionFragment();
    }


    private void initializeWindow() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_main);
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



}