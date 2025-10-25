package vn.vinaacademy.chat.service.impl;

import static vn.vinaacademy.chat.constants.WebsocketConstants.ONLINE_USERS_WEBSOCKET_TOPIC;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.vinaacademy.chat.service.UserPresenceService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPresenceServiceImpl implements UserPresenceService {
  private final RedisTemplate<String, Object> redisTemplate;
  private final SimpMessagingTemplate messagingTemplate;

  @Value("${app.redis.online-users-key:online_users}")
  private String onlineUsersKey;

  @Override
  public void userConnected(UUID userId) {
    log.info("User connected: {}", userId);
    redisTemplate.opsForSet().add(onlineUsersKey, userId);
    Set<Object> onlineUsers = redisTemplate.opsForSet().members(onlineUsersKey);
    if (onlineUsers == null) {
      onlineUsers = Set.of();
      log.warn("Online users set is null after adding userId={}", userId);
    }
    messagingTemplate.convertAndSend(ONLINE_USERS_WEBSOCKET_TOPIC, onlineUsers);
  }

  @Override
  public void userDisconnected(UUID userId) {
    log.info("User disconnected: {}", userId);
    redisTemplate.opsForSet().remove(onlineUsersKey, userId);
    Set<Object> onlineUsers = redisTemplate.opsForSet().members(onlineUsersKey);
    if (onlineUsers == null) {
      onlineUsers = Set.of();
    }
    messagingTemplate.convertAndSend(ONLINE_USERS_WEBSOCKET_TOPIC, onlineUsers);
  }

  @Override
  public Set<UUID> getOnlineUsers() {
    Set<Object> onlineUsers = redisTemplate.opsForSet().members(onlineUsersKey);
    if (onlineUsers == null) {
      log.info("Online users set is null when retrieving online users");
      return Set.of();
    }
    Set<UUID> onlineUserIds = new HashSet<>();
    for (Object userIdObj : onlineUsers) {
      try {
        UUID userId = UUID.fromString(userIdObj.toString());
        onlineUserIds.add(userId);
      } catch (IllegalArgumentException e) {
        log.error("Failed to parse online user ID to UUID: {}", userIdObj, e);
      }
    }
    return onlineUserIds;
  }
}
