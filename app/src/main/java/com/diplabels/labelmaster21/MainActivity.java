package com.diplabels.labelmaster21;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Application for verification of genuine of a specially designed label. This version works with REST API. Find more on github.com/PDymala
 *
 * @author Piotr Dymala p.dymala@gmail.com
 * @version 2.1
 * @since 2020-06-03
 */


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LabelMaster";
    Button buttonScan;
    Button buttonReset;

    TextView textCodesVerif;
    View viewColoredBar;
    View viewColoredBar2;
    private String codeValue = "";

    // REST API, scanned label will be added to this string
    //  String restApiUrl = "http://localhost:8080/LabelMaster2/restapi/standardlabel/checklabel/";
    String restApiUrl = "http://192.168.0.22:8080/LabelMaster2/restapi/standardlabel/checklabel/";
    //


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        permission();


    }

    private void permission() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.CAMERA
                    },
                    100);

        }


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.INTERNET
                    },
                    100);

        }

    }


    public void init() {

        buttonScan = findViewById(R.id.buttonScanQR);

        buttonReset = findViewById(R.id.buttonReset);
        textCodesVerif = findViewById(R.id.textCodesVerif);
        viewColoredBar = findViewById(R.id.colored_bar);
        viewColoredBar2 = findViewById(R.id.colored_bar2);

        viewColoredBar.setBackgroundColor(Color.WHITE);
        viewColoredBar2.setBackgroundColor(Color.WHITE);


    }

    public void scanCode(View view) {


        int LAUNCH_SECOND_ACTIVITY = 100;
        Intent i = new Intent(this, CameraActivity.class);
        startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);


    }


    public void checkCode() {


        if (!codeValue.equals(null) && !codeValue.equals("")) {
            Toast.makeText(this, "Checking...", Toast.LENGTH_SHORT).show();


            new JsonTask().execute(restApiUrl + codeValue);


        } else {
            Toast.makeText(this, "Empty or no code to check", Toast.LENGTH_SHORT).show();

        }


    }

    public void reset(View view) {
        codeValue = "";
        viewColoredBar.setBackgroundColor(Color.WHITE);
        viewColoredBar2.setBackgroundColor(Color.WHITE);
        textCodesVerif.setText("Scan code");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {

                codeValue = data.getStringExtra("result");

                checkCode();


            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Scanning canceled", Toast.LENGTH_SHORT).show();

            }

        }

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line);

                }

                return buffer.toString();


            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error has occured", Toast.LENGTH_SHORT).show();

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error has occured", Toast.LENGTH_SHORT).show();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            if (result.equals(Integer.toString(1))) {
                textCodesVerif.setText("Code is in database");
                viewColoredBar.setBackgroundColor(Color.GREEN);
                viewColoredBar2.setBackgroundColor(Color.GREEN);
            } else if (result.equals(Integer.toString(0))) {
                textCodesVerif.setText("Code is not in database");
                viewColoredBar.setBackgroundColor(Color.RED);

                viewColoredBar2.setBackgroundColor(Color.RED);

            } else {
                textCodesVerif.setText("Error has occured");
                viewColoredBar.setBackgroundColor(Color.WHITE);

                viewColoredBar2.setBackgroundColor(Color.WHITE);


            }

            //  Log.i(TAG, result);


        }
    }
}




