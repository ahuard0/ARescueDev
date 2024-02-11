package com.Huard.PhoneRFFL;

import android.graphics.Bitmap;

public class BitmapManager {
    private Bitmap bitmap;
    private int[][] valueMatrix;

    public BitmapManager(Bitmap bitmap, int[][] valueMatrix) {
        this.bitmap = bitmap;
        this.valueMatrix = valueMatrix;
    }

    public void colorReset() {
        // Implement color reset logic
    }

    public void squarePlot(int x, int y) {
        // Implement square plotting logic
    }

    public void eraseDelayed(int left, int up, int right, int bot) {
        // Implement delayed erase logic
    }
}
