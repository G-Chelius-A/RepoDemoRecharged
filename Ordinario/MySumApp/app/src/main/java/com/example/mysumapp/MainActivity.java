package com.example.mysumapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText num1;
    EditText num2;
    EditText res;
    Button boton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        num1 = findViewById(R.id.editTextNumberDecimal3);
        num2 = findViewById(R.id.editTextNumberDecimal4);
        res = findViewById(R.id.editTextNumberDecimal5);
        boton = findViewById(R.id.button);


        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double n1 = Double.parseDouble(num1.getText().toString());
                Double n2 = Double.parseDouble(num2.getText().toString());
                Double result = n1+n2;
                res.setText(String.valueOf(result));
            }
        });    }
}