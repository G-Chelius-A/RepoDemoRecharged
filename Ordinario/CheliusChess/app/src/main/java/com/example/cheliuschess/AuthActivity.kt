package com.example.cheliuschess

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cheliuschess.databinding.ActivityAuthBinding
import com.example.cheliuschess.remote.OnlineGameRepository
import com.example.cheliuschess.remote.SupabaseClientManager
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var repository: OnlineGameRepository
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.authRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        repository = OnlineGameRepository()

        // Verificar si ya existe una sesión activa
        if (repository.getCurrentUserId() != null && SupabaseClientManager.isConfigured()) {
            openLobby()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        binding.btnModeLogin.setOnClickListener {
            isRegisterMode = false
            updateModeUI()
        }

        binding.btnModeRegister.setOnClickListener {
            isRegisterMode = true
            updateModeUI()
        }

        binding.btnAuthSubmit.setOnClickListener {
            performAuth()
        }
    }

    private fun updateModeUI() {
        if (isRegisterMode) {
            binding.tilUsername.visibility = View.VISIBLE
            binding.btnAuthSubmit.text = "CREAR CUENTA"
            binding.btnModeRegister.strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#007AFF"))
            binding.btnModeLogin.strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#3A3A3C"))
        } else {
            binding.tilUsername.visibility = View.GONE
            binding.btnAuthSubmit.text = "INICIAR SESIÓN"
            binding.btnModeLogin.strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#007AFF"))
            binding.btnModeRegister.strokeColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#3A3A3C"))
        }
    }

    private fun performAuth() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Completa el correo y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        if (isRegisterMode && username.isEmpty()) {
            Toast.makeText(this, "Completa tu nombre de usuario", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressAuth.visibility = View.VISIBLE
        binding.btnAuthSubmit.isEnabled = false

        lifecycleScope.launch {
            val result = if (isRegisterMode) {
                repository.signUp(email, password, username)
            } else {
                repository.signIn(email, password)
            }

            binding.progressAuth.visibility = View.GONE
            binding.btnAuthSubmit.isEnabled = true

            result.onSuccess {
                Toast.makeText(this@AuthActivity, "Autenticación exitosa", Toast.LENGTH_SHORT).show()
                openLobby()
            }.onFailure { error ->
                Toast.makeText(this@AuthActivity, "Error de autenticación: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openLobby() {
        startActivity(Intent(this, LobbyActivity::class.java))
        finish()
    }
}
