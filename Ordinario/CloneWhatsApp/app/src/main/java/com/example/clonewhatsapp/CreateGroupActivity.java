package com.example.clonewhatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText etGroupName;
    private RecyclerView rvMembers;
    private ProgressBar progressBar;
    private Button btnCreateGroup;

    private SupabaseAuthManager authManager;
    private GroupMemberAdapter adapter;
    private String currentUserId;
    private final List<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        View root = findViewById(android.R.id.content);
        if (root != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                androidx.core.graphics.Insets statusBarInsets = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars());
                v.setPadding(0, statusBarInsets.top, 0, 0);
                return insets;
            });
        }

        authManager = new SupabaseAuthManager(this);
        currentUserId = authManager.getUserId();

        etGroupName = findViewById(R.id.etGroupName);
        rvMembers = findViewById(R.id.rvMembers);
        progressBar = findViewById(R.id.progressBar);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);

        rvMembers.setLayoutManager(new LinearLayoutManager(this));

        loadUsers();

        btnCreateGroup.setOnClickListener(v -> performCreateGroup());
    }

    private void loadUsers() {
        showLoading(true);
        authManager.getUsers(new SupabaseAuthManager.UsersCallback() {
            @Override
            public void onSuccess(List<User> users) {
                runOnUiThread(() -> {
                    showLoading(false);
                    userList.clear();
                    if (users != null) {
                        for (User u : users) {
                            if (currentUserId != null && currentUserId.equals(u.getId())) {
                                continue;
                            }
                            userList.add(u);
                        }
                    }
                    adapter = new GroupMemberAdapter(CreateGroupActivity.this, userList);
                    rvMembers.setAdapter(adapter);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CreateGroupActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void performCreateGroup() {
        String groupName = etGroupName.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Ingresa un nombre para el grupo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (adapter == null) return;
        List<String> selectedIds = adapter.getSelectedUserIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos un integrante", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId != null && !selectedIds.contains(currentUserId)) {
            selectedIds.add(currentUserId);
        }

        showLoading(true);
        btnCreateGroup.setEnabled(false);

        authManager.createGroupConversation(groupName, selectedIds, new SupabaseAuthManager.GetOrCreateConversationCallback() {
            @Override
            public void onSuccess(long convoId) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CreateGroupActivity.this, "¡Grupo creado exitosamente!", Toast.LENGTH_SHORT).show();

                    User groupUser = new User("group_" + convoId, groupName, groupName, null);
                    Intent intent = new Intent(CreateGroupActivity.this, ChatActivity.class);
                    intent.putExtra(ChatActivity.EXTRA_CONVO_ID, convoId);
                    intent.putExtra(ChatActivity.EXTRA_RECIPIENT_USER, groupUser);
                    startActivity(intent);

                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    btnCreateGroup.setEnabled(true);
                    Toast.makeText(CreateGroupActivity.this, "Error al crear grupo: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
