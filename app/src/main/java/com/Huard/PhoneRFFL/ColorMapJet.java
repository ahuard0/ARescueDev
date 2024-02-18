package com.Huard.PhoneRFFL;

public class ColorMapJet {

    private int[] colors; // colors for the JET heatmap

    public ColorMapJet() {
        generateJetColors();
    }

    private void generateJetColors() {
        colors = new int[14]; // colors for the JET heatmap
        String colorString = "0x00000000, 0x60000088, 0x600000ff, 0x600088ff, 0x6000ffff, 0x6088ff88, 0x60ffff00, 0x60ff8800, 0x60ff0000, 0x60880000, 0x60880000, 0x60880000, 0x60880000, 0x60880000";
        String[] colorsByHexCode = colorString.split(", ");
        for (int i = 0; i < this.length(); i++) {
            colors[i] = Integer.decode(colorsByHexCode[i]);
        }
    }

    public int length() {
        return colors.length;
    }

    public int getColorByIndex(int index) {
        if (index > this.length() - 1)
            index = this.length() - 1;
        if (index < 0)
            index = 0;
        return colors[index];
    }
}
