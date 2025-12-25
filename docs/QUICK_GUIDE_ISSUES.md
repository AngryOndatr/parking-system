# 📝 Quick Guide - Как создать Issues из декомпозиции

## 🎯 Быстрый старт (5 минут)

### Шаг 1: Создать Labels на GitHub

```
GitHub → Repository → Issues → Labels → New label

Создать:
1. database          (цвет: #0366d6)
2. migration         (цвет: #0366d6)
3. flyway            (цвет: #0366d6)
4. sql               (цвет: #0075ca)
5. feature           (цвет: #a2eeef)
6. testing           (цвет: #d4c5f9)
7. documentation     (цвет: #0075ca)
8. production        (цвет: #d73a4a)
9. priority-high     (цвет: #d73a4a)
10. priority-medium  (цвет: #fbca04)
```

### Шаг 2: Открыть DATABASE_MIGRATION_TASKS.md

Файл: `docs/DATABASE_MIGRATION_TASKS.md`

### Шаг 3: Создать Issue #1

```
GitHub → Issues → New issue

Title: [DB Migration] Setup Flyway dependency and configuration

Description: (скопировать из Issue #1 в файле)

Labels: database, migration, flyway, priority-high

Assignees: @yourself

Project: Parking System Development

Column: To Do
```

### Шаг 4: Повторить для Issues #2-#8

Для каждого Issue:
- Копировать Title
- Копировать Description
- Добавить Labels
- Добавить в Project
- Установить Dependencies (в описании)

---

## 🚀 Альтернатива - GitHub CLI (быстрее!)

### Установка GitHub CLI

```bash
# Windows (Winget)
winget install --id GitHub.cli

# После установки
gh auth login
```

### Создание Issues через CLI

```powershell
# Issue #1
gh issue create `
  --title "[DB Migration] Setup Flyway dependency and configuration" `
  --label "database,migration,flyway,priority-high" `
  --body "См. docs/DATABASE_MIGRATION_TASKS.md - Issue #1"

# Issue #2
gh issue create `
  --title "[DB Migration] Create V1__initial_schema.sql migration" `
  --label "database,migration,sql,priority-high" `
  --body "См. docs/DATABASE_MIGRATION_TASKS.md - Issue #2"

# ... и так далее для всех 8 issues
```

---

## 📋 Checklist

- [ ] Labels созданы на GitHub (10 labels)
- [ ] GitHub Project Board создан
- [ ] Issue #1 создан и добавлен в Board
- [ ] Issue #2 создан и добавлен в Board
- [ ] Issue #3 создан и добавлен в Board
- [ ] Issue #4 создан и добавлен в Board
- [ ] Issue #5 создан и добавлен в Board
- [ ] Issue #6 создан и добавлен в Board
- [ ] Issue #7 создан и добавлен в Board
- [ ] Issue #8 создан и добавлен в Board
- [ ] Все Issues в колонке "To Do" (кроме #7, #8 в "Backlog")
- [ ] Dependencies между Issues отмечены

---

## 🎯 После создания Issues

### Порядок работы:
1. ✅ Issue #1 → In Progress → Done
2. ✅ Issue #2 → In Progress → Done
3. ✅ Issue #3 → In Progress → Done
4. ✅ Issue #4 → In Progress → Done
5. ✅ Issue #5 → In Progress → Done
6. ✅ Issue #6 → In Progress → Done
7. ✅ Issue #7 → In Progress → Done (можно параллельно с #8)
8. ✅ Issue #8 → In Progress → Done (можно параллельно с #7)

**Общее время: ~3.5 часа**

---

**TL;DR:** Открыть DATABASE_MIGRATION_TASKS.md, скопировать каждый Issue на GitHub, добавить labels, начать с Issue #1.

