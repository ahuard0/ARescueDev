package com.Huard.PhoneRFFL;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

public class CorrectionStorage {

    private static final String FILENAME = "correction_settings.txt";
    private static final String DIRECTORY_NAME = "CorrectionSettings";
    private final Context mContext;

    public CorrectionStorage(Context context) {
        mContext = context;
    }

    public void Save(boolean invertAzimuth, boolean invertElevation, double corrAzimuth, double corrElevation) {
        String data = String.format(Locale.getDefault(), "%b,%b,%.4f,%.4f\n", invertAzimuth, invertElevation, corrAzimuth, corrElevation);
        saveDataToExternalStorage(data);
    }

    public void Save(CorrectionSettings settings) {
        this.Save(settings.invertAzimuth, settings.invertElevation, settings.corrAzimuth, settings.corrElevation);
    }

    public CorrectionSettings Load() {
        String loadedData = loadDataFromExternalStorage();
        if (loadedData == null)
            return null;
        if (loadedData.isEmpty())
            return null;

        String[] parts = loadedData.split(",");
        CorrectionSettings result = new CorrectionSettings();
        if (parts.length == 4) {
            result.invertAzimuth = Boolean.parseBoolean(parts[0]);
            result.invertElevation = Boolean.parseBoolean(parts[1]);
            result.corrAzimuth = Double.parseDouble(parts[2]);
            result.corrElevation = Double.parseDouble(parts[3]);
            return result;
        } else {
            Log.e("CorrectionStorage", "Failed to parse saved correction settings file data.");
            return null;
        }
    }

    public void saveDataToExternalStorage(String data) {
        if (!isExternalStorageWritable()) {
            Log.e("CorrectionStorage", "External storage not available");
            return;
        }

        File file = getStorageFile();
        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            fos.write(data.getBytes());
            Log.i("CorrectionStorage", "Data saved to external storage");
        } catch (IOException e) {
            Log.e("CorrectionStorage", "Error saving data to external storage", e);
        }
    }

    public String loadDataFromExternalStorage() {
        if (!isExternalStorageReadable()) {
            Log.e("CorrectionStorage", "External storage not available or readable");
            return null;
        }

        File file = getStorageFile();
        if (file.exists()) {
            StringBuilder stringBuilder = new StringBuilder();
            try (FileInputStream fis = new FileInputStream(file);
                 InputStreamReader inputStreamReader = new InputStreamReader(fis);
                 BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                Log.i("CorrectionStorage", "Data read from external storage");
                return stringBuilder.toString();
            } catch (FileNotFoundException e) {
                Log.e("CorrectionStorage", "File not found", e);
            } catch (IOException e) {
                Log.e("CorrectionStorage", "Error reading data from external storage", e);
            }
        }

        return null;
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    private File getStorageFile() {
        File directory = new File(mContext.getExternalFilesDir(null), DIRECTORY_NAME);
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e("CorrectionStorage", "Failed to create directory");
        }
        return new File(directory, FILENAME);
    }
}
