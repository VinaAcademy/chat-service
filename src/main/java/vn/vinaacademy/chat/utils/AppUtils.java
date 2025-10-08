package vn.vinaacademy.chat.utils;

import java.security.Principal;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import vn.vinaacademy.security.exception.AccessDeniedException;

@UtilityClass
public class AppUtils {
  public static UUID getSenderId(Principal principal) {
    UUID senderId;
    try {
      senderId = UUID.fromString(principal.getName());
    } catch (IllegalArgumentException e) {
      throw new AccessDeniedException("Invalid user ID format in WebSocket message");
    }
    return senderId;
  }
}
