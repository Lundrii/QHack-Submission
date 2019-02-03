package com.johngiorshev.circuitsafe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    // Ignore when the file picker UI closes when resetting error message text.
    boolean ignoreNextResume = false;
    Button  pickerButton = null;
    TextView errorView;

    File holderFile = null;

    // Two connections occur. Json and file upload.
    // 0 means other process didn't finish yet.
    // 1 means other process was a success
    // -1 means other process failed
    // Connection status variable is used to show connection error to user.
    int connectionStatus = 0;

    public Activity getInstance() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Background Image source modified from:
        // https://img-aws.ehowcdn.com/877x500p/photos.demandstudios.com/getty/article/97/247/89676655.jpg
        // In Photoshop CC, only a simple gaussian blur and
        // histogram adjustment (to lighten the image) was used

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_main);

        // Lock screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);

        pickerButton = (Button)findViewById(R.id.filePickerButton);
        pickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ignoreNextResume = true;
                openFilePickerUI();
            }
        });

        errorView = (TextView)findViewById(R.id.errorMessage);

        Button submitButton = (Button)findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickerButton.isEnabled()) {
                    errorView.setText("File hasn't been selected!");
                    errorView.setVisibility(View.VISIBLE);
                    return; // file hasn't been selected yet
                }

                // CHECK IF OTHER VALUES AREN'T BLANK
                Double voltage = getValue((EditText)findViewById(R.id.voltageInput));
                Double current = getValue((EditText)findViewById(R.id.currentInput));
                Double dist = getValue((EditText)findViewById(R.id.minDistInput));
                Double width = getValue((EditText)findViewById(R.id.minWidthInput));
                if (voltage <= 0 || current <= 0 || dist <= 0 || width <= 0) {
                    errorView.setText("Inputs must be positive non-zero values!");
                    errorView.setVisibility(View.VISIBLE);
                    return;
                }

                connectionStatus = 0; //reset status.

                FileUtils.ConnectionCallBack ccb = new FileUtils.ConnectionCallBack() {
                    String errorMessage = "An error has occurred when connecting to the server!\n" +
                            "Try again later.";
                    private void showError() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Purposeful error text change.
                                if (errorView.getText().toString().contains(errorMessage)) {
                                    errorView.setText(errorView.getText().toString() + ".");
                                } else {
                                    errorView.setText(errorMessage);
                                    errorView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    @Override
                    public void success(boolean b) {
                        if (connectionStatus==-1) {
                            return; // Other process failed and printed error message
                        }
                        if (connectionStatus==1) {
                            // Other process succeeded
                            if (b) {
                                // both succeeded
                                switchToBoard();
                            } else {
                                // Other succeeded but this one failed
                                showError();
                            }
                        }
                        if (connectionStatus==0) {
                            // Other process hasn't finished yet
                            if (b) {
                                connectionStatus = 1;
                            } else {
                                connectionStatus = -1;
                                showError();
                            }
                        }

                    }
                };

                JSONObject obj = new JSONObject();
                try {
                    obj.put("voltage", voltage);
                    obj.put("current", current);
                    obj.put("min_dist", dist);
                    obj.put("min_width", width);

                    FileUtils.sendJSONPost(obj, ccb);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                FileUtils.sendFile(holderFile, ccb);


                // Switch to other activity.

            }
        });
    }

    private void switchToBoard() {
        Intent intent = new Intent(MainActivity.this, BoardDisplay.class);
        startActivity(intent);
    }

    private static Double getValue(EditText et) {
        String content = et.getText().toString();
        try {
            Double d = Double.parseDouble(content);
            return d;
        } catch (Exception e) {
            return new Double(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ignoreNextResume) {
            ignoreNextResume = false;
            return;
        }
        // Hide error message if there hasn't been an error yet
        TextView errorView = (TextView)findViewById(R.id.errorMessage);
        errorView.setVisibility(View.GONE);
        pickerButton.setEnabled(true);
        pickerButton.setText("CHOOSE A GERBER FILE");
    }

    private static final int REQUESTCODE = 42069;
    private static final String[] VALIDEXTENSIONS = new String[] {"dri","gbl","gbo","gbs","gml",
            "gpi","gtl","gto","gtp","gts","txt"};

    public void openFilePickerUI() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // All files
        intent.setType("*/*");
        startActivityForResult(intent, REQUESTCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == REQUESTCODE && resultCode == Activity.RESULT_OK && resultData != null) {
            Intent intent = new Intent(getBaseContext(), BoardDisplay.class);

            // This doesn't work.
            // String path = resultData.getData().getPath();

            String fakePath = resultData.getData().getPath();

            // Check if extension is valid
            // Parsing:
            // Get text after last '.'
            // Bring to lowercase
            String extension = fakePath.substring(fakePath.lastIndexOf(".") + 1)
                    .toLowerCase();
            if (Arrays.asList(VALIDEXTENSIONS).contains(extension)) {
                // Write file to place dir where app has control. Only place app can access file object
                try {
                    holderFile = new File(getFilesDir() + "placeholderfile");
                    InputStream in = getContentResolver().openInputStream(resultData.getData());
                    OutputStream out = new FileOutputStream(holderFile, false);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();
                    pickerButton.setText("File Selected: "
                            + fakePath.substring(fakePath.lastIndexOf("/") + 1));
                    errorView.setVisibility(View.GONE);
                    pickerButton.setEnabled(false);

                    BoardDisplay.fileName = fakePath.substring(fakePath.lastIndexOf("/")+1);
                } catch (Exception e) {
                    // nah
                }

                String realPath = holderFile.getAbsolutePath();
            } else {
                errorView.setText("Invalid File Extension.\n" +
                        "Valid extensions include \"gtl\" or \"gbl\".\nChoose a different file.");
                errorView.setVisibility(View.VISIBLE);
            }
        }
    }


}
