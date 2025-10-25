package vn.vinaacademy.chat.mapper;

import com.vinaacademy.grpc.UserInfo;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import vn.vinaacademy.chat.dto.ConversationDto;
import vn.vinaacademy.chat.dto.MemberDto;
import vn.vinaacademy.chat.dto.MessageDto;
import vn.vinaacademy.chat.entity.Conversation;
import vn.vinaacademy.chat.entity.ConversationMember;
import vn.vinaacademy.chat.entity.Message;
import vn.vinaacademy.chat.repository.projection.ConversationUnreadDto;
import vn.vinaacademy.security.authentication.SecurityContextHolder;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

  ConversationMapper INSTANCE = Mappers.getMapper(ConversationMapper.class);

  // === Top-level Mapping ===

  default List<ConversationDto> toDtoList(
      List<Conversation> conversations, List<UserInfo> usersList) {
    return conversations.stream()
        .map(conversation -> toDto(conversation, usersList))
        .collect(Collectors.toList());
  }

  default List<ConversationDto> toDtoList(
      List<Conversation> conversations,
      List<ConversationUnreadDto> unreadDtos,
      List<UserInfo> usersList) {
    Map<UUID, Long> unreadMap =
        unreadDtos.stream()
            .collect(
                Collectors.toMap(
                    ConversationUnreadDto::conversationId, ConversationUnreadDto::unreadCount));
    return conversations.stream()
        .map(
            conversation ->
                toDto(
                    conversation,
                    unreadMap.getOrDefault(conversation.getId(), 0L).intValue(),
                    usersList))
        .collect(Collectors.toList());
  }

  default ConversationDto toDto(Conversation conversation, List<UserInfo> users) {
    // Tạo map userId → UserInfo để tra cứu nhanh
    Map<String, UserInfo> userMap =
        users.stream().collect(Collectors.toMap(UserInfo::getId, Function.identity()));

    // Lấy userId hiện tại một lần, tránh gọi lại SecurityContextHolder nhiều lần
    String currentUserId = SecurityContextHolder.getCurrentUserId();

    // Tạo ConversationDto
    ConversationDto conversationDto =
        ConversationDto.builder()
            .id(String.valueOf(conversation.getId()))
            .type(conversation.getType())
            .title(conversation.getTitle())
            .avatarUrl(
                Optional.ofNullable(conversation.getAvatarFileId())
                    .map(Object::toString)
                    .orElse(null))
            .createdAt(formatInstant(conversation.getCreatedAt()))
            .updatedAt(formatInstant(conversation.getLastMessageAt()))
            .lastMessageAt(conversation.getLastMessageAt())
            .lastMessage(toDto(conversation.getLastMessage()))
            .members(toMemberDtoList(conversation.getMembers(), userMap))
            .build();

    // Tìm thành viên hiện tại trong danh sách members
    conversationDto.setRead(isConversationRead(conversation, currentUserId));

    return conversationDto;
  }

  default ConversationDto toDto(Conversation conversation, int unreadCount, List<UserInfo> users) {
    ConversationDto dto = toDto(conversation, users);
    dto.setUnreadCount(unreadCount);
    return dto;
  }

  /** Kiểm tra xem cuộc hội thoại đã được đọc bởi user hiện tại hay chưa. */
  private boolean isConversationRead(Conversation conversation, String currentUserId) {
    return conversation.getMembers().stream()
        .filter(m -> m.getId().getUserId().toString().equals(currentUserId))
        .findFirst()
        .map(
            member -> {
              Instant lastReadAt = member.getLastReadAt();
              Instant lastMessageAt = conversation.getLastMessageAt();
              return lastReadAt != null
                  && (lastMessageAt == null || !lastReadAt.isBefore(lastMessageAt));
            })
        .orElse(false);
  }

  // === Mapping Message entity → MessageDto ===

  @Mapping(source = "conversation.id", target = "conversationId")
  @Mapping(target = "createdAt", expression = "java(formatInstant(message.getCreatedAt()))")
  @Mapping(target = "deletedAt", expression = "java(formatInstant(message.getDeletedAt()))")
  MessageDto toDto(Message message);

  // === Mapping ConversationMember entity → MemberDto ===

  default List<MemberDto> toMemberDtoList(
      List<ConversationMember> members, Map<String, UserInfo> userMap) {
    return members.stream()
        .map(member -> toMemberDto(member, userMap.get(member.getId().getUserId().toString())))
        .toList();
  }

  default MemberDto toMemberDto(ConversationMember member, UserInfo userInfo) {
    return MemberDto.builder()
        .memberId(member.getId().getUserId())
        .conversationId(member.getId().getConversationId())
        .role(member.getRole())
        .joinedAt(member.getJoinedAt())
        .leftAt(member.getLeftAt())
        .muteUntil(member.getMuteUntil())
        .lastReadAt(member.getLastReadAt())
        .lastReadMsgId(member.getLastReadMsgId())
        .username(userInfo != null ? userInfo.getUsername() : null)
        .fullName(userInfo != null ? userInfo.getFullName() : null)
        .avatarUrl(userInfo != null ? userInfo.getAvatarUrl() : null)
        .build();
  }

  // === Helper: format Instant to String ===
  default String formatInstant(Instant instant) {
    if (instant == null) return null;
    return DateTimeFormatter.ISO_INSTANT.format(instant);
  }
}
