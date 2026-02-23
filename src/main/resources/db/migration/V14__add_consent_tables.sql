CREATE TABLE consent_definitions (
    id UUID NOT NULL,
    code VARCHAR(255) NOT NULL,
    version VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_consent_definitions PRIMARY KEY (id)
);

CREATE TABLE user_consents (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    consent_definition_id UUID NOT NULL,
    granted_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ip_address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_user_consents PRIMARY KEY (id),
    CONSTRAINT fk_user_consents_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_consents_definition FOREIGN KEY (consent_definition_id) REFERENCES consent_definitions (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_consents_user_id ON user_consents (user_id);
CREATE INDEX idx_user_consents_definition_id ON user_consents (consent_definition_id);