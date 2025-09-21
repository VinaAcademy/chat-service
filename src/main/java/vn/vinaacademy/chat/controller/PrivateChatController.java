package vn.vinaacademy.chat.controller;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.vinaacademy.chat.dto.PrivateMessage;

@Controller
@RequiredArgsConstructor
public class PrivateChatController {

  private final SimpMessagingTemplate template;

  // Client gửi lên /app/pm
  @MessageMapping("/pm")
  public void handlePrivate(@Payload PrivateMessage msg, Principal principal) {
    // principal.getName() chính là userId đã set trong JwtHandshakeInterceptor
    String senderId = principal.getName();

    // Tùy ý đóng gói lại payload trả xuống người nhận
    var outbound = new OutboundMessage(senderId, msg.getContent(), System.currentTimeMillis());

    // Đẩy đến đích riêng của user nhận: /user/{toUserId}/queue/pm
    template.convertAndSendToUser(msg.getToUserId(), "/queue/pm", outbound);

    // Optionally: đẩy echo về người gửi để update UI
    template.convertAndSendToUser(senderId, "/queue/pm", outbound);
  }

  record OutboundMessage(String fromUserId, String content, long ts) {}
}
