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

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class BaseActivity extends Activity {

    public static final int MAX_SENSOR_VALUES = 6;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener listener;
    private Sensor[] sensorArray;
    private int sensorIndex;
    private TextView viewSensorType;
    private TextView viewSensorDetails;
    private TextView viewSensorAccuracy;
    private TextView viewSensorRaw;
    private BarView[] viewBarArray;
    private LinearLayout viewSensorBarLayout;
    private Button viewSensorNext;
    private Button viewSensorPrev;
    private GraphView viewSensorGraph;
    private RelativeLayout viewMainLayout;
    private DecimalFormat decimalFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        decimalFormat = new DecimalFormat("+@@@@;-@@@@"); // 4 significant figures

        viewSensorType = (TextView)findViewById(R.id.sensorType);
        viewSensorDetails = (TextView)findViewById(R.id.sensorDetails);
        viewSensorAccuracy = (TextView)findViewById(R.id.sensorAccuracy);
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
        if (list.size() < 1)
            Logging.fatal("No sensors returned from getSensorList");
        sensorArray = list.toArray(new Sensor[list.size()]);
        for (int i = 0; i < sensorArray.length; i++) {
            Logging.debug("Found sensor " + i + " " + sensorArray[i].toString());
        }
        sensorIndex = 0;
        sensor = sensorArray[sensorIndex];

        // Implement the ability to cycle through the sensor list
        viewSensorNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorIndex++;
                if (sensorIndex >= sensorArray.length)
                    sensorIndex = 0;
                sensor = sensorArray[sensorIndex];
                stopSensor();
                startSensor();
            }
        });
        viewSensorPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorIndex--;
                if (sensorIndex < 0)
                    sensorIndex = sensorArray.length-1;
                sensor = sensorArray[sensorIndex];
                stopSensor();
                startSensor();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        startSensor();
    }

    private void startSensor() {
        String type = "#" + (sensorIndex+1) + ", type " + sensor.getType();
        if (Build.VERSION.SDK_INT >= 20)
            type = type + "=" + sensor.getStringType();
        Logging.debug("Opened up " + type + " - " + sensor.toString());
        viewSensorType.setText(type);
        viewSensorDetails.setText(sensor.toString().replace("{Sensor ", "").replace("}", ""));
        for (int i = 0; i < viewBarArray.length; i++) {
            viewBarArray[i].setMaximum(sensor.getMaximumRange());
        }
        viewSensorGraph.resetMaximum(sensor.getMaximumRange());
        viewSensorRaw.setText("n/a");
        viewSensorAccuracy.setText("n/a");

        listener = new SensorEventListener() {

            public int min(int a, int b) { if (a < b) { return a; } else { return b; } }

            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == sensor.getType()) {
                    Logging.detailed("Sensor update: " + Arrays.toString(sensorEvent.values));

                    String raw = "";
                    for (int i = 0; i < sensorEvent.values.length; i++) {
                        String str = decimalFormat.format(sensorEvent.values[i]);
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
    }

    private void stopSensor() {
        sensorManager.unregisterListener(listener);
    }
}
