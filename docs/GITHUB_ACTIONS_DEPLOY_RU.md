## GitHub Actions — Secrets и настройка деплоя

Этот документ содержит конкретные шаги и рекомендации по настройке секретов и автоматического деплоя через GitHub Actions для проекта `parking-system`.

Цель: безопасно подготовить репозиторий для автоматического пуша образов в GHCR и для выполнения SSH-деплоя на сервере при пуше в `main` или при создании релиз-тега.

1) Короткий чеклист (выполнить перед включением deploy job)
- [ ] Удалить приватные ключи и секреты из рабочей копии и из истории Git
- [ ] Добавить необходимые секреты в GitHub Repo → Settings → Secrets → Actions
- [ ] Проверить, что `GITHUB_TOKEN` имеет права на запись пакетов (GHCR) или создать персональный токен с scope `write:packages`/`delete:packages`
- [ ] Убедиться, что целевой сервер настроен: `docker`, `docker-compose` установлены; `DEPLOY_USER` имеет права на запуск docker-compose
- [ ] (Опционально) Создать GitHub Environment `production` и настроить required reviewers / protection

2) Какие секреты нужны и как их заполнить

- `DEPLOY_HOST` — IP или DNS имя сервера, куда будете деплоить (пример: `192.0.2.10` или `example.com`).
- `DEPLOY_USER` — SSH пользователь на целевом сервере (пример: `ubuntu` или `deploy`).
- `DEPLOY_SSH_KEY` — приватный SSH-ключ в открытом текстовом виде (начинается с `-----BEGIN OPENSSH PRIVATE KEY-----`).
  - Рекомендация: используйте deploy-ключ без passphrase или храните passphrase в отдельном секретe и используйте ssh-agent (сложнее).
  - При вставке в GitHub UI убедитесь, что все переносы строк сохранены. В `gh` CLI можно сохранить файл и выполнить `gh secret set DEPLOY_SSH_KEY --body "$(Get-Content -Raw path\to\id_rsa)"`.
- `DEPLOY_PATH` — путь на сервере, где находится `docker-compose.yml` и где будет выполняться pull/up (пример: `/home/deploy/parking-system`).

Дополнительные (опционально):
- `DEPLOY_SSH_KEY_PASSPHRASE` — если ключ защищён фразой, храните её отдельно и используйте в workflow для `ssh-add`.
- `REGISTRY_USERNAME`, `REGISTRY_TOKEN` — если вы используете приватный регистри, вместо `GITHUB_TOKEN`.

3) Пример: добавление секретов через GitHub UI

1. Откройте: `https://github.com/<owner>/<repo>/settings/secrets/actions`
2. Нажмите "New repository secret"
3. В поле Name введите `DEPLOY_SSH_KEY`, в поле Value вставьте содержимое приватного ключа (включая BEGIN/END строки). Сохраните.

Пример через `gh` (Windows PowerShell):
```powershell
# Установите GitHub CLI и авторизуйтесь: gh auth login
gh secret set DEPLOY_HOST --body "192.0.2.10"
gh secret set DEPLOY_USER --body "deploy"
gh secret set DEPLOY_PATH --body "/home/deploy/parking-system"
# DEPLOY_SSH_KEY: безопасно передать содержимое файла
$key = Get-Content -Raw -Path C:\path\to\id_rsa
gh secret set DEPLOY_SSH_KEY --body "$key"
```

4) Разрешения и использование `GITHUB_TOKEN` / GHCR

- В большинстве случаев `GITHUB_TOKEN` достаточно для входа в GHCR из workflow (`docker/login-action`). Но проверьте в `Settings → Actions → General → Workflow permissions`, что опция `Read and write permissions` для `GITHUB_TOKEN` включена.
- Если вы используете отдельный registry (Docker Hub, private registry), добавьте `REGISTRY_USERNAME` и `REGISTRY_PASSWORD` в Secrets и используйте `docker/login-action` с ними.

5) Рекомендации по безопасности

- Никогда не храните приватные ключи в репозитории. Если ключ был закоммичен, немедленно регенерируйте ключ на сервере и удалите старый из `authorized_keys`.
- Используйте GitHub Environments и required reviewers для production deploy (в workflow добавить `environment: production` и требовать approval).
- Не используйте долгоживущие секреты в явном виде — предпочтительнее deploy via SSH ключи + ограниченные права на сервере.
- Регулярно ротация ключей/токенов.

6) Как включить `deploy` job в `.github/workflows/cd.yml`

Откройте файл ` .github/workflows/cd.yml ` и найдите закомментированный блок `deploy:` (внизу файла). Раскомментируйте его и при необходимости адаптируйте скрипт deploy. Пример адаптированного шага:

```yaml
deploy:
  name: Deploy to Production
  needs: [ build-push-backend, build-push-frontend ]
  runs-on: ubuntu-latest
  environment: production
  if: github.ref == 'refs/heads/main'

  steps:
    - name: Install SSH client
      run: sudo apt-get update && sudo apt-get install -y openssh-client

    - name: Setup SSH key
      run: |
        mkdir -p ~/.ssh
        echo "${{ secrets.DEPLOY_SSH_KEY }}" > ~/.ssh/id_rsa
        chmod 600 ~/.ssh/id_rsa
        ssh-keyscan -H ${{ secrets.DEPLOY_HOST }} >> ~/.ssh/known_hosts

    - name: Deploy via SSH
      run: |
        ssh -o StrictHostKeyChecking=yes ${{ secrets.DEPLOY_USER }}@${{ secrets.DEPLOY_HOST }} \
          "cd ${{ secrets.DEPLOY_PATH }} && docker compose pull && docker compose up -d --remove-orphans"

```

7) Проверка и тестирование

- Перед включением deploy job протестируйте локально следующие шаги:
  - `mvn clean package -DskipTests`
  - Сборка docker-образов локально: `docker build -t parking-api-gateway:local backend/api-gateway`
  - Локальный запуск `docker compose` с тем же `docker-compose.yml`.
- В GitHub Actions сначала запустите `cd.yml` на ветке staging (или временно измените условие `if`) чтобы убедиться, что сборка и пуш работают корректно.

8) Что делать, если приватный ключ уже был закоммичен в историю

- Если ключ был закоммичен хотя бы в одном коммите, удаление файла и новый коммит не удалит его из истории. Процедура:
  1. Регенерируйте ключи на стороне сервера (и замените public key в `~/.ssh/authorized_keys`).
  2. Очистите историю репозитория локально (BFG или `git filter-repo`) и форс-пушьте в origin. Пример с BFG: `bfg --delete-files id_rsa` затем `git reflog expire --expire=now --all && git gc --prune=now --aggressive` и `git push --force`.
  3. Уведомьте команду о необходимости синхронизировать форс-пуш (если это общая репа).

9) Контроль и аудит

- Включите логирование действий deploy (stdout of SSH command), храните артефакты сборки в GH Actions artifacts для ретроспективы.
- Ограничьте доступ к секретам в GitHub: только админы/пользователи с нужными правами.

Если хотите, могу:
- автоматически внести пример `deploy` job в `cd.yml` (раскомментировать и адаптировать),
- или выполнить безопасное удаление `ssh key.txt` из рабочей директории и добавить запись в `.gitignore` и закоммитить изменения.

Файл создан: `docs/GITHUB_ACTIONS_DEPLOY_RU.md`.

