CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1) Conversation: chung cho DIRECT và GROUP
CREATE TABLE conversation
(
    id             UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    type           varchar(255) NOT NULL, -- DIRECT | GROUP
    title          TEXT,                  -- tên nhóm (null với DIRECT)
    avatar_file_id UUID,                  -- ảnh nhóm (tùy chọn)
    created_by     UUID         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    last_msg_id    UUID,
    last_msg_at    TIMESTAMPTZ
);

-- (Tùy chọn) unique cho DIRECT: cùng 2 user không được có 2 conv DIRECT
-- Ta enforce bằng conversation_member + constraint dưới:
-- đảm bảo DIRECT có đúng 2 thành viên duy nhất (ở mức app/service),
-- còn ràng buộc cứng có thể đặt bằng trigger nếu muốn.

-- 2) Thành viên conversation + marker đọc theo từng user
CREATE TABLE conversation_member
(
    conversation_id  UUID        NOT NULL REFERENCES conversation (id) ON DELETE CASCADE,
    user_id          UUID        NOT NULL,
    role             TEXT        NOT NULL DEFAULT 'MEMBER', -- OWNER/MOD/MEMBER (tùy bạn)
    joined_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    left_at          TIMESTAMPTZ,                           -- user rời nhóm (soft-leave)
    mute_until       TIMESTAMPTZ,                           -- tắt thông báo tạm thời
    last_read_msg_id UUID,
    last_read_at     TIMESTAMPTZ,
    PRIMARY KEY (conversation_id, user_id)
);

CREATE INDEX idx_member_user ON conversation_member (user_id, conversation_id);
CREATE INDEX idx_member_conv ON conversation_member (conversation_id);
CREATE INDEX idx_member_lastread ON conversation_member (conversation_id, user_id, last_read_at DESC);

-- 3) Message: giữ đơn giản, gộp file metadata như bạn
CREATE TABLE message
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    conversation_id UUID         NOT NULL REFERENCES conversation (id) ON DELETE CASCADE,
    sender_id       UUID         NOT NULL,
    type            varchar(255) NOT NULL, -- TEXT | IMAGE | FILE
    text_content    TEXT,

    -- metadata file (nếu IMAGE/FILE)
    file_id         UUID,
    file_name       TEXT,
    file_size       BIGINT,

    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,
    seq             BIGSERIAL,             -- cursor phân trang

    CONSTRAINT chk_text_has_content
        CHECK ((type <> 'TEXT') OR (text_content IS NOT NULL AND length(text_content) > 0)),
    CONSTRAINT chk_non_text_has_file_meta
        CHECK ((type = 'TEXT') OR (file_id IS NOT NULL AND file_name IS NOT NULL))
);

-- Index cho list & paging
CREATE INDEX idx_conv_last ON conversation (last_msg_at DESC, id DESC);
CREATE INDEX idx_msg_conv_paging ON message (conversation_id, created_at DESC, id DESC);
CREATE INDEX idx_msg_conv_seq ON message (conversation_id, seq DESC);
CREATE INDEX idx_msg_sender ON message (conversation_id, sender_id, created_at DESC);
