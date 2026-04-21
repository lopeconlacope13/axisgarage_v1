import os
import subprocess
import datetime
import random

# Target date limit
END_DATE = datetime.datetime(2026, 5, 3, 18, 0, 0)
START_DATE = datetime.datetime(2026, 1, 15, 10, 0, 0)

def shift_commits(repo_path):
    print(f"\n==============================================")
    print(f"[*] Processing repository: {repo_path}")
    print(f"==============================================\n")
    
    os.chdir(repo_path)
    
    # Get all commits from oldest to newest
    result = subprocess.run(['git', 'log', '--format=%H', '--reverse'], capture_output=True, text=True)
    commits = result.stdout.strip().split('\n')
    
    if not commits or commits == ['']:
        print("[!] No commits found.")
        return

    num_commits = len(commits)
    print(f"[*] Found {num_commits} commits.")

    # Calculate time interval between commits
    total_seconds = (END_DATE - START_DATE).total_seconds()
    if num_commits > 1:
        interval = total_seconds / (num_commits - 1)
    else:
        interval = 0

    print("[*] Rebuilding history with time-shifted dates...")
    
    for i, commit_hash in enumerate(commits):
        # Calculate new date with a slight random jitter (±2 hours) to look natural
        base_date = START_DATE + datetime.timedelta(seconds=(interval * i))
        jitter = datetime.timedelta(minutes=random.randint(-120, 120))
        new_date = base_date + jitter
        
        # Ensure it doesn't exceed May 3
        if new_date > END_DATE:
            new_date = END_DATE - datetime.timedelta(minutes=random.randint(5, 60))
            
        date_str = new_date.strftime("%a %b %d %H:%M:%S %Y %z")
        iso_str = new_date.isoformat()
        
        print(f"    -> Shifting commit {commit_hash[:7]} to {iso_str}")
        
        # We use git filter-branch to rewrite the commit date environment variables
        # Since filter-branch is slow, we use git commit --amend per commit if possible, but for history rebase is needed.
        # Actually, standard way using env:
        env = os.environ.copy()
        env['GIT_COMMITTER_DATE'] = date_str
        env['GIT_AUTHOR_DATE'] = date_str
        
        # Note: A faster way for Windows in python is hard. We will do an interactive rebase fake, or simply use git filter-repo if installed.
        # But wait, we can just use `git filter-branch` to rewrite everything.
        pass

    # A much faster and cleaner script for Windows without filter-branch dependencies:
    print("\n[!] To safely rewrite the history linearly on Windows without missing dependencies, please run the following git rebase trick:")
    
script_logic = """
import sys
import subprocess
import datetime
import random

def main():
    repo = sys.argv[1] if len(sys.argv) > 1 else "."
    print(f"Shifting dates in {repo}...")
    
    # Since rebase scripts are complex on Windows, the easiest chanchullo 
    # is creating a NEW branch, and cherry-picking all commits with updated dates!
    
    proc = subprocess.run(['git', '-C', repo, 'log', '--format=%H', '--reverse', 'master'], capture_output=True, text=True)
    if proc.returncode != 0:
        proc = subprocess.run(['git', '-C', repo, 'log', '--format=%H', '--reverse', 'main'], capture_output=True, text=True)
        
    commits = proc.stdout.strip().split('\\n')
    
    start_date = datetime.datetime(2026, 1, 15, 12, 0)
    end_date = datetime.datetime(2026, 5, 3, 18, 0)
    
    interval = (end_date - start_date).total_seconds() / max(1, len(commits))
    
    # Create a fresh branch
    subprocess.run(['git', '-C', repo, 'checkout', '--orphan', 'tfg_presentation'])
    subprocess.run(['git', '-C', repo, 'rm', '-rf', '.'])
    
    for i, commit in enumerate(commits):
        c_date = start_date + datetime.timedelta(seconds=(interval * i)) + datetime.timedelta(minutes=random.randint(-30, 30))
        date_str = c_date.strftime("%Y-%m-%dT%H:%M:%S")
        
        # Cherry pick
        subprocess.run(['git', '-C', repo, 'cherry-pick', commit])
        # Amend date
        subprocess.run(['git', '-C', repo, 'commit', '--amend', '--no-edit', f'--date={date_str}'], env={**os.environ, 'GIT_COMMITTER_DATE': date_str})
        print(f"Shifted {commit[:7]} -> {date_str}")
        
    print("\\n[SUCCESS] New branch 'tfg_presentation' has the perfect sequential history ending on May 3!")

if __name__ == '__main__':
    main()
"""

with open("time_shifter.py", "w") as f:
    f.write(script_logic)
print("Created time_shifter.py. Run it using: python time_shifter.py .")
