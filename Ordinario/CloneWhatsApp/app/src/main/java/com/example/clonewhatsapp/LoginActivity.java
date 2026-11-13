package com.example.clonewhatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private SupabaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = new SupabaseAuthManager(this);

        // Si ya hay sesión activa, ir directo al MainActivity (Dashboard)
        if (authManager.isSessionActive()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvGoToRegister = findViewById(R.id.tvGoToRegister);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setEnabled(false);
            authManager.signIn(email, password, new SupabaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        tvGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            showResetPasswordDialog();
        });

        handleDeepLink(getIntent());
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        if (intent != null && intent.getData() != null) {
            android.net.Uri uri = intent.getData();
            String host = uri.getHost();

            if ("reset-password".equals(host)) {
                String accessToken = extractAccessToken(uri);
                showUpdatePasswordDialog(accessToken);
            } else if ("auth-callback".equals(host)) {
                Toast.makeText(this, "¡Correo verificado exitosamente! Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String extractAccessToken(android.net.Uri uri) {
        if (uri == null) return null;
        String token = uri.getQueryParameter("access_token");
        if (token != null && !token.isEmpty()) return token;

        String fragment = uri.getFragment();
        if (fragment != null && fragment.contains("access_token=")) {
            for (String param : fragment.split("&")) {
                if (param.startsWith("access_token=")) {
                    return param.substring("access_token=".length());
                }
            }
        }
        return null;
    }

    private void showUpdatePasswordDialog(String accessToken) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Establecer nueva contraseña");

        final EditText input = new EditText(this);
        input.setHint("Nueva contraseña");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();
            if (newPassword.length() >= 6) {
                authManager.updateUserPassword(newPassword, accessToken, new SupabaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show());
                    }
                });
            } else {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showResetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar contraseña");

        final EditText input = new EditText(this);
        input.setHint("Correo electrónico");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) {
                authManager.resetPassword(email, new SupabaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show());
                    }
                });
            } else {
                Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
