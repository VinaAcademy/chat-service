package vn.vinaacademy.chat.service.impl;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import vn.vinaacademy.chat.domain.ConversationDomainService;
import vn.vinaacademy.chat.dto.MessageDto;
import vn.vinaacademy.chat.dto.request.GroupMessage;
import vn.vinaacademy.chat.dto.request.PrivateMessage;
import vn.vinaacademy.chat.entity.Conversation;
import vn.vinaacademy.chat.entity.Message;
import vn.vinaacademy.chat.mapper.MessageMapper;
import vn.vinaacademy.chat.repository.ConversationRepository;
import vn.vinaacademy.chat.repository.MessageRepository;
import vn.vinaacademy.chat.service.KafkaMessageService;
import vn.vinaacademy.chat.service.MessageService;
import vn.vinaacademy.security.authentication.SecurityContextHolder;
import vn.vinaacademy.security.authentication.UserContext;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
  private final MessageRepository messageRepository;
  private final ConversationRepository conversationRepository;
  private final KafkaMessageService kafkaMessageService;
  private final ConversationDomainService conversationDomainService;

  @Override
  public List<MessageDto> getMessagesByRecipientId(UUID recipientId, int page, int size) {
    UserContext userContext = SecurityContextHolder.getContext();
    UUID currentUserId = UUID.fromString(userContext.getUserId());
    Conversation conversation =
        conversationDomainService.getOrCreateDirectConversation(currentUserId, recipientId);
    Pageable pageable = Pageable.ofSize(size).withPage(page);
    List<Message> messages =
        messageRepository.findByConversationIdOrderByCreatedAtDesc(conversation.getId(), pageable);
    return MessageMapper.INSTANCE.toDtoList(messages);
  }

  @Override
  public List<MessageDto> getMessagesByConversationId(UUID conversationId, int page, int size) {
    UserContext userContext = SecurityContextHolder.getContext();
    UUID currentUserId = UUID.fromString(userContext.getUserId());

    // Check if user is a member of the conversation
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new AccessDeniedException("Access denied: Conversation not found"));

    boolean isParticipant =
        conversationDomainService.isUserInConversation(currentUserId, conversation);
    if (!isParticipant) {
      throw new AccessDeniedException(
          "Access denied: User is not a participant in the conversation");
    }

    Pageable pageable = Pageable.ofSize(size).withPage(page);
    List<Message> messages =
        messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable);
    return MessageMapper.INSTANCE.toDtoList(messages);
  }

  @Override
  @Transactional
  public void sendPrivateMessage(PrivateMessage messageDto, UUID senderId) {
    Conversation conversation =
        conversationDomainService.getOrCreateDirectConversation(
            senderId, messageDto.getRecipientId());

    Message message = MessageMapper.INSTANCE.fromPrivateMessage(messageDto, conversation, senderId);

    Message savedMessage = messageRepository.save(message);

    conversation.setLastMessage(savedMessage);
    conversationRepository.save(conversation);

    MessageDto result = MessageMapper.INSTANCE.toDto(savedMessage);
    String senderIdStr = String.valueOf(message.getSenderId());
    if (!Objects.equals(senderIdStr, messageDto.getRecipientId())) {
      kafkaMessageService.sendPrivateMessage(senderIdStr, result);
    }
    kafkaMessageService.sendPrivateMessage(String.valueOf(messageDto.getRecipientId()), result);
  }

  @Override
  @Transactional
  public void sendGroupMessage(GroupMessage messageDto, UUID senderId) {
    UUID conversationId = messageDto.getConversationId();

    // Validate that conversation exists and sender is a member
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new AccessDeniedException("Access denied: Conversation not found"));

    boolean isParticipant = conversationDomainService.isUserInConversation(senderId, conversation);
    if (!isParticipant) {
      throw new AccessDeniedException(
          "Access denied: User is not a participant in the conversation");
    }

    // Create message
    Message message = MessageMapper.INSTANCE.fromGroupMessage(messageDto, conversation, senderId);
    Message savedMessage = messageRepository.save(message);

    // Update conversation's last message
    conversation.setLastMessage(savedMessage);
    conversationRepository.save(conversation);

    MessageDto result = MessageMapper.INSTANCE.toDto(savedMessage);

    // Send to all group members via Kafka
    kafkaMessageService.sendGroupMessage(String.valueOf(messageDto.getConversationId()), result);
  }
}
