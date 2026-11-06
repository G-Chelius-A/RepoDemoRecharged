package com.example.clonewhatsapp;

import java.io.Serializable;

public class Message implements Serializable {
    private long id;
    private String content;
    private long cmId;
    private String senderId;
    private String createdAt;
    private String estado;

    public Message() {
    }

    public Message(long id, String content, long cmId, String senderId, String createdAt) {
        this(id, content, cmId, senderId, createdAt, "ENVIADO");
    }

    public Message(long id, String content, long cmId, String senderId, String createdAt, String estado) {
        this.id = id;
        this.content = content;
        this.cmId = cmId;
        this.senderId = senderId;
        this.createdAt = createdAt;
        this.estado = estado != null ? estado : "ENVIADO";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCmId() {
        return cmId;
    }

    public void setCmId(long cmId) {
        this.cmId = cmId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
