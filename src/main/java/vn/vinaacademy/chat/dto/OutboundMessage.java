package vn.vinaacademy.chat.dto;

public record OutboundMessage(String fromUserId, String toUserId, String content, long timestamp) {}
