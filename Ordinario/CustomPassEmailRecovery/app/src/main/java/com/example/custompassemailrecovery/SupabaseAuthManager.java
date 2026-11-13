package com.example.custompassemailrecovery;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SupabaseAuthManager {

    private final OkHttpClient client;
    private final Handler mainHandler;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface AuthCallback {
        void onSuccess(String message, JSONObject data);
        void onError(String errorMessage);
    }

    public SupabaseAuthManager() {
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Registro de usuario en Supabase Auth.
     */
    public void signUp(String email, String password, AuthCallback callback) {
        String url = getBaseUrl() + "/auth/v1/signup";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            notifyError(callback, "Error al preparar datos de registro.");
            return;
        }

        executePostRequest(url, jsonBody.toString(), new InternalCallback() {
            @Override
            public void onResponse(boolean isSuccessful, int statusCode, JSONObject responseJson) {
                if (isSuccessful) {
                    String message = "Registro exitoso. Revisa tu correo si tienes confirmación activada.";
                    notifySuccess(callback, message, responseJson);
                } else {
                    String error = extractErrorMessage(responseJson, "Error al registrar usuario (Código " + statusCode + ")");
                    notifyError(callback, error);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                notifyError(callback, errorMessage);
            }
        });
    }

    /**
     * Inicio de sesión con correo y contraseña.
     */
    public void signIn(String email, String password, AuthCallback callback) {
        String url = getBaseUrl() + "/auth/v1/token?grant_type=password";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            notifyError(callback, "Error al preparar datos de inicio de sesión.");
            return;
        }

        executePostRequest(url, jsonBody.toString(), new InternalCallback() {
            @Override
            public void onResponse(boolean isSuccessful, int statusCode, JSONObject responseJson) {
                if (isSuccessful) {
                    notifySuccess(callback, "Inicio de sesión exitoso.", responseJson);
                } else {
                    String error = extractErrorMessage(responseJson, "Credenciales incorrectas o error en inicio de sesión.");
                    notifyError(callback, error);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                notifyError(callback, errorMessage);
            }
        });
    }

    /**
     * Envío de correo para recuperación de contraseña.
     */
    public void sendPasswordReset(String email, AuthCallback callback) {
        String url = getBaseUrl() + "/auth/v1/recover";
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
        } catch (JSONException e) {
            notifyError(callback, "Error al preparar correo de recuperación.");
            return;
        }

        executePostRequest(url, jsonBody.toString(), new InternalCallback() {
            @Override
            public void onResponse(boolean isSuccessful, int statusCode, JSONObject responseJson) {
                if (isSuccessful) {
                    notifySuccess(callback, "Correo de recuperación enviado exitosamente.", responseJson);
                } else {
                    String error = extractErrorMessage(responseJson, "Error al enviar correo de recuperación.");
                    notifyError(callback, error);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                notifyError(callback, errorMessage);
            }
        });
    }

    private interface InternalCallback {
        void onResponse(boolean isSuccessful, int statusCode, JSONObject responseJson);
        void onFailure(String errorMessage);
    }

    private void executePostRequest(String url, String jsonBody, InternalCallback callback) {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure("Error de red: " + e.getLocalizedMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    String responseString = responseBody != null ? responseBody.string() : "";
                    Log.d("SupabaseAuthManager", "POST " + url + " | Code: " + response.code() + " | Response: " + responseString);
                    JSONObject json;
                    try {
                        json = responseString.isEmpty() ? new JSONObject() : new JSONObject(responseString);
                    } catch (JSONException e) {
                        json = new JSONObject();
                    }
                    callback.onResponse(response.isSuccessful(), response.code(), json);
                } catch (IOException e) {
                    Log.e("SupabaseAuthManager", "Error de lectura en respuesta", e);
                    callback.onFailure("Error al procesar respuesta del servidor.");
                }
            }
        });
    }

    private String getBaseUrl() {
        String url = SupabaseConfig.SUPABASE_URL.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private String extractErrorMessage(JSONObject json, String fallback) {
        if (json == null) return fallback;
        if (json.has("error_description")) {
            return json.optString("error_description");
        } else if (json.has("msg")) {
            return json.optString("msg");
        } else if (json.has("message")) {
            return json.optString("message");
        } else if (json.has("error")) {
            return json.optString("error");
        }
        return fallback;
    }

    private void notifySuccess(AuthCallback callback, String message, JSONObject data) {
        mainHandler.post(() -> {
            if (callback != null) callback.onSuccess(message, data);
        });
    }

    private void notifyError(AuthCallback callback, String errorMessage) {
        mainHandler.post(() -> {
            if (callback != null) callback.onError(errorMessage);
        });
    }
}
