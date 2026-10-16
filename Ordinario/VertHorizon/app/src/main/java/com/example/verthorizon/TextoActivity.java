package com.example.verthorizon;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TextoActivity extends AppCompatActivity {

    EditText cuadro;
    TextView input;
    Button boton;

    static String guardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_texto);


        cuadro = findViewById(R.id.editTextText);
        input = findViewById(R.id.textView);
        boton = findViewById(R.id.button3);

        if(savedInstanceState!=null){
            input.setText(savedInstanceState.getString("HOLA"));
        }


        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mensaje = cuadro.getText().toString();
                if(TextoActivity.guardar!=null){
                    input.setText(guardar);
                } else {
                    input.setText(mensaje);
                }
                guardar = mensaje;
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("HOLA", cuadro.getText().toString());
    }
}