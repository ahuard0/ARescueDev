package com.Huard.PhoneRFFL;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.LinkedList;

public class ImageFragment extends Fragment implements ImageReader.OnImageAvailableListener {
    public ImageView imageView;
    public final Bitmap bitmap = Bitmap.createBitmap(MainActivity.screenWidth, MainActivity.screenHeight, Bitmap.Config.ARGB_8888);

    /** ValueMatrix and ColoredSquares variables */
    final int[] jets = new int[14]; // colors for the JET heatmap
    final int[][] valueMatrix = new int[1080][2400]; // matrix of numerical values for pixel colors
    final int lifetime = 2500; // display duration of data points (in ms)
    final int interval = 35; // interval between incoming data points (in ms) -FOR AUTOGEN-
    int x; // x pixel coordinate
    int y; // y pixel coordinate
    final double[][] degrees = new double[288][2]; // fake data storage (azimuth, elevation) -FOR AUTOGEN-
    final LinkedList<Pair<Double, Double>> pairQueue = new LinkedList<>(); // list of <El, Az> for centroid calculation
    final int centroidHistoryCutoff = 200; // number of stored points for centroid calculation
    public final double[] avgCentroid = new double[2]; // store the centroid based on recent history
    final float fx = (float) MainActivity.screenWidth; // for matrix transformation
    final float fy = (float) -MainActivity.screenHeight; // for matrix transformation

//    double f = 19285.7; // suggested value of f (can't test on mock data, too zoomed in)

    private SolutionViewModel solutionViewModel;

    public ImageFragment() {
        initializeArrays();
        initializeSyntheticData();

        colorReset(); // initial bitmap coloring (transparent and almost invisible)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image, container, false);  // Inflate the layout for this fragment
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {  // Asks for camera permissions
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.CAMERA}, 121);  // calls showCameraFragment() if permission is granted upon request
        } else {
            showCameraFragment();  // show live camera footage if permission is already granted
        }

        imageView = view.findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);

        SideViewModel sideViewModel = new ViewModelProvider(requireActivity()).get(SideViewModel.class);
        sideViewModel.getHeatMapSelected().observe(getViewLifecycleOwner(), this::updateHeatMap);

        solutionViewModel = new ViewModelProvider(requireActivity()).get(SolutionViewModel.class);

        BeginLoopSyntheticData();
    }

    private void updateHeatMap(boolean isHeatMapSelected) {
        if (isHeatMapSelected) {
            imageView.setImageAlpha(255);
        } else {
            imageView.setImageAlpha(0);
        }
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        imageReader.acquireLatestImage().close();
    }

    public void colorReset() {  // reset color to fully transparent (almost)
        for (int i = 0; i < MainActivity.screenWidth; i++) {
            for (int j = 0; j < MainActivity.screenHeight; j++) {
                bitmap.setPixel(i, j, jets[0]);
                valueMatrix[j][i] = 0;
            }
        }
    }

    protected void showCameraFragment() {  // Creates camera fragment showing live footage
        final CameraManager manager = (CameraManager) requireContext().getSystemService(Context.CAMERA_SERVICE);
        String cameraId = null;
        try {
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        CameraConnectionFragment camera2Fragment =
                CameraConnectionFragment.newInstance(
                        (size, rotation) -> {},
                        this,
                        R.layout.camera_fragment,
                        new Size(MainActivity.screenWidth, MainActivity.screenHeight));

        camera2Fragment.setCamera(cameraId);
        requireActivity().getFragmentManager().beginTransaction().replace(R.id.container, camera2Fragment).commit();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {  // show live camera footage
            showCameraFragment();  // show live camera footage
        } else {
            requireActivity().finish();
        }
    }


    /** Displays a square given coordinates (and size) */
    public void squarePlot(int x, int y) {
        int left = Math.max(x - MainActivity.range, 0);
        int up = Math.max(y - MainActivity.range, 0);
        int right = Math.min(x + MainActivity.range, MainActivity.screenWidth);
        int bot = Math.min(y + MainActivity.range, MainActivity.screenHeight);

        for (int i = left; i < right; i++) {
            for (int j = up; j < bot; j++) {
                int color = valueMatrix[j][i];
                color = Math.min(color + 1, jets.length - 1);
                bitmap.setPixel(i, j, jets[color]);
                valueMatrix[j][i] = color;
            }
        }

        // call erase data points
        eraseDelayed(left, up, right, bot);
    }

    /** Removes a square after specified "lifetime" */
    public void eraseDelayed(int left, int up, int right, int bot) {
        Runnable eraser = new Runnable() {
            @Override
            public void run() {
                synchronized(this) {
                    try {
                        wait(lifetime);
                    } catch (InterruptedException ignored) { }
                }
                for (int i = left; i < right; i++) {
                    for (int j = up; j < bot; j++) {
                        int color = valueMatrix[j][i];
                        color = Math.max(color - 1, 0);
                        bitmap.setPixel(i, j, jets[color]);
                        valueMatrix[j][i] = color;
                    }
                }
            }
        };
        new Thread(eraser).start();
    }


    private void initializeArrays() {
        // Initialize arrays
        String[] jetColors = "0x00000000, 0x60000088, 0x600000ff, 0x600088ff, 0x6000ffff, 0x6088ff88, 0x60ffff00, 0x60ff8800, 0x60ff0000, 0x60880000, 0x60880000, 0x60880000, 0x60880000, 0x60880000".split(", ");
        for (int i = 0; i < jets.length; i++) {
            jets[i] = Integer.decode(jetColors[i]);
        }
    }

    // TODO: Move Into SolutionFragment
    private void initializeSyntheticData() {
        // Mock data here
        String[] ADC1234 = "441,434,421,434, 480,474,460,474, 486,470,459,470, 390,386,378,386, 485,478,469,478, 390,388,371,388, 470,461,445,461, 390,383,373,383, 502,500,493,500, 484,473,462,473, 403,396,381,396, 416,410,402,410, 473,468,455,468, 434,428,417,428, 456,453,438,453, 467,452,445,452, 402,395,384,395, 455,448,437,448, 471,457,455,457, 428,427,416,427, 432,426,418,426, 459,447,442,447, 411,404,393,404, 446,442,430,442, 445,434,423,434, 382,369,356,369, 437,428,413,428, 381,373,363,373, 414,405,396,405, 455,447,434,447, 399,395,386,395, 432,428,414,428, 423,413,398,413, 417,409,393,409, 436,430,417,430, 372,367,352,367, 444,440,438,440, 384,373,357,373, 464,455,443,455, 511,504,497,504, 415,406,392,406, 448,447,438,447, 490,483,481,483, 476,468,457,468, 520,513,502,513, 464,457,446,457, 438,431,423,431, 531,528,514,528, 500,493,482,493, 475,470,457,470, 382,374,361,374, 468,461,449,461, 494,488,472,488, 385,369,358,369, 545,534,520,534, 501,497,489,497, 388,378,365,378, 552,543,532,543, 480,474,466,474, 389,385,373,385, 478,475,466,475, 542,534,519,534, 426,417,397,417, 496,480,466,480, 397,394,384,394, 460,454,445,454, 492,491,480,491, 424,417,401,417, 477,467,457,467, 447,444,435,444, 452,445,432,445, 453,440,429,440, 483,477,469,477, 460,454,447,454, 475,468,463,468, 490,485,473,485, 393,388,372,388, 386,382,367,382, 439,433,422,433, 396,386,373,386, 452,448,435,448, 420,412,399,412, 459,447,441,447, 451,445,429,445, 477,467,453,467, 370,365,350,365, 484,476,471,476, 475,467,457,467, 446,439,432,439, 454,443,424,443, 452,444,431,444, 448,449,436,449, 485,480,468,480, 446,439,432,439, 447,444,433,444, 375,369,358,369, 465,463,453,463, 481,476,464,476, 544,535,522,535, 477,470,458,470, 543,532,521,532, 510,506,494,506, 392,387,374,387, 484,478,464,478, 546,542,530,542, 437,425,415,425, 412,407,394,407, 403,394,382,394, 446,437,422,437, 386,378,364,378, 434,424,411,424, 533,525,516,525, 420,416,409,416, 423,410,403,410, 395,391,379,391, 505,499,489,499, 386,378,359,378, 488,480,467,480, 470,462,452,462, 407,404,394,404, 462,457,445,457, 498,491,481,491, 378,372,355,372, 400,399,386,399, 507,498,488,498, 487,484,470,484, 438,430,425,430, 414,411,400,411, 476,468,459,468, 466,458,445,458, 390,378,363,378, 455,448,439,448, 421,411,392,411, 424,419,410,419, 468,461,446,461, 445,435,427,435, 469,463,458,463, 480,475,466,475, 431,426,413,426, 404,397,380,397, 484,477,463,477, 388,374,359,374, 474,465,452,465, 433,425,417,425, 466,458,447,458, 447,442,424,442, 439,438,427,438, 467,459,449,459, 405,401,390,401, 376,366,352,366, 481,474,458,474, 464,462,449,462, 449,442,429,442, 466,461,450,461, 381,377,366,377, 489,478,467,478, 430,419,409,419, 458,449,434,449, 478,470,450,470, 398,391,375,391, 492,488,475,488, 392,386,373,386, 495,488,478,488, 435,423,409,423, 430,424,415,424, 486,480,471,480, 377,366,355,366, 498,493,477,493, 399,398,382,398, 433,420,409,420, 491,478,469,478, 431,426,412,426, 449,439,430,439, 417,405,391,405, 458,450,439,450, 469,458,445,458, 381,371,358,371, 460,455,452,455, 398,391,380,391, 467,458,442,458, 404,398,387,398, 408,399,387,399, 395,388,381,388, 410,398,387,398, 422,413,402,413, 446,442,425,442, 528,521,511,521, 457,451,437,451, 514,509,498,509, 467,458,447,458, 389,381,371,381, 526,520,512,520, 423,414,404,414, 480,471,457,471, 524,513,506,513, 487,477,470,477, 519,514,508,514, 512,513,499,513, 412,405,395,405, 491,480,472,480, 439,431,423,431, 457,449,434,449, 451,443,432,443, 405,396,389,396, 452,441,430,441, 450,445,431,445, 417,407,390,407, 392,384,375,384, 386,383,370,383, 405,392,379,392, 447,437,424,437, 492,484,479,484, 372,365,353,365, 407,399,384,399, 491,485,475,485, 512,508,492,508, 457,451,435,451, 484,477,465,477, 432,429,423,429, 418,412,399,412, 482,472,467,472, 510,509,503,509, 427,420,408,420, 502,494,483,494, 464,458,446,458, 417,406,394,406, 519,511,504,511, 449,444,431,444, 499,493,485,493, 518,512,505,512, 432,432,422,432, 503,489,484,489, 476,465,453,465, 421,417,408,417, 510,503,488,503, 514,506,498,506, 528,522,509,522, 381,378,363,378, 501,497,488,497, 490,483,469,483, 481,476,468,476, 486,477,472,477, 411,398,383,398, 487,480,469,480, 461,448,439,448, 465,458,449,458, 391,379,364,379, 510,497,486,497, 464,454,443,454, 503,496,486,496, 386,378,370,378, 402,394,380,394, 459,453,444,453, 490,476,468,476, 379,366,353,366, 514,511,502,511, 409,409,397,409, 405,396,381,396, 390,380,371,380, 376,367,355,367, 498,490,486,490, 507,500,495,500, 380,370,355,370, 373,366,355,366, 487,477,468,477, 554,545,537,545, 437,429,419,429, 473,471,456,471, 549,536,527,536, 429,422,408,422, 479,477,468,477, 499,489,476,489, 496,489,476,489, 373,370,362,370, 390,381,373,381, 425,415,407,415, 526,521,508,521, 467,461,445,461, 390,380,374,380, 505,498,486,498, 452,442,432,442, 397,386,365,386, 514,502,490,502, 531,527,515,527, 436,432,423,432, 397,387,383,387, 467,458,445,458, 468,463,456,463".split(", ");

        for (int i = 0; i < ADC1234.length; i++) {
            String[] ch1ch2ch3ch4 = ADC1234[i].split(",");
            double ADC1 = Float.parseFloat(ch1ch2ch3ch4[0]); // CH1:  ADC1 = 16.468 * dbm1 + 1155.3
            double ADC2 = Float.parseFloat(ch1ch2ch3ch4[1]); // CH2:  ADC2 = 16.461 * dbm1 + 1145.5
            double ADC3 = Float.parseFloat(ch1ch2ch3ch4[2]); // CH3:  ADC3 = 16.272 * dbm3 + 1131.4
            double ADC4 = Float.parseFloat(ch1ch2ch3ch4[3]); // CH4:  ADC4 = 16.014 * dbm4 + 1121
            double dbm1 = (ADC1 - 1155.3) / 16.468;
            double dbm2 = (ADC2 - 1145.5) / 16.461;
            double dbm3 = (ADC3 - 1131.4) / 16.272;
            double dbm4 = (ADC4 - 1121) / 16.014;
            double dbm12diff = dbm2 - dbm1;
            double dbm34diff = dbm3 - dbm4;
            double AOAResultVert = dbm12diff / (-0.1462); // dbm12diff = -0.1462 * AOAResult + 0.0000
            double AOAResultHorizontal = dbm34diff / (-0.1462); // dbm34diff = -0.1462 * AOAResult + 0.0000
            degrees[i][0] = AOAResultHorizontal;
            degrees[i][1] = AOAResultVert;
        }
    }

    // TODO: Move Into SolutionFragment, add pixel conversion function to ImageFragment
    private void BeginLoopSyntheticData() {
        // Automatically generates data on a separate thread (-AUTOGEN-)
        Runnable loop = () -> {
            Looper.prepare();
            for (int i = 0; i < 9999; i ++) {
                if (i > degrees.length - 1) {
                    i = 0;
                }
                x = ((int) (fx * Math.tan(Math.PI * degrees[i][0] / 180))) + MainActivity.screenWidth / 2;
                y = ((int) (fy * Math.tan(Math.PI * degrees[i][1] / 180))) + MainActivity.screenHeight / 2;

                // centroid history update
                pairQueue.add(new Pair<>(degrees[i][0], degrees[i][1]));
                if (pairQueue.size() >= centroidHistoryCutoff) {
                    for (int j = 0; j < pairQueue.size(); j++) {
                        avgCentroid[0] += pairQueue.get(j).first;
                        avgCentroid[1] += pairQueue.get(j).second;
                    }
                    avgCentroid[0] /= pairQueue.size();
                    avgCentroid[1] /= pairQueue.size();

                    // Send centroid to Solution Fragment
                    Handler handler = new Handler(Looper.getMainLooper());  // Post updates to LiveData on the main thread
                    handler.post(() -> {
                        solutionViewModel.setAzimuth(avgCentroid[0]);
                        solutionViewModel.setElevation(avgCentroid[1]);
                    });

                    pairQueue.poll();
                }

                squarePlot(x, y);
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        new Thread(loop).start(); // to work in Background
    }

}