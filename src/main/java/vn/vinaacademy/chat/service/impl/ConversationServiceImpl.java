package vn.vinaacademy.chat.service.impl;

import com.vinaacademy.grpc.GetUserByIdsResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.vinaacademy.chat.domain.ConversationDomainService;
import vn.vinaacademy.chat.dto.ConversationDto;
import vn.vinaacademy.chat.entity.Conversation;
import vn.vinaacademy.chat.entity.Message;
import vn.vinaacademy.chat.mapper.ConversationMapper;
import vn.vinaacademy.chat.repository.ConversationMemberRepository;
import vn.vinaacademy.chat.repository.ConversationRepository;
import vn.vinaacademy.chat.repository.MessageRepository;
import vn.vinaacademy.chat.repository.projection.ConversationUnreadDto;
import vn.vinaacademy.chat.service.ConversationService;
import vn.vinaacademy.common.exception.BadRequestException;
import vn.vinaacademy.security.authentication.SecurityContextHolder;
import vn.vinaacademy.security.exception.AccessDeniedException;
import vn.vinaacademy.security.grpc.UserGrpcClient;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
  private final ConversationDomainService conversationDomainService;
  private final ConversationRepository conversationRepository;
  private final ConversationMemberRepository conversationMemberRepository;
  private final MessageRepository messageRepository;
  private final UserGrpcClient userGrpcClient;

  @Override
  @Transactional(readOnly = true)
  public ConversationDto getConversationById(UUID conversationId) {
    UUID currentUserId = UUID.fromString(SecurityContextHolder.getCurrentUserId());
    Conversation conversation = validateAndGetConversation(conversationId, currentUserId);
    return getConversationDto(conversation);
  }

  @Override
  @Transactional
  public ConversationDto getDirectConversation(UUID userId1, UUID userId2) {
    UUID currentUserId = UUID.fromString(SecurityContextHolder.getCurrentUserId());
    if (!currentUserId.equals(userId1) && !currentUserId.equals(userId2)) {
      throw new AccessDeniedException(
          "Access denied: User is not a participant in the conversation");
    }
    UUID peerId = currentUserId.equals(userId1) ? userId2 : userId1;
    Conversation conversation =
        conversationDomainService.getOrCreateDirectConversation(currentUserId, peerId);
    return getConversationDto(conversation);
  }

  private ConversationDto getConversationDto(Conversation conversation) {
    List<String> memberIds =
        conversation.getMembers().stream()
            .map(member -> member.getId().getUserId().toString())
            .toList();
    GetUserByIdsResponse usersResponse = userGrpcClient.getUserByIds(memberIds);
    if (!usersResponse.getSuccess()) {
      throw BadRequestException.message("Failed to fetch user details");
    }
    return ConversationMapper.INSTANCE.toDto(conversation, usersResponse.getUsersList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<ConversationDto> getAllConversationsByCurrentUser() {
    UUID currentUserId = UUID.fromString(SecurityContextHolder.getCurrentUserId());
    List<Conversation> conversations =
        conversationRepository.findConversationsByUserId(currentUserId);

    // Collect all unique member IDs from all conversations
    Set<String> allMemberIds =
        conversations.stream()
            .flatMap(conv -> conv.getMembers().stream())
            .map(member -> member.getId().getUserId().toString())
            .collect(Collectors.toSet());

    // Fetch user details in batch
    GetUserByIdsResponse usersResponse = userGrpcClient.getUserByIds(new ArrayList<>(allMemberIds));
    if (!usersResponse.getSuccess()) {
      throw BadRequestException.message("Failed to fetch user details");
    }

    // Fetch unread counts for conversations
    List<ConversationUnreadDto> unreadDtos =
        conversationRepository.findUnreadCountPerConversation(currentUserId);

    return ConversationMapper.INSTANCE.toDtoList(
        conversations, unreadDtos, usersResponse.getUsersList());
  }

  @Override
  @Transactional
  public ConversationDto createGroupConversation(String title, List<UUID> memberIds) {
    UUID currentUserId = UUID.fromString(SecurityContextHolder.getCurrentUserId());

    Conversation conversation =
        conversationDomainService.createGroupConversation(currentUserId, title, memberIds);

    // Fetch user details for response
    List<String> memberIdStrings = memberIds.stream().map(UUID::toString).toList();
    GetUserByIdsResponse usersResponse = userGrpcClient.getUserByIds(memberIdStrings);
    if (!usersResponse.getSuccess()) {
      throw BadRequestException.message("Failed to fetch user details");
    }

    return ConversationMapper.INSTANCE.toDto(conversation, usersResponse.getUsersList());
  }

  @Override
  @Transactional
  public void markConversationAsRead(UUID conversationId) {
    UUID currentUserId = UUID.fromString(SecurityContextHolder.getCurrentUserId());

    // Validate that the conversation exists and user is a participant
    Conversation conversation = validateAndGetConversation(conversationId, currentUserId);

    // Get the last message in the conversation
    Optional<Message> lastMessageToUse = Optional.ofNullable(conversation.getLastMessage());
    if (lastMessageToUse.isEmpty()) {
      lastMessageToUse =
          messageRepository
              .findLastMessageByConversationId(conversationId, PageRequest.of(0, 1))
              .stream()
              .findFirst();
    }

    // Update the user's last read message
    lastMessageToUse.ifPresent(
        message ->
            conversationMemberRepository.updateLastReadMessage(
                conversationId, currentUserId, message.getId(), Instant.now()));
  }

  private Conversation validateAndGetConversation(UUID conversationId, UUID currentUserId) {
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> BadRequestException.message("Conversation not found"));

    boolean isParticipant =
        conversationDomainService.isUserInConversation(currentUserId, conversation);
    if (!isParticipant) {
      throw new AccessDeniedException(
          "Access denied: User is not a participant in the conversation");
    }
    return conversation;
  }
}
