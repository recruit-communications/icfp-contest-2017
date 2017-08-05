import sys, subprocess

N, port = list(map(int, input().split()))
for _ in range(N-1):
    subprocess.run("python bridges/online_bridge.py 'python iehn/iUdon1.py' {} >/dev/null &".format(port), shell=True, universal_newlines=True)

print(subprocess.run("python bridges/online_bridge.py 'python iehn/t.py' {}".format(port), shell=True, universal_newlines=True))
