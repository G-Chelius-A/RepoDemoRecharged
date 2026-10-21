package com.example.listas;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_perro);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<Perro> perros = new ArrayList<>();
        perros.add(new Perro("Perro 1", "Labrador"));
        perros.add(new Perro("Perro 2", "Dalmata"));
        perros.add(new Perro("Perro miedoso", "Chihuahua"));
        perros.add(new Perro("Perro pequeño", "Corgi"));
        perros.add(new Perro("Perro mediano", "Xoloitzcuincle"));
        perros.add(new Perro("Perro gigante", "Gran danés"));
        perros.add(new Perro("Perro inteligente", "Border collie"));
        perros.add(new Perro("Perro de carreras", "El perro largo ese"));
        perros.add(new Perro("Perro chaparro", "Salchicha"));
        perros.add(new Perro("Perro barbón", "Schnauzer"));
        perros.add(new Perro("Perro mugroso", "Caniche"));
        perros.add(new Perro("Perro violento", "Pitbull"));
        perros.add(new Perro("Perro violento 2", "Rotweiller"));
        perros.add(new Perro("Perro asfixia", "Pug"));
        perros.add(new Perro("Perro chido", "Cruza"));
        perros.add(new Perro("Perro contento", "Labrador"));


        PerroAdapter adapter = new PerroAdapter(perros);
        recyclerView.setAdapter(adapter);
    }
}