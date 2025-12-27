# Скрипт для сброса brute force защиты и разблокировки пользователей

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Сброс Brute Force защиты" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Проверка подключения к БД
Write-Host "[1/3] Проверка подключения к базе данных..." -ForegroundColor Yellow
$dbCheck = docker exec parking_db pg_isready -U postgres 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ База данных доступна" -ForegroundColor Green
} else {
    Write-Host "✗ База данных недоступна" -ForegroundColor Red
    exit 1
}

# Показать текущее состояние пользователей
Write-Host "`n[2/3] Текущее состояние пользователей:" -ForegroundColor Yellow
docker exec parking_db psql -U postgres -d parking_db -c "
SELECT
    username,
    user_role,
    enabled,
    account_non_locked,
    failed_login_attempts,
    account_locked_until
FROM users
ORDER BY id;
"

# Сброс счетчиков и разблокировка
Write-Host "`n[3/3] Сброс счетчиков и разблокировка всех пользователей..." -ForegroundColor Yellow

$resetQuery = @"
UPDATE users
SET
    failed_login_attempts = 0,
    account_non_locked = true,
    account_locked_until = NULL
WHERE username IN ('admin', 'user', 'manager');

SELECT
    username,
    failed_login_attempts,
    account_non_locked,
    CASE
        WHEN account_locked_until IS NULL THEN 'Not locked'
        ELSE 'Locked until: ' || account_locked_until::text
    END as lock_status
FROM users
WHERE username IN ('admin', 'user', 'manager');
"@

docker exec parking_db psql -U postgres -d parking_db -c $resetQuery

Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "✓ Brute force защита сброшена" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Все пользователи разблокированы и готовы к использованию:" -ForegroundColor Cyan
Write-Host "  - admin / parking123" -ForegroundColor White
Write-Host "  - user / user123" -ForegroundColor White
Write-Host "  - manager / manager123" -ForegroundColor White
Write-Host ""
Write-Host "Теперь можно попробовать авторизоваться:" -ForegroundColor Yellow
Write-Host "  .\test-auth.ps1" -ForegroundColor Gray
Write-Host ""

