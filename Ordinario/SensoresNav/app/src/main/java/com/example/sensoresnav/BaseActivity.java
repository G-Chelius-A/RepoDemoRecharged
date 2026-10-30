package com.example.sensoresnav;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected MaterialToolbar toolbar;

    @Override
    public void setContentView(int layoutResID) {
        DrawerLayout fullLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityContainer = fullLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        super.setContentView(fullLayout);

        drawerLayout = fullLayout.findViewById(R.id.drawer_layout);
        navigationView = fullLayout.findViewById(R.id.nav_view);
        toolbar = fullLayout.findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        highlightCurrentMenuItem();
    }

    private void highlightCurrentMenuItem() {
        if (navigationView == null) return;
        int selectedId = R.id.nav_home;
        if (this instanceof AccelerometerActivity) {
            selectedId = R.id.nav_accelerometer;
        } else if (this instanceof PedometerActivity) {
            selectedId = R.id.nav_pedometer;
        } else if (this instanceof GyroscopeActivity) {
            selectedId = R.id.nav_gyroscope;
        } else if (this instanceof CameraActivity) {
            selectedId = R.id.nav_camera;
        } else if (this instanceof GpsActivity) {
            selectedId = R.id.nav_gps;
        } else if (this instanceof MagnetometerActivity) {
            selectedId = R.id.nav_magnetometer;
        }
        navigationView.setCheckedItem(selectedId);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        int itemId = item.getItemId();
        Class<?> targetClass = null;

        if (itemId == R.id.nav_home) {
            targetClass = MainActivity.class;
        } else if (itemId == R.id.nav_accelerometer) {
            targetClass = AccelerometerActivity.class;
        } else if (itemId == R.id.nav_pedometer) {
            targetClass = PedometerActivity.class;
        } else if (itemId == R.id.nav_gyroscope) {
            targetClass = GyroscopeActivity.class;
        } else if (itemId == R.id.nav_camera) {
            targetClass = CameraActivity.class;
        } else if (itemId == R.id.nav_gps) {
            targetClass = GpsActivity.class;
        } else if (itemId == R.id.nav_magnetometer) {
            targetClass = MagnetometerActivity.class;
        }

        if (targetClass != null && !this.getClass().equals(targetClass)) {
            Intent intent = new Intent(this, targetClass);
            if (targetClass == MainActivity.class) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            }
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}