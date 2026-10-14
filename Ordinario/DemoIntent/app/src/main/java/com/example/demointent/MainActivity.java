package com.example.demointent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    Button sumaButton;
    Button multiButton;
    Button cuadradoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        sumaButton = findViewById(R.id.button2);
        multiButton = findViewById(R.id.button3);
        cuadradoButton = findViewById(R.id.button4);

        Intent sumaIntent = new Intent(MainActivity.this, SumaActivity.class);
        Intent multIntent = new Intent(MainActivity.this, MultiActivity.class);
        Intent cuadradoIntent = new Intent(MainActivity.this, CuadradoActivity.class);
        sumaIntent.putExtra("NUMERO_1", 3.5);
        sumaIntent.putExtra("NUMERO_2",6.7);
        multIntent.putExtra("NUMERO_1", 3.5);
        multIntent.putExtra("NUMERO_2", 6.7);
        cuadradoIntent.putExtra("NUMERO", 5.5);

        sumaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(sumaIntent);
            }
        });

        multiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(multIntent);
            }
        });

        cuadradoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(cuadradoIntent);
            }
        });
    }
}