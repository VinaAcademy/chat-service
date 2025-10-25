package vn.vinaacademy.chat.utils;

import java.security.Principal;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.springframework.messaging.MessagingException;
import vn.vinaacademy.security.exception.AccessDeniedException;

@UtilityClass
public class AppUtils {
  public static UUID getUserIdByPrincipal(Principal principal) {
    if (principal == null) {
      throw new AccessDeniedException("Principal is required");
    }
    UUID senderId;
    try {
      senderId = UUID.fromString(principal.getName());
    } catch (IllegalArgumentException e) {
      throw new MessagingException("AUTH_ERROR: Invalid user ID format in WebSocket message");
    }
    return senderId;
  }
}
