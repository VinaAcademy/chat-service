package vn.vinaacademy.chat.service.impl;

import com.vinaacademy.grpc.GetUserByIdsResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import vn.vinaacademy.chat.domain.ConversationDomainService;
import vn.vinaacademy.chat.dto.ConversationDto;
import vn.vinaacademy.chat.entity.Conversation;
import vn.vinaacademy.chat.mapper.ConversationMapper;
import vn.vinaacademy.chat.repository.ConversationRepository;
import vn.vinaacademy.chat.service.ConversationService;
import vn.vinaacademy.security.authentication.SecurityContextHolder;
import vn.vinaacademy.security.grpc.UserGrpcClient;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
  private final ConversationDomainService conversationDomainService;
  private final ConversationRepository conversationRepository;
  private final UserGrpcClient userGrpcClient;

  @Override
  @Transactional(readOnly = true)
  public ConversationDto getConversationById(UUID conversationId) {
    UUID currentUserId = UUID.fromString(SecurityContextHolder.getCurrentUserId());
    Conversation conversation =
        conversationRepository
            .findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
    boolean isParticipant =
        conversationDomainService.isUserInConversation(currentUserId, conversation);
    if (!isParticipant) {
      throw new AccessDeniedException("Access denied: User is not a participant in the conversation");
    }
    List<String> memberIds =
        conversation.getMembers().stream()
            .map(member -> member.getId().getUserId().toString())
            .toList();
    GetUserByIdsResponse usersResponse = userGrpcClient.getUserByIds(memberIds);
    if (!usersResponse.getSuccess()) {
      throw new RuntimeException("Failed to fetch user details");
    }
    return ConversationMapper.INSTANCE.toDto(conversation, usersResponse.getUsersList());
  }

  @Override
  public ConversationDto getDirectConversation(UUID userId1, UUID userId2) {
    Conversation conversation = conversationDomainService.getOrCreateDirectConversation(userId1, userId2);
    
    List<String> memberIds = conversation.getMembers().stream()
        .map(member -> member.getId().getUserId().toString())
        .toList();
    GetUserByIdsResponse usersResponse = userGrpcClient.getUserByIds(memberIds);
    if (!usersResponse.getSuccess()) {
      throw new RuntimeException("Failed to fetch user details");
    }
    return ConversationMapper.INSTANCE.toDto(conversation, usersResponse.getUsersList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<ConversationDto> getAllConversationsByCurrentUser() {
    UUID currentUserId = UUID.fromString(SecurityContextHolder.getCurrentUserId());
    List<Conversation> conversations = conversationRepository.findConversationsByUserId(currentUserId);
    
    // Collect all unique member IDs from all conversations
    Set<String> allMemberIds = conversations.stream()
        .flatMap(conv -> conv.getMembers().stream())
        .map(member -> member.getId().getUserId().toString())
        .collect(Collectors.toSet());
    
    // Fetch user details in batch
    GetUserByIdsResponse usersResponse = userGrpcClient.getUserByIds(new ArrayList<>(allMemberIds));
    if (!usersResponse.getSuccess()) {
      throw new RuntimeException("Failed to fetch user details");
    }
    
    // Convert to DTOs
    return conversations.stream()
        .map(conv -> ConversationMapper.INSTANCE.toDto(conv, usersResponse.getUsersList()))
        .toList();
  }

  @Override
  @Transactional
  public ConversationDto createGroupConversation(String title, List<UUID> memberIds) {
    UUID currentUserId = UUID.fromString(SecurityContextHolder.getCurrentUserId());
    
    Conversation conversation = conversationDomainService.createGroupConversation(
        currentUserId, title, memberIds);
    
    // Fetch user details for response
    List<String> memberIdStrings = memberIds.stream().map(UUID::toString).toList();
    GetUserByIdsResponse usersResponse = userGrpcClient.getUserByIds(memberIdStrings);
    if (!usersResponse.getSuccess()) {
      throw new RuntimeException("Failed to fetch user details");
    }
    
    return ConversationMapper.INSTANCE.toDto(conversation, usersResponse.getUsersList());
  }
}
