package com.Huard.PhoneRFFL;

public class PointAOA {
    private final double value;
    private final long timestamp;
    private final Type type;

    public enum Type {AZIMUTH, ELEVATION}

    public PointAOA(double value, Type type) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
        this.type = type;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Type getType() {
        return type;
    }

    public boolean isOlderThan(long milliseconds) {
        long diff = System.currentTimeMillis() - this.getTimestamp();
        //Log.d("PointAOA", "Value: " + value + ", Type: " + type + ", Timestamp: " + timestamp + ", Expired: " + diff);
        return diff > milliseconds;
    }
}
