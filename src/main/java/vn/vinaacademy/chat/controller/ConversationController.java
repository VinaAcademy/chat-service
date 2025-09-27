package vn.vinaacademy.chat.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.vinaacademy.chat.dto.ConversationDto;
import vn.vinaacademy.chat.dto.request.CreateGroupRequest;
import vn.vinaacademy.chat.service.ConversationService;
import vn.vinaacademy.security.annotation.PreAuthorize;
import vn.vinaacademy.security.authentication.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {
  private final ConversationService conversationService;

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public List<ConversationDto> getAllConversations() {
    return conversationService.getAllConversationsByCurrentUser();
  }

  @GetMapping("/{conversationId}")
  @PreAuthorize("isAuthenticated()")
  public ConversationDto getConversationById(
      @PathVariable("conversationId") String conversationId) {
    return conversationService.getConversationById(UUID.fromString(conversationId));
  }

  @GetMapping("/direct/{userId2}")
  @PreAuthorize("isAuthenticated()")
  public ConversationDto getDirectConversation(@PathVariable("userId2") String userId2) {
    UUID currentUserId = UUID.fromString(SecurityContextHolder.getCurrentUserId());
    return conversationService.getDirectConversation(currentUserId, UUID.fromString(userId2));
  }

  @PostMapping("/groups")
  @PreAuthorize("isAuthenticated()")
  public ConversationDto createGroupConversation(@RequestBody CreateGroupRequest request) {
    return conversationService.createGroupConversation(request.getTitle(), request.getMemberIds());
  }
}
