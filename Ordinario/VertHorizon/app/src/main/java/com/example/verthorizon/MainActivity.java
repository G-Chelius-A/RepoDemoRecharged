package com.example.verthorizon;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Intent textoActividad;
    Button boton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Log.d("Hola a todos tag", "Ejecutando: onCreate()");
        Toast.makeText(this, "Pantalla Creada (onCreate)", Toast.LENGTH_SHORT).show();

        textoActividad = new Intent(MainActivity.this, TextoActivity.class);
        boton = findViewById(R.id.button);

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(textoActividad);
            }
        });

    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d("Hola a todos tag", "Ejecutando: onPause()");
        Toast.makeText(this, "Pantalla pausada (onPause)", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("Hola a todos tag", "Ejecutando: onDestroy()");
        Toast.makeText(this, "Pantalla destruida (onDestroy)", Toast.LENGTH_SHORT).show();
    }
}