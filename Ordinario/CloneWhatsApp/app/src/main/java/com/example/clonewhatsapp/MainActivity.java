package com.example.clonewhatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SupabaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = new SupabaseAuthManager(this);

        // Si no hay sesión activa, redirigir a LoginActivity
        if (!authManager.isSessionActive()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        TextView tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        Button btnSignOut = findViewById(R.id.btnSignOut);

        tvWelcomeUser.setText("Sesión activa con JWT");

        btnSignOut.setOnClickListener(v -> {
            authManager.signOut(new SupabaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    });
                }
            });
        });
    }

    private void redirectToLogin() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
