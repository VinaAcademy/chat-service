package vn.vinaacademy.chat.config;

import static vn.vinaacademy.chat.interceptor.JwtHandshakeInterceptor.WS_AUTH_ATTR;

import java.security.Principal;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import vn.vinaacademy.chat.interceptor.JwtHandshakeInterceptor;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

  @Value("${app.ws.allowed-origins:*}")
  private String[] allowedOrigins;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    // Enable simple broker for local WebSocket communication
    // Kafka handles the message distribution between service instances
    registry.enableSimpleBroker("/topic", "/queue");
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .addInterceptors(jwtHandshakeInterceptor)
        .setHandshakeHandler(jwtPrincipalHandshakeHandler())
        .setAllowedOriginPatterns(allowedOrigins)
        .withSockJS();
  }

  @Bean
  DefaultHandshakeHandler jwtPrincipalHandshakeHandler() {
    return new DefaultHandshakeHandler() {
      @Override
      protected Principal determineUser(
          @NonNull ServerHttpRequest req,
          @NonNull WebSocketHandler wsHandler,
          @NonNull Map<String, Object> attrs) {
        return (Principal) attrs.get(WS_AUTH_ATTR);
      }
    };
  }
}
