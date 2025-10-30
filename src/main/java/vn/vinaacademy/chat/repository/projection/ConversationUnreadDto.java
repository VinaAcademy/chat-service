package vn.vinaacademy.chat.repository.projection;

import java.util.UUID;

public record ConversationUnreadDto(UUID conversationId, long unreadCount) {}
