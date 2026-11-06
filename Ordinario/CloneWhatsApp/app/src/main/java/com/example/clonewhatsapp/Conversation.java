package com.example.clonewhatsapp;

import java.io.Serializable;

public class Conversation implements Serializable {
    private long convoId;
    private String convoName;
    private boolean isGroup;
    private User otherUser;
    private String lastMessage;
    private String lastMessageTime;
    private long currentMemberId;

    public Conversation() {
    }

    public Conversation(long convoId, String convoName, boolean isGroup, User otherUser, String lastMessage, String lastMessageTime, long currentMemberId) {
        this.convoId = convoId;
        this.convoName = convoName;
        this.isGroup = isGroup;
        this.otherUser = otherUser;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.currentMemberId = currentMemberId;
    }

    public long getConvoId() {
        return convoId;
    }

    public void setConvoId(long convoId) {
        this.convoId = convoId;
    }

    public String getConvoName() {
        return convoName;
    }

    public void setConvoName(String convoName) {
        this.convoName = convoName;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public User getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(User otherUser) {
        this.otherUser = otherUser;
    }

    public String getLastMessage() {
        return lastMessage != null && !lastMessage.trim().isEmpty() ? lastMessage : "Sin mensajes";
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public long getCurrentMemberId() {
        return currentMemberId;
    }

    public void setCurrentMemberId(long currentMemberId) {
        this.currentMemberId = currentMemberId;
    }

    public String getDisplayName() {
        if (!isGroup && otherUser != null) {
            return otherUser.getDisplayName();
        }
        if (convoName != null && !convoName.trim().isEmpty()) {
            return convoName;
        }
        return "Conversación";
    }
}
