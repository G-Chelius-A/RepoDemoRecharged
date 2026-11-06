package com.example.clonewhatsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private final Context context;
    private final List<User> originalList;
    private final List<User> filteredList;
    private final OnContactClickListener listener;

    public interface OnContactClickListener {
        void onContactClick(User user);
    }

    public ContactAdapter(Context context, List<User> users, OnContactClickListener listener) {
        this.context = context;
        this.originalList = new ArrayList<>(users);
        this.filteredList = new ArrayList<>(users);
        this.listener = listener;
    }

    public void updateUsers(List<User> newUsers) {
        this.originalList.clear();
        this.originalList.addAll(newUsers);
        this.filteredList.clear();
        this.filteredList.addAll(newUsers);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (User user : originalList) {
                boolean matchesName = user.getFullName() != null && user.getFullName().toLowerCase().contains(filterPattern);
                boolean matchesUsername = user.getUsername() != null && user.getUsername().toLowerCase().contains(filterPattern);
                if (matchesName || matchesUsername) {
                    filteredList.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        User user = filteredList.get(position);

        holder.tvName.setText(user.getDisplayName());
        String usernameText = user.getUsername() != null ? "@" + user.getUsername() : "";
        holder.tvUsername.setText(usernameText);

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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvUsername;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvUsername = itemView.findViewById(R.id.tvUsername);
        }
    }
}
