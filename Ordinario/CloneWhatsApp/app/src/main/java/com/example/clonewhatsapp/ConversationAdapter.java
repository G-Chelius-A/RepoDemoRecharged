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

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private final Context context;
    private final List<Conversation> conversationList;
    private final OnConversationClickListener listener;

    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    public ConversationAdapter(Context context, List<Conversation> conversations, OnConversationClickListener listener) {
        this.context = context;
        this.conversationList = new ArrayList<>(conversations);
        this.listener = listener;
    }

    public void updateConversations(List<Conversation> newConversations) {
        this.conversationList.clear();
        this.conversationList.addAll(newConversations);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversationList.get(position);

        holder.tvName.setText(conversation.getDisplayName());
        holder.tvLastMessage.setText(conversation.getLastMessage());

        String rawTime = conversation.getLastMessageTime();
        if (rawTime != null && rawTime.contains("T")) {
            try {
                String timePart = rawTime.split("T")[1];
                if (timePart.contains(":")) {
                    String[] parts = timePart.split(":");
                    holder.tvTime.setText(parts[0] + ":" + parts[1]);
                } else {
                    holder.tvTime.setText(timePart);
                }
            } catch (Exception e) {
                holder.tvTime.setText(rawTime);
            }
        } else {
            holder.tvTime.setText(rawTime != null ? rawTime : "");
        }

        User otherUser = conversation.getOtherUser();
        if (otherUser != null && otherUser.getImgUrl() != null && !otherUser.getImgUrl().trim().isEmpty()) {
            Glide.with(context)
                    .load(otherUser.getImgUrl())
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.ic_launcher_round)
                    .circleCrop()
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.mipmap.ic_launcher_round);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvName, tvLastMessage, tvTime;

        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
