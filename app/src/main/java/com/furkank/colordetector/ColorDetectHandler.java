package com.furkank.colordetector;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class ColorDetectHandler {

    Activity activity = null;

    protected int red;
    protected int green;
    protected int blue;

    protected int rgb;
    protected String hex;

    protected String name;
    protected String hue;

    private Bitmap bitmap;

    ArrayList<JSONObject> colorList = null;

    public ColorDetectHandler(Activity activity) {
        this.activity = activity;
        colorList = new ArrayList<JSONObject>();

        reset();
        readColorInformationList();
    }

    /**
     * Detects the color of pixel where the center of the pointer
     *
     * @param cameraView
     * @param pointer
     */
    protected void detect(TextureView cameraView, View pointer) {
        // Reset all variables before to detect new color
        reset();

        // Get the coordinate of pointer
        float x = pointer.getX();
        float y = pointer.getY();

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
        int[] hsl = convertRgbToHsl(hex);
        int h = hsl[0];
        int s = hsl[1];
        int l = hsl[2];

        int ndf = 0, ndf1 = 0, ndf2 = 0;
        double df = -1;
        int cl = -1;

        for (int i = 0; i < colorList.size(); i++) {
            try {
                // Get the object
                JSONObject colorObject = colorList.get(i);

                // If hex matches with any color in the list
                // then set name and hue, and return nothing
                if (colorObject.getString("hex").equals(hex)) {
                    name = colorObject.getString("name");
                    hue = colorObject.getString("hue");
                    return;
                }

                int c_r = Integer.parseInt(colorObject.getString("r"));
                int c_g = Integer.parseInt(colorObject.getString("g"));
                int c_b = Integer.parseInt(colorObject.getString("b"));

                int c_h = Integer.parseInt(colorObject.getString("h"));
                int c_s = Integer.parseInt(colorObject.getString("s"));
                int c_l = Integer.parseInt(colorObject.getString("l"));

                ndf1 = (int) Math.pow(r - c_r, 2) + (int) Math.pow(g - c_g, 2) + (int) Math.pow(b - c_b, 2);
                ndf2 = Math.abs((int) Math.pow(h - c_h, 2)) + (int) Math.pow(s - c_s, 2) + Math.abs((int) Math.pow(l - c_l, 2));
                ndf = ndf1 + ndf2 * 2;
                if (df < 0 || df > ndf) {
                    df = ndf;
                    cl = i;
                }

            } catch (JSONException exception) {
                Toast.makeText(activity, "An error has occurred while getting color name", Toast.LENGTH_SHORT).show();
            }
        }

        try {
            JSONObject colorObject = colorList.get(cl);
            this.hex = colorObject.getString("hex");
            name = colorObject.getString("name");
            hue = colorObject.getString("hue");
        } catch (JSONException exception) {
            Toast.makeText(activity, "An error has occurred while getting color name", Toast.LENGTH_SHORT).show();
        }
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
     * Reads color names, and hex information from file
     */
    private void readColorInformationList() {

        Scanner sc = null;
        InputStream inputStream = null;

        String json = "";

        try {
            // Get the json file
            inputStream = activity.getAssets().open("colors.json");
            sc = new Scanner(inputStream, "UTF-8");

            // Read
            while (sc.hasNextLine()) {
                json += sc.nextLine();
            }

            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                Toast.makeText(activity, "An error has occurred while reading color list", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(activity, "An error has occurred while reading color list", Toast.LENGTH_SHORT).show();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Toast.makeText(activity, "An error has occurred while reading color list", Toast.LENGTH_SHORT).show();
                }
            }
            if (sc != null) {
                sc.close();
            }
        }

        // Convert json string to array
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                colorList.add(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            Toast.makeText(activity, "Color list file is corrupted", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Convert RGB value to HSL array
     *
     * @param hex
     * @return
     */
    private int[] convertRgbToHsl(String hex) {
        double[] rgb = new double[]{
                (float) Integer.decode("0x" + hex.substring(1, 3)) / 255,
                (float) Integer.decode("0x" + hex.substring(3, 5)) / 255,
                (float) Integer.decode("0x" + hex.substring(5, 7)) / 255
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
}
