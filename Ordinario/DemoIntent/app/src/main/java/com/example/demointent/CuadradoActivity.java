package com.example.demointent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class CuadradoActivity extends AppCompatActivity {
    Intent intent;
    double num1;
    EditText eNum1;
    EditText res;
    Button boton;

    Button regresar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cuadrado);

        intent = getIntent();
        num1 = intent.getDoubleExtra("NUMERO", 1.0);
        res = findViewById(R.id.editTextNumberDecimal5);
        boton = findViewById(R.id.button);
        regresar = findViewById(R.id.button7);
        eNum1 = findViewById(R.id.editTextNumberDecimal3);

        eNum1.setText(Double.toString(num1));

        //7

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double result = num1*num1;
                res.setText(String.valueOf(result));
            }
        });

        regresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
