package com.example.demointent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SumaActivity extends AppCompatActivity {
    Intent intent;
    double num1;
    double num2;
    EditText eNum1;
    EditText eNum2;
    EditText res;
    Button boton;

    Button regresar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suma);

        intent = getIntent();
        num1 = intent.getDoubleExtra("NUMERO_1", 1.0);
        num2 = intent.getDoubleExtra("NUMERO_2", 1.0);
        res = findViewById(R.id.editTextNumberDecimal5);
        boton = findViewById(R.id.button);
        regresar = findViewById(R.id.button5);

        eNum1 = findViewById(R.id.editTextNumberDecimal3);
        eNum2 = findViewById(R.id.editTextNumberDecimal4);

        eNum1.setText(Double.toString(num1));
        eNum2.setText(Double.toString(num2));

        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double result = num1+num2;
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
