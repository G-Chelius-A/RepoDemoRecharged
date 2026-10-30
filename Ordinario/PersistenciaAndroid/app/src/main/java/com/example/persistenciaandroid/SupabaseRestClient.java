package com.example.persistenciaandroid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class SupabaseRestClient {
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    public List<Producto> getProductos() throws IOException, JSONException {
        JSONArray jsonArray = new JSONArray(execute("GET", endpoint("?select=*&order="
                + SupabaseConfig.ID_COLUMN + ".desc"), null));
        List<Producto> productos = new ArrayList<>();
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            productos.add(Producto.fromJson(jsonArray.getJSONObject(i)));
        }
        return productos;
    }

    public void crearProducto(Producto producto) throws IOException, JSONException {
        execute("POST", endpoint(""), producto.toJson());
    }

    public void actualizarProducto(Producto producto) throws IOException, JSONException {
        execute("PATCH", endpoint("?" + SupabaseConfig.ID_COLUMN + "=eq."
                + encode(producto.id)), producto.toJson());
    }

    public void eliminarProducto(String id) throws IOException {
        execute("DELETE", endpoint("?" + SupabaseConfig.ID_COLUMN + "=eq." + encode(id)), null);
    }

    private String execute(String method, String url, JSONObject json)
            throws IOException {
        String apiKey = SupabaseConfig.ANON_KEY.trim();
        Request.Builder request = new Request.Builder()
                .url(url)
                .addHeader("apikey", apiKey)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Accept", "application/json")
                .addHeader("Prefer", "return=representation");

        if (json != null) {
            request.addHeader("Content-Type", "application/json")
                    .method(method, RequestBody.create(json.toString(), JSON));
        } else {
            request.method(method, null);
        }

        try (Response response = httpClient.newCall(request.build()).execute()) {
            String body = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new IOException("Supabase respondió " + response.code()
                        + (body.isEmpty() ? "" : ": " + body));
            }
            return body;
        }
    }

    private String endpoint(String query) {
        String baseUrl = SupabaseConfig.URL.trim();
        return baseUrl.replaceAll("/+$", "") + "/rest/v1/" + SupabaseConfig.TABLE + query;
    }

    private String encode(String value) {
        return android.net.Uri.encode(value);
    }
}
