package com.example.persistenciaandroid;

import org.json.JSONException;
import org.json.JSONObject;

public final class Producto {
    public final String id;
    public final String producto;
    public final String descripcion;
    public final double precio;

    public Producto(String id, String producto, String descripcion, double precio) {
        this.id = id;
        this.producto = producto;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public static Producto fromJson(JSONObject json) throws JSONException {
        return new Producto(
                json.getString(SupabaseConfig.ID_COLUMN),
                json.getString("producto"),
                json.optString("descripcion", ""),
                json.getDouble("precio")
        );
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("producto", producto);
        json.put("descripcion", descripcion);
        json.put("precio", precio);
        return json;
    }
}
