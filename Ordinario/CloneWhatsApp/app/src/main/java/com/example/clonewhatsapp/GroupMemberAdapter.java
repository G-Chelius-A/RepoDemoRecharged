package com.example.clonewhatsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder> {

    private final Context context;
    private final List<User> userList;
    private final Set<String> selectedUserIds = new HashSet<>();

    public GroupMemberAdapter(Context context, List<User> users) {
        this.context = context;
        this.userList = new ArrayList<>(users);
    }

    public List<String> getSelectedUserIds() {
        return new ArrayList<>(selectedUserIds);
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvName.setText(user.getDisplayName());
        String usernameText = user.getUsername() != null ? "@" + user.getUsername() : "";
        holder.tvUsername.setText(usernameText);

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(selectedUserIds.contains(user.getId()));

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedUserIds.add(user.getId());
            } else {
                selectedUserIds.remove(user.getId());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            boolean current = selectedUserIds.contains(user.getId());
            if (current) {
                selectedUserIds.remove(user.getId());
                holder.cbSelect.setChecked(false);
            } else {
                selectedUserIds.add(user.getId());
                holder.cbSelect.setChecked(true);
            }
        });

        if (user.getImgUrl() != null && !user.getImgUrl().trim().isEmpty()) {
            Glide.with(context)
                    .load(user.getImgUrl())
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvUsername;
        CheckBox cbSelect;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            cbSelect = itemView.findViewById(R.id.cbSelect);
        }
    }
}
