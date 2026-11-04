package com.example.clonewhatsapp;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseAuthManager {
    private static final String PREF_NAME = "SupabaseAuthPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_EMAIL = "user_email";

    private final OkHttpClient client;
    private final Gson gson;
    private final SharedPreferences prefs;
    private final String supabaseUrl;
    private final String supabaseKey;

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public SupabaseAuthManager(Context context) {
        this.client = new OkHttpClient();
        this.gson = new Gson();
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.supabaseUrl = BuildConfig.SUPABASE_URL;
        this.supabaseKey = BuildConfig.SUPABASE_KEY;
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public boolean isSessionActive() {
        return getAccessToken() != null;
    }

    public void saveSession(String accessToken, String email) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_USER_EMAIL, email)
                .apply();
    }

    public void clearSession() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_USER_EMAIL)
                .apply();
    }

    // 1. Registro con correo, contraseña, username y full_name (Soporta el trigger de profiles)
    public void signUp(String email, String password, String username, String fullName, AuthCallback callback) {
        String url = supabaseUrl + "/auth/v1/signup";

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);

        JsonObject dataObject = new JsonObject();
        dataObject.addProperty("username", username);
        dataObject.addProperty("full_name", fullName);

        JsonObject optionsObject = new JsonObject();
        optionsObject.add("data", dataObject);

        jsonBody.add("options", optionsObject);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        String userId = null;
                        String accessToken = null;

                        if (jsonResponse.has("id")) {
                            userId = jsonResponse.get("id").getAsString();
                        } else if (jsonResponse.has("user")) {
                            JsonObject userObj = jsonResponse.getAsJsonObject("user");
                            if (userObj.has("id")) {
                                userId = userObj.get("id").getAsString();
                            }
                        }

                        if (jsonResponse.has("access_token")) {
                            accessToken = jsonResponse.get("access_token").getAsString();
                            saveSession(accessToken, email);
                        }

                        if (userId != null && accessToken != null) {
                            createOrUpdateProfile(userId, username, fullName, new AuthCallback() {
                                @Override
                                public void onSuccess(String msg) {
                                    callback.onSuccess("Registro y perfil creados exitosamente.");
                                }
                                @Override
                                public void onError(String err) {
                                    callback.onError("Registro OK, pero error al crear perfil: " + err);
                                }
                            });
                        } else {
                            callback.onSuccess("Registro exitoso. (Confirma tu correo si está habilitado).");
                        }
                    } catch (Exception e) {
                        callback.onSuccess("Registro exitoso.");
                    }
                } else {
                    callback.onError("Error en registro: " + responseBody);
                }
            }
        });
    }

    // 2. Inicio de sesión con correo y contraseña (Guarda JWT token para persistencia)
    public void signIn(String email, String password, AuthCallback callback) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);
        jsonBody.addProperty("password", password);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                        if (jsonResponse.has("access_token")) {
                            String accessToken = jsonResponse.get("access_token").getAsString();
                            saveSession(accessToken, email);

                            // Opcional: Extraer el ID del usuario y asegurar que su perfil exista en public.profiles
                            if (jsonResponse.has("user")) {
                                JsonObject userObj = jsonResponse.getAsJsonObject("user");
                                if (userObj.has("id")) {
                                    String userId = userObj.get("id").getAsString();
                                    String username = email.split("@")[0]; // fallback username
                                    if (userObj.has("user_metadata") && !userObj.get("user_metadata").isJsonNull()) {
                                        JsonObject meta = userObj.getAsJsonObject("user_metadata");
                                        if (meta.has("username")) {
                                            username = meta.get("username").getAsString();
                                        }
                                    }
                                    // Llamada directa para asegurar persistencia en profiles
                                    createOrUpdateProfile(userId, username, username, new AuthCallback() {
                                        @Override
                                        public void onSuccess(String msg) {
                                            callback.onSuccess("Inicio de sesión y perfil guardados exitosamente.");
                                        }
                                        @Override
                                        public void onError(String err) {
                                            callback.onError("Login OK, pero error al guardar perfil: " + err);
                                        }
                                    });
                                    return; // Para evitar llamar a callback.onSuccess duplicado antes de que termine createOrUpdateProfile
                                }
                            }

                            callback.onSuccess("Inicio de sesión exitoso. Sesión JWT persistida.");
                        } else {
                            callback.onError("Token no encontrado en la respuesta.");
                        }
                    } catch (Exception e) {
                        callback.onError("Error al parsear respuesta: " + e.getMessage());
                    }
                } else {
                    callback.onError("Error al iniciar sesión: " + responseBody);
                }
            }
        });
    }

    // 3. Cierre de sesión (Revoca el token y limpia SharedPreferences)
    public void signOut(AuthCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            clearSession();
            callback.onSuccess("No había sesión activa.");
            return;
        }

        String url = supabaseUrl + "/auth/v1/logout";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .post(RequestBody.create(new byte[0], null))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                clearSession();
                callback.onError("Error de red al cerrar sesión (sesión local limpiada): " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                clearSession();
                callback.onSuccess("Cierre de sesión exitoso.");
            }
        });
    }

    // 4. Recuperación de contraseña
    public void resetPassword(String email, AuthCallback callback) {
        String url = supabaseUrl + "/auth/v1/recover";

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("email", email);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    callback.onSuccess("Correo de recuperación enviado exitosamente.");
                } else {
                    callback.onError("Error al enviar recuperación: " + responseBody);
                }
            }
        });
    }

    // Método para guardar o actualizar el perfil directamente en la tabla public.profiles vía PostgREST
    public void createOrUpdateProfile(String userId, String username, String fullName, AuthCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay token JWT activo para guardar el perfil.");
            return;
        }

        String url = supabaseUrl + "/rest/v1/profiles";

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("id", userId);
        jsonBody.addProperty("username", username);
        jsonBody.addProperty("full_name", fullName);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red al guardar perfil: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    if (responseBody.trim().equals("[]")) {
                        callback.onError("Error RLS: Supabase devolvió [] (0 filas afectadas por políticas de seguridad).");
                    } else {
                        callback.onSuccess("Perfil guardado en base de datos exitosamente.");
                    }
                } else {
                    callback.onError("Error (" + response.code() + ") al guardar perfil: " + responseBody);
                }
            }
        });
    }

    // 5. Validar sesión JWT existente contra Supabase
    public void validateSession(AuthCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay sesión persistida.");
            return;
        }

        String url = supabaseUrl + "/auth/v1/user";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    callback.onSuccess("Sesión JWT activa y válida.");
                } else {
                    clearSession();
                    callback.onError("Sesión expirada o inválida.");
                }
            }
        });
    }
}
