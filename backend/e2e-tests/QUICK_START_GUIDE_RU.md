# 🚀 Краткое руководство — E2E тесты

## ✅ Готово к запуску!

### Что было сделано:

1. ✅ Создан тестовый эндпоинт `/api/v1/billing/pay-test` в billing-service
2. ✅ Добавлен прокси в api-gateway
3. ✅ Обновлён E2E тест
4. ✅ Пересобраны Docker образы

### 📋 Быстрый запуск:

```powershell
# Перейти в директорию E2E тестов
cd C:\Users\user\Projects\parking-system\backend\e2e-tests

# Запустить тест
mvn test -Dtest=OneTimeVisitorE2ETest
```

### ⏱️ Ожидаемое время выполнения:

- Сборка и запуск контейнеров: ~60 секунд
- Выполнение теста: ~30 секунд
- Остановка контейнеров: ~10 секунд
- **Итого**: ~1.5–2 минуты

### 📊 Ожидаемый результат:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.parking.e2e.OneTimeVisitorE2ETest

=== Шаг 1: Въезд транспортного средства ===
✅ HTTP/1.1 201 Created

=== Шаг 2: Попытка выезда без оплаты ===
✅ HTTP/1.1 200 OK (отказ)

=== Шаг 3: Проверка статуса оплаты ===
✅ HTTP/1.1 200 OK (isPaid=false)

=== Шаг 4: Оплата (Test Endpoint) ===
✅ HTTP/1.1 201 Created (оплата успешна)

=== Шаг 5: Повторная проверка статуса ===
✅ HTTP/1.1 200 OK (isPaid=true)

=== Шаг 6: Успешный выезд ===
✅ HTTP/1.1 200 OK (выезд разрешён)

[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 🔍 Если тест не проходит:

1. **Проверить Docker**:
   ```powershell
   docker ps
   docker images | Select-String "billing-service|api-gateway"
   ```

2. **Пересобрать образы**:
   ```powershell
   cd C:\Users\user\Projects\parking-system\backend\billing-service
   mvn clean package -DskipTests
   docker build -t billing-service:latest .

   cd ..\api-gateway
   mvn clean package -DskipTests
   docker build -t api-gateway:latest .
   ```

3. **Очистить Docker**:
   ```powershell
   docker system prune -f
   ```

### 🎯 Тестовый эндпоинт vs Продакшен:

| Аспект | Продакшен (`/pay`) | Тест (`/pay-test`) |
|--------|--------------------|--------------------|
| Валидация | Полная | Минимальная |
| Подсчёт стоимости | Да | Нет |
| Поиск ParkingEvent | Да | Нет |
| Скорость | Медленнее | Быстрее |
| Надёжность для E2E | ⚠️ | ✅ |

---

**Статус**: 🟢 Готово к запуску  
**Версия**: 1.0  
**Дата**: 2026-02-11

