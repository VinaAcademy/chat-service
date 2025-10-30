package vn.vinaacademy.chat.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class WebsocketConstants {
  public static final String PRIVATE_MESSAGE_QUEUE = "/queue/pm";
  public static final String GROUP_MESSAGE_WEBSOCKET_TOPIC = "/topic/group";
  public static final String ONLINE_USERS_WEBSOCKET_TOPIC = "/topic/online-users";
}
