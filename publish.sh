#!/bin/bash
REPO_DIR="$(pwd)"
COMMIT_MSG="Automatic Maven publish $(date '+%Y-%m-%d %H:%M:%S')"
BRANCH="main"
IGNORE_FILE="$(basename "$0")"
TOKEN="YOUR_TOKEN"
USERNAME="leycm"
REPO="repository"

mvn clean install

cd "$REPO_DIR" || { echo "Repository not found!"; exit 1; }

git update-index --assume-unchanged "$IGNORE_FILE"

git add .

git commit -m "$COMMIT_MSG"

git push -f "https://${USERNAME}:${TOKEN}@github.com/${USERNAME}/${REPO}.git" "$BRANCH"

git update-index --no-assume-unchanged "$IGNORE_FILE"

echo "Maven build and GitHub Pages push completed via token!"
