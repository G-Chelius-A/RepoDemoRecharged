package com.example.sensoresnav;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class PedometerActivity extends BaseActivity implements SensorEventListener {

    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1001;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor stepDetectorSensor;

    private TextView tvStepsCount, tvSessionSteps, tvPedometerStatus;
    private MaterialButton btnResetSteps;

    private int initialSteps = -1;
    private int currentTotalSteps = 0;
    private int sessionSteps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_pedometer);
        }

        tvStepsCount = findViewById(R.id.tvStepsCount);
        tvSessionSteps = findViewById(R.id.tvSessionSteps);
        tvPedometerStatus = findViewById(R.id.tvPedometerStatus);
        btnResetSteps = findViewById(R.id.btnResetSteps);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        }

        btnResetSteps.setOnClickListener(v -> {
            initialSteps = currentTotalSteps;
            sessionSteps = 0;
            updateStepViews();
            Toast.makeText(this, "Contador de sesión reiniciado", Toast.LENGTH_SHORT).show();
        });

        checkPermissionAndRegister();
    }

    private void checkPermissionAndRegister() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
            } else {
                initSensors();
            }
        } else {
            initSensors();
        }
    }

    private void initSensors() {
        if (stepCounterSensor == null && stepDetectorSensor == null) {
            tvPedometerStatus.setText(R.string.sensor_not_available);
            Toast.makeText(this, R.string.sensor_not_available, Toast.LENGTH_LONG).show();
        } else {
            tvPedometerStatus.setText("Sensor de pasos listo y activo");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            if (stepCounterSensor != null) {
                sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
            }
            if (stepDetectorSensor != null) {
                sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            currentTotalSteps = (int) event.values[0];
            if (initialSteps < 0) {
                initialSteps = currentTotalSteps;
            }
            sessionSteps = currentTotalSteps - initialSteps;
            updateStepViews();
        } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // Step detector increments by 1
            if (stepCounterSensor == null) {
                sessionSteps++;
                currentTotalSteps++;
                updateStepViews();
            }
            tvPedometerStatus.setText(R.string.step_detected_event);
        }
    }

    private void updateStepViews() {
        tvStepsCount.setText(String.valueOf(currentTotalSteps));
        tvSessionSteps.setText(String.format(Locale.getDefault(), "Pasos en esta sesión: %d", sessionSteps));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSensors();
            } else {
                tvPedometerStatus.setText(R.string.permission_required);
                Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show();
            }
        }
    }
}