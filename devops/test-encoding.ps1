# Тестовый скрипт для проверки кодировки
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " Тест исправленной кодировки" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Шаг 1: Проверка кириллицы..." -ForegroundColor Yellow
Write-Host "  ✓ Кириллица работает!" -ForegroundColor Green

Write-Host "`nШаг 2: Проверка цветного вывода..." -ForegroundColor Yellow
Write-Host "  ✓ Цвета работают!" -ForegroundColor Green

Write-Host "`nШаг 3: Проверка переменных..." -ForegroundColor Yellow
$testVar = "Тестовая переменная"
Write-Host "  ✓ Переменная: $testVar" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host " ✅ Все проверки пройдены успешно!" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Скрипт выполнен целиком, а не построчно!" -ForegroundColor Magenta

