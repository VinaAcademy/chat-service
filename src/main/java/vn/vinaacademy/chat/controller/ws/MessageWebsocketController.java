package vn.vinaacademy.chat.controller.ws;

import static vn.vinaacademy.chat.utils.AppUtils.getSenderId;

import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import vn.vinaacademy.chat.dto.request.GroupMessage;
import vn.vinaacademy.chat.dto.request.PrivateMessage;
import vn.vinaacademy.chat.service.MessageService;
import vn.vinaacademy.security.exception.AccessDeniedException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MessageWebsocketController {
  private final MessageService messageService;

  @MessageMapping("/pm")
  public void handlePrivateMessage(PrivateMessage msg, Principal principal) {
    if (principal == null || principal.getName() == null) {
      throw new AccessDeniedException("Unauthenticated WebSocket message");
    }
    UUID senderId = getSenderId(principal);
    log.info("Received private message from user: {} to user: {}", senderId, msg.getRecipientId());
    messageService.sendPrivateMessage(msg, senderId);
  }

  @MessageMapping("/group")
  public void handleGroupMessage(@Payload GroupMessage msg, Principal principal) {
    if (principal == null || principal.getName() == null) {
      throw new AccessDeniedException("Unauthenticated WebSocket message");
    }
    UUID senderId = getSenderId(principal);
    log.info(
        "Received group message from user: {} to conversation: {}",
        senderId,
        msg.getConversationId());
    messageService.sendGroupMessage(msg, senderId);
  }
}
