package com.Huard.PhoneRFFL;

import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import java.util.TimerTask;

public class TimerManager {
    private final TimerFragment fragment;
    private TimerTask timerTask;
    private double time;
    private boolean timerStarted;

    java.util.Timer timer;

    public TimerManager(TimerFragment fragment) {
        this.fragment = fragment;
        timer = new java.util.Timer();
    }

    private void startTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                fragment.requireActivity().runOnUiThread(() -> {
                    time++;
                    fragment.lblTimerStatus.setText(getLblTimerStatus());
                });
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    public void startStop() {
        if (!timerStarted) {
            timerStarted = true;
            setButtonUI("STOP", R.color.red);

            startTimer();
        } else {
            timerStarted = false;
            setButtonUI("START", R.color.green);

            timerTask.cancel();
        }
    }

    private void setButtonUI(String start, int color) {
        fragment.btnStartStop.setText(start);
        fragment.btnStartStop.setTextColor(ContextCompat.getColor(fragment.requireActivity(), color));
    }

    public void reset() {
        AlertDialog.Builder resetAlert = new AlertDialog.Builder(fragment.requireActivity());
        resetAlert.setTitle("Reset TimerFragment");
        resetAlert.setMessage("Are you sure you want to reset the timer?");
        resetAlert.setPositiveButton("Reset", (dialogInterface, i) -> {
            if (timerTask != null) {
                timerTask.cancel();
                setButtonUI("START", R.color.green);
                time = 0.0;
                timerStarted = false;
                fragment.lblTimerStatus.setText(formatTime(0,0,0));
            }
        });

        resetAlert.setNeutralButton("Cancel", (dialogInterface, i) -> {
            // do nothing
        });

        resetAlert.show();
    }

    private String getLblTimerStatus() {
        int rounded = (int) Math.round(time);

        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = (rounded % 86400) / 3600;

        return formatTime(seconds, minutes, hours);
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(int seconds, int minutes, int hours) {
        return String.format("%02d", hours) + " : " + String.format("%02d", minutes) + " : " + String.format("%02d", seconds);
    }
}
