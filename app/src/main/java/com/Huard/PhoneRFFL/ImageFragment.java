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

public class ImageFragment extends Fragment implements ImageReader.OnImageAvailableListener {
    public ImageView imageView;
    private SolutionViewModel solutionViewModel;
    private final Canvas canvas;
    public Bitmap bitmap = Bitmap.createBitmap(MainActivity.screenWidth, MainActivity.screenHeight, Bitmap.Config.ARGB_8888);
    private final LinkedList<PointAOA> azimuthPointsAOA = new LinkedList<>();
    private final LinkedList<PointAOA> elevationPointsAOA = new LinkedList<>();
    private final ColorMapJet colorMap = new ColorMapJet();  // matrix of numerical values for pixel colors
    private double centroidAzimuth = 0f;
    private double centroidElevation = 0f;
    private double sigmaAzimuth;
    private double sigmaElevation;
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
        sideViewModel.getHeatMapSelected().observe(getViewLifecycleOwner(), this::receiveHeatMapSelector);

        solutionViewModel = new ViewModelProvider(requireActivity()).get(SolutionViewModel.class);
        solutionViewModel.getAzimuthPointAOA().observe(getViewLifecycleOwner(), this::receiveAzimuthPointAOA);
        solutionViewModel.getElevationPointAOA().observe(getViewLifecycleOwner(), this::receiveElevationPointAOA);
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {  /* Necessary for smooth camera updates */
        imageReader.acquireLatestImage().close();
    }

    private void receiveAzimuthPointAOA(double AOA_deg) {  // adds AOA point to the Queue for plotting
        azimuthPointsAOA.add(new PointAOA(AOA_deg, PointAOA.Type.AZIMUTH));
    }

    private void receiveElevationPointAOA(double AOA_deg) {  // adds AOA point to the Queue for plotting
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
        updatePlotGaussianEllipse();
        postCentroidSolution();
    }

    private void updatePlotGaussianEllipse() {
        double meanAzimuthPx = convertAngleToPixels(new PointAOA(this.centroidAzimuth, PointAOA.Type.AZIMUTH));
        double meanElevationPx = convertAngleToPixels(new PointAOA(this.centroidElevation, PointAOA.Type.ELEVATION));
        double meanPlusOneSigmaAziPx = convertAngleToPixels(new PointAOA(this.centroidAzimuth + this.sigmaAzimuth, PointAOA.Type.AZIMUTH));
        double meanPlusOneSigmaElevPx = convertAngleToPixels(new PointAOA(this.centroidElevation + this.sigmaElevation, PointAOA.Type.ELEVATION));
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

        // Draw the "X" at the mean position
        paint.setStrokeWidth(6);
        canvas.drawLine((float) (meanX_px - 10), (float) (meanY_px - 10), (float) (meanX_px + 10), (float) (meanY_px + 10), paint);
        canvas.drawLine((float) (meanX_px + 10), (float) (meanY_px - 10), (float) (meanX_px - 10), (float) (meanY_px + 10), paint);
        imageView.invalidate();
    }

    private void postCentroidSolution() {  // Send centroid to Solution Fragment
        Handler handler = new Handler(Looper.getMainLooper());  // Post updates to LiveData on the main thread
        handler.post(() -> {
            solutionViewModel.setCentroidAzimuth(centroidAzimuth);
            solutionViewModel.setCentroidElevation(centroidElevation);
        });
    }

    private void resetBitmap() {
        bitmap.eraseColor(colorMap.getColorByIndex(0));
    }

    private void updateMarginalDistributionAzimuth() {
        removeExpiredPoints(azimuthPointsAOA);

        // Calculate Mean
        double sumAngle = 0;
        for (int i = 0; i<azimuthPointsAOA.size(); i++) {
            sumAngle += azimuthPointsAOA.get(i).getValue();
            centroidAzimuth = sumAngle/azimuthPointsAOA.size();
        }

        // Calculate StdDev
        double sumSquaredDeviations = 0;
        for (PointAOA point : azimuthPointsAOA) {
            sumSquaredDeviations += Math.pow(point.getValue() - centroidAzimuth, 2);
        }
        sigmaAzimuth = Math.sqrt(sumSquaredDeviations / (azimuthPointsAOA.size() - 1)); // Sample standard deviation
    }

    private void updateMarginalDistributionElevation() {
        removeExpiredPoints(elevationPointsAOA);

        // Calculate Mean and StdDev
        double sumAngle = 0;
        double sumSquaredDeviations = 0;
        for (PointAOA point : elevationPointsAOA) {
            sumAngle += point.getValue();
            sumSquaredDeviations += Math.pow(point.getValue() - centroidElevation, 2);
        }
        centroidElevation = sumAngle/elevationPointsAOA.size();
        sigmaElevation = Math.sqrt(sumSquaredDeviations / (elevationPointsAOA.size() - 1)); // Sample standard deviation
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

        // Show ImageView
        if (isHeatMapSelected) {
            imageView.setImageAlpha(255);
        } else {
            imageView.setImageAlpha(0);
        }

        // Clear Bitmap
        if (!isHeatMapSelected) {
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