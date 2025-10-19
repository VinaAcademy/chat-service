package vn.vinaacademy.chat.utils;

import java.security.Principal;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.springframework.messaging.MessagingException;

@UtilityClass
public class AppUtils {
  public static UUID getSenderId(Principal principal) {
    UUID senderId;
    try {
      senderId = UUID.fromString(principal.getName());
    } catch (IllegalArgumentException e) {
      throw new MessagingException("AUTH_ERROR: Invalid user ID format in WebSocket message");
    }
    return senderId;
  }
}
