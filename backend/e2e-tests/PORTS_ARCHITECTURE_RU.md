# Архитектура портов E2E

Документ описывает порты из `backend/e2e-tests/docker-compose-e2e.yml`.

## Таблица портов (E2E стек)

| Сервис | Порт контейнера | Порт хоста |
|--------|-----------------|------------|
| postgres | 5432 | не публикуется (`expose`) |
| redis | 6379 | не публикуется (`expose`) |
| eureka-server | 8761 | не публикуется (`expose`) |
| api-gateway | 8080 | не публикуется (`expose`) |
| client-service | 8081 | не публикуется (`expose`) |
| gate-control-service | 8080 | не публикуется (`expose`) |
| billing-service | 8080 | не публикуется (`expose`) |
| reporting-service | 8080 | не публикуется (`expose`) |
| management-service | 8080 | не публикуется (`expose`) |

## Межсервисные URL в E2E

- `CLIENT_SERVICE_URL=http://client-service:8081`
- `BILLING_SERVICE_URL=http://billing-service:8080`
- `REPORTING_SERVICE_URL=http://reporting-service:8080` (где используется)

## Примечания

- В E2E контейнеры общаются только по внутренней Docker-сети.
- В тестах используйте имена сервисов/контейнеров, а не `localhost` порты хоста.
