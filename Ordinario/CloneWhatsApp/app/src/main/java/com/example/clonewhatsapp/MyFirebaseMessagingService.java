package com.example.clonewhatsapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public static final String CHANNEL_ID = "clone_whatsapp_messages";
    public static final String CHANNEL_NAME = "Notificaciones de Chat";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token FCM generado: " + token);

        SupabaseAuthManager authManager = new SupabaseAuthManager(getApplicationContext());
        authManager.saveFcmToken(token);

        String currentUserId = authManager.getUserId();
        if (currentUserId != null) {
            authManager.registerDeviceTokenInSupabase(currentUserId, token, null);
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Mensaje FCM recibido de: " + remoteMessage.getFrom());

        String title = "Nuevo Mensaje";
        String body = "";
        long convoId = -1;

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        if (remoteMessage.getData().size() > 0) {
            if (remoteMessage.getData().containsKey("title")) {
                title = remoteMessage.getData().get("title");
            }
            if (remoteMessage.getData().containsKey("body")) {
                body = remoteMessage.getData().get("body");
            }
            if (remoteMessage.getData().containsKey("convo_id")) {
                try {
                    convoId = Long.parseLong(remoteMessage.getData().get("convo_id"));
                } catch (Exception ignored) {}
            }
        }

        showLocalNotification(this, title, body, convoId);
    }

    public static void showLocalNotification(Context context, String title, String body, long convoId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Permiso POST_NOTIFICATIONS no concedido.");
                return;
            }
        }

        createNotificationChannel(context);

        Intent intent;
        if (convoId != -1) {
            intent = new Intent(context, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CONVO_ID, convoId);
        } else {
            intent = new Intent(context, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
        }
    }

    public static void showLocalNotification(Context context, String title, String body) {
        showLocalNotification(context, title, body, -1);
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones para mensajes entrantes de CloneWhatsApp");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
