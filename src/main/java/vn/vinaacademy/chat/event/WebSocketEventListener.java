package vn.vinaacademy.chat.event;

import static vn.vinaacademy.chat.utils.AppUtils.getUserIdByPrincipal;

import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import vn.vinaacademy.chat.service.UserPresenceService;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

  private final UserPresenceService presenceService;

  @EventListener
  public void handleConnect(SessionConnectedEvent event) {
    Principal principal = event.getUser();
    if (principal == null) {
      log.warn("Unauthenticated user attempted to connect via WebSocket");
      return;
    }
    UUID userId = getUserIdByPrincipal(principal);
    presenceService.userConnected(userId);
    log.info("User connected: {}", userId);
  }

  @EventListener
  public void handleDisconnect(SessionDisconnectEvent event) {
    Principal principal = event.getUser();
    UUID userId = principal != null ? getUserIdByPrincipal(principal) : null;
    presenceService.userDisconnected(userId);
    log.info("User disconnected: {}", userId);
  }
}
