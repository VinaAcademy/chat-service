package vn.vinaacademy.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateGroupRequest {
  @NotBlank
  @Size(max = 100)
  private String title;

  @NotEmpty
  @Size(min = 2, max = 100)
  private List<UUID> memberIds;
}
