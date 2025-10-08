package vn.vinaacademy.chat.interceptor;

import com.vinaacademy.grpc.ValidateTokenResponse;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import vn.vinaacademy.security.authentication.UserContext;
import vn.vinaacademy.security.grpc.JwtGrpcClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String AUTH_HEADER = "Authorization";
  private static final String TOKEN_HEADER = "token";

  private final JwtGrpcClient jwtGrpcClient;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
      String token = extractToken(accessor);

      if (token != null) {
        try {
          ValidateTokenResponse validationResponse = jwtGrpcClient.validateToken(token);

          if (validationResponse.getIsValid()) {
            String userId = validationResponse.getUserId();
            var parsedRoles = UserContext.parseRoles(validationResponse.getRoles());
            var authorities = parsedRoles.stream().map(SimpleGrantedAuthority::new).toList();

            var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // Set user in STOMP session
            accessor.setUser(authentication);

            log.info(
                "WebSocket STOMP authentication successful for user: {} with roles: {}",
                userId,
                parsedRoles);
          } else {
            log.warn("Invalid JWT token in STOMP CONNECT frame");
            throw new IllegalArgumentException("Invalid authentication token");
          }
        } catch (Exception e) {
          log.error("Error validating JWT token in STOMP CONNECT", e);
          throw new IllegalArgumentException("Authentication failed: " + e.getMessage());
        }
      } else {
        log.warn("No authentication token found in STOMP CONNECT frame");
        throw new IllegalArgumentException("Missing authentication token");
      }
    }

    return message;
  }

  /**
   * Extract JWT token from STOMP headers. Supports multiple formats: 1. Authorization header with
   * "Bearer " prefix 2. Authorization header without prefix 3. Custom "token" header
   */
  private String extractToken(StompHeaderAccessor accessor) {
    // Try Authorization header first
    List<String> authHeaders = accessor.getNativeHeader(AUTH_HEADER);
    if (authHeaders != null && !authHeaders.isEmpty()) {
      String authHeader = authHeaders.get(0);
      if (authHeader.startsWith(BEARER_PREFIX)) {
        return authHeader.substring(BEARER_PREFIX.length());
      }
      // Also accept token without Bearer prefix
      return authHeader;
    }

    // Try custom token header
    List<String> tokenHeaders = accessor.getNativeHeader(TOKEN_HEADER);
    if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
      return tokenHeaders.get(0);
    }

    return null;
  }
}
