package com.Huard.PhoneRFFL;

import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

public class SerialMonitor extends Thread implements AutoCloseable {

    private boolean running;
    private StringBuilder searchStr;
    private String msg;

    private final SerialClient client;

    SerialMonitor(SerialClient client) {
        this.client = client;

        SerialClient.initialize();
        running = false;
    }

    public void quit() {
        running = false;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[64];

        running = true;

        searchStr = new StringBuilder();
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(SerialClient.manager);
        UsbSerialDriver driver = availableDrivers.get(0);

        if (SerialClient.device == null)
            SerialClient.device = driver.getDevice();

        if (!SerialClient.manager.hasPermission(SerialClient.device)) {
            Log.e("USB", "Permission denied. USB Connection: " + SerialClient.connection);
            return;
        }

        SerialClient.connection = SerialClient.manager.openDevice(SerialClient.device);
        if (SerialClient.connection == null) {
            Log.e("USB", "Could not open connection. USB Connection: " + null);
            return;
        }

        SerialClient.connection.claimInterface(SerialClient.usbInterface, true);

        List<UsbSerialPort> ports = driver.getPorts();
        UsbSerialPort port = ports.get(0);

        try {
            port.open(SerialClient.connection);
            port.setDTR(true);
            port.setRTS(true);
            port.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int iterCount = 0;
        while(running) {
            synchronized (this) {
                iterCount++;
                if (iterCount == 100)
                    client.write("$|clear\n");  // clear the write buffer

                int receivedBytes = SerialClient.connection.bulkTransfer(SerialClient.inputEndpoint, buffer, buffer.length, 1000);

                if (receivedBytes >= 0) {
                    String receivedData = new String(buffer, 0, receivedBytes);  // Convert the received data to a string
                    //Log.d("USB", "Received Raw: " + receivedData);

                    searchStr.append(receivedData);
                    processMessages();
                }
            }
        }
        close();
    }

    public void processMessages() {
        int idx_header;
        int idx_footer;
        idx_header = searchStr.indexOf("#");

        if (idx_header >= 0) { // truncate anything before the current header
            searchStr = new StringBuilder(searchStr.substring(idx_header));
        }

        idx_header = searchStr.indexOf("#");
        idx_footer = searchStr.indexOf("\n");

        while ((idx_header >= 0) && (idx_footer >= 0)) {
            try {
                msg = searchStr.substring(idx_header, idx_footer);
                //Log.d("USB", "USB message: " + msg);
                updateTerminalStatus(msg);  // send data to SerialClient
            } catch (StringIndexOutOfBoundsException e) {
                Log.e("USB", "Index Out of Bounds Error: " + searchStr);
                return;
            }
            searchStr.delete(0, idx_footer + 1); // Remove processed message from searchStr
            idx_header = searchStr.indexOf("#");
            idx_footer = searchStr.indexOf("\n");
        }
    }

    public void updateTerminalStatus(String message) {
        client.onMessageReceived(message);  // Send Data to SerialClient
    }

    @Override
    public void close() {
        searchStr = null;
        msg = null;
    }
}
