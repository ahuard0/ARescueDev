package com.Huard.PhoneRFFL;

import java.util.ArrayList;

public class TerminalManager {

    public enum MeasurementType {
        AZIMUTH,
        ELEVATION,
        UNDETERMINED
    }

    public static Triplet<ArrayList<Integer>, MeasurementType, Integer> parseMessage(String message) {
        // Message will be of the form: #|1234|MON|10|3,4|123,573|4312
        //      # is a delimiter.
        //      1234 is a index.
        //      MON is a message identifier
        //      10 is the sample number in a burst
        //      3,4 represents a list of monitored channels.
        //      123,573 represents the corresponding ADC data.
        //      4312 is a checksum of the message string

        ArrayList<Integer> adcData = new ArrayList<>(2);
        ArrayList<Integer> adcChan = new ArrayList<>(2);
        String[] parts = message.split("\\|");

        // Step 1:  Parse Index Number
        String indexNumber = parts[1];
        int index = Integer.parseInt(indexNumber);

        // Step 2:  Parse message identifier token
        String identifier = parts[2];
        if (identifier.equals("MON")) {

            // Step 3:  Parse channel numbers
            String[] channelNumbers = parts[4].split(",");
            for (String value : channelNumbers) {
                adcChan.add(Integer.parseInt(value));
            }

            // Step 4: Determine Azimuth or Elevation
            MeasurementType measurementType;
            if (adcChan.get(0)==2 && adcChan.get(1)==3) {
                measurementType = MeasurementType.AZIMUTH;
            } else if (adcChan.get(0)==0 && adcChan.get(1)==1) {
                measurementType = MeasurementType.ELEVATION;
            } else {
                measurementType = MeasurementType.UNDETERMINED;
            }

            // Step 5: Parse ADC Data
            String[] adcValues = parts[5].split(",");
            for (String value : adcValues) {
                adcData.add(Integer.parseInt(value));
            }

            return new Triplet<>(adcData, measurementType, index);
        }
        else
            return null;
    }
}
