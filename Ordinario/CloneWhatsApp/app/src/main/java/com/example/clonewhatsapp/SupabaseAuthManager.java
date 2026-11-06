package com.example.clonewhatsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class SupabaseAuthManager {
    private static final String PREF_NAME = "SupabaseAuthPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ID = "user_id";

    private final OkHttpClient client;
    private final Gson gson;
    private final SharedPreferences prefs;
    private final String supabaseUrl;
    private final String supabaseKey;

    private WebSocket realtimeWebSocket;
    private ScheduledExecutorService heartbeatExecutor;

    public interface AuthCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface UserImageCallback {
        void onSuccess(String imgUrl);
        void onError(String error);
    }

    public interface UsersCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }

    public interface UserProfileCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface GetOrCreateConversationCallback {
        void onSuccess(long convoId);
        void onError(String error);
    }

    public interface ConversationsCallback {
        void onSuccess(List<Conversation> conversations);
        void onError(String error);
    }

    public interface SendMessageCallback {
        void onSuccess(Message message);
        void onError(String error);
    }

    public interface MessagesCallback {
        void onSuccess(List<Message> messages);
        void onError(String error);
    }

    public interface RealtimeMessageCallback {
        void onNewMessage(Message message);
        void onMessageStateUpdated(long messageId, String newEstado);
        void onTypingStatusChanged(boolean isTyping);
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

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public boolean isSessionActive() {
        return getAccessToken() != null;
    }

    public void saveSession(String accessToken, String email, String userId) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ID, userId)
                .apply();
    }

    public void saveSession(String accessToken, String email) {
        saveSession(accessToken, email, getUserId());
    }

    public void clearSession() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_USER_ID)
                .apply();
    }

    // 1. Registro con correo, contraseña, username y full_name
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
                            saveSession(accessToken, email, userId);
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

    // 2. Inicio de sesión con correo y contraseña
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
                            String userId = null;
                            JsonObject userObj = jsonResponse.has("user") ? jsonResponse.getAsJsonObject("user") : null;
                            if (userObj != null && userObj.has("id")) {
                                userId = userObj.get("id").getAsString();
                            }
                            saveSession(accessToken, email, userId);

                            if (userObj != null && userId != null) {
                                String username = email.split("@")[0];
                                if (userObj.has("user_metadata") && !userObj.get("user_metadata").isJsonNull()) {
                                    JsonObject meta = userObj.getAsJsonObject("user_metadata");
                                    if (meta.has("username")) {
                                        username = meta.get("username").getAsString();
                                    }
                                }
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
                                return;
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

    // 3. Cierre de sesión
    public void signOut(AuthCallback callback) {
        unsubscribeRealtimeMessages();
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

    // Guardar o actualizar perfil en public.profiles
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

    // Actualizar la última conexión del usuario
    public void updateLastLogin(String userId) {
        String token = getAccessToken();
        if (token == null || userId == null) return;

        String url = supabaseUrl + "/rest/v1/profiles?id=eq." + userId;
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("last_login", "now()");

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {}
        });
    }

    // 5. Validar sesión JWT existente
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
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonObject userObj = gson.fromJson(responseBody, JsonObject.class);
                        if (userObj.has("id")) {
                            String userId = userObj.get("id").getAsString();
                            String email = userObj.has("email") ? userObj.get("email").getAsString() : getUserEmail();
                            saveSession(token, email, userId);
                            updateLastLogin(userId);
                        }
                    } catch (Exception ignored) {}
                    callback.onSuccess("Sesión JWT activa y válida.");
                } else {
                    clearSession();
                    callback.onError("Sesión expirada o inválida.");
                }
            }
        });
    }

    // 6. Guardar o actualizar la imagen del usuario en public.user_img
    public void saveOrUpdateUserImage(String userId, String imgUrl, AuthCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay token JWT activo para guardar la imagen.");
            return;
        }

        String url = supabaseUrl + "/rest/v1/user_img";

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("user_id", userId);
        jsonBody.addProperty("img_url", imgUrl);

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
                callback.onError("Error de red al guardar imagen: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    if (responseBody.trim().equals("[]")) {
                        callback.onError("Error RLS: Supabase devolvió [] (verificar políticas RLS en public.user_img).");
                    } else {
                        callback.onSuccess("Imagen guardada en base de datos exitosamente.");
                    }
                } else {
                    callback.onError("Error (" + response.code() + ") al guardar imagen: " + responseBody);
                }
            }
        });
    }

    // 7. Obtener la URL de la imagen del usuario desde public.user_img
    public void getUserImage(String userId, UserImageCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay token JWT activo.");
            return;
        }

        String url = supabaseUrl + "/rest/v1/user_img?user_id=eq." + userId + "&select=img_url";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red al obtener imagen: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonArray array = gson.fromJson(responseBody, JsonArray.class);
                        if (array != null && !array.isEmpty()) {
                            JsonObject obj = array.get(0).getAsJsonObject();
                            if (obj.has("img_url") && !obj.get("img_url").isJsonNull()) {
                                callback.onSuccess(obj.get("img_url").getAsString());
                            } else {
                                callback.onSuccess(null);
                            }
                        } else {
                            callback.onSuccess(null);
                        }
                    } catch (Exception e) {
                        callback.onError("Error al procesar respuesta: " + e.getMessage());
                    }
                } else {
                    callback.onError("Error (" + response.code() + ") al obtener imagen: " + responseBody);
                }
            }
        });
    }

    // 8. Sube una imagen en bytes a Supabase Storage
    public void uploadAndSaveProfileImage(String userId, byte[] imageBytes, String mimeType, UserImageCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay token JWT activo para subir la imagen.");
            return;
        }

        String extension = mimeType != null && mimeType.contains("png") ? ".png" : ".jpg";
        String fileName = userId + extension;

        String storageUrl = supabaseUrl + "/storage/v1/object/user/pfp/" + fileName;

        RequestBody body = RequestBody.create(imageBytes, MediaType.parse(mimeType != null ? mimeType : "image/jpeg"));
        Request request = new Request.Builder()
                .url(storageUrl)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("x-upsert", "true")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red al subir la imagen a Storage: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    String publicUrl = supabaseUrl + "/storage/v1/object/public/user/pfp/" + fileName;

                    saveOrUpdateUserImage(userId, publicUrl, new AuthCallback() {
                        @Override
                        public void onSuccess(String message) {
                            callback.onSuccess(publicUrl);
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError("Imagen subida a Storage, pero falló el registro en BD: " + error);
                        }
                    });
                } else {
                    callback.onError("Error (" + response.code() + ") al subir a Storage: " + responseBody);
                }
            }
        });
    }

    // 9. Sobrecarga para subir la imagen directamente desde un Uri de Android
    public void uploadAndSaveProfileImage(Context context, String userId, Uri imageUri, UserImageCallback callback) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onError("No se pudo abrir el archivo de imagen.");
                return;
            }
            byte[] bytes = getBytesFromInputStream(inputStream);
            String mimeType = context.getContentResolver().getType(imageUri);
            uploadAndSaveProfileImage(userId, bytes, mimeType, callback);
        } catch (Exception e) {
            callback.onError("Error al procesar el archivo: " + e.getMessage());
        }
    }

    // 10. Consulta todos los usuarios registrados
    public void getUsers(UsersCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay sesión activa.");
            return;
        }

        String url = supabaseUrl + "/rest/v1/profiles?select=id,username,full_name,user_img(img_url)";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red al consultar usuarios: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonArray profilesArray = gson.fromJson(responseBody, JsonArray.class);
                        List<User> userList = new ArrayList<>();
                        if (profilesArray != null) {
                            for (JsonElement elem : profilesArray) {
                                JsonObject obj = elem.getAsJsonObject();
                                String id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsString() : "";
                                String username = obj.has("username") && !obj.get("username").isJsonNull() ? obj.get("username").getAsString() : "";
                                String fullName = obj.has("full_name") && !obj.get("full_name").isJsonNull() ? obj.get("full_name").getAsString() : "";

                                String imgUrl = null;
                                if (obj.has("user_img") && !obj.get("user_img").isJsonNull()) {
                                    JsonElement imgElem = obj.get("user_img");
                                    if (imgElem.isJsonArray() && imgElem.getAsJsonArray().size() > 0) {
                                        JsonObject firstImg = imgElem.getAsJsonArray().get(0).getAsJsonObject();
                                        if (firstImg.has("img_url") && !firstImg.get("img_url").isJsonNull()) {
                                            imgUrl = firstImg.get("img_url").getAsString();
                                        }
                                    } else if (imgElem.isJsonObject()) {
                                        JsonObject imgObj = imgElem.getAsJsonObject();
                                        if (imgObj.has("img_url") && !imgObj.get("img_url").isJsonNull()) {
                                            imgUrl = imgObj.get("img_url").getAsString();
                                        }
                                    }
                                }
                                userList.add(new User(id, username, fullName, imgUrl));
                            }
                        }
                        callback.onSuccess(userList);
                    } catch (Exception e) {
                        callback.onError("Error al parsear lista de usuarios: " + e.getMessage());
                    }
                } else {
                    callback.onError("Error (" + response.code() + ") al consultar usuarios: " + responseBody);
                }
            }
        });
    }

    // 11. Consulta el perfil de un usuario específico
    public void getUserProfile(String userId, UserProfileCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay sesión activa.");
            return;
        }

        String url = supabaseUrl + "/rest/v1/profiles?id=eq." + userId + "&select=id,username,full_name,user_img(img_url)";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red al obtener perfil: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonArray array = gson.fromJson(responseBody, JsonArray.class);
                        if (array != null && !array.isEmpty()) {
                            JsonObject obj = array.get(0).getAsJsonObject();
                            String id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsString() : userId;
                            String username = obj.has("username") && !obj.get("username").isJsonNull() ? obj.get("username").getAsString() : "";
                            String fullName = obj.has("full_name") && !obj.get("full_name").isJsonNull() ? obj.get("full_name").getAsString() : "";

                            String imgUrl = null;
                            if (obj.has("user_img") && !obj.get("user_img").isJsonNull()) {
                                JsonElement imgElem = obj.get("user_img");
                                if (imgElem.isJsonArray() && imgElem.getAsJsonArray().size() > 0) {
                                    JsonObject firstImg = imgElem.getAsJsonArray().get(0).getAsJsonObject();
                                    if (firstImg.has("img_url") && !firstImg.get("img_url").isJsonNull()) {
                                        imgUrl = firstImg.get("img_url").getAsString();
                                    }
                                } else if (imgElem.isJsonObject()) {
                                    JsonObject imgObj = imgElem.getAsJsonObject();
                                    if (imgObj.has("img_url") && !imgObj.get("img_url").isJsonNull()) {
                                        imgUrl = imgObj.get("img_url").getAsString();
                                    }
                                }
                            }
                            callback.onSuccess(new User(id, username, fullName, imgUrl));
                        } else {
                            callback.onError("Perfil no encontrado.");
                        }
                    } catch (Exception e) {
                        callback.onError("Error al parsear perfil: " + e.getMessage());
                    }
                } else {
                    callback.onError("Error (" + response.code() + ") al obtener perfil: " + responseBody);
                }
            }
        });
    }

    // 12. Obtener o crear una conversación individual
    public void getOrCreateConversation(String currentUserId, String recipientUserId, GetOrCreateConversationCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay sesión activa.");
            return;
        }

        String urlStep1 = supabaseUrl + "/rest/v1/conversaciones_members?profile_id=eq." + currentUserId + "&select=convo_id,conversaciones(id,is_group)";

        Request request1 = new Request.Builder()
                .url(urlStep1)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request1).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red al consultar conversaciones: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonArray membersArray = gson.fromJson(responseBody, JsonArray.class);
                        List<Long> candidateConvoIds = new ArrayList<>();
                        if (membersArray != null) {
                            for (JsonElement elem : membersArray) {
                                JsonObject obj = elem.getAsJsonObject();
                                if (obj.has("conversaciones") && !obj.get("conversaciones").isJsonNull()) {
                                    JsonObject convoObj = obj.getAsJsonObject("conversaciones");
                                    boolean isGroup = convoObj.has("is_group") && convoObj.get("is_group").getAsBoolean();
                                    if (!isGroup && convoObj.has("id")) {
                                        candidateConvoIds.add(convoObj.get("id").getAsLong());
                                    }
                                }
                            }
                        }

                        if (!candidateConvoIds.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < candidateConvoIds.size(); i++) {
                                if (i > 0) sb.append(",");
                                sb.append(candidateConvoIds.get(i));
                            }

                            String urlStep2 = supabaseUrl + "/rest/v1/conversaciones_members?profile_id=eq." + recipientUserId + "&convo_id=in.(" + sb + ")&select=convo_id";
                            Request request2 = new Request.Builder()
                                    .url(urlStep2)
                                    .addHeader("apikey", supabaseKey)
                                    .addHeader("Authorization", "Bearer " + token)
                                    .addHeader("Accept", "application/json")
                                    .get()
                                    .build();

                            client.newCall(request2).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    callback.onError("Error de red al verificar destinatario: " + e.getMessage());
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response2) throws IOException {
                                    String body2 = response2.body() != null ? response2.body().string() : "";
                                    if (response2.isSuccessful()) {
                                        try {
                                            JsonArray matchArray = gson.fromJson(body2, JsonArray.class);
                                            if (matchArray != null && !matchArray.isEmpty()) {
                                                JsonObject firstMatch = matchArray.get(0).getAsJsonObject();
                                                long existingConvoId = firstMatch.get("convo_id").getAsLong();
                                                callback.onSuccess(existingConvoId);
                                                return;
                                            }
                                        } catch (Exception ignored) {}
                                    }
                                    createNewConversation(currentUserId, recipientUserId, callback);
                                }
                            });
                        } else {
                            createNewConversation(currentUserId, recipientUserId, callback);
                        }
                    } catch (Exception e) {
                        callback.onError("Error al procesar conversaciones: " + e.getMessage());
                    }
                } else {
                    callback.onError("Error (" + response.code() + ") al consultar conversaciones: " + responseBody);
                }
            }
        });
    }

    private void createNewConversation(String currentUserId, String recipientUserId, GetOrCreateConversationCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay sesión activa.");
            return;
        }

        String urlConvo = supabaseUrl + "/rest/v1/conversaciones";
        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("convo_name", "Chat");
        jsonBody.addProperty("is_group", false);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(urlConvo)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red al crear conversación: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonArray createdArray = gson.fromJson(responseBody, JsonArray.class);
                        if (createdArray != null && !createdArray.isEmpty()) {
                            long convoId = createdArray.get(0).getAsJsonObject().get("id").getAsLong();

                            String urlMembers = supabaseUrl + "/rest/v1/conversaciones_members";
                            JsonArray membersJson = new JsonArray();

                            JsonObject m1 = new JsonObject();
                            m1.addProperty("profile_id", currentUserId);
                            m1.addProperty("convo_id", convoId);

                            JsonObject m2 = new JsonObject();
                            m2.addProperty("profile_id", recipientUserId);
                            m2.addProperty("convo_id", convoId);

                            membersJson.add(m1);
                            membersJson.add(m2);

                            RequestBody bodyMembers = RequestBody.create(membersJson.toString(), MediaType.parse("application/json; charset=utf-8"));
                            Request requestMembers = new Request.Builder()
                                    .url(urlMembers)
                                    .addHeader("apikey", supabaseKey)
                                    .addHeader("Authorization", "Bearer " + token)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Prefer", "return=representation")
                                    .post(bodyMembers)
                                    .build();

                            client.newCall(requestMembers).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    callback.onError("Error al registrar miembros: " + e.getMessage());
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response responseMembers) throws IOException {
                                    if (responseMembers.isSuccessful()) {
                                        callback.onSuccess(convoId);
                                    } else {
                                        callback.onError("Error al agregar miembros a la conversación.");
                                    }
                                }
                            });
                        } else {
                            callback.onError("No se pudo obtener el ID de la conversación creada.");
                        }
                    } catch (Exception e) {
                        callback.onError("Error al procesar creación de conversación: " + e.getMessage());
                    }
                } else {
                    callback.onError("Error (" + response.code() + ") al crear conversación: " + responseBody);
                }
            }
        });
    }

    // 13. Obtener todas las conversaciones del usuario
    public void getConversations(String currentUserId, ConversationsCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay sesión activa.");
            return;
        }

        String urlMembers = supabaseUrl + "/rest/v1/conversaciones_members?profile_id=eq." + currentUserId + "&select=id,convo_id,conversaciones(id,convo_name,is_group)";

        Request request = new Request.Builder()
                .url(urlMembers)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red al consultar tus chats: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonArray myMemberships = gson.fromJson(responseBody, JsonArray.class);
                        List<Conversation> resultList = new ArrayList<>();
                        if (myMemberships == null || myMemberships.isEmpty()) {
                            callback.onSuccess(resultList);
                            return;
                        }

                        int totalConversations = myMemberships.size();
                        final int[] completedCount = {0};

                        for (JsonElement elem : myMemberships) {
                            JsonObject mObj = elem.getAsJsonObject();
                            long myCmId = mObj.get("id").getAsLong();
                            long convoId = mObj.get("convo_id").getAsLong();

                            boolean isGroup = false;
                            String convoName = "Chat";
                            if (mObj.has("conversaciones") && !mObj.get("conversaciones").isJsonNull()) {
                                JsonObject cObj = mObj.getAsJsonObject("conversaciones");
                                if (cObj.has("is_group")) isGroup = cObj.get("is_group").getAsBoolean();
                                if (cObj.has("convo_name") && !cObj.get("convo_name").isJsonNull()) convoName = cObj.get("convo_name").getAsString();
                            }

                            final boolean finalIsGroup = isGroup;
                            final String finalConvoName = convoName;

                            String urlOther = supabaseUrl + "/rest/v1/conversaciones_members?convo_id=eq." + convoId + "&profile_id=neq." + currentUserId + "&select=id,profile_id,profiles(id,username,full_name,user_img(img_url))";
                            Request reqOther = new Request.Builder()
                                    .url(urlOther)
                                    .addHeader("apikey", supabaseKey)
                                    .addHeader("Authorization", "Bearer " + token)
                                    .addHeader("Accept", "application/json")
                                    .get()
                                    .build();

                            client.newCall(reqOther).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    checkComplete(totalConversations, completedCount, resultList, callback);
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response resOther) throws IOException {
                                    String bodyOther = resOther.body() != null ? resOther.body().string() : "";
                                    User otherUser = null;
                                    if (resOther.isSuccessful()) {
                                        try {
                                            JsonArray otherArr = gson.fromJson(bodyOther, JsonArray.class);
                                            if (otherArr != null && !otherArr.isEmpty()) {
                                                JsonObject otherObj = otherArr.get(0).getAsJsonObject();
                                                if (otherObj.has("profiles") && !otherObj.get("profiles").isJsonNull()) {
                                                    JsonObject pObj = otherObj.getAsJsonObject("profiles");
                                                    String uId = pObj.has("id") ? pObj.get("id").getAsString() : "";
                                                    String uName = pObj.has("username") && !pObj.get("username").isJsonNull() ? pObj.get("username").getAsString() : "";
                                                    String fName = pObj.has("full_name") && !pObj.get("full_name").isJsonNull() ? pObj.get("full_name").getAsString() : "";

                                                    String img = null;
                                                    if (pObj.has("user_img") && !pObj.get("user_img").isJsonNull()) {
                                                        JsonElement imgElem = pObj.get("user_img");
                                                        if (imgElem.isJsonArray() && imgElem.getAsJsonArray().size() > 0) {
                                                            JsonObject fImg = imgElem.getAsJsonArray().get(0).getAsJsonObject();
                                                            if (fImg.has("img_url") && !fImg.get("img_url").isJsonNull()) img = fImg.get("img_url").getAsString();
                                                        } else if (imgElem.isJsonObject()) {
                                                            JsonObject fObj = imgElem.getAsJsonObject();
                                                            if (fObj.has("img_url") && !fObj.get("img_url").isJsonNull()) img = fObj.get("img_url").getAsString();
                                                        }
                                                    }
                                                    otherUser = new User(uId, uName, fName, img);
                                                }
                                            }
                                        } catch (Exception ignored) {}
                                    }

                                    fetchLastMessage(token, convoId, finalConvoName, finalIsGroup, otherUser, myCmId, resultList, completedCount, totalConversations, callback);
                                }
                            });
                        }
                    } catch (Exception e) {
                        callback.onError("Error al procesar tus conversaciones: " + e.getMessage());
                    }
                } else {
                    callback.onError("Error (" + response.code() + ") al obtener tus chats: " + responseBody);
                }
            }
        });
    }

    private void fetchLastMessage(String token, long convoId, String convoName, boolean isGroup, User otherUser, long myCmId, List<Conversation> resultList, int[] completedCount, int totalConversations, ConversationsCallback callback) {
        String urlCmIds = supabaseUrl + "/rest/v1/conversaciones_members?convo_id=eq." + convoId + "&select=id";
        Request reqCm = new Request.Builder()
                .url(urlCmIds)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(reqCm).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                synchronized (resultList) {
                    resultList.add(new Conversation(convoId, convoName, isGroup, otherUser, "Sin mensajes", "", myCmId));
                }
                checkComplete(totalConversations, completedCount, resultList, callback);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response resCm) throws IOException {
                String bodyCm = resCm.body() != null ? resCm.body().string() : "";
                String cmCsv = "";
                if (resCm.isSuccessful()) {
                    try {
                        JsonArray cmArr = gson.fromJson(bodyCm, JsonArray.class);
                        if (cmArr != null && !cmArr.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < cmArr.size(); i++) {
                                if (i > 0) sb.append(",");
                                sb.append(cmArr.get(i).getAsJsonObject().get("id").getAsLong());
                            }
                            cmCsv = sb.toString();
                        }
                    } catch (Exception ignored) {}
                }

                if (!cmCsv.isEmpty()) {
                    String urlMsg = supabaseUrl + "/rest/v1/messages?cm_id=in.(" + cmCsv + ")&select=message,created_at&order=created_at.desc&limit=1";
                    Request reqMsg = new Request.Builder()
                            .url(urlMsg)
                            .addHeader("apikey", supabaseKey)
                            .addHeader("Authorization", "Bearer " + token)
                            .addHeader("Accept", "application/json")
                            .get()
                            .build();

                    client.newCall(reqMsg).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            synchronized (resultList) {
                                resultList.add(new Conversation(convoId, convoName, isGroup, otherUser, "Sin mensajes", "", myCmId));
                            }
                            checkComplete(totalConversations, completedCount, resultList, callback);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response resMsg) throws IOException {
                            String bodyMsg = resMsg.body() != null ? resMsg.body().string() : "";
                            String lastText = "Sin mensajes";
                            String lastTime = "";
                            if (resMsg.isSuccessful()) {
                                try {
                                    JsonArray msgArr = gson.fromJson(bodyMsg, JsonArray.class);
                                    if (msgArr != null && !msgArr.isEmpty()) {
                                        JsonObject mObj = msgArr.get(0).getAsJsonObject();
                                        if (mObj.has("message") && !mObj.get("message").isJsonNull()) {
                                            lastText = mObj.get("message").getAsString();
                                        }
                                        if (mObj.has("created_at") && !mObj.get("created_at").isJsonNull()) {
                                            lastTime = mObj.get("created_at").getAsString();
                                        }
                                    }
                                } catch (Exception ignored) {}
                            }
                            synchronized (resultList) {
                                resultList.add(new Conversation(convoId, convoName, isGroup, otherUser, lastText, lastTime, myCmId));
                            }
                            checkComplete(totalConversations, completedCount, resultList, callback);
                        }
                    });
                } else {
                    synchronized (resultList) {
                        resultList.add(new Conversation(convoId, convoName, isGroup, otherUser, "Sin mensajes", "", myCmId));
                    }
                    checkComplete(totalConversations, completedCount, resultList, callback);
                }
            }
        });
    }

    private synchronized void checkComplete(int total, int[] completed, List<Conversation> resultList, ConversationsCallback callback) {
        completed[0]++;
        if (completed[0] >= total) {
            callback.onSuccess(resultList);
        }
    }

    // 14. Enviar un mensaje a una conversación
    public void sendMessage(long convoId, String senderUserId, String content, SendMessageCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay sesión activa.");
            return;
        }

        String urlCm = supabaseUrl + "/rest/v1/conversaciones_members?convo_id=eq." + convoId + "&profile_id=eq." + senderUserId + "&select=id";
        Request reqCm = new Request.Builder()
                .url(urlCm)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(reqCm).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red al obtener miembro: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response resCm) throws IOException {
                String bodyCm = resCm.body() != null ? resCm.body().string() : "";
                if (resCm.isSuccessful()) {
                    try {
                        JsonArray cmArr = gson.fromJson(bodyCm, JsonArray.class);
                        if (cmArr != null && !cmArr.isEmpty()) {
                            long cmId = cmArr.get(0).getAsJsonObject().get("id").getAsLong();

                            String urlMsg = supabaseUrl + "/rest/v1/messages";
                            JsonObject jsonBody = new JsonObject();
                            jsonBody.addProperty("message", content);
                            jsonBody.addProperty("cm_id", cmId);
                            jsonBody.addProperty("estado", "ENVIADO");

                            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
                            Request reqMsg = new Request.Builder()
                                    .url(urlMsg)
                                    .addHeader("apikey", supabaseKey)
                                    .addHeader("Authorization", "Bearer " + token)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Prefer", "return=representation")
                                    .post(body)
                                    .build();

                            client.newCall(reqMsg).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    callback.onError("Error de red al enviar mensaje: " + e.getMessage());
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response resInsert) throws IOException {
                                    String bodyInsert = resInsert.body() != null ? resInsert.body().string() : "";
                                    if (resInsert.isSuccessful()) {
                                        try {
                                            JsonArray createdArr = gson.fromJson(bodyInsert, JsonArray.class);
                                            if (createdArr != null && !createdArr.isEmpty()) {
                                                JsonObject mObj = createdArr.get(0).getAsJsonObject();
                                                long msgId = mObj.get("id").getAsLong();
                                                String text = mObj.get("message").getAsString();
                                                String createdAt = mObj.has("created_at") ? mObj.get("created_at").getAsString() : "";
                                                String estado = mObj.has("estado") && !mObj.get("estado").isJsonNull() ? mObj.get("estado").getAsString() : "ENVIADO";
                                                Message sentMsg = new Message(msgId, text, cmId, senderUserId, createdAt, estado);
                                                callback.onSuccess(sentMsg);
                                            } else {
                                                callback.onError("No se pudo obtener el mensaje enviado.");
                                            }
                                        } catch (Exception e) {
                                            callback.onError("Error al procesar mensaje enviado: " + e.getMessage());
                                        }
                                    } else {
                                        callback.onError("Error (" + resInsert.code() + ") al enviar mensaje: " + bodyInsert);
                                    }
                                }
                            });
                        } else {
                            callback.onError("No se encontró la membresía del usuario en el chat.");
                        }
                    } catch (Exception e) {
                        callback.onError("Error al consultar miembros: " + e.getMessage());
                    }
                } else {
                    callback.onError("Error (" + resCm.code() + ") al verificar usuario: " + bodyCm);
                }
            }
        });
    }

    // 15. Consultar el historial de mensajes
    public void getMessages(long convoId, String currentUserId, MessagesCallback callback) {
        String token = getAccessToken();
        if (token == null) {
            callback.onError("No hay sesión activa.");
            return;
        }

        String urlMembers = supabaseUrl + "/rest/v1/conversaciones_members?convo_id=eq." + convoId + "&select=id,profile_id";
        Request reqMembers = new Request.Builder()
                .url(urlMembers)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(reqMembers).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error de red al consultar historial de mensajes: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response resMembers) throws IOException {
                String bodyMembers = resMembers.body() != null ? resMembers.body().string() : "";
                if (resMembers.isSuccessful()) {
                    try {
                        JsonArray membersArr = gson.fromJson(bodyMembers, JsonArray.class);
                        if (membersArr == null || membersArr.isEmpty()) {
                            callback.onSuccess(new ArrayList<>());
                            return;
                        }

                        Map<Long, String> cmIdToSenderMap = new HashMap<>();
                        StringBuilder sbCm = new StringBuilder();

                        for (int i = 0; i < membersArr.size(); i++) {
                            JsonObject mObj = membersArr.get(i).getAsJsonObject();
                            long cmId = mObj.get("id").getAsLong();
                            String profileId = mObj.get("profile_id").getAsString();
                            cmIdToSenderMap.put(cmId, profileId);

                            if (i > 0) sbCm.append(",");
                            sbCm.append(cmId);
                        }

                        String urlMsgs = supabaseUrl + "/rest/v1/messages?cm_id=in.(" + sbCm + ")&select=id,message,cm_id,estado,created_at&order=created_at.asc";
                        Request reqMsgs = new Request.Builder()
                                .url(urlMsgs)
                                .addHeader("apikey", supabaseKey)
                                .addHeader("Authorization", "Bearer " + token)
                                .addHeader("Accept", "application/json")
                                .get()
                                .build();

                        client.newCall(reqMsgs).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                callback.onError("Error de red al cargar historial: " + e.getMessage());
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response resMsgs) throws IOException {
                                String bodyMsgs = resMsgs.body() != null ? resMsgs.body().string() : "";
                                if (resMsgs.isSuccessful()) {
                                    try {
                                        JsonArray msgsArr = gson.fromJson(bodyMsgs, JsonArray.class);
                                        List<Message> messageList = new ArrayList<>();
                                        if (msgsArr != null) {
                                            for (JsonElement elem : msgsArr) {
                                                JsonObject mObj = elem.getAsJsonObject();
                                                long id = mObj.get("id").getAsLong();
                                                String content = mObj.get("message").getAsString();
                                                long cmId = mObj.get("cm_id").getAsLong();
                                                String createdAt = mObj.has("created_at") ? mObj.get("created_at").getAsString() : "";
                                                String estado = mObj.has("estado") && !mObj.get("estado").isJsonNull() ? mObj.get("estado").getAsString() : "ENVIADO";
                                                String senderId = cmIdToSenderMap.getOrDefault(cmId, "");

                                                messageList.add(new Message(id, content, cmId, senderId, createdAt, estado));
                                            }
                                        }
                                        callback.onSuccess(messageList);
                                    } catch (Exception e) {
                                        callback.onError("Error al parsear mensajes: " + e.getMessage());
                                    }
                                } else {
                                    callback.onError("Error (" + resMsgs.code() + ") al obtener historial de mensajes: " + bodyMsgs);
                                }
                            }
                        });
                    } catch (Exception e) {
                        callback.onError("Error al procesar miembros de la conversación: " + e.getMessage());
                    }
                } else {
                    callback.onError("Error (" + resMembers.code() + ") al consultar la conversación: " + bodyMembers);
                }
            }
        });
    }

    // 16. Marcar todos los mensajes recibidos no leídos como LEIDO
    public void markMessagesAsRead(long convoId, String currentUserId, AuthCallback callback) {
        String token = getAccessToken();
        if (token == null) return;

        String urlOtherCm = supabaseUrl + "/rest/v1/conversaciones_members?convo_id=eq." + convoId + "&profile_id=neq." + currentUserId + "&select=id";
        Request req = new Request.Builder()
                .url(urlOtherCm)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    try {
                        JsonArray cmArr = gson.fromJson(body, JsonArray.class);
                        if (cmArr != null && !cmArr.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < cmArr.size(); i++) {
                                if (i > 0) sb.append(",");
                                sb.append(cmArr.get(i).getAsJsonObject().get("id").getAsLong());
                            }

                            String patchUrl = supabaseUrl + "/rest/v1/messages?cm_id=in.(" + sb + ")&estado=neq.LEIDO";
                            JsonObject patchBody = new JsonObject();
                            patchBody.addProperty("estado", "LEIDO");

                            RequestBody reqBody = RequestBody.create(patchBody.toString(), MediaType.parse("application/json; charset=utf-8"));
                            Request patchReq = new Request.Builder()
                                    .url(patchUrl)
                                    .addHeader("apikey", supabaseKey)
                                    .addHeader("Authorization", "Bearer " + token)
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Prefer", "return=minimal")
                                    .patch(reqBody)
                                    .build();

                            client.newCall(patchReq).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {}

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response resPatch) throws IOException {
                                    if (resPatch.isSuccessful() && callback != null) {
                                        callback.onSuccess("Mensajes marcados como leídos.");
                                    }
                                }
                            });
                        }
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    // 17. Suscribirse a mensajes y estado en tiempo real
    public void subscribeToRealtimeMessages(long convoId, RealtimeMessageCallback callback) {
        unsubscribeRealtimeMessages();

        String urlMembers = supabaseUrl + "/rest/v1/conversaciones_members?convo_id=eq." + convoId + "&select=id,profile_id";
        Request reqMembers = new Request.Builder()
                .url(urlMembers)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + getAccessToken())
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(reqMembers).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Error al obtener miembros para tiempo real: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    Map<Long, String> cmIdToSenderMap = new HashMap<>();
                    try {
                        JsonArray membersArr = gson.fromJson(body, JsonArray.class);
                        if (membersArr != null) {
                            for (JsonElement elem : membersArr) {
                                JsonObject obj = elem.getAsJsonObject();
                                long cmId = obj.get("id").getAsLong();
                                String profileId = obj.get("profile_id").getAsString();
                                cmIdToSenderMap.put(cmId, profileId);
                            }
                        }
                    } catch (Exception ignored) {}

                    connectRealtimeWebSocket(convoId, cmIdToSenderMap, callback);
                } else {
                    callback.onError("Error (" + response.code() + ") al preparar tiempo real.");
                }
            }
        });
    }

    private void connectRealtimeWebSocket(long convoId, Map<Long, String> cmIdToSenderMap, RealtimeMessageCallback callback) {
        String wsUrl = supabaseUrl.replace("https://", "wss://").replace("http://", "ws://") 
                + "/realtime/v1/websocket?apikey=" + supabaseKey + "&vsn=1.0.0";

        Request request = new Request.Builder()
                .url(wsUrl)
                .build();

        String topicName = "realtime:convo_" + convoId;

        realtimeWebSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
                JsonObject joinPayload = new JsonObject();
                joinPayload.addProperty("topic", topicName);
                joinPayload.addProperty("event", "phx_join");

                JsonObject configObj = new JsonObject();
                JsonObject broadcastObj = new JsonObject();
                broadcastObj.addProperty("self", false);
                configObj.add("broadcast", broadcastObj);

                JsonArray postgresChanges = new JsonArray();

                JsonObject change1 = new JsonObject();
                change1.addProperty("event", "INSERT");
                change1.addProperty("schema", "public");
                change1.addProperty("table", "messages");

                JsonObject change2 = new JsonObject();
                change2.addProperty("event", "UPDATE");
                change2.addProperty("schema", "public");
                change2.addProperty("table", "messages");

                postgresChanges.add(change1);
                postgresChanges.add(change2);

                configObj.add("postgres_changes", postgresChanges);

                JsonObject payloadObj = new JsonObject();
                payloadObj.add("config", configObj);
                joinPayload.add("payload", payloadObj);
                joinPayload.addProperty("ref", "1");

                webSocket.send(joinPayload.toString());

                heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
                heartbeatExecutor.scheduleWithFixedDelay(() -> {
                    try {
                        JsonObject hb = new JsonObject();
                        hb.addProperty("topic", "phoenix");
                        hb.addProperty("event", "heartbeat");
                        hb.add("payload", new JsonObject());
                        hb.addProperty("ref", "heartbeat");
                        webSocket.send(hb.toString());
                    } catch (Exception ignored) {}
                }, 25, 25, TimeUnit.SECONDS);
            }

            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                try {
                    JsonObject msgObj = gson.fromJson(text, JsonObject.class);

                    if (msgObj.has("payload") && !msgObj.get("payload").isJsonNull()) {
                        JsonObject payload = msgObj.getAsJsonObject("payload");

                        if (payload.has("type") && "broadcast".equalsIgnoreCase(payload.get("type").getAsString())) {
                            if (payload.has("event") && "typing".equalsIgnoreCase(payload.get("event").getAsString())) {
                                if (payload.has("payload") && !payload.get("payload").isJsonNull()) {
                                    JsonObject innerPayload = payload.getAsJsonObject("payload");
                                    if (innerPayload.has("sender_id") && innerPayload.has("is_typing")) {
                                        String senderId = innerPayload.get("sender_id").getAsString();
                                        if (!senderId.equals(getUserId())) {
                                            callback.onTypingStatusChanged(innerPayload.get("is_typing").getAsBoolean());
                                        }
                                        return;
                                    }
                                }
                            }
                        }

                        if (payload.has("data") && !payload.get("data").isJsonNull()) {
                            JsonObject data = payload.getAsJsonObject("data");
                            String changeType = data.has("type") ? data.get("type").getAsString() : "";

                            if (data.has("record") && !data.get("record").isJsonNull()) {
                                JsonObject record = data.getAsJsonObject("record");

                                if ("INSERT".equalsIgnoreCase(changeType) && record.has("cm_id") && record.has("message")) {
                                    long cmId = record.get("cm_id").getAsLong();
                                    if (cmIdToSenderMap.containsKey(cmId)) {
                                        long msgId = record.has("id") ? record.get("id").getAsLong() : System.currentTimeMillis();
                                        String content = record.get("message").getAsString();
                                        String senderId = cmIdToSenderMap.get(cmId);
                                        String createdAt = record.has("created_at") && !record.get("created_at").isJsonNull() ? record.get("created_at").getAsString() : "";
                                        String estado = record.has("estado") && !record.get("estado").isJsonNull() ? record.get("estado").getAsString() : "RECIBIDO";

                                        Message newMsg = new Message(msgId, content, cmId, senderId, createdAt, estado);
                                        callback.onNewMessage(newMsg);
                                    }
                                } else if ("UPDATE".equalsIgnoreCase(changeType) && record.has("id") && record.has("estado")) {
                                    long msgId = record.get("id").getAsLong();
                                    String newEstado = record.get("estado").getAsString();
                                    callback.onMessageStateUpdated(msgId, newEstado);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }

            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                callback.onError("Error de conexión WebSocket Realtime: " + t.getMessage());
            }
        });
    }

    // 18. Enviar señal de "Escribiendo..."
    public void sendTypingSignal(long convoId, String senderId, boolean isTyping) {
        if (realtimeWebSocket != null) {
            try {
                JsonObject typingObj = new JsonObject();
                typingObj.addProperty("topic", "realtime:convo_" + convoId);
                typingObj.addProperty("event", "broadcast");

                JsonObject payload = new JsonObject();
                payload.addProperty("type", "broadcast");
                payload.addProperty("event", "typing");

                JsonObject innerPayload = new JsonObject();
                innerPayload.addProperty("sender_id", senderId);
                innerPayload.addProperty("is_typing", isTyping);

                payload.add("payload", innerPayload);
                typingObj.add("payload", payload);
                typingObj.addProperty("ref", "typing");

                realtimeWebSocket.send(typingObj.toString());
            } catch (Exception ignored) {}
        }
    }

    public void unsubscribeRealtimeMessages() {
        if (heartbeatExecutor != null && !heartbeatExecutor.isShutdown()) {
            heartbeatExecutor.shutdown();
            heartbeatExecutor = null;
        }
        if (realtimeWebSocket != null) {
            realtimeWebSocket.close(1000, "Unsubscribing");
            realtimeWebSocket = null;
        }
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
