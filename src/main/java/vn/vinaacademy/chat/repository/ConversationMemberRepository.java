package vn.vinaacademy.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import vn.vinaacademy.chat.entity.Conversation;
import vn.vinaacademy.chat.entity.ConversationMember;
import vn.vinaacademy.chat.entity.id.ConversationMemberId;

public interface ConversationMemberRepository
    extends JpaRepository<ConversationMember, ConversationMemberId> {

  List<ConversationMember> findByIdUserId(UUID userId);

  List<ConversationMember> findByConversation(Conversation conversation);
}
