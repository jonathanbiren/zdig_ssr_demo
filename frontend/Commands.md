It is completely normal for your project to grow in the amount of commands it requires as time progresses. You can
however add simpler aliases for those commands inside the 'scripts' object of your 'package.json' file. But keeping them
inside of a file like this is also a valid solution of course. 

### Node.js Build Commands
- pnpm run build (builds the application into the 'build' directory)
- node build/index.js (runs the built version of the application locally)
- pnpm run preview (runs the built version using SvelteKit's preview server)
- pnpm run dev (starts the development server with hot reload)
- pnpm run check (runs TypeScript and Svelte checks)

### pnpm Commands

#### Package Installation
- pnpm install (installs all dependencies from package.json)
- pnpm add <package> (adds a package as a dependency)
- pnpm add -D <package> (adds a package as a dev dependency)
- pnpm add -g <package> (installs a package globally)
- pnpm install --frozen-lockfile (installs exact versions from pnpm-lock.yaml, like npm ci)

#### Package Updates & Information
- pnpm outdated (shows packages that have newer versions available)
- pnpm update (updates all packages to their latest compatible versions)
- pnpm update <package> (updates a specific package)
- pnpm list (shows installed packages in a tree format)
- pnpm list --depth=0 (shows only top-level packages)
- pnpm why <package> (shows why a package is installed and its dependency tree)
- pnpm update --interactive --latest (updates all packages to their latest compatible versions interactively)

#### Package Removal & Cleanup
- pnpm remove <package> (removes a package from dependencies)
- pnpm remove -D <package> (removes a dev dependency)
- pnpm prune (removes extraneous packages not in package.json)
- pnpm store prune (cleans up the global pnpm store)

#### Useful pnpm Features
- pnpm exec <command> (runs a command from node_modules/.bin)
- pnpm dlx <package> (downloads and runs a package without installing it, like npx)
- pnpm audit (checks for security vulnerabilities)
- pnpm audit --fix (automatically fixes security issues where possible)
- pnpm store path (shows where pnpm stores packages globally)
- pnpm store status (shows store statistics and disk usage)

### Git Commands
- git fetch (Get updates from remote without merging)
- git pull (Fetch and merge changes from remote)
- git push (Push changes to remote)
- git add . (Add all changes to staging area)
- git commit -m "message" (Commit changes with a message)
- git checkout <branch-name> (Switch to a different branch)
- git branch (List all branches)
- git branch <branch-name> (Create a new branch)

#### Daily Workflow Commands
- git status (shows current branch, staged/unstaged changes, and untracked files)
- git add . (stages all changes in current directory)
- git add <file> (stages specific file)
- git commit -m "message" (commits staged changes with a message)
- git push (pushes commits to remote repository)
- git pull (fetches and merges changes from remote repository)
- git diff (shows unstaged changes in detail)
- git diff --staged (shows staged changes that will be committed)


#### Branch Management
- git branch (lists all local branches, * shows current branch)
- git branch <branch-name> (creates a new branch)
- git checkout <branch-name> (switches to existing branch)
- git checkout -b <branch-name> (creates and switches to new branch in one command)
- git merge <branch-name> (merges specified branch into current branch)
- git branch -d <branch-name> (deletes a local branch that's been merged)
- git branch -D <branch-name> (force deletes a local branch, even if not merged)

#### History and Information
- git log --oneline --graph --all --decorate (visual tree of all commits across branches)
- git log --oneline (compact one-line view of commit history)
- git log -p <file> (shows commit history with changes for specific file)
- git show <commit-hash> (shows details of a specific commit)
- git blame <file> (shows who last modified each line of a file)

#### Undoing Changes
- git checkout -- <file> (discards unstaged changes in specific file)
- git reset HEAD <file> (unstages a staged file, keeps changes)
- git reset --soft HEAD~1 (undoes last commit, keeps changes staged)
- git reset --hard HEAD~1 (undoes last commit and discards all changes - DANGEROUS)
- git revert <commit-hash> (creates new commit that undoes a previous commit)

#### Stash Management (Temporary Storage)
- git stash (quickly saves current changes without committing)
- git stash save "descriptive message" (saves changes with a descriptive message)
- git stash list (shows all saved stashes)
- git stash apply (applies most recent stash, keeps it in stash list)
- git stash apply stash@{2} (applies specific stash by index)
- git stash pop (applies most recent stash and removes it from stash list)
- git stash drop stash@{1} (deletes specific stash without applying)
- git stash branch new-branch-name stash@{1} (creates branch from stash)

#### Remote Repository Management
- git remote -v (shows configured remote repositories)
- git fetch (downloads changes from remote without merging)
- git push origin <branch-name> (pushes specific branch to remote)
- git push --set-upstream origin <branch-name> (pushes new branch and sets up tracking)
- git clone <url> (downloads entire repository from remote)

#### Useful Shortcuts and Aliases
- git commit -am "message" (stages all modified files and commits in one command)
- git checkout - (switches to previously checked out branch)
- git log --follow <file> (tracks file history through renames)

### Docker commands
- docker build -t soldaten-wohnen:latest . (Builds the image of the application based on the Dockerfile)
- docker run -p 3000:3000 --name soldaten-wohnen-app soldaten-wohnen:latest (Builds the container based on the specified image and runs it on the specified port through port-forwarding)
- docker stop soldaten-wohnen-app (Stops the running container)
- docker rm soldaten-wohnen-app (Removes the stopped container)
- docker images (List all current images including their IDs)
- docker rmi <image-id> (Allows you to remove an image by procuring its ID) 
- docker ps (Shows running containers)
- docker ps -a (Shows all containers, including stopped ones)
- docker stop <container-name> (Stops a running container)
- docker rm <container-name> (Removes a stopped container) 


### dotenv CLI tool
The dotenv CLI tool allows you to run the locally built version of the Node.js application with environment variables
from your .env file. This is useful for testing the production build locally with your environment configuration.

- npm install -g dotenv-cli
- dotenv -- node build/index.js (run the built Node.js application with access to the environment variables)
- dotenv -- pnpm run preview (run the SvelteKit preview server with environment variables)

### Sitefetch
Sitefetch is a CLI tool which lets you scrape web pages, and generate .txt files from them, so that you may feed them to your LLM as context. This is especially useful for when the Cursor feature that attempts to do this does not work, and when used in combination with large Context LLMs like Google Gemini 2.5 Pro

https://github.com/egoist/sitefetch