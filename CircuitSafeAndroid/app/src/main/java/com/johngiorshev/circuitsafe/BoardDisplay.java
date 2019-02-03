package com.johngiorshev.circuitsafe;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;

public class BoardDisplay extends AppCompatActivity {

    // e.g. {"currentRating":0.07263097538259303,"otherProperty":58585}
    // Content is only one level deep.
    static String jsonOutput = null;
    static JSONObject jsonInput = null;
    static String fileName = null;

    public static final HashMap<String, String> UNITS = new HashMap<String, String>() {{
        put("currentRating", "amps");
        put("electricField", "newtons/coloumb");
        put("signalSpeed", "meters/second");
        put("propDelay", "seconds");
        put("tempDelta", "°C");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_board_display);

        // Lock screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        final ImageView imageView = (ImageView)findViewById(R.id.imageView);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://35.230.167.95/output.png");
                    final Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            imageView.setImageBitmap(bmp);
                            TextView longTextInput = (TextView)findViewById(R.id.longTextInput);
                            longTextInput.setText("");
                            longTextInput.append("File: \"" + fileName + "\"\n");

                            try {
                                longTextInput.append("Minimum Distance (mm): " + jsonInput.getString("min_dist") + "\n");
                                longTextInput.append("Minimum Width (mm): " + jsonInput.getString("min_width") + "\n");
                                longTextInput.append("Voltage (Volts): " + jsonInput.getString("voltage") + "\n");
                                longTextInput.append("Current (Amps): " + jsonInput.getString("current") + "\n");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            TextView longTextOutput = (TextView)findViewById(R.id.longTextOutput);
                            longTextOutput.setText("");
                            String pairs[] = jsonOutput.substring(1, jsonOutput.length()-1).split(",");
                            for (String s : pairs) {
                                String[] pair = s.split(":");
                                String property = pair[0].substring(1, pair[0].length() - 1);

                                String unit = UNITS.get(property);
                                Log.d("OUTPUTS", s);

                                // Capitalize first letter
                                property = Character.toUpperCase(property.charAt(0))
                                        + property.substring(1);
                                // StringBuilder should be used here but meh
                                for (int i = property.length() - 1; i > 0; i--) {
                                    if (Character.isUpperCase(property.charAt(i))) {
                                        property = property.substring(0, i) + " " +
                                                property.substring(i, property.length());
                                    }
                                }
                                String value = pair[1];
                                Double dVal = Double.parseDouble(value);
                                if (Math.abs(dVal) > 1000000000) {
                                    dVal /= 1000000000;
                                    unit = "giga" + unit;
                                } else if (Math.abs(dVal) > 1000000) {
                                    dVal /= 1000000;
                                    unit = "mega" + unit;
                                } else if (Math.abs(dVal) > 1000) {
                                    dVal /= 1000;
                                    unit = "kilo" + unit;
                                } else if (Math.abs(dVal) < 0.000000001) {
                                    dVal *= 1000000000;
                                    unit = "nano" + unit;
                                } else if (Math.abs(dVal) < 0.000001) {
                                    dVal *= 1000000;
                                    unit = "micro" + unit;
                                } else if (Math.abs(dVal) < 0.001) {
                                    dVal *= 1000;
                                    unit = "milli" + unit;
                                }
                                value = "" + dVal;
                                //Remove needless decimal places. Leaves ~3 places after decimal place.
                                if (value.length() > 6) {
                                    value = value.substring(0, Math.min(s.length(), 6));
                                }
                                if (value.length() > 10) {
                                    value = value.substring(0, 5);
                                }
                                longTextOutput.append(property + ": " + value + " " +
                                        (unit==null ? "" : unit) + "\n");
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
