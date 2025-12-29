# 🚀 Phase 1 Issues — Quick Reference

> **5-Minute Guide to Creating Phase 1 Issues**

---

## 📖 Full Documentation
👉 **[PHASE_1_ISSUES.md](./PHASE_1_ISSUES.md)** — Complete issue templates with detailed descriptions

---

## ⚡ Quick Start (3 Options)

### Option 1: Manual Creation (15 minutes)
1. Open [PHASE_1_ISSUES.md](./PHASE_1_ISSUES.md)
2. For each issue, copy title + body
3. Go to GitHub → Issues → New issue
4. Paste content, add labels, create

### Option 2: GitHub CLI (5 minutes)
```bash
gh auth login
# Copy commands from PHASE_1_ISSUES.md (section: GitHub CLI Commands)
# Run each command to create issues
```

### Option 3: GitHub Copilot (2 minutes)
- Share PHASE_1_ISSUES.md with GitHub Copilot
- Say: "Create these GitHub issues for me"
- Review and confirm

---

## 📋 Issues Summary

| # | Title | Service | Estimate | Dependencies |
|---|-------|---------|----------|--------------|
| Epic | Phase 1 — Basic Backend | All | 3 weeks | - |
| #1 | CLIENT-SVC — CRUD for CLIENTS | client-service | 2-3 days | Epic |
| #2 | CLIENT-SVC — CRUD for VEHICLES | client-service | 2-3 days | #1 |
| #3 | CLIENT-SVC — GET /check | client-service | 1 day | #2 |
| #4 | MANAGEMENT-SVC — GET /available | management-service | 2 days | Epic |
| #5 | MANAGEMENT-SVC — POST /update | management-service | 1-2 days | #4 |
| #6 | REPORTING-SVC — POST /log | reporting-service | 1-2 days | Epic |
| #7 | DB — Flyway migrations | database | 1 day | Epic |
| #8 | Integration — API Gateway proxy | api-gateway | 1 day | #4,#5,#6 |
| #9 | Tests & Docs | all services | 1-2 days | #1-#8 |

---

## 🏷️ Required Labels

Create these labels before creating issues:

**Priority & Type:**
- `epic` `phase-1` `high-priority` `feature` `infra`

**Services:**
- `backend` `client-service` `management-service` `reporting-service` `api-gateway`

**Technology:**
- `postgres` `database` `flyway`

**Quality:**
- `needs-tests` `testing` `tests` `docs` `ci` `devops` `integration`

---

## 📅 Milestone
Create milestone: **"Phase 1 (3 weeks)"**

---

## 🔗 Implementation Order

### Week 1: Client Service
1. Issue #1 → #2 → #3 (sequential)

### Week 2: Management & Reporting
1. Issue #4 → #5 (sequential)
2. Issue #6 (parallel with #4-#5)
3. Issue #7 (parallel, verification)

### Week 3: Integration & Testing
1. Issue #8 (after #4, #5, #6)
2. Issue #9 (after all)

---

## ✅ Pre-Creation Checklist

- [ ] Milestone "Phase 1 (3 weeks)" created
- [ ] All labels created (see list above)
- [ ] GitHub Project Board ready (optional)
- [ ] [PHASE_1_ISSUES.md](./PHASE_1_ISSUES.md) reviewed
- [ ] Ready to create issues

---

## 🎯 After Creation

1. **Link Dependencies:** Add dependency links in issue descriptions
2. **Add to Board:** Move issues to appropriate columns
3. **Set Assignees:** Assign team members if known
4. **Verify Epic Links:** Ensure all child issues reference the Epic

---

## 📚 Related Documentation

- [PHASE_1_ISSUES.md](./PHASE_1_ISSUES.md) — Full issue templates
- [QUICK_GUIDE_ISSUES.md](./QUICK_GUIDE_ISSUES.md) — General issue creation guide
- [DATABASE_MIGRATION_TASKS_EN.md](./DATABASE_MIGRATION_TASKS_EN.md) — Database migration tasks
- [README.md](../README.md) — Project overview

---

**Status:** ✅ Ready to use
**Last Updated:** 2025-12-29
