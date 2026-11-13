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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context context;
    private final String currentUserId;
    private final List<Message> originalList;
    private final List<Message> filteredList;
    private final OnMessageLongClickListener longClickListener;
    private final OnImageClickListener imageClickListener;

    public interface OnMessageLongClickListener {
        void onMessageLongClick(Message message);
    }

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    public MessageAdapter(Context context, String currentUserId, List<Message> messages, OnMessageLongClickListener longClickListener, OnImageClickListener imageClickListener) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.originalList = new ArrayList<>(messages);
        this.filteredList = new ArrayList<>(messages);
        this.longClickListener = longClickListener;
        this.imageClickListener = imageClickListener;
    }

    public MessageAdapter(Context context, String currentUserId, List<Message> messages, OnMessageLongClickListener longClickListener) {
        this(context, currentUserId, messages, longClickListener, null);
    }

    public MessageAdapter(Context context, String currentUserId, List<Message> messages) {
        this(context, currentUserId, messages, null, null);
    }

    public void updateMessages(List<Message> newMessages) {
        this.originalList.clear();
        this.originalList.addAll(newMessages);
        this.filteredList.clear();
        this.filteredList.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Message message : originalList) {
                if (message.getContent() != null && message.getContent().toLowerCase().contains(filterPattern)) {
                    filteredList.add(message);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = filteredList.get(position);
        if (currentUserId != null && currentUserId.equals(message.getSenderId())) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = viewType == VIEW_TYPE_SENT ? R.layout.item_message_sent : R.layout.item_message_received;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = filteredList.get(position);
        String content = message.getContent() != null ? message.getContent() : "";

        // Manejo de mensajes de imagen ([IMAGE]:https://...)
        if (content.startsWith("[IMAGE]:")) {
            String imageUrl = content.substring(8);
            if (holder.ivMessageImage != null) {
                holder.ivMessageImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(holder.ivMessageImage);

                holder.ivMessageImage.setOnClickListener(v -> {
                    if (imageClickListener != null) {
                        imageClickListener.onImageClick(imageUrl);
                    }
                });
            }
            holder.tvMessage.setVisibility(View.GONE);
        } else {
            if (holder.ivMessageImage != null) {
                holder.ivMessageImage.setVisibility(View.GONE);
            }
            holder.tvMessage.setVisibility(View.VISIBLE);
            holder.tvMessage.setText(content);
        }

        String rawTime = message.getCreatedAt();
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

        if (holder.tvStatus != null) {
            String estado = message.getEstado();
            if (estado == null) estado = "ENVIADO";

            switch (estado.toUpperCase()) {
                case "ENVIANDO":
                    holder.tvStatus.setText(" ⏳");
                    holder.tvStatus.setAlpha(0.6f);
                    break;
                case "RECIBIDO":
                    holder.tvStatus.setText(" ✓✓");
                    holder.tvStatus.setAlpha(0.8f);
                    break;
                case "LEIDO":
                    holder.tvStatus.setText(" ✓✓");
                    holder.tvStatus.setAlpha(1.0f);
                    break;
                case "ENVIADO":
                default:
                    holder.tvStatus.setText(" ✓");
                    holder.tvStatus.setAlpha(0.8f);
                    break;
            }
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onMessageLongClick(message);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMessageImage;
        TextView tvMessage, tvTime, tvStatus;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMessageImage = itemView.findViewById(R.id.ivMessageImage);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
