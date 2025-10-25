package vn.vinaacademy.chat.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.vinaacademy.chat.dto.ConversationDto;
import vn.vinaacademy.chat.dto.request.CreateGroupRequest;
import vn.vinaacademy.chat.service.ConversationService;
import vn.vinaacademy.common.response.ApiResponse;
import vn.vinaacademy.security.annotation.PreAuthorize;
import vn.vinaacademy.security.authentication.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {
  private final ConversationService conversationService;

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<List<ConversationDto>> getAllConversations() {
    return ApiResponse.success(conversationService.getAllConversationsByCurrentUser());
  }

  @GetMapping("/{conversationId}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<ConversationDto> getConversationById(
      @PathVariable("conversationId") UUID conversationId) {
    return ApiResponse.success(conversationService.getConversationById(conversationId));
  }

  @GetMapping("/direct/{userId2}")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<ConversationDto> getDirectConversation(@PathVariable("userId2") UUID userId2) {
    UUID currentUserId = UUID.fromString(SecurityContextHolder.getCurrentUserId());
    return ApiResponse.success(conversationService.getDirectConversation(currentUserId, userId2));
  }

  @PostMapping("/groups")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<ConversationDto> createGroupConversation(
      @RequestBody CreateGroupRequest request) {
    return ApiResponse.success(
        conversationService.createGroupConversation(request.getTitle(), request.getMemberIds()));
  }

  @PutMapping("/{conversationId}/mark-read")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<?> markConversationAsRead(
      @PathVariable("conversationId") UUID conversationId) {
    conversationService.markConversationAsRead(conversationId);
    return ApiResponse.success("Cuộc trò chuyện đã được đánh dấu là đã đọc");
  }
}
