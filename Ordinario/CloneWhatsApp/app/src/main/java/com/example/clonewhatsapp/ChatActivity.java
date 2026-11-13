package com.example.clonewhatsapp;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements MessageAdapter.OnMessageLongClickListener, MessageAdapter.OnImageClickListener {

    public static final String EXTRA_CONVO_ID = "extra_convo_id";
    public static final String EXTRA_RECIPIENT_USER = "extra_recipient_user";

    private ImageView ivRecipientAvatar;
    private TextView tvRecipientName, tvRecipientSubtext;
    private RecyclerView rvMessages;
    private EditText etMessage, etSearchMessage;
    private ImageButton btnSend, ibSearch, ibCloseSearch;
    private LinearLayout layoutHeaderNormal, layoutSearchBar;
    private ProgressBar progressBar;

    private SupabaseAuthManager authManager;
    private MessageAdapter messageAdapter;
    private final List<Message> messageList = new ArrayList<>();

    private long convoId = -1;
    private User recipientUser;
    private String currentUserId;

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private static final long POLL_INTERVAL_MS = 1500;

    private final Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable stopTypingRunnable;

    private final ActivityResultLauncher<String> attachImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadAndSendImage(uri);
                }
            }
    );

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (convoId != -1) {
                fetchMessagesSilently();
            }
            pollHandler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        View root = findViewById(android.R.id.content);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.setPadding(0, statusBarInsets.top, 0, 0);
                return insets;
            });
        }

        authManager = new SupabaseAuthManager(this);
        currentUserId = authManager.getUserId();

        convoId = getIntent().getLongExtra(EXTRA_CONVO_ID, -1);
        if (getIntent().hasExtra(EXTRA_RECIPIENT_USER)) {
            recipientUser = (User) getIntent().getSerializableExtra(EXTRA_RECIPIENT_USER);
        }

        if (convoId == -1 || currentUserId == null) {
            Toast.makeText(this, "Error al abrir la conversación", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        ivRecipientAvatar = findViewById(R.id.ivRecipientAvatar);
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvRecipientSubtext = findViewById(R.id.tvRecipientSubtext);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        ImageButton ibAttach = findViewById(R.id.ibAttach);
        ibSearch = findViewById(R.id.ibSearch);
        ibCloseSearch = findViewById(R.id.ibCloseSearch);
        etSearchMessage = findViewById(R.id.etSearchMessage);
        layoutHeaderNormal = findViewById(R.id.layoutHeaderNormal);
        layoutSearchBar = findViewById(R.id.layoutSearchBar);
        progressBar = findViewById(R.id.progressBar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(this, currentUserId, messageList, this, this);
        rvMessages.setAdapter(messageAdapter);

        setupHeader();
        setupTypingListener();
        setupSearchListener();

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> performSendMessage());
        ibAttach.setOnClickListener(v -> attachImageLauncher.launch("image/*"));

        ibSearch.setOnClickListener(v -> toggleSearch(true));
        ibCloseSearch.setOnClickListener(v -> toggleSearch(false));

        layoutHeaderNormal.setOnClickListener(v -> showConversationInfoDialog());

        loadInitialMessages();
    }

    private void setupHeader() {
        authManager.getConversationDetails(convoId, new SupabaseAuthManager.ConversationDetailsCallback() {
            @Override
            public void onSuccess(String convoName, boolean isGroup, String createdAt, List<User> members) {
                runOnUiThread(() -> {
                    if (isGroup) {
                        tvRecipientName.setText(convoName);
                        tvRecipientSubtext.setText("Grupo · " + members.size() + " integrantes");
                        ivRecipientAvatar.setImageResource(R.drawable.ic_group_24);
                    } else if (recipientUser != null) {
                        tvRecipientName.setText(recipientUser.getDisplayName());
                        String subtext = recipientUser.getUsername() != null ? "@" + recipientUser.getUsername() : "En línea";
                        tvRecipientSubtext.setText(subtext);

                        if (recipientUser.getImgUrl() != null && !recipientUser.getImgUrl().trim().isEmpty()) {
                            Glide.with(ChatActivity.this)
                                    .load(recipientUser.getImgUrl())
                                    .placeholder(R.drawable.ic_default_avatar)
                                    .error(R.drawable.ic_default_avatar)
                                    .circleCrop()
                                    .into(ivRecipientAvatar);
                        } else {
                            ivRecipientAvatar.setImageResource(R.drawable.ic_default_avatar);
                        }
                    } else {
                        tvRecipientName.setText(convoName);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (recipientUser != null) {
                        tvRecipientName.setText(recipientUser.getDisplayName());
                    }
                });
            }
        });
    }

    private void showConversationInfoDialog() {
        showLoading(true);
        authManager.getConversationDetails(convoId, new SupabaseAuthManager.ConversationDetailsCallback() {
            @Override
            public void onSuccess(String convoName, boolean isGroup, String createdAt, List<User> members) {
                runOnUiThread(() -> {
                    showLoading(false);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                    builder.setTitle(isGroup ? "👥 " + convoName : "👤 " + (recipientUser != null ? recipientUser.getDisplayName() : convoName));

                    StringBuilder sb = new StringBuilder();
                    String formattedTime = createdAt;
                    if (createdAt != null && createdAt.contains("T")) {
                        try {
                            String[] parts = createdAt.split("T");
                            formattedTime = parts[0] + " " + parts[1].substring(0, 5);
                        } catch (Exception ignored) {}
                    }
                    sb.append("📅 Creado el: ").append(formattedTime).append("\n\n");

                    if (isGroup) {
                        sb.append("👥 Miembros (").append(members.size()).append("):\n");
                        for (User u : members) {
                            sb.append("• ").append(u.getDisplayName());
                            if (u.getUsername() != null && !u.getUsername().isEmpty()) {
                                sb.append(" (@").append(u.getUsername()).append(")");
                            }
                            sb.append("\n");
                        }
                    } else if (recipientUser != null) {
                        sb.append("📧 Correo/Usuario: ").append(recipientUser.getUsername() != null ? "@" + recipientUser.getUsername() : "Contacto");
                    }

                    builder.setMessage(sb.toString().trim());
                    builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
                    builder.show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ChatActivity.this, "Error al obtener info: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupTypingListener() {
        stopTypingRunnable = () -> authManager.sendTypingSignal(convoId, currentUserId, false);

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                authManager.sendTypingSignal(convoId, currentUserId, true);
                typingHandler.removeCallbacks(stopTypingRunnable);
                typingHandler.postDelayed(stopTypingRunnable, 1500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupSearchListener() {
        etSearchMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (messageAdapter != null) {
                    messageAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void toggleSearch(boolean showSearch) {
        if (showSearch) {
            layoutHeaderNormal.setVisibility(View.GONE);
            layoutSearchBar.setVisibility(View.VISIBLE);
            etSearchMessage.requestFocus();
        } else {
            layoutHeaderNormal.setVisibility(View.VISIBLE);
            layoutSearchBar.setVisibility(View.GONE);
            etSearchMessage.setText("");
            if (messageAdapter != null) {
                messageAdapter.filter("");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribeToRealtime();
        pollHandler.post(pollRunnable);
        markRead();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable);
        typingHandler.removeCallbacks(stopTypingRunnable);
        authManager.sendTypingSignal(convoId, currentUserId, false);
        authManager.unsubscribeRealtimeMessages();
    }

    private void markRead() {
        authManager.markMessagesAsRead(convoId, currentUserId, null);
    }

    private void subscribeToRealtime() {
        if (convoId == -1) return;

        authManager.subscribeToRealtimeMessages(convoId, new SupabaseAuthManager.RealtimeMessageCallback() {
            @Override
            public void onNewMessage(Message newMsg) {
                runOnUiThread(() -> {
                    boolean exists = false;
                    for (Message m : messageList) {
                        if (m.getId() == newMsg.getId()) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        messageList.add(newMsg);
                        messageAdapter.updateMessages(messageList);
                        scrollToBottom();
                        markRead();
                    }
                });
            }

            @Override
            public void onMessageStateUpdated(long messageId, String newEstado) {
                runOnUiThread(() -> {
                    for (Message m : messageList) {
                        if (m.getId() == messageId) {
                            m.setEstado(newEstado);
                            messageAdapter.updateMessages(messageList);
                            break;
                        }
                    }
                });
            }

            @Override
            public void onTypingStatusChanged(boolean isTyping) {
                runOnUiThread(() -> {
                    if (isTyping) {
                        tvRecipientSubtext.setText("Escribiendo... 💬");
                    } else {
                        setupHeader();
                    }
                });
            }

            @Override
            public void onError(String error) {}
        });
    }

    private void loadInitialMessages() {
        showLoading(true);
        authManager.getMessages(convoId, currentUserId, new SupabaseAuthManager.MessagesCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                runOnUiThread(() -> {
                    showLoading(false);
                    messageList.clear();
                    if (messages != null) {
                        messageList.addAll(messages);
                    }
                    messageAdapter.updateMessages(messageList);
                    scrollToBottom();
                    markRead();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchMessagesSilently() {
        authManager.getMessages(convoId, currentUserId, new SupabaseAuthManager.MessagesCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                runOnUiThread(() -> {
                    if (messages != null) {
                        boolean changed = false;
                        if (messages.size() != messageList.size()) {
                            changed = true;
                        } else {
                            for (int i = 0; i < messages.size(); i++) {
                                Message oldM = messageList.get(i);
                                Message newM = messages.get(i);
                                if (oldM.getId() != newM.getId() || 
                                    !equalsNullable(oldM.getEstado(), newM.getEstado()) ||
                                    !equalsNullable(oldM.getContent(), newM.getContent())) {
                                    changed = true;
                                    break;
                                }
                            }
                        }

                        if (changed) {
                            messageList.clear();
                            messageList.addAll(messages);
                            messageAdapter.updateMessages(messageList);
                            scrollToBottom();
                            markRead();
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {}
        });
    }

    private boolean equalsNullable(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private void performSendMessage() {
        String content = etMessage.getText().toString().trim();
        if (content.isEmpty()) return;

        etMessage.setText("");
        btnSend.setEnabled(false);

        authManager.sendMessage(convoId, currentUserId, content, new SupabaseAuthManager.SendMessageCallback() {
            @Override
            public void onSuccess(Message sentMessage) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    boolean exists = false;
                    for (Message m : messageList) {
                        if (m.getId() == sentMessage.getId()) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        messageList.add(sentMessage);
                        messageAdapter.updateMessages(messageList);
                        scrollToBottom();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    Toast.makeText(ChatActivity.this, "Error al enviar mensaje: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void uploadAndSendImage(Uri imageUri) {
        showLoading(true);
        authManager.uploadChatImage(this, imageUri, new SupabaseAuthManager.UserImageCallback() {
            @Override
            public void onSuccess(String imgUrl) {
                runOnUiThread(() -> {
                    showLoading(false);
                    authManager.sendMessage(convoId, currentUserId, "[IMAGE]:" + imgUrl, new SupabaseAuthManager.SendMessageCallback() {
                        @Override
                        public void onSuccess(Message message) {
                            runOnUiThread(() -> {
                                messageList.add(message);
                                messageAdapter.updateMessages(messageList);
                                scrollToBottom();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show());
                        }
                    });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ChatActivity.this, "Error al subir imagen: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onImageClick(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) return;

        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image);

        ImageView ivFull = dialog.findViewById(R.id.ivFullImage);
        ImageButton btnClose = dialog.findViewById(R.id.btnCloseFullImage);

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .into(ivFull);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        ivFull.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onMessageLongClick(Message message) {
        if (!currentUserId.equals(message.getSenderId())) {
            return; // Solo se permite editar/eliminar mensajes propios
        }

        CharSequence[] options = new CharSequence[]{"Editar mensaje", "Eliminar mensaje"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones de mensaje");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showEditDialog(message);
            } else if (which == 1) {
                showDeleteDialog(message);
            }
        });
        builder.show();
    }

    private void showEditDialog(Message message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar mensaje");

        final EditText input = new EditText(this);
        input.setText(message.getContent().startsWith("[IMAGE]:") ? "" : message.getContent());
        input.setSelection(input.getText().length());
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String newText = input.getText().toString().trim();
            if (!newText.isEmpty()) {
                authManager.editMessage(message.getId(), newText, new SupabaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        runOnUiThread(() -> fetchMessagesSilently());
                    }

                    @Override
                    public void onError(String err) {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this, err, Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteDialog(Message message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminar mensaje");
        builder.setMessage("¿Deseas eliminar este mensaje para todos?");
        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            authManager.deleteMessage(message.getId(), new SupabaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String msg) {
                    runOnUiThread(() -> fetchMessagesSilently());
                }

                @Override
                public void onError(String err) {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, err, Toast.LENGTH_SHORT).show());
                }
            });
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void scrollToBottom() {
        if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
            rvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
