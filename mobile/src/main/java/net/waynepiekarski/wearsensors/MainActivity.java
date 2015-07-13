package net.waynepiekarski.wearsensors;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity extends Activity {

    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener listener;
    private int sensorType;
    private TextView viewSensorType;
    private TextView viewSensorDetails;
    private TextView viewSensorAccuracy;
    private TextView viewSensorRaw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewSensorType = (TextView)findViewById(R.id.sensorType);
        viewSensorDetails = (TextView)findViewById(R.id.sensorDetails);
        viewSensorAccuracy = (TextView)findViewById(R.id.sensorAccuracy);
        viewSensorRaw = (TextView)findViewById(R.id.sensorRaw);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        sensorType = Sensor.TYPE_ACCELEROMETER;
        sensor = sensorManager.getDefaultSensor(sensorType);
        Logging.debug("Opened up sensor: " + sensor);
        viewSensorType.setText(sensor.getStringType());
        viewSensorDetails.setText(sensor.toString());

        listener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent.sensor.getType() == sensorType) {
                    Logging.debug("Accelerometer update: " + Arrays.toString(sensorEvent.values));
                    viewSensorRaw.setText(Arrays.toString(sensorEvent.values));
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Logging.debug("Accelerometer accuracy: " + accuracy);
                viewSensorAccuracy.setText("Accuracy=" + accuracy);
            }
        };
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(listener);
    }
}
