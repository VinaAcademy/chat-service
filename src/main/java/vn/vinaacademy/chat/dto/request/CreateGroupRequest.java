package vn.vinaacademy.chat.dto.request;

import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateGroupRequest {
  private String title;
  private List<UUID> memberIds;
}
