package vn.vinaacademy.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import vn.vinaacademy.chat.interceptor.AuthChannelInterceptor;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  private final AuthChannelInterceptor authChannelInterceptor;

  @Value("${app.ws.allowed-origins:*}")
  private String[] allowedOrigins;

  @Value("${app.ws.heartbeat-interval-ms:60000}")
  private long heartbeatIntervalMs;

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    // Enable simple broker for local WebSocket communication
    // Kafka handles the message distribution between service instances
    registry
        .enableSimpleBroker("/topic", "/queue")
        .setHeartbeatValue(new long[] {heartbeatIntervalMs, heartbeatIntervalMs})
        .setTaskScheduler(heartBeatScheduler());
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws/chat").setAllowedOriginPatterns(allowedOrigins).withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    // Add interceptor to authenticate user from STOMP CONNECT frame
    registration.interceptors(authChannelInterceptor);
  }

  @Bean
  public ThreadPoolTaskScheduler heartBeatScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(1);
    scheduler.setThreadNamePrefix("ws-heartbeat-");
    scheduler.initialize();
    return scheduler;
  }
}
