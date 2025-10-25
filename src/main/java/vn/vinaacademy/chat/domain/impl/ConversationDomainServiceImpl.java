package vn.vinaacademy.chat.domain.impl;

import com.vinaacademy.grpc.GetUserByIdResponse;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.vinaacademy.chat.domain.ConversationDomainService;
import vn.vinaacademy.chat.entity.Conversation;
import vn.vinaacademy.chat.entity.ConversationMember;
import vn.vinaacademy.chat.entity.enums.ConversationType;
import vn.vinaacademy.chat.entity.enums.MemberRole;
import vn.vinaacademy.chat.entity.id.ConversationMemberId;
import vn.vinaacademy.chat.repository.ConversationMemberRepository;
import vn.vinaacademy.chat.repository.ConversationRepository;
import vn.vinaacademy.security.grpc.UserGrpcClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationDomainServiceImpl implements ConversationDomainService {
  private final ConversationRepository conversationRepository;
  private final ConversationMemberRepository conversationMemberRepository;
  private final UserGrpcClient userGrpcClient;

  @Override
  @Transactional
  public Conversation createDirectConversation(UUID currentUserId, UUID recipientId) {
    GetUserByIdResponse userInfo1 = userGrpcClient.getUserById(String.valueOf(currentUserId));
    GetUserByIdResponse userInfo2 = userGrpcClient.getUserById(String.valueOf(recipientId));
    if (!userInfo1.getSuccess() || !userInfo2.getSuccess()) {
      throw new IllegalArgumentException("One or both users do not exist.");
    }

    if (Objects.equals(currentUserId, recipientId)) {
      return createSelfConversation(currentUserId);
    }

    Conversation conversation =
        Conversation.builder().type(ConversationType.DIRECT).createdBy(currentUserId).build();
    conversation = conversationRepository.save(conversation);

    ConversationMember member1 =
        ConversationMember.builder()
            .id(new ConversationMemberId(conversation.getId(), currentUserId))
            .conversation(conversation)
            .role(MemberRole.MEMBER)
            .build();
    ConversationMember member2 =
        ConversationMember.builder()
            .id(new ConversationMemberId(conversation.getId(), recipientId))
            .conversation(conversation)
            .role(MemberRole.MEMBER)
            .build();

    conversation.setMembers(new ArrayList<>(List.of(member1, member2)));
    conversation = conversationRepository.save(conversation);

    return conversation;
  }

  private Conversation createSelfConversation(UUID userId) {
    Conversation conversation =
        Conversation.builder().type(ConversationType.DIRECT).createdBy(userId).build();
    conversation = conversationRepository.save(conversation);

    ConversationMember member =
        ConversationMember.builder()
            .id(new ConversationMemberId(conversation.getId(), userId))
            .conversation(conversation)
            .role(MemberRole.MEMBER)
            .build();

    conversation.setMembers(new ArrayList<>(List.of(member)));
    conversation = conversationRepository.save(conversation);

    return conversation;
  }

  @Override
  @Transactional
  public Conversation getOrCreateDirectConversation(UUID currentUserId, UUID recipientId) {
    List<Conversation> conversations =
        conversationRepository.findOneByUserIds(currentUserId, recipientId);

    if (Objects.equals(currentUserId, recipientId)) {
      conversations = conversations.stream().filter(c -> c.getMembers().size() == 1).toList();
    }

    if (conversations.isEmpty()) {
      return createDirectConversation(currentUserId, recipientId);
    }

    if (conversations.size() > 1) {
      log.warn(
          "Multiple direct conversations found between users {} and {}. Returning the first one.",
          currentUserId,
          recipientId);
    }

    return conversations.get(0);
  }

  @Override
  public boolean isUserInConversation(UUID currentUserId, Conversation conversation) {
    return conversationMemberRepository.existsById(
        new ConversationMemberId(conversation.getId(), currentUserId));
  }

  @Override
  @Transactional
  public Conversation createGroupConversation(UUID creatorId, String title, List<UUID> memberIds) {
    // Validate that creator is included in member list
    if (!memberIds.contains(creatorId)) {
      memberIds.add(creatorId);
    }

    // Create conversation
    Conversation conversation =
        Conversation.builder()
            .type(ConversationType.GROUP)
            .title(title)
            .createdBy(creatorId)
            .build();
    conversation = conversationRepository.save(conversation);

    // Create members
    Set<UUID> uniqueMemberIds = new LinkedHashSet<>(memberIds);
    List<ConversationMember> members = new ArrayList<>();
    for (UUID memberId : uniqueMemberIds) {
      MemberRole role = memberId.equals(creatorId) ? MemberRole.OWNER : MemberRole.MEMBER;
      ConversationMember member =
          ConversationMember.builder()
              .id(new ConversationMemberId(conversation.getId(), memberId))
              .conversation(conversation)
              .role(role)
              .build();
      members.add(member);
    }
    conversation.setMembers(members);
    conversation = conversationRepository.save(conversation);

    return conversation;
  }
}
