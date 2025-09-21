package vn.vinaacademy.chat.interceptor;

import com.vinaacademy.grpc.ValidateTokenResponse;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import vn.vinaacademy.security.authentication.UserContext;
import vn.vinaacademy.security.grpc.JwtGrpcClient;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
  public static final String WS_AUTH_ATTR = "wsAuth";

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String AUTH_HEADER = "Authorization";
  private static final String TOKEN_PARAM = "token";
  private final JwtGrpcClient jwtGrpcClient;

  @Override
  public boolean beforeHandshake(
      @NonNull ServerHttpRequest req,
      @NonNull ServerHttpResponse res,
      @NonNull WebSocketHandler wsHandler,
      @NonNull Map<String, Object> attrs) {
    var servletReq = ((ServletServerHttpRequest) req).getServletRequest();
    String auth = servletReq.getHeader(AUTH_HEADER);
    if (auth == null || !auth.startsWith(BEARER_PREFIX)) {
      String tokenQuery = servletReq.getParameter(TOKEN_PARAM);
      if (tokenQuery == null || tokenQuery.isBlank()) return false;
      auth = BEARER_PREFIX + tokenQuery;
    }

    String token = auth.substring(BEARER_PREFIX.length());
    ValidateTokenResponse v = jwtGrpcClient.validateToken(token);
    if (!v.getIsValid()) return false;

    var user =
        new UsernamePasswordAuthenticationToken(
            v.getUserId(),
            null,
            UserContext.parseRoles(v.getRoles()).stream()
                .map(SimpleGrantedAuthority::new)
                .toList());
    attrs.put(WS_AUTH_ATTR, user);
    return true;
  }

  @Override
  public void afterHandshake(
      @NonNull ServerHttpRequest r,
      @NonNull ServerHttpResponse s,
      @NonNull WebSocketHandler w,
      Exception e) {}
}
