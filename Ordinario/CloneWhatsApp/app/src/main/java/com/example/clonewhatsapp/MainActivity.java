package com.example.clonewhatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ConversationAdapter.OnConversationClickListener {

    private SupabaseAuthManager authManager;
    private ImageView ivUserAvatar, ivRecipientAvatar;
    private TextView tvWelcomeUser, tvUserSubtext, tvRecipientName, tvRecipientId, tvEmptyConvos;
    private MaterialCardView cardSelectedRecipient;
    private ProgressBar progressBarConvos;
    private RecyclerView rvConversations;

    private ConversationAdapter conversationAdapter;
    private final List<Conversation> conversationList = new ArrayList<>();
    private String currentUserId;

    private final ActivityResultLauncher<Intent> contactsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    String recipientId = data.getStringExtra(ContactsActivity.EXTRA_SELECTED_USER_ID);
                    String recipientName = data.getStringExtra(ContactsActivity.EXTRA_SELECTED_USER_NAME);
                    String recipientImg = data.getStringExtra(ContactsActivity.EXTRA_SELECTED_USER_IMG);
                    long convoId = data.getLongExtra(ContactsActivity.EXTRA_CONVO_ID, -1);

                    displaySelectedRecipient(recipientId, recipientName, recipientImg, convoId);
                    loadConversations();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = new SupabaseAuthManager(this);

        if (!authManager.isSessionActive()) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        currentUserId = authManager.getUserId();

        ivUserAvatar = findViewById(R.id.ivUserAvatar);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvUserSubtext = findViewById(R.id.tvUserSubtext);

        Button btnMyProfile = findViewById(R.id.btnMyProfile);
        Button btnOpenContacts = findViewById(R.id.btnOpenContacts);
        Button btnSignOut = findViewById(R.id.btnSignOut);

        cardSelectedRecipient = findViewById(R.id.cardSelectedRecipient);
        ivRecipientAvatar = findViewById(R.id.ivRecipientAvatar);
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvRecipientId = findViewById(R.id.tvRecipientId);

        progressBarConvos = findViewById(R.id.progressBarConvos);
        tvEmptyConvos = findViewById(R.id.tvEmptyConvos);
        rvConversations = findViewById(R.id.rvConversations);

        rvConversations.setLayoutManager(new LinearLayoutManager(this));
        conversationAdapter = new ConversationAdapter(this, conversationList, this);
        rvConversations.setAdapter(conversationAdapter);

        btnMyProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        btnOpenContacts.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
            contactsLauncher.launch(intent);
        });

        btnSignOut.setOnClickListener(v -> performSignOut());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (authManager.isSessionActive()) {
            loadUserProfile();
            loadConversations();
        }
    }

    private void loadUserProfile() {
        if (currentUserId == null) {
            currentUserId = authManager.getUserId();
        }
        if (currentUserId == null) return;

        authManager.getUserProfile(currentUserId, new SupabaseAuthManager.UserProfileCallback() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    if (user != null) {
                        tvWelcomeUser.setText(user.getDisplayName());
                        String usernameText = user.getUsername() != null ? "@" + user.getUsername() : authManager.getUserEmail();
                        tvUserSubtext.setText(usernameText);

                        if (user.getImgUrl() != null && !user.getImgUrl().trim().isEmpty()) {
                            Glide.with(MainActivity.this)
                                    .load(user.getImgUrl())
                                    .placeholder(R.mipmap.ic_launcher_round)
                                    .error(R.mipmap.ic_launcher_round)
                                    .circleCrop()
                                    .into(ivUserAvatar);
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    if (authManager.getUserEmail() != null) {
                        tvWelcomeUser.setText(authManager.getUserEmail());
                    }
                });
            }
        });
    }

    private void loadConversations() {
        if (currentUserId == null) {
            currentUserId = authManager.getUserId();
        }
        if (currentUserId == null) return;

        showConvoLoading(true);
        authManager.getConversations(currentUserId, new SupabaseAuthManager.ConversationsCallback() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                runOnUiThread(() -> {
                    showConvoLoading(false);
                    conversationList.clear();
                    if (conversations != null) {
                        conversationList.addAll(conversations);
                    }
                    conversationAdapter.updateConversations(conversationList);
                    checkEmptyConvosState(conversationList.size());
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showConvoLoading(false);
                    checkEmptyConvosState(0);
                });
            }
        });
    }

    private void checkEmptyConvosState(int count) {
        if (count == 0) {
            tvEmptyConvos.setVisibility(View.VISIBLE);
            rvConversations.setVisibility(View.GONE);
        } else {
            tvEmptyConvos.setVisibility(View.GONE);
            rvConversations.setVisibility(View.VISIBLE);
        }
    }

    private void showConvoLoading(boolean loading) {
        progressBarConvos.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void displaySelectedRecipient(String id, String name, String imgUrl, long convoId) {
        if (id == null) return;

        cardSelectedRecipient.setVisibility(View.VISIBLE);
        tvRecipientName.setText(name != null ? name : "Contacto");
        tvRecipientId.setText("Conversación ID: " + convoId + " | Usuario ID: " + id);

        if (imgUrl != null && !imgUrl.trim().isEmpty()) {
            Glide.with(this)
                    .load(imgUrl)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .circleCrop()
                    .into(ivRecipientAvatar);
        } else {
            ivRecipientAvatar.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CONVO_ID, conversation.getConvoId());
        intent.putExtra(ChatActivity.EXTRA_RECIPIENT_USER, conversation.getOtherUser());
        startActivity(intent);
    }

    private void performSignOut() {
        authManager.signOut(new SupabaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                });
            }
        });
    }

    private void redirectToLogin() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}
