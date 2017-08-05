import sys, subprocess

N, port = list(map(int, input().split()))
for _ in range(N-1):
    subprocess.run("python bridges/bridge.py 'python ai/iehn/iUdon1.py' iUdon1 {} >/dev/null &".format(port), shell=True, universal_newlines=True)

print(subprocess.run("python bridges/bridge.py 'python ai/iehn/t.py' iUdon2 {}".format(port), shell=True, universal_newlines=True))
