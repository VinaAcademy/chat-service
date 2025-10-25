package vn.vinaacademy.chat.service;

import java.util.UUID;
import org.springframework.data.domain.Page;
import vn.vinaacademy.chat.dto.MessageDto;
import vn.vinaacademy.chat.dto.request.GroupMessage;
import vn.vinaacademy.chat.dto.request.PrivateMessage;

public interface MessageService {
  Page<MessageDto> getMessagesByRecipientId(UUID recipientId, int page, int size);

  Page<MessageDto> getMessagesByConversationId(UUID conversationId, int page, int size);

  void sendPrivateMessage(PrivateMessage messageDto, UUID senderId);

  void sendGroupMessage(GroupMessage messageDto, UUID senderId);
}
