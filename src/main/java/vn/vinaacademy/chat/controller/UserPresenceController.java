package vn.vinaacademy.chat.controller;

import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.vinaacademy.chat.service.UserPresenceService;
import vn.vinaacademy.common.response.ApiResponse;
import vn.vinaacademy.security.annotation.PreAuthorize;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class UserPresenceController {
  private final UserPresenceService userPresenceService;

  @GetMapping("/online-users")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<Set<UUID>> getOnlineUsers() {
    return ApiResponse.success(userPresenceService.getOnlineUsers());
  }
}
