import sys
import json
import subprocess
import shlex


def compress(sorted_site_ids):
    """
    >>> conv = compress([1, 3, 6, 10])
    >>> conv
    {1: 0, 3: 1, 6: 2, 10: 3}
    """
    return {sid: i for i, sid in enumerate(sorted_site_ids)}


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

    def ready(self, state, sorted_site_ids):
        self.send_json({'ready': self.punter_id, 'state': [state, sorted_site_ids]})

    def recmove(self, move):
        moves = move['move']['moves']
        state, sorted_site_ids = move['state']

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
        return G, state, sorted_site_ids

    def sendmove(self, s, t, state, sorted_site_ids):
        if s == -1 or t == -1:
            self.send_json({'pass': {'punter': self.punter_id}})
        else:
            self.send_json({'claim': {'punter': self.punter_id, 'source': s, 'target': t}})
        self.send_json({'state': [state, sorted_site_ids]})


class Process:
    def __init__(self, cmd):
        self.cmdline = shlex.split(cmd)

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

        site_ids = []
        for site in ma['sites']:
            site_ids.append(site['id'])
        site_ids.sort()
        conv = compress(site_ids)

        S = []
        T = []
        for r in ma['rivers']:
            s = conv[r['source']]
            t = conv[r['target']]
            S.append(s)
            T.append(t)
        M = [conv[m] for m in ma['mines']]

        lines = ['I', ' '.join(map(str, [num_players, punter_id])), ' '.join(map(str, [n, m, k]))]
        for i in range(m):
            lines.append(' '.join(map(str, [S[i], T[i]])))
        lines.append(' '.join(map(str, M)))
        txt = '\n'.join(lines)
        sorted_site_ids = site_ids
        return self.exec(txt), sorted_site_ids

    def G(self, G, state, sorted_site_ids):
        conv = compress(sorted_site_ids)
        lines = ['G', state]
        for g in G:
            if g[1] == -1:
                lines.append('-1 -1')
            else:
                lines.append(' '.join(map(str, [conv[g[1]], conv[g[2]]])))
        txt = '\n'.join(lines)
        recv = self.exec(txt)
        l1, l2 = recv.split('\n')
        s, t = map(int, l2.split(' '))
        if s != -1:
            s = sorted_site_ids[s]
            t = sorted_site_ids[t]
        new_state = l1
        return s, t, new_state


def main():
    cmd = sys.argv[1]
    proc = Process(cmd)

    bridge = Bridge()

    obj = bridge.read_json()

    if 'punter' in obj:
        # Setup
        bridge.setup(obj)
        state, sorted_site_ids = proc.I(bridge.punters, bridge.punter_id, bridge.map)
        bridge.ready(state, sorted_site_ids)
    elif 'move' in obj:
        # Gameplay
        G, state, sorted_site_ids = bridge.recmove(obj)
        s, t, state = proc.G(G, state, sorted_site_ids)
        bridge.sendmove(s, t, state, sorted_site_ids)
    elif 'stop' in obj:
        # Scoreing
        pass


if __name__ == '__main__':
    main()
