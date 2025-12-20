-- ******************************************************
-- Production-Ready Users Table Structure
-- Расширение таблицы users для enterprise безопасности
-- ******************************************************

-- Добавляем недостающие поля безопасности
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS email VARCHAR(255) UNIQUE,
ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT true NOT NULL,
ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT false NOT NULL,
ADD COLUMN IF NOT EXISTS account_non_expired BOOLEAN DEFAULT true NOT NULL,
ADD COLUMN IF NOT EXISTS account_non_locked BOOLEAN DEFAULT true NOT NULL,
ADD COLUMN IF NOT EXISTS credentials_non_expired BOOLEAN DEFAULT true NOT NULL,

-- Password Security
ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(255),
ADD COLUMN IF NOT EXISTS password_reset_expires_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS force_password_change BOOLEAN DEFAULT false NOT NULL,

-- Login Security & Brute Force Protection  
ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER DEFAULT 0 NOT NULL,
ADD COLUMN IF NOT EXISTS account_locked_until TIMESTAMP,
ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS last_login_ip INET,
ADD COLUMN IF NOT EXISTS current_login_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS current_login_ip INET,
ADD COLUMN IF NOT EXISTS login_count INTEGER DEFAULT 0 NOT NULL,

-- Two-Factor Authentication
ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN DEFAULT false NOT NULL,
ADD COLUMN IF NOT EXISTS two_factor_secret VARCHAR(64),
ADD COLUMN IF NOT EXISTS backup_codes TEXT[], -- Array для backup кодов

-- Session Management
ADD COLUMN IF NOT EXISTS active_sessions_limit INTEGER DEFAULT 3 NOT NULL,
ADD COLUMN IF NOT EXISTS last_password_check_at TIMESTAMP,

-- Security Audit
ADD COLUMN IF NOT EXISTS security_question_hash VARCHAR(255),
ADD COLUMN IF NOT EXISTS security_answer_hash VARCHAR(255),
ADD COLUMN IF NOT EXISTS terms_accepted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS privacy_accepted_at TIMESTAMP,

-- Additional Security Metadata
ADD COLUMN IF NOT EXISTS user_agent_hash VARCHAR(64), -- Для device tracking
ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(10) DEFAULT 'en',
ADD COLUMN IF NOT EXISTS timezone VARCHAR(50) DEFAULT 'UTC',
ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500),

-- Soft Delete Support
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS deleted_by BIGINT REFERENCES users(id);

-- Создаем индексы для производительности
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_enabled ON users(enabled);
CREATE INDEX IF NOT EXISTS idx_users_account_locked ON users(account_locked_until) WHERE account_locked_until IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_last_login ON users(last_login_at);
CREATE INDEX IF NOT EXISTS idx_users_deleted ON users(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_users_password_reset ON users(password_reset_token) WHERE password_reset_token IS NOT NULL;

-- Создаем триггер для автоматического обновления updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- Добавляем constraints для безопасности
ALTER TABLE users 
ADD CONSTRAINT chk_failed_login_attempts CHECK (failed_login_attempts >= 0),
ADD CONSTRAINT chk_login_count CHECK (login_count >= 0),
ADD CONSTRAINT chk_active_sessions_limit CHECK (active_sessions_limit BETWEEN 1 AND 10),
ADD CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

-- Комментарии для документации
COMMENT ON COLUMN users.email IS 'User email address for password recovery and notifications';
COMMENT ON COLUMN users.enabled IS 'Account enabled status - false disables all access';
COMMENT ON COLUMN users.account_non_locked IS 'Account lock status - false when temporarily locked due to security';
COMMENT ON COLUMN users.failed_login_attempts IS 'Counter for failed login attempts for brute force protection';
COMMENT ON COLUMN users.two_factor_enabled IS 'Whether 2FA is enabled for this user';
COMMENT ON COLUMN users.backup_codes IS 'Array of one-time backup codes for 2FA recovery';