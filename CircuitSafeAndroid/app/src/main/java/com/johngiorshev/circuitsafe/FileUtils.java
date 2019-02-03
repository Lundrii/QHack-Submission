package com.johngiorshev.circuitsafe;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

public class FileUtils {
    private FileUtils() {}

    private static String crlf = "\r\n";
    private static String twoHyphens = "--";
    private static String boundary =  "*****";

    interface ConnectionCallBack {
        void success(boolean b);
    }

    public static void sendFile(File f, ConnectionCallBack connectioncb) {
        // THIS METHOD IS BASED OFF:
        // https://stackoverflow.com/a/11826317

        final File file = f;
        final ConnectionCallBack ccb = connectioncb;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    HttpURLConnection conn = (HttpURLConnection)
                            new URL("http://35.230.167.95/test")
                                    .openConnection();
                    conn.setUseCaches(false);
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("Cache-Control", "no-cache");
                    conn.setRequestProperty(
                            "Content-Type", "multipart/form-data;boundary=" + boundary);

                    DataOutputStream request = new DataOutputStream(
                            conn.getOutputStream());

                    request.writeBytes(twoHyphens + boundary + crlf);
                    request.writeBytes("Content-Disposition: form-data; name=\"gerberfile\";filename=\"" +
                            file.getName() + "\"" + crlf);
                    request.writeBytes(crlf);

                    request.write(getBytesFromFile(file));

                    request.writeBytes(crlf);
                    request.writeBytes(twoHyphens + boundary +
                            twoHyphens + crlf);
                    request.flush();
                    request.close();

                    Log.i("FILEMSG" , conn.getResponseMessage());
                    // 200 code is success
                    ccb.success(conn.getResponseCode() == 200);
                    conn.disconnect();
                } catch (Exception e) {
                    ccb.success(false);
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private static byte[] getBytesFromFile(File f) {
        byte[] bytes = new byte[(int)f.length()];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(f));
            buf.read(bytes,0, bytes.length);
            buf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static void sendJSONPost(JSONObject json, ConnectionCallBack connectioncb) {
        final String jsonstr = json.toString();
        BoardDisplay.jsonInput = json;
        final ConnectionCallBack ccb = connectioncb;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://35.230.167.95/compute");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    os.writeBytes(jsonstr);
                    os.flush();
                    os.close();
                    Log.i("JSONSENDMSG" , conn.getResponseMessage());
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }

                    BoardDisplay.jsonOutput = sb.toString();

                    // 200 code is success
                    ccb.success(conn.getResponseCode() == 200);
                    conn.disconnect();
                } catch (Exception e) {
                    ccb.success(false);
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}
