import sys, subprocess

N, port = list(map(int, input().split()))
for i in range(N-1):
    subprocess.run("python bridges/bridge.py 'python ai/iehn/jUdon1.py' jUdon1 {p} &>/dev/null &".format(p=port), shell=True, universal_newlines=True)

print(subprocess.run("python bridges/bridge.py 'python ai/iehn/t.py' jUdon1 {}".format(port), shell=True, universal_newlines=True))
