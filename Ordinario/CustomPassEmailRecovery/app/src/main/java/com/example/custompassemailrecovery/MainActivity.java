package com.example.custompassemailrecovery;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private SupabaseAuthManager authManager;

    private LinearProgressIndicator progressLoading;
    private View mainLayout;

    // Vistas contenedoras
    private LinearLayout layoutLogin;
    private LinearLayout layoutRegister;
    private LinearLayout layoutRecovery;
    private LinearLayout layoutHome;

    // Campos de Login
    private TextInputLayout tilEmailLogin, tilPasswordLogin;
    private TextInputEditText etEmailLogin, etPasswordLogin;
    private MaterialButton btnLogin, btnGoToRegister, btnForgotPassword;

    // Campos de Registro
    private TextInputLayout tilEmailRegister, tilPasswordRegister, tilConfirmPasswordRegister;
    private TextInputEditText etEmailRegister, etPasswordRegister, etConfirmPasswordRegister;
    private MaterialButton btnRegister, btnGoToLoginFromRegister;

    // Campos de Recuperación
    private TextInputLayout tilEmailRecovery;
    private TextInputEditText etEmailRecovery;
    private MaterialButton btnSendRecovery, btnGoToLoginFromRecovery;

    // Campos de Home
    private TextView tvWelcomeUser;
    private MaterialButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mainLayout = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authManager = new SupabaseAuthManager();
        initViews();
        setupListeners();
        showLoginView();
    }

    private void initViews() {
        progressLoading = findViewById(R.id.progressLoading);

        layoutLogin = findViewById(R.id.layoutLogin);
        layoutRegister = findViewById(R.id.layoutRegister);
        layoutRecovery = findViewById(R.id.layoutRecovery);
        layoutHome = findViewById(R.id.layoutHome);

        // Login
        tilEmailLogin = findViewById(R.id.tilEmailLogin);
        tilPasswordLogin = findViewById(R.id.tilPasswordLogin);
        etEmailLogin = findViewById(R.id.etEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);

        // Registro
        tilEmailRegister = findViewById(R.id.tilEmailRegister);
        tilPasswordRegister = findViewById(R.id.tilPasswordRegister);
        tilConfirmPasswordRegister = findViewById(R.id.tilConfirmPasswordRegister);
        etEmailRegister = findViewById(R.id.etEmailRegister);
        etPasswordRegister = findViewById(R.id.etPasswordRegister);
        etConfirmPasswordRegister = findViewById(R.id.etConfirmPasswordRegister);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoToLoginFromRegister = findViewById(R.id.btnGoToLoginFromRegister);

        // Recuperación
        tilEmailRecovery = findViewById(R.id.tilEmailRecovery);
        etEmailRecovery = findViewById(R.id.etEmailRecovery);
        btnSendRecovery = findViewById(R.id.btnSendRecovery);
        btnGoToLoginFromRecovery = findViewById(R.id.btnGoToLoginFromRecovery);

        // Home
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupListeners() {
        // Navegación entre vistas
        btnGoToRegister.setOnClickListener(v -> showRegisterView());
        btnForgotPassword.setOnClickListener(v -> showRecoveryView());
        btnGoToLoginFromRegister.setOnClickListener(v -> showLoginView());
        btnGoToLoginFromRecovery.setOnClickListener(v -> showLoginView());

        // Botones de acción
        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> handleRegister());
        btnSendRecovery.setOnClickListener(v -> handleRecovery());
        btnLogout.setOnClickListener(v -> {
            showLoginView();
            showSnackbar("Sesión cerrada correctamente.");
        });
    }

    private void showLoginView() {
        clearErrors();
        layoutLogin.setVisibility(View.VISIBLE);
        layoutRegister.setVisibility(View.GONE);
        layoutRecovery.setVisibility(View.GONE);
        layoutHome.setVisibility(View.GONE);
    }

    private void showRegisterView() {
        clearErrors();
        layoutLogin.setVisibility(View.GONE);
        layoutRegister.setVisibility(View.VISIBLE);
        layoutRecovery.setVisibility(View.GONE);
        layoutHome.setVisibility(View.GONE);
    }

    private void showRecoveryView() {
        clearErrors();
        layoutLogin.setVisibility(View.GONE);
        layoutRegister.setVisibility(View.GONE);
        layoutRecovery.setVisibility(View.VISIBLE);
        layoutHome.setVisibility(View.GONE);
    }

    private void showHomeView(String email) {
        clearErrors();
        layoutLogin.setVisibility(View.GONE);
        layoutRegister.setVisibility(View.GONE);
        layoutRecovery.setVisibility(View.GONE);
        layoutHome.setVisibility(View.VISIBLE);
        tvWelcomeUser.setText("Usuario: " + email);
    }

    private void clearErrors() {
        tilEmailLogin.setError(null);
        tilPasswordLogin.setError(null);
        tilEmailRegister.setError(null);
        tilPasswordRegister.setError(null);
        tilConfirmPasswordRegister.setError(null);
        tilEmailRecovery.setError(null);
    }

    private void handleLogin() {
        clearErrors();
        String email = etEmailLogin.getText() != null ? etEmailLogin.getText().toString().trim() : "";
        String password = etPasswordLogin.getText() != null ? etPasswordLogin.getText().toString().trim() : "";

        if (!isValidEmail(email)) {
            tilEmailLogin.setError("Ingresa un correo electrónico válido");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPasswordLogin.setError("Ingresa tu contraseña");
            return;
        }

        setLoading(true);
        authManager.signIn(email, password, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message, JSONObject data) {
                setLoading(false);
                showSnackbar(message);
                showHomeView(email);
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                showSnackbar("Error: " + errorMessage);
            }
        });
    }

    private void handleRegister() {
        clearErrors();
        String email = etEmailRegister.getText() != null ? etEmailRegister.getText().toString().trim() : "";
        String password = etPasswordRegister.getText() != null ? etPasswordRegister.getText().toString().trim() : "";
        String confirmPassword = etConfirmPasswordRegister.getText() != null ? etConfirmPasswordRegister.getText().toString().trim() : "";

        if (!isValidEmail(email)) {
            tilEmailRegister.setError("Ingresa un correo electrónico válido");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            tilPasswordRegister.setError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPasswordRegister.setError("Las contraseñas no coinciden");
            return;
        }

        setLoading(true);
        authManager.signUp(email, password, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message, JSONObject data) {
                setLoading(false);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                etEmailLogin.setText(email);
                showLoginView();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                showSnackbar("Error: " + errorMessage);
            }
        });
    }

    private void handleRecovery() {
        clearErrors();
        String email = etEmailRecovery.getText() != null ? etEmailRecovery.getText().toString().trim() : "";

        if (!isValidEmail(email)) {
            tilEmailRecovery.setError("Ingresa un correo electrónico válido");
            return;
        }

        setLoading(true);
        authManager.sendPasswordReset(email, new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message, JSONObject data) {
                setLoading(false);
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                etEmailLogin.setText(email);
                showLoginView();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                showSnackbar("Error: " + errorMessage);
            }
        });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void setLoading(boolean isLoading) {
        progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        btnRegister.setEnabled(!isLoading);
        btnSendRecovery.setEnabled(!isLoading);
    }

    private void showSnackbar(String message) {
        Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).show();
    }
}
