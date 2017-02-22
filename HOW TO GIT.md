# 1. Choose folder you want to use for Git

!! BE SURE NOT TO USE A FOLDER WITH " " SPACES IN THE LINK !!
!! ANDROID STUDIO CANNOT WORK WITH THIS !!

I use the following location:
"D:\Github\School\Fitrax"

# 2. Right click in folder and choose: 
"Git Bash Here"

# 3. To initialise (activate) git in this folder, type in:
git init

# 4. To connect the right repo to your folder, type in:
git remote add origin https://github.com/InternationalProject-AP-OAMK/international-project-repo-ip20172b.git

# 5. Check if the origin (fetch and push) are correct:
git remote -v

# 6. To sync all data from git with the folder on your PC, type in:
git pull origin master
(If this doesn't work):
git fetch origin master

Now all data is in sync.

# 7. If you have new data in your folder, which isn't on github yet, type in: 
git add .
git commit -m "<short description>"
git push origin master


# These are the commands you have to use every time:
# Every time you start with the project, be sure everything is in sync:
git pull origin master

# When done:
git add .
git commit -m "<short description>"
git push origin master

# Troubleshoot:
git push -f origin master    (to force)

git merge origin/master --allow-unrelated-histories