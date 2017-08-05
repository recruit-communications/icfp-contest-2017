import sys, subprocess

N, port = list(map(int, input().split()))
for i in range(N-1):
    ii = (i+1) % 2 + 1
    subprocess.run("python bridges/bridge.py 'python ai/iehn/t.py' iUdon3 {p} &".format(p=port), shell=True, universal_newlines=True)

print(subprocess.run("python bridges/bridge.py 'python ai/iehn/t.py' iUdon3 {}".format(port), shell=True, universal_newlines=True))
