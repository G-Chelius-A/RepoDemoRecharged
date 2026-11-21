package com.example.cheliuschess

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cheliuschess.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainMenuRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnPlayOnline.setOnClickListener {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        binding.btnPlayVsComputer.setOnClickListener {
            val intent = Intent(this, TimeConfigActivity::class.java).apply {
                putExtra(TimeConfigActivity.EXTRA_VS_COMPUTER, true)
            }
            startActivity(intent)
        }

        binding.btnPlayOffline.setOnClickListener {
            val intent = Intent(this, TimeConfigActivity::class.java).apply {
                putExtra(TimeConfigActivity.EXTRA_VS_COMPUTER, false)
            }
            startActivity(intent)
        }

        binding.btnAbout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Chelius Chess")
            .setMessage(
                "Chelius Chess - Producto Final\n\n" +
                "• Multijugador en Línea: Supabase Auth, Base de Datos PostgreSQL, RLS y WebSockets en Vivo (Realtime).\n" +
                "• Motor de IA: Algoritmo Minimax con Poda Alfa-Beta en 3 niveles (Fácil, Medio, Difícil).\n" +
                "• Selección de Bando: Juega con Blancas, Negras o Aleatorio.\n" +
                "• Modalidades de Tiempo: Blitz, Rápida o Personalizada con incremento.\n" +
                "• Reglas Oficiales FIDE: Notación SAN Internacional, Enroques, Peón al Paso, Promoción.\n" +
                "• Reversión inteligente de jugadas y Pausa durante la partida.\n" +
                "• Marcador de Puntajes Oficial (1-0, 0-1, 1/2-1/2) y Registro en la Nube."
            )
            .setPositiveButton("Entendido", null)
            .show()
    }
}
