package com.example.clonewhatsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final Context context;
    private final String currentUserId;
    private final List<Message> messageList;

    public MessageAdapter(Context context, String currentUserId, List<Message> messages) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.messageList = new ArrayList<>(messages);
    }

    public void updateMessages(List<Message> newMessages) {
        this.messageList.clear();
        this.messageList.addAll(newMessages);
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        this.messageList.add(message);
        notifyItemInserted(messageList.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
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
        Message message = messageList.get(position);

        holder.tvMessage.setText(message.getContent());

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
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvStatus;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
