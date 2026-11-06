package com.example.clonewhatsapp;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfileAvatar;
    private EditText etFullName, etUsername;
    private ProgressBar progressBar;
    private Button btnSaveProfile, btnChangePhoto;

    private SupabaseAuthManager authManager;
    private String currentUserId;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfileAvatar.setImageURI(uri);
                    uploadSelectedImage();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        authManager = new SupabaseAuthManager(this);
        currentUserId = authManager.getUserId();

        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        progressBar = findViewById(R.id.progressBar);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        if (currentUserId == null) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserProfile();

        btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        ivProfileAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        btnSaveProfile.setOnClickListener(v -> saveProfileData());
    }

    private void loadUserProfile() {
        showLoading(true);
        authManager.getUserProfile(currentUserId, new SupabaseAuthManager.UserProfileCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (user != null) {
                        etFullName.setText(user.getFullName());
                        etUsername.setText(user.getUsername());
                        if (user.getImgUrl() != null && !user.getImgUrl().trim().isEmpty()) {
                            Glide.with(ProfileActivity.this)
                                    .load(user.getImgUrl())
                                    .placeholder(R.mipmap.ic_launcher_round)
                                    .error(R.mipmap.ic_launcher_round)
                                    .circleCrop()
                                    .into(ivProfileAvatar);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void uploadSelectedImage() {
        if (selectedImageUri == null || currentUserId == null) return;

        showLoading(true);
        authManager.uploadAndSaveProfileImage(this, currentUserId, selectedImageUri, new SupabaseAuthManager.UserImageCallback() {
            @Override
            public void onSuccess(String imgUrl) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Foto de perfil actualizada exitosamente", Toast.LENGTH_SHORT).show();
                    Glide.with(ProfileActivity.this)
                            .load(imgUrl)
                            .placeholder(R.mipmap.ic_launcher_round)
                            .circleCrop()
                            .into(ivProfileAvatar);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Error al subir foto: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void saveProfileData() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "El nombre de usuario no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        authManager.createOrUpdateProfile(currentUserId, username, fullName, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!loading);
        btnChangePhoto.setEnabled(!loading);
    }
}
