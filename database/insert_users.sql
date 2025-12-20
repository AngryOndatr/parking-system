-- Insert test users with BCrypt hashed passwords
-- admin password: parking123
-- user password: user123
-- manager password: manager123

INSERT INTO users (username, password_hash, user_role) 
VALUES 
    ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG', 'ADMIN'),
    ('user', '$2a$10$WZBJOo6VkVqSN6S9JWtb6upEeF8VqAGsKuGPEv.9M8VpEhZFqiZFO', 'USER'),
    ('manager', '$2a$10$dXJ3SW6G7P8o0dC/5iGz8OoKmqx5eoo0J1svT2Em.b83TK0Mq/D7K', 'ADMIN')
ON CONFLICT (username) DO UPDATE SET 
    password_hash = EXCLUDED.password_hash, 
    user_role = EXCLUDED.user_role;