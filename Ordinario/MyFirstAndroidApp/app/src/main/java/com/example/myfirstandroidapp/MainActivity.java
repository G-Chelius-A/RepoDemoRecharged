package com.example.myfirstandroidapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    // Ciclo de vida app en Android. Android es MVC: kotlin+java: controller, res: vista, modelos es otra cosa.
    TextView texto;
    Button boton;
    EditText input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        texto = findViewById(R.id.textView);
        boton = findViewById(R.id.miBoton);
        input = findViewById(R.id.editEntrada);

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String mensaje = texto.getText().toString();
                //Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_LONG).show();
                String entrada = input.getText().toString();
                texto.setText(entrada);
            }
        });
    }
}