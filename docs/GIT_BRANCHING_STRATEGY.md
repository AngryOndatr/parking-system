# 🌿 Git Branching Strategy (Phase 4)

**Дата обновления:** 2026-08-10  
**Контекст:** проект находится в **Phase 4**

---

## 1. Цель стратегии

Поддерживать предсказуемый поток изменений:
- `develop` — активная разработка;
- `main` — стабильные релизные срезы;
- feature/bugfix/release ветки — изолированные единицы работы.

---

## 2. Рекомендуемая схема веток

```
main (stable releases)
  │
  ├── develop (integration branch)
  │     ├── feature/phase-4-admin-ui
  │     ├── feature/phase-4-auth-hardening
  │     ├── bugfix/phase-4-gateway-proxy
  │     └── docs/phase-4-contract-alignment
  │
  ├── release/v1.4.0
  ├── release/v1.4.1
  └── hotfix/critical-prod-fix
```

---

## 3. Правила нейминга

- **feature:** `feature/phase-4-<scope>`
- **bugfix:** `bugfix/phase-4-<scope>`
- **docs:** `docs/phase-4-<scope>`
- **release:** `release/v1.4.x`
- **hotfix:** `hotfix/<scope>`

---

## 4. Рабочий процесс

1. Создать ветку от `develop`.
2. Вести работу в рамках одной задачи/темы.
3. Открыть PR в `develop` с ссылкой на issue.
4. После набора изменений для релизного среза — создать `release/v1.4.x`.
5. Merge `release/*` в `main`, поставить tag `v1.4.x`.
6. Синхронизировать `main` обратно в `develop`.

---

## 5. Примеры команд

### Feature
```bash
git checkout develop
git pull origin develop
git checkout -b feature/phase-4-db-schema-isolation
```

### Pull Request
```text
Base: develop
Compare: feature/phase-4-db-schema-isolation
Title: "Phase 4: DB schema isolation groundwork"
```

### Release
```bash
git checkout develop
git checkout -b release/v1.4.0
git push origin release/v1.4.0
```

### Tag
```bash
git checkout main
git pull origin main
git tag -a v1.4.0 -m "Release v1.4.0"
git push origin v1.4.0
```

---

## 6. Когда делать merge `develop` -> `main`

Не по каждому коммиту, а:
- по завершению релизного среза;
- после прохождения критических проверок;
- когда документация/конфигурация для релиза синхронизированы.

---

## 7. Release checklist (кратко)

- [ ] Все запланированные issues для среза закрыты
- [ ] Нет блокирующих багов
- [ ] Документация обновлена
- [ ] PR `release/v1.4.x -> main` подготовлен
- [ ] Tag `v1.4.x` создан

---

## 8. Что было устаревшим и заменено

Устаревшие раннефазные примеры заменены на актуальные для текущего цикла:
- `feature/phase-4-*`
- `release/v1.4.x`
- `v1.4.x`
