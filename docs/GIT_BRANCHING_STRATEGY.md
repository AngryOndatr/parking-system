# 🌿 Git Branching Strategy - Руководство для вашего проекта

**Дата:** 2025-12-25  
**Контекст:** Работа с ветками `main` и `develop` в GitHub

---

## 🎯 ВАША ТЕКУЩАЯ СИТУАЦИЯ

```
main (пустая или старая)
  │
  └── develop (все ваши наработки здесь) ← ВЫ ЗДЕСЬ
```

**Вопросы:**
1. Когда синхронизировать `develop` → `main`?
2. Нужно ли создавать Issue для этого?
3. Каковы общепринятые практики?

---

## 📚 ОБЩЕПРИНЯТЫЕ ПРАКТИКИ

### 🌟 Git Flow (Самая популярная стратегия)

```
main (production-ready код)
  │
  ├── develop (основная ветка разработки)
  │     │
  │     ├── feature/add-flyway      ← Feature branches
  │     ├── feature/user-service
  │     └── feature/booking-system
  │
  ├── release/v1.0.0                ← Release branches
  │
  └── hotfix/critical-bug           ← Hotfix branches
```

### 🔄 Поток работы:

1. **feature/** → **develop** (постоянно)
2. **develop** → **release/** (когда готовы к релизу)
3. **release/** → **main** (после тестирования)
4. **main** → **hotfix/** → **main** + **develop** (критические баги)

---

## ✅ ОТВЕТЫ НА ВАШИ ВОПРОСЫ

### 1️⃣ Когда синхронизировать `develop` → `main`?

**Короткий ответ:** Когда у вас есть **стабильная, готовая к production версия**.

#### Варианты синхронизации:

**A. По завершении крупных milestone (рекомендую для вас):**
```
Phase 0 завершена → develop → main (v0.1.0)
Phase 1 завершена → develop → main (v0.2.0)
Phase 2 завершена → develop → main (v1.0.0)
```

**B. По расписанию (для больших команд):**
- Еженедельно (если команда большая)
- Каждые 2 недели (стандарт)
- Ежемесячно (крупные проекты)

**C. По готовности фич (для малых проектов/соло):**
- Завершили крупную фичу → протестировали → в `main`
- Например: "Database Migration готова и работает"

#### 🎯 Для вашего проекта (pet/учебный):

**Рекомендую:**
```
Сейчас (Phase 0):
1. Завершить все 8 Issues по Database Migration ✅
2. Протестировать на develop ✅
3. Создать Release v0.1.0 ✅
4. Merge develop → main через Pull Request ✅
5. Создать Git Tag v0.1.0 ✅

Далее:
- Phase 1 (микросервисы) → v0.2.0
- Phase 2 (frontend) → v1.0.0
```

---

### 2️⃣ Нужно ли создавать Issue для слияния веток?

**Короткий ответ:** НЕТ, обычно Issue НЕ создают. Вместо этого используют **Pull Request**.

#### Правильный workflow:

```
1. Работа в develop завершена
      ↓
2. Создать Pull Request: develop → main
   Title: "Release v0.1.0 - Database Migration"
      ↓
3. Review (самопроверка или команда)
      ↓
4. Merge Pull Request
      ↓
5. Создать Git Tag: v0.1.0
      ↓
6. GitHub Release (опционально)
```

#### Когда НУЖЕН Issue:

```
Issue: "Release v0.1.0 - Phase 0 Complete"
  ├─ Checklist:
  │   ├─ [x] All 8 database migration issues closed
  │   ├─ [x] Tests pass
  │   ├─ [x] Documentation updated
  │   ├─ [ ] Create PR: develop → main
  │   ├─ [ ] Merge PR
  │   └─ [ ] Create release tag
  └─ Закрывается ПОСЛЕ успешного релиза
```

**Это делают для:**
- Больших релизов (v1.0.0, v2.0.0)
- Для трекинга в Project Board
- Для истории решений

---

### 3️⃣ Общепринятые практики Git Flow

#### 📋 Git Flow Strategy (Vincent Driessen, 2010)

```
Постоянные ветки:
├── main (production)
└── develop (integration)

Временные ветки:
├── feature/* (новые фичи)
├── release/* (подготовка релиза)
├── hotfix/* (срочные баги в prod)
└── bugfix/* (обычные баги)
```

#### 🔄 Детальный workflow:

**1. Разработка новой фичи:**
```bash
# Создать ветку от develop
git checkout develop
git pull origin develop
git checkout -b feature/flyway-migration

# Работать, коммитить
git add .
git commit -m "feat: add Flyway configuration"

# Закончили - создать PR
git push origin feature/flyway-migration

# На GitHub: Create Pull Request
# feature/flyway-migration → develop
```

**2. Подготовка релиза:**
```bash
# Все фичи в develop готовы
# Создать release ветку
git checkout develop
git checkout -b release/v0.1.0

# Финальные правки (версии, changelog)
git commit -m "chore: prepare release v0.1.0"

# Создать PR: release/v0.1.0 → main
# После merge:
git tag -a v0.1.0 -m "Release v0.1.0"
git push origin v0.1.0

# Merge обратно в develop
git checkout develop
git merge release/v0.1.0
git push origin develop
```

**3. Hotfix (срочный баг в prod):**
```bash
# От main (!)
git checkout main
git checkout -b hotfix/critical-bug

# Исправить
git commit -m "fix: critical security issue"

# PR → main
# После merge → также в develop!
```

---

## 🎯 РЕКОМЕНДАЦИИ ДЛЯ ВАШЕГО ПРОЕКТА

### Стратегия для pet/solo проекта:

#### Упрощенный Git Flow:

```
main (stable, production-ready)
  │
  ├── develop (active development)
  │     │
  │     ├── feature/issue-1-flyway
  │     ├── feature/issue-2-v1-schema
  │     └── feature/issue-3-parking-lots
  │
  └── Merge develop → main когда:
        - Завершен Phase/Milestone
        - Все тесты проходят
        - Готово к демонстрации
```

#### 📅 План для вас (сейчас):

**Week 1 (сейчас):**
```
✅ Все в develop
✅ 8 Issues по Database Migration
✅ Тесты проходят
✅ Документация обновлена

→ Создать PR: develop → main
→ Title: "Release v0.1.0 - Phase 0 Complete"
→ Merge
→ Tag: v0.1.0
```

**Week 2+:**
```
develop:
  ├── Работа над Phase 1
  ├── Новые фичи
  └── Новые Issues

main:
  └── Стабильная v0.1.0 (пока не трогаем)

Когда Phase 1 готова:
  → Повторить процесс → v0.2.0
```

---

## 📝 ПОШАГОВАЯ ИНСТРУКЦИЯ ДЛЯ ВАС

### Вариант A: Простой (рекомендую сейчас)

**Когда:** После завершения Phase 0 / Database Migration

```bash
# 1. Убедиться, что все в develop закоммичено
git status
git add .
git commit -m "docs: complete Phase 0 documentation"

# 2. Push develop на GitHub
git push origin develop

# 3. На GitHub: Create Pull Request
#    Base: main
#    Compare: develop
#    Title: "Release v0.1.0 - Phase 0 Complete: Database Migration"
#    Description: (см. ниже)

# 4. Merge Pull Request на GitHub

# 5. Создать Release на GitHub
#    Tag: v0.1.0
#    Title: "v0.1.0 - Phase 0: Database Migration"
#    Description: Release notes

# 6. Локально обновить main
git checkout main
git pull origin main

# 7. Вернуться в develop для дальнейшей работы
git checkout develop
```

#### Pull Request Description (шаблон):

```markdown
## 🎯 Release v0.1.0 - Phase 0 Complete

### ✅ Completed
- [x] Database schema initialized (8 tables)
- [x] Flyway migration setup
- [x] 3 new tables added (parking_lots, parking_spaces, bookings)
- [x] All migrations tested
- [x] Documentation updated

### 📊 Statistics
- 8 Issues closed
- 11 tables in database
- 3.5 hours of work

### 🔗 Related Issues
Closes #1, #2, #3, #4, #5, #6, #7, #8

### 🧪 Testing
- [x] All migrations pass
- [x] Services start without errors
- [x] Database schema validated

### 📚 Documentation
- README.md updated
- Database migration guide added
- NEXT_STEPS updated
```

---

### Вариант B: С Release Branch (профессиональный)

**Когда:** Перед важным релизом (v1.0.0)

```bash
# 1. Создать release ветку
git checkout develop
git pull origin develop
git checkout -b release/v0.1.0

# 2. Финальные правки
# - Обновить version в pom.xml
# - Создать CHANGELOG.md
# - Финальные тесты

git commit -m "chore: prepare release v0.1.0"
git push origin release/v0.1.0

# 3. PR: release/v0.1.0 → main
# Merge

# 4. Создать tag
git checkout main
git pull origin main
git tag -a v0.1.0 -m "Release v0.1.0: Phase 0 Complete"
git push origin v0.1.0

# 5. Merge обратно в develop
git checkout develop
git merge release/v0.1.0
git push origin develop

# 6. Удалить release ветку
git branch -d release/v0.1.0
git push origin --delete release/v0.1.0
```

---

## 🏷️ Naming Conventions

### Ветки:

```
feature/issue-23-add-flyway      # Новая фича
feature/database-migration       # Крупная фича
bugfix/fix-jwt-authentication    # Исправление бага
hotfix/critical-security-patch   # Срочное исправление
release/v0.1.0                   # Подготовка релиза
docs/update-readme               # Документация
```

### Коммиты (Conventional Commits):

```
feat: add Flyway migration support
fix: resolve JWT token validation issue
docs: update database migration guide
chore: prepare release v0.1.0
test: add integration tests for migrations
refactor: improve error handling in auth service
style: format code according to style guide
perf: optimize database queries
```

### Теги (Git Tags):

```
v0.1.0          # Releas Phase 0
v0.2.0          # Release Phase 1
v1.0.0          # Major release
v1.0.1          # Patch
v1.1.0          # Minor update
```

---

## 📊 СРАВНЕНИЕ СТРАТЕГИЙ

### Для вашего проекта (pet/solo/учебный):

| Стратегия | Сложность | Подходит? | Когда использовать |
|-----------|-----------|-----------|-------------------|
| **Trunk-based** | ⭐ Простая | ✅ Да | Соло, быстрые итерации |
| **GitHub Flow** | ⭐⭐ Средняя | ✅ Да | Малые команды, частые деплои |
| **Git Flow** | ⭐⭐⭐ Сложная | ⚠️ Можно | Версионированные релизы |
| **GitLab Flow** | ⭐⭐ Средняя | ✅ Да | Окружения (dev/staging/prod) |

### Рекомендую для вас: **GitHub Flow (упрощенный)**

```
main (всегда готов к деплою)
  ↑
  │ Pull Request
  │
feature/issue-N (работа над задачей)
```

**Процесс:**
1. Создать feature ветку от `main`
2. Работать, коммитить
3. Создать PR → `main`
4. Review (самопроверка)
5. Merge
6. Delete feature branch

---

## ✅ ЧЕКЛИСТ: ЧТО ДЕЛАТЬ СЕЙЧАС

### Вариант 1: Быстрый (для продолжения работы)

- [ ] Закоммитить все изменения в `develop`
- [ ] Push `develop` на GitHub
- [ ] На GitHub: Settings → Branches → Set `develop` as default
- [ ] Продолжать работать в `develop`
- [ ] Merge в `main` после Phase 1

**Плюсы:** Быстро, просто
**Минусы:** `main` остается старой

---

### Вариант 2: Правильный (рекомендую)

- [ ] Завершить все текущие Issues в `develop`
- [ ] Протестировать все изменения
- [ ] Обновить документацию
- [ ] Создать Pull Request: `develop` → `main`
- [ ] Добавить описание релиза в PR
- [ ] Self-review PR
- [ ] Merge PR
- [ ] Создать Git Tag `v0.1.0`
- [ ] Создать GitHub Release
- [ ] Вернуться в `develop` для дальнейшей работы

**Плюсы:** Профессионально, правильная история
**Минусы:** Требует 30 минут

---

## 📚 ДОПОЛНИТЕЛЬНЫЕ МАТЕРИАЛЫ

### Git Flow Cheat Sheet:

```bash
# Начать новую фичу
git checkout develop
git checkout -b feature/my-feature

# Закончить фичу
git checkout develop
git merge feature/my-feature
git branch -d feature/my-feature

# Начать релиз
git checkout develop
git checkout -b release/v1.0.0

# Закончить релиз
git checkout main
git merge release/v1.0.0
git tag -a v1.0.0 -m "Release v1.0.0"
git checkout develop
git merge release/v1.0.0
git branch -d release/v1.0.0

# Hotfix
git checkout main
git checkout -b hotfix/critical-bug
# fix...
git checkout main
git merge hotfix/critical-bug
git tag -a v1.0.1 -m "Hotfix v1.0.1"
git checkout develop
git merge hotfix/critical-bug
git branch -d hotfix/critical-bug
```

---

## 🎯 ИТОГОВАЯ РЕКОМЕНДАЦИЯ ДЛЯ ВАС

### Сейчас (2025-12-25):

**Вы находитесь в Phase 0 (Database Migration setup)**

**Рекомендую:**

1. **Оставить как есть до завершения Phase 0**
   - Продолжать работу в `develop`
   - Завершить все 8 Issues по Database Migration
   - Протестировать

2. **После завершения Phase 0:**
   - Создать PR: `develop` → `main`
   - Title: "Release v0.1.0 - Phase 0: Database Migration"
   - Merge PR
   - Создать Tag `v0.1.0`
   - Создать GitHub Release

3. **Начать Phase 1:**
   - Продолжать работу в `develop`
   - Создавать feature branches для крупных задач
   - Merge в `main` после завершения Phase 1 → `v0.2.0`

### Периодичность merge `develop` → `main`:

```
✅ Рекомендую: По завершении Phase/Milestone
  Phase 0 → v0.1.0
  Phase 1 → v0.2.0
  Phase 2 → v1.0.0

❌ НЕ рекомендую: Каждый коммит (слишком часто)
❌ НЕ рекомендую: Раз в месяц (слишком редко для pet-проекта)
```

---

## 📖 ССЫЛКИ

- [Git Flow Original](https://nvie.com/posts/a-successful-git-branching-model/)
- [GitHub Flow](https://guides.github.com/introduction/flow/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)

---

**TL;DR:**

1. **Не создавайте Issue для merge веток** - используйте Pull Request
2. **Merge `develop` → `main` по завершении Phase/Milestone** (у вас - после Phase 0)
3. **Используйте упрощенный GitHub Flow** для pet-проекта
4. **Создавайте Git Tags** для релизов (v0.1.0, v0.2.0, v1.0.0)
5. **Сейчас продолжайте работать в `develop`**, merge после завершения Phase 0

🎯 **Готово! Теперь вы знаете, как правильно работать с ветками!**

