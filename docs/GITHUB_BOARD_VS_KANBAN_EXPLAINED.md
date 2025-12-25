# 📚 GITHUB PROJECTS: Board vs Kanban - Подробное объяснение

**Дата:** 2025-12-25  
**Вопрос:** Чем отличается Board от Kanban в GitHub Projects?

---

## 🎯 КРАТКИЙ ОТВЕТ

**Board и Kanban в GitHub Projects - это одно и то же!**

Когда вы создаете "Board" в GitHub Projects, вы создаете **Kanban-доску**.

---

## 📖 ПОДРОБНОЕ ОБЪЯСНЕНИЕ

### 1. Что такое Kanban?

**Kanban** - это методология управления проектами, разработанная в Toyota в 1940-х годах.

**Основные принципы Kanban:**
- 📋 Визуализация работы через колонки
- 🔄 Ограничение незавершенной работы (WIP Limit)
- 📊 Управление потоком задач
- 🎯 Непрерывное улучшение процесса

**Классическая Kanban-доска выглядит так:**

```
┌────────────┬───────────┬──────────────┬─────────┬────────┐
│  Backlog   │  To Do    │ In Progress  │ Review  │  Done  │
├────────────┼───────────┼──────────────┼─────────┼────────┤
│ Feature A  │ Task 3    │ Bug Fix 1    │ Task 5  │ Task 7 │
│ Feature B  │ Task 4    │ Feature C    │         │ Task 8 │
│ Bug 1      │           │              │         │ Task 9 │
└────────────┴───────────┴──────────────┴─────────┴────────┘
```

**Как работает:**
1. Задачи начинаются в Backlog
2. Перемещаются в To Do когда готовы к работе
3. Переходят в In Progress когда начинается работа
4. Идут на Review после завершения
5. Попадают в Done после проверки

---

### 2. Что такое Board в GitHub?

**GitHub Projects** (новая версия, также известная как Projects Beta или Projects v2) - это инструмент для управления проектами на GitHub.

**GitHub Projects предоставляет 3 вида отображения (views):**

#### 📋 Board View (Board)
Отображение в виде **колонок с карточками** - это и есть **Kanban-доска**!

```
GitHub Projects → Board View = Kanban доска
```

**Пример Board view:**
```
┌─────────────────────────────────────────────────┐
│  Parking System Development (Board view)        │
├──────────┬────────────┬──────────┬──────────────┤
│ To Do    │ In Progress│ Review   │ Done         │
├──────────┼────────────┼──────────┼──────────────┤
│ #23 Add  │ #45 Fix    │ #67 API  │ #12 Setup   │
│ Flyway   │ JWT bug    │ Gateway  │ Docker      │
│          │            │          │             │
│ #24 DB   │ #46 Client │          │ #13 Eureka  │
│ schema   │ Service    │          │ Server      │
└──────────┴────────────┴──────────┴──────────────┘
```

#### 📊 Table View
Табличное представление (как Excel/Google Sheets):
```
┌─────┬──────────────────┬─────────┬──────────────┬──────────┐
│ ID  │ Title            │ Status  │ Assignee     │ Priority │
├─────┼──────────────────┼─────────┼──────────────┼──────────┤
│ #23 │ Add Flyway       │ To Do   │ @developer1  │ High     │
│ #45 │ Fix JWT bug      │ Progress│ @developer2  │ Critical │
│ #67 │ API Gateway docs │ Review  │ @developer1  │ Medium   │
└─────┴──────────────────┴─────────┴──────────────┴──────────┘
```

#### 📅 Roadmap View
Временная шкала (Gantt chart):
```
Dec 2025                    Jan 2026
├─────────────────────────────────────────────┤
│ ████████ Setup Docker                        │
│         ████████████ API Gateway             │
│                  ████████ Client Service     │
│                        ████████ Database     │
└─────────────────────────────────────────────┘
```

---

### 3. Путаница в терминологии

**Почему возникает путаница:**

#### В старой версии GitHub Projects:
- Назывался **"Project boards"** (множественное число)
- Была только одна "доска" с колонками
- Явно позиционировалась как Kanban

#### В новой версии GitHub Projects (v2):
- Называется просто **"Projects"**
- Есть 3 вида: Board, Table, Roadmap
- **Board** - это новое название для Kanban-доски

**Что вызывает confusion:**

| Термин | Что это на самом деле |
|--------|----------------------|
| **Board** | Общее название для проекта в GitHub |
| **Board view** | Kanban-доска с колонками |
| **Kanban** | Методология (способ организации) |
| **Kanban board** | То же самое что Board view |

---

### 4. Как они используются вместе?

**Ответ: Они НЕ используются вместе, потому что это одно и то же!**

```
┌─────────────────────────────────────────────┐
│        GitHub Projects (Board)              │
│                   ↓                         │
│      реализует                              │
│                   ↓                         │
│        Kanban методологию                   │
│                   ↓                         │
│      через Board view                       │
└─────────────────────────────────────────────┘
```

**Правильное понимание:**
- **Board** (в GitHub) = **интерфейс** для управления проектом
- **Kanban** = **методология** организации работы
- **Board view** = **реализация** Kanban в GitHub

---

### 5. Практический пример

**Задача из плана:** "Создание канбан-доски (GitHub Projects)"

**Как это сделать в GitHub:**

```bash
Шаг 1: Перейти в репозиторий на GitHub
  └─→ Вкладка "Projects"

Шаг 2: Нажать "Link a project" → "New project"
  └─→ Появится выбор шаблона:
       📋 Board       ← ВЫБИРАЕМ ЭТОТ!
       📊 Table
       🗺️ Roadmap

Шаг 3: Выбрать "Board"
  └─→ Это создаст Kanban-доску с колонками!

Шаг 4: Настроить колонки (опционально)
  └─→ По умолчанию: Todo, In Progress, Done
  └─→ Можно добавить: Backlog, Review, Testing
```

**Результат:**
✅ У вас есть **GitHub Project** (Board)  
✅ С **Board view** (колонки)  
✅ Реализующий **Kanban методологию**  
✅ Это и есть **"канбан-доска"** из плана!

---

## 🎓 ДОПОЛНИТЕЛЬНЫЕ ДЕТАЛИ

### История GitHub Projects

#### Classic Projects (старая версия, до 2022)
```
GitHub → Settings → Projects (classic)
  └─→ Простая Kanban-доска
  └─→ Только колонки и карточки
  └─→ Ограниченные возможности
```

#### New Projects (v2, текущая версия, с 2022)
```
GitHub → Projects (beta/v2)
  └─→ Мощная система управления
  └─→ 3 вида отображения (Board, Table, Roadmap)
  └─→ Custom fields, filters, automation
  └─→ Board view = продвинутая Kanban-доска
```

---

### Преимущества GitHub Projects (Board view)

1. **Интеграция с Issues и PRs**
   - Автоматическое создание карточек из issues
   - Связь с pull requests
   - Обновление статусов

2. **Автоматизация**
   - Auto-add новых issues
   - Auto-move при изменении статуса
   - Workflows для перемещения карточек

3. **Кастомизация**
   - Произвольное количество колонок
   - Пользовательские поля
   - Фильтры и сортировка

4. **Множественные виды**
   - Board view (Kanban) для визуализации
   - Table view для детального анализа
   - Roadmap view для планирования

---

## 💡 ИТОГОВЫЕ ВЫВОДЫ

### ✅ Что нужно понимать:

1. **Board в GitHub = Kanban доска**
   - Это не два разных инструмента
   - Это один инструмент с разными названиями

2. **При создании выбирайте "Board"**
   - Это даст вам Kanban-доску с колонками
   - Это то, что нужно для управления проектом

3. **Kanban - это методология**
   - Board view реализует эту методологию
   - Вы получаете все преимущества Kanban

4. **Можно переключаться между видами**
   - Board view (колонки) - для Kanban
   - Table view (таблица) - для анализа
   - Roadmap view (шкала) - для планирования

---

## 🔗 КАК BOARD СВЯЗАН С ВАШИМ ПРОЕКТОМ?

Это ОЧЕНЬ важный вопрос! Давайте разберемся подробно.

### Способы связи GitHub Projects Board с репозиторием:

#### 1️⃣ **Автоматическая интеграция с Issues**

Когда вы создаете Issue в репозитории, вы можете добавить его в Project Board:

```
┌─────────────────────────────────────────────────┐
│  GitHub Repository                              │
│                                                 │
│  Issues:                                        │
│  #1 Add Flyway migration                        │
│  #2 Create parking_lots table                   │
│  #3 Implement User Service                      │
│                                                 │
│         │                                       │
│         ▼ (автоматически или вручную)          │
│                                                 │
│  GitHub Projects Board:                         │
│  ┌─────────┬──────────┬──────┬──────┐          │
│  │ To Do   │ Progress │Review│ Done │          │
│  ├─────────┼──────────┼──────┼──────┤          │
│  │ #1 ✓    │ #2 ✓     │      │ #3 ✓ │          │
│  └─────────┴──────────┴──────┴──────┘          │
└─────────────────────────────────────────────────┘
```

**Как это работает:**
- Создаете Issue в репозитории
- Справа в панели есть "Projects" секция
- Выбираете свой Board → Issue появляется в колонке

#### 2️⃣ **Автоматическая интеграция с Pull Requests**

Pull Request тоже можно добавить на доску:

```
Developer создает PR #45 "Fix JWT authentication"
           ↓
Автоматически добавляется в Board → "In Progress"
           ↓
PR проходит review
           ↓
Автоматически перемещается в "Review"
           ↓
PR merged
           ↓
Автоматически перемещается в "Done"
```

#### 3️⃣ **Двусторонняя синхронизация**

**С Board → В репозиторий:**
```
На Board создаете карточку "Add database migration"
           ↓
Можете конвертировать в Issue
           ↓
Issue #50 создается в репозитории
```

**Из репозитория → На Board:**
```
В Issues создаете "Fix bug in Client Service" #51
           ↓
Добавляете в Projects
           ↓
Появляется на Board в колонке "To Do"
```

#### 4️⃣ **Автоматизация через Workflows**

GitHub Projects поддерживает автоматические правила:

```yaml
Пример автоматизации:
┌─────────────────────────────────────────────┐
│ Правило 1: Auto-add issues                 │
│ • Все новые Issues автоматически            │
│   добавляются в колонку "To Do"             │
│                                             │
│ Правило 2: Auto-move on status change      │
│ • Issue с меткой "in-progress"              │
│   → перемещается в "In Progress"            │
│                                             │
│ Правило 3: Auto-close on PR merge          │
│ • Когда PR merged                           │
│   → связанный Issue перемещается в "Done"   │
└─────────────────────────────────────────────┘
```

### 📊 Практический пример связи:

**Сценарий: Разработка User Service**

```
День 1:
├─ Создаете Issue #25 "Implement User Service"
│  └─ Добавляете в Project → попадает в "To Do"
│
День 2:
├─ Назначаете Issue себе
│  └─ Меняете статус на "In Progress"
│  └─ На Board карточка автоматически переместилась!
│
День 3:
├─ Создаете PR #30 "feat: User Service implementation"
│  └─ Связываете с Issue #25 (в описании: "Closes #25")
│  └─ PR автоматически появляется на Board в "Review"
│
День 4:
├─ PR одобрен и merged
│  └─ Issue #25 автоматически закрывается
│  └─ На Board карточка перемещается в "Done"
```

### 🎯 Визуализация связи:

```
┌─────────────────────────────────────────────────────────────┐
│                   GitHub Repository                         │
│                                                             │
│  ┌─────────────┐     ┌──────────────┐    ┌──────────────┐ │
│  │   Issues    │     │  Pull        │    │   Commits    │ │
│  │             │     │  Requests    │    │              │ │
│  │  #1 #2 #3   │     │  #30 #31     │    │  sha123...   │ │
│  └──────┬──────┘     └──────┬───────┘    └──────┬───────┘ │
│         │                   │                   │         │
│         └───────────────────┼───────────────────┘         │
│                             │                             │
│                             ▼                             │
│         ┌───────────────────────────────────┐             │
│         │   GitHub Projects Board           │             │
│         │                                   │             │
│         │   ┌────────┬─────────┬──────┐    │             │
│         │   │ To Do  │Progress │ Done │    │             │
│         │   ├────────┼─────────┼──────┤    │             │
│         │   │ #1,#2  │ #3,#30  │  #31 │    │             │
│         │   └────────┴─────────┴──────┘    │             │
│         └───────────────────────────────────┘             │
└─────────────────────────────────────────────────────────────┘
```

### 🔧 Настройка связи - Пошаговая инструкция:

#### Способ 1: При создании Project (рекомендуется)

```bash
1. GitHub → Repository → Projects tab
2. Click "Link a project" → "New project"
3. Choose "Board"
4. Name: "Parking System Development"

✅ Project автоматически связан с репозиторием!

5. Settings → Enable workflows:
   ☑ Auto-add new issues
   ☑ Auto-move on status change
   ☑ Auto-close on PR merge
```

#### Способ 2: Связать существующий Project

```bash
1. Repository → Settings → Projects
2. Click "Link a project"
3. Select existing project
4. Configure automation rules
```

### 📝 Как добавлять Items на Board:

#### Метод 1: Из Issues
```
1. Open Issue #25
2. Right panel → "Projects"
3. Select "Parking System Development"
4. Issue появляется на Board
```

#### Метод 2: Прямо на Board
```
1. Open Board
2. Click "+" в нужной колонке
3. Можно:
   - Create draft (просто заметка)
   - Add item from repository (выбрать Issue)
   - Convert to issue (создать Issue из draft)
```

#### Метод 3: Автоматически
```
Настроить workflow:
"When issue is created → Add to Project → To Do column"
```

### 🎨 Дополнительные возможности связи:

#### 1. Метки (Labels)
```
Issue с меткой "bug" → автоматически красная карточка
Issue с меткой "feature" → автоматически зеленая карточка
```

#### 2. Assignees (Исполнители)
```
Назначаете Issue разработчику
↓
На Board видно аватар исполнителя
↓
Можно фильтровать: "Показать только мои задачи"
```

#### 3. Milestones (Вехи)
```
Issue #25 → Milestone "Phase 1"
↓
На Board можно группировать по Milestones
↓
Видно прогресс по каждой вехе
```

#### 4. Custom Fields
```
Можно добавить свои поля:
• Priority (High, Medium, Low)
• Estimate (Story points)
• Sprint (Sprint 1, Sprint 2)
• Team (Frontend, Backend)
```

### 💡 Реальный пример для вашего проекта:

```
Parking System Repository
├─ Issues:
│  ├─ #23 "Add Flyway migration" (label: database)
│  ├─ #24 "Create parking_lots table" (label: database)
│  ├─ #25 "Implement User Service" (label: backend)
│  └─ #26 "Fix JWT bug" (label: bug, priority: high)
│
└─ GitHub Projects Board: "Parking System Development"
   ├─ Backlog:
   │  └─ (пусто - задачи еще не запланированы)
   │
   ├─ To Do:
   │  ├─ #23 Add Flyway migration
   │  └─ #24 Create parking_lots table
   │
   ├─ In Progress:
   │  └─ #25 Implement User Service
   │     └─ Assignee: @developer1
   │     └─ PR #30 (draft)
   │
   ├─ Review:
   │  └─ #26 Fix JWT bug (priority: HIGH)
   │     └─ PR #31 (ready for review)
   │
   └─ Done:
      └─ (ранее завершенные задачи)
```

### 🚀 Автоматизация для продуктивности:

```yaml
# Рекомендуемые настройки automation:

1. Auto-add to project:
   When: Issue is created
   Then: Add to project → Column "To Do"

2. Auto-move to In Progress:
   When: Issue labeled "in-progress"
   Then: Move to column "In Progress"

3. Auto-move to Review:
   When: Pull request opened
   Then: Move to column "Review"

4. Auto-move to Done:
   When: Issue closed
   Then: Move to column "Done"
```

### ✅ Преимущества такой связи:

1. **Единый источник правды**
   - Issues и Board всегда синхронизированы
   - Не нужно обновлять в двух местах

2. **Визуализация прогресса**
   - Видно, что в работе, что завершено
   - Легко отслеживать bottlenecks

3. **Командная работа**
   - Все видят одну доску
   - Понятно, кто чем занимается

4. **Связь с кодом**
   - От карточки → к Issue → к PR → к коду
   - Полная история изменений

---

## 📋 ЧЕКЛИСТ ДЛЯ СОЗДАНИЯ

- [ ] Открыть GitHub репозиторий
- [ ] Перейти на вкладку "Projects"
- [ ] Нажать "New project"
- [ ] Выбрать **"Board"** (это Kanban!)
- [ ] Назвать: "Parking System Development"
- [ ] ✅ **Board автоматически связан с репозиторием!**
- [ ] Настроить automation workflows
- [ ] Настроить колонки:
  - [ ] Backlog
  - [ ] To Do
  - [ ] In Progress
  - [ ] Review
  - [ ] Done
- [ ] Добавить первые issues на доску
- [ ] Настроить custom fields (опционально)

---

## 🔗 Полезные ссылки

- [GitHub Projects Documentation](https://docs.github.com/en/issues/planning-and-tracking-with-projects)
- [About Board view](https://docs.github.com/en/issues/planning-and-tracking-with-projects/customizing-views-in-your-project/changing-the-layout-of-a-view#about-the-board-layout)
- [Kanban Methodology](https://www.atlassian.com/agile/kanban)

---

**Надеюсь, теперь всё понятно! 🎉**

**TL;DR:** Когда создаёте GitHub Project и выбираете "Board", вы создаёте Kanban-доску. Board = Kanban. Это одно и то же, просто разные названия.

