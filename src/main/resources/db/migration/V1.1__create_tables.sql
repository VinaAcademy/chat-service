CREATE
    EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1) Conversation: chỉ cặp user + marker đọc theo từng user
CREATE TABLE conversation
(
    id                 UUID PRIMARY KEY     DEFAULT uuid_generate_v4(),
    user_a_id          UUID        NOT NULL,
    user_b_id          UUID        NOT NULL,
    user_min_id        UUID GENERATED ALWAYS AS (LEAST(user_a_id, user_b_id)) STORED,
    user_max_id        UUID GENERATED ALWAYS AS (GREATEST(user_a_id, user_b_id)) STORED,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- marker đã đọc cho từng user
    last_read_msg_id_a UUID,
    last_read_at_a     TIMESTAMPTZ,
    last_read_msg_id_b UUID,
    last_read_at_b     TIMESTAMPTZ,
    -- tiện cho list hội thoại
    last_msg_id        UUID,
    last_msg_at        TIMESTAMPTZ,
    CONSTRAINT uq_pair UNIQUE (user_min_id, user_max_id)
);

-- 2) Message: gộp luôn metadata file/ảnh
DO
$$
    BEGIN
        IF
            NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'message_type') THEN
            CREATE TYPE message_type AS ENUM ('TEXT','IMAGE','FILE');
        END IF;
    END
$$;

CREATE TABLE message
(
    id              UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    conversation_id UUID         NOT NULL REFERENCES conversation (id) ON DELETE CASCADE,
    sender_id       UUID         NOT NULL,
    type            message_type NOT NULL,
    text_content    TEXT,      -- dùng cho TEXT
    -- gộp metadata upload (S3/MinIO)
    file_id         UUID,      -- tham chiếu đến file trong storage service
    file_name       TEXT,      -- tên file gốc
    file_size       BIGINT,    -- kích thước (bytes)

    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at      TIMESTAMPTZ,
    -- để phân trang ổn định
    seq             BIGSERIAL, -- tăng dần trong toàn bảng
    CONSTRAINT chk_text_has_content
        CHECK ((type <> 'TEXT') OR (text_content IS NOT NULL AND length(text_content) > 0))
);

-- Index gọn cho truy vấn
CREATE INDEX idx_conv_last ON conversation (last_msg_at DESC);
CREATE INDEX idx_msg_conv_paging ON message (conversation_id, created_at DESC, id DESC);
CREATE INDEX idx_msg_conv_seq ON message (conversation_id, seq DESC);
