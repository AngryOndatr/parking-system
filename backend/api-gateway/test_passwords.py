import bcrypt

# Хэши из базы данных
admin_hash = b'$2a$10$N9qo8uLOickgx2ZMRZoMye1jDzl0fI7qgzQCqGVxEOTqHnKKWbfaG'
user_hash = b'$2a$10$WZBJOo6VkVqSN6S9JWtb6upEeF8VqAGsKuGPEv.9M8VpEhZFqiZFO'
manager_hash = b'$2a$10$dXJ3SW6G7P8o0dC/5iGz8OoKmqx5eoo0J1svT2Em.b83TK0Mq/D7K'

# Пароли для проверки (только >=8 символов из-за валидации)
passwords = ['parking123', 'user1234', 'manager123', 'admin123', 'password', 'password123', 'adminadmin', 'testtest']

print("Testing ADMIN hash:")
for pwd in passwords:
    if bcrypt.checkpw(pwd.encode(), admin_hash):
        print(f"  ✓ MATCH: {pwd}")

print("\nTesting USER hash:")
for pwd in passwords:
    if bcrypt.checkpw(pwd.encode(), user_hash):
        print(f"  ✓ MATCH: {pwd}")

print("\nTesting MANAGER hash:")
for pwd in passwords:
    if bcrypt.checkpw(pwd.encode(), manager_hash):
        print(f"  ✓ MATCH: {pwd}")

# Создаём новые правильные хэши
print("\n=== Creating NEW hashes (BCrypt rounds=10) ===")
print(f"parking123: {bcrypt.hashpw(b'parking123', bcrypt.gensalt(rounds=10)).decode()}")
print(f"user1234: {bcrypt.hashpw(b'user1234', bcrypt.gensalt(rounds=10)).decode()}")
print(f"manager123: {bcrypt.hashpw(b'manager123', bcrypt.gensalt(rounds=10)).decode()}")

