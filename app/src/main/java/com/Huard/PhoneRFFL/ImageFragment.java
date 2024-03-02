package com.Huard.PhoneRFFL;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Queue;

public class ImageFragment extends Fragment implements ImageReader.OnImageAvailableListener {
    public ImageView imageView;
    private SolutionViewModel solutionViewModel;
    private final Canvas canvas;
    public final Bitmap bitmap = Bitmap.createBitmap(MainActivity.screenWidth, MainActivity.screenHeight, Bitmap.Config.ARGB_8888);
    private final LinkedList<PointAOA> azimuthPointsAOA = new LinkedList<>();
    private final LinkedList<PointAOA> elevationPointsAOA = new LinkedList<>();
    private final ColorMapJet colorMap = new ColorMapJet();  // matrix of numerical values for pixel colors
    private double centroidAOAAzimuth = 0f;
    private double centroidAOAElevation = 0f;
    private double sigmaAOAAzimuth;
    private double sigmaAOAElevation;
    private final double[] valueBufferAzimuth = new double[10];
    private int bufferIndexAzimuth = 0; // Index to keep track of the current position in the circular buffer
    private double rollingSumAzimuth = 0; // Variable to store the sum of the last 10 values
    private double rollingCentroidAOAAzimuth = 0;
    private final double[] valueBufferElevation = new double[10];
    private int bufferIndexElevation = 0; // Index to keep track of the current position in the circular buffer
    private double rollingSumElevation = 0; // Variable to store the sum of the last 10 values
    private double rollingCentroidAOAElevation = 0;
    final float fx = (float) MainActivity.screenWidth; // for matrix transformation
    final float fy = -fx; // for matrix transformation

    public ImageFragment() {
        canvas = new Canvas(bitmap);
        resetBitmap(); // initial bitmap coloring (transparent and almost invisible)
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image, container, false);  // Inflate the layout for this fragment
        imageView = rootView.findViewById(R.id.imageView);

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
        sideViewModel.getEllipseSelected().observe(getViewLifecycleOwner(), this::receiveEllipseSelector);

        solutionViewModel = new ViewModelProvider(requireActivity()).get(SolutionViewModel.class);
        solutionViewModel.getAzimuthPointsAOA().observe(getViewLifecycleOwner(), this::receiveAzimuthPointAOA);
        solutionViewModel.getElevationPointsAOA().observe(getViewLifecycleOwner(), this::receiveElevationPointAOA);
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {  /* Necessary for smooth camera updates */
        imageReader.acquireLatestImage().close();
    }

    private void receiveAzimuthPointAOA(Queue<Double> queue_AOA_deg) {  // adds AOA point to the Queue for plotting
        for (double AOA_deg : queue_AOA_deg)
            azimuthPointsAOA.add(new PointAOA(AOA_deg, PointAOA.Type.AZIMUTH));
    }

    private void receiveElevationPointAOA(Queue<Double> queue_AOA_deg) {  // adds AOA point to the Queue for plotting
        for (double AOA_deg : queue_AOA_deg)
            elevationPointsAOA.add(new PointAOA(AOA_deg, PointAOA.Type.ELEVATION));
    }

    private final Handler uiHandler = new Handler(Looper.getMainLooper()); // UI handler on the main (UI) thread

    private void startBitmapUpdates() {
        uiHandler.post(updateBitmapRunnable);  // Schedule the first update immediately
    }

    private final Runnable updateBitmapRunnable = new Runnable() {  // Define a Runnable for updating the bitmap
        @Override
        public void run() {
            updateBitmap(); // Generate frame
            imageView.setImageBitmap(bitmap); // Update the ImageView UI

            int FPS = 60;
            uiHandler.postDelayed(this, 1000 / FPS);  // Schedule the next update after the specified delay
        }
    };

    private void updateBitmap() {
        resetBitmap();
        updateMarginalDistributionAzimuth();
        updateMarginalDistributionElevation();
        updateRollingAverageCentroidAzimuth(centroidAOAAzimuth);
        updateRollingAverageCentroidElevation(centroidAOAElevation);
        updatePlotGaussianEllipse();
        postCentroidAOASolution();
    }



    private void updatePlotGaussianEllipse() {
        double meanAzimuthPx = convertAngleToPixels(new PointAOA(this.rollingCentroidAOAAzimuth, PointAOA.Type.AZIMUTH));
        double meanElevationPx = convertAngleToPixels(new PointAOA(this.rollingCentroidAOAElevation, PointAOA.Type.ELEVATION));
        double meanPlusOneSigmaAziPx = convertAngleToPixels(new PointAOA(this.rollingCentroidAOAAzimuth + this.sigmaAOAAzimuth, PointAOA.Type.AZIMUTH));
        double meanPlusOneSigmaElevPx = convertAngleToPixels(new PointAOA(this.rollingCentroidAOAElevation + this.sigmaAOAElevation, PointAOA.Type.ELEVATION));
        drawGaussianSigma(this.canvas,
                meanAzimuthPx,
                meanElevationPx,
                meanPlusOneSigmaAziPx - meanAzimuthPx,
                meanPlusOneSigmaElevPx - meanElevationPx);
    }

    private void drawGaussianSigma(Canvas canvas, double meanX_px, double meanY_px, double sigmaX_px, double sigmaY_px) {
        if (canvas == null) return;

        // Create a RectF to define the bounds of the ellipse
        RectF bounds1S = new RectF();
        bounds1S.left = (float) (meanX_px - sigmaX_px);
        bounds1S.top = (float) (meanY_px - sigmaY_px);
        bounds1S.right = (float) (meanX_px + sigmaX_px);
        bounds1S.bottom = (float) (meanY_px + sigmaY_px);

        // Create a RectF to define the bounds of the ellipse
        RectF bounds2S = new RectF();
        bounds2S.left = (float) (meanX_px - 2*sigmaX_px);
        bounds2S.top = (float) (meanY_px - 2*sigmaY_px);
        bounds2S.right = (float) (meanX_px + 2*sigmaX_px);
        bounds2S.bottom = (float) (meanY_px + 2*sigmaY_px);

        // Create a RectF to define the bounds of the ellipse
        RectF bounds0p5S = new RectF();
        bounds0p5S.left = (float) (meanX_px - 0.5*sigmaX_px);
        bounds0p5S.top = (float) (meanY_px - 0.5*sigmaY_px);
        bounds0p5S.right = (float) (meanX_px + 0.5*sigmaX_px);
        bounds0p5S.bottom = (float) (meanY_px + 0.5*sigmaY_px);

        // Create a RectF to define the bounds of the ellipse
        RectF bounds0p25S = new RectF();
        bounds0p25S.left = (float) (meanX_px - 0.25*sigmaX_px);
        bounds0p25S.top = (float) (meanY_px - 0.25*sigmaY_px);
        bounds0p25S.right = (float) (meanX_px + 0.25*sigmaX_px);
        bounds0p25S.bottom = (float) (meanY_px + 0.25*sigmaY_px);

        // Draw the ellipse on the canvas
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);

        // Draw the "X" at the mean position
        paint.setStrokeWidth(6);
        canvas.drawLine((float) (meanX_px - 10), (float) (meanY_px - 10), (float) (meanX_px + 10), (float) (meanY_px + 10), paint);
        canvas.drawLine((float) (meanX_px + 10), (float) (meanY_px - 10), (float) (meanX_px - 10), (float) (meanY_px + 10), paint);

        paint.setStrokeWidth(4);
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{10, 5}, 0);  // Define the dash pattern (10 pixels on, 5 pixels off)
        paint.setPathEffect(dashPathEffect);
        canvas.drawOval(bounds1S, paint);
        canvas.drawOval(bounds2S, paint);
        canvas.drawOval(bounds0p5S, paint);
        canvas.drawOval(bounds0p25S, paint);

        // Draw the "X" at the mean position
        paint.setStrokeWidth(6);
        canvas.drawLine((float) (meanX_px - 10), (float) (meanY_px - 10), (float) (meanX_px + 10), (float) (meanY_px + 10), paint);
        canvas.drawLine((float) (meanX_px + 10), (float) (meanY_px - 10), (float) (meanX_px - 10), (float) (meanY_px + 10), paint);
        imageView.invalidate();
    }

    private void postCentroidAOASolution() {  // Send centroid to Solution Fragment
        Handler handler = new Handler(Looper.getMainLooper());  // Post updates to LiveData on the main thread
        handler.post(() -> {
            solutionViewModel.setCentroidAOAAzimuth(rollingCentroidAOAAzimuth);
            solutionViewModel.setCentroidAOAElevation(rollingCentroidAOAElevation);
        });
    }

    private void resetBitmap() {
        bitmap.eraseColor(colorMap.getColorByIndex(0));
    }

    private void updateRollingAverageCentroidAzimuth(double newValue) {
        if (Double.isNaN(newValue))
            return;

        // Average over time to reduce the number of updates to the GUI
        rollingSumAzimuth += newValue - valueBufferAzimuth[bufferIndexAzimuth]; // Adjust rolling sum by subtracting the value leaving the buffer
        valueBufferAzimuth[bufferIndexAzimuth] = newValue; // Store the new value in the circular buffer
        bufferIndexAzimuth = (bufferIndexAzimuth + 1) % valueBufferAzimuth.length; // Increment buffer index in a circular manner

        // Calculate the rolling average
        rollingCentroidAOAAzimuth = rollingSumAzimuth / valueBufferAzimuth.length;
    }

    private void updateRollingAverageCentroidElevation(double newValue) {
        if (Double.isNaN(newValue))
            return;

        // Average over time to reduce the number of updates to the GUI
        rollingSumElevation += newValue - valueBufferElevation[bufferIndexElevation]; // Adjust rolling sum by subtracting the value leaving the buffer
        valueBufferElevation[bufferIndexElevation] = newValue; // Store the new value in the circular buffer
        bufferIndexElevation = (bufferIndexElevation + 1) % valueBufferElevation.length; // Increment buffer index in a circular manner

        // Calculate the rolling average
        rollingCentroidAOAElevation = rollingSumElevation / valueBufferElevation.length;
    }

    private void updateMarginalDistributionAzimuth() {
        removeExpiredPoints(azimuthPointsAOA);

        // Calculate Mean
        double sumAngle = 0;
        for (int i = 0; i<azimuthPointsAOA.size(); i++) {
            sumAngle += azimuthPointsAOA.get(i).getValue();
            centroidAOAAzimuth = sumAngle/azimuthPointsAOA.size();
        }

        // Calculate StdDev
        double sumSquaredDeviations = 0;
        for (PointAOA point : azimuthPointsAOA) {
            sumSquaredDeviations += Math.pow(point.getValue() - centroidAOAAzimuth, 2);
        }
        sigmaAOAAzimuth = Math.sqrt(sumSquaredDeviations / (azimuthPointsAOA.size() - 1)); // Sample standard deviation
    }

    private void updateMarginalDistributionElevation() {
        removeExpiredPoints(elevationPointsAOA);

        // Calculate Mean and StdDev
        double sumAngle = 0;
        double sumSquaredDeviations = 0;
        for (PointAOA point : elevationPointsAOA) {
            sumAngle += point.getValue();
            sumSquaredDeviations += Math.pow(point.getValue() - centroidAOAElevation, 2);
        }
        centroidAOAElevation = sumAngle/elevationPointsAOA.size();
        sigmaAOAElevation = Math.sqrt(sumSquaredDeviations / (elevationPointsAOA.size() - 1)); // Sample standard deviation
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

    private void receiveEllipseSelector(boolean isEllipseSelected) {  // toggle switch enables or disables heatmap visibility

        // Show ImageView
        if (isEllipseSelected) {
            imageView.setImageAlpha(255);
        } else {
            imageView.setImageAlpha(0);
        }

        // Clear Bitmap
        if (!isEllipseSelected) {
            resetBitmap();
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