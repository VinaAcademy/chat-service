package vn.vinaacademy.chat.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.vinaacademy.chat.dto.MessageDto;
import vn.vinaacademy.chat.service.MessageService;
import vn.vinaacademy.common.response.ApiResponse;
import vn.vinaacademy.security.annotation.PreAuthorize;

@Slf4j
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

  private final MessageService messageService;

  @GetMapping("/recipient/{recipientId}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<List<MessageDto>> getMessageByRecipientId(
      @PathVariable("recipientId") UUID recipientId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    return ApiResponse.success(messageService.getMessagesByRecipientId(recipientId, page, size));
  }

  @GetMapping("/conversation/{conversationId}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<List<MessageDto>> getMessagesByConversationId(
      @PathVariable("conversationId") UUID conversationId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "50") int size) {
    return ApiResponse.success(
        messageService.getMessagesByConversationId(conversationId, page, size));
  }
}
