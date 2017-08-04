import sys
import json
import subprocess
import shlex


class Bridge:
    def __init__(self, host=None, port=0):
        self.buffer = b''

    def send_json(self, obj):
        d = json.dumps(obj)
        print('SEND', d)
        bytes = d.encode()
        n = len(bytes)
        sys.stdout.write(str(n).encode())
        sys.stdout.write(':'.encode())
        sys.stdout.write(bytes)

    def read_json(self):
        while 1:
            idx = self.buffer.find(b':')
            if idx >= 0:
                break
            # まだ : がない
            self.buffer += sys.stdin.read()
        n = int(self.buffer[:idx].decode())
        self.buffer = self.buffer[idx + 1:]

        while len(self.buffer) < n:
            # n 以上になるまで追加で読む
            self.buffer += sys.stdin.read()

        json_bytes = self.buffer[:n].decode()
        self.buffer = self.buffer[n:]
        obj = json.loads(json_bytes)
        print('RECV', obj)
        return obj

    def handshake(self, name):
        self.send_json({'me': name})
        self.read_json()

    def setup(self, obj):
        self.punter_id = obj['punter']
        self.punters = obj['punters']
        self.map = obj['map']

    def ready(self, state):
        self.send_json({'ready': self.punter_id, 'state': state})

    def recmove(self, move):
        moves = move['move']['moves']
        state = move['state']

        G = []
        for move in moves:
            if 'pass' in move:
                pid = move['pass']['punter']
                G.append((pid, -1, -1))
            else:
                claim = move['claim']
                pid = claim['punter']
                s = claim['source']
                t = claim['target']
                G.append((pid, s, t))
        G.sort()
        return G, state

    def sendmove(self, s, t, state):
        if s == -1 or t == -1:
            self.send_json({'pass': {'punter': self.punter_id}})
        else:
            self.send_json({'claim': {'punter': self.punter_id, 'source': s, 'target': t}})
        self.send_json({'state': state})


class Process:
    def __init__(self, cmd):
        self.cmdline = shlex.split(cmd)
        self.state = ''

    def exec(self, input):
        proc = subprocess.Popen(self.cmdline, stdout=subprocess.PIPE, stdin=subprocess.PIPE)
        stdout = proc.communicate(input=input.encode())[0]
        return stdout.decode().rstrip()

    def start(self):
        return self.exec('?\n')

    def I(self, num_players, punter_id, ma):
        n = len(ma['sites'])
        m = len(ma['rivers'])
        k = len(ma['mines'])

        S = []
        T = []
        for r in ma['rivers']:
            s = r['source']
            t = r['target']
            S.append(s)
            T.append(t)
        M = ma['mines']

        lines = ['I', ' '.join(map(str, [num_players, punter_id])), ' '.join(map(str, [n, m, k]))]
        for i in range(m):
            lines.append(' '.join(map(str, [S[i], T[i]])))
        lines.append(' '.join(map(str, M)))
        txt = '\n'.join(lines)
        self.state = self.exec(txt)

    def G(self, G):
        lines = ['G', self.state]
        for g in G:
            lines.append(' '.join(map(str, g[1:])))
        txt = '\n'.join(lines)
        recv = self.exec(txt)
        l1, l2 = recv.split('\n')
        s, t = map(int, l2.split(' '))
        state = l1
        return state, s, t


def main():
    cmd = sys.argv[1]
    proc = Process(cmd)

    bridge = Bridge()

    obj = bridge.read_json()

    if 'punter' in obj:
        # Setup
        bridge.setup(obj)
        proc.I(bridge.punters, bridge.punter_id, bridge.map)
        bridge.ready(proc.state)
    elif 'move' in obj:
        # Gameplay
        G, state = bridge.recmove(obj)
        state, s, t = proc.G(G)
        bridge.sendmove(s, t, state)
    elif 'stop' in obj:
        # Scoreing
        pass


if __name__ == '__main__':
    main()
