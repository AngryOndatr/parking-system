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
## GitHub Actions — Secrets and Deploy Setup

This document contains concrete steps and recommendations for configuring repository secrets and enabling automated deploys via GitHub Actions for the `parking-system` project.

Purpose: prepare the repository securely for pushing Docker images to GHCR and performing SSH-based deploys on a target server when pushing to `main` or creating a release tag.

1) Short checklist (run before enabling the deploy job)
- [ ] Remove private keys and other secrets from the working copy and Git history
- [ ] Add required secrets to the GitHub repository: Settings → Secrets → Actions
- [ ] Verify that `GITHUB_TOKEN` has package write permissions for GHCR or create a personal access token with `write:packages`/`delete:packages` scope
- [ ] Ensure the target server is prepared: `docker` and `docker-compose` installed; `DEPLOY_USER` has rights to run docker-compose
- [ ] (Optional) Create a GitHub Environment named `production` and configure required reviewers / protection

2) Which secrets are required and how to set them

- `DEPLOY_HOST` — IPv4/hostname of the target server (example: `192.0.2.10` or `example.com`).
- `DEPLOY_USER` — SSH user on the server (example: `ubuntu` or `deploy`).
- `DEPLOY_SSH_KEY` — private SSH key text (starts with `-----BEGIN OPENSSH PRIVATE KEY-----`).
  - Recommendation: use a deployment key without a passphrase or store the passphrase in a separate secret and use ssh-agent within the workflow.
  - When pasting into the GitHub UI make sure line breaks are preserved. With `gh` CLI you can load a file and run `gh secret set DEPLOY_SSH_KEY --body "$(Get-Content -Raw path\to\id_rsa)"`.
- `DEPLOY_PATH` — path on the server where `docker-compose.yml` lives and where `docker compose pull && docker compose up -d` will run (example: `/home/deploy/parking-system`).

Optional additional secrets:
- `DEPLOY_SSH_KEY_PASSPHRASE` — if the private key is passphrase-protected, store the passphrase separately and use it to unlock the key in the workflow.
- `REGISTRY_USERNAME`, `REGISTRY_TOKEN` — if you use a private registry instead of GHCR.

3) Example: adding secrets via the GitHub UI

1. Open: `https://github.com/<owner>/<repo>/settings/secrets/actions`
2. Click "New repository secret"
3. Enter `DEPLOY_SSH_KEY` as Name and paste the private key (including BEGIN/END lines) into Value and save.

Example using `gh` (Windows PowerShell):
```powershell
# Install and authenticate GitHub CLI first: gh auth login
gh secret set DEPLOY_HOST --body "192.0.2.10"
gh secret set DEPLOY_USER --body "deploy"
gh secret set DEPLOY_PATH --body "/home/deploy/parking-system"
# Set DEPLOY_SSH_KEY from a local file
$key = Get-Content -Raw -Path C:\path\to\id_rsa
gh secret set DEPLOY_SSH_KEY --body "$key"
```

4) Permissions and using `GITHUB_TOKEN` / GHCR

- In most cases `GITHUB_TOKEN` is sufficient for GHCR login from a workflow (`docker/login-action`). Check `Settings → Actions → General → Workflow permissions` and ensure `Read and write permissions` is enabled for the `GITHUB_TOKEN`.
- If you use Docker Hub or another private registry, add `REGISTRY_USERNAME` and `REGISTRY_PASSWORD` secrets and authenticate using `docker/login-action` with them.

5) Security recommendations

- Never commit private keys or secrets to the repository. If a key was committed, immediately regenerate keys on the server and remove the old public key from `~/.ssh/authorized_keys`.
- Use GitHub Environments and required reviewers for production deploys (add `environment: production` in the workflow to require approval).
- Prefer short-lived or limited-scope secrets. SSH deploy keys with constrained access are better than long-lived tokens.
- Rotate keys and tokens regularly.

6) How to enable the `deploy` job in `.github/workflows/cd.yml`

Open `.github/workflows/cd.yml` and find the commented `deploy:` job near the bottom of the file. Uncomment it and adapt the deploy script if needed. Example adapted job:

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

7) Verification and testing

- Before enabling deploy, test the following locally:
  - `mvn clean package -DskipTests`
  - Build Docker images locally: `docker build -t parking-api-gateway:local backend/api-gateway`
  - Run `docker compose` locally with the same `docker-compose.yml`.
- In GitHub Actions, first run `cd.yml` on a staging branch (or temporarily change the `if` condition) to validate build and push steps.

8) If a private key was committed to the Git history

- If a private key exists in any commit, deleting the file and committing will not remove it from the history. Procedure:
  1. Regenerate keys on the server and replace the old public key in `~/.ssh/authorized_keys`.
  2. Purge the key file from the Git history locally (BFG or `git filter-repo`) and force-push to origin. Example with BFG: `bfg --delete-files id_rsa` then `git reflog expire --expire=now --all && git gc --prune=now --aggressive` and `git push --force`.
  3. Notify the team to re-clone or rebase after the force-push.

9) Audit and control

- Enable logging of deployment steps (capture SSH output), and keep build artifacts in GitHub Actions artifacts for traceability.
- Restrict access to repository secrets to administrators or a small set of trusted users.

If you want, I can:
- automatically add the example `deploy` job into `cd.yml` (uncomment and adapt),
- or perform secure removal of the local `ssh key.txt`, add `.gitignore` entry and commit the changes.

File created: `docs/GITHUB_ACTIONS_DEPLOY.md`.
