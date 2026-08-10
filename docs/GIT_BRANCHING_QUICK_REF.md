# 🚀 Git Branching - Quick Reference (Phase 4)

## 📋 Краткие ответы

### 1. Когда merge `develop` -> `main`?
**По завершении milestone/релизного среза Phase 4.**

Пример:
- Phase 4 increment A -> `v1.4.0`
- Phase 4 increment B -> `v1.4.1`
- Phase 5 release prep -> `v1.5.0`

### 2. Нужен ли отдельный Issue для merge?
Обычно **нет**. Используйте **Pull Request** и чеклист релиза в описании PR.

### 3. Какие branch names использовать сейчас?
- `feature/phase-4-<scope>`
- `bugfix/phase-4-<scope>`
- `release/v1.4.x`

---

## 🎯 Рекомендуемый поток (текущий проект)

1. Работайте от `develop`
2. Создавайте feature/bugfix ветки под задачи Phase 4
3. Merge веток в `develop` через PR
4. На релизном срезе создавайте `release/v1.4.x`
5. Merge `release/*` в `main` и ставьте tag (`v1.4.x`)

---

## 📝 Минимальные команды

### Feature branch
```bash
git checkout develop
git pull origin develop
git checkout -b feature/phase-4-auth-doc-consolidation
```

### Release branch
```bash
git checkout develop
git checkout -b release/v1.4.0
git push origin release/v1.4.0
```

### Tag after merge to main
```bash
git checkout main
git pull origin main
git tag -a v1.4.0 -m "Release v1.4.0"
git push origin v1.4.0
```

---

**TL;DR:** для текущего состояния проекта используйте naming и релизные примеры Phase 4 (`feature/phase-4-*`, `release/v1.4.x`, `v1.4.x`).

📖 **Подробно:** см. [GIT_BRANCHING_STRATEGY.md](./GIT_BRANCHING_STRATEGY.md)

