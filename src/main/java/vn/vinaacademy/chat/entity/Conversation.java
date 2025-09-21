package vn.vinaacademy.chat.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(
    name = "conversation",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_pair", columnNames = {"user_min_id", "user_max_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_a_id", nullable = false)
    private UUID userAId;

    @Column(name = "user_b_id", nullable = false)
    private UUID userBId;

    // user_min_id, user_max_id là generated column -> Hibernate chỉ đọc
    @Column(name = "user_min_id", insertable = false, updatable = false)
    private UUID userMinId;

    @Column(name = "user_max_id", insertable = false, updatable = false)
    private UUID userMaxId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "last_read_msg_id_a")
    private UUID lastReadMsgIdA;

    @Column(name = "last_read_at_a")
    private OffsetDateTime lastReadAtA;

    @Column(name = "last_read_msg_id_b")
    private UUID lastReadMsgIdB;

    @Column(name = "last_read_at_b")
    private OffsetDateTime lastReadAtB;

    @Column(name = "last_msg_id")
    private UUID lastMsgId;

    @Column(name = "last_msg_at")
    private OffsetDateTime lastMsgAt;
}
