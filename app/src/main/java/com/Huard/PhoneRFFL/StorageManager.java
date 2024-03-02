package com.Huard.PhoneRFFL;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

public class StorageManager {
    private static final String FILENAME = "telemetry_data.txt";
    private static final String DIRECTORY_NAME = "TelemetryData";
    private static final int EXPORT_REQUEST_CODE = 1001;
    private final Context mContext;

    public StorageManager(Context context) {
        mContext = context;
    }

    public void saveDataToExternalStorage(String data) {
        File file = getTelemetryFile();
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(data.getBytes());
            fos.close();
        } catch (IOException e) {
            Log.e("StorageManager", "Error saving data to external storage", e);
        }
    }

    public void exportTelemetryData() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, FILENAME);
        ((Activity) mContext).startActivityForResult(intent, EXPORT_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXPORT_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri destinationUri = data.getData();
            try {
                exportFile(destinationUri);
            } catch (IOException e) {
                Log.e("StorageManager", "Error exporting file", e);
            }
        }
    }

    private File getTelemetryFile() {
        File directory = new File(mContext.getExternalFilesDir(null), DIRECTORY_NAME);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e("StorageManager", "Failed to create directory");
            }
        }

        return new File(directory, FILENAME);
    }

    private void exportFile(Uri destinationUri) throws IOException {
        InputStream inputStream = Files.newInputStream(getTelemetryFile().toPath());
        OutputStream outputStream = mContext.getContentResolver().openOutputStream(destinationUri);
        if (outputStream != null) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
        }
    }

}
