package com.Huard.PhoneRFFL;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
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
    public Bitmap bitmap = Bitmap.createBitmap(MainActivity.screenWidth, MainActivity.screenHeight, Bitmap.Config.ARGB_8888);
    private final LinkedList<PointAOA> azimuthPointsAOA = new LinkedList<>();
    private final LinkedList<PointAOA> elevationPointsAOA = new LinkedList<>();
    private HandlerThread handlerThread;
    private Handler workerHandler;  // Worker thread
    private final int FPS = 30;
    private final ColorMapJet colorMap = new ColorMapJet();
    private int[][] valueMatrix; // matrix of numerical values for pixel colors
    private final LinkedList<Integer> x_marginal = new LinkedList<>();
    private final LinkedList<Integer> y_marginal = new LinkedList<>();
    private int sumX = 0;
    private int sumY = 0;
    private int pointCount = 0;
    private double centroidAzimuth = 0f;
    private double centroidElevation = 0f;
    private double centroidX = 0f;
    private double centroidY = 0f;
    final float fx = (float) MainActivity.screenWidth; // for matrix transformation
    final float fy = (float) -fx; // for matrix transformation
    private SolutionViewModel solutionViewModel;
    private boolean isHeatMapSelected = false;

    public ImageFragment() {
        resetPlotMatrix();
        resetBitmap(); // initial bitmap coloring (transparent and almost invisible)
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handlerThread.quit();  // Quit the worker thread when the fragment's view is destroyed
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);  // Inflate the layout for this fragment
        imageView = rootView.findViewById(R.id.imageView);

        // Create and start the worker thread
        handlerThread = new HandlerThread("WorkerThread");
        handlerThread.start();
        workerHandler = new Handler(handlerThread.getLooper());

        startBitmapUpdates();  // Image Update Loop

        return rootView;
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
        sideViewModel.getHeatMapSelected().observe(getViewLifecycleOwner(), this::receiveHeatMapSelector);

        solutionViewModel = new ViewModelProvider(requireActivity()).get(SolutionViewModel.class);
        solutionViewModel.getAzimuthPointAOA().observe(getViewLifecycleOwner(), this::receiveAzimuthPointAOA);
        solutionViewModel.getElevationPointAOA().observe(getViewLifecycleOwner(), this::receiveElevationPointAOA);
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        imageReader.acquireLatestImage().close();
    }

    private void receiveAzimuthPointAOA(double AOA_deg) {  // adds AOA point to the Queue for plotting
        azimuthPointsAOA.add(new PointAOA(AOA_deg, PointAOA.Type.AZIMUTH));
    }

    private void receiveElevationPointAOA(double AOA_deg) {  // adds AOA point to the Queue for plotting
        elevationPointsAOA.add(new PointAOA(AOA_deg, PointAOA.Type.ELEVATION));
    }

    private final Handler uiHandler = new Handler(msg -> {  // asynchronous handler to update the bitmap image
        if (msg.what == 1 && bitmap != null) {
            imageView.setImageBitmap(bitmap);  // Update the ImageView UI
        }
        return true;
    });

    private void startBitmapUpdates() {
        workerHandler.post(new Runnable() {  // bitmap update loop
            @Override
            public void run() {
                while(true) {
                    updateBitmap();  // generate frame
                    uiHandler.sendEmptyMessage(1);  // Notify UI thread to update the image view
                    workerHandler.postDelayed(this, (long) 1000 / FPS); // 30.3 FPS, schedule the next update
                }
            }
        });
    }

    private void updateBitmap() {
        resetBitmap();
        resetPlotMatrix();
        resetPixelCentroid();
        updateMarginalDistributionAzimuth();
        updateMarginalDistributionElevation();
        updatePlotMatrixCentroidAndBitmap();
        postCentroidSolution();
    }

    private void postCentroidSolution() {  // Send centroid to Solution Fragment
        Handler handler = new Handler(Looper.getMainLooper());  // Post updates to LiveData on the main thread
        handler.post(() -> {
            solutionViewModel.setCentroidAzimuth(centroidAzimuth);
            solutionViewModel.setCentroidElevation(centroidElevation);
            solutionViewModel.setCentroidX(centroidX);
            solutionViewModel.setCentroidY(centroidY);
            if (!this.isHeatMapSelected) {
                Canvas canvas = new Canvas(bitmap);

                Paint paint = new Paint();
                paint.setColor(Color.RED); // Set the color

                canvas.drawCircle((float) centroidX, (float) centroidY, 30, paint);
                canvas.setBitmap(null);
            }
        });
    }

    private void resetBitmap() {
        bitmap.eraseColor(colorMap.getColorByIndex(0));
    }

    private void resetPlotMatrix() {  // sets the plot matrix to zero
        valueMatrix = new int[MainActivity.screenHeight][MainActivity.screenWidth];
    }

    private void resetPixelCentroid() {
        sumX = 0;
        sumY = 0;
        pointCount = 0;
        centroidX = 0;
        centroidY = 0;
    }

    private void updatePlotMatrixCentroidAndBitmap() {  // generates the intersection density of x-y marginal distributions
        for (int i=0; i<x_marginal.size(); i++) {
            for (int j=0; j<y_marginal.size(); j++) {
                addPixelToBitmap(x_marginal.get(i), y_marginal.get(j));  // PIXEL SIZE: 10px
                addPixelToCentroid(x_marginal.get(i), y_marginal.get(j));
            }
        }
    }

    private void addPixelToCentroid(int x, int y) {
        sumX += x;
        sumY += y;
        pointCount++;
        centroidX = (double)sumX/pointCount;
        centroidY = (double)sumY/pointCount;
    }

    private void addPixelToBitmap(int x, int y) {
        int left = Math.max(x - 30 /2, 0);
        int up = Math.max(y - 30 /2, 0);
        int right = Math.min(x + 30 /2, MainActivity.screenWidth);
        int bot = Math.min(y + 30 /2, MainActivity.screenHeight);

        for (int i = left; i <= right; i++) {
            for (int j = up; j <= bot; j++) {
                valueMatrix[j][i]++;  // increment color map intensity
                bitmap.setPixel(i, j, colorMap.getColorByIndex(valueMatrix[j][i]));  // draw pixel with color
            }
        }
    }

    private void updateMarginalDistributionAzimuth() {
        removeExpiredPoints(azimuthPointsAOA);
        x_marginal.clear();
        double sumAngle = 0;
        for (int i = 0; i<azimuthPointsAOA.size(); i++) {
            x_marginal.add(convertAngleToPixels(azimuthPointsAOA.get(i)));
            sumAngle += azimuthPointsAOA.get(i).getValue();
            centroidAzimuth = sumAngle/azimuthPointsAOA.size();
        }
    }

    private void updateMarginalDistributionElevation() {
        removeExpiredPoints(elevationPointsAOA);
        y_marginal.clear();
        double sumAngle = 0;
        for (int i = 0; i<elevationPointsAOA.size(); i++) {
            y_marginal.add(convertAngleToPixels(elevationPointsAOA.get(i)));
            sumAngle += elevationPointsAOA.get(i).getValue();
            centroidElevation = sumAngle/elevationPointsAOA.size();
        }
    }

    private int convertAngleToPixels(PointAOA pointAOA) {
        if (pointAOA.getType() == PointAOA.Type.AZIMUTH) {
            return ((int) (fx * Math.tan(Math.PI * pointAOA.getValue() / 180))) + MainActivity.screenWidth / 2;
        } else if (pointAOA.getType() == PointAOA.Type.ELEVATION) {
            return ((int) (fy * Math.tan(Math.PI * pointAOA.getValue() / 180))) + MainActivity.screenHeight / 2;
        } else {
            throw new IllegalArgumentException("Unsupported PointAOA type: " + pointAOA.getType());
        }
    }

    private void removeExpiredPoints(LinkedList<PointAOA> pointsList) {  // modifies reference object pointsList
        for (int i = 0; i < pointsList.size(); i++) {
            if (pointsList.get(i).isOlderThan(2500)) {  // Remove old points with a 2500 lifetime duration
                pointsList.remove(i);
                i--; // Decrement i because elements are shifted after removal
            }
        }
    }

    private void receiveHeatMapSelector(boolean isHeatMapSelected) {  // toggle switch enables or disables heatmap visibility
        this.isHeatMapSelected = isHeatMapSelected;
        if (this.isHeatMapSelected) {
            imageView.setImageAlpha(255);
        } else {
            imageView.setImageAlpha(0);
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
}