package com.furkank.colordetector.handler;

import android.view.View;
import android.widget.Toast;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.view.TextureView;

import com.furkank.colordetector.model.ColorDefinition;
import com.furkank.colordetector.MainActivity;
import com.furkank.colordetector.firebase.FirebaseReadHandler;

import java.util.ArrayList;

public class ColorDetectHandler {

    Activity activity = null;

    public int red;
    public int green;
    public int blue;

    public int rgb;
    public String hex;

    public String name;
    public String hue;

    private Bitmap bitmap;

    ArrayList<ColorDefinition> colorList = null;

    public ColorDetectHandler(Activity activity) {
        this.activity = activity;
        colorList = new ArrayList<ColorDefinition>();

        reset();
        readColorDefinitions();
    }

    /**
     * Resets all variables in this class
     */
    private void reset() {
        this.red = 0;
        this.green = 0;
        this.blue = 0;
        this.rgb = 0;
        this.hex = "";
        this.name = "";
        this.hue = "";
    }

    /**
     * Reads color definition list from database
     */
    private void readColorDefinitions() {
        FirebaseReadHandler<ColorDefinition> fbHandler = new FirebaseReadHandler<ColorDefinition>(ColorDefinition.class, activity);
        fbHandler.read("color_definitions", this::readCallback);
    }

    /**
     * This is called after firebase read completed
     *
     * @param list
     * @return
     */
    private Integer readCallback(ArrayList<ColorDefinition> list) {
        colorList = list;
        Toast.makeText(activity, "Its ready.", Toast.LENGTH_SHORT).show();
        ((MainActivity) activity).readyToCatch = true;
        return 0;
    }

    /**
     * Convert RGB value to HSL array
     *
     * @param red
     * @param green
     * @param blue
     * @return
     */
    private int[] convertRgbToHsl(int red, int green, int blue) {
        double[] rgb = new double[]{
                (float) red / 255,
                (float) green / 255,
                (float) blue / 255
        };

        double r = rgb[0];
        double g = rgb[1];
        double b = rgb[2];

        double min, max, delta, h, s, l;

        min = Math.min(r, Math.min(g, b));
        max = Math.max(r, Math.max(g, b));
        delta = max - min;
        l = (min + max) / 2;

        s = 0;
        if (l > 0 && l < 1)
            s = delta / (l < 0.5 ? (2 * l) : (2 - 2 * l));

        h = 0;
        if (delta > 0) {
            if (max == r && max != g) h += (g - b) / delta;
            if (max == g && max != b) h += (2 + (b - r) / delta);
            if (max == b && max != r) h += (4 + (r - g) / delta);
            h /= 6;
        }

        double factor = 255.0;

        return new int[]{
                (int) (h * factor),
                (int) (s * factor),
                (int) (l * factor)
        };
    }

    /**
     * Detects the color of pixel where the center of the pointer
     *
     * @param cameraView
     * @param pointer
     */
    public void detect(TextureView cameraView, View pointer) {
        // Reset all variables before to detect new color
        reset();

        // Get the coordinate of pointer
        // Add half of width and half of height to x and y
        // to be able to target the center of the pointer
        float x = pointer.getX() + (float)(pointer.getWidth() / 2);
        float y = pointer.getY() + (float)(pointer.getHeight() / 2);

        // Create a bitmap using camera view
        // # Note: It is important that bitmap size should
        // # be equals to or be greater than pointer size
        bitmap = cameraView.getBitmap();

        // Get the pixel of pointer using its x and y coordinate
        int pixel = bitmap.getPixel((int) x, (int) y);

        // Get RGB - r (red), g (green) and b (blue) - colors of the pixel
        red = Color.red(pixel);
        green = Color.green(pixel);
        blue = Color.blue(pixel);

        // This is the color which can be used to set color of something directly
        // # Example: set an image view's color
        // # ImageView.setBackgroundColor(rgb)
        rgb = Color.rgb(red, green, blue);

        // Get hex code of RGB
        hex = "#" + Integer.toHexString(rgb & 0x00ffffff);

        // Get and set color name using hex
        setColorName(red, green, blue, hex);
    }

    /**
     * Finds color name by hex and sets into variable
     *
     * @param hex
     */
    private void setColorName(int r, int g, int b, String hex) {
        // Get HSL of color
        int[] hsl = convertRgbToHsl(r, g, b);
        int h = hsl[0];
        int s = hsl[1];
        int l = hsl[2];

        int ndf = 0, ndf1 = 0, ndf2 = 0;
        double df = -1;
        int cl = -1;

        int size = colorList.size();
        for (int i = 0; i < colorList.size(); i++) {
            // Get the object
            ColorDefinition colorObject = colorList.get(i);

            // If hex matches with any color in the list
            // then set name and hue, and return nothing
            if (colorObject.hex.equals(hex)) {
                this.name = colorObject.name;
                this.hue = colorObject.hue;
                return;
            }

            int c_r = Integer.parseInt(colorObject.r);
            int c_g = Integer.parseInt(colorObject.g);
            int c_b = Integer.parseInt(colorObject.b);

            int c_h = Integer.parseInt(colorObject.h);
            int c_s = Integer.parseInt(colorObject.s);
            int c_l = Integer.parseInt(colorObject.l);

            ndf1 = (int) Math.pow(r - c_r, 2) + (int) Math.pow(g - c_g, 2) + (int) Math.pow(b - c_b, 2);
            ndf2 = Math.abs((int) Math.pow(h - c_h, 2)) + (int) Math.pow(s - c_s, 2) + Math.abs((int) Math.pow(l - c_l, 2));
            ndf = ndf1 + ndf2 * 2;
            if (df < 0 || df > ndf) {
                df = ndf;
                cl = i;
            }
        }

        ColorDefinition colorObject = colorList.get(cl);
        this.hex = colorObject.hex;
        this.name = colorObject.name;
        this.hue = colorObject.hue;
    }
}
