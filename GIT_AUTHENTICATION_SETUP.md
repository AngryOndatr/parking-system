# 🔐 Git Authentication Setup Guide

## Problem
GitHub no longer accepts password authentication for HTTPS operations.
Error: "Password authentication is not supported for Git operations"

---

## ✅ Solution: Use Personal Access Token (PAT)

### Step 1: Create GitHub Personal Access Token

1. **Go to GitHub Settings**:
   ```
   https://github.com/settings/tokens
   ```

2. **Click**: "Generate new token" → "Generate new token (classic)"

3. **Configure token**:
   - **Note**: `parking-system-development`
   - **Expiration**: 90 days (or custom)
   - **Select scopes**:
     - ✅ `repo` (Full control of private repositories)
     - ✅ `workflow` (Update GitHub Action workflows)
     - ✅ `write:packages` (if using GitHub Packages)

4. **Click**: "Generate token"

5. **IMPORTANT**: Copy the token immediately! 
   ```
   ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```
   ⚠️ You won't be able to see it again!

---

### Step 2: Update Windows Credential Manager

#### Option A: Automatic (Recommended)
Next time you push, Git will ask for credentials:
- **Username**: `AngryOndatr`
- **Password**: Paste your PAT token (not your GitHub password!)

Git will save it automatically in Windows Credential Manager.

#### Option B: Manual Update
1. Open **Windows Credential Manager**:
   - Press `Win + R`
   - Type: `control /name Microsoft.CredentialManager`
   - Or search "Credential Manager" in Start menu

2. Find **"git:https://github.com"** credential

3. Click **"Edit"** or **"Remove"** if exists

4. Next push will prompt for new credentials

---

### Step 3: Test Authentication

```bash
cd C:\Users\user\Projects\parking-system
git push origin develop
```

When prompted:
- **Username**: `AngryOndatr`
- **Password**: `ghp_YOUR_TOKEN_HERE` ← Paste your PAT!

---

## 📋 Current Git Configuration

```bash
# View current config
git config --global --list | Select-String "credential"

# Should show:
credential.helper=wincred
credential.https://github.com.usehttppath=false
```

---

## 🔐 Security Best Practices

### Token Security:
1. ✅ **Never share** your PAT
2. ✅ **Never commit** PAT to repository
3. ✅ Set **expiration date** (e.g., 90 days)
4. ✅ Use **minimal scopes** needed
5. ✅ **Regenerate** if compromised

### Token Management:
- Store PAT securely (e.g., password manager)
- Set calendar reminder before expiration
- Regenerate tokens periodically

---

## 🚀 Alternative: SSH Authentication (Advanced)

If you prefer SSH over HTTPS:

### Generate SSH Key:
```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
```

### Add to GitHub:
1. Copy public key: `~/.ssh/id_ed25519.pub`
2. Go to: https://github.com/settings/keys
3. Click "New SSH key"
4. Paste and save

### Update remote URL:
```bash
git remote set-url origin git@github.com:AngryOndatr/parking-system.git
```

---

## 📖 References

- [GitHub PAT Documentation](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)
- [Git Credential Storage](https://git-scm.com/book/en/v2/Git-Tools-Credential-Storage)

---

## ✅ Quick Checklist

- [ ] Create GitHub PAT at https://github.com/settings/tokens
- [ ] Copy token to safe place
- [ ] Try `git push origin develop`
- [ ] Enter username: `AngryOndatr`
- [ ] Paste PAT as password
- [ ] Verify credentials saved (no prompt on next push)
- [ ] Delete this guide or store PAT securely

---

**Next Steps**: Create your PAT and try pushing again!

