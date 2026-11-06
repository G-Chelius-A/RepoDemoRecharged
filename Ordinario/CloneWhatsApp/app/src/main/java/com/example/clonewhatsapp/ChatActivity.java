package com.example.clonewhatsapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CONVO_ID = "extra_convo_id";
    public static final String EXTRA_RECIPIENT_USER = "extra_recipient_user";

    private ImageView ivRecipientAvatar;
    private TextView tvRecipientName, tvRecipientSubtext;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;

    private SupabaseAuthManager authManager;
    private MessageAdapter messageAdapter;
    private final List<Message> messageList = new ArrayList<>();

    private long convoId = -1;
    private User recipientUser;
    private String currentUserId;

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private static final long POLL_INTERVAL_MS = 1500; // 1.5 segundos para respuesta ultrarrápida

    private final Handler typingHandler = new Handler(Looper.getMainLooper());
    private Runnable stopTypingRunnable;

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
        progressBar = findViewById(R.id.progressBar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);

        messageAdapter = new MessageAdapter(this, currentUserId, messageList);
        rvMessages.setAdapter(messageAdapter);

        setupHeader();
        setupTypingListener();

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> performSendMessage());

        loadInitialMessages();
    }

    private void setupHeader() {
        if (recipientUser != null) {
            tvRecipientName.setText(recipientUser.getDisplayName());
            String subtext = recipientUser.getUsername() != null ? "@" + recipientUser.getUsername() : "En línea";
            tvRecipientSubtext.setText(subtext);

            if (recipientUser.getImgUrl() != null && !recipientUser.getImgUrl().trim().isEmpty()) {
                Glide.with(this)
                        .load(recipientUser.getImgUrl())
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.ic_launcher_round)
                        .circleCrop()
                        .into(ivRecipientAvatar);
            } else {
                ivRecipientAvatar.setImageResource(R.mipmap.ic_launcher_round);
            }
        }
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

    private void scrollToBottom() {
        if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
            rvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
