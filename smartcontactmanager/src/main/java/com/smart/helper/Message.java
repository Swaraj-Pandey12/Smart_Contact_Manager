// src/main/java/com/yourproject/utils/Message.java (or similar package)
package com.smart.helper;// Adjust package  needed

public class Message {
    private String content;
    private String type; // e.g., "success", "error", "warning", "info"

    public Message(String content, String type) {
        this.content = content;
        this.type = type;
    }

    // Getters
    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    // You might want to add setters if messages are mutable, but for simple display, getters are enough.

    // Optional: Override toString() for better debugging, though not strictly necessary for Thymeleaf.
    @Override
    public String toString() {
        return "Message [content=" + content + ", type=" + type + "]";
    }
}