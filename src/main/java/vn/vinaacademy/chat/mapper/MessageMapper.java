package vn.vinaacademy.chat.mapper;

import java.util.List;
import java.util.UUID;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import vn.vinaacademy.chat.dto.MessageDto;
import vn.vinaacademy.chat.dto.request.GroupMessage;
import vn.vinaacademy.chat.dto.request.PrivateMessage;
import vn.vinaacademy.chat.entity.Conversation;
import vn.vinaacademy.chat.entity.Message;

@Mapper(
    imports = {
      UUID.class,
      Conversation.class,
      java.time.format.DateTimeFormatter.class,
      java.time.ZoneOffset.class
    })
public interface MessageMapper {
  MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);

  // ===== Mapping Entity to DTO =====
  @Mapping(source = "conversation.id", target = "conversationId")
  @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  @Mapping(source = "deletedAt", target = "deletedAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  MessageDto toDto(Message message);

  List<MessageDto> toDtoList(List<Message> messages);

  // ===== Mapping from DTO (PrivateMessage) to Entity =====
  @Named("fromPrivateMessage")
  default Message fromPrivateMessage(
      PrivateMessage messageDto, Conversation conversation, UUID senderId) {
    return Message.builder()
        .conversation(conversation)
        .senderId(senderId)
        .type(messageDto.getType())
        .textContent(messageDto.getTextContent())
        .fileId(messageDto.getFileId())
        .fileName(messageDto.getFileName())
        .fileSize(messageDto.getFileSize())
        .build();
  }

  // ===== Mapping from DTO (GroupMessage) to Entity =====
  @Named("fromGroupMessage")
  default Message fromGroupMessage(
      GroupMessage messageDto, Conversation conversation, UUID senderId) {
    return Message.builder()
        .conversation(conversation)
        .senderId(senderId)
        .type(messageDto.getType())
        .textContent(messageDto.getTextContent())
        .fileId(messageDto.getFileId())
        .fileName(messageDto.getFileName())
        .fileSize(messageDto.getFileSize())
        .build();
  }
}
