package vn.vinaacademy.chat.controller;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import vn.vinaacademy.chat.dto.OutboundMessage;
import vn.vinaacademy.chat.dto.PrivateMessage;
import vn.vinaacademy.chat.service.KafkaMessageService;
import vn.vinaacademy.security.exception.AccessDeniedException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PrivateChatController {

  private final KafkaMessageService kafkaMessageService;

  // Client gửi lên /app/pm
  @MessageMapping("/pm")
  public void handlePrivate(@Payload PrivateMessage msg, Principal principal) {
    if (principal == null || principal.getName() == null) {
      throw new AccessDeniedException("Unauthenticated WebSocket message");
    }
    // principal.getName() chính là userId đã set trong JwtHandshakeInterceptor
    String senderId = principal.getName();

    log.info("Received private message from user: {} to user: {}", senderId, msg.getToUserId());

    // Tạo message với thông tin đầy đủ để gửi qua Kafka
    var outboundMessage =
        new OutboundMessage(
            senderId, msg.getToUserId(), msg.getContent(), System.currentTimeMillis());

    // Gửi message qua Kafka - Kafka consumer sẽ forward đến WebSocket
    kafkaMessageService.sendPrivateMessage(msg.getToUserId(), outboundMessage);

    // Optionally: gửi echo về người gửi để update UI
    kafkaMessageService.sendPrivateMessage(senderId, outboundMessage);
  }
}
