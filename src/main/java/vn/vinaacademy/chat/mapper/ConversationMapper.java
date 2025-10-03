package vn.vinaacademy.chat.mapper;

import com.vinaacademy.grpc.UserInfo;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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

@Mapper(componentModel = "spring")
public interface ConversationMapper {

  ConversationMapper INSTANCE = Mappers.getMapper(ConversationMapper.class);

  // === Top-level Mapping ===

  default ConversationDto toDto(Conversation conversation, List<UserInfo> users) {
    Map<String, UserInfo> userMap =
        users.stream().collect(Collectors.toMap(UserInfo::getId, Function.identity()));

    return ConversationDto.builder()
        .id(String.valueOf(conversation.getId()))
        .type(conversation.getType())
        .title(conversation.getTitle())
        .avatarUrl(
            conversation.getAvatarFileId() != null
                ? conversation.getAvatarFileId().toString()
                : null)
        .createdAt(formatInstant(conversation.getCreatedAt()))
        .updatedAt(formatInstant(conversation.getLastMessageAt()))
        .lastMessageAt(conversation.getLastMessageAt())
        .lastMessage(toDto(conversation.getLastMessage()))
        .members(toMemberDtoList(conversation.getMembers(), userMap))
        .build();
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
