# Git Flow Guide for Class & Exam Management System

This document describes how to manage source control for CEMS using a lightweight GitFlow variant focused on release branches. Each feature iteration travels through the `release` branch before reaching `main`, ensuring structured reviews and stable snapshots.

## Branch Naming Convention
- `main`: always reflects the latest production-ready code.
- `release`: aggregation branch for current release work; only receives merges from `release-*` branches after testing.
- `release-initial`: first release branch created from `release` to bootstrap the workflow.
- `release-{feature}`: feature-specific release branches (e.g., `release-auth`, `release-exam-module`). Always prefix with `release-` to signal they feed the release stream.

## Workflow Steps
1. **Create the release base**
   ```powershell
   git checkout -b release origin/main
   git push -u origin release
   ```
   `release` now tracks the upcoming release cycle.

2. **Spin up a release feature branch**
   ```powershell
   git checkout release
   git pull
   git checkout -b release-initial
   git push -u origin release-initial
   ```
   - Use `release-initial` for the very first release cycle.
   - For additional work, create `release-{feature}` branches from `release` following the same commands with the new branch name.

3. **Develop & commit on the release feature branch**
   ```powershell
   # make changes
   git add .
   git commit -m "Add <feature>"
   git push
   ```
   Keep commits scoped to a single feature or fix. Include references to tasks or tickets when available.

4. **Merge `release-{feature}` back into `release`**
   ```powershell
   git checkout release
   git pull
   git merge --no-ff release-{feature}
   git push
   ```
   - Resolve conflicts locally before pushing.
   - After a successful merge, delete the feature branch if desired: `git branch -d release-{feature}` and `git push origin --delete release-{feature}`.

5. **Open a pull request from `release` to `main`**
   - Create a PR titled `Release <version>` or similar.
   - Include a summary of merged `release-*` branches and testing evidence.
   - Once approved, fast-forward or merge `release` into `main` through the PR.

6. **Tag the main branch after merge** (optional but recommended)
   ```powershell
   git checkout main
   git pull
   git tag -a vX.Y.Z -m "Release vX.Y.Z"
   git push origin vX.Y.Z
   ```
   - Replace `vX.Y.Z` with your semantic version (for example `v1.4.0`).
   - Use `git tag -d vX.Y.Z && git push origin :refs/tags/vX.Y.Z` if you ever need to rename a tag.

### Manage release versions on GitHub
- After pushing the tag, open the GitHub repository and go to `Releases`.
- Click `Draft a new release`, select the tag you just pushed (or create it from the UI), and give it a title/notes.
- Publishing the release keeps a history of tagged builds, lets teammates download source archives, and helps automated deployments pick the correct release artifact.
- Whenever you cut a new release, repeat the tag + release flow so GitHub always reflects the latest production build.

## Commit Message Format
Use short, descriptive commit messages:
```
Add user registration flow
Fix exam schedule timezone
Refactor dashboard context
```
Include issue IDs if using a tracker: `Add user registration flow (#42)`.

## Guidelines
- Keep `main` protected; forbid direct pushes.
- Enforce PR reviews for merges into `release` and `main`.
- Rebase `release-{feature}` branches regularly onto `release` to reduce conflicts.
- Run the full test suite before merging into `release` and again before requesting the `release -> main` PR merge.

Following this structure keeps the project release cadence predictable while still allowing parallel work on multiple features.

How to Open a Pull Request from Release to Main

### Prepare the Release Branch

First, ensure your release branch is ready:
```
bash
# Switch to release branch
git checkout release

# Ensure it's up to date
git pull origin release

# Verify all release-* feature branches are merged
git log --oneline --graph

```


Option 2: Pure Git Commands + Manual PR
If you don't have GitHub CLI installed, use pure git:

```
# 1. Ensure release branch is up to date
git checkout release
git pull origin release

# 2. Push release branch to remote
git push origin release

# 3. Open your browser and create PR manually
# The command below will give you the URL
echo "Create PR at: https://github.com/YOUR_USERNAME/YOUR_REPO/compare/main...release"
```

Then manually create the PR on GitHub's web interface.

### Option 3: Git Push with Auto PR Link
```
# Push and GitHub will give you a PR creation link
git checkout release
git push origin release

# Git will output a URL like:
# https://github.com/user/repo/pull/new/release
# Click that link to create the PR
```


To force ignore files from being pushed to Git, you need to use .gitignore and potentially remove them from Git's tracking. Here's how:

1. Add Files to .gitignore
Create or edit a .gitignore file in your repository root and add the files/patterns you want to ignore:

gitignore
```
# Ignore specific file
config/secrets.txt

# Ignore all files with certain extension
*.log
*.tmp

# Ignore entire directories
node_modules/
.env.local
```

2. If Files Are Already Tracked
If the files you want to ignore are already committed/tracked by Git, adding them to .gitignore alone won't work. You need to remove them from Git's index (but keep them locally):

```
# Remove a specific file from tracking
git rm --cached path/to/file.txt

# Remove a directory from tracking
git rm --cached -r path/to/directory/

# Remove all files matching a pattern
git rm --cached *.log
```
Then commit the changes:


git add .gitignore
git commit -m "Remove ignored files from tracking"
git push
3. Global Ignore (Optional)
For files you want to ignore across all your repositories:

```
git config --global core.excludesfile ~/.gitignore_global
```
Then add patterns to ~/.gitignore_global.

Important Notes:

--cached flag removes files from Git tracking but keeps them on your local filesystem
Without --cached, the files would be deleted from your filesystem too
After removing tracked files, they won't be included in future commits or pushes
Would you like me to help you set this up for your BS2020 project, or do you have specific files you want to ignore?

ignore a specific fiule



To ignore a specific file, follow these steps:

Step 1: Add the file to .gitignore
Open or create a .gitignore file in your repository root (c:\Users\pc\Documents\BS2020\.gitignore) and add the file path:

gitignore
```
# Example: ignore a specific file
path/to/your/file.txt

```
Step 2: If the file is already tracked by Git
If the file is already committed and tracked, you need to remove it from Git's index:
```

# Navigate to your repository
cd c:\Users\pc\Documents\BS2020

# Remove the file from Git tracking (keeps it locally)
git rm --cached path/to/your/file.txt

# Stage the .gitignore file
git add .gitignore

# Commit the changes
git commit -m "Stop tracking [filename]"

# Push to remote
git push
```