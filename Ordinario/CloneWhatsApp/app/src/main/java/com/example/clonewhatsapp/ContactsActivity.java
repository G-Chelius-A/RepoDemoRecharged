package com.example.clonewhatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity implements ContactAdapter.OnContactClickListener {

    public static final String EXTRA_SELECTED_USER_ID = "extra_selected_user_id";
    public static final String EXTRA_SELECTED_USER_NAME = "extra_selected_user_name";
    public static final String EXTRA_SELECTED_USER_IMG = "extra_selected_user_img";
    public static final String EXTRA_SELECTED_USER = "extra_selected_user";
    public static final String EXTRA_CONVO_ID = "extra_convo_id";

    private EditText etSearch;
    private RecyclerView rvContacts;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private SupabaseAuthManager authManager;
    private ContactAdapter adapter;
    private String currentUserId;
    private final List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        authManager = new SupabaseAuthManager(this);
        currentUserId = authManager.getUserId();

        etSearch = findViewById(R.id.etSearch);
        rvContacts = findViewById(R.id.rvContacts);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(this, userList, this);
        rvContacts.setAdapter(adapter);

        setupSearchListener();
        loadContacts();
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                    checkEmptyState(adapter.getItemCount());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadContacts() {
        showLoading(true);
        authManager.getUsers(new SupabaseAuthManager.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                runOnUiThread(() -> {
                    showLoading(false);
                    userList.clear();
                    if (users != null) {
                        for (User user : users) {
                            if (currentUserId != null && currentUserId.equals(user.getId())) {
                                continue;
                            }
                            userList.add(user);
                        }
                    }
                    adapter.updateUsers(userList);
                    checkEmptyState(userList.size());
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ContactsActivity.this, error, Toast.LENGTH_LONG).show();
                    checkEmptyState(0);
                });
            }
        });
    }

    private void checkEmptyState(int count) {
        if (count == 0) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvContacts.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvContacts.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onContactClick(User user) {
        if (currentUserId == null) {
            currentUserId = authManager.getUserId();
        }

        showLoading(true);
        authManager.getOrCreateConversation(currentUserId, user.getId(), new SupabaseAuthManager.GetOrCreateConversationCallback() {
            @Override
            public void onSuccess(long convoId) {
                runOnUiThread(() -> {
                    showLoading(false);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(EXTRA_SELECTED_USER_ID, user.getId());
                    resultIntent.putExtra(EXTRA_SELECTED_USER_NAME, user.getDisplayName());
                    resultIntent.putExtra(EXTRA_SELECTED_USER_IMG, user.getImgUrl());
                    resultIntent.putExtra(EXTRA_SELECTED_USER, user);
                    resultIntent.putExtra(EXTRA_CONVO_ID, convoId);

                    setResult(RESULT_OK, resultIntent);

                    // Abrir directamente la pantalla de Chat
                    Intent chatIntent = new Intent(ContactsActivity.this, ChatActivity.class);
                    chatIntent.putExtra(ChatActivity.EXTRA_CONVO_ID, convoId);
                    chatIntent.putExtra(ChatActivity.EXTRA_RECIPIENT_USER, user);
                    startActivity(chatIntent);

                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ContactsActivity.this, "Error al iniciar conversación: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
