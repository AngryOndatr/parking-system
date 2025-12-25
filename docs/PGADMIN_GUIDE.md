# Как работать с pgAdmin 4 для Parking System

## 🚀 Доступ к pgAdmin

**URL:** http://localhost:5050

**Логин:** admin@parking.com  
**Пароль:** admin

## 📊 Подключение к базе данных

После входа в pgAdmin вы увидите уже настроенный сервер:

- **Сервер:** "Parking System DB (Docker)"
- **База данных:** parking_db
- **Пользователь:** postgres
- **Пароль:** postgres (при первом подключении система попросит ввести)

## 🔍 Как просмотреть таблицы

1. В левом меню разверните: **Servers → Parking System DB (Docker) → Databases → parking_db → Schemas → public → Tables**

2. Вы увидите все таблицы:
   - `users` - пользователи системы
   - `user_backup_codes` - коды восстановления для 2FA
   - `clients` - клиенты парковки
   - `vehicles` - автомобили
   - `subscriptions` - абонементы
   - `parking_events` - события парковки
   - `payments` - платежи
   - `logs` - логи системы

## 📝 Выполнение SQL запросов

Правой кнопкой на **parking_db → Query Tool** и можете выполнять любые SQL команды:

```sql
-- Посмотреть всех пользователей
SELECT id, username, email, user_role, enabled FROM users;

-- Посмотреть структуру таблицы users
\d users

-- Посмотреть всех клиентов
SELECT * FROM clients;
```

## 🔄 Команды для управления контейнером pgAdmin

```powershell
# Остановить pgAdmin
docker stop parking_pgadmin

# Запустить pgAdmin
docker start parking_pgadmin

# Перезапустить pgAdmin
docker restart parking_pgadmin

# Посмотреть логи pgAdmin
docker logs parking_pgadmin
```

## 💡 Полезные возможности

1. **View/Edit Data:** Правый клик на таблице → View/Edit Data → All Rows
2. **Backup:** Правый клик на БД → Backup
3. **Restore:** Правый клик на БД → Restore
4. **ER Diagram:** Правый клик на БД → ERD For Database

## 🛠️ Troubleshooting

Если pgAdmin не запустился:
```powershell
cd C:\Users\user\Projects\parking-system\devops
docker-compose -f docker-compose.infrastructure.yml logs pgadmin
```

Если забыли пароль базы данных, он указан в `docker-compose.infrastructure.yml`:
- Username: `postgres`
- Password: `postgres`

