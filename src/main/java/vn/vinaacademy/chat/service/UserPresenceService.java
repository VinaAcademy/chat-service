package vn.vinaacademy.chat.service;

import java.util.Set;
import java.util.UUID;

public interface UserPresenceService {

  void userConnected(UUID userId);

  void userDisconnected(UUID userId);

  Set<UUID> getOnlineUsers();
}
