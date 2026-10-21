package com.example.layouts;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button linearButton;
    Button relativeButton;
    Button frameButton;
    Button tableButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        linearButton = findViewById(R.id.button2);
        relativeButton = findViewById(R.id.button3);
        frameButton = findViewById(R.id.button4);
        tableButton = findViewById(R.id.button5);

        Intent linear = new Intent(MainActivity.this, LinearActivity.class);
        Intent relative = new Intent(MainActivity.this, RelativeActivity.class);
        Intent frame = new Intent(MainActivity.this, FrameActivity.class);
        Intent table = new Intent(MainActivity.this, TableActivity.class);

        linearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(linear);
            }
        });

        relativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(relative);
            }
        });

        frameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(frame);
            }
        });

        tableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(table);
            }
        });




    }
}