import sys
import telnetlib
import json
import subprocess
import shlex


class OnlineBridge:
    def __init__(self, host, port):
        self.telnet = telnetlib.Telnet(host, port)
        self.buffer = b''

    def close(self):
        self.telnet.close()

    def send_json(self, obj):
        d = json.dumps(obj)
        print('SEND', d)
        bytes = d.encode()
        n = len(bytes)
        self.telnet.write(str(n).encode())
        self.telnet.write(':'.encode())
        self.telnet.write(bytes)

    def read_json(self):
        while 1:
            idx = self.buffer.find(b':')
            if idx >= 0:
                break
            # まだ : がない
            self.buffer += self.telnet.read_some()
        n = int(self.buffer[:idx].decode())
        self.buffer = self.buffer[idx + 1:]

        while len(self.buffer) < n:
            # n 以上になるまで追加で読む
            self.buffer += self.telnet.read_some()

        json_bytes = self.buffer[:n].decode()
        self.buffer = self.buffer[n:]
        obj = json.loads(json_bytes)
        print('RECV', obj)
        return obj

    def handshake(self, name):
        self.send_json({'me': name})
        self.read_json()

    def setup(self):
        sup = self.read_json()
        self.punter_id = sup['punter']
        self.punters = sup['punters']
        self.map = sup['map']

    def ready(self):
        self.send_json({'ready': self.punter_id})

    def recmove(self):
        move = self.read_json()
        if 'stop' in move:
            print(move)
            return None
        moves = move['move']['moves']
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
        return G

    def sendmove(self, s, t):
        if s == -1 or t == -1:
            self.send_json({'pass': {'punter': self.punter_id}})
        else:
            self.send_json({'claim': {'punter': self.punter_id, 'source': s, 'target': t}})


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
        self.state = l1
        return s, t


def main():
    host = 'punter.inf.ed.ac.uk'
    cmd = sys.argv[1]
    port = int(sys.argv[2])

    proc = Process(cmd)
    name = proc.start()

    bridge = OnlineBridge(host, port)
    bridge.handshake(name)
    bridge.setup()

    proc.I(bridge.punters, bridge.punter_id, bridge.map)
    bridge.ready()

    while 1:
        G = bridge.recmove()
        if G is None:
            break
        s, t = proc.G(G)
        bridge.sendmove(s, t)
    bridge.close()


if __name__ == '__main__':
    main()
