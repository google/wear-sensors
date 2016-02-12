// ---------------------------------------------------------------------
// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ---------------------------------------------------------------------

package net.waynepiekarski.wearsensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class BaseActivity extends DeviceActivity {

    public static final int MAX_SENSOR_VALUES = 6;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener listener;
    private Sensor[] sensorArray;
    private int sensorIndex;
    private TextView viewDeviceType;
    private TextView viewSensorType;
    private TextView viewSensorDetails;
    private TextView viewSensorAccuracy;
    private TextView viewSensorRate;
    private TextView viewSensorRaw;
    private BarView[] viewBarArray;
    private LinearLayout viewSensorBarLayout;
    private Button viewSensorNext;
    private Button viewSensorPrev;
    private GraphView viewSensorGraph;
    private RelativeLayout viewMainLayout;
    private DecimalFormat decimalFormat;
    private boolean stopHandler;
    private Handler uiThreadHandler;
    private Object lockSensorRate;
    private Runnable uiRunnableUpdate;
    private int samplesCount;
    private int samplesSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decimalFormat = new DecimalFormat("+@@@@;-@@@@"); // 4 significant figures

        viewDeviceType = (TextView)findViewById(R.id.deviceType);
        viewSensorType = (TextView)findViewById(R.id.sensorType);
        viewSensorDetails = (TextView)findViewById(R.id.sensorDetails);
        viewSensorAccuracy = (TextView)findViewById(R.id.sensorAccuracy);
        viewSensorRate = (TextView)findViewById(R.id.sensorRate);
        viewSensorRaw = (TextView)findViewById(R.id.sensorRaw);
        viewSensorBarLayout = (LinearLayout)findViewById(R.id.sensorBarLayout);
        viewSensorNext = (Button)findViewById(R.id.sensorNext);
        viewSensorPrev = (Button)findViewById(R.id.sensorPrev);
        viewMainLayout = (RelativeLayout)findViewById(R.id.mainLayout);

        viewBarArray = new BarView[MAX_SENSOR_VALUES];
        for (int i = 0; i < viewBarArray.length; i++) {
            viewBarArray[i] = new BarView(this, null);
            viewSensorBarLayout.addView(viewBarArray[i]);
        }

        viewSensorGraph = new GraphView(this, null);
        viewMainLayout.addView(viewSensorGraph, 0); // Add underneath everything else

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
        if (list.size() < 1) {
            Toast.makeText(this, "No sensors returned from getSensorList", Toast.LENGTH_SHORT);
            Logging.fatal("No sensors returned from getSensorList");
        }
        sensorArray = list.toArray(new Sensor[list.size()]);
        for (int i = 0; i < sensorArray.length; i++) {
            Logging.debug("Found sensor " + i + " " + sensorArray[i].toString());
        }
        sensorIndex = 0;
        sensor = sensorArray[sensorIndex];

        // Implement the ability to cycle through the sensor list with next/prev buttons
        viewSensorNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSensor(+1);
            }
        });
        viewSensorPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSensor(-1);
            }
        });

        // Implement a runnable that updates the rate statistics once per second. Note
        // that if we change sensors, it will take 1 second to adjust to the new speed.
        uiThreadHandler = new Handler();
        lockSensorRate = new Object();
        samplesCount = -1;
        samplesSeconds = 0;
        stopHandler = false;
        uiRunnableUpdate = new Runnable() {
            @Override
            public void run() {
                Logging.debug("Updating the UI every second, count is " + samplesCount);
                if (samplesCount == -1)
                    viewSensorRate.setText("Waiting for first sample ...");
                else if (samplesCount == 0) {
                    samplesSeconds ++;
                    viewSensorRate.setText("No update after " + samplesSeconds + " seconds");
                } else {
                    samplesSeconds = 0;
                    viewSensorRate.setText("" + samplesCount + "/sec at " + (1000 / samplesCount) + " msec");
                    samplesCount = 0;
                }

                if (!stopHandler) {
                    uiThreadHandler.postDelayed(this, 1000);
                }
            }
        };
    }

    public void changeSensor(int ofs) {
        sensorIndex += ofs;
        if (sensorIndex >= sensorArray.length)
            sensorIndex = 0;
        else if (sensorIndex < 0)
            sensorIndex = sensorArray.length-1;
        sensor = sensorArray[sensorIndex];
        stopSensor();
        startSensor();
    }

    @Override
    public void onStart() {
        super.onStart();
        startSensor();
        uiThreadHandler.post(uiRunnableUpdate);
    }

    private void startSensor() {
        String type = "#" + (sensorIndex+1) + ", type " + sensor.getType();
        if (Build.VERSION.SDK_INT >= 20)
            type = type + "=" + sensor.getStringType();
        Logging.debug("Opened up " + type + " - " + sensor.toString());
        viewDeviceType.setText(android.os.Build.DEVICE + " " + android.os.Build.ID);
        viewSensorType.setText(type);
        viewSensorDetails.setText(sensor.toString().replace("{Sensor ", "").replace("}", ""));
        for (int i = 0; i < viewBarArray.length; i++) {
            viewBarArray[i].setMaximum(sensor.getMaximumRange());
        }
        viewSensorGraph.resetMaximum(sensor.getMaximumRange());
        viewSensorRaw.setText("Waiting for sensor data ...");
        viewSensorAccuracy.setText("Waiting for sensor accuracy ...");
        samplesCount = 0;

        listener = new SensorEventListener() {

            public String getStrFromFloat(float in) {
                if ((in > -0.00001) && (in < 0.00001))
                    in = 0;
                if (in == Math.rint(in))
                    return Integer.toString((int)in);
                else
                    return decimalFormat.format(in);
            }

            public int min(int a, int b) { if (a < b) { return a; } else { return b; } }

            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == sensor.getType()) {
                    Logging.detailed("Sensor update: " + Arrays.toString(sensorEvent.values));
                    samplesCount++;

                    String raw = "";
                    for (int i = 0; i < sensorEvent.values.length; i++) {
                        String str = getStrFromFloat(sensorEvent.values[i]);
                        if (raw.length() != 0)
                            raw = raw + "\n";
                        raw = raw + str;
                    }
                    viewSensorRaw.setText(raw);

                    if (sensorEvent.values.length != min(sensorEvent.values.length, viewBarArray.length))
                        Logging.debug("Sensor update contained " + sensorEvent.values.length + " which is larger than expected " + viewBarArray.length);
                    for (int i = 0; i < min(sensorEvent.values.length, viewBarArray.length); i++) {
                        viewBarArray[i].setValue(sensorEvent.values[i]);
                    }
                    for (int i = sensorEvent.values.length; i < viewBarArray.length; i++) {
                        viewBarArray[i].setValue(0);
                    }
                    viewSensorGraph.setSize(sensorEvent.values.length);
                    viewSensorGraph.setValues(sensorEvent.values);
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Logging.detailed("Accuracy update: " + accuracy);
                viewSensorAccuracy.setText("Accuracy=" + accuracy);
            }
        };
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopSensor();
        uiThreadHandler.removeCallbacks(uiRunnableUpdate);
    }

    private void stopSensor() {
        sensorManager.unregisterListener(listener);
    }
}
