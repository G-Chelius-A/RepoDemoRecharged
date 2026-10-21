package com.example.listas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PerroAdapter extends RecyclerView.Adapter<PerroAdapter.PerroViewHolder>{

    private ArrayList<Perro> lista;

    public PerroAdapter(ArrayList<Perro> lista) {
        this.lista = lista;
    }

    @Override
    public PerroViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_perro, parent, false);
        return new PerroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PerroViewHolder holder, int position){
        Perro perroActual = lista.get(position);
        holder.tNombre.setText("Nombre del perro: "+perroActual.getNombre());
        holder.tRaza.setText("Raza del perro: "+perroActual.getRaza());
    }

    @Override
    public int getItemCount(){
        return lista.size();
    }

    public static class PerroViewHolder extends RecyclerView.ViewHolder {
        TextView tNombre;
        TextView tRaza;

        public PerroViewHolder(View itemView){
            super(itemView);
            tNombre = itemView.findViewById(R.id.text_nombre_perro);
            tRaza = itemView.findViewById(R.id.text_raza_perro);
        }
    }
}
