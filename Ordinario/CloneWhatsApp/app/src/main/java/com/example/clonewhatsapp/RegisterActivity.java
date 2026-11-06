package com.example.clonewhatsapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etUsername, etFullName;
    private ImageView ivProfileAvatar;
    private ProgressBar progressBar;
    private Button btnRegister;

    private SupabaseAuthManager authManager;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    Glide.with(RegisterActivity.this)
                            .load(uri)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .circleCrop()
                            .into(ivProfileAvatar);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authManager = new SupabaseAuthManager(this);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        etFullName = findViewById(R.id.etFullName);
        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        TextView tvSelectPhoto = findViewById(R.id.tvSelectPhoto);
        progressBar = findViewById(R.id.progressBar);
        btnRegister = findViewById(R.id.btnRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        View.OnClickListener selectPhotoListener = v -> {
            try {
                imagePickerLauncher.launch("image/*");
            } catch (Exception e) {
                Toast.makeText(RegisterActivity.this, "Error al abrir galería: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        ivProfileAvatar.setOnClickListener(selectPhotoListener);
        if (tvSelectPhoto != null) {
            tvSelectPhoto.setOnClickListener(selectPhotoListener);
        }

        btnRegister.setOnClickListener(v -> performRegistration());

        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Completa correo, contraseña y username", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        authManager.signUp(email, password, username, fullName, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                String createdUserId = authManager.getUserId();
                if (selectedImageUri != null && createdUserId != null) {
                    uploadProfilePhoto(createdUserId);
                } else {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void uploadProfilePhoto(String userId) {
        authManager.uploadAndSaveProfileImage(this, userId, selectedImageUri, new SupabaseAuthManager.UserImageCallback() {
            @Override
            public void onSuccess(String imgUrl) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, "Registro e imagen de perfil guardados correctamente", Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this, "Registro exitoso, pero fallo al subir imagen: " + error, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!loading);
    }
}
